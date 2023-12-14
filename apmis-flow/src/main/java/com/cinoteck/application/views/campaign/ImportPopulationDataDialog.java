package com.cinoteck.application.views.campaign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.importutils.DataImporter;
import com.cinoteck.application.views.utils.importutils.FileUploader;
import com.cinoteck.application.views.utils.importutils.PopulationDataImporter;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.importexport.ValueSeparator;
import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.user.UserDto;

public class ImportPopulationDataDialog extends Dialog {

//	ComboBox<CampaignReferenceDto> campaignFilter = new ComboBox<>();
	Button downloadImportTemplate = new Button(I18nProperties.getCaption(Captions.importDownloadImportTemplate));
	Button downloadDefaultPopulationTemplate = new Button(I18nProperties.getCaption(Captions.importDefaultPopulationFile));

	Button startDataImport = new Button(I18nProperties.getCaption(Captions.importImportData));
	public Button donloadErrorReport = new Button(I18nProperties.getCaption(Captions.importDownloadErrorReport));
//	ComboBox valueSeperator = new ComboBox<>();
//	private boolean callbackRunning = false;
//	private Timer timer;
//	private int pollCounter = 0;
	FileUploader buffer = new FileUploader();  
    Upload upload = new Upload(buffer);
	private File file_;
	Span anchorSpan = new Span();
	public Anchor downloadErrorReportButton;
	
	@Autowired
	CampaignForm campaignForm;
	
