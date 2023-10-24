package de.symeda.sormas.backend.campaign.form;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "campaignformmetawithexp")
public class CampaignFormMetaExpDay implements Serializable{// extends AbstractDomainObject {

	private static final long serialVersionUID = -5200626281564146910L;

	public static final String TABLE_NAME = "campaignformmetawithexp";

	public static final String FORM_ID = "formId";
	public static final String CAMPAIGN = "campaignId";
	public static final String EXPIRE_DAY = "expiryDay"; 

	private String formId;
	private String campaignId;
	private int expiryDay;


	
	
	public CampaignFormMetaExpDay(String formId, String campaignId, int expiryDay) {
		super();
		this.formId = formId;
		this.campaignId = campaignId;
		this.expiryDay = expiryDay;
	}

	public CampaignFormMetaExpDay() {
		// TODO Auto-generated constructor stub
	}

	@Id
	@Column
	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}
	
	@Column
	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	
	@Column
	public int getExpiryDay() {
		return expiryDay;
	}

	public void setExpiryDay(int expiryDay) {
		this.expiryDay = expiryDay;
	}

	
}
