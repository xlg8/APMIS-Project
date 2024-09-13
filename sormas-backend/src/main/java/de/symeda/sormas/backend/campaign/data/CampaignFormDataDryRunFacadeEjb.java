/*
 * ******************************************************************************
 * * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * *
 * * This program is free software: you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation, either version 3 of the License, or
 * * (at your option) any later version.
 * *
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program. If not, see <https://www.gnu.org/licenses/>.
 * ******************************************************************************
 */

package de.symeda.sormas.backend.campaign.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.campaign.data.CampaignFormDataDryRunDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDryRunFacade;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.campaign.CampaignFacadeEjb;
import de.symeda.sormas.backend.campaign.CampaignService;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaFacadeEjb;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaService;
import de.symeda.sormas.backend.campaign.statistics.CampaignStatisticsService;
import de.symeda.sormas.backend.infrastructure.PopulationDataFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.AreaFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.community.CommunityService;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictService;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.user.UserRoleConfigFacadeEjb.UserRoleConfigFacadeEjbLocal;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "CampaignFormDataDryRunFacade")
public class CampaignFormDataDryRunFacadeEjb implements CampaignFormDataDryRunFacade {

	private FormAccess frmsAccess;

	private Integer popAddiontions = 0;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private CampaignFormDataDryRunService campaignFormDataDryRunService;

//	@EJB
//	private CampaignFormDataService campaignFormDataService;

	@EJB
	private CampaignService campaignService;

	@EJB
	private CampaignFormMetaService campaignFormMetaService;

	@EJB
	private RegionService regionService;

	@EJB
	private DistrictService districtService;

	@EJB
	private CommunityService communityService;

	@EJB
	private UserService userService;

	@EJB
	private UserFacadeEjb.UserFacadeEjbLocal userServiceEBJ;

	@EJB
	private PopulationDataFacadeEjb.PopulationDataFacadeEjbLocal populationDataFacadeEjb;

	@EJB
	private AreaService areaService;

	@EJB
	private AreaFacadeEjb.AreaFacadeEjbLocal areaFacadeEjb;

	@EJB
	private RegionFacadeEjb.RegionFacadeEjbLocal regionFacadeEjb;

	@EJB
	private DistrictFacadeEjb.DistrictFacadeEjbLocal districtFacadeEjb;

	@EJB
	private UserRoleConfigFacadeEjbLocal userRoleConfigFacade;

	@EJB
	private CampaignStatisticsService campaignStatisticsService;

	public Integer populationx;

