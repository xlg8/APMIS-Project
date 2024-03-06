/*
 * ******************************************************************************
 * * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * *
 * * This program is free software: you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation, either version 3 of the License, or
 * * (at your option) any later version.
 * *
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

package de.symeda.sormas.api.campaign.form;

import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.user.FormAccess;

public class CampaignFormMetaWithExpReferenceDto extends ReferenceDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String FORM_ID = "formId";
	public static final String EXPIRATION = "expiryDay";
	public static final String CAMPAIGN_ID = "campaignId";
	public static final String END_DATE = "endDate";
//	public static final String UUID = "uuid";


	private String formId;
	private String campaignId;
	private Long daysExpired;
	private Date date;
//	private String uuid;


	public CampaignFormMetaWithExpReferenceDto() {
	}
	
	public CampaignFormMetaWithExpReferenceDto(String uuid) {
        this.setUuid(uuid);
    }
	
	public CampaignFormMetaWithExpReferenceDto(String formId, String campaignId, Long expiryDay, Date date, String uuid) {
		// TODO Auto-generated constructor stub

		this.formId = formId;
		this.campaignId = campaignId;
		this.daysExpired = expiryDay;
		this.date = date;
		setUuid(uuid);
	}

	public CampaignFormMetaWithExpReferenceDto(String formId, String campaignId, Long expiryDay, Date date) {
		// TODO Auto-generated constructor stub

		this.formId = formId;
		this.campaignId = campaignId;
		this.daysExpired = expiryDay;
		this.date = date;
	}

	public CampaignFormMetaWithExpReferenceDto(String campaignId, String formId, Long expiryDay) {
//		super();
		this.campaignId = campaignId;
		this.formId = formId;
		this.daysExpired = expiryDay;
	}

	public Long getDaysExpired() {
		return daysExpired;
	}

	public void setDaysExpired(Long daysExpired) {
		this.daysExpired = daysExpired;
	}

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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	

}
