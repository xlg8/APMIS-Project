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

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DependingOnFeatureType;
import de.symeda.sormas.api.utils.FieldConstraints;

public class RegionDryRunDto extends EntityDto {

	private static final long serialVersionUID = -1610675328037466348L;

	public static final String I18N_PREFIX = "Region";
	public static final String NAME = "name";
	public static final String FA_AF = "fa_af";
	public static final String PS_AF = "ps_af";
	public static final String EPID_CODE = "epidCode";
	public static final String GROWTH_RATE = "growthRate";
	public static final String EXTERNAL_ID = "externalId";
	public static final String EXTERNAL_ID_DUMMY = "externalIddummy";
	public static final String AREA = "area";
	public static final String COUNTRY = "country";

	@Size(max = FieldConstraints.CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String name;
	@Size(max = FieldConstraints.CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String fa_af;
	@Size(max = FieldConstraints.CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String ps_af;
	@Size(max = FieldConstraints.CHARACTER_LIMIT_SMALL, message = Validations.textTooLong)
	private String epidCode;
	private Float growthRate;
	private boolean archived;
	//@NotNull
	private Long externalId;
	//@DependingOnFeatureType(featureType = FeatureType.INFRASTRUCTURE_TYPE_AREA)
	private AreaReferenceDto area;
	private CountryReferenceDto country;
	private String externalIddummy;
	private Long populationData;
	private Long regionId;
	private String areaUuid_;
	private String uuid_;
	private String mapHasc;

	public RegionDryRunDto(
		Date creationDate,
		Date changeDate,
		String uuid,
		boolean archived,
		String name,
		String epidCode,
		Float growthRate,
		Long externalId,
		String countryUuid,
		String countryName,
		String countryIsoCode,
		String areaUuid) {

		super(creationDate, changeDate, uuid);
		this.archived = archived;
		this.name = name;
		this.epidCode = epidCode;
		this.growthRate = growthRate;
		this.externalId = externalId;

		if (countryUuid != null) {
			this.country = new CountryReferenceDto(countryUuid, I18nProperties.getCountryName(countryIsoCode, countryName), countryIsoCode);
		}

		if (areaUuid != null) {
			this.area = new AreaReferenceDto(areaUuid);
		}
	}

	public RegionDryRunDto() {
		super();
	}
	
	public RegionDryRunDto(String name, Long populationData, Long regionId, String areaUuid, String uuid_) {
		this.name = name;
		this.populationData = populationData;
		this.regionId = regionId;
		this.areaUuid_ = areaUuid;
		this.uuid_ = uuid_;
	};
	
	
	public RegionDryRunDto(@Size(max = 255, message = "textTooLong") String name, String mapHasc) {
		super();
		this.name = name;
		this.mapHasc = mapHasc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEpidCode() {
		return epidCode;
	}

	public void setEpidCode(String epidCode) {
		this.epidCode = epidCode;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Float getGrowthRate() {
		return growthRate;
	}

	public void setGrowthRate(Float growthRate) {
		this.growthRate = growthRate;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	//@DependingOnFeatureType(featureType = FeatureType.INFRASTRUCTURE_TYPE_AREA)
	public AreaReferenceDto getArea() {
		return area;
	}

	public void setArea(AreaReferenceDto area) {
		this.area = area;
	}

	public CountryReferenceDto getCountry() {
		return country;
	}

	public void setCountry(CountryReferenceDto country) {
		this.country = country;
	}
	
	public Long getPopulationData() {
		return populationData;
	}

	public void setPopulationData(Long populationData) {
		this.populationData = populationData;
	}

	public Long getRegionId() {
		return regionId;
	}

	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}

	public String getExternalIddummy() {
		
		if(externalId != null) {
			externalIddummy = externalId+"";
		}
		
		return externalIddummy;
	}

	public void setExternalIddummy(String externalIddummy) {
		
		if(externalIddummy != null) {
			this.externalId = Long.parseLong(externalIddummy);
		}
				
	}
	public RegionReferenceDto toReference() {
		return new RegionReferenceDto(getUuid(), name, externalId);
	}

	public static RegionDryRunDto build() {
		RegionDryRunDto dto = new RegionDryRunDto();
		dto.setUuid(DataHelper.createUuid());
		return dto;
	}

	public String getAreaUuid_() {
		return areaUuid_;
	}

	public void setAreaUuid_(String areaUuid_) {
		this.areaUuid_ = areaUuid_;
	}

	public String getUuid_() {
		return uuid_;
	}

	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}

	public String getMapHasc() {
		return mapHasc;
	}

	public void setMapHasc(String mapHasc) {
		this.mapHasc = mapHasc;
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
	
}
