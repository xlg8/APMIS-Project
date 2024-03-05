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

package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;

public class CampaignFormMetaExpiryIndexDto implements Serializable, Cloneable {

	public static final String I18N_PREFIX = "campaignformmetawithexp";

	public static final String FORM_UUID = "formid";
	public static final String CAMPAIGN_UDID = "campaignid";
	public static final String EXPIRYDAY = "expiryday";
	public static final String ENDDATE = "enddate";


	private static final long serialVersionUID = -6672198324526771162L;

	private String formid;
	private String campaignid;
	private Integer expiryday;
	private Date enddate;


	public CampaignFormMetaExpiryIndexDto(String formid, String campaignid, Integer bigInteger, Date enddate) {
		super();
		this.formid = formid;
		this.campaignid = campaignid;
		this.expiryday = bigInteger;
		this.enddate = enddate;
	}

	public String getFormid() {
		return formid;
	}

	public void setFormid(String formid) {
		this.formid = formid;
	}

	public String getCampaignid() {
		return campaignid;
	}

	public void setCampaignid(String campaignid) {
		this.campaignid = campaignid;
	}

	public Integer getExpiryday() {
		return expiryday;
	}

	public void setExpiryday(Integer expiryday) {
		this.expiryday = expiryday;
	}

	public Date getEnddate() {
		return enddate;
	}

	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}


	

}
