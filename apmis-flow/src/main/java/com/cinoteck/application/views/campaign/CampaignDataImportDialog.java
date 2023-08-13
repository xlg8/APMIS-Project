package com.cinoteck.application.views.campaign;

import java.util.Timer;
import java.util.TimerTask;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;


public class CampaignDataImportDialog extends Dialog{
	
	ComboBox campaignFilter = new ComboBox<>();
	Button downloadImportTemplate = new Button(I18nProperties.getCaption(Captions.downloadImportTemplate));
	Button startDataImport = new Button(I18nProperties.getCaption(Captions.startDataImport));
	Button donloadErrorReport = new Button(I18nProperties.getCaption(Captions.downloadErrorReport));
	ComboBox valueSeperator = new ComboBox<>();
	Timer timer = new Timer();
	public CampaignDataImportDialog(CampaignFormMetaReferenceDto campaignFormMetaRefdto, CampaignReferenceDto campaignReferenceDto) {
		this.setHeaderTitle(I18nProperties.getCaption(Captions.importCampaignFormData) +" | "+ campaignFormMetaRefdto.getCaption());
//		this.getStyle().set("color" , "#0D6938");
		//insrease this to accept more time
		

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	UI.getCurrent().getPage().executeJs("return $server.pokeServer()").then(String.class, pokeValue -> {
            		 System.out.println("-----poker:"+pokeValue);
            	});
                System.out.println("Task executed at: " + System.currentTimeMillis() +" | "+pokeServerLocal());
            }
        };
//        
//        this.$server.pokeServer().then((result) => {
//            if (result === true) {
//              console.log('Idle-Notification: Server poked successfully. Session extended.');
//              this.opened = false;
//              this._resetTimer();
//            } else {
//              console.error('Could not poke the server');
//            }
//          });
        

        // Schedule the task to run every 60 seconds (1000 milliseconds = 1 second)
        timer.scheduleAtFixedRate(task, 0, 20 * 1000);
        
        
		Hr seperatorr = new Hr();
		seperatorr.getStyle().set("color" , " #0D6938");
		
		
	VerticalLayout dialog = new VerticalLayout();
	
//		campaignFilter.setId(CampaignDto.NAME);
//		campaignFilter.setRequired(true);
//		campaignFilter.setItems(FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference());
//		campaignFilter.setEnabled(false);
//		
//		Label lblCollectionDateInfo = new Label(I18nProperties.getString(Strings.infoPopulationCollectionDate));
		
	H3 step2 = new H3();
	step2.add(I18nProperties.getString(Strings.step1));
	Label lblImportTemplateInfo = new Label(I18nProperties.getString(Strings.step1Description));
	downloadImportTemplate.addClickListener(null);
	
	H3 step3 = new H3();
	step3.add(I18nProperties.getString(Strings.step2));
	Label lblImportCsvFile = new Label(I18nProperties.getString(Strings.stepDesciption));
	startDataImport.addClickListener(null);
	
	
	H3 step4 = new H3();
	step4.add(I18nProperties.getString(Strings.step3));
	Label lblDnldErrorReport = new Label(I18nProperties.getString(Strings.step3Description));
	donloadErrorReport.addClickListener(null);
	
	
	
		dialog.add(seperatorr, step2, lblImportTemplateInfo, 
				downloadImportTemplate, step3, lblImportCsvFile, startDataImport, step4, lblDnldErrorReport,donloadErrorReport);
		
		this.addDialogCloseActionListener(
		            event -> {
		             
		                    System.out.println("Dialog was closed with the close button.");
		                    timer.cancel();
		             
		            });
		
	add(dialog);
		
	}
		 private boolean pokeServerLocal() {
		        return true;
		    }

}
