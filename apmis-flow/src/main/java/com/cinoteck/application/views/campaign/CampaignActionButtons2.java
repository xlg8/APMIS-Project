package com.cinoteck.application.views.campaign;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

public class CampaignActionButtons2 extends HorizontalLayout {
	Anchor archiveDearchive = new Anchor(I18nProperties.getCaption(Captions.archive));
	Button saveandcontinue;
	
	
	Button discardChanges;
	Button saveChanges;
	

	public CampaignActionButtons2() {
		setWidthFull();
		saveandcontinue = new Button();
		saveandcontinue.setText(I18nProperties.getCaption(Captions.saveAndContinue));
		saveChanges.addClickListener(e-> Notification.show(I18nProperties.getString(Strings.campaignDataUpdated), 2, Position.MIDDLE));

		
		discardChanges = new Button();
		discardChanges.setText(I18nProperties.getCaption(Captions.actionDiscard));
//		discardChanges.addClickListener(e->{ dialog.close();});
		
		saveChanges = new Button();
		saveChanges.setText(I18nProperties.getCaption(Captions.actionSave));
		saveChanges.addClickListener(e-> Notification.show(I18nProperties.getString(Strings.campaignDataSaved), 2, Position.MIDDLE));
		
//		 setVerticalComponentAlignment(Alignment.END, discardChanges, saveChanges);
//		
//		 
//		 
//		add(archiveDearchive, publishUnpublishCampaign, openCloseCampaign, duplicateCampaign, deleteCampaign
//				, discardChanges, saveChanges);
		
	    // Create an empty spacer component
        Button spacer = new Button();
        spacer.setWidth("70%");
        spacer.getStyle().set("background-color", "none !important");
        spacer.getStyle().set("box-shadow", "none !important");
        spacer.getStyle().set("color", "none !important");
        spacer.getStyle().set("background", "none !important");

        
        


        // Add the buttons to the layout
        add(saveandcontinue, spacer, discardChanges, saveChanges);

        // Set the justify content mode to END
        setJustifyContentMode(JustifyContentMode.END);

	}

}