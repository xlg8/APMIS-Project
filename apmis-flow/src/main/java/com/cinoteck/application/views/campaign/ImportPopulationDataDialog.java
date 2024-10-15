package com.cinoteck.application.views.campaign;

import java.io.ByteArrayInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.FileUploader;
import com.cinoteck.application.views.utils.importutils.PopulationDataDryRunner;
import com.cinoteck.application.views.utils.importutils.PopulationDataImporter;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserDto;

public class ImportPopulationDataDialog extends Dialog {

//	ComboBox<CampaignReferenceDto> campaignFilter = new ComboBox<>();
	Button downloadImportTemplate = new Button(I18nProperties.getCaption(Captions.importDownloadImportTemplate));

	Button startDataImport = new Button(I18nProperties.getCaption(Captions.importImportData));
	Button startDryRunImport = new Button(I18nProperties.getCaption(Captions.importImportData) + " Dry Run");

	public Button donloadErrorReport = new Button(I18nProperties.getCaption(Captions.importDownloadErrorReport));
//	ComboBox valueSeperator = new ComboBox<>();
	private boolean callbackRunning = false;
	private Timer timer;
//	private int pollCounter = 0;
	FileUploader buffer = new FileUploader();
	Upload upload = new Upload(buffer);
	private File file_;
	public Checkbox overWriteExistingData = new Checkbox(
			I18nProperties.getCaption(Captions.overridaExistingEntriesWithImportedData));
	boolean overWrite = false;
	Span anchorSpan = new Span();
	public Anchor downloadErrorReportButton;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	CampaignForm campaignForm;

