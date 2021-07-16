package de.symeda.sormas.backend.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.util.List;

import javax.persistence.Query;

import org.junit.Test;

import de.symeda.sormas.backend.JpaBaseTest;

public class HistoryTablesSchemaTest extends JpaBaseTest {

	@Test
	public void testIfSchemaIsInitialized() {

//		em.getTransaction().begin();
//		em.getTransaction().commit();

		Query createNativeQuery = em.createNativeQuery("SELECT * FROM cases_history");
		List<Object> resultList = createNativeQuery.getResultList();

		assertThat(resultList, is(empty()));
	}
}
