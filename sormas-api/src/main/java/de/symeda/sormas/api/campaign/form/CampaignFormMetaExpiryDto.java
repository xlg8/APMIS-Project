package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;

import de.symeda.sormas.api.campaign.CampaignDto;


public class CampaignFormMetaExpiryDto implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = -7995825772133756685L;
//extends EntityDto {

	public static final String FORM_ID = "formId";
	public static final String EXPIRATION = "expiryDay";
	public static final String CAMPAIGN_ID = "campaignId";
	
	
	private CampaignDto campaignId;
	private CampaignFormMetaReferenceDto formId;
	private int expiryDay;
	
	public CampaignFormMetaExpiryDto(CampaignDto campaignId, CampaignFormMetaReferenceDto formId, int expiryDay) {
		super();
		this.campaignId = campaignId;
		this.formId = formId;
		this.expiryDay = expiryDay;
	}
	
	public CampaignFormMetaExpiryDto() {
		// TODO Auto-generated constructor stub
	}

	public CampaignDto getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(CampaignDto campaignId) {
		this.campaignId = campaignId;
	}
	public CampaignFormMetaReferenceDto getFormId() {
		return formId;
	}
	public void setFormId(CampaignFormMetaReferenceDto formId) {
		this.formId = formId;
	}
	public int getExpiryDay() {
		return expiryDay;
	}
	public void setExpiryDay(int expiryDay) {
		this.expiryDay = expiryDay;
	}
	
	
	
	
	
}
