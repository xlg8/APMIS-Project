package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;
import java.util.Date;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.campaign.CampaignDto;

public class CampaignFormMetaExpiryDto extends EntityDto{
	/**
	* 
	*/
	private static final long serialVersionUID = -7995825772133756685L;
//extends EntityDto {

	public static final String FORM_ID = "formId";
	public static final String EXPIRATION = "expiryDay";
	public static final String CAMPAIGN_ID = "campaignId";
	public static final String END_DATE = "enddate";
//	public static final String UUID = "uuid";

	private String formId;
	private String campaignId;
	private Long expiryDay;
	private Date enddate;

	public CampaignFormMetaExpiryDto() {
		// TODO Auto-generated constructor stub
	}
//
//
//	public CampaignFormMetaExpiryDto(String formidd, String campId, Long expday, Date enddatee) {
//		super();
//		this.formId = formidd;
//		this.campaignId = campId;
//		this.expiryDay = expday;
//		this.enddate = enddatee;
//	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public Long getExpiryDay() {
		return expiryDay;
	}

	public void setExpiryDay(Long expiryDay) {
		this.expiryDay = expiryDay;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}


}
