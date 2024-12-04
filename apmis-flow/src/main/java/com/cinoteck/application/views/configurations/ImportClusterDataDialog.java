package com.cinoteck.application.views.configurations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaigndata.CampaignFormDataImporter;
import com.cinoteck.application.views.utils.IdleNotification;
import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.FileUploader;
import com.cinoteck.application.views.utils.importutils.ImportProgressLayout;
import com.cinoteck.application.views.utils.importutils.PopulationDataImporter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.CSVCommentLineValidator;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.CharsetHelper;
import de.symeda.sormas.api.utils.DataHelper;

public class ImportClusterDataDialog extends Dialog {

	Button downloadImportTemplate = new Button(I18nProperties.getCaption(Captions.importDownloadImportTemplate));
	Button startDataImport = new Button(I18nProperties.getCaption(Captions.importImportData));
	public Button donloadErrorReport = new Button(I18nProperties.getCaption(Captions.importDownloadErrorReport));
//	public Button donloadUserLodReport = new Button("Download CridentialsButton");
	ComboBox valueSeperator = new ComboBox<>();
	private boolean callbackRunning = false;
	private Timer timer;
	private int pollCounter = 0;
	private File file_;
	public Checkbox overWriteExistingData = new Checkbox(
			I18nProperties.getCaption(Captions.overridaExistingEntriesWithImportedData));
	boolean overWrite = false;

	Span anchorSpan = new Span();
	public Anchor downloadErrorReportButton;
	Button startImportDryRun = new Button(I18nProperties.getCaption(Captions.importImportData) + " Dry Run");
	
	DataImporter importer = null;
	boolean checkForException = false;
	IdleNotification idleNotification;
	
private char csvSeparator;
	
	private final boolean hasEntityClassRow;


	public ImportClusterDataDialog() {

		this.hasEntityClassRow = true;
		this.setHeaderTitle(I18nProperties.getString(Strings.clusterImportModule));
//		this.getStyle().set("color" , "#0D6938");
		
		MainLayout mainLayout = (MainLayout) UI.getCurrent().getSession().getAttribute(MainLayout.class);
		if (mainLayout != null) {
			idleNotification = mainLayout.getIdleNotification();
		}

		Hr seperatorr = new Hr();
		seperatorr.getStyle().set("color", " #0D6938");

		VerticalLayout dialog = new VerticalLayout();

		H3 step2 = new H3();
		step2.add(I18nProperties.getString(Strings.step1));
		Label lblImportTemplateInfo = new Label(I18nProperties.getString(Strings.infoDownloadCaseImportTemplate));

		Icon downloadImportTemplateButtonIcon = new Icon(VaadinIcon.DOWNLOAD);
		downloadImportTemplate.setIcon(downloadImportTemplateButtonIcon);
		downloadImportTemplate.addClickListener(e -> {

			try {

				String templateFilePath;
				String templateFileName;
				ImportFacade importFacade = FacadeProvider.getImportFacade();

				importFacade.generateCommunityImportTemplateFile();

				templateFileName = "Cluster_Import_Template.csv";

				templateFilePath = importFacade.getCommunityImportTemplateFilePath();

				String content = FacadeProvider.getImportFacade().getImportTemplateContent(templateFilePath);

				InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

				// Create a StreamResource
				StreamResource streamResource = new StreamResource(templateFileName, () -> inputStream);

				// Open the StreamResource in browser for download
				streamResource.setContentType("text/csv");
				streamResource.setCacheTime(0); // Disable caching

				// Create an anchor to trigger the download
				Anchor downloadAnchor = new Anchor(streamResource, I18nProperties.getCaption(Captions.downloadCsv));
				downloadAnchor.getElement().setAttribute("download", true);
				downloadAnchor.getStyle().set("display", "none");

				step2.add(downloadAnchor);

				// Simulate a click event on the hidden anchor to trigger the download
				downloadAnchor.getElement().callJsFunction("click");
				Notification.show("downloading...");

			} catch (IOException ioException) {
				ioException.printStackTrace();

				Notification.show(I18nProperties.getString(Strings.headingTemplateNotAvailable) + ": "
						+ I18nProperties.getString(Strings.messageTemplateNotAvailable));

			}

		}

		);

		H3 step3 = new H3();
		step3.add(I18nProperties.getString(Strings.step2));
		Label lblImportCsvFile = new Label(I18nProperties.getString(Strings.infoImportCsvFile));
//		Label lblImportCsvFile = new Label(I18nProperties.getString(Strings.infoImportCsvFile));
		overWriteExistingData.setValue(false);
		overWriteExistingData.addValueChangeListener(e -> {
			overWrite = e.getValue();
		});
		Label sd = new Label(I18nProperties.getCaption(Captions.upload));

//		MemoryBuffer memoryBuffer = new MemoryBuffer();
		FileUploader buffer = new FileUploader();
		Upload upload = new Upload(buffer);

		Icon startImportButtonIcon = new Icon(VaadinIcon.UPLOAD);
		startDataImport.setIcon(startImportButtonIcon);
		startImportDryRun.setIcon(startImportButtonIcon);

		startDataImport.setVisible(false);
		startImportDryRun.setVisible(false);

		upload.setAcceptedFileTypes("text/csv");
		upload.addSucceededListener(event -> {

			file_ = new File(buffer.getFilename());
			startDataImport.setVisible(false);
//			startImportDryRun.setVisible(true);

		});

		UserProvider usr = new UserProvider();
		UserDto userDto = usr.getUser();
//		DistrictDto regionDto = new DistrictDto();
		CommunityDto clusterDto = new CommunityDto();
//		startDataImport.addClickListener(ed -> {
//
//			startIntervalCallback();
//			try {
//
//				//CampaignDto campaignDto = FacadeProvider.getCampaignFacade().getByUuid(campaignFilter.getValue().getUuid());
//				
//				DataImporter importer = new ClusterDataImporter(file_, false, regionDto, ValueSeparator.COMMA, overWrite);
//				
//				
//				importer.startImport(this::extendDownloadErrorReportButton, null, true, UI.getCurrent(), true);
//			} catch (IOException | CsvValidationException e) {
//				Notification.show(
//					I18nProperties.getString(Strings.headingImportFailed) +" : "+
//					I18nProperties.getString(Strings.messageImportFailed));
//			}
//			
//			
//		});

		startDataImport.addClickListener(ed -> {
			startIntervalCallback();
			List<String> cCodeColumnValues = new ArrayList<>();
			List<String> dCodeColumnValues = new ArrayList<>();

			try {
				ClusterDataImporter importer = new ClusterDataImporter(file_, false, clusterDto, ValueSeparator.COMMA,
						overWrite);

				// Trying to read the csv file to extract values of specific columns
				// Eventual Goal is to finally get the list of all clusters from the db by their
				// districts
				// upon retrieval i'd be archiving any cluster external id that is not amongst
				// the cCodeColumnValues

				// Forseeable problem: If we are getting the list at this point the result of
				// the imports is not being considered at this point

				cCodeColumnValues = importer.extractColumnValues("CCode"); // replace "Column_Name" with the
				dCodeColumnValues = importer.extractColumnValues("DCode"); // replace "Column_Name" with the

				// actual column name
				importer.startImport(file_ ,this::extendDownloadErrorReportButton, null, true, UI.getCurrent(), true);

				// Process the column values as needed
//				columnValues.forEach(System.out::println);
//				System.out.println(columnValues + "Column values ");

			} catch (IOException | CsvValidationException e) {
				Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
						+ I18nProperties.getString(Strings.messageImportFailed));
			} finally {

				System.out.println(dCodeColumnValues + "Column values " + cCodeColumnValues);

			}
		});
		
