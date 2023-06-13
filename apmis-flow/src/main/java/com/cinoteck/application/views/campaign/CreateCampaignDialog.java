package com.cinoteck.application.views.campaign;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import de.symeda.sormas.api.campaign.CampaignDto;

public class CreateCampaignDialog  extends Dialog{
	CampaignDto campaignDto;
	public CreateCampaignDialog() {
		this.setHeaderTitle("Create New Campaign");
		CampaignForm campaignForm;
		VerticalLayout dialog = new VerticalLayout();
		
		campaignForm = new CampaignForm(campaignDto);
		
		dialog.add(campaignForm);
		
		add(dialog);
		
	}

}
