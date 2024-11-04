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

package de.symeda.sormas.api.campaign.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto; 
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

public class CampaignFormDataCriteria extends BaseCriteria implements Serializable {

	public static final String CAMPAIGN = "campaign";
	public static final String CAMPAIGN_FORM_META = "campaignFormMeta";
	public static final String REGION = "region";
	public static final String AREA = "area";
	public static final String DISTRICT = "district";
	public static final String COMMUNITY = "community";
	public static final String FORM_DATE = "formDate";
	public static final String FORM_TYPE = "formType";
	public static final String FORM_PHASE = "formPhase"; 
	public static final String ERROR_STATUS = "error_status";
	public static final String USER_LANGUAGE = "userLanguage";
	public static final String USER_TYPE = "usertype";
	public static final String ISVERIFIED = "isVerified";
	public static final String ISPUBLISHED = "isPublished";

	//for filter
	

	private static final long serialVersionUID = 8124072093160133408L;

	private CampaignReferenceDto campaign;
	private CampaignFormMetaReferenceDto campaignFormMeta;
	private AreaReferenceDto area;
	private Set<AreaReferenceDto> areaSet;
	private RegionReferenceDto region;
	private Set<RegionReferenceDto> regionSet;


	private DistrictReferenceDto district;
	private Set<DistrictReferenceDto> districtSet;
	private CommunityReferenceDto community;
	private Date formDate;
	private String formType;
	private CampaignFormMetaReferenceDto formPhase; 
	private String error_status;
	private String userLanguage;
	private String usertype;
	private Boolean isVerified;
	private Boolean isPublished;
	private UserType usertypeEnum;
	// for filter

	

	public CampaignReferenceDto getCampaign() {
		return campaign;
	}

	public void setCampaign(CampaignReferenceDto campaign) {
		this.campaign = campaign; 
	}

	public CampaignFormDataCriteria campaign(CampaignReferenceDto campaign) {
		this.campaign = campaign;
		return this;
	}

	public CampaignFormMetaReferenceDto getCampaignFormMeta() {
		return campaignFormMeta;
	}

	public void setCampaignFormMeta(CampaignFormMetaReferenceDto campaignFormMeta) {
		this.campaignFormMeta = campaignFormMeta;
	}

	public CampaignFormDataCriteria campaignFormMeta(CampaignFormMetaReferenceDto campaignFormMeta) {
		this.campaignFormMeta = campaignFormMeta;
		return this;
	}
	
	public AreaReferenceDto getArea() {
		return area;
	}

	public Set<AreaReferenceDto> getAreaSet() {
		return areaSet;
	}

	public void setAreaSet(Set<AreaReferenceDto> areaSet) {
		this.areaSet = areaSet;
	}

	public void setArea(AreaReferenceDto area) {
		this.area = area;
	}
	
	public CampaignFormDataCriteria area(AreaReferenceDto area) {
		this.area = area;
		return this;
	}
	
	public CampaignFormDataCriteria areaSet(Set<AreaReferenceDto> areaSet) {
		
		this.areaSet = areaSet;
		return this;
	}
	

	public RegionReferenceDto getRegion() {
		return region;
	}

	public void setRegion(RegionReferenceDto region) {
		this.region = region;
	}

	public CampaignFormDataCriteria region(RegionReferenceDto region) {
		this.region = region;
		return this;
	}
	
	public Set<RegionReferenceDto> getRegionSet() {
		return regionSet;
	}

	public void setRegionSet(Set<RegionReferenceDto> regionSet) {
		this.regionSet = regionSet;
	}

	public CampaignFormDataCriteria regionSet(Set<RegionReferenceDto> regionSet) {
		
		this.regionSet = regionSet;
		return this;
	}

	public DistrictReferenceDto getDistrict() {
		return district;
	}

	public void setDistrict(DistrictReferenceDto district) {
		this.district = district;
	}

	public CampaignFormDataCriteria district(DistrictReferenceDto district) {
		this.district = district;
		return this;
	}
	

	public Set<DistrictReferenceDto> getDistrictSet() {
		return districtSet;
	}

	public void setDistrictSet(Set<DistrictReferenceDto> districtSet) {
		this.districtSet = districtSet;
	}
	
	
	public CampaignFormDataCriteria districtSet(Set<DistrictReferenceDto> districtSet) {
		
		this.districtSet = districtSet;
		return this;
	}

	public CommunityReferenceDto getCommunity() {
		return community;
	}

	public void setCommunity(CommunityReferenceDto community) {
		this.community = community;
	}

	public CampaignFormDataCriteria community(CommunityReferenceDto community) {
		this.community = community;
		return this;
	}

	public Date getFormDate() {
		return formDate;
	}

	public void setFormDate(Date formDate) {
		this.formDate = formDate;
	}

	public CampaignFormDataCriteria formDate(Date formDate) {
		this.formDate = formDate;
		return this;
	}

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}
	
	
	//needed for filter purpose

	public CampaignFormMetaReferenceDto getFormPhase() {
		return formPhase;
	}

	public void setFormPhase(CampaignFormMetaReferenceDto formPhase) {
		this.formPhase = formPhase;
	}
	
	public CampaignFormDataCriteria formPhase(CampaignFormMetaReferenceDto formPhase) {
		this.formPhase = formPhase;
		return this;
	}

	public String getError_status() {
		return error_status;
	}

	public void setError_status(String error_status) {
		this.error_status = error_status;
	}

	public String getUserLanguage() {
		return userLanguage;
	}

	public void setUserLanguage(String userLanguage) {
		this.userLanguage = userLanguage;
	}

	public String getUsertype() {
		return usertype;
	}

	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	
	public UserType getUsertypeEnum() {
		return usertypeEnum;
	}

	public void setUsertypeEnum(UserType usertype) {
		this.usertypeEnum = usertype;
	}

	public Boolean getIsVerified() {
		return isVerified;
	}

	public void setIsVerified(Boolean isVerified) {
		this.isVerified = isVerified;
	}
	
	public Boolean getIsPublished() {
		return isPublished;
	}

	public void setIsPublished(Boolean isPublished) {
		this.isPublished = isPublished;
	}
	
	
	
	
}
