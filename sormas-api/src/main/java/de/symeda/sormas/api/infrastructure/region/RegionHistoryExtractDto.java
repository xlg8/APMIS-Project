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
package de.symeda.sormas.api.infrastructure.region;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.report.CampaignDataExtractDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DependingOnFeatureType;
import de.symeda.sormas.api.utils.FieldConstraints;

public class RegionHistoryExtractDto implements Serializable, Comparable<RegionHistoryExtractDto> {

	private String uuid_;
	private String name;
	private Long externalId;
	private boolean archived;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	
//	private String fa_af;
//	private String ps_af;
//	private String epidCode;
//	private Float growthRate;
//
//
//	private AreaReferenceDto area;
//	private CountryReferenceDto country;
//	private String externalIddummy;
//	private Long populationData;
//	private Long regionId;
//	private String areaUuid_;
//	private String mapHasc;
	
	public RegionHistoryExtractDto(String uuid, String name, Boolean archived, Long externalID, LocalDateTime startDate, LocalDateTime endDate) {
	    this.uuid_ = uuid;
	    this.name = name;
	    this.archived = archived;
	    this.externalId = externalID;
	    this.startDate = startDate;
	    this.endDate = endDate;
	}
    public RegionHistoryExtractDto(String string, String string2, Boolean boolean1, long l, LocalDateTime localDateTime, Object object) {
        this.uuid_ = string;
        this.name = string2;
        this.archived = boolean1;
        this.externalId = l;
        this.startDate = localDateTime;
        this.endDate = (object instanceof LocalDateTime) ? (LocalDateTime) object : null;
    }

    
    


	public String getUuid_() {
		return uuid_;
	}
	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getExternalId() {
		return externalId;
	}
	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}
	public boolean isArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	public LocalDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(archived,    endDate,  externalId,
				    name,    startDate, uuid_);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegionHistoryExtractDto other = (RegionHistoryExtractDto) obj;
		return archived == other.archived 
//				&& Objects.equals(area, other.area)
//				&& Objects.equals(areaUuid_, other.areaUuid_) && Objects.equals(country, other.country)
				&& Objects.equals(endDate, other.endDate) 
//				&& Objects.equals(epidCode, other.epidCode)
				&& Objects.equals(externalId, other.externalId)
//				&& Objects.equals(externalIddummy, other.externalIddummy) && Objects.equals(fa_af, other.fa_af)
//				&& Objects.equals(growthRate, other.growthRate) && Objects.equals(mapHasc, other.mapHasc)
				&& Objects.equals(name, other.name) 
//				&& Objects.equals(populationData, other.populationData)
//				&& Objects.equals(ps_af, other.ps_af) && Objects.equals(regionId, other.regionId)
				&& Objects.equals(startDate, other.startDate) && Objects.equals(uuid_, other.uuid_);
	}

	@Override
	public int compareTo(RegionHistoryExtractDto o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
