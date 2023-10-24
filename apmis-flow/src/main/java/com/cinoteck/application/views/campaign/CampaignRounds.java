package com.cinoteck.application.views.campaign;

import de.symeda.sormas.api.i18n.I18nProperties;

public enum CampaignRounds {

	NID("NID"), SNID("SNID"), CASE_RESPOND("Case Respond"), MOCK_UP("Mopping-Up"), TRAINING("Training");

	private String displayName;
	
	CampaignRounds(String displayName) {
        this.displayName = displayName;
    }

	public String toString() {
		return I18nProperties.getEnumCaption(this);
	}

	public String getDisplayName() {
		return displayName;
	}

}
