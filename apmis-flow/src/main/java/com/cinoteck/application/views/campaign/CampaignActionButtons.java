package com.cinoteck.application.views.campaign;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;

public class CampaignActionButtons extends HorizontalLayout {
	Anchor archiveDearchive = new Anchor(I18nProperties.getCaption(Captions.actionArchive));
	Button openCloseCampaign;
	Button duplicateCampaign;
	Button deleteCampaign;
	Button publishUnpublishCampaign;
	
	Button discardChanges;
	Button saveChanges;
	

	public CampaignActionButtons() {
		setWidthFull();
		openCloseCampaign = new Button();
		openCloseCampaign.setText("Open Campaign");
		duplicateCampaign = new Button();
		duplicateCampaign.setText("Duplicate");
		deleteCampaign = new Button();
		deleteCampaign.setText(I18nProperties.getCaption(Captions.actionDelete));
		deleteCampaign.getStyle().set("background", "red");
		publishUnpublishCampaign = new Button();
		publishUnpublishCampaign.setText("Publish Campaign");
		discardChanges = new Button();
		discardChanges.setText("Discard Changes");
		saveChanges = new Button();
		saveChanges.setText(I18nProperties.getCaption(Captions.actionSave));
		
//		 setVerticalComponentAlignment(Alignment.END, discardChanges, saveChanges);
//		
//		 
//		 
//		add(archiveDearchive, publishUnpublishCampaign, openCloseCampaign, duplicateCampaign, deleteCampaign
//				, discardChanges, saveChanges);
		
	    // Create an empty spacer component
        Button spacer = new Button();
        spacer.setWidth("54%");
        spacer.getStyle().set("background-color", "none !important");
        spacer.getStyle().set("box-shadow", "none !important");
        spacer.getStyle().set("color", "none !important");
        spacer.getStyle().set("background", "none !important");



        // Add the buttons to the layout
        add(archiveDearchive, publishUnpublishCampaign, openCloseCampaign, duplicateCampaign, deleteCampaign, spacer, discardChanges, saveChanges);

        // Set the justify content mode to END
        setJustifyContentMode(JustifyContentMode.END);

	}

}