	public ImportPopulationDataDialog(InfrastructureType infrastructureType, CampaignDto camapigndto) {
		String dto;
		if (camapigndto != null) {
			 dto = camapigndto.getName();
		}else {
			 dto = "New Campaign";
		}
		this.setHeaderTitle(I18nProperties.getString(Strings.headingImportPopulationData) + " | "+ dto );
//		this.getStyle().set("color" , "#0D6938");

		Hr seperatorr = new Hr();
		seperatorr.getStyle().set("color", " #0D6938");

		VerticalLayout dialog = new VerticalLayout();



		H3 step1 = new H3();
		step1.add("Step 1: Download the Import Template");
		Label lblImportTemplateInfo = new Label(I18nProperties.getString(Strings.infoDownloadCaseImportTemplate));
		Icon downloadButtonnIcon = new Icon(VaadinIcon.DOWNLOAD);
		
		downloadDefaultPopulationTemplate.setIcon(downloadButtonnIcon);
		downloadImportTemplate.setIcon(downloadButtonnIcon);
		downloadDefaultPopulationTemplate.addClickListener(ee -> {
			StreamResource streamResource = new StreamResource("default_population_data.csv", () -> {
			    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("default_population_data.csv");
			    if (inputStream != null) {
			        return inputStream;
			    } else {
			        // Handle error, e.g., show a notification
			        Notification.show("CSV file not found");
			        return null;
			    }
			});

			// Create a StreamResource
//				StreamResource streamResource = new StreamResource(templateFileName, () -> inputStream);

			// Open the StreamResource in browser for download
			streamResource.setContentType("text/csv");
			streamResource.setCacheTime(0); // Disable caching

			// Create an anchor to trigger the download
			Anchor downloadAnchorx = new Anchor(streamResource, "Download CSV");
			downloadAnchorx.getElement().setAttribute("download", true);
			downloadAnchorx.getStyle().set("display", "none");

			step1.add(downloadAnchorx);

			// Simulate a click event on the hidden anchor to trigger the download
			downloadAnchorx.getElement().callJsFunction("click");
			Notification.show("downloading...");
			
			 UI.getCurrent().getPage().executeJs(
				        "const downloadAnchor = $0;" +
				        "downloadAnchor.addEventListener('load', function() {" +
				        "    setTimeout(function() {" +
				        "        $1.click();" +
				        "    }, 1000);" + // Trigger upload after a 1-second delay
				        "});",
				        downloadAnchorx.getElement(), // $0 represents the anchor
				        upload.getElement() // $1 represents the upload element
				    );


		});
		
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
		Label sd = new Label("Upload");
		
//		MemoryBuffer memoryBuffer = new MemoryBuffer();
		
        
        startDataImport.setVisible(false);
        upload.setAcceptedFileTypes("text/csv");
        
        upload.addSucceededListener(event -> {
        	
        	file_ = new File(buffer.getFilename());
			 startDataImport.setVisible(true);
        	
        });
		
        UserProvider usr = new UserProvider();
		UserDto srDto = usr.getUser();
		
		
		Icon startImportButtonnIcon = new Icon(VaadinIcon.UPLOAD);
		startDataImport.setIcon(startImportButtonnIcon);
		startDataImport.addClickListener(ed -> {

			
			try {

				CampaignDto acmpDto = FacadeProvider.getCampaignFacade().getByUuid(camapigndto.getUuid());
				
				DataImporter importer = new PopulationDataImporter(file_, srDto, acmpDto, ValueSeparator.COMMA);
				importer.startImport(this::extendDownloadErrorReportButton, null, false, UI.getCurrent(), true);
			} catch (IOException | CsvValidationException e) {
				Notification.show(
					I18nProperties.getString(Strings.headingImportFailed) +" : "+
					I18nProperties.getString(Strings.messageImportFailed));
			}
			
			
		});

		H3 step3 = new H3();
		step3.add("Step 3: Download Error Report");
		Label lblDnldErrorReport = new Label(I18nProperties.getString(Strings.infoDownloadErrorReport));
		downloadErrorReportButton = new Anchor("beforechange");
		//downloadErrorReportButton.setVisible(false);
		
		Icon downloadErrorReporttButtonnIcon = new Icon(VaadinIcon.DOWNLOAD);
		donloadErrorReport.setIcon(downloadErrorReporttButtonnIcon);
		donloadErrorReport.setVisible(false);
		donloadErrorReport.addClickListener(e -> {
		//	Notification.show("Button clicke to download error "+downloadErrorReportButton.getHref());
		downloadErrorReportButton.getElement().callJsFunction("click");
		});
		
		anchorSpan.add(downloadErrorReportButton);
//		anchorSpan.setVisible(false);
//		Button startButton = new Button("Start Interval__ Callback");
//		startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//		startButton.setId("pokers");
//		startButton.addClickListener(e -> {
//			startIntervalCallback();
//		});
		
	//	startIntervalCallback();

//		Button stopButton = new Button("Stop Interval Callback");
//		stopButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//		stopButton.addClickListener(e -> stopIntervalCallback());

		dialog.add(seperatorr, //startButton, stopButton,
//				lblCollectionDateInfo, campaignFilter, lblCollectionDateInfo,
				step1, lblImportTemplateInfo, downloadImportTemplate,downloadDefaultPopulationTemplate, step2, lblImportCsvFile, upload, startDataImport, step3,
				lblDnldErrorReport, donloadErrorReport, anchorSpan);

		Button doneButton = new Button("Done", e -> {
			close();
		//	stopIntervalCallback();
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
//
//	private void pokeFlow() {
//	//	Notification.show("dialog detected... User wont logout");
//	}

//	private void startIntervalCallback() {
//		UI.getCurrent().setPollInterval(5000);
//		if (!callbackRunning) {
//			timer = new Timer();
//			timer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					stopIntervalCallback();
//				}
//			}, 15000); // 10 minutes
//
//			callbackRunning = true;
//		}
//	}
//
//	private void stopIntervalCallback() {
//		if (callbackRunning) {
//			callbackRunning = false;
//			if (timer != null) {
//				timer.cancel();
//				timer.purge();
//			}
//
//		}
//	}
	
	
//	
//
//	private void stopPullers() {
//		UI.getCurrent().setPollInterval(-1);
//	}

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

		downloadErrorReportButton = new Anchor(streamResource, ".");//, I18nProperties.getCaption(Captions.downloadErrorReport));   I18nProperties.getCaption(Captions.importDownloadErrorReport)
		downloadErrorReportButton.setHref(streamResource);
		downloadErrorReportButton.setClassName("vaadin-button");
		
		anchorSpan.add(downloadErrorReportButton);
		
	}
	
	
}