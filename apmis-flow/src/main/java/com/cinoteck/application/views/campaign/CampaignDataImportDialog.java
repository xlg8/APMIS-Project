package com.cinoteck.application.views.campaign;

import java.io.IOException;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dnd.internal.DndUtil;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.importexport.ImportFacade;
import de.symeda.sormas.api.infrastructure.InfrastructureType;


public class CampaignDataImportDialog extends Dialog{
	
	ComboBox campaignFilter = new ComboBox<>();
	Button downloadImportTemplate = new Button("Download Import Template");
	Button startDataImport = new Button("Start Data Import");
	Button donloadErrorReport = new Button("Download Error Report");
	ComboBox valueSeperator = new ComboBox<>();
	
	public CampaignDataImportDialog() {
		this.setHeaderTitle("Import Campaign Form Data");
//		this.getStyle().set("color" , "#0D6938");
		
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
	step2.add("Step 1: Download the Import Template");
	Label lblImportTemplateInfo = new Label("You can use this template .csv file to bring your data into a format APMIS can read. Please do this every time you import data, never use a file you have downloaded before.");
	downloadImportTemplate.addClickListener(null);
	
	H3 step3 = new H3();
	step3.add("Step 2: Import CSV File");
	Label lblImportCsvFile = new Label("Depending on the amount of rows you want to import, this may take a while. You will receive a notification when the import process has finished.");
	startDataImport.addClickListener(null);
	
	
	H3 step4 = new H3();
	step4.add("Step 3: Download Error Report");
	Label lblDnldErrorReport = new Label("If there were any rows that could not be imported, you will be offered a .csv file containing all these rows as well as the error descriptions.");
	donloadErrorReport.addClickListener(null);
	
	
	
		dialog.add(seperatorr, step2, lblImportTemplateInfo, 
				downloadImportTemplate, step3, lblImportCsvFile, startDataImport, step4, lblDnldErrorReport,donloadErrorReport);
	add(dialog);
		
	}
	

}
