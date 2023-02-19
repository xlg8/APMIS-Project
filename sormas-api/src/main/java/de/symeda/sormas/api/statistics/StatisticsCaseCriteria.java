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
package de.symeda.sormas.api.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.IntegerRange;
import de.symeda.sormas.api.Month;
import de.symeda.sormas.api.MonthOfYear;
import de.symeda.sormas.api.Year;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.EpiWeek;

public class StatisticsCaseCriteria implements Serializable {

	private static final long serialVersionUID = 4997176351789123549L;

	private List<Year> onsetYears;
	private List<Year> reportYears;
	private List<Year> outcomeYears;
	private List<Month> onsetMonths;
	private List<Month> reportMonths;
	private List<Month> outcomeMonths;
	private List<EpiWeek> onsetEpiWeeks;
	private List<EpiWeek> reportEpiWeeks;
	private List<EpiWeek> outcomeEpiWeeks;
	
	private List<MonthOfYear> onsetMonthsOfYear;
	private List<MonthOfYear> reportMonthsOfYear;
	private List<MonthOfYear> outcomeMonthsOfYear;
	private List<EpiWeek> onsetEpiWeeksOfYear;
	private List<EpiWeek> reportEpiWeeksOfYear;
	private List<EpiWeek> outcomeEpiWeeksOfYear;
	private Date onsetDateFrom;
	private Date onsetDateTo;
	private Date reportDateFrom;
	private Date reportDateTo;
	private Date outcomeDateFrom;
	private Date outcomeDateTo;
	
	private Boolean sexUnknown;
	private List<IntegerRange> ageIntervals;
	private List<AgeGroup> ageGroups;
	
	private List<RegionReferenceDto> regions;
	private List<DistrictReferenceDto> districts;
	private List<CommunityReferenceDto> communities;
	private List<FacilityReferenceDto> healthFacilities;
	private List<RegionReferenceDto> personRegions;
	private List<DistrictReferenceDto> personDistricts;
	private List<CommunityReferenceDto> personCommunities;
	private String personCity;
	private String personPostcode;
	private List<UserRole> reportingUserRoles;

	public List<Year> getOnsetYears() {
		return onsetYears;
	}

	public List<Year> getReportYears() {
		return reportYears;
	}

	public List<Year> getOutcomeYears() {
		return outcomeYears;
	}

	

	public List<Month> getOnsetMonths() {
		return onsetMonths;
	}

	public List<Month> getReportMonths() {
		return reportMonths;
	}

	public List<Month> getOutcomeMonths() {
		return outcomeMonths;
	}

	public List<EpiWeek> getOnsetEpiWeeks() {
		return onsetEpiWeeks;
	}

	public List<EpiWeek> getReportEpiWeeks() {
		return reportEpiWeeks;
	}

	public List<EpiWeek> getOutcomeEpiWeeks() {
		return outcomeEpiWeeks;
	}

	

	public List<MonthOfYear> getOnsetMonthsOfYear() {
		return onsetMonthsOfYear;
	}

	public List<MonthOfYear> getReportMonthsOfYear() {
		return reportMonthsOfYear;
	}

	public List<MonthOfYear> getOutcomeMonthsOfYear() {
		return outcomeMonthsOfYear;
	}

	public List<EpiWeek> getOnsetEpiWeeksOfYear() {
		return onsetEpiWeeksOfYear;
	}

	public List<EpiWeek> getReportEpiWeeksOfYear() {
		return reportEpiWeeksOfYear;
	}

	public List<EpiWeek> getOutcomeEpiWeeksOfYear() {
		return outcomeEpiWeeksOfYear;
	}

	public Date getOnsetDateFrom() {
		return onsetDateFrom;
	}

	public Date getOnsetDateTo() {
		return onsetDateTo;
	}

	public Date getReportDateFrom() {
		return reportDateFrom;
	}

	public Date getReportDateTo() {
		return reportDateTo;
	}

	public Date getOutcomeDateFrom() {
		return outcomeDateFrom;
	}

	public Date getOutcomeDateTo() {
		return outcomeDateTo;
	}

	

	public Boolean isSexUnknown() {
		return sexUnknown;
	}

	public List<IntegerRange> getAgeIntervals() {
		return ageIntervals;
	}

	public List<AgeGroup> getAgeGroups() {
		return ageGroups;
	}

	

	public List<RegionReferenceDto> getRegions() {
		return regions;
	}

	public List<DistrictReferenceDto> getDistricts() {
		return districts;
	}

	public List<CommunityReferenceDto> getCommunities() {
		return communities;
	}

	public List<FacilityReferenceDto> getHealthFacilities() {
		return healthFacilities;
	}

	public List<RegionReferenceDto> getPersonRegions() {
		return personRegions;
	}

	public List<DistrictReferenceDto> getPersonDistricts() {
		return personDistricts;
	}

	public List<CommunityReferenceDto> getPersonCommunities() {
		return personCommunities;
	}

	public String getPersonCity() {
		return personCity;
	}

	public String getPersonPostcode() {
		return personPostcode;
	}

	public List<UserRole> getReportingUserRoles() {
		return reportingUserRoles;
	}

	

	

	public StatisticsCaseCriteria sexUnknown(Boolean sexUnknown) {
		this.sexUnknown = sexUnknown;
		return this;
	}

	public StatisticsCaseCriteria addAgeIntervals(List<IntegerRange> ageIntervals) {
		if (this.ageIntervals == null) {
			this.ageIntervals = new ArrayList<>();
		}

		this.ageIntervals.addAll(ageIntervals);
		return this;
	}

	public StatisticsCaseCriteria addAgeGroups(List<AgeGroup> ageGroups) {
		if (this.ageGroups == null) {
			this.ageGroups = new ArrayList<>();
		}

		this.ageGroups.addAll(ageGroups);
		return this;
	}

	public StatisticsCaseCriteria regions(List<RegionReferenceDto> regions) {
		this.regions = regions;
		return this;
	}

	public StatisticsCaseCriteria districts(List<DistrictReferenceDto> districts) {
		this.districts = districts;
		return this;
	}

	public StatisticsCaseCriteria communities(List<CommunityReferenceDto> communities) {
		this.communities = communities;
		return this;
	}

	public StatisticsCaseCriteria healthFacilities(List<FacilityReferenceDto> healthFacilities) {
		this.healthFacilities = healthFacilities;
		return this;
	}

	public StatisticsCaseCriteria personRegions(List<RegionReferenceDto> personRegions) {
		this.personRegions = personRegions;
		return this;
	}

	public StatisticsCaseCriteria personDistricts(List<DistrictReferenceDto> personDistricts) {
		this.personDistricts = personDistricts;
		return this;
	}

	public StatisticsCaseCriteria personCommunities(List<CommunityReferenceDto> personCommunities) {
		this.personCommunities = personCommunities;
		return this;
	}

	public StatisticsCaseCriteria setPersonCity(String personCity) {
		this.personCity = personCity;
		return this;
	}

	public StatisticsCaseCriteria setPersonPostcode(String personPostcode) {
		this.personPostcode = personPostcode;
		return this;
	}

	public StatisticsCaseCriteria reportingUserRoles(List<UserRole> reportingUserRoles) {
		this.reportingUserRoles = reportingUserRoles;
		return this;
	}

	

	public boolean hasOnsetDate() {

		return onsetDateFrom != null
			|| onsetDateTo != null
			|| onsetEpiWeeks != null
			|| onsetEpiWeeksOfYear != null
			|| onsetMonths != null
			|| onsetMonthsOfYear != null
			|| onsetYears != null;
	}
}