		startImportDryRun.addClickListener(ed -> {
			startIntervalCallback();
			
			try {
				truncateDryRunTable();

			} finally {

				try {
					importer = new ClusterDataDryRunner(file_, false, clusterDto, ValueSeparator.COMMA, overWrite);
					importer.startDryRunImport(file_ ,this::extendDownloadErrorReportButton, null, false, UI.getCurrent(),
							true);
				} catch (IOException | CsvValidationException e) {
					checkForException = true;
					Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
							+ I18nProperties.getString(Strings.messageImportFailed));
				} finally {

					startDataImport.setVisible(true);
			

				}
			}
		});

		downloadErrorReportButton = new Anchor("beforechange");
//		downloadCredntialsReportButton = new Anchor("beforechange");
		// downloadErrorReportButton.setVisible(false);

		Icon downloadErrorButtonIcon = new Icon(VaadinIcon.DOWNLOAD);
//		donloadUserLodReport.setIcon(downloadErrorButtonIcon);
//		donloadUserLodReport.setVisible(false);
//		donloadUserLodReport.addClickListener(e -> {
//			Notification.show("Button clicke to download error "+downloadCredntialsReportButton.getHref());
//			
//			downloadCredntialsReportButton.getElement().callJsFunction("click");
//		});

		H3 step5 = new H3();
		step5.add(I18nProperties.getString(Strings.step3));
		Label lblDnldErrorReport = new Label(I18nProperties.getString(Strings.infoDownloadErrorReport));
//		downloadErrorReportButton = new Anchor("beforechange");
//		downloadCredntialsReportButton = new Anchor("beforechange");
		// downloadErrorReportButton.setVisible(false);
		donloadErrorReport.setVisible(false);
		donloadErrorReport.setIcon(downloadErrorButtonIcon);
		donloadErrorReport.addClickListener(e -> {
			Notification.show("Button clicked to download error " + downloadErrorReportButton.getHref());

			downloadErrorReportButton.getElement().callJsFunction("click");
		});

		anchorSpan.add(downloadErrorReportButton);
