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

public class CampaignFormMetaIndexDto implements Serializable, Cloneable {


	private static final long serialVersionUID = -6672198324526771162L;

	private String fieldid;
	private String fieldtype;
	private String fieldcaption;
	private String fieldexpression;


	public CampaignFormMetaIndexDto(String fieldid, String fieldtype, String fieldcaption, String fieldexpression) {
		super();
		this.fieldid = fieldid;
		this.fieldtype = fieldtype;
		this.fieldcaption = fieldcaption;
		this.fieldexpression = fieldexpression;
	}


	public String getFieldid() {
		return fieldid;
	}


	public void setFieldid(String fieldid) {
		this.fieldid = fieldid;
	}


	public String getFieldtype() {
		return fieldtype;
	}


	public void setFieldtype(String fieldtype) {
		this.fieldtype = fieldtype;
	}


	public String getFieldcaption() {
		return fieldcaption;
	}


	public void setFieldcaption(String fieldcaption) {
		this.fieldcaption = fieldcaption;
	}


	public String getFieldexpression() {
		return fieldexpression;
	}


	public void setFieldexpression(String fieldexpression) {
		this.fieldexpression = fieldexpression;
	}

	

	

}
