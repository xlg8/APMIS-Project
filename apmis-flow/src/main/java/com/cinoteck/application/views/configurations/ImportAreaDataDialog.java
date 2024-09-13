package com.cinoteck.application.views.configurations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaigndata.CampaignFormDataImporter;
import com.cinoteck.application.views.utils.IdleNotification;
import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.FileUploader;
import com.cinoteck.application.views.utils.importutils.ImportProgressLayout;
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
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;

public class ImportAreaDataDialog extends Dialog {

	Button downloadImportTemplate = new Button(I18nProperties.getCaption(Captions.importDownloadImportTemplate));
	Button startDataImport = new Button(I18nProperties.getCaption(Captions.importImportData));
	public Button donloadErrorReport = new Button(I18nProperties.getCaption(Captions.importDownloadErrorReport));
	Button startImportDryRun = new Button(I18nProperties.getCaption(Captions.importImportData) + " Dry Run");

	ComboBox valueSeperator = new ComboBox<>();
	private boolean callbackRunning = false;
	private Timer timer;
	private int pollCounter = 0;
	private File file_;
	public Checkbox overWriteExistingData = new Checkbox(
			I18nProperties.getCaption(Captions.overridaExistingEntriesWithImportedData));
	boolean overWrite = false;

	boolean checkForException = false;

	Span anchorSpan = new Span();
	public Anchor downloadErrorReportButton;

	DataImporter importer = null;
	IdleNotification idleNotification;

	public ImportAreaDataDialog() {

		this.setHeaderTitle(I18nProperties.getString(Strings.regionImportModule));
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

				importFacade.generateAreaImportTemplateFile();

				templateFileName = "APMIS_Region_Import_Template.csv";

				templateFilePath = importFacade.getAreaImportTemplateFilePath();

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

		overWriteExistingData.setValue(false);
		overWriteExistingData.addValueChangeListener(e -> {
			overWrite = e.getValue();
		});

		Label sd = new Label(I18nProperties.getCaption(Captions.upload));

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
			startDataImport.setVisible(true);
//			startImportDryRun.setVisible(true);

		});

		UserProvider usr = new UserProvider();
		UserDto userDto = usr.getUser();
		AreaDto areaDto = new AreaDto();

		startDataImport.addClickListener(ed -> {
			startIntervalCallback();

			try {
				DataImporter importer = new AreaDataImporter(file_, false, userDto, areaDto, ValueSeparator.COMMA,
						overWrite);
				importer.startImport(this::extendDownloadErrorReportButton, null, false, UI.getCurrent(), true);
			} catch (IOException | CsvValidationException e) {
				Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
						+ I18nProperties.getString(Strings.messageImportFailed));
			}

		});

//		startImportDryRun.addClickListener(ed -> {
//			startIntervalCallback();
//			
//			try {
//				truncateDryRunTable();
//				
//				
//			} finally {
//
//				try {
//					importer = new AreaDataDryRunner(file_, false, userDto, areaDto, ValueSeparator.COMMA, overWrite);
//					importer.startDryRunImport(this::extendDownloadErrorReportButton, null, false, UI.getCurrent(),
//							true);
//				} catch (IOException | CsvValidationException e) {
//					checkForException = true;
//					Notification.show(I18nProperties.getString(Strings.headingImportFailed) + " : "
//							+ I18nProperties.getString(Strings.messageImportFailed));
//				} finally {
//
//					startDataImport.setVisible(true);
//					boolean hasErrors = checkForException || (importer != null && importer.hasErrors());
//					ImportProgressLayout progressLayout;
//					try {
//						progressLayout = importer.getImportProgressLayout(UI.getCurrent(), true);
//
//						if (importer != null) {
//							progressLayout.makeClosable(() -> {
//								if (!checkForException && !importer.hasErrors()) {
//									startDataImport.setVisible(true);
//								}
//							});
//						}
//					} catch (CsvValidationException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} catch (IOException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					;
//					if (!checkForException && importer != null && !importer.hasErrors()) {
//						startDataImport.setVisible(true);
//					}
//
//					if (!checkForException) {
//
//						progressLayout = null;
//						try {
//							progressLayout = importer.getImportProgressLayout(UI.getCurrent(), true);
//						} catch (CsvValidationException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						} catch (IOException e1) {
//							// TODO Auto-generated catch block
//							e1.printStackTrace();
//						}
//
//				
//						boolean exceptionValue = progressLayout.hasImportErrors();
//
//						if (!exceptionValue) {
//							startDataImport.setVisible(true);
//						}
//
//					}
//				}
//			}
//
//		});

		downloadErrorReportButton = new Anchor("beforechange");

		Icon downloadErrorButtonIcon = new Icon(VaadinIcon.DOWNLOAD);

		H3 step5 = new H3();
		step5.add(I18nProperties.getString(Strings.step3));
		Label lblDnldErrorReport = new Label(I18nProperties.getString(Strings.infoDownloadErrorReport));

		donloadErrorReport.setVisible(false);
		donloadErrorReport.setIcon(downloadErrorButtonIcon);
		donloadErrorReport.addClickListener(e -> {
			Notification.show("Button clicked to download error " + downloadErrorReportButton.getHref());

			downloadErrorReportButton.getElement().callJsFunction("click");
		});

		
		anchorSpan.add(downloadErrorReportButton);
		
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

		Button doneButton = new Button(I18nProperties.getCaption(Captions.done), e -> {
//			UI.getCurrent().getPage().executeJs("window.inactivityHandler.stopTimer();");
//			UI.getCurrent().getPage().executeJs("window.inactivityHandler.resetTimer($0);", 60000);
			close();			
//			UI.getCurrent().getPage().executeJs("window.inactivityHandler.startInactivityTimer(); "
//					+ "console.log(\"Time out function Reset-------------------------\");");
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
//					stopIntervalCallback();
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
			FacadeProvider.getAreaDryRunFacade().clearDryRunTable();

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

}