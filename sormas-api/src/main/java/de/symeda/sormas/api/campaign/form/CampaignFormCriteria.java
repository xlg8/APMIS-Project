package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;
import java.util.Date;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.IgnoreForUrl;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

public class CampaignFormCriteria extends BaseCriteria implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4206918019308278802L;
	private FormAccess formCategory;
	private String modality;
//	private String formPhase;
	private Date startDate;
	private Date endDate;
	private String formName;
	private String formType;
	private EntityRelevanceStatus relevanceStatus;
	
	public String getModality() {
		return modality;
	}

	public CampaignFormCriteria setModality(String modality) {
		this.modality = modality;
		return this;
	}

	public FormAccess getFormCategory() {
		return formCategory;
	}
	
	public CampaignFormCriteria setFormCategory(FormAccess formCategory) {
		this.formCategory = formCategory;
		return this;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public CampaignFormCriteria setStartDate(Date startDate) {
		this.startDate = startDate;
		return this;
	}

	public Date getEndDate() {
		return endDate;
	}

	public CampaignFormCriteria setEndDate(Date endDate) {
		this.endDate = endDate;
		return this;
	}

//	public String getFormPhase() {
//		return formPhase;
//	}
//	
//	public CampaignFormCriteria setFormPhase(String formPhase) {
//		this.formPhase = formPhase;
//		return this;
//	}

	public String getFormType() {
		return formType;
	}
	
	public CampaignFormCriteria setFormType(String formType) {
		this.formType = formType;
		return this;
	}
	
	@IgnoreForUrl
	public String getFormName() {
		return formName;
	}
	
	public CampaignFormCriteria setFormName(String formName) {
		this.formName = formName;
		return this;
	}
	
	public CampaignFormCriteria relevanceStatus(EntityRelevanceStatus relevanceStatus) {
		this.relevanceStatus = relevanceStatus;
		return this;
	}

	public EntityRelevanceStatus getRelevanceStatus() {
		return relevanceStatus;
	}
	
}
