package de.symeda.sormas.api.infrastructure.area;

import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.GeoLocationFacade;

@Remote
public interface AreaDryRunFacade extends GeoLocationFacade<AreaDryRunDto, AreaDryRunDto, AreaReferenceDto, AreaCriteria> {

	List<AreaReferenceDto> getAllActiveAsReference();

	List<AreaReferenceDto> getAllActiveAsReferencePashto();

	List<AreaReferenceDto> getAllActiveAsReferenceDari();

	List<AreaReferenceDto> getAllActiveAndSelectedAsReference(String campaignUuid);

	List<AreaDryRunDto> getAllActiveAsReferenceAndPopulation(CampaignDto campaignDto);

	List<AreaDryRunDto> getAllActiveAsReferenceAndPopulationPashto(CampaignDto campaignDto);

	List<AreaDryRunDto> getAllActiveAsReferenceAndPopulationsDari(CampaignDto campaignDto);

	List<AreaDryRunDto> getAllActiveAsReferenceAndPopulation();

	boolean isUsedInOtherInfrastructureData(Collection<String> areaUuids);

	List<AreaReferenceDto> getByName(String name, boolean includeArchived);

	List<AreaReferenceDto> getByExternalID(Long ext_id, boolean includeArchived);

	AreaReferenceDto getAreaReferenceByUuid(String uuid);

	ConfigurationChangeLogDto saveAreaChangeLog(ConfigurationChangeLogDto configurationChangeLogDto);
	
	void clearDryRunTable();

}
