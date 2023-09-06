package com.cinoteck.application.views.campaign;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cinoteck.application.views.utils.DownloadUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.server.FileDownloader;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.utils.DataHelper;


public class CampaignDataImportDialog extends Dialog{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3457925733267877650L;
	ComboBox campaignFilter = new ComboBox<>();
	Button downloadImportTemplate = new Button(I18nProperties.getCaption(Captions.downloadImportTemplate));
	
	Button startDataImport = new Button(I18nProperties.getCaption(Captions.startDataImport));
	Button donloadErrorReport = new Button(I18nProperties.getCaption(Captions.downloadErrorReport));
	ComboBox valueSeperator = new ComboBox<>();
	
	public CampaignDataImportDialog(CampaignFormMetaReferenceDto campaignForm, CampaignReferenceDto campaignRefDto) {
		this.setHeaderTitle(I18nProperties.getCaption(Captions.importCampaignFormData) +" | "+ campaignForm.getCaption() + " (" +campaignRefDto.getCaption() +")");
		
		
        System.out.println("-----poker---5676567---");
        UI.getCurrent().getPage().executeJs(" setInterval(function () {console.log('working...')}, 1000); ");
        System.out.println("Task executed at: " + System.currentTimeMillis());
        
        
		Hr seperatorr = new Hr();
		seperatorr.getStyle().set("color" , " #0D6938");
		
		
	VerticalLayout dialog = new VerticalLayout();
	dialog.getStyle().set("gap", "0.5rem!important");
	
	H3 step1 = new H3();
	step1.add(I18nProperties.getString(Strings.step1));
	Label lblImportTemplateInfo = new Label(I18nProperties.getString(Strings.step1Description));
	
	System.out.println(">>>>>>>000000");
	downloadImportTemplate.addClickListener(e -> {
		ImportFacade importFacade = FacadeProvider.getImportFacade();
		try {
			importFacade.generateCampaignFormImportTemplateFile(campaignForm.getUuid());
			String templateFileName = DataHelper.sanitizeFileName(campaignRefDto.getCaption().replaceAll(" ", "_")) + "_"
					+ DataHelper.sanitizeFileName(campaignForm.getCaption().replaceAll(" ", "_")) + ".csv";
			System.out.println(">>>>>>>2222");
			
				String content = FacadeProvider.getImportFacade().getImportTemplateContent(importFacade.getCampaignFormImportTemplateFilePath());
				
				System.out.println(">>>>>>>333"+content);
				 // Convert CSV data to InputStream
		        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

		        // Create a StreamResource
		        StreamResource streamResource = new StreamResource("data.csv", () -> inputStream);

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
			
			
			
		}  catch (IOException ioException) {
			ioException.printStackTrace();
			
			Notification.show(
					I18nProperties.getString(Strings.headingTemplateNotAvailable) +": "+
					I18nProperties.getString(Strings.messageTemplateNotAvailable));
			
			
		}
			
	});
	
	H3 step2 = new H3();
	step2.add(I18nProperties.getString(Strings.step2));
	Label lblImportCsvFile = new Label(I18nProperties.getString(Strings.stepDesciption));
	startDataImport.addClickListener(null);
	
	
	H3 step3 = new H3();
	step3.add(I18nProperties.getString(Strings.step3));
	Label lblDnldErrorReport = new Label(I18nProperties.getString(Strings.step3Description));
	donloadErrorReport.addClickListener(null);
	
	
	
		dialog.add(seperatorr, step1, lblImportTemplateInfo, 
				downloadImportTemplate, step2, lblImportCsvFile, startDataImport, step3, lblDnldErrorReport,donloadErrorReport);
		
		this.addDialogCloseActionListener(
		            event -> {
		             
		                    System.out.println("Dialog was closed with the close button.");
		                   // timer.cancel();
		             
		            });
		
		H3 stepsdfass= new H3();
		stepsdfass.add("CLOSE");
		stepsdfass.addClickListener(e -> {
			
			this.close();
		});
		dialog.add(stepsdfass);
	add(dialog);
		
	}
		 private boolean pokeServerLocal() {
		        return true;
		    }

}
