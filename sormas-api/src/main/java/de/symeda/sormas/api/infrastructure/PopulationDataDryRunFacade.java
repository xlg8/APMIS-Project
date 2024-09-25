package de.symeda.sormas.api.infrastructure;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.validation.Valid;

import de.symeda.sormas.api.AgeGroup;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.statistics.StatisticsCaseCriteria;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

@Remote
public interface PopulationDataDryRunFacade {

	Integer getDistrictPopulation(String districtUuid, PopulationDataCriteria critariax);
	
	Integer getDistrictPopulationByType(String districtUuid, String campaignUuid, AgeGroup ageGroup);
	
	List<PopulationDataDryRunDto> getDistrictPopulationByTypeUsingUUIDs(String districtUuid, String campaignUuid, AgeGroup ageGroup);
	
	List<PopulationDataDryRunDto> getDistrictModalityByUUIDsandCampaignUUIdAndAgeGroup(String districtUuid, String campaignUuid, AgeGroup ageGroup);
	
//	List<PopulationDataDto> getDistrictStatusByDistrictUuidandCampaignUUIdAndAgeGroup(String districtUuid, String campaignUuid, AgeGroup ageGroup);

	
	Integer getDistrictPopulationByUuidAndAgeGroup(String districtUuid, String campaignUuid, String ageGroup);
	
	String getDistrictModalityByUuidAndCampaignAndAgeGroup(String districtUuid, String campaignUuid,  String ageGroup);
	
	String getDistrictStatusByCampaign(String districtUuid, String campaignUuid, String ageGroup);
	/**
	 * Returns the population of the district, projected to the current point in time based on its growth rate
	 */
	Integer getProjectedDistrictPopulation(String districtUuid, PopulationDataCriteria critariax);

	Integer getRegionPopulation(String regionUuid, PopulationDataCriteria critariax);

	/**
	 * Returns the population of the region, projected to the current point in time based on its growth rate
	 */
	Integer getProjectedRegionPopulation(String regionUuid, PopulationDataCriteria critariax);

	void savePopulationData(@Valid List<PopulationDataDryRunDto> populationDataList) throws ValidationRuntimeException;
	
	List<PopulationDataDryRunDto> getPopulationData(PopulationDataDryRunCriteria criteria);
	
	List<PopulationDataDryRunDto> getAllPopulationData();

	List<Object[]> getPopulationDataForExport(String campaignUuid);
	
	
	void savePopulationList(Set<PopulationDataDryRunDto> savePopulationList);

	/**
	 * Checks whether there is general population data available for all regions and districts
	 */
	List<Long> getMissingPopulationDataForStatistics(
		StatisticsCaseCriteria criteria,
		boolean groupByRegion,
		boolean groupByDistrict,
		boolean groupBySex,
		boolean groupByAgeGroup);

	List<PopulationDataDryRunDto> getPopulationDataImportChecker(PopulationDataDryRunCriteria criteria);

	List<PopulationDataDryRunDto> getPopulationDataWithCriteria(String criteria);

	void savePopulationDatax(@Valid List<PopulationDataDryRunDto> populationDataList,
			@Valid List<PopulationDataFauxDto> fauxPopulationDataList, boolean isFauxData) throws ValidationRuntimeException;
//	void truncateDryRunTable();
	
	
	boolean checkDuplicatePopulationData(String districtExternalID, String AgeGroup, String campaignUUId);

	void truncateDryRunTable();

}