	public CampaignFormDataDryRun fromDto(@NotNull CampaignFormDataDryRunDto source, boolean checkChangeDate) {
		CampaignFormDataDryRun target = DtoHelper.fillOrBuildEntity(source,
				campaignFormDataDryRunService.getByUuid(source.getUuid()), CampaignFormDataDryRun::new,
				checkChangeDate);

		target.setFormValues(source.getFormValues());
		target.setCampaign(campaignService.getByReferenceDto(source.getCampaign()));
		target.setCampaignFormMeta(campaignFormMetaService.getByReferenceDto(source.getCampaignFormMeta()));
		target.setFormDate(source.getFormDate());
		target.setArea(areaService.getByReferenceDto(source.getArea()));
		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));
		target.setCommunity(communityService.getByReferenceDto(source.getCommunity()));
		target.setCreatingUser(userService.getByReferenceDto(source.getCreatingUser()));
		target.setSource(source.getSource());
		return target;
	}

	public CampaignFormDataDryRunDto toDto(CampaignFormDataDryRun source) {
		if (source == null) {
			return null;
		}

		CampaignFormDataDryRunDto target = new CampaignFormDataDryRunDto();
		DtoHelper.fillDto(target, source);

		target.setFormValues(source.getFormValues());
		target.setCampaign(CampaignFacadeEjb.toReferenceDto(source.getCampaign()));
		target.setCampaignFormMeta(CampaignFormMetaFacadeEjb.toReferenceDto(source.getCampaignFormMeta()));
		target.setFormDate(source.getFormDate());
		target.setArea(AreaFacadeEjb.toReferenceDto(source.getArea()));
		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		target.setCommunity(CommunityFacadeEjb.toReferenceDto(source.getCommunity()));
		target.setCreatingUser(UserFacadeEjb.toReferenceDto(source.getCreatingUser()));
		target.setSource(source.getSource());
		return target;
	}

	public CampaignFormDataDryRunDto toDtoWithArchive(CampaignFormDataDryRun source) {
		if (source == null) {
			return null;
		}

		CampaignFormDataDryRunDto target = new CampaignFormDataDryRunDto();
		DtoHelper.fillDto(target, source);

		target.setFormValues(source.getFormValues());
		target.setCampaign(CampaignFacadeEjb.toReferenceDto(source.getCampaign()));
		target.setCampaignFormMeta(CampaignFormMetaFacadeEjb.toReferenceDto(source.getCampaignFormMeta()));
		target.setFormDate(source.getFormDate());
		target.setArea(AreaFacadeEjb.toReferenceDto(source.getArea()));
		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		target.setCommunity(CommunityFacadeEjb.toReferenceDto(source.getCommunity()));
		target.setCreatingUser(UserFacadeEjb.toReferenceDto(source.getCreatingUser()));
		target.setSource(source.getSource());
		target.setArchived(source.isArchived());
		target.setIspublished(source.isIspublished());
		target.setIsverified(source.isIsverified());

		return target;
	}



	private void validate(CampaignFormDataDryRunDto campaignFormDataDto) {
		if (campaignFormDataDto.getCampaign() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError("Campaign_id now valid!"));
		}
		if (campaignFormDataDto.getArea() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validArea));
		}
		if (campaignFormDataDto.getRegion() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validRegion));
		}
		if (campaignFormDataDto.getDistrict() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validDistrict));
		}
		if (campaignFormDataDto.getCommunity() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validCommunity));
		}
	}



	public List<CampaignFormDataIndexDto> getCreatingUsersUserType(String username) {

		final String fetchUserTypeByUsername = "select usertype from users where username ilike '" + username + "';";

		Query seriesDataQuery = em.createNativeQuery(fetchUserTypeByUsername);

		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<String> resultList = seriesDataQuery.getResultList(); // Change the type to List<String>

		resultData.addAll(
				resultList.stream().map(result -> new CampaignFormDataIndexDto(result)).collect(Collectors.toList()));

		return resultData;
	}

	@LocalBean
	@Stateless
	public static class CampaignFormDataDryRunFacadeEjbLocal extends CampaignFormDataDryRunFacadeEjb {

		public CampaignFormDataDryRunFacadeEjbLocal() {

		}

	}

	@Override
	public CampaignFormDataDryRunDto saveCampaignFormData(@Valid CampaignFormDataDryRunDto dto)	throws ValidationRuntimeException {
		UserReferenceDto currtUsr = userServiceEBJ.getCurrentUserAsReference();
		dto.setCreatingUser(currtUsr);

		CampaignFormDataDryRun campaignFormData = fromDto(dto, true);
		CampaignFormDataEntry.removeNullValueEntries(campaignFormData.getFormValues());

		validate(dto);

		campaignFormDataDryRunService.ensurePersisted(campaignFormData);

		return toDto(campaignFormData);
	}

	@Override
	public void truncateDryRunTable() {
		
//		System.out.println("11111----------------------------------------------Credentials from backend OOOPPPPPP" );

		final String joinBuilder = "truncate table campaignformdatadryrun;";
		
		
		  Query query = em.createNativeQuery(joinBuilder);

		    // Execute the query
		    query.executeUpdate();
//		em.createNativeQuery(joinBuilder);

	}



	
//	final String joinBuilder = "truncate table campaignformdatadryrun;";


}