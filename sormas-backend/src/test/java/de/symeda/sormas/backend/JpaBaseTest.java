package de.symeda.sormas.backend;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;
import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Creates an in-memory database with the {@code sormas_schema.sql}.
 */
public class JpaBaseTest {

	protected static EntityManagerFactory emf;
	protected static EntityManager em;

	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {

		emf = Persistence.createEntityManagerFactory("jpaTestPU");
		em = emf.createEntityManager();
	}

//	@Before
//	public void initializeDatabase() {
//
//		Session session = em.unwrap(Session.class);
//		session.doWork(connection -> {
//			try {
//				File script = new File(getClass().getResource("/sql/sormas_schema.sql").getFile());
//				Reader reader = new FileReader(script);
//
//				byte[] bytes = Files.readAllBytes(new File(getClass().getResource("/sql/sormas_schema2.sql").getFile()).toPath());
//
//				String originalScript = new String(bytes, StandardCharsets.UTF_8);
//				System.out.println(originalScript);
//				String sanitizedScript = originalScript;
//
//				RunScript.execute(connection, new StringReader(sanitizedScript));
//			} catch (IOException e) {
//				throw new UncheckedIOException("could not initialize with script", e);
//			}
//		});
//	}

	@AfterClass
	public static void tearDown() {

		em.clear();
		em.close();
		emf.close();
	}

	@Test
	public void testReduceScript() {

		Session session = em.unwrap(Session.class);
		session.doWork(connection -> {
			try {
				File script = new File(getClass().getResource("/sql/sormas_schema.sql").getFile());
				Reader reader = new FileReader(script);

				byte[] bytes = Files.readAllBytes(new File(getClass().getResource("/sql/sormas_schema.sql").getFile()).toPath());

				String originalScript = new String(bytes, StandardCharsets.UTF_8);
//				System.out.println(originalScript);
				String sanitizedScript = originalScript;
				sanitizedScript = sanitizedScript.replaceAll("WITH\\s+\\(\\s+OIDS\\=FALSE\\s+\\)", "");
				sanitizedScript = sanitizedScript.replaceAll("ALTER TABLE \\w* OWNER TO sormas_user;", "");
				sanitizedScript = sanitizedScript.replaceAll("\\nSET\\s.*;", "");
				sanitizedScript = sanitizedScript.replaceAll("ALTER TABLE ONLY", "ALTER TABLE");
//				sanitizedScript = sanitizedScript.replaceAll("\\nALTER TABLE \\w* RENAME", "\\nALTER TABLE \\w* RENAME COLUMN");

				RunScript.execute(connection, new StringReader(sanitizedScript));
			} catch (IOException e) {
				throw new UncheckedIOException("could not initialize with script", e);
			}
		});
	}

	@Test
	public void developRegex() {

		{
			String originalScript = "ALTER TABLE sormas_version OWNER TO sormas_user;";
			String sanitizedScript = originalScript.replaceAll("ALTER TABLE \\w+ OWNER TO sormas_user;", "");
			assertThat(sanitizedScript, equalTo(StringUtils.EMPTY));
		}

		{
			String originalScript = "SET statement_timeout = 0;";
			String sanitizedScript = originalScript.replaceAll("SET\\s.*;", "");
			assertThat(sanitizedScript, equalTo(StringUtils.EMPTY));
		}

		{
			String originalScript = "WITH (\r\n  OIDS=FALSE\r\n)";
			String sanitizedScript = originalScript.replaceAll("WITH\\s+\\(\\s+OIDS\\=FALSE\\s+\\)", "");
			assertThat(sanitizedScript, equalTo(StringUtils.EMPTY));
		}

		// FIXME #6121: How to fix this?
		{
			String originalScript = "\nALTER TABLE cases RENAME columnName";
			String sanitizedScript = originalScript.replaceAll("\\nALTER TABLE \\w* RENAME", "\nALTER TABLE \\w* RENAME COLUMN");
			assertThat(sanitizedScript, equalTo("\nALTER TABLE cases RENAME COLUMN columnName"));
		}
	}
}