//		anchorSpanCredential.add(downloadCredntialsReportButton);

//		anchorSpan.setVisible(false);
//		Button startButton = new Button("Start Interval__ Callback");
//		startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//		startButton.setId("pokers");
//		startButton.addClickListener(e -> {
//			startIntervalCallback();
//		});

//		startIntervalCallback();

//		Button stopButton = new Button("Stop Interval Callback");
//		stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//		stopButton.addClickListener(e -> stopIntervalCallback());
		startIntervalCallback();
		UI.getCurrent().addPollListener(event -> {
			if (callbackRunning) {
				UI.getCurrent().access(this::pokeFlow);
			} else {
				stopPullers();
			}
		});

		dialog.add(step2, lblImportTemplateInfo, downloadImportTemplate, step3, lblImportCsvFile, overWriteExistingData,
				upload, startImportDryRun, startDataImport, step5, lblDnldErrorReport, donloadErrorReport, anchorSpan);

		// hacky: hide the anchor
		anchorSpan.getStyle().set("display", "none");
//		anchorSpanCredential.getStyle().set("display", "none");

		Button doneButton = new Button(I18nProperties.getCaption(Captions.done), e -> {
			close();
			stopIntervalCallback();

		});
		Icon doneButtonIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);

		doneButton.setIcon(doneButtonIcon);
		getFooter().add(doneButton);

		add(dialog);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);

	}

	private void pokeFlow() {
		if (idleNotification.getSecondsBeforeNotification() < 121) {
			idleNotification.setSecondsBeforeNotification(200);
		}
	}

	private void startIntervalCallback() {
		UI.getCurrent().setPollInterval(5000);
		if (!callbackRunning) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					stopIntervalCallback();
				}
			}, 10000); // 10 minutes

			callbackRunning = true;
		}
	}

	private void stopIntervalCallback() {
		if (callbackRunning) {
			callbackRunning = false;
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}

		}
	}
	
	private void truncateDryRunTable() {
		try {
			FacadeProvider.getCommunityDryRunFacade().clearDryRunTable();

		} catch (Exception e) {

		}
	}

	private void stopPullers() {
		UI.getCurrent().setPollInterval(-1);
	}

	private void refreshPage() {
		// Get the current UI
		UI ui = UI.getCurrent();

		// Get the current page and reload it
		Page page = ui.getPage();
		page.reload();
	}

	protected void resetDownloadErrorReportButton() {
		downloadErrorReportButton.removeAll();
		downloadErrorReportButton.setVisible(false);
	}

	public void extendDownloadErrorReportButton(StreamResource streamResource) {
		anchorSpan.remove(downloadErrorReportButton);
		donloadErrorReport.setVisible(true);

		downloadErrorReportButton = new Anchor(streamResource, ".");
		downloadErrorReportButton.setHref(streamResource);
		downloadErrorReportButton.setClassName("vaadin-button");

		anchorSpan.add(downloadErrorReportButton);

	}
	
	
	private CSVReader getCSVReader(File inputFile) throws IOException {
		CharsetDecoder decoder = CharsetHelper.getDecoder(inputFile);
		InputStream inputStream = Files.newInputStream(inputFile.toPath());
		BOMInputStream bomInputStream = new BOMInputStream(inputStream);
		Reader reader = new InputStreamReader(bomInputStream, decoder);
		BufferedReader bufferedReader = new BufferedReader(reader);
		return CSVUtils.createCSVReader(bufferedReader, this.csvSeparator, new CSVCommentLineValidator());
	}
	
	private String[] readNextValidLine(CSVReader csvReader) throws IOException, CsvValidationException {
		String[] nextValidLine = null;
		boolean isCommentLine;

		do {
			try {
				nextValidLine = csvReader.readNext();
				isCommentLine = false;
			} catch (CsvValidationException e) {
				if (StringUtils.contains(e.getMessage(), CSVCommentLineValidator.ERROR_MESSAGE)) {
//					logger.debug("Found comment line. Skipping it");
					csvReader.skip(1);
					isCommentLine = true;
				} else {
					throw e;
				}
			}
		} while (isCommentLine);
		return nextValidLine;
	}

	protected int readImportFileLength(File inputFile) throws IOException, CsvValidationException {
		int importFileLength = 0;
		try (CSVReader caseCountReader = getCSVReader(inputFile)) {
			while (readNextValidLine(caseCountReader) != null) {
				importFileLength++;
			}
			// subtract header line(s)
			importFileLength--;
			if (hasEntityClassRow) {
				importFileLength--;
			}
		}

		return importFileLength;
	}

}