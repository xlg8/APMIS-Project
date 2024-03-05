/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.app.backend.campaign.form;

import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_DEFAULT;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.common.AbstractDomainObject;
import de.symeda.sormas.app.backend.common.PseudonymizableAdo;

@Entity(name = CampaignFormMetaWithExp.TABLE_NAME)
@DatabaseTable(tableName = CampaignFormMetaWithExp.TABLE_NAME)
public class CampaignFormMetaWithExp extends AbstractDomainObject implements Serializable {

	public static final String TABLE_NAME = "campaignformmetawithexp";
	public static final String I18N_PREFIX = "campaignformmetawithexp";

	public static final String UUID = "uuid";

	public static final String FORM_ID = "formId";
	public static final String CAMPAIGN_ID = "campaignId";
	public static final String EXPIRE_DAY = "expiryDay";
	public static final String EXPIRE_DATE = "endDate";
	public static final String CHANGE_DATE = "changeDate";



	@Column
	@DatabaseField(dataType = DataType.STRING)
	private String formId;
	@Column
	@DatabaseField(dataType = DataType.STRING)

	private String campaignId;
	@Column
	private Long expiryDay;
	@DatabaseField(dataType = DataType.DATE_LONG)//, canBeNull = true)
	private Date expiryDate;

//	@Column
//	@DatabaseField(dataType = DataType.STRING)
//	private String uuid ;


	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Long getExpiryDay() {
		return expiryDay;
	}

	public void setExpiryDay(Long expiryDay) {
		this.expiryDay = expiryDay;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}



	@Override
	public String getI18nPrefix() {
		return I18N_PREFIX;
	}

//	@Override
//	public String toString() {
//		return getFormName();
//	}


}
