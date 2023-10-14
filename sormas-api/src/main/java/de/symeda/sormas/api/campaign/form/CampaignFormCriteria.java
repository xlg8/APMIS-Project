package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;
import java.util.Date;

import de.symeda.sormas.api.utils.IgnoreForUrl;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

public class CampaignFormCriteria extends BaseCriteria implements Serializable {

//	public static final String CAMPAIGN_FORM_META_ELEMENTS = "campaignFormElements";
//	public static final String FORM_PHASE = "formPhase";
//	public static final String FORM_NAME = "formName";
//	public static final String FORM_TYPE = "formType";
//	public static final String FORM_CATEGORY = "formCategory";
//	public static final String MODALITY = "modality";

	private CampaignFormElement campaignFormElements;	
	private CampaignFormMetaReferenceDto formCategory;
	private CampaignFormMetaReferenceDto formPhase;
	private Date formDate;
	private String formName;
	private String formType;
	
	public CampaignFormElement getCampaignFormElements() {
		return campaignFormElements;
	}
	
	public CampaignFormCriteria setCampaignFormElements(CampaignFormElement campaignFormElements) {
		this.campaignFormElements = campaignFormElements;
		return this;
	}
	
	public CampaignFormMetaReferenceDto getFormCategory() {
		return formCategory;
	}
	
	public CampaignFormCriteria setFormCategory(CampaignFormMetaReferenceDto formCategory) {
		this.formCategory = formCategory;
		return this;
	}
	
	public CampaignFormMetaReferenceDto getFormPhase() {
		return formPhase;
	}
	
	public CampaignFormCriteria setFormPhase(CampaignFormMetaReferenceDto formPhase) {
		this.formPhase = formPhase;
		return this;
	}
	
	public Date getFormDate() {
		return formDate;
	}
	
	public CampaignFormCriteria setFormDate(Date formDate) {
		this.formDate = formDate;
		return this;
	}
	
	public String getFormName() {
		return formName;
	}
	
	public CampaignFormCriteria setFormName(String formName) {
		this.formName = formName;
		return this;
	}
	
	@IgnoreForUrl
	public String getFormType() {
		return formType;
	}
	
	public CampaignFormCriteria setFormType(String formType) {
		this.formType = formType;
		return this;
	}
	
}
