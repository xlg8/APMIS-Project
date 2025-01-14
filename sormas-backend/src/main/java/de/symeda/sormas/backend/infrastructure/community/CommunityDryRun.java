/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.backend.infrastructure.community;

import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_DEFAULT;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import de.symeda.sormas.backend.common.InfrastructureAdo;
import de.symeda.sormas.backend.infrastructure.district.District;

@Entity
public class CommunityDryRun extends InfrastructureAdo {

	private static final long serialVersionUID = 5912389960534155909L;

	public static final String TABLE_NAME = "communitydryrun";

	public static final String NAME = "name";
	public static final String FA_AF = "fa_af";
	public static final String PS_AF = "ps_af";
	public static final String DISTRICT = "district";
	public static final String GROWTH_RATE = "growthRate";
	public static final String EXTERNAL_ID = "externalId";
	public static final String CLUSTER_NUMBER = "clusterNumber";
	public static final String FLOATING_STATUS = "floating";

	private String name;
	private String fa_af;
	private String ps_af;
	private District district;
	private Float growthRate;
	private Long externalId;
	private Integer clusterNumber;
	private String floating;

	public String getName() {
			return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFa_af() {
		return fa_af;
	}

	public void setFa_af(String fa_af) {
		this.fa_af = fa_af;
	}

	public String getPs_af() {
		return ps_af;
	}

	public void setPs_af(String ps_af) {
		this.ps_af = ps_af;
	}

	@ManyToOne(cascade = CascadeType.REFRESH, optional = false)
	@JoinColumn(nullable = false)
	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	public Float getGrowthRate() {
		return growthRate;
	}

	public void setGrowthRate(Float growthRate) {
		this.growthRate = growthRate;
	}

	//@Column(length = CHARACTER_LIMIT_DEFAULT)
	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	public Integer getClusterNumber() {
		return clusterNumber;
	}

	public void setClusterNumber(Integer clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	
	

	public String getFloating() {
		return floating;
	}

	public void setFloating(String floating) {
		this.floating = floating;
	}

	@Override
	public String toString() {
		return getName();
	}
}
