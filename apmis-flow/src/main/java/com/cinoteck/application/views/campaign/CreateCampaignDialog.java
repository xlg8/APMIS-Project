package com.cinoteck.application.views.campaign;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;

public class CreateCampaignDialog  extends Dialog{
	CampaignDto campaignDto;
	public CreateCampaignDialog() {
		this.setHeaderTitle(I18nProperties.getCaption(Captions.createNewCampaign));
		CampaignForm campaignForm;
		VerticalLayout dialog = new VerticalLayout();
		
		campaignForm = new CampaignForm(campaignDto);
		
		dialog.add(campaignForm);
		
		add(dialog);
		
	}

}