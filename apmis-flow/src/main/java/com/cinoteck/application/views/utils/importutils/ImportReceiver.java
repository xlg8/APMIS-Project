package com.cinoteck.application.views.utils.importutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.StartedEvent;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;

import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletRequest;



import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.importexport.ImportExportUtils;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

@SuppressWarnings("serial")
public class ImportReceiver extends Upload implements Receiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private File file;
	private String fileNameAddition;
	Consumer<File> fileConsumer;

	public ImportReceiver(String fileNameAddition, Consumer<File> fileConsumer) {
		this.fileNameAddition = fileNameAddition;
		this.fileConsumer = fileConsumer;
	}

	@Override
	public OutputStream receiveUpload(String fileName, String mimeType) {
		// Reject empty files
		if (fileName == null || fileName.isEmpty()) {
			file = null;
			Notification.show(
				I18nProperties.getString(Strings.headingNoFile) +" : "+
				I18nProperties.getString(Strings.messageNoCsvFile));
			// Workaround because returning null here throws an uncatchable UploadException
			return new ByteArrayOutputStream();
		}
		// Reject all files except .csv files - we also need to accept excel files here
		if (!(mimeType.equals("text/csv") || mimeType.equals("application/vnd.ms-excel"))) {
			file = null;
			Notification.show(
				I18nProperties.getString(Strings.headingWrongFileType) +" : "+
				I18nProperties.getString(Strings.messageWrongFileType));
			// Workaround because returning null here throws an uncatchable UploadException
			return new ByteArrayOutputStream();
		}

		final FileOutputStream fos;
		try {
			Path tempDirectory = Paths.get(FacadeProvider.getConfigFacade().getTempFilesPath());
			if (!tempDirectory.toFile().exists() || !tempDirectory.toFile().canWrite()) {
				throw new FileNotFoundException("Temp directory doesn't exist or cannot be accessed");
			}
			String newFileName = ImportExportUtils.TEMP_FILE_PREFIX + fileNameAddition + DateHelper.formatDateForExport(new Date()) + "_"
				+ DataHelper.getShortUuid(UserProvider.getCurrent().getUuid()) + ".csv";
			file = new File(tempDirectory.resolve(newFileName).toString());
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			file = null;
			logger.error("Reading the file to import failed", e);
			Notification.show(
				I18nProperties.getString(Strings.headingImportError) +" : "+
				I18nProperties.getString(Strings.messageImportError));
			// Workaround because returning null here throws an uncatchable UploadException
			return new ByteArrayOutputStream();
		}

		return fos;
	}

	public void uploadStarted(StartedEvent startedEvent) {
		long fileSizeLimitMb = FacadeProvider.getConfigFacade().getImportFileSizeLimitMb();
		if (startedEvent.getContentLength() > fileSizeLimitMb * 1_000_000) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.fileTooBig, fileSizeLimitMb));
		}
	}

	public void uploadSucceeded(SucceededEvent event) {

		if (file == null) {
			return;
		}

		try {
			// Read file and create readers
			File csvFile = new File(file.getPath());
			if (!csvFile.exists()) {
				throw new FileNotFoundException("CSV file does not exist");
			}

			fileConsumer.accept(csvFile);
		} catch (IOException e) {
			Notification.show(
				I18nProperties.getString(Strings.headingImportFailed)+" : "+
				I18nProperties.getString(Strings.messageImportFailed)
				);
		}
	}
	 
	    
}
