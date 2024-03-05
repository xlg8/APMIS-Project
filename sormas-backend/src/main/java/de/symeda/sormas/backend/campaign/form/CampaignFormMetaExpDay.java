package de.symeda.sormas.backend.campaign.form;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;


import java.util.Date;


import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaWithExpReferenceDto;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.common.AbstractDomainObject;

@Entity(name = "campaignformmetawithexp")
public class CampaignFormMetaExpDay  extends AbstractDomainObject {

	private static final long serialVersionUID = -5200626281564146910L;

	public static final String TABLE_NAME = "campaignformmetawithexp";

	public static final String FORM_ID = "formId";
	public static final String CAMPAIGN = "campaignId";
	public static final String EXPIRE_DAY = "expiryDay"; 
	public static final String EXPIRE_DATE = "endDate"; 
	public static final String UUID = "uuid"; 

	


	private String formId;
	private String campaignId;
	private int expiryDay;
	private Date expiryDate;
	private String uuid;


//	public CampaignFormMetaExpDay(String formId, String campaignId, int expiryDay) {
//		super();
//		this.formId = formId;
//		this.campaignId = campaignId;
//		this.expiryDay = expiryDay;
//	}
//	
//	public CampaignFormMetaExpDay(String formId, String campaignId, int expiryDay, Date expiryDate) {
//		super();
//		this.formId = formId;
//		this.campaignId = campaignId;
//		this.expiryDay = expiryDay;
//		this.expiryDate = expiryDate;
//	}
//	
//	public CampaignFormMetaExpDay(String formId, String campaignId, int expiryDay, Date expiryDate, Date changedDate) {
//		super();
//		this.formId = formId;
//		this.campaignId = campaignId;
//		this.expiryDay = expiryDay;
//		this.expiryDate = expiryDate;
////		this.setChangeDate(changedDate);
//	}

	public CampaignFormMetaExpDay() {
		// TODO Auto-generated constructor stub
	}

//	@Id
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

	@Column
	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
	
	
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public CampaignFormMetaWithExpReferenceDto toReference() {
		Long expiryDayLong  = Long.valueOf(expiryDay);
		return new CampaignFormMetaWithExpReferenceDto(formId, campaignId, expiryDayLong, expiryDate, uuid);
	}

	
}
