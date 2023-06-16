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
package de.symeda.sormas.api.infrastructure.area;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;

public class AreaIndexDto extends EntityDto {

	private static final long serialVersionUID = -199144233786408125L;

	public static final String I18N_PREFIX = "Region";
	public static final String NAME = "name";
	public static final String POPULATION = "population";
	public static final String GROWTH_RATE = "growthRate";
	public static final String EXTERNAL_ID = "externalId";
	public static final String AREA_EXTERNAL_ID = "areaexternalId";

	private String name;
	private Integer population;
	private Float growthRate;
	private Long externalId;
	private Long areaexternalId;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return getName();
	}

	public Integer getPopulation() {
		return population;
	}

	public void setPopulation(Integer population) {
		this.population = population;
	}

	public Float getGrowthRate() {
		return growthRate;
	}

	public void setGrowthRate(Float growthRate) {
		this.growthRate = growthRate;
	}

	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}
	
	public Long getAreaexternalId() {
		return areaexternalId;
	}
	
	public void setAreaexternalId(Long areaexternalId) {
		this.areaexternalId = areaexternalId;
	}

	public AreaReferenceDto toReference() {
		return new AreaReferenceDto(getUuid(), name, externalId);
	}

	public static AreaIndexDto build() {

		AreaIndexDto dto = new AreaIndexDto();
		dto.setUuid(DataHelper.createUuid());
		return dto;
	}
}
