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
package de.symeda.sormas.api.infrastructure.community;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.symeda.sormas.api.ClusterFloatStatus;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.ImportIgnore;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionHistoryExtractDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.FieldConstraints;


public class CommunityHistoryExtractDto implements Serializable, Comparable<CommunityHistoryExtractDto> {

	private String uuid_;
	private String name;
	private Long externalId;
	private boolean archived;
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	
	public CommunityHistoryExtractDto(String uuid, String name, Boolean archived, Long externalID, LocalDateTime startDate, LocalDateTime endDate) {
	    this.uuid_ = uuid;
	    this.name = name;
	    this.archived = archived;
	    this.externalId = externalID;
	    this.startDate = startDate;
	    this.endDate = endDate;
	}
    public CommunityHistoryExtractDto(String string, String string2, Boolean boolean1, long l, LocalDateTime localDateTime, Object object) {
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
		return Objects.hash(archived, endDate, externalId, name, startDate, uuid_);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommunityHistoryExtractDto other = (CommunityHistoryExtractDto) obj;
		return archived == other.archived && Objects.equals(endDate, other.endDate)
				&& Objects.equals(externalId, other.externalId) && Objects.equals(name, other.name)
				&& Objects.equals(startDate, other.startDate) && Objects.equals(uuid_, other.uuid_);
	}
	@Override
	public int compareTo(CommunityHistoryExtractDto o) {
		// TODO Auto-generated method stub
		return 0;
	}

    
    
	
	
	
}
