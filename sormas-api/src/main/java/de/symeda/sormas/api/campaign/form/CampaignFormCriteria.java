package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;
import java.util.Date;

import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.utils.IgnoreForUrl;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

public class CampaignFormCriteria extends BaseCriteria implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4206918019308278802L;
	private String formCategory;
	private String formPhase;
	private Date startDate;
	private Date endDate;
	private String formName;
	private String formType;
	
	public String getFormCategory() {
		return formCategory;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public CampaignFormCriteria setFormCategory(String formCategory) {
		this.formCategory = formCategory;
		return this;
	}
	
	public String getFormPhase() {
		return formPhase;
	}
	
	public CampaignFormCriteria setFormPhase(String formPhase) {
		this.formPhase = formPhase;
		return this;
	}
	
//	@IgnoreForUrl
	public String getFormName() {
		return formName;
	}
	
	public CampaignFormCriteria setFormName(String formName) {
		this.formName = formName;
		return this;
	}
	
//	@IgnoreForUrl
	public String getFormType() {
		return formType;
	}
	
	public CampaignFormCriteria setFormType(String formType) {
		this.formType = formType;
		return this;
	}
	
}