	public ImportPopulationDataDialog(InfrastructureType infrastructureType, CampaignDto camapigndto) {
		String dto;
		if (camapigndto != null) {
			dto = camapigndto.getName();
		} else {
			dto = "New Campaign";
		}
		this.setHeaderTitle(I18nProperties.getString(Strings.headingImportPopulationData) + " | " + dto);
//		this.getStyle().set("color" , "#0D6938");

		Hr seperatorr = new Hr();
		seperatorr.getStyle().set("color", " #0D6938");

		VerticalLayout dialog = new VerticalLayout();

		H3 step1 = new H3();
		step1.add("Step 1: Download the Import Template");
		Label lblImportTemplateInfo = new Label(I18nProperties.getString(Strings.infoDownloadCaseImportTemplate));
		Icon downloadButtonnIcon = new Icon(VaadinIcon.DOWNLOAD);

		downloadImportTemplate.setIcon(downloadButtonnIcon);

		downloadImportTemplate.addClickListener(e -> {

			try {

				String templateFilePath;
				String templateFileName;
				String fileNameAddition;
				ImportFacade importFacade = FacadeProvider.getImportFacade();

				templateFilePath = importFacade.getPopulationDataImportTemplateFilePath();
				templateFileName = importFacade.getPopulationDataImportTemplateFileName();
				fileNameAddition = camapigndto.getName().replace(" ", "_") + "_population_data_import_";

				String content = FacadeProvider.getImportFacade().getImportTemplateContent(templateFilePath);

				InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

				// Create a StreamResource
				StreamResource streamResource = new StreamResource(templateFileName, () -> inputStream);

				// Open the StreamResource in browser for download
				streamResource.setContentType("text/csv");
				streamResource.setCacheTime(0); // Disable caching

				// Create an anchor to trigger the download
				Anchor downloadAnchor = new Anchor(streamResource, "Download CSV");
				downloadAnchor.getElement().setAttribute("download", true);
				downloadAnchor.getStyle().set("display", "none");

				step1.add(downloadAnchor);

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

		H3 step2 = new H3();
		step2.add("Step 2: Import CSV File");
		Label lblImportCsvFile = new Label(I18nProperties.getString(Strings.infoImportCsvFile));

		overWriteExistingData.setValue(false);
		overWriteExistingData.addValueChangeListener(e -> {
			overWrite = e.getValue();
		});

		Label sd = new Label("Upload");

//		MemoryBuffer memoryBuffer = new MemoryBuffer();

		startDataImport.setVisible(false);
		startDryRunImport.setVisible(false);

		upload.setAcceptedFileTypes("text/csv");

		upload.addSucceededListener(event -> {

			file_ = new File(buffer.getFilename());

			startDryRunImport.setVisible(true);

			startDataImport.setVisible(false);

		});

		UserProvider usr = new UserProvider();
		UserDto srDto = usr.getUser();

		Icon startImportButtonnIcon = new Icon(VaadinIcon.UPLOAD);
		startDataImport.setIcon(startImportButtonnIcon);
		startDataImport.addClickListener(ed -> {
			startIntervalCallback();

			try (FileReader reader = new FileReader(file_)) {
				CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
				// Get headers
				List<String> headerStrings = new ArrayList<>();

				for (String header : parser.getHeaderMap().keySet()) {

					headerStrings.add(header);
					System.out.println("Header: " + header);
				}

				if (headerStrings.contains("TOTAL_AGE_0_4") && headerStrings.contains("TOTAL_AGE_5_10")) {

					try {
						System.out.println("Start import Clicked + 111111111111111111111111111111");

						CampaignDto acmpDto = FacadeProvider.getCampaignFacade().getByUuid(camapigndto.getUuid());

						DataImporter importer = new PopulationDataImporter(file_, srDto, acmpDto, ValueSeparator.COMMA,
								overWrite);
						importer.startImport(this::extendDownloadErrorReportButton, null, false, UI.getCurrent(), true);
					} catch (IOException e) {
						Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
								+ I18nProperties.getString(Strings.messageImportFailed));
					} catch (CsvValidationException e1) {
						// TODO Auto-generated catch block
						Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
								+ I18nProperties.getString(Strings.messageImportFailed));
						e1.printStackTrace();
					} finally {
						UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
						userActivitySummaryDto.setActionModule("Population Data Import");
						userActivitySummaryDto
								.setAction("User Attempted Population Import to " + camapigndto.getName());
						userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
						FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);
					}

				} else {

					Notification notification = Notification.show(
							"Please Check to ensure Columns for Population Age Groups 0-4 and 5-10 are respectively available in the import template.");

					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);

				}
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		});

		Icon startDryRunImportButtonnIcon = new Icon(VaadinIcon.UPLOAD);
		startDryRunImport.setIcon(startImportButtonnIcon);
		startDryRunImport.addClickListener(ed -> {
			startIntervalCallback();

			try (FileReader reader = new FileReader(file_)) {
				CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
				// Get headers
				List<String> headerStrings = new ArrayList<>();
				for (String header : parser.getHeaderMap().keySet()) {
					headerStrings.add(header);
					System.out.println("Header: " + header);
				}
				if (headerStrings.contains("TOTAL_AGE_0_4") && headerStrings.contains("TOTAL_AGE_5_10")) {

					try {

						FacadeProvider.getPopulationDataDryRunFacade().truncateDryRunTable();

					} finally {
						try {
							CampaignDto acmpDto = FacadeProvider.getCampaignFacade().getByUuid(camapigndto.getUuid());
							DataImporter importer = new PopulationDataDryRunner(file_, srDto, acmpDto,
									ValueSeparator.COMMA, overWrite);
							importer.startImport(this::extendDownloadErrorReportButton, null, false, UI.getCurrent(),
									true);
						} catch (IOException e) {
							Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
									+ I18nProperties.getString(Strings.messageImportFailed));
						} catch (CsvValidationException e1) {
							// TODO Auto-generated catch block
							Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
									+ I18nProperties.getString(Strings.messageImportFailed));
							e1.printStackTrace();
						} finally {
							startDataImport.setVisible(true);
							UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
							userActivitySummaryDto.setActionModule("Population Data Dry Run");
							userActivitySummaryDto
									.setAction("User Attempted Population Import Dry run to " + camapigndto.getName());
							userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
							FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);
						}
					}
				} else {

					Notification notification = Notification.show(
							"Please Check to ensure Columns for Population Age Groups 0-4 and 5-10 are respectively available in the import template.");

					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);

				}
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		});

		H3 step3 = new H3();
		step3.add("Step 3: Download Error Report");
		Label lblDnldErrorReport = new Label(I18nProperties.getString(Strings.infoDownloadErrorReport));
		downloadErrorReportButton = new Anchor("beforechange");
		// downloadErrorReportButton.setVisible(false);

		Icon downloadErrorReporttButtonnIcon = new Icon(VaadinIcon.DOWNLOAD);
		donloadErrorReport.setIcon(downloadErrorReporttButtonnIcon);
		donloadErrorReport.setVisible(false);
		donloadErrorReport.addClickListener(e -> {
			// Notification.show("Button clicke to download error
			// "+downloadErrorReportButton.getHref());
			downloadErrorReportButton.getElement().callJsFunction("click");
		});

		anchorSpan.add(downloadErrorReportButton);

		dialog.add(seperatorr, // startButton, stopButton,
//				lblCollectionDateInfo, campaignFilter, lblCollectionDateInfo,
				step1, lblImportTemplateInfo, downloadImportTemplate, step2, lblImportCsvFile, overWriteExistingData,
				upload, startDryRunImport, startDataImport, step3, lblDnldErrorReport, donloadErrorReport, anchorSpan);

		Button doneButton = new Button("Done", e -> {
			close();
			stopIntervalCallback();
			campaignForm.treeGrid.getDataProvider().refreshAll();
			// refreshPage();
		});
		Icon doneButtonIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		doneButton.setIcon(doneButtonIcon);
		getFooter().add(doneButton);

		add(dialog);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);

	}

	private void stopPullers() {
		UI.getCurrent().setPollInterval(-1);
	}

	private void startIntervalCallback() {
		// UI.getCurrent().setPollInterval(300);
		if (!callbackRunning) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
//					stopIntervalCallback();
					pokeFlow();
				}
			}, 1000); // 10 minutes

			callbackRunning = true;
		}
	}

	private void stopIntervalCallback() {
		System.out.println("stopIntervalCallback_________________");
		if (callbackRunning) {
			callbackRunning = false;
			if (timer != null) {
				timer.cancel();
				timer.purge();
			}

		}
	}

	private void pokeFlow() {
		logger.debug("runingImport...");
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

		downloadErrorReportButton = new Anchor(streamResource, ".");// ,
																	// I18nProperties.getCaption(Captions.downloadErrorReport));
																	// I18nProperties.getCaption(Captions.importDownloadErrorReport)
		downloadErrorReportButton.setHref(streamResource);
		downloadErrorReportButton.setClassName("vaadin-button");

		anchorSpan.add(downloadErrorReportButton);

	}

}