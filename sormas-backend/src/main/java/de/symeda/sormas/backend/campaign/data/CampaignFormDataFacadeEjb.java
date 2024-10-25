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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignAggregateDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataFacade;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataReferenceDto;
import de.symeda.sormas.api.campaign.data.MapCampaignDataDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramCriteria;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDataDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramSeries;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElementType;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.PopulationDataCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.CampaignDataExtractDto;
import de.symeda.sormas.api.report.JsonDictionaryReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.campaign.CampaignFacadeEjb;
import de.symeda.sormas.backend.campaign.CampaignService;
import de.symeda.sormas.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaFacadeEjb;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaService;
import de.symeda.sormas.backend.campaign.statistics.CampaignStatisticsService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.PopulationDataFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.community.CommunityService;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictService;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.user.UserRoleConfigFacadeEjb.UserRoleConfigFacadeEjbLocal;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "CampaignFormDataFacade")
public class CampaignFormDataFacadeEjb implements CampaignFormDataFacade {

	private FormAccess frmsAccess;

	private Integer popAddiontions = 0;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private CampaignFormDataService campaignFormDataService;

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

	public CampaignFormData fromDto(@NotNull CampaignFormDataDto source, boolean checkChangeDate) {
		CampaignFormData target = DtoHelper.fillOrBuildEntity(source,
				campaignFormDataService.getByUuid(source.getUuid()), CampaignFormData::new, checkChangeDate);

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

	public CampaignFormDataDto toDto(CampaignFormData source) {
		if (source == null) {
			return null;
		}

		CampaignFormDataDto target = new CampaignFormDataDto();
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

	public CampaignFormDataDto toDtoWithArchive(CampaignFormData source) {
		if (source == null) {
			return null;
		}

		CampaignFormDataDto target = new CampaignFormDataDto();
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

	@Override
	public CampaignFormDataDto saveCampaignFormDataMobile(@Valid CampaignFormDataDto campaignFormDataDto)
			throws ValidationRuntimeException {
		UserReferenceDto currtUsr = userServiceEBJ.getCurrentUserAsReference();
		campaignFormDataDto.setSource("MOBILE");
		campaignFormDataDto.setCreatingUser(currtUsr);
		CampaignFormData campaignFormData = fromDto(campaignFormDataDto, true);
		CampaignFormDataEntry.removeNullValueEntries(campaignFormData.getFormValues());

		validate(campaignFormDataDto);

		campaignFormDataService.ensurePersisted(campaignFormData);
		return toDto(campaignFormData);
	}

	@Override
	public CampaignFormDataDto saveCampaignFormData(@Valid CampaignFormDataDto campaignFormDataDto)
			throws ValidationRuntimeException {
		UserReferenceDto currtUsr = userServiceEBJ.getCurrentUserAsReference();
		campaignFormDataDto.setCreatingUser(currtUsr);

		CampaignFormData campaignFormData = fromDto(campaignFormDataDto, true);
		CampaignFormDataEntry.removeNullValueEntries(campaignFormData.getFormValues());

		validate(campaignFormDataDto);

		campaignFormDataService.ensurePersisted(campaignFormData);

		return toDto(campaignFormData);
	}

	private void validate(CampaignFormDataDto campaignFormDataDto) {
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

	@Override
	public List<CampaignFormDataDto> getByUuids(List<String> uuids) {
		return campaignFormDataService.getByUuids(uuids).stream().map(c -> convertToDto(c))
				.collect(Collectors.toList());
	}

	@Override
	public void deleteCampaignFormData(String campaignFormDataUuid) {
		if (!userService.hasRight(UserRight.CAMPAIGN_FORM_DATA_DELETE)) {
			throw new UnsupportedOperationException(
					"User " + userService.getCurrentUser().getUuid() + "is not allowed to delete Campaign Form Data");
		}

		CampaignFormData campaignFormData = campaignFormDataService.getByUuid(campaignFormDataUuid);
		campaignFormDataService.delete(campaignFormData);
	}

	private CampaignFormDataDto convertToDto(CampaignFormData source) {
		CampaignFormDataDto dto = toDto(source);
		return dto;
	}

	private CampaignFormDataDto convertToDtoWithArchive(CampaignFormData source) {
		CampaignFormDataDto dto = toDtoWithArchive(source);
		return dto;
	}

	@Override
	public CampaignFormDataDto getCampaignFormDataByUuid(String uuid) {
		return toDto(campaignFormDataService.getByUuid(uuid));
	}

	@Override
	public List<CampaignFormDataIndexDto> getCampaignFormDataByCreatingUser(String creatingUser) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataIndexDto> cq = cb.createQuery(CampaignFormDataIndexDto.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);
		Join<CampaignFormData, Campaign> campaignJoin = root.join(CampaignFormData.CAMPAIGN, JoinType.LEFT);
		Join<CampaignFormData, CampaignFormMeta> campaignFormMetaJoin = root.join(CampaignFormData.CAMPAIGN_FORM_META,
				JoinType.LEFT);
		Join<CampaignFormData, Area> areaJoin = root.join(CampaignFormData.AREA, JoinType.LEFT);
		Join<CampaignFormData, Region> regionJoin = root.join(CampaignFormData.REGION, JoinType.LEFT);
		Join<CampaignFormData, District> districtJoin = root.join(CampaignFormData.DISTRICT, JoinType.LEFT);
		Join<CampaignFormData, Community> communityJoin = root.join(CampaignFormData.COMMUNITY, JoinType.LEFT);
		Join<CampaignFormData, User> userJoin = root.join(CampaignFormData.CREATED_BY, JoinType.LEFT);

		cq.multiselect(root.get(CampaignFormData.UUID), campaignJoin.get(Campaign.NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME), root.get(CampaignFormData.FORM_VALUES),
				areaJoin.get(Area.NAME), areaJoin.get(Area.EXTERNAL_ID), regionJoin.get(Region.NAME),
				regionJoin.get(Region.EXTERNAL_ID), districtJoin.get(District.NAME),
				districtJoin.get(District.EXTERNAL_ID), communityJoin.get(Community.NAME),
				communityJoin.get(Community.CLUSTER_NUMBER), communityJoin.get(Community.EXTERNAL_ID),
				root.get(CampaignFormData.FORM_DATE), campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				root.get(CampaignFormData.SOURCE), userJoin.get(User.USER_NAME));

		cq.where(cb.and(cb.equal(userJoin.get(User.USER_NAME), creatingUser)));
		return em.createQuery(cq).getResultList();
	}

	@Override
	public CampaignFormDataDto getCampaignFormDataByCcode(Long ccode) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormData> cq = cb.createQuery(CampaignFormData.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);
//		Join<CampaignFormData, Campaign> campaignJoin = root.join(CampaignFormData.CAMPAIGN, JoinType.LEFT);
//		Join<CampaignFormData, CampaignFormMeta> campaignFormMetaJoin = root.join(CampaignFormData.CAMPAIGN_FORM_META,
//				JoinType.LEFT);
//		Join<CampaignFormData, Area> areaJoin = root.join(CampaignFormData.AREA, JoinType.LEFT);
//		Join<CampaignFormData, Region> regionJoin = root.join(CampaignFormData.REGION, JoinType.LEFT);
//		Join<CampaignFormData, District> districtJoin = root.join(CampaignFormData.DISTRICT, JoinType.LEFT);
		Join<CampaignFormData, Community> communityJoin = root.join(CampaignFormData.COMMUNITY, JoinType.LEFT);
//		Join<CampaignFormData, User> userJoin = root.join(CampaignFormData.CREATED_BY, JoinType.LEFT);

//		cq.multiselect(root.get(CampaignFormData.UUID), campaignJoin.get(Campaign.NAME),
//				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME), root.get(CampaignFormData.FORM_VALUES),
//				areaJoin.get(Area.NAME), areaJoin.get(Area.EXTERNAL_ID), regionJoin.get(Region.NAME),
//				regionJoin.get(Region.EXTERNAL_ID), districtJoin.get(District.NAME),
//				districtJoin.get(District.EXTERNAL_ID), communityJoin.get(Community.NAME),
//				communityJoin.get(Community.CLUSTER_NUMBER), communityJoin.get(Community.EXTERNAL_ID),
//				root.get(CampaignFormData.FORM_DATE), campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
//				root.get(CampaignFormData.SOURCE), userJoin.get(User.USER_NAME));

		cq.where(cb.and(cb.equal(communityJoin.get(Community.EXTERNAL_ID), ccode)));
		return QueryHelper.getFirstResult(em, cq, this::toDto);
	}

	@Override
	public List<CampaignFormDataIndexDto> getCampaignFormDataByCampaignandFormMeta(String campaignid,
			String campaignformmetaid, String district, String community) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataIndexDto> cq = cb.createQuery(CampaignFormDataIndexDto.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);
		Join<CampaignFormData, Campaign> campaignJoin = root.join(CampaignFormData.CAMPAIGN, JoinType.LEFT);
		Join<CampaignFormData, CampaignFormMeta> campaignFormMetaJoin = root.join(CampaignFormData.CAMPAIGN_FORM_META,
				JoinType.LEFT);

		Join<CampaignFormData, Area> areaJoin = root.join(CampaignFormData.AREA, JoinType.LEFT);
		Join<CampaignFormData, Region> regionJoin = root.join(CampaignFormData.REGION, JoinType.LEFT);
		Join<CampaignFormData, District> districtJoin = root.join(CampaignFormData.DISTRICT, JoinType.LEFT);
		Join<CampaignFormData, Community> communityJoin = root.join(CampaignFormData.COMMUNITY, JoinType.LEFT);
		Join<CampaignFormData, User> userJoin = root.join(CampaignFormData.CREATED_BY, JoinType.LEFT);

		cq.multiselect(root.get(CampaignFormData.UUID), campaignJoin.get(Campaign.NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME), root.get(CampaignFormData.FORM_VALUES),
				areaJoin.get(Area.NAME), areaJoin.get(Area.EXTERNAL_ID), regionJoin.get(Region.NAME),
				regionJoin.get(Region.EXTERNAL_ID), districtJoin.get(District.NAME),
				districtJoin.get(District.EXTERNAL_ID), communityJoin.get(Community.NAME),
				communityJoin.get(Community.CLUSTER_NUMBER), communityJoin.get(Community.EXTERNAL_ID),
				root.get(CampaignFormData.FORM_DATE), campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				root.get(CampaignFormData.SOURCE), userJoin.get(User.USER_NAME));

		cq.where(cb.and(cb.equal(campaignJoin.get(Campaign.UUID), campaignid),
				cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.UUID), campaignformmetaid),
				cb.equal(districtJoin.get(District.NAME), district),
				cb.equal(communityJoin.get(Community.NAME), community)));
		return em.createQuery(cq).getResultList();
	}

	@Override
	public boolean isArchived(String campaignFormDataUuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CampaignFormData> from = cq.from(CampaignFormData.class);

		cq.where(cb.and(cb.equal(from.get(CampaignFormData.ARCHIVED), true),
				cb.equal(from.get(AbstractDomainObject.UUID), campaignFormDataUuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();

		return count > 0;
	}

	@Override
	public boolean exists(String uuid) {
		return campaignFormDataService.exists(uuid);
	}

	@Override
	public CampaignFormDataReferenceDto getReferenceByUuid(String uuid) {
		return toReferenceDto(campaignFormDataService.getByUuid(uuid));
	}

	@Override
	public CampaignFormDataDto getExistingData(CampaignFormDataCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormData> cq = cb.createQuery(CampaignFormData.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);

		Predicate filter = CriteriaBuilderHelper.and(cb,
				campaignFormDataService.createCriteriaFilter(criteria, cb, root),
				campaignFormDataService.createUserFilter(cb, cq, root));
		if (filter != null) {
			cq.where(filter);
		}

		cq.orderBy(cb.desc(root.get(CampaignFormData.CHANGE_DATE)));

		return QueryHelper.getFirstResult(em, cq, this::toDto);
	}

	@Override
	public List<CampaignFormDataIndexDto> getIndexList(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {

		// System.out.println((criteria == null) +" ---poiuhgfghj==__==kjhghyujkl---
		// "+criteria.getCampaign());

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataIndexDto> cq = cb.createQuery(CampaignFormDataIndexDto.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);
		Join<CampaignFormData, Campaign> campaignJoin = root.join(CampaignFormData.CAMPAIGN, JoinType.LEFT);
		Join<CampaignFormData, CampaignFormMeta> campaignFormMetaJoin = root.join(CampaignFormData.CAMPAIGN_FORM_META,
				JoinType.LEFT);
		Join<CampaignFormData, Area> areaJoin = root.join(CampaignFormData.AREA, JoinType.LEFT);
		Join<CampaignFormData, Region> regionJoin = root.join(CampaignFormData.REGION, JoinType.LEFT);
		Join<CampaignFormData, District> districtJoin = root.join(CampaignFormData.DISTRICT, JoinType.LEFT);
		Join<CampaignFormData, Community> communityJoin = root.join(CampaignFormData.COMMUNITY, JoinType.LEFT);
		Join<CampaignFormData, User> userJoin = root.join(CampaignFormData.CREATED_BY, JoinType.LEFT);

		cq.multiselect(root.get(CampaignFormData.UUID), campaignJoin.get(Campaign.NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME),
				criteria.getCampaignFormMeta() != null ? root.get(CampaignFormData.FORM_VALUES)
						: cb.nullLiteral(String.class),
				areaJoin.get(Area.NAME), areaJoin.get(Area.EXTERNAL_ID), regionJoin.get(Region.NAME),
				regionJoin.get(Region.EXTERNAL_ID), districtJoin.get(District.NAME),
				districtJoin.get(District.EXTERNAL_ID), communityJoin.get(Community.NAME),
				communityJoin.get(Community.CLUSTER_NUMBER), communityJoin.get(Community.EXTERNAL_ID),
				root.get(CampaignFormData.FORM_DATE), campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				root.get(CampaignFormData.SOURCE), userJoin.get(User.USER_NAME), root.get(CampaignFormData.ISVERIFIED),
				root.get(CampaignFormData.ISPUBLISHED));

		Predicate filter = CriteriaBuilderHelper.and(cb,
				campaignFormDataService.createCriteriaFilter(criteria, cb, root),
				campaignFormDataService.createUserFilter(cb, cq, root));

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {

				System.out.println(sortProperty.propertyName + "sortinpropertynaem ");
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case CampaignFormDataIndexDto.UUID:
				case CampaignFormDataIndexDto.FORM_DATE:
					expression = root.get(sortProperty.propertyName);
					break;
				case CampaignFormDataIndexDto.CAMPAIGN:
					expression = campaignJoin.get(Campaign.NAME);
					break;
				case CampaignFormDataIndexDto.FORM:
					expression = campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME);
					break;
				case CampaignFormDataIndexDto.REGION:
					expression = regionJoin.get(Region.NAME);
					break;
				case CampaignFormDataIndexDto.PCODE:
					expression = regionJoin.get(Region.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.AREA:
					expression = areaJoin.get(Area.NAME);
					break;
				case CampaignFormDataIndexDto.RCODE:
					expression = areaJoin.get(Area.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.DISTRICT:
					expression = districtJoin.get(District.NAME);
					break;
				case CampaignFormDataIndexDto.DCODE:
					expression = districtJoin.get(District.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.COMMUNITY:
					expression = communityJoin.get(Community.NAME);
					break;
				case CampaignFormDataIndexDto.COMMUNITYNUMBER:
					expression = communityJoin.get(Community.CLUSTER_NUMBER);
					break;
				case CampaignFormDataIndexDto.CCODE:
					expression = communityJoin.get(Community.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.FORM_TYPE:
					expression = campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE);
					break;

				case CampaignFormDataIndexDto.ISVERIFIED:
					expression = campaignFormMetaJoin.get(CampaignFormData.ISVERIFIED);
					break;

				case CampaignFormDataIndexDto.ISPUBLISHED:
					expression = campaignFormMetaJoin.get(CampaignFormData.ISPUBLISHED);
					break;

				case CampaignFormDataIndexDto.FORM_VALUES:
					System.out.println("formvaluee eee");
					expression = root.get(sortProperty.propertyName);
					break;
				default:

					System.out.println("defaulttttttt");

					expression = root.get(CampaignFormData.FORM_VALUES + "->> '" + sortProperty.propertyName + "'");
					break;
//					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(root.get(CampaignFormData.CHANGE_DATE)));
		}
		System.out.println("DEBUGGER r567ujhgty8ijyu8dfrf this query " + SQLExtractor.from(em.createQuery(cq)));

		return QueryHelper.getResultList(em, cq, first, max);
	}

	@Override
	public List<CampaignFormDataIndexDto> getIndexListDari(CampaignFormDataCriteria criteria, Integer first,
			Integer max, List<SortProperty> sortProperties) {

		// System.out.println((criteria == null) +" ---poiuhgfghj==__==kjhghyujkl---
		// "+criteria.getCampaign());

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataIndexDto> cq = cb.createQuery(CampaignFormDataIndexDto.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);
		Join<CampaignFormData, Campaign> campaignJoin = root.join(CampaignFormData.CAMPAIGN, JoinType.LEFT);
		Join<CampaignFormData, CampaignFormMeta> campaignFormMetaJoin = root.join(CampaignFormData.CAMPAIGN_FORM_META,
				JoinType.LEFT);
		Join<CampaignFormData, Area> areaJoin = root.join(CampaignFormData.AREA, JoinType.LEFT);
		Join<CampaignFormData, Region> regionJoin = root.join(CampaignFormData.REGION, JoinType.LEFT);
		Join<CampaignFormData, District> districtJoin = root.join(CampaignFormData.DISTRICT, JoinType.LEFT);
		Join<CampaignFormData, Community> communityJoin = root.join(CampaignFormData.COMMUNITY, JoinType.LEFT);
		Join<CampaignFormData, User> userJoin = root.join(CampaignFormData.CREATED_BY, JoinType.LEFT);

		cq.multiselect(root.get(CampaignFormData.UUID), campaignJoin.get(Campaign.NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI),
				criteria.getCampaignFormMeta() != null ? root.get(CampaignFormData.FORM_VALUES)
						: cb.nullLiteral(String.class),
				areaJoin.get(Area.FA_AF), areaJoin.get(Area.EXTERNAL_ID), regionJoin.get(Region.FA_AF),
				regionJoin.get(Region.EXTERNAL_ID), districtJoin.get(District.FA_AF),
				districtJoin.get(District.EXTERNAL_ID), communityJoin.get(Community.NAME),
				communityJoin.get(Community.CLUSTER_NUMBER), communityJoin.get(Community.EXTERNAL_ID),
				root.get(CampaignFormData.FORM_DATE), campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				root.get(CampaignFormData.SOURCE), userJoin.get(User.USER_NAME));

		Predicate filter = CriteriaBuilderHelper.and(cb,
				campaignFormDataService.createCriteriaFilter(criteria, cb, root),
				campaignFormDataService.createUserFilter(cb, cq, root));

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case CampaignFormDataIndexDto.UUID:
				case CampaignFormDataIndexDto.FORM_DATE:
					expression = root.get(sortProperty.propertyName);
					break;
				case CampaignFormDataIndexDto.CAMPAIGN:
					expression = campaignJoin.get(Campaign.NAME);
					break;
				case CampaignFormDataIndexDto.FORM:
					expression = campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME);
					break;
				case CampaignFormDataIndexDto.REGION:
					expression = regionJoin.get(Region.NAME);
					break;
				case CampaignFormDataIndexDto.PCODE:
					expression = regionJoin.get(Region.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.AREA:
					expression = areaJoin.get(Area.NAME);
					break;
				case CampaignFormDataIndexDto.RCODE:
					expression = areaJoin.get(Area.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.DISTRICT:
					expression = districtJoin.get(District.NAME);
					break;
				case CampaignFormDataIndexDto.DCODE:
					expression = districtJoin.get(District.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.COMMUNITY:
					expression = communityJoin.get(Community.NAME);
					break;
				case CampaignFormDataIndexDto.COMMUNITYNUMBER:
					expression = communityJoin.get(Community.CLUSTER_NUMBER);
					break;
				case CampaignFormDataIndexDto.CCODE:
					expression = communityJoin.get(Community.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.FORM_TYPE:
					expression = campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(root.get(CampaignFormData.CHANGE_DATE)));
		} // System.out.println("DEBUGGER r567ujhgty8ijyu8dfrf this query " +
			// SQLExtractor.from(em.createQuery(cq)));

		return QueryHelper.getResultList(em, cq, first, max);
	}

	@Override
	public List<CampaignFormDataIndexDto> getIndexListPashto(CampaignFormDataCriteria criteria, Integer first,
			Integer max, List<SortProperty> sortProperties) {

		// System.out.println((criteria == null) +" ---poiuhgfghj==__==kjhghyujkl---
		// "+criteria.getCampaign());

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataIndexDto> cq = cb.createQuery(CampaignFormDataIndexDto.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);
		Join<CampaignFormData, Campaign> campaignJoin = root.join(CampaignFormData.CAMPAIGN, JoinType.LEFT);
		Join<CampaignFormData, CampaignFormMeta> campaignFormMetaJoin = root.join(CampaignFormData.CAMPAIGN_FORM_META,
				JoinType.LEFT);
		Join<CampaignFormData, Area> areaJoin = root.join(CampaignFormData.AREA, JoinType.LEFT);
		Join<CampaignFormData, Region> regionJoin = root.join(CampaignFormData.REGION, JoinType.LEFT);
		Join<CampaignFormData, District> districtJoin = root.join(CampaignFormData.DISTRICT, JoinType.LEFT);
		Join<CampaignFormData, Community> communityJoin = root.join(CampaignFormData.COMMUNITY, JoinType.LEFT);
		Join<CampaignFormData, User> userJoin = root.join(CampaignFormData.CREATED_BY, JoinType.LEFT);

		cq.multiselect(root.get(CampaignFormData.UUID), campaignJoin.get(Campaign.NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO),
				criteria.getCampaignFormMeta() != null ? root.get(CampaignFormData.FORM_VALUES)
						: cb.nullLiteral(String.class),
				areaJoin.get(Area.PS_AF), areaJoin.get(Area.EXTERNAL_ID), regionJoin.get(Region.PS_AF),
				regionJoin.get(Region.EXTERNAL_ID), districtJoin.get(District.PS_AF),
				districtJoin.get(District.EXTERNAL_ID), communityJoin.get(Community.NAME),
				communityJoin.get(Community.CLUSTER_NUMBER), communityJoin.get(Community.EXTERNAL_ID),
				root.get(CampaignFormData.FORM_DATE), campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				root.get(CampaignFormData.SOURCE), userJoin.get(User.USER_NAME));

		Predicate filter = CriteriaBuilderHelper.and(cb,
				campaignFormDataService.createCriteriaFilter(criteria, cb, root),
				campaignFormDataService.createUserFilter(cb, cq, root));

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case CampaignFormDataIndexDto.UUID:
				case CampaignFormDataIndexDto.FORM_DATE:
					expression = root.get(sortProperty.propertyName);
					break;
				case CampaignFormDataIndexDto.CAMPAIGN:
					expression = campaignJoin.get(Campaign.NAME);
					break;
				case CampaignFormDataIndexDto.FORM:
					expression = campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME);
					break;
				case CampaignFormDataIndexDto.REGION:
					expression = regionJoin.get(Region.NAME);
					break;
				case CampaignFormDataIndexDto.PCODE:
					expression = regionJoin.get(Region.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.AREA:
					expression = areaJoin.get(Area.NAME);
					break;
				case CampaignFormDataIndexDto.RCODE:
					expression = areaJoin.get(Area.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.DISTRICT:
					expression = districtJoin.get(District.NAME);
					break;
				case CampaignFormDataIndexDto.DCODE:
					expression = districtJoin.get(District.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.COMMUNITY:
					expression = communityJoin.get(Community.NAME);
					break;
				case CampaignFormDataIndexDto.COMMUNITYNUMBER:
					expression = communityJoin.get(Community.CLUSTER_NUMBER);
					break;
				case CampaignFormDataIndexDto.CCODE:
					expression = communityJoin.get(Community.EXTERNAL_ID);
					break;
				case CampaignFormDataIndexDto.FORM_TYPE:
					expression = campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(root.get(CampaignFormData.CHANGE_DATE)));
		} // System.out.println("DEBUGGER r567ujhgty8ijyu8dfrf this query " +
			// SQLExtractor.from(em.createQuery(cq)));

		System.out.println("ttDEBUGGER r567ujhgty8ijyu8dfrf this query " + SQLExtractor.from(em.createQuery(cq)));

		return QueryHelper.getResultList(em, cq, first, max);
	}

	@Override
	public String getByCompletionAnalysisCount(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess frms) {

		int isLocked = isUpdateTrakerLocked("completionanalysisview_e");
		if (isLocked == 0) {
			updateTrakerTableCoreTimeStamp("campaignformdata");
			// Logic to check if campaign data has recently been changed, if yes... the
			// analytics will run again ro provide refreshed data
			boolean isAnalyticsOld = campaignStatisticsService.checkChangedDb("campaignformdata",
					"completionanalysisview_e");
			if (isAnalyticsOld) {
				try {
					updateTrakerTable("completionanalysisview_e", true);
					int noUse = prepareAllCompletionAnalysis();
				} finally {
					updateTrakerTable("completionanalysisview_e", false);
				}
			}
		}

		String error_statusFilter = "";
		boolean filterIsNull = criteria.getCampaign() == null;

		String joiner = "";

		if (!filterIsNull) {
			final CampaignReferenceDto campaign = criteria.getCampaign();
			final AreaReferenceDto area = criteria.getArea();
			final RegionReferenceDto region = criteria.getRegion();
			final DistrictReferenceDto district = criteria.getDistrict();
			final String error_status = criteria.getError_status();

			//@formatter:off
			
			final String campaignFilter = campaign != null ? "campaignfo0_x.uuid = '"+campaign.getUuid()+"'" : "";

			final String areaFilter = area != null ? " AND  area3_x.uuid = '"+area.getUuid()+"'" : "";
			final String regionFilter = region != null ? " AND region4_x.uuid = '"+region.getUuid()+"'" : "";
			final String districtFilter = district != null ? " AND district5_x.uuid = '"+district.getUuid()+"'" : "";
			
			if(error_status != null) {
				error_statusFilter = "WHERE error_status = '" +error_status + "'" ;
				System.out.println(error_statusFilter+" =========errrrrooor status ============ ");

					}
			
			joiner = "where "+campaignFilter +areaFilter + regionFilter + districtFilter ;
			
//			System.out.println(campaignFilter+" ===================== "+joiner);
		} 
		
		
		
		
//		final String joinBuilder = "select count(*)\n"
//				+ "from completionAnalysisView_e analyticz\n"
//				+ "left outer join community commut on analyticz.community_id = commut.id\n"
//				+ "left outer join District district5_x on commut.district_id=district5_x.id\n"
//				+ "left outer join Region region4_x on district5_x.region_id=region4_x.id\n"
//				+ "left outer join areas area3_x on region4_x.area_id=area3_x.id\n"
//				+ "left outer join ( SELECT id, uuid  FROM campaigns) campaignfo0_x on analyticz.campaign_id=campaignfo0_x.id\n"
//				
//				+ ""+joiner+"\n";
		
		final String joinBuilder = "WITH cte AS (select area3_x.\"name\" as area_, region4_x.\"name\" as region_, district5_x.\"name\" as district_, commut.clusternumber as clusternumber_, commut.externalid as ccode,\n"
				+ "analyticz.supervisor, analyticz.revisit, analyticz.household, analyticz.teammonitori, \n"
				+ "CASE\n"
				+ "        WHEN\n"
				+ "            analyticz.supervisor = 0 OR\n"
				+ "            analyticz.revisit = 0 OR\n"
				+ "            analyticz.household = 0 OR\n"
				+ "            analyticz.teammonitori = 0\n"
				+ "        THEN 'Error Report'\n"
				+ "        ELSE 'None Error Report'\n"
				+ "    END AS error_status\n"
				+ "from completionAnalysisView_e analyticz\n"
				+ "left outer join community commut on analyticz.community_id = commut.id\n"
				+ "left outer join District district5_x on commut.district_id=district5_x.id\n"
				+ "left outer join Region region4_x on district5_x.region_id=region4_x.id\n"
				+ "left outer join areas area3_x on region4_x.area_id=area3_x.id\n"
				+ "left outer join  ( SELECT id, uuid  FROM campaigns)campaignfo0_x on analyticz.campaign_id=campaignfo0_x.id\n"
				
				+ ""+joiner+" "+") select count(*) from cte"+ " "+ error_statusFilter +";";
		
//		System.out.println(joinBuilder+" ===========JOINBUILDERRRR========== "+joiner);
	return ((BigInteger) em.createNativeQuery(joinBuilder).getSingleResult()).toString();
	}
	
	 
	
	@Override
	public List<CampaignFormDataIndexDto> getByCompletionAnalysis(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess frms) {
		String error_statusFilter ="";
		
		
		
		boolean filterIsNull = criteria.getCampaign() == null ;
		
		String whereclause = "";
		
		if(!filterIsNull) {
		final CampaignReferenceDto campaign = criteria.getCampaign();
		final AreaReferenceDto area = criteria.getArea();
		final RegionReferenceDto region = criteria.getRegion();
		final DistrictReferenceDto district = criteria.getDistrict();
		final String error_status = criteria.getError_status();
		
		
		
		
		//@formatter:off
		

		
		final String campaignFilter = campaign != null ? "campaignfo0_x.uuid = '"+campaign.getUuid()+"'" : "";
		final String areaFilter = area != null ? "AND area3_x.uuid = '"+area.getUuid()+"'" : "";
		final String regionFilter = region != null ? " AND region4_x.uuid = '"+region.getUuid()+"'" : "";
		final String districtFilter = district != null ? " AND district5_x.uuid = '"+district.getUuid()+"'" : "";
		if(error_status != null) {
			error_statusFilter = "WHERE error_status = '" +error_status + "'" ;
			System.out.println(error_statusFilter+" =========errrrrooor status ============ "+whereclause);

				}
		

		
		whereclause = "where " + campaignFilter + areaFilter + regionFilter + districtFilter ;
		
		
		
		
		System.out.println(campaignFilter+" ===================== "+whereclause);
		}
		String addedWhere = "";
		
		
		if(!filterIsNull) {
			
			whereclause = whereclause;
//			+" and (analyticz.supervisor = 0 or analyticz.revisit = 0 or analyticz.household = 0 or analyticz.teammonitori = 0)";
			
		} 
//		else {
//			whereclause = "where analyticz.supervisor = 0 or analyticz.revisit = 0 or analyticz.household = 0 or analyticz.teammonitori = 0";
//		}

		String orderby = "";

		if (sortProperties != null && sortProperties.size() > 0) {
			for (SortProperty sortProperty : sortProperties) {
				switch (sortProperty.propertyName) {
				case "region":
					orderby = orderby.isEmpty() ? " order by area_ " + (sortProperty.ascending ? "asc" : "desc") : orderby+", area_ " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "province":
					orderby = orderby.isEmpty() ? " order by region_ " + (sortProperty.ascending ? "asc" : "desc") : orderby+", region_ " + (sortProperty.ascending ? "asc" : "desc");
				break;
					
				case "district":
					orderby = orderby.isEmpty() ? " order by district_ " + (sortProperty.ascending ? "asc" : "desc") : orderby+", district_ " + (sortProperty.ascending ? "asc" : "desc");
				break;	
				
				case "clusterNumber":
					orderby = orderby.isEmpty() ? " order by clusternumber_ " + (sortProperty.ascending ? "asc" : "desc") : orderby+", clusternumber_ " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "ccode":
					orderby = orderby.isEmpty() ? " order by ccode " + (sortProperty.ascending ? "asc" : "desc") : orderby+", ccode " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "supervisor":
					orderby = orderby.isEmpty() ? " order by supervisor " + (sortProperty.ascending ? "asc" : "desc") : orderby+", supervisor " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "revisit":
					orderby = orderby.isEmpty() ? " order by revisit " + (sortProperty.ascending ? "asc" : "desc") : orderby+", revisit " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "household":
					orderby = orderby.isEmpty() ? " order by household " + (sortProperty.ascending ? "asc" : "desc") : orderby+", household " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "teammonitori":
					orderby = orderby.isEmpty() ? " order by teammonitori " + (sortProperty.ascending ? "asc" : "desc") : orderby+", teammonitori " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "errorfilter":
					orderby = orderby.isEmpty() ? " order by error_status " + (sortProperty.ascending ? "asc" : "desc") : orderby+", error_status " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				
				
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				
			}
		}
		
		System.out.println(" ====orderbyorderbyderby====== "+orderby);
		
		
		
		
		
		
		
		
		final String joinBuilder = "WITH cte AS (select area3_x.\"name\" as area_, region4_x.\"name\" as region_, district5_x.\"name\" as district_, commut.clusternumber as clusternumber_, commut.externalid as ccode,\n"
				+ "analyticz.supervisor, analyticz.revisit, analyticz.household, analyticz.teammonitori, \n"
				+ "CASE\n"
				+ "        WHEN\n"
				+ "            analyticz.supervisor = 0 OR\n"
				+ "            analyticz.revisit = 0 OR\n"
				+ "            analyticz.household = 0 OR\n"
				+ "            analyticz.teammonitori = 0\n"
				+ "        THEN 'Error Report'\n"
				+ "        ELSE 'None Error Report'\n"
				+ "    END AS error_status\n"
				+ "from completionAnalysisView_e analyticz\n"
				+ "left outer join community commut on analyticz.community_id = commut.id\n"
				+ "left outer join District district5_x on commut.district_id=district5_x.id\n"
				+ "left outer join Region region4_x on district5_x.region_id=region4_x.id\n"
				+ "left outer join areas area3_x on region4_x.area_id=area3_x.id\n"
				+ "left outer join  ( SELECT id, uuid  FROM campaigns)campaignfo0_x on analyticz.campaign_id=campaignfo0_x.id\n"
				
				+ ""+whereclause+" "+") select * from cte"+ " "+ error_statusFilter +addedWhere+" \n"
				
				+ orderby
				+ " limit "+max+" offset "+first+";";
		
	System.out.println("=====seriesDataQuery======== "+joinBuilder);
		
		
		Query seriesDataQuery = em.createNativeQuery(joinBuilder);
		
		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();
		
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList(); 
		
	System.out.println("starting....");
		
		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignFormDataIndexDto(
						(String) result[0].toString(), 
						(String) result[1].toString(),
						(String) result[2].toString(),
						"", 
						(Integer) result[3], 
						((BigInteger) result[4]).longValue(),  
						((BigInteger) result[5]).longValue(), 
						((BigInteger) result[6]).longValue(), 
						((BigInteger) result[7]).longValue(),
						((BigInteger) result[8]).longValue(),
					
						(String) result[9].toString()
				)).collect(Collectors.toList()));
		
	//	//System.out.println("ending...." +resultData.size());
	
	
	////System.out.println("resultData - "+ resultData.toString()); //SQLExtractor.from(seriesDataQuery));
	return resultData;
	}
	
	private void updateTrakerTable() {
		// get the total size of the analysis
		final String joinBuilder = "INSERT INTO tracktableupdates (table_name, last_updated)\n"
				+ "    VALUES ('lateformdataportion', NOW())\n" + "    ON CONFLICT (table_name)\n"
				+ "    DO UPDATE SET last_updated = NOW();";

		em.createNativeQuery(joinBuilder).executeUpdate();

	};
	
	@Override
	public String getByTimelinessAnalysisCount(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess frms) {

		
		int isLocked = isUpdateTrakerLocked("lateformdataportion");
		if(isLocked == 0){
			boolean isAnalyticsOld = campaignStatisticsService.checkChangedDb("campaignformdata", "lateformdataportion");
			System.out.println(isAnalyticsOld + " =========dcccccccccccccccTotalAnalyticsOld===--------------------: " );

				if (isAnalyticsOld) {
					try {
						updateTrakerTable("lateformdataportion", true);
						
						updateTrakerTable();
						String updateAnalysisSql = "REFRESH MATERIALIZED VIEW  lateformdataportion  with data ;"
								+ "REFRESH MATERIALIZED VIEW  lateformdatatotal  with data ;";
						em.createNativeQuery(updateAnalysisSql).executeUpdate();

					} catch (Exception e) {
						
					} finally {
						updateTrakerTable("lateformdataportion", false);
					}
				}
		}

		
		
		boolean filterIsNull = criteria.getCampaign() == null;

		String joiner = "";

		if (!filterIsNull) {
			final CampaignReferenceDto campaign = criteria.getCampaign();
			final CampaignFormMetaReferenceDto form = criteria.getCampaignFormMeta();
//			System.out.println(  "campaign fromr filter value ======================" + form);

			final AreaReferenceDto area = criteria.getArea();
			final RegionReferenceDto region = criteria.getRegion();
			final DistrictReferenceDto district = criteria.getDistrict();
//			final String error_status = criteria.getError_status();
			

			//@formatter:off
			
			final String campaignFilter = campaign != null ? "campaigns.name = '"+campaign.getCaption()+"'" : "";
			String getFormNameByLanguage = "";
			if(criteria.getUserLanguage().equalsIgnoreCase("pashto")) {
				getFormNameByLanguage = " AND formmeta.formname_ps_af = '";
			}else if(criteria.getUserLanguage().equalsIgnoreCase("dari")) {
				getFormNameByLanguage = " AND formmeta.formname_fa_af = '";
			}else {
				getFormNameByLanguage =" AND formmeta.formname = '";
			}
			final String campaignFormFilter = form != null ? getFormNameByLanguage +form.getCaption()+"'" : "";
//			System.out.println( campaignFormFilter + "AND campaign fromr filter value ======================" + form);

			final String areaFilter = area != null ? " AND areas.name = '"+area.getCaption()+"'" : "";
			final String regionFilter = region != null ? " AND region.name = '"+region.getCaption()+"'" : "";
			final String districtFilter = district != null ? " AND district.name = '"+district.getCaption()+"'" : "";
			
			joiner = " where " + campaignFilter + campaignFormFilter + areaFilter + regionFilter + districtFilter ;
			
		} 
		
	
		String formNameByUserLanguageJoiner = "";
		
		if(criteria.getUserLanguage() != null) {
			if(criteria.getUserLanguage().equalsIgnoreCase("pashto")) {
				formNameByUserLanguageJoiner = 	"formmeta.formname_ps_af";
			}else if(criteria.getUserLanguage().equalsIgnoreCase("dari")) {
				formNameByUserLanguageJoiner = 	"formmeta.formname_fa_af";
			}else {
				formNameByUserLanguageJoiner = 	"formmeta.formname";

			}
		}else {
			formNameByUserLanguageJoiner = 	"formmeta.formname";
		}
		

		
		
		final String joinBuilder = "WITH cte AS ( " 
				+ "SELECT dst.count, dst.formmetauuid, dst.campaignuuid, dst.district_id, campaigns.name, \n" 
				+  formNameByUserLanguageJoiner + ", areas.name, region.name, district.name,\r\n"
				+ "    trunc(cast((( SELECT lp.count\r\n"
				+ "           FROM lateformdataportion lp\r\n"
				+ "         WHERE lp.district_id = dst.district_id\r\n"
				+ "          and lp.campaignuuid = dst.campaignuuid\r\n"
				+ "          and lp.formmetauuid = dst.formmetauuid\r\n"
				+ "         LIMIT 1)) as numeric) / cast((( SELECT lt.count\r\n"
				+ "           FROM lateformdatatotal lt\r\n"
				+ "          WHERE lt.district_id = dst.district_id\r\n"
				+ "          and lt.campaignuuid = dst.campaignuuid\r\n"
				+ "          and lt.formmetauuid = dst.formmetauuid\r\n"
				+ "         LIMIT 1)) as numeric) * cast(100 as numeric), 2) AS perc\r\n"
				+ "   FROM lateformdataportion dst\r\n"
				+ "LEFT JOIN campaigns ON dst.campaignuuid = campaigns.uuid \r\n"
				+ "left join campaignformmeta formmeta on dst.formmetauuid  = formmeta.uuid\r\n"
				+ "LEFT JOIN district ON dst.district_id = district.id\r\n"
				+ "LEFT JOIN region ON district.region_id = region.id\r\n"
				+ "LEFT JOIN areas ON region.area_id = areas.id " 
				+ joiner +" ) select count(*) from cte ;";
		
		System.out.println(joinBuilder+" ===========JOINBUILDERRRR========== ");
		

	return ((BigInteger) em.createNativeQuery(joinBuilder).getSingleResult()).toString();
	}
	
	
	@Override
	public List<CampaignFormDataIndexDto> getByTimelinessAnalysis(CampaignFormDataCriteria criteria, Integer first,
			Integer max, List<SortProperty> sortProperties, FormAccess frm) {
	
			
		
		boolean filterIsNull = criteria.getCampaign() == null ;
		
		String whereclause = "";
		
		if(!filterIsNull) {
			final CampaignReferenceDto campaign = criteria.getCampaign();
			final CampaignFormMetaReferenceDto form = criteria.getCampaignFormMeta();
			System.out.println(  "campaign fromr filter value ======================" + form);

			final AreaReferenceDto area = criteria.getArea();
			final RegionReferenceDto region = criteria.getRegion();
			final DistrictReferenceDto district = criteria.getDistrict();
//			final String error_status = criteria.getError_status();


			//@formatter:off
			
			final String campaignFilter = campaign != null ? "campaigns.name = '"+campaign.getCaption()+"'" : "";
			
			String getFormNameByLanguage = "";
			if(criteria.getUserLanguage().equalsIgnoreCase("pashto")) {
				getFormNameByLanguage = " AND formmeta.formname_ps_af = '";
			}else if(criteria.getUserLanguage().equalsIgnoreCase("dari")) {
				getFormNameByLanguage = " AND formmeta.formname_fa_af = '";
			}else {
				getFormNameByLanguage =" AND formmeta.formname = '";
			}
			final String campaignFormFilter = form != null ? getFormNameByLanguage +form.getCaption()+"'" : "";
//			System.out.println( campaignFormFilter + "AND campaign fromr filter value ======================" + form);

			final String areaFilter = area != null ? " AND areas.name = '"+area.getCaption()+"'" : "";
			final String regionFilter = region != null ? " AND region.name = '"+region.getCaption()+"'" : "";
			final String districtFilter = district != null ? " AND district.name = '"+district.getCaption()+"'" : "";
			
			whereclause = " where " + campaignFilter + campaignFormFilter + areaFilter + regionFilter + districtFilter ;
		
		
//		System.out.println(campaignFilter+" ===================== "+whereclause);
		}
		String addedWhere = "";
		
		
		if(!filterIsNull) {
			
			whereclause = whereclause;
			
		} 


		String orderby = "";

		if (sortProperties != null && sortProperties.size() > 0) {
			
			System.out.println(sortProperties +  " sort propertiesn by ejbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
			for (SortProperty sortProperty : sortProperties) {
				
				System.out.println(sortProperty.propertyName +  " sort propertiesn by ejbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
	
				switch (sortProperty.propertyName) {
				
				
				case "capaignname":
					orderby = orderby.isEmpty() ? " order by capaignname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", capaignname " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "formname":
				
				if(criteria.getUserLanguage() != null) {
					if(criteria.getUserLanguage().equalsIgnoreCase("pashto")) {
						orderby = orderby.isEmpty() ? " order by formmeta.formname_ps_af " + (sortProperty.ascending ? "asc" : "desc") : orderby+", formmeta.formname_ps_af " + (sortProperty.ascending ? "asc" : "desc");
					}else if(criteria.getUserLanguage().equalsIgnoreCase("dari")) {
						orderby = orderby.isEmpty() ? " order by formmeta.formname_fa_af " + (sortProperty.ascending ? "asc" : "desc") : orderby+", formmeta.formname_fa_af " + (sortProperty.ascending ? "asc" : "desc");
					}else {
						orderby = orderby.isEmpty() ? " order by formmeta.formname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", formmeta.formname " + (sortProperty.ascending ? "asc" : "desc");
					}				
				}else {
					orderby = orderby.isEmpty() ? " order by formmeta.formname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", formmeta.formname " + (sortProperty.ascending ? "asc" : "desc");

				}
				
				break;
				
				case "region":
					orderby = orderby.isEmpty() ? " order by areaname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", areaname " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "province":
					orderby = orderby.isEmpty() ? " order by regionname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", regionname " + (sortProperty.ascending ? "asc" : "desc");
				break;
					
				case "district":
					orderby = orderby.isEmpty() ? " order by districtname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", districtname " + (sortProperty.ascending ? "asc" : "desc");
				break;	
				
				case "latecount":
					orderby = orderby.isEmpty() ? " order by dst.count " + (sortProperty.ascending ? "asc" : "desc") : orderby+", dst.count " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "percentage":
					orderby = orderby.isEmpty() ? " order by perc " + (sortProperty.ascending ? "asc" : "desc") : orderby+", perc " + (sortProperty.ascending ? "asc" : "desc");
				break;

				
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				
			}
		}
		
String formNameByUserLanguageJoiner = "";
		
if(criteria.getUserLanguage() != null) {
	if(criteria.getUserLanguage().equalsIgnoreCase("pashto")) {
		formNameByUserLanguageJoiner = 	"formmeta.formname_ps_af";
	}else if(criteria.getUserLanguage().equalsIgnoreCase("dari")) {
		formNameByUserLanguageJoiner = 	"formmeta.formname_fa_af";
	}else {
		formNameByUserLanguageJoiner = 	"formmeta.formname";

	}
}else {
	formNameByUserLanguageJoiner = 	"formmeta.formname";
}
		
		
		
		final String joinBuilder = "SELECT dst.formmetauuid, dst.campaignuuid, dst.district_id, campaigns.name as capaignname, \n" 
				+ formNameByUserLanguageJoiner + ", areas.name as areaname, region.name as regionname, district.name as districtname, dst.count, \r\n"
				+ "    trunc(cast((( SELECT lp.count\r\n"
				+ "           FROM lateformdataportion lp\r\n"
				+ "         WHERE lp.district_id = dst.district_id\r\n"
				+ "          and lp.campaignuuid = dst.campaignuuid\r\n"
				+ "          and lp.formmetauuid = dst.formmetauuid\r\n"
				+ "         LIMIT 1)) as numeric) / cast((( SELECT lt.count\r\n"
				+ "           FROM lateformdatatotal lt\r\n"
				+ "          WHERE lt.district_id = dst.district_id\r\n"
				+ "          and lt.campaignuuid = dst.campaignuuid\r\n"
				+ "          and lt.formmetauuid = dst.formmetauuid\r\n"
				+ "         LIMIT 1)) as numeric) * cast(100 as numeric), 2) AS perc\r\n"
				+ "   FROM lateformdataportion dst\r\n"
				+ "LEFT JOIN campaigns ON dst.campaignuuid = campaigns.uuid \r\n"
				+ "left join campaignformmeta formmeta on dst.formmetauuid  = formmeta.uuid\r\n"
				+ "LEFT JOIN district ON dst.district_id = district.id\r\n"
				+ "LEFT JOIN region ON district.region_id = region.id\r\n"

				+ "LEFT JOIN areas ON region.area_id = areas.id" + whereclause 
//				
				+ orderby
				
				+ " limit "+max+" offset "+first+";";
//		
	System.out.println( max + "dfdf"+ first +"=====seriesDataQuery======== "+joinBuilder );
		
		
		Query seriesDataQuery = em.createNativeQuery(joinBuilder);
		
		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();
		
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList(); 
		
	System.out.println("starting....");
		
		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignFormDataIndexDto(
						(String) result[0].toString(), 
						(String) result[1].toString(),
						((BigInteger) result[2]).longValue(), 
						(String) result[3].toString(),
						(String) result[4].toString(),
						(String) result[5].toString(), 
						(String) result[6].toString(), 
						(String) result[7].toString(), 
						((BigInteger) result[8]).longValue(), 
						((BigDecimal) result[9]).longValue()
				)).collect(Collectors.toList()));
		
	//	//System.out.println("ending...." +resultData.size());
	
	
//	System.out.println("resultData - " + SQLExtractor.from(seriesDataQuery));
	return resultData;
	}
	
	
	
	
	
	@Override
	public List<CampaignFormDataIndexDto> getFlwDuplicateErrorAnalysis(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		String error_statusFilter ="";

		boolean filterIsNull = criteria.getCampaign() == null ;
		
		String whereclause = "";
		
		if(!filterIsNull) {
		final CampaignReferenceDto campaign = criteria.getCampaign();
		final AreaReferenceDto area = criteria.getArea();
		final RegionReferenceDto region = criteria.getRegion();
		final DistrictReferenceDto district = criteria.getDistrict();
		final String error_status = criteria.getError_status();
		
		
		
		
		//@formatter:off
		

		
		final String campaignFilter = campaign != null ? "campaigns.uuid = '"+campaign.getUuid()+"'" : "";
		final String areaFilter = area != null ? "AND areas.uuid = '"+area.getUuid()+"'" : "";
		final String regionFilter = region != null ? " AND region.uuid = '"+region.getUuid()+"'" : "";
		final String districtFilter = district != null ? " AND district.uuid = '"+district.getUuid()+"'" : "";
		if(error_status != null) {
			error_statusFilter = "and error_status = '" +error_status + "'" ;
			System.out.println(error_statusFilter+" =========errrrrooor status ============ "+whereclause);

				}
		

		
		whereclause = "and " + campaignFilter + areaFilter + regionFilter + districtFilter ;
		
		
		
		
		System.out.println(campaignFilter+" ===================== "+whereclause);
		}
		String addedWhere = "";
		
		
		if(!filterIsNull) {
			
			whereclause = whereclause;
//			+" and (analyticz.supervisor = 0 or analyticz.revisit = 0 or analyticz.household = 0 or analyticz.teammonitori = 0)";
			
		} 
//		else {
//			whereclause = "where analyticz.supervisor = 0 or analyticz.revisit = 0 or analyticz.household = 0 or analyticz.teammonitori = 0";
//		}

		String orderby = "";

		if (sortProperties != null && sortProperties.size() > 0) {
			for (SortProperty sortProperty : sortProperties) {
				switch (sortProperty.propertyName) {
				case "region":
					orderby = orderby.isEmpty() ? " order by areas.name " + (sortProperty.ascending ? "asc" : "desc") : orderby+", areas.name " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "province":
					orderby = orderby.isEmpty() ? " order by region.name " + (sortProperty.ascending ? "asc" : "desc") : orderby+", region.name " + (sortProperty.ascending ? "asc" : "desc");
				break;
					
				case "district":
					orderby = orderby.isEmpty() ? " order by district.name " + (sortProperty.ascending ? "asc" : "desc") : orderby+", district.name " + (sortProperty.ascending ? "asc" : "desc");
				break;	
				
				case "clusterNumber":
					orderby = orderby.isEmpty() ? " order by community.clusternumber " + (sortProperty.ascending ? "asc" : "desc") : orderby+", community.clusternumber " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "ccode":
					orderby = orderby.isEmpty() ? " order by community.externalid " + (sortProperty.ascending ? "asc" : "desc") : orderby+", community.externalid " + (sortProperty.ascending ? "asc" : "desc");
				break;
								
				case "creatinguser":
					orderby = orderby.isEmpty() ? " order by users.firstname " + (sortProperty.ascending ? "asc" : "desc") : orderby+", users.firstname " + (sortProperty.ascending ? "asc" : "desc");
				break;
//				
				case "title":
					orderby = orderby.isEmpty() ? " order by users.userposition " + (sortProperty.ascending ? "asc" : "desc") : orderby+", users.userposition " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				
			}
		}
		
		System.out.println(" ====orderbyorderbyderby====== "+orderby);
		
		
		
		
		
		
		
		
		final String joinBuilder = ""
				+ " SELECT \n"
				+ "    areas.name AS area,\n"
				+ "    region.name AS region,\n"
				+ "    district.name AS district,\n"
				+ "    community.clusternumber AS clusterNo,\n"
				+ "    community.externalid AS ccode,\n"
				+ "    users.firstname as firstName,\n"
				+ "    users.userposition as title,\n"
				+ "    TO_CHAR(CAST(jsondata.value ->> 'value' AS NUMERIC), 'FM999999999999999999') as tazkiraNumber, \n"
				 + "    CASE \n"
				    + "        WHEN EXISTS (\n"
				    + "            SELECT 1\n"
				    + "            FROM campaignformdata c2\n"
				    + "            CROSS JOIN LATERAL json_array_elements(c2.formvalues) j2(value)\n"
				    + "            WHERE (j2.value ->> 'id') = 'TazkiraNo'\n"
				    + "            AND (j2.value ->> 'value') = (jsondata.value ->> 'value')\n"
				    + "            AND c2.id != campaignformdata.id\n"
				    + "        ) THEN 'Error: Duplicate Tazkira number'\n"
				    + "        ELSE 'No Error'\n"
				    + "    END as error_status\n"
				+ "FROM campaignformdata\n"
				+ "LEFT JOIN campaignformmeta ON campaignformdata.campaignformmeta_id = campaignformmeta.id\n"
				+ "LEFT JOIN region ON campaignformdata.region_id = region.id\n"
				+ "LEFT JOIN areas ON campaignformdata.area_id = areas.id\n"
				+ "LEFT JOIN district ON campaignformdata.district_id = district.id\n"
				+ "LEFT JOIN community ON campaignformdata.community_id = community.id\n"
				+ "LEFT JOIN users ON campaignformdata.creatinguser_id = users.id\n"
				+ "LEFT JOIN campaigns ON campaignformdata.campaign_id = campaigns.id,\n"
				+ "LATERAL json_array_elements(campaignformdata.formvalues) jsondata(value),\n"
				+ "LATERAL json_array_elements(campaignformmeta.campaignformelements) jsonmeta(value)\n"
				+ "WHERE \n"
				+ "    (jsondata.value ->> 'id') IN ('TazkiraNo') AND\n"
				+ "    (jsondata.value ->> 'id') = (jsonmeta.value ->> 'id') \n"
				+whereclause+"\n"
				+ "    and campaignformdata.id in (\n"
				+ "	    SELECT CAST(unnest(string_to_array(array_to_string(array_agg (id),', '), ',')) AS bigint) AS individual_values\n"
				+ "		FROM flwduplicateerrorreport\n"
				+ "		GROUP BY value\n"
				+ "		HAVING COUNT(*) > 1\n"
				+ "    ) "
				
				+ orderby
				+ " limit "+max+" offset "+first+";";
		
	System.out.println("=====seriesDataQuery======== "+joinBuilder);
		
		
		Query seriesDataQuery = em.createNativeQuery(joinBuilder);
		
		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();
		
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList(); 
		
	System.out.println("starting....");
		
	
	//public CampaignFormDataIndexDto(String area, String region, String district, Integer clusternumber, Long ccode,
	//String source, String creatingUser, String title, String error_status) {
	
	resultData.addAll(resultList.stream()
			.map((result) -> new CampaignFormDataIndexDto(
						(String) result[0].toString(), 
						(String) result[1].toString(),
						(String) result[2].toString(),
						(Integer) result[3], 
						((BigInteger) result[4]).longValue(),  
						((String) result[5]).toString(), 
						((String) result[6]).toString(), 
						((String) result[7]).toString() != null 
						|| !((String) result[7]).toString().isEmpty() 
						? ((String) result[7]).toString() 
								: "No Title",
						((String) result[8]).toString()
					)).collect(Collectors.toList()));
		
	return resultData;
	}
	
	
	

	
	@Override
	public int getFlwDuplicateErrorAnalysisCount(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		String error_statusFilter ="";

		boolean filterIsNull = criteria.getCampaign() == null ;
		
		String whereclause = "";
		
		if(!filterIsNull) {
		final CampaignReferenceDto campaign = criteria.getCampaign();
		final AreaReferenceDto area = criteria.getArea();
		final RegionReferenceDto region = criteria.getRegion();
		final DistrictReferenceDto district = criteria.getDistrict();
		final String error_status = criteria.getError_status();
		
		
		
		
		//@formatter:off
		

		
		final String campaignFilter = campaign != null ? "campaigns.uuid = '"+campaign.getUuid()+"'" : "";
		final String areaFilter = area != null ? "AND areas.uuid = '"+area.getUuid()+"'" : "";
		final String regionFilter = region != null ? " AND region.uuid = '"+region.getUuid()+"'" : "";
		final String districtFilter = district != null ? " AND district.uuid = '"+district.getUuid()+"'" : "";
		if(error_status != null) {
			error_statusFilter = "and error_status = '" +error_status + "'" ;
			System.out.println(error_statusFilter+" =========errrrrooor status ============ "+whereclause);

				}
		

		
		whereclause = "and " + campaignFilter + areaFilter + regionFilter + districtFilter ;
	
		System.out.println(campaignFilter+" ===================== "+whereclause);
		}
		String addedWhere = "";
		
		
		if(!filterIsNull) {
			
			whereclause = whereclause;
	
		} 

		final String joinBuilder = ""
				+ "SELECT count(*) \n"
//				+ "    areas.name AS area,\n"
//				+ "    region.name AS region,\n"
//				+ "    district.name AS district,\n"
//				+ "    community.clusternumber AS clusterNo,\n"
//				+ "    community.externalid AS ccode,\n"
//				+ "    users.firstname as firstName,\n"
//				+ "    users.userposition as title,\n"
//				+ "    jsondata.value ->> 'value' as tazkiraNumber\n"
				+ "FROM campaignformdata\n"
				+ "LEFT JOIN campaignformmeta ON campaignformdata.campaignformmeta_id = campaignformmeta.id\n"
				+ "LEFT JOIN region ON campaignformdata.region_id = region.id\n"
				+ "LEFT JOIN areas ON campaignformdata.area_id = areas.id\n"
				+ "LEFT JOIN district ON campaignformdata.district_id = district.id\n"
				+ "LEFT JOIN community ON campaignformdata.community_id = community.id\n"
				+ "LEFT JOIN users ON campaignformdata.creatinguser_id = users.id\n"
				+ "LEFT JOIN campaigns ON campaignformdata.campaign_id = campaigns.id,\n"
				+ "LATERAL json_array_elements(campaignformdata.formvalues) jsondata(value),\n"
				+ "LATERAL json_array_elements(campaignformmeta.campaignformelements) jsonmeta(value)\n"
				+ "WHERE \n"
				+ "    (jsondata.value ->> 'id') IN ('TazkiraNo') AND\n"
				+ "    (jsondata.value ->> 'id') = (jsonmeta.value ->> 'id') \n"
				+whereclause+" \n"
				+ "    and campaignformdata.id in (\n"
				+ "	    SELECT CAST(unnest(string_to_array(array_to_string(array_agg (id),', '), ',')) AS bigint) AS individual_values\n"
				+ "		FROM flwduplicateerrorreport\n"
				+ "		GROUP BY value\n"
				+ "		HAVING COUNT(*) > 1\n"
				+ "    )"
//				+ " limit "+max+" offset "+first+";";
;
		
	System.out.println("=====seriesDataQuery======== "+joinBuilder);
		
		
//		Query seriesDataQuery = em.createNativeQuery(joinBuilder);
		
		
		return Integer.parseInt(((BigInteger) em.createNativeQuery(joinBuilder).getSingleResult()).toString());

//	return seriesDataQuery.getResultList().size();

	}

	@Override
	public int prepareAllCompletionAnalysis() {
		System.out.println(" =========prepareAllCompletionAnalysis==--------------------");
		final String[] jpqlQueries = {

			//clear the table
			"truncate table completionAnalysisView_a, completionAnalysisView_b, completionAnalysisView_c, completionAnalysisView_d, completionAnalysisView_e;",
			
			//create the four analytics Temporary tables
			"insert into completionAnalysisView_a\n"
			+ "select count(campaignfo0_.formvalues), campaignfo0_.community_id, campaignfo0_.campaign_id\n"
			+ "from campaignFormData campaignfo0_\n"
			+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
			+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id\n"
			+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
			+ "where campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%upervisor%')\n"
			+ "group by campaignfo0_.community_id, campaignfo0_.community_id, campaignfo0_.campaign_id;",
			
			"insert into completionAnalysisView_b\n"
			+ "select count(campaignfo0_.formvalues), campaignfo0_.community_id, campaignfo0_.campaign_id\n"
			+ "from campaignFormData campaignfo0_\n"
			+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
			+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id\n"
			+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
			+ "where campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%Revisit%')\n"
			+ "group by campaignfo0_.community_id, campaignfo0_.community_id, campaignfo0_.campaign_id;",
			
			"insert into completionAnalysisView_c\n"
			+ "select count(campaignfo0_.formvalues), campaignfo0_.community_id, campaignfo0_.campaign_id\n"
			+ "from campaignFormData campaignfo0_\n"
			+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
			+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id\n"
			+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
			+ "where campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%ousehold%')\n"
			+ "group by campaignfo0_.community_id, campaignfo0_.community_id, campaignfo0_.campaign_id;",
			
			"insert into completionAnalysisView_d\n"
			+ "select count(campaignfo0_.formvalues), campaignfo0_.community_id, campaignfo0_.campaign_id\n"
			+ "from campaignFormData campaignfo0_\n"
			+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
			+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id\n"
			+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
			+ "where campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%eam Monitori%')\n"
			+ "group by campaignfo0_.community_id, campaignfo0_.community_id, campaignfo0_.campaign_id;",
			
			//Aggregate the tables 
			"insert into completionAnalysisView_e\n"
			+ "SELECT 	COALESCE(t1.community_id, t3.community_id, t4.community_id, t5.community_id, 0) as community_id,\n"
			+ "		COALESCE(t1.campaign_id, t3.campaign_id, t4.campaign_id, t5.campaign_id) AS campaign_id,\n"
			+ "	  	COALESCE(t1.count, 0) AS supervisor,\n"
			+ "       	COALESCE(t3.count, 0) AS revisit,\n"
			+ "       	COALESCE(t4.count, 0) AS household,\n"
			+ "       	COALESCE(t5.count, 0) AS teammonitori\n"
			+ "FROM completionAnalysisView_a t1\n"
			+ "FULL JOIN completionAnalysisView_b t3 ON t1.campaign_id = t3.campaign_id and t1.community_id = t3.community_id\n"
			+ "FULL JOIN completionAnalysisView_c t4 ON t1.campaign_id = t4.campaign_id and t1.community_id = t4.community_id\n"
			+ "FULL JOIN completionAnalysisView_d t5 ON t1.campaign_id = t5.campaign_id and t1.community_id = t5.community_id\n"
			+ ";"
			
			
		
		};
		
//		EntityTransaction transaction = em.getTransaction();
//		transaction.begin();

		try {
			for (String sqlQuery : jpqlQueries) {
				System.out.println(" =========EXCUTING: " +sqlQuery);
		      int dsc = em.createNativeQuery(sqlQuery).executeUpdate();
		      System.out.println(" =========done: " +dsc);
		    }
		} catch (Exception e) {
		   System.err.println(e.getStackTrace());
		}
		
		
		return getTotalSize();
	}
	
	private int getTotalSize() {
		//get the total size of the analysis
		final String joinBuilder = "select count(*) from completionAnalysisView_e;";
				
		return Integer.parseInt(((BigInteger) em.createNativeQuery(joinBuilder).getSingleResult()).toString());
		
	};

	@Override
	public List<String> getAllActiveUuids() {
		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}
		Date date = new Date(0);

		 List<CampaignFormMeta> allAfter = campaignFormMetaService.getAllAfter(date, userService.getCurrentUser());
			List<CampaignFormMeta> filtered = new ArrayList<>();
			allAfter.removeIf(e -> e.getFormCategory() == null);
			
			for (FormAccess n : userService.getCurrentUser().getFormAccess()) {
				boolean yn = allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filtered.addAll(allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}
			
			List<Long> formMetaIdList = new ArrayList<>();
			for (CampaignFormMeta met : filtered) {
				formMetaIdList.add(met.getId());
			}
			
			List<Long> commIdList = new ArrayList<>();
			if(userService.getCurrentUser().getCommunity().size() > 0) {
				for (Community commm : userService.getCurrentUser().getCommunity()) {
					commIdList.add(commm.getId());
				}
			}
			
			List<Long> distrIdList = new ArrayList<>();
			if(userService.getCurrentUser().getDistricts().size() > 0) {
				for (District distr : userService.getCurrentUser().getDistricts()) {
				
					distrIdList.add(distr.getId());
					}
			}
			
			
			List<CampaignFormData> newlst = campaignFormDataService.getAllActiveAfter(date, formMetaIdList, commIdList, distrIdList);
			List<CampaignFormData> filterednewlst = new ArrayList<>();
			for (CampaignFormMeta met : filtered) {
				
				boolean ynn = newlst.stream().filter(eee -> eee.getCampaignFormMeta().getId() != null).filter(ee -> ee.getCampaignFormMeta().getId().equals(met.getId())).collect(Collectors.toList()).size() > 0;
				
				
				if (ynn) {
					
					filterednewlst.addAll(newlst.stream().filter(eee -> eee.getCampaignFormMeta().getId() != null).filter(ee -> ee.getCampaignFormMeta().getId().equals(met.getId())).collect(Collectors.toList()));
				}
			}
			
			 List<CampaignFormData> filterednewlstLst = new ArrayList<>();
				for (Community commm : userService.getCurrentUser().getCommunity()) {
					////System.out.println(">>>>>>>>>>>>getCommunity>>>>>>>>>"+commm.getId());
					boolean ynn = filterednewlst.stream().filter(comf -> comf.getCommunity().getId().equals(commm.getId())).collect(Collectors.toList()).size() > 0;
						
					if (ynn) {
						filterednewlstLst.addAll(filterednewlst.stream().filter(comf -> comf.getCommunity().getId().equals(commm.getId())).filter(comdf -> comdf.getCampaign().isOpenandclose() == true).collect(Collectors.toList()));
					}
				}
				
				
			
			List<String> lstfinal = new ArrayList<>();
			
			 for(CampaignFormData dtfiltere : filterednewlstLst) {
				 ////System.out.println(">>>>>>>>>>>>>>>>>_____>>>>>>>>>>>>>>>>>>>"+dtfiltere.getUuid());
				 lstfinal.addAll(campaignFormDataService.getAllActiveUuids().stream().filter(eed -> eed.equals(dtfiltere.getUuid())).collect(Collectors.toList()));
				 }
			 
			 

			 
			 ////System.out.println(">>>>>> "+lstfinal.size());

		return lstfinal;
	}

	@Override
	public List<CampaignFormDataDto> getAllActiveAfter(Date date) {
		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}
		
		 List<CampaignFormMeta> allAfter = campaignFormMetaService.getAllAfter(new Date(0), userService.getCurrentUser());
			List<CampaignFormMeta> filtered = new ArrayList<>();
			allAfter.removeIf(e -> e.getFormCategory() == null);
			
			for (FormAccess n : userService.getCurrentUser().getFormAccess()) {
				boolean yn = allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
				if (yn) {
					filtered.addAll(allAfter.stream().filter(e -> !e.getFormCategory().equals(null))
							.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
				}
			}
			
			List<Long> formMetaIdList = new ArrayList<>();
			for (CampaignFormMeta met : filtered) {
				formMetaIdList.add(met.getId());
			}
			
			List<Long> commIdList = new ArrayList<>();
			if(userService.getCurrentUser().getCommunity().size() > 0) {
				for (Community commm : userService.getCurrentUser().getCommunity()) {
					commIdList.add(commm.getId());
				}
			}
			
			List<Long> distrIdList = new ArrayList<>();
			if(userService.getCurrentUser().getDistricts().size() > 0) {
				for (District distr : userService.getCurrentUser().getDistricts()) {
				
					distrIdList.add(distr.getId());
					}
			}
			
			List<CampaignFormData> newDataList = campaignFormDataService.getAllActiveAfter(date, formMetaIdList, commIdList, distrIdList);
			newDataList.removeIf(ee -> ee.getCampaign().isOpenandclose() == false);
			
		return newDataList.stream().map(c -> convertToDto(c))
				.collect(Collectors.toList());
	}

	@Override
	public List<CampaignFormDataDto> getAllActive() {
		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}
		return campaignFormDataService.getAllActive().stream().map(c -> convertToDto(c)).collect(Collectors.toList());
	}
	
	@Override
	public List<CampaignFormDataDto> getAllActiveData(Integer first, Integer max, Boolean includeArchived) {
	
		return campaignFormDataService.getAllActiveData(first, max, includeArchived).stream().map(c -> convertToDtoWithArchive(c)).collect(Collectors.toList());
	}
	
	@Override
	public Integer getAllActiveDataTotalRowCount() {
		StringBuilder selectBuilder = new StringBuilder("SELECT count(id) from campaignformdata where archived is not null");
		BigInteger inte = (BigInteger) em.createNativeQuery(selectBuilder.toString()).getSingleResult();
		return (int) inte.longValue();
		
		}
	
	@Override
	public Integer getAllDataTotalRowCountByArchivedStatus(Boolean includeArchived) {
		
		StringBuilder selectBuilder ;//= new StringBuilder("SELECT count(id) from campaignformdata where archived is not null");
	if (includeArchived != null ) {
		if(includeArchived){
			selectBuilder = new StringBuilder("SELECT count(id) from campaignformdata where archived = true");
		}else {
			selectBuilder = new StringBuilder("SELECT count(id) from campaignformdata where archived = false");
		}
	}else {
		selectBuilder = new StringBuilder("SELECT count(id) from campaignformdata where archived is not null");
	}

		BigInteger inte = (BigInteger) em.createNativeQuery(selectBuilder.toString()).getSingleResult();
		return (int) inte.longValue();
		
		}
	
	@Override
	public List<CampaignFormDataDto> getAllActiveRef() {
		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}
//		return campaignFormDataService.getAllActiveRef().stream().map(c -> convertToDto(c)).collect(Collectors.toList());
		return null;
	}
	
	public List<CampaignDiagramDataDto> getDiagramDataByFieldGroup(CampaignDiagramSeries diagramSeriesTotal, CampaignDiagramSeries diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
	
			List<Area> areas = areaService.getAll();
			areas.forEach(areaItem -> {
				Integer population = populationDataFacadeEjb.getAreaPopulationSelected(areaItem.getUuid(),
						diagramSeriesTotal.getPopulationGroup(), campaignDiagramCriteria);
				if (population == 0) {
					resultData.add(new CampaignDiagramDataDto(areaItem.getName(), 0, areaItem.getUuid(),
							areaItem.getName(), diagramSeries.getFieldId(), diagramSeries.getFormId(), false));
				} else {
					resultData.add(new CampaignDiagramDataDto(areaItem.getName(), population, areaItem.getUuid(),
							areaItem.getName(), diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
				}
			});
		
		
		
		
		System.out.println("getDiagramDataByFieldGroup-------: "+resultData.size());
		
		return resultData;
	}

	public List<CampaignDiagramDataDto> getDiagramDataByAgeGroup(
			CampaignDiagramSeries diagramSeriesTotal, CampaignDiagramSeries diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignJurisdictionLevel grouping = campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy();
		// final String formTyper = campaignDiagramCriteria.getFormType();
		
		

		if (grouping == CampaignJurisdictionLevel.AREA) {
			List<Area> areas = areaService.getAll();
			areas.forEach(areaItem -> {
				Integer population = populationDataFacadeEjb.getAreaPopulationSelected(areaItem.getUuid(),
						diagramSeriesTotal.getPopulationGroup(), campaignDiagramCriteria);
			//	////System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> "+population);
				if (population == 0) {
					resultData.add(new CampaignDiagramDataDto(areaItem.getName(), 0, areaItem.getUuid(),
							areaItem.getName(), diagramSeries.getFieldId(), diagramSeries.getFormId(), false));
				} else {
					resultData.add(new CampaignDiagramDataDto(areaItem.getName(), population, areaItem.getUuid(),
							areaItem.getName(), diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
				}
			});
		} else if (grouping == CampaignJurisdictionLevel.REGION) {
			List<RegionReferenceDto> regions;
			if (area != null)
				regions = regionFacadeEjb.getAllActiveByArea(area.getUuid());
			else
				regions = regionFacadeEjb.getAllActiveAsReference();

			regions.stream().forEach(regionReferenceDto -> {
				PopulationDataCriteria criteria = new PopulationDataCriteria();
				criteria.sexIsNull(true);
				criteria.region(regionReferenceDto);
				criteria.ageGroup(diagramSeriesTotal.getPopulationGroup());
				criteria.setCampaign(campaignDiagramCriteria.getCampaign());
				List<PopulationDataDto> populationDataDto = populationDataFacadeEjb.getPopulationDataSelected(criteria);
				Integer populationSum = 0;
				if (!populationDataDto.isEmpty()) {
					populationSum = populationDataDto.stream().mapToInt(e -> e.getPopulation()).sum();
					resultData.add(new CampaignDiagramDataDto(regionReferenceDto.getCaption(), populationSum,
							regionReferenceDto.getUuid(), regionReferenceDto.getCaption(), diagramSeries.getFieldId(),
							diagramSeries.getFormId(), true));
				} else {
					resultData.add(new CampaignDiagramDataDto(regionReferenceDto.getCaption(), 0,
							regionReferenceDto.getUuid(), regionReferenceDto.getCaption(), diagramSeries.getFieldId(),
							diagramSeries.getFormId(), false));
				}
			});
//			}
		} else if (grouping == CampaignJurisdictionLevel.DISTRICT || Objects.isNull(district)) {

			List<DistrictReferenceDto> districts;
			if (region != null) {
				districts = districtFacadeEjb.getAllActiveByRegion(region.getUuid());
			} else if (area != null) {
				districts = districtFacadeEjb.getAllActiveByArea(area.getUuid());
			} else {
				districts = districtFacadeEjb.getAllActiveAsReference();
			}

			districts.stream().forEach(districtReferenceDto -> {
				PopulationDataCriteria criteria = new PopulationDataCriteria();
				criteria.sexIsNull(true);
				criteria.district(districtReferenceDto);
				criteria.region(region);
				criteria.ageGroup(diagramSeriesTotal.getPopulationGroup());
				criteria.setCampaign(campaignDiagramCriteria.getCampaign());
				List<PopulationDataDto> populationDataDtoList = populationDataFacadeEjb.getPopulationDataSelected(criteria);
				Integer populationSum = 0;
				if (!populationDataDtoList.isEmpty()) {
					populationSum = populationDataDtoList.stream().mapToInt(e -> e.getPopulation()).sum();
					resultData.add(new CampaignDiagramDataDto(districtReferenceDto.getCaption(), populationSum,
							districtReferenceDto.getUuid(), districtReferenceDto.getCaption(),
							diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
				} else {
					resultData.add(new CampaignDiagramDataDto(districtReferenceDto.getCaption(), populationSum,
							districtReferenceDto.getUuid(), districtReferenceDto.getCaption(),
							diagramSeries.getFieldId(), diagramSeries.getFormId(), false));
				}
			});
//			}
		} else if (district != null) {
			resultData.add(new CampaignDiagramDataDto(district.getCaption(), 0, district.getUuid(),
					district.getCaption(), diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
		}
		
		System.out.println("getDiagramDataByAgeGroup-------: "+resultData.size());
		return resultData;
	}

	public List<CampaignDiagramDataDto> getDiagramDataByAgeGroupCard(
			CampaignDiagramSeries diagramSeriesTotal, CampaignDiagramSeries diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
	//	//System.out.println(campaignDiagramCriteria.getArea() + " dddddddddddddddddddddd getDiagramDataByAgeGroupCard "+campaignDiagramCriteria.getCampaign());
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignJurisdictionLevel grouping = campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy();
		System.out.println("getDiagramDataByAgeGroupCard: "+campaignDiagramCriteria.getArea() + " : "+campaignDiagramCriteria.getRegion()+" : "+campaignDiagramCriteria.getDistrict());
		if (area != null && region == null && district == null) {
			List<Area> areas = new ArrayList<>();
			areas.add(areaService.getByUuid(area.getUuid()));
			areas.forEach(areaItem -> {
			
				Integer	population = populationDataFacadeEjb.getAreaPopulationByUuidSelect(areaItem.getUuid(),
						diagramSeriesTotal.getPopulationGroup(), campaignDiagramCriteria);
				if (population == 0) {
					resultData.add(new CampaignDiagramDataDto(areaItem.getName(), 0, areaItem.getUuid(),
							areaItem.getName(), diagramSeries.getFieldId(), diagramSeries.getFormId(), false));
				} else {
					resultData.add(new CampaignDiagramDataDto(areaItem.getName(), population, areaItem.getUuid(),
							areaItem.getName(), diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
				}
				return;
			});
			
			//System.out.println(resultData.size());
		}else if (grouping == CampaignJurisdictionLevel.AREA && area == null) {
			populationx = 0;
			populationx = populationDataFacadeEjb.getAreaPopulationParentSelect("notneeded",
						diagramSeriesTotal.getPopulationGroup(), campaignDiagramCriteria);
				
		System.out.println(diagramSeriesTotal.getPopulationGroup()+">>>>>>>>>>>>>>>YEAH - population = "+populationx);
				
				if (populationx == 0) {
					resultData.add(new CampaignDiagramDataDto("notneeded", 0, "notneeded",
							"notneeded", diagramSeries.getFieldId(), diagramSeries.getFormId(), false));
				} else {
					resultData.add(new CampaignDiagramDataDto("notneeded", populationx, "notneeded",
							"notneeded", diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
				}
		
			
			//System.out.println(resultData.size());
		} else if (region != null && district == null) {
			List<RegionReferenceDto> regions = new ArrayList<>();
			regions.add(region);

			regions.stream().forEach(regionReferenceDto -> {
				PopulationDataCriteria criteria = new PopulationDataCriteria();
				criteria.sexIsNull(true);
				criteria.region(regionReferenceDto);
				criteria.ageGroup(diagramSeriesTotal.getPopulationGroup());
				criteria.setCampaign(campaignDiagramCriteria.getCampaign());
				List<PopulationDataDto> populationDataDto = populationDataFacadeEjb.getPopulationDataSelected(criteria);
				Integer populationSum = 0;
				if (!populationDataDto.isEmpty()) {
					populationSum = populationDataDto.stream().mapToInt(e -> e.getPopulation()).sum();
					resultData.add(new CampaignDiagramDataDto(regionReferenceDto.getCaption(), populationSum,
							regionReferenceDto.getUuid(), regionReferenceDto.getCaption(), diagramSeries.getFieldId(),
							diagramSeries.getFormId(), true));
				} else {
					resultData.add(new CampaignDiagramDataDto(regionReferenceDto.getCaption(), 0,
							regionReferenceDto.getUuid(), regionReferenceDto.getCaption(), diagramSeries.getFieldId(),
							diagramSeries.getFormId(), false));
				}
			});
		}
		else if (district != null) {

			List<DistrictReferenceDto> districts = new ArrayList<>();
			districts.add(district);

			districts.stream().forEach(districtReferenceDto -> {
				PopulationDataCriteria criteria = new PopulationDataCriteria();
				criteria.sexIsNull(true);
				criteria.district(districtReferenceDto);
				criteria.region(region);
				criteria.ageGroup(diagramSeriesTotal.getPopulationGroup());
				criteria.setCampaign(campaignDiagramCriteria.getCampaign());
				List<PopulationDataDto> populationDataDtoList = populationDataFacadeEjb.getPopulationDataSelected(criteria);
				Integer populationSum = 0;
				if (!populationDataDtoList.isEmpty()) {
					populationSum = populationDataDtoList.stream().mapToInt(e -> e.getPopulation()).sum();
					resultData.add(new CampaignDiagramDataDto(districtReferenceDto.getCaption(), populationSum,
							districtReferenceDto.getUuid(), districtReferenceDto.getCaption(),
							diagramSeries.getFieldId(), diagramSeries.getFormId(), true));
				} else {
					resultData.add(new CampaignDiagramDataDto(districtReferenceDto.getCaption(), populationSum,
							districtReferenceDto.getUuid(), districtReferenceDto.getCaption(),
							diagramSeries.getFieldId(), diagramSeries.getFormId(), false));
				}
			});
			}
		
		System.out.println("getDiagramDataByAgeGroupCard-------: "+resultData.size());
		return resultData;
	}

	private Integer popTotal(Integer flexNumber) {
		return popAddiontions = popAddiontions + flexNumber;
	}
	

	@Override
	public List<CampaignDiagramDataDto> getDiagramDataCardFlow(List<CampaignDiagramSeries> diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignReferenceDto campaign = campaignDiagramCriteria.getCampaign();

		for (CampaignDiagramSeries series : diagramSeries) {
			//@formatter:off

				 final String areaFilter = area != null ? " AND " + Area.TABLE_NAME + "_" + Area.UUID + " = :areaUuid" : "";
				final String regionFilter = region != null ? " AND " + CampaignFormData.REGION + "_" + Region.UUID + " = :regionUuid" : "";
				final String districtFilter = district != null ? " AND " + CampaignFormData.DISTRICT + "_" + District.UUID + " = :districtUuid" : "";
				final String campaignFilter = campaign != null ? " AND " + Campaign.TABLE_NAME + "_" + Campaign.UUID + " = :campaignUuid" : "";
				//@formatter:on

			// SELECT
			StringBuilder selectBuilder = new StringBuilder("SELECT formUuid, formId");

			if (series.getFieldId() != null) {
				selectBuilder.append(",  fieldId, fieldCaption, ");

				if (series.getAverageDefault() != null && "true".equals(series.getAverageDefault())) {
					selectBuilder.append(" avg(sumValue) ");
				} else {
					selectBuilder.append("sum(sumValue) ");
				}
			} else {
				selectBuilder.append(", null, null, 0");
			}

			final String jurisdictionGrouping;

			// WHERE
			StringBuilder whereBuilder = new StringBuilder(" WHERE ").append(CampaignFormMeta.FORM_ID)
					.append(" = :campaignFormMetaId");

			String cds = "";

			if (series.getFieldId() != null) {
				whereBuilder.append(" AND fieldId = :campaignFormDataId");
			}

			whereBuilder.append(areaFilter).append(regionFilter).append(districtFilter).append(campaignFilter);// .append(campaignPhaseFilter);

			// GROUP BY
			StringBuilder groupByBuilder = new StringBuilder(" GROUP BY formUuid, formId");

			if (series.getFieldId() != null) {
				groupByBuilder.append(", fieldId, fieldCaption");
			}

			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
//				jurisdictionGrouping = ", " + Region.TABLE_NAME + "." + Region.UUID + ", " + Region.TABLE_NAME + "."
//						+ Region.NAME;

				jurisdictionGrouping = ", " + Area.TABLE_NAME + "_" + Area.UUID + ", " + Area.TABLE_NAME + "_"
						+ Area.NAME;
				break;
			case DISTRICT:
//				jurisdictionGrouping = ", " + District.TABLE_NAME + "." + District.UUID + ", " + District.TABLE_NAME
//						+ "." + District.NAME;

				jurisdictionGrouping = ", " + Region.TABLE_NAME + "_" + Region.UUID + ", " + Region.TABLE_NAME + "_"
						+ Region.NAME;
				break;
			case COMMUNITY:
//				jurisdictionGrouping = ", " + Community.TABLE_NAME + "." + Community.UUID + ", " + Community.TABLE_NAME
//						+ "." + Community.NAME;

				jurisdictionGrouping = ", " + District.TABLE_NAME + "_" + District.UUID + ", " + District.TABLE_NAME
						+ "_" + District.NAME;
				break;
			case AREA:
			default:
				jurisdictionGrouping = ", " + Area.TABLE_NAME + "_" + Area.UUID + ", " + "area";
			}

			// System.out.println("getDiagramDataCard = " +selectBuilder.toString() + " FROM
			// camapaigndata_main " + whereBuilder + groupByBuilder);

			//@formatter:off
			Query seriesDataQuery = em.createNativeQuery(
					selectBuilder.toString() + " FROM camapaigndata_main "  + whereBuilder + groupByBuilder);
			//@formatter:on

			// String gatherd = "campaignFormMetaId: " + series.getFormId() +" | ";
			seriesDataQuery.setParameter("campaignFormMetaId", series.getFormId());
			if (area != null) {
				// gatherd = gatherd +"areaUuid: "+ area.getUuid() +" | ";
				seriesDataQuery.setParameter("areaUuid", area.getUuid());
			}
			if (region != null) {
				// gatherd = gatherd +"regionUuid: "+ region.getUuid() +" | ";
				seriesDataQuery.setParameter("regionUuid", region.getUuid());
			}
			if (district != null) {
				// gatherd = gatherd +"districtUuid: "+ district.getUuid() +" | ";

				seriesDataQuery.setParameter("districtUuid", district.getUuid());
			}
			if (campaign != null) {
				// gatherd = gatherd +"campaignUuid: "+ campaign.getUuid() +" | ";

				seriesDataQuery.setParameter("campaignUuid", campaign.getUuid());
			}

			if (series.getFieldId() != null) {
				// gatherd = gatherd +"campaignFormDataId: "+ series.getFieldId() +" | ";

				seriesDataQuery.setParameter("campaignFormDataId", series.getFieldId());
			}

			// System.out.println("getDiagramDataCardGathere: "+gatherd);

			@SuppressWarnings("unchecked")
			List<Object[]> resultList = seriesDataQuery.getResultList();

			resultData.addAll(resultList.stream()
					.map((result) -> new CampaignDiagramDataDto((String) result[0], (String) result[1],
							(String) result[2], (String) result[3], result[4].toString(), "", // (String) result[5],
							// (String) result[6],
							"",
							// (String) result[7],
							series.getStack()))
					.collect(Collectors.toList()));
		}
		//// System.out.println("resultData - "+
		//// resultData.toString());//SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<CampaignDiagramDataDto> getDiagramDataFlow(List<CampaignDiagramSeries> diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignReferenceDto campaign = campaignDiagramCriteria.getCampaign();

		for (CampaignDiagramSeries series : diagramSeries) {
			//@formatter:off

				final String areaFilter = area != null ? " AND " + Area.TABLE_NAME + "_" + Area.UUID + " = :areaUuid" : "";
				final String regionFilter = region != null ? " AND " + CampaignFormData.REGION + "_" + Region.UUID + " = :regionUuid" : "";
				final String districtFilter = district != null ? " AND " + CampaignFormData.DISTRICT + "_" + District.UUID + " = :districtUuid" : "";
				final String campaignFilter = campaign != null ? " AND " + Campaign.TABLE_NAME + "_" + Campaign.UUID + " = :campaignUuid" : "";
				//@formatter:on

			// SELECT
			StringBuilder selectBuilder = new StringBuilder("SELECT  formUuid, formId");

			if (series.getFieldId() != null) {

				selectBuilder.append(", fieldId, fieldCaption,");

				// TODO must redo for
//				if(series.getReferenceValue() != null) {
//					//////System.out.println("+==+ "+series.getReferenceValue());
//					selectBuilder.append("sum (CASE WHEN ").append("((jsonData->>'")
//					.append(CampaignFormDataEntry.VALUE)
//					.append("') = '").append(series.getReferenceValue())
//					.append("') THEN 1 ELSE 0 END)"); 
//				} else {

				if (series.getAverageDefault() != null && "true".equals(series.getAverageDefault())) {
					selectBuilder.append(" avg(sumValue) ");
				} else {
					selectBuilder.append(" sum(sumValue) ");
				}

			} else {
				selectBuilder.append(", null, null, 0,");
			}

			final String jurisdictionGrouping;
			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
				appendInfrastructureSelectionFlow(selectBuilder, Region.TABLE_NAME, Region.NAME);
				break;
			case DISTRICT:
			case COMMUNITY:
				appendInfrastructureSelectionFlow(selectBuilder, District.TABLE_NAME, District.NAME);
				break;
//			case COMMUNITY:
//				appendInfrastructureSelectionFlow(selectBuilder, Community.TABLE_NAME, Community.NAME);
//				break;
			case AREA:
			default:
				selectBuilder.append(", " + Area.TABLE_NAME + "_" + Area.UUID + ", " + "area");
			}

			if (series.getFieldId() != null) {
				// TODO check why this is needed : series.getFieldId()
//				joinBuilder.append(", json_array_elements(").append(CampaignFormData.FORM_VALUES)
//						.append(") as jsonData, json_array_elements(").append(CampaignFormMeta.CAMPAIGN_FORM_ELEMENTS)
//						.append(") as jsonMeta");
			}

			// WHERE
			StringBuilder whereBuilder = new StringBuilder(" WHERE ").append(CampaignFormMeta.FORM_ID)
					.append(" = :campaignFormMetaId");

			String cds = "";

			if (series.getFieldId() != null) {
				whereBuilder.append(" AND fieldId = :campaignFormDataId");
			}

			whereBuilder.append(areaFilter).append(regionFilter).append(districtFilter).append(campaignFilter);// .append(campaignPhaseFilter);

			// GROUP BY
			StringBuilder groupByBuilder = new StringBuilder(" GROUP BY formUuid, formId ");

			if (series.getFieldId() != null) {
				groupByBuilder.append(", fieldId, fieldCaption");
			}

			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
				jurisdictionGrouping = ", " + Region.TABLE_NAME + "_" + Region.UUID + ", " + Region.TABLE_NAME;
				break;
			case DISTRICT:
			case COMMUNITY:
				jurisdictionGrouping = ", " + District.TABLE_NAME + "_" + District.UUID + ", " + District.TABLE_NAME;
				break;
			// case COMMUNITY:
//				jurisdictionGrouping = ", " + Community.TABLE_NAME + "_" + Community.UUID + ", " + Community.TABLE_NAME
//						+ "_" + Community.NAME;
//				break;
			// TODO there is no cluster on the new logic... let see how we can add that

			// break;

			case AREA:
			default:
				jurisdictionGrouping = ", " + Area.TABLE_NAME + "_" + Area.UUID + ", " + "area";
			}

			groupByBuilder.append(jurisdictionGrouping);

			// ////System.out.println("getDataDiagram "+selectBuilder.toString() + " FROM "
			// + CampaignFormData.TABLE_NAME +" "+ joinBuilder +" "+ whereBuilder +" "+
			// groupByBuilder);

			// +selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME +
			// joinBuilder

			//@formatter:off
			Query seriesDataQuery = em.createNativeQuery(
					selectBuilder.toString() + " FROM camapaigndata_main "  + whereBuilder + groupByBuilder);
			//@formatter:on

			seriesDataQuery.setParameter("campaignFormMetaId", series.getFormId());
			if (area != null) {
				seriesDataQuery.setParameter("areaUuid", area.getUuid());
			}
			if (region != null) {
				seriesDataQuery.setParameter("regionUuid", region.getUuid());
			}
			if (district != null) {
				seriesDataQuery.setParameter("districtUuid", district.getUuid());
			}
			if (campaign != null) {
				seriesDataQuery.setParameter("campaignUuid", campaign.getUuid());
			}

			if (series.getFieldId() != null) {
				seriesDataQuery.setParameter("campaignFormDataId", series.getFieldId());
			}

			System.out.println(" getDiagramData seriesDataQuery = " + selectBuilder.toString()
					+ " FROM camapaigndata_main " + whereBuilder + groupByBuilder);

			@SuppressWarnings("unchecked")
			List<Object[]> resultList = seriesDataQuery.getResultList();

			resultData.addAll(resultList.stream()
					.map((result) -> new CampaignDiagramDataDto((String) result[0], (String) result[1],
							(String) result[2], (String) result[3], result[4].toString(), (String) result[5],
							(String) result[6],
							// (String) result[7],
							series.getStack()))
					.collect(Collectors.toList()));
		}
		// System.out.println("resultData - "+
		// resultData.toString());//SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<CampaignAggregateDataDto> getCampaignFormDataAggregatetoCSV(String campaing_uuid) {

		System.err.println("now in db...");
		List<CampaignAggregateDataDto> resultData = new ArrayList<>();
		StringBuilder selectBuilder = new StringBuilder(
				"SELECT campaignformmeta.uuid as formUuid, campaignformmeta.formId as formId, jsonData->>'id' as fieldId, jsonMeta->>'caption' as fieldCaption, CASE WHEN ((jsonMeta ->> 'type') = 'number') OR((jsonMeta ->> 'type') = 'decimal') OR((jsonMeta ->> 'type') = 'range') THEN sum(cast_number(jsonData->>'value', 0)) ELSE sum(CASE WHEN(jsonData->>'value') = '' THEN 0 ELSE 1 END) END as sumValue, areas.\"name\" as Area, region.\"name\" as Region, district.\"name\" as District  \n"
						+ "FROM campaignFormData \n"
						+ "LEFT JOIN campaignformmeta ON campaignFormMeta_id = campaignformmeta.id \n"
						+ "LEFT JOIN region ON region_id =region.id \n"
						+ "LEFT JOIN areas ON campaignFormData.area_id = areas.id \n"
						+ "LEFT JOIN district ON district_id = district.id \n"
						+ "LEFT JOIN community ON community_id = community.id \n"
						+ "LEFT JOIN campaigns ON campaign_id = campaigns.id, \n"
						+ "json_array_elements(formValues) as jsonData, json_array_elements(campaignFormElements) as jsonMeta \n"
						+ "WHERE jsonData->>'value' IS NOT NULL \n" + "AND jsonData->>'id' = jsonMeta->>'id' \n"
						+ "AND campaigns.uuid = '" + campaing_uuid + "' and jsonData->>'value' != 'NaN'\n"
						+ "GROUP BY campaignformmeta.uuid, campaignformmeta.formId, jsonData->>'id', jsonMeta->>'caption', jsonMeta->>'type', areas.\"name\", region.\"name\", district.\"name\" ");

		System.out.println("query used - " + selectBuilder.toString()); // SQLExtractor.from(seriesDataQuery));
		System.err.println("now in db...");
		Query seriesDataQuery = em.createNativeQuery(selectBuilder.toString());
		System.err.println("now in db...");

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		// String formUuid, String formId, String formField, String formCaption, String
		// area,String region, String district, Long sumValue
		System.err.println("convertting to constructor at db");
		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignAggregateDataDto((String) result[0], (String) result[1],
						(String) result[2], (String) result[3], (String) result[5], (String) result[6],
						(String) result[7], ((BigDecimal) result[4]).longValue()))
				.collect(Collectors.toList()));

		// System.out.println("query used - "+ resultData.toString());
		// //SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<CampaignDiagramDataDto> getDiagramDataByGroupsFlow(List<CampaignDiagramSeries> diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignReferenceDto campaign = campaignDiagramCriteria.getCampaign();

		for (CampaignDiagramSeries series : diagramSeries) {
			//@formatter:off

				final String areaFilter = area != null ? " AND " + Area.TABLE_NAME + "_" + Area.UUID + " = :areaUuid" : "";
				final String regionFilter = region != null ? " AND " + CampaignFormData.REGION + "_" + Region.UUID + " = :regionUuid" : "";
				final String districtFilter = district != null ? " AND " + CampaignFormData.DISTRICT + "_" + District.UUID + " = :districtUuid" : "";
				final String campaignFilter = campaign != null ? " AND " + Campaign.TABLE_NAME + "_" + Campaign.UUID + " = :campaignUuid" : "";
				//@formatter:on

			// SELECT
			StringBuilder selectBuilder = new StringBuilder("SELECT  formUuid, formId");

			if (series.getFieldId() != null) {

				selectBuilder.append(", fieldId, fieldCaption,");

				// TODO must redo for
//				if(series.getReferenceValue() != null) {
//					//////System.out.println("+==+ "+series.getReferenceValue());
//					selectBuilder.append("sum (CASE WHEN ").append("((jsonData->>'")
//					.append(CampaignFormDataEntry.VALUE)
//					.append("') = '").append(series.getReferenceValue())
//					.append("') THEN 1 ELSE 0 END)"); 
//				} else {

				if (series.getAverageDefault() != null && "true".equals(series.getAverageDefault())) {
					selectBuilder.append(" avg(sumValue) ");
				} else {
					selectBuilder.append(" sum(sumValue) ");
				}

			} else {
				selectBuilder.append(", null, null, 0,");
			}

			final String jurisdictionGrouping;
			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
				appendInfrastructureSelectionFlow(selectBuilder, Region.TABLE_NAME, Region.NAME);
				break;
			case DISTRICT:
			case COMMUNITY:
				appendInfrastructureSelectionFlow(selectBuilder, District.TABLE_NAME, District.NAME);
				break;
//			case COMMUNITY:
//				appendInfrastructureSelectionFlow(selectBuilder, Community.TABLE_NAME, Community.NAME);
//				break;
			case AREA:
			default:
				selectBuilder.append(", " + Area.TABLE_NAME + "_" + Area.UUID + ", " + "area");
			}

			if (series.getFieldId() != null) {
				// TODO check why this is needed : series.getFieldId()
//				joinBuilder.append(", json_array_elements(").append(CampaignFormData.FORM_VALUES)
//						.append(") as jsonData, json_array_elements(").append(CampaignFormMeta.CAMPAIGN_FORM_ELEMENTS)
//						.append(") as jsonMeta");
			}

			// WHERE
			StringBuilder whereBuilder = new StringBuilder(" WHERE ").append(CampaignFormMeta.FORM_ID)
					.append(" = :campaignFormMetaId");

			String cds = "";

			if (series.getFieldId() != null) {
				whereBuilder.append(" AND fieldId = :campaignFormDataId");
			}

			whereBuilder.append(areaFilter).append(regionFilter).append(districtFilter).append(campaignFilter);// .append(campaignPhaseFilter);

			// GROUP BY
			StringBuilder groupByBuilder = new StringBuilder(" GROUP BY formUuid, formId ");

			if (series.getFieldId() != null) {
				groupByBuilder.append(", fieldId, fieldCaption");
			}

			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
				jurisdictionGrouping = ", " + Region.TABLE_NAME + "_" + Region.UUID + ", " + Region.TABLE_NAME;
				break;
			case DISTRICT:
			case COMMUNITY:
				jurisdictionGrouping = ", " + District.TABLE_NAME + "_" + District.UUID + ", " + District.TABLE_NAME;
				break;
			// case COMMUNITY:
//				jurisdictionGrouping = ", " + Community.TABLE_NAME + "_" + Community.UUID + ", " + Community.TABLE_NAME
//						+ "_" + Community.NAME;
//				break;
			// TODO there is no cluster on the new logic... let see how we can add that

			// break;

			case AREA:
			default:
				jurisdictionGrouping = ", " + Area.TABLE_NAME + "_" + Area.UUID + ", " + "area";
			}

			groupByBuilder.append(jurisdictionGrouping);

			// ////System.out.println("getDataDiagram "+selectBuilder.toString() + " FROM "
			// + CampaignFormData.TABLE_NAME +" "+ joinBuilder +" "+ whereBuilder +" "+
			// groupByBuilder);

			// +selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME +
			// joinBuilder

			//@formatter:off
			Query seriesDataQuery = em.createNativeQuery(
					selectBuilder.toString() + " FROM camapaigndata_main "  + whereBuilder + groupByBuilder);
			//@formatter:on

			seriesDataQuery.setParameter("campaignFormMetaId", series.getFormId());
			if (area != null) {
				seriesDataQuery.setParameter("areaUuid", area.getUuid());
			}
			if (region != null) {
				seriesDataQuery.setParameter("regionUuid", region.getUuid());
			}
			if (district != null) {
				seriesDataQuery.setParameter("districtUuid", district.getUuid());
			}
			if (campaign != null) {
				seriesDataQuery.setParameter("campaignUuid", campaign.getUuid());
			}

			if (series.getFieldId() != null) {
				seriesDataQuery.setParameter("campaignFormDataId", series.getFieldId());
			}

			System.out.println(" getDiagramData seriesDataQuery = " + selectBuilder.toString()
					+ " FROM camapaigndata_main " + whereBuilder + groupByBuilder);

			@SuppressWarnings("unchecked")
			List<Object[]> resultList = seriesDataQuery.getResultList();

			resultData.addAll(resultList.stream()
					.map((result) -> new CampaignDiagramDataDto((String) result[0], (String) result[1],
							(String) result[2], (String) result[3], result[4].toString(), // (String) result[5],
							// (String) result[6],
							// (String) result[7],
							series.getStack()))
					.collect(Collectors.toList()));
		}

		// System.out.println("resultData - "+
		// resultData.toString());//SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	private void appendInfrastructureSelection(StringBuilder sb, String tableNameField, String nameField) {
		sb.append(tableNameField).append(".").append(AbstractDomainObject.UUID).append(", ").append(tableNameField)
				.append(".").append(nameField);
	}

	private void appendInfrastructureSelectionFlow(StringBuilder sb, String tableNameField, String nameField) {
		sb.append(", ").append(tableNameField).append("_").append(AbstractDomainObject.UUID).append(", ")
				.append(tableNameField);
	}

	@Override
	public long count(CampaignFormDataCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<CampaignFormData> root = cq.from(CampaignFormData.class);

		Predicate filter = CriteriaBuilderHelper.and(cb,
				campaignFormDataService.createCriteriaFilter(criteria, cb, root),
				campaignFormDataService.createUserFilter(cb, cq, root));
		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.count(root));

		// resultData.toString());//SQLExtractor.from(seriesDataQuery));
		System.out.println("Count Queryyy " + SQLExtractor.from(em.createQuery(cq)));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public void overwriteCampaignFormData(CampaignFormDataDto existingData, CampaignFormDataDto newData) {
		DtoHelper.copyDtoValues(existingData, newData, true);
		saveCampaignFormData(existingData);
	}

	private CampaignFormDataReferenceDto toReferenceDto(CampaignFormData source) {
		if (source == null) {
			return null;
		}

		return source.toReference();
	}

	@Override
	public List<CampaignDataExtractDto> getCampaignFormDataExtractApi(String campaignformuuids, String formuuids) {
		String queryString = "select c3.campaignyear, c3.\"name\" as campagin, c2.formname, jd.\"key\", jd.value, a.\"name\" as area, r.\"name\" as region, d.\"name\" as district, c.\"name\" as community, c.clusternumber as clusternumber\n"
				+ "from campaignformdata_jsonextract jd\n" + "inner join campaignformdata cd on jd.id = cd.id\n"
				+ "left outer join campaignformmeta c2 on cd.campaignformmeta_id = c2.id\n"
				+ "left outer join campaigns c3 on cd.campaign_id = c3.id\n"
				+ "left outer join region r on cd.region_id = r.id\n" + "left outer join areas a on r.area_id = a.id\n"
				+ "left outer join district d on cd.district_id = d.id\n"
				+ "left outer join community c on cd.community_id = c.id\n"
				+ "where jd.\"value\" is not null limit 10000;";

		Query seriesDataQuery = em.createNativeQuery(queryString);

		List<CampaignDataExtractDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		// Long campaignyear, String campaign, String formname, String key, String
		// value,
		// String area, String region, String district, String cummunity, Long
		// clusternumber

		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignDataExtractDto((String) result[0], (String) result[1], (String) result[2],
						(String) result[3], (String) result[4], (String) result[5], (String) result[6],
						(String) result[7], (String) result[8], ((Integer) result[9]).longValue()))
				.collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public List<CampaignDataExtractDto> getCampaignFormDataPivotExtractApi() {
		String queryString = "select c3.campaignyear, c3.\"name\" as campagin, c2.formname, jd.\"key\" as field, jd.value, a.\"name\" as area, r.\"name\" as region, d.\"name\" as district \n"
//				+ ", c.\"name\" as community, c.clusternumber as clusternumber\n"
				+ "from campaignformdata_jsonextract jd\n" + "inner join campaignformdata cd on jd.id = cd.id\n"
				+ "left outer join campaignformmeta c2 on cd.campaignformmeta_id = c2.id\n"
				+ "left outer join campaigns c3 on cd.campaign_id = c3.id\n"
				+ "left outer join region r on cd.region_id = r.id\n" + "left outer join areas a on r.area_id = a.id\n"
				+ "left outer join district d on cd.district_id = d.id\n"
//				+ "left outer join community c on cd.community_id = c.id\n"
				+ "where jd.\"value\" is not null limit 20000;";

		Query seriesDataQuery = em.createNativeQuery(queryString);

		List<CampaignDataExtractDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		// Long campaignyear, String campaign, String formname, String key, String
		// value,
		// String area, String region, String district, String cummunity, Long
		// clusternumber

		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignDataExtractDto((String) result[0], (String) result[1], (String) result[2],
						(String) result[3], (String) result[4], (String) result[5], (String) result[6],
						(String) result[7]
//								,
//						(String) result[8],
//						((Integer) result[9]).longValue()
				)).collect(Collectors.toList()));

		return resultData;
	}

//	@Override
//	public void checkIfMaterializedViewNeedsUpdate() {
//		
//		String queryString = "select count(jd.id) FROM campaignformdata_jsonextract jd "
//				+ "INNER JOIN campaignformdata cd ON jd.id = cd.id "
//				+ "where cd.uuid = '" + 
//				
//		
//	}

	@Override
	public List<Object[]> getTransposedCampaignFormDataDaywiseData(CampaignFormDataCriteria criteria) {
		String queryString = ""
//				+ "REFRESH MATERIALIZED VIEW campaignformdata_jsonextract; \n"

				+ "WITH day_split AS ( SELECT c3.campaignyear, c3.\"name\" AS campaign, c2.formname, a.\"name\" AS area, r.\"name\" AS region, d.\"name\" AS district, c.\"name\" AS cluster, regexp_matches(jd.\"key\", '_(day[0-9]+)$') AS day_suffix, jd.\"key\" AS field, jd.value \n"
				+ "FROM campaignformdata_jsonextract jd INNER JOIN campaignformdata cd ON jd.id = cd.id LEFT OUTER JOIN campaignformmeta c2 ON cd.campaignformmeta_id = c2.id LEFT OUTER JOIN campaigns c3 ON cd.campaign_id = c3.id LEFT OUTER JOIN region r ON cd.region_id = r.id \n"
				+ "LEFT OUTER JOIN areas a ON r.area_id = a.id  LEFT OUTER JOIN  district d ON cd.district_id = d.id LEFT OUTER JOIN community c  ON cd.community_id = c.id \n"
				+ "WHERE jd.\"value\" IS NOT NULL AND c3.uuid = '" + criteria.getCampaign().getUuid()
				+ "' AND c2.uuid = '" + criteria.getCampaignFormMeta().getUuid() + "') \n"
				+ "SELECT campaignyear, campaign, formname, area, region, district, cluster, day_suffix[1] AS day, field, value \n"
				+ "FROM  day_split \n"
				+ "ORDER BY  campaignyear, campaign, formname, area, region, district, cluster, day, field;";

//		Query seriesDataQuery = em.createNativeQuery(queryString);
//		System.out.println("queryyyyyy used - " + queryString.toString()); // SQLExtractor.from(seriesDataQuery));
		return em.createNativeQuery(queryString).getResultList();
//		List<CampaignDataExtractDto> resultData = new ArrayList<>();
//
//		@SuppressWarnings("unchecked")
//		List<Object[]> resultList = seriesDataQuery.getResultList();
//
//		// Long campaignyear, String campaign, String formname, String key, String
//		// value,
//		// String area, String region, String district, String cummunity, Long
//		// clusternumber
//
//		resultData.addAll(resultList.stream()
//				.map((result) -> new CampaignDataExtractDto((String) result[0], (String) result[1], (String) result[2],
//						(String) result[3], (String) result[4], (String) result[5], (String) result[6],
//						(String) result[7],(String) result[8],(String) result[9]
////								,
////						(String) result[8],
////						((Integer) result[9]).longValue()
//				)).collect(Collectors.toList()));
//		
//		
//
//		return resultData;
	}

	@Override
	public List<CampaignFormDataDto> getCampaignFormData(String campaignformuuid, String formuuid) {
//		// get the campaign
//		Campaign xxxxx = campaignService.getByUuid(campaignformuuid);
//		Long metaid = xxxxx.getId();
//		// get the form
//		CampaignFormMeta yyyyyy = campaignFormMetaService.getByUuid(formuuid);
//		Long formid = yyyyyy.getId();
//
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//		CriteriaQuery<CampaignFormData> cq = cb.createQuery(CampaignFormData.class);
//		Root<CampaignFormData> from = cq.from(CampaignFormData.class);
//
//		cq.where(cb.and(cb.equal(from.get(CampaignFormData.CAMPAIGN_FORM_META), formid),
//				cb.equal(from.get(CampaignFormData.CAMPAIGN), metaid)));
//
//		CriteriaQuery<CampaignFormData> select = ((CriteriaQuery<CampaignFormData>) cq).select(from);
//		TypedQuery<CampaignFormData> tq = em.createQuery(select);
//		List<CampaignFormData> list = tq.getResultList();
//
//		return list.stream().map(c -> convertToDto(c)).collect(Collectors.toList()); // multiselect(
		return null;
	}

	@Override
	public List<MapCampaignDataDto> getCampaignDataforMaps() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<MapCampaignDataDto> cq = cb.createQuery(MapCampaignDataDto.class);
		Root<CampaignFormData> caze = cq.from(CampaignFormData.class);

		List<MapCampaignDataDto> result;

		cq.multiselect(caze.get(CampaignFormData.UUID), caze.get(CampaignFormData.LAT), caze.get(CampaignFormData.LON));

		result = em.createQuery(cq).getResultList();

		return result;
	}

	@Override
	public String getByClusterDropDown(CommunityReferenceDto community, CampaignFormMetaDto campaignForm,
			CampaignDto campaign) {
////System.out.println(community.getUuid());
////System.out.println(campaignForm.getUuid());	
////System.out.println(campaign.getUuid());

		String query = "select cb.uuid from campaignformdata cb left join community cm on cb.community_id = cm.id \r\n"
				+ "left join campaignformmeta ff on cb.campaignformmeta_id = ff.id left join campaigns gn on cb.campaign_id = gn.id\r\n"
				+ "where cm.uuid = '" + community.getUuid() + "' and ff.uuid = '" + campaignForm.getUuid()
				+ "' and gn.uuid = '" + campaign.getUuid() + "' limit 1";
		Query poquery = em.createNativeQuery(query);
		try {
			return (String) poquery.getSingleResult();
		} catch (NoResultException e) {
			return "nul";
		}

	}

	@Override
	public List<CampaignFormDataIndexDto> getByCompletionAnalysisNew(CampaignFormDataCriteria criteria,
			List<SortProperty> sortProperties, FormAccess frms) {
		// System.out.println("Query running puuurrrrr!!!!!!!");
		String joinBuilder = "\n"
				+ "select area3_x.\"name\" as area_, region4_x.\"name\" as region_, district5_x.\"name\" as district_, campaignfo0_x.clusternumber as clusternumber_, campaignfo0_x.externalid as ccode, COALESCE((\n"
				+ "select count(campaignfo0_.formvalues)\n" + "from campaignFormData campaignfo0_\n"
				+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
				+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id\n"
				+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
				+ "where area3_.uuid=area3_x.uuid and campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%Revisit%')\n"
				+ "and campaignfo0_.community_id = campaignfo0_x.id\n" + "group by campaignfo0_.community_id\n"
				+ "), 0) as hh, COALESCE((\n" + "select count(campaignfo0_.formvalues)\n"
				+ "from campaignFormData campaignfo0_\n"
				+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
				+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id \n"
				+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
				+ "where area3_.uuid=area3_x.uuid and campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%Revisit%')\n"
				+ "and campaignfo0_.community_id = campaignfo0_x.id \n" + "group by campaignfo0_.community_id\n"
				+ "), 0) as sup, COALESCE((\n" + "select count(campaignfo0_.formvalues)\n"
				+ "from campaignFormData campaignfo0_\n"
				+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
				+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id \n"
				+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
				+ "where area3_.uuid=area3_x.uuid and campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%Revisit%')\n"
				+ "and campaignfo0_.community_id = campaignfo0_x.id \n" + "group by campaignfo0_.community_id\n"
				+ "), 0) as team, COALESCE((\n" + "select count(campaignfo0_.formvalues)\n"
				+ "from campaignFormData campaignfo0_\n"
				+ "left outer join campaigns campaign1_ on campaignfo0_.campaign_id=campaign1_.id\n"
				+ "left outer join areas area3_ on campaignfo0_.area_id=area3_.id \n"
				+ "left outer join CampaignFormMeta campaignfo2_ on campaignfo0_.campaignFormMeta_id=campaignfo2_.id\n"
				+ "where area3_.uuid=area3_x.uuid and campaignfo2_.formCategory= 'ICM' and (campaignfo2_.formName like '%Revisit%')\n"
				+ "and campaignfo0_.community_id = campaignfo0_x.id\n" + "group by campaignfo0_.community_id\n"
				+ "), 0) as rev from community campaignfo0_x\n"
				+ "left outer join District district5_x on campaignfo0_x.district_id=district5_x.id\n"
				+ "left outer join Region region4_x on district5_x.region_id=region4_x.id\n"
				+ "left outer join areas area3_x on region4_x.area_id=area3_x.id\n"
				+ "where area3_x.uuid='W5R34K-APYPCA-4GZXDO-IVJWKGIM' and campaignfo0_x.archived = false\n"
				+ "limit 30";

		Query seriesDataQuery = em.createNativeQuery(joinBuilder);

		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		// System.out.println("starting....");

		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignFormDataIndexDto((String) result[0].toString(),
						(String) result[1].toString(), (String) result[2].toString(), "", (Integer) result[3],
						((BigInteger) result[4]).longValue(), ((BigInteger) result[5]).longValue(),
						((BigInteger) result[6]).longValue(), ((BigInteger) result[7]).longValue(),
						((BigInteger) result[8]).longValue()))
				.collect(Collectors.toList()));

		// System.out.println("ending...." +resultData.size());

		//// System.out.println("resultData - "+ resultData.toString());
		//// //SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public String getByJsonFormDefinitonToCSVCount() {

		//@formatter:off
		
		String joiner = "";
		
	
		
		
		String joinBuilder = "SELECT COUNT(*) as count_result \r\n"
				+ "FROM (\r\n"
				+ "  SELECT formid, fe->>'caption' as caption, fe->>'id' as id, fe->>'type' as datatype, formtype, modality\r\n"
				+ "  FROM campaignformmeta c , \r\n"
				+ "       json_array_elements(c.campaignformelements) AS fe \r\n"
				+ "  WHERE fe->>'caption' IS NOT NULL \r\n"
				+ "    AND fe->>'caption' NOT LIKE '%<%'\r\n"
				+ ") AS subquery;";
	
	return ((BigInteger) em.createNativeQuery(joinBuilder).getSingleResult()).toString();
	}
	

	@Override
	public void deleteCampaignData(List<String> uuids) {
		
		User user = userService.getCurrentUser();
		if (!userRoleConfigFacade.getEffectiveUserRights(user.getUserRoles().toArray(new UserRole[user.getUserRoles().size()]))
			.contains(UserRight.CAMPAIGN_DELETE)) {
			throw new UnsupportedOperationException(
				I18nProperties.getString(Strings.entityUser) + " " + user.getUuid() + " is not allowed to delete "
					+ I18nProperties.getString(Strings.entityCampaigns).toLowerCase() + ".");
		}
		
		for(String uuidx : uuids) {
				campaignFormDataService.delete(campaignFormDataService.getByUuid(uuidx));
		}
		
	}
	
	@Override
	public void verifyCampaignData(List<String> uuids, boolean isUnVerifyingAction) {
				for(String uuidx : uuids) {
					
					if(isUnVerifyingAction) {
						System.out.println("unverifyyyyyyyyyyyyyyy");
						campaignFormDataService.unVerifyData(uuidx);
					}else {
						System.out.println("verifyyyyyyyyyyyyyyy");

						campaignFormDataService.verify(uuidx);
					}
								
		}
		
	}
	
	@Override
	public void publishCampaignData(List<String> uuids, boolean isUnPublishingAction) {
				for(String uuidx : uuids) {
					
					if(isUnPublishingAction) {
						System.out.println("unverifyyyyyyyyyyyyyyy");
						campaignFormDataService.unPublishData(uuidx);
					}else {
						System.out.println("verifyyyyyyyyyyyyyyy");

						campaignFormDataService.publishData(uuidx);
					}
								
		}
		
	}
	
	@Override
	public  void updateReassignedDistrictData(String uuid) {
		// TODO Auto-generated method stub
		
		campaignFormDataService.updateReassignedDistrictData(uuid);
		
	}
	
	@Override
	public boolean getVerifiedStatus(String uuid) {

				
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
				Root<CampaignFormData> from = cq.from(CampaignFormData.class);

				cq.where(cb.equal(from.get(CampaignFormData.UUID), uuid)
//						cb.equal(from.get(AbstractDomainObject.UUID), campaignFormDataUuid))
						);
				cq.select(from.get(CampaignFormData.ISVERIFIED));
				System.out.println("resultData - "+ SQLExtractor.from(em.createQuery(cq)));
				boolean count = em.createQuery(cq).getSingleResult();
				
				
				
				return count;
		
	}
	
	@Override
	public boolean getPublishedStatus(String uuid) {

				
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
				Root<CampaignFormData> from = cq.from(CampaignFormData.class);

				cq.where(cb.equal(from.get(CampaignFormData.UUID), uuid)
//						cb.equal(from.get(AbstractDomainObject.UUID), campaignFormDataUuid))
						);
				cq.select(from.get(CampaignFormData.ISPUBLISHED));
				System.out.println("resultData - "+ SQLExtractor.from(em.createQuery(cq)));
				boolean count = em.createQuery(cq).getSingleResult();
				
				
				
				return count;
		
	}
	
	

	

	@Override
	public List<JsonDictionaryReportModelDto> getByJsonFormDefinitonToCSV() {
		
		System.err.println("now in dbxc");
		List<JsonDictionaryReportModelDto> resultData = new ArrayList<>();
		StringBuilder selectBuilder = new StringBuilder("SELECT formid, fe->>'caption' as caption, fe->>'id' as id,  fe->>'type' as datatype, formtype, modality\r\n"
				+ "FROM campaignformmeta c , \r\n"
				+ "     json_array_elements(c.campaignformelements) AS fe \r\n"
				+ "WHERE fe->>'caption' IS NOT NULL \r\n"
				+ "  AND fe->>'caption' NOT LIKE '%<%';");
		
		//System.out.println("query used - "+ selectBuilder.toString()); //SQLExtractor.from(seriesDataQuery));
		
		Query seriesDataQuery = em.createNativeQuery(selectBuilder.toString());
				
				
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList(); 
		
		//String formUuid, String formId, String formField, String formCaption, String area,String region, String district, Long sumValue
		System.err.println("convertting to constructor at db");
		resultData.addAll(resultList.stream()
				.map((result) -> new JsonDictionaryReportModelDto(
						(String) result[0],
						(String) result[1],
						(String) result[2], 
						(String) result[3],
						(String) result[4],
						(String) result[5]
						)).collect(Collectors.toList()));

	//System.out.println("query used - "+ resultData.toString()); //SQLExtractor.from(seriesDataQuery));
	return resultData;
	}

	
	

	@Override
	public List<CampaignDiagramDataDto> getDiagramDataCard(List<CampaignDiagramSeries> diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignReferenceDto campaign = campaignDiagramCriteria.getCampaign();

		for (CampaignDiagramSeries series : diagramSeries) {
			//@formatter:off

				 final String areaFilter = area != null ? " AND " + Area.TABLE_NAME + "." + Area.UUID + " = :areaUuid" : "";
				final String regionFilter = region != null ? " AND " + CampaignFormData.REGION + "." + Region.UUID + " = :regionUuid" : "";
				final String districtFilter = district != null ? " AND " + CampaignFormData.DISTRICT + "." + District.UUID + " = :districtUuid" : "";
				final String campaignFilter = campaign != null ? " AND " + Campaign.TABLE_NAME + "." + Campaign.UUID + " = :campaignUuid" : "";
				//@formatter:on

			// SELECT
			StringBuilder selectBuilder = new StringBuilder("SELECT ").append(CampaignFormMeta.TABLE_NAME).append(".")
					.append(CampaignFormMeta.UUID).append(" as formUuid,").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.FORM_ID).append(" as formId");

			if (series.getFieldId() != null) {

				selectBuilder.append(", jsonData->>'").append(CampaignFormDataEntry.ID)
						.append("' as fieldId, jsonMeta->>'").append(CampaignFormElement.CAPTION)
						.append("' as fieldCaption,");

				if (series.getReferenceValue() != null) {
					////// System.out.println("+==+ "+series.getReferenceValue());
					selectBuilder.append("sum (CASE WHEN ").append("((jsonData->>'").append(CampaignFormDataEntry.VALUE)
							.append("') = '").append(series.getReferenceValue()).append("') THEN 1 ELSE 0 END)");
				} else {

					////// System.out.println("+==+ "+series.getReferenceValue());
					selectBuilder.append(" CASE WHEN ((jsonMeta ->> '").append(CampaignFormElement.TYPE)
							.append("') = '").append(CampaignFormElementType.NUMBER.toString() + "') OR")

							.append("((jsonMeta ->> '").append(CampaignFormElement.TYPE).append("') = '")
							.append(CampaignFormElementType.DECIMAL.toString() + "') OR")

							.append("((jsonMeta ->> '").append(CampaignFormElement.TYPE).append("') = '")
							.append(CampaignFormElementType.RANGE.toString() + "')");

					if (series.getAverageDefault() != null && "true".equals(series.getAverageDefault())) {
						selectBuilder.append(" THEN avg(cast_number(jsonData->>'");
					} else {
						selectBuilder.append(" THEN sum(cast_number(jsonData->>'");
					}

					selectBuilder.append(CampaignFormDataEntry.VALUE).append("', 0)) ELSE sum(CASE WHEN(jsonData->>'")
							.append(CampaignFormDataEntry.VALUE).append("') = '")// .append(series.getReferenceValue())
							.append("' THEN 0 ELSE 1 END) END");

				}
				selectBuilder.append(" as sumValue");

				// .append("THEN round(SUM (CAST
				// (jsonData->>'").append(CampaignFormDataEntry.VALUE)
				// .append("' AS NUMERIC)), 2) ELSE sum(CASE
				// WHEN(jsonData->>'").append(CampaignFormDataEntry.VALUE)
				// .append("') = '").append(series.getReferenceValue())
				// .append("' THEN 1 ELSE 0 END) END as sumValue,");
			} else {
				selectBuilder.append(", null as fieldId, null as fieldCaption, count(formId) as sumValue,");
			}

			final String jurisdictionGrouping;
			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
//			case REGION:
//				appendInfrastructureSelection(selectBuilder, Region.TABLE_NAME, Region.NAME);
//				break;
			case DISTRICT:
//				appendInfrastructureSelection(selectBuilder, District.TABLE_NAME, District.NAME);
				// appendInfrastructureSelection(selectBuilder, Region.TABLE_NAME, Region.NAME);
				break;
			case COMMUNITY:
//				appendInfrastructureSelection(selectBuilder, Community.TABLE_NAME, Community.NAME);
				// appendInfrastructureSelection(selectBuilder, District.TABLE_NAME,
				// District.NAME);
				break;
			case AREA:
			case REGION:
			default:
				// appendInfrastructureSelection(selectBuilder, Area.TABLE_NAME, Area.NAME);
			}

			// JOINS
			StringBuilder joinBuilder = new StringBuilder(" LEFT JOIN ").append(CampaignFormMeta.TABLE_NAME)
					.append(" ON ").append(CampaignFormData.CAMPAIGN_FORM_META).append("_id = ")
					.append(CampaignFormMeta.TABLE_NAME).append(".").append(CampaignFormMeta.ID).append(" LEFT JOIN ")
					.append(Region.TABLE_NAME).append(" ON ").append(CampaignFormData.REGION).append("_id =")
					.append(Region.TABLE_NAME).append(".").append(Region.ID).append(" LEFT JOIN ")
					.append(Area.TABLE_NAME).append(" ON ").append(CampaignFormData.TABLE_NAME).append(".")
					.append(Region.AREA).append("_id = ").append(Area.TABLE_NAME).append(".").append(Area.ID)
					.append(" LEFT JOIN ").append(District.TABLE_NAME).append(" ON ").append(CampaignFormData.DISTRICT)
					.append("_id = ").append(District.TABLE_NAME).append(".").append(District.ID).append(" LEFT JOIN ")
					.append(Community.TABLE_NAME).append(" ON ").append(CampaignFormData.COMMUNITY).append("_id = ")
					.append(Community.TABLE_NAME).append(".").append(Community.ID).append(" LEFT JOIN ")
					.append(Campaign.TABLE_NAME).append(" ON ").append(CampaignFormData.CAMPAIGN).append("_id = ")
					.append(Campaign.TABLE_NAME).append(".").append(Campaign.ID);

			if (series.getFieldId() != null) {
				joinBuilder.append(", json_array_elements(").append(CampaignFormData.FORM_VALUES)
						.append(") as jsonData, json_array_elements(").append(CampaignFormMeta.CAMPAIGN_FORM_ELEMENTS)
						.append(") as jsonMeta");
			}

			// WHERE
			StringBuilder whereBuilder = new StringBuilder(" WHERE ").append(CampaignFormMeta.TABLE_NAME).append(".")
					.append(CampaignFormMeta.FORM_ID).append(" = :campaignFormMetaId");

			String cds = "";

			if (series.getFieldId() != null) {
				whereBuilder.append(" AND jsonData->>'").append(CampaignFormDataEntry.ID)
						.append("' = :campaignFormDataId").append(" AND jsonData->>'")
						.append(CampaignFormDataEntry.VALUE).append("' IS NOT NULL AND jsonData->>'")
						.append(CampaignFormDataEntry.ID).append("' = jsonMeta->>'").append(CampaignFormElement.ID)
						.append("'");
			}

			whereBuilder.append(areaFilter).append(regionFilter).append(districtFilter).append(campaignFilter);// .append(campaignPhaseFilter);

			// GROUP BY
			StringBuilder groupByBuilder = new StringBuilder(" GROUP BY ").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.UUID).append(",").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.FORM_ID);

			if (series.getFieldId() != null) {
				groupByBuilder.append(", jsonData->>'").append(CampaignFormDataEntry.ID).append("', jsonMeta->>'")
						.append(CampaignFormElement.CAPTION).append("', jsonMeta->>'").append(CampaignFormElement.TYPE)
						.append("'");
			}

			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
//				jurisdictionGrouping = ", " + Region.TABLE_NAME + "." + Region.UUID + ", " + Region.TABLE_NAME + "."
//						+ Region.NAME;

				jurisdictionGrouping = ", " + Area.TABLE_NAME + "." + Area.UUID + ", " + Area.TABLE_NAME + "."
						+ Area.NAME;
				break;
			case DISTRICT:
//				jurisdictionGrouping = ", " + District.TABLE_NAME + "." + District.UUID + ", " + District.TABLE_NAME
//						+ "." + District.NAME;

				jurisdictionGrouping = ", " + Region.TABLE_NAME + "." + Region.UUID + ", " + Region.TABLE_NAME + "."
						+ Region.NAME;
				break;
			case COMMUNITY:
//				jurisdictionGrouping = ", " + Community.TABLE_NAME + "." + Community.UUID + ", " + Community.TABLE_NAME
//						+ "." + Community.NAME;

				jurisdictionGrouping = ", " + District.TABLE_NAME + "." + District.UUID + ", " + District.TABLE_NAME
						+ "." + District.NAME;
				break;
			case AREA:
			default:
				jurisdictionGrouping = ", " + Area.TABLE_NAME + "." + Area.UUID + ", " + Area.TABLE_NAME + "."
						+ Area.NAME;
			}

			// groupByBuilder.append(jurisdictionGrouping);

			// System.out.println("getDataDiagram----- "+selectBuilder.toString() + " FROM "
			// + CampaignFormData.TABLE_NAME +" "+ joinBuilder +" "+ whereBuilder +" "+
			// groupByBuilder);

			// +selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME +
			// joinBuilder

			//@formatter:off
			Query seriesDataQuery = em.createNativeQuery(
					selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME + joinBuilder + whereBuilder + groupByBuilder);
			//@formatter:on

			seriesDataQuery.setParameter("campaignFormMetaId", series.getFormId());
			if (area != null) {
				seriesDataQuery.setParameter("areaUuid", area.getUuid());
			}
			if (region != null) {
				seriesDataQuery.setParameter("regionUuid", region.getUuid());
			}
			if (district != null) {
				seriesDataQuery.setParameter("districtUuid", district.getUuid());
			}
			if (campaign != null) {
				seriesDataQuery.setParameter("campaignUuid", campaign.getUuid());
			}

			if (series.getFieldId() != null) {
				seriesDataQuery.setParameter("campaignFormDataId", series.getFieldId());
			}

			System.out.println("getDiagramDataCard seriesDataQuery = " + selectBuilder.toString() + " FROM "
					+ CampaignFormData.TABLE_NAME + joinBuilder + whereBuilder + groupByBuilder);

			@SuppressWarnings("unchecked")
			List<Object[]> resultList = seriesDataQuery.getResultList();

			resultData.addAll(resultList.stream()
					.map((result) -> new CampaignDiagramDataDto((String) result[0], (String) result[1],
							(String) result[2], (String) result[3], result[4].toString(), "", // (String) result[5],
							// (String) result[6],
							"",
							// (String) result[7],
							series.getStack()))
					.collect(Collectors.toList()));
		}
		//// System.out.println("resultData - "+
		//// resultData.toString());//SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<CampaignDiagramDataDto> getDiagramData(List<CampaignDiagramSeries> diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignReferenceDto campaign = campaignDiagramCriteria.getCampaign();

		for (CampaignDiagramSeries series : diagramSeries) {
			//@formatter:off

				final String areaFilter = area != null ? " AND " + Area.TABLE_NAME + "." + Area.UUID + " = :areaUuid" : "";
				final String regionFilter = region != null ? " AND " + CampaignFormData.REGION + "." + Region.UUID + " = :regionUuid" : "";
				final String districtFilter = district != null ? " AND " + CampaignFormData.DISTRICT + "." + District.UUID + " = :districtUuid" : "";
				final String campaignFilter = campaign != null ? " AND " + Campaign.TABLE_NAME + "." + Campaign.UUID + " = :campaignUuid" : "";
				//@formatter:on

			// SELECT
			StringBuilder selectBuilder = new StringBuilder("SELECT ").append(CampaignFormMeta.TABLE_NAME).append(".")
					.append(CampaignFormMeta.UUID).append(" as formUuid,").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.FORM_ID).append(" as formId");

			if (series.getFieldId() != null) {

				selectBuilder.append(", jsonData->>'").append(CampaignFormDataEntry.ID)
						.append("' as fieldId, jsonMeta->>'").append(CampaignFormElement.CAPTION)
						.append("' as fieldCaption,");

				if (series.getReferenceValue() != null) {
					////// System.out.println("+==+ "+series.getReferenceValue());
					selectBuilder.append("sum (CASE WHEN ").append("((jsonData->>'").append(CampaignFormDataEntry.VALUE)
							.append("') = '").append(series.getReferenceValue()).append("') THEN 1 ELSE 0 END)");
				} else {

					////// System.out.println("+==+ "+series.getReferenceValue());
					selectBuilder.append(" CASE WHEN ((jsonMeta ->> '").append(CampaignFormElement.TYPE)
							.append("') = '").append(CampaignFormElementType.NUMBER.toString() + "') OR")

							.append("((jsonMeta ->> '").append(CampaignFormElement.TYPE).append("') = '")
							.append(CampaignFormElementType.DECIMAL.toString() + "') OR")

							.append("((jsonMeta ->> '").append(CampaignFormElement.TYPE).append("') = '")
							.append(CampaignFormElementType.RANGE.toString() + "')");

					if (series.getAverageDefault() != null && "true".equals(series.getAverageDefault())) {
						selectBuilder.append(" THEN avg(cast_number(jsonData->>'");
					} else {
						selectBuilder.append(" THEN sum(cast_number(jsonData->>'");
					}

					selectBuilder.append(CampaignFormDataEntry.VALUE).append("', 0)) ELSE sum(CASE WHEN(jsonData->>'")
							.append(CampaignFormDataEntry.VALUE).append("') = '")// .append(series.getReferenceValue())
							.append("' THEN 0 ELSE 1 END) END");

				}
				selectBuilder.append(" as sumValue,");

				// .append("THEN round(SUM (CAST
				// (jsonData->>'").append(CampaignFormDataEntry.VALUE)
				// .append("' AS NUMERIC)), 2) ELSE sum(CASE
				// WHEN(jsonData->>'").append(CampaignFormDataEntry.VALUE)
				// .append("') = '").append(series.getReferenceValue())
				// .append("' THEN 1 ELSE 0 END) END as sumValue,");
			} else {
				selectBuilder.append(", null as fieldId, null as fieldCaption, count(formId) as sumValue,");
			}

			final String jurisdictionGrouping;
			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
				appendInfrastructureSelection(selectBuilder, Region.TABLE_NAME, Region.NAME);
				break;
			case DISTRICT:
				appendInfrastructureSelection(selectBuilder, District.TABLE_NAME, District.NAME);
				break;
			case COMMUNITY:
				appendInfrastructureSelection(selectBuilder, Community.TABLE_NAME, Community.NAME);
				break;
			case AREA:
			default:
				appendInfrastructureSelection(selectBuilder, Area.TABLE_NAME, Area.NAME);
			}

			// JOINS
			StringBuilder joinBuilder = new StringBuilder(" LEFT JOIN ").append(CampaignFormMeta.TABLE_NAME)
					.append(" ON ").append(CampaignFormData.CAMPAIGN_FORM_META).append("_id = ")
					.append(CampaignFormMeta.TABLE_NAME).append(".").append(CampaignFormMeta.ID).append(" LEFT JOIN ")
					.append(Region.TABLE_NAME).append(" ON ").append(CampaignFormData.REGION).append("_id =")
					.append(Region.TABLE_NAME).append(".").append(Region.ID).append(" LEFT JOIN ")
					.append(Area.TABLE_NAME).append(" ON ").append(CampaignFormData.TABLE_NAME).append(".")
					.append(Region.AREA).append("_id = ").append(Area.TABLE_NAME).append(".").append(Area.ID)
					.append(" LEFT JOIN ").append(District.TABLE_NAME).append(" ON ").append(CampaignFormData.DISTRICT)
					.append("_id = ").append(District.TABLE_NAME).append(".").append(District.ID).append(" LEFT JOIN ")
					.append(Community.TABLE_NAME).append(" ON ").append(CampaignFormData.COMMUNITY).append("_id = ")
					.append(Community.TABLE_NAME).append(".").append(Community.ID).append(" LEFT JOIN ")
					.append(Campaign.TABLE_NAME).append(" ON ").append(CampaignFormData.CAMPAIGN).append("_id = ")
					.append(Campaign.TABLE_NAME).append(".").append(Campaign.ID);

			if (series.getFieldId() != null) {
				joinBuilder.append(", json_array_elements(").append(CampaignFormData.FORM_VALUES)
						.append(") as jsonData, json_array_elements(").append(CampaignFormMeta.CAMPAIGN_FORM_ELEMENTS)
						.append(") as jsonMeta");
			}

			// WHERE
			StringBuilder whereBuilder = new StringBuilder(" WHERE ").append(CampaignFormMeta.TABLE_NAME).append(".")
					.append(CampaignFormMeta.FORM_ID).append(" = :campaignFormMetaId");

			String cds = "";

			if (series.getFieldId() != null) {
				whereBuilder.append(" AND jsonData->>'").append(CampaignFormDataEntry.ID)
						.append("' = :campaignFormDataId").append(" AND jsonData->>'")
						.append(CampaignFormDataEntry.VALUE).append("' IS NOT NULL AND jsonData->>'")
						.append(CampaignFormDataEntry.ID).append("' = jsonMeta->>'").append(CampaignFormElement.ID)
						.append("'");
			}

			whereBuilder.append(areaFilter).append(regionFilter).append(districtFilter).append(campaignFilter);// .append(campaignPhaseFilter);

			// GROUP BY
			StringBuilder groupByBuilder = new StringBuilder(" GROUP BY ").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.UUID).append(",").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.FORM_ID);

			if (series.getFieldId() != null) {
				groupByBuilder.append(", jsonData->>'").append(CampaignFormDataEntry.ID).append("', jsonMeta->>'")
						.append(CampaignFormElement.CAPTION).append("', jsonMeta->>'").append(CampaignFormElement.TYPE)
						.append("'");
			}

			switch (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) {
			case REGION:
				jurisdictionGrouping = ", " + Region.TABLE_NAME + "." + Region.UUID + ", " + Region.TABLE_NAME + "."
						+ Region.NAME;
				break;
			case DISTRICT:
				jurisdictionGrouping = ", " + District.TABLE_NAME + "." + District.UUID + ", " + District.TABLE_NAME
						+ "." + District.NAME;
				break;
			case COMMUNITY:
				jurisdictionGrouping = ", " + Community.TABLE_NAME + "." + Community.UUID + ", " + Community.TABLE_NAME
						+ "." + Community.NAME;
				break;
			case AREA:
			default:
				jurisdictionGrouping = ", " + Area.TABLE_NAME + "." + Area.UUID + ", " + Area.TABLE_NAME + "."
						+ Area.NAME;
			}

			groupByBuilder.append(jurisdictionGrouping);

			// ////System.out.println("getDataDiagram "+selectBuilder.toString() + " FROM "
			// + CampaignFormData.TABLE_NAME +" "+ joinBuilder +" "+ whereBuilder +" "+
			// groupByBuilder);

			// +selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME +
			// joinBuilder

			//@formatter:off
			Query seriesDataQuery = em.createNativeQuery(
					selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME + joinBuilder + whereBuilder + groupByBuilder);
			//@formatter:on

			seriesDataQuery.setParameter("campaignFormMetaId", series.getFormId());
			if (area != null) {
				seriesDataQuery.setParameter("areaUuid", area.getUuid());
			}
			if (region != null) {
				seriesDataQuery.setParameter("regionUuid", region.getUuid());
			}
			if (district != null) {
				seriesDataQuery.setParameter("districtUuid", district.getUuid());
			}
			if (campaign != null) {
				seriesDataQuery.setParameter("campaignUuid", campaign.getUuid());
			}

			if (series.getFieldId() != null) {
				seriesDataQuery.setParameter("campaignFormDataId", series.getFieldId());
			}

			System.out.println(" getDiagramData seriesDataQuery = " + selectBuilder.toString() + " FROM "
					+ CampaignFormData.TABLE_NAME + joinBuilder + whereBuilder + groupByBuilder);

			@SuppressWarnings("unchecked")
			List<Object[]> resultList = seriesDataQuery.getResultList();

			resultData.addAll(resultList.stream()
					.map((result) -> new CampaignDiagramDataDto((String) result[0], (String) result[1],
							(String) result[2], (String) result[3], result[4].toString(), (String) result[5],
							(String) result[6],
							// (String) result[7],
							series.getStack()))
					.collect(Collectors.toList()));
		}
		// System.out.println("resultData - "+
		// resultData.toString());//SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<CampaignDiagramDataDto> getDiagramDataByGroups(List<CampaignDiagramSeries> diagramSeries,
			CampaignDiagramCriteria campaignDiagramCriteria) {
		List<CampaignDiagramDataDto> resultData = new ArrayList<>();
		final AreaReferenceDto area = campaignDiagramCriteria.getArea();
		final RegionReferenceDto region = campaignDiagramCriteria.getRegion();
		final DistrictReferenceDto district = campaignDiagramCriteria.getDistrict();
		final CampaignReferenceDto campaign = campaignDiagramCriteria.getCampaign();

		/*
		 * String formType = campaignDiagramCriteria.getFormType();
		 * 
		 * if ("all phases".equals(formType)) { formType = null; }
		 */

		for (CampaignDiagramSeries series : diagramSeries) {
			//@formatter:off

				final String areaFilter = area != null ? " AND " + Area.TABLE_NAME + "." + Area.UUID + " = :areaUuid" : "";
				final String regionFilter = region != null ? " AND " + CampaignFormData.REGION + "." + Region.UUID + " = :regionUuid" : "";
				final String districtFilter = district != null ? " AND " + CampaignFormData.DISTRICT + "." + District.UUID + " = :districtUuid" : "";
				final String campaignFilter = campaign != null ? " AND " + Campaign.TABLE_NAME + "." + Campaign.UUID + " = :campaignUuid" : "";
			//	final String campaignPhaseFilter = formType != null ? " AND " + CampaignFormData.CAMPAIGN_FORM_META + ".formtype = '"+formType+"'" : "";
				//@formatter:on

			StringBuilder selectBuilder = new StringBuilder("SELECT ").append(CampaignFormMeta.TABLE_NAME).append(".")
					.append(CampaignFormMeta.UUID).append(" as formUuid,").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.FORM_ID).append(" as formId");

			if (series.getFieldId() != null) {

				selectBuilder.append(", jsonData->>'").append(CampaignFormDataEntry.ID)
						.append("' as fieldId, jsonMeta->>'").append(CampaignFormElement.CAPTION)
						.append("' as fieldCaption,");

				if (series.getReferenceValue() != null) {
					selectBuilder.append("sum (CASE WHEN ").append("((jsonData->>'").append(CampaignFormDataEntry.VALUE)
							.append("') = '").append(series.getReferenceValue()).append("') THEN 1 ELSE 0 END)");
				} else {
					selectBuilder.append(" CASE WHEN ((jsonMeta ->> '").append(CampaignFormElement.TYPE)
							.append("') = '").append(CampaignFormElementType.NUMBER.toString() + "') OR")

							.append("((jsonMeta ->> '").append(CampaignFormElement.TYPE).append("') = '")
							.append(CampaignFormElementType.DECIMAL.toString() + "') OR")

							.append("((jsonMeta ->> '").append(CampaignFormElement.TYPE).append("') = '")
							.append(CampaignFormElementType.RANGE.toString() + "')");

					if (series.getAverageDefault() != null && "true".equals(series.getAverageDefault())) {
						selectBuilder.append(" THEN avg(cast_number(jsonData->>'");
					} else {
						selectBuilder.append(" THEN sum(cast_number(jsonData->>'");
					}
					selectBuilder.append(CampaignFormDataEntry.VALUE).append("', 0)) ELSE sum(CASE WHEN(jsonData->>'")
							.append(CampaignFormDataEntry.VALUE).append("') = '")// .append(series.getReferenceValue())
							.append("' THEN 0 ELSE 1 END) END");

				}
				selectBuilder.append(" as sumValue");
			} else {
				selectBuilder.append(", null as fieldId, null as fieldCaption, count(formId) as sumValue");
			}
			/*
			 * final String jurisdictionGrouping; switch
			 * (campaignDiagramCriteria.getCampaignJurisdictionLevelGroupBy()) { case
			 * REGION: // appendInfrastructureSelection(selectBuilder, Region.TABLE_NAME,
			 * Region.NAME); break; case DISTRICT: //
			 * appendInfrastructureSelection(selectBuilder, District.TABLE_NAME,
			 * District.NAME); break; case COMMUNITY: //
			 * appendInfrastructureSelection(selectBuilder, Community.TABLE_NAME,
			 * Community.NAME); break; case AREA: default: //
			 * appendInfrastructureSelection(selectBuilder, Area.TABLE_NAME, Area.NAME); }
			 */
			// JOINS
			StringBuilder joinBuilder = new StringBuilder(" LEFT JOIN ").append(CampaignFormMeta.TABLE_NAME)
					.append(" ON ").append(CampaignFormData.CAMPAIGN_FORM_META).append("_id = ")
					.append(CampaignFormMeta.TABLE_NAME).append(".").append(CampaignFormMeta.ID).append(" LEFT JOIN ")
					.append(Region.TABLE_NAME).append(" ON ").append(CampaignFormData.REGION).append("_id =")
					.append(Region.TABLE_NAME).append(".").append(Region.ID).append(" LEFT JOIN ")
					.append(Area.TABLE_NAME).append(" ON ").append(CampaignFormData.TABLE_NAME).append(".")
					.append(Region.AREA).append("_id = ").append(Area.TABLE_NAME).append(".").append(Area.ID)
					.append(" LEFT JOIN ").append(District.TABLE_NAME).append(" ON ").append(CampaignFormData.DISTRICT)
					.append("_id = ").append(District.TABLE_NAME).append(".").append(District.ID).append(" LEFT JOIN ")
					.append(Community.TABLE_NAME).append(" ON ").append(CampaignFormData.COMMUNITY).append("_id = ")
					.append(Community.TABLE_NAME).append(".").append(Community.ID).append(" LEFT JOIN ")
					.append(Campaign.TABLE_NAME).append(" ON ").append(CampaignFormData.CAMPAIGN).append("_id = ")
					.append(Campaign.TABLE_NAME).append(".").append(Campaign.ID);

			if (series.getFieldId() != null) {
				joinBuilder.append(", json_array_elements(").append(CampaignFormData.FORM_VALUES)
						.append(") as jsonData, json_array_elements(").append(CampaignFormMeta.CAMPAIGN_FORM_ELEMENTS)
						.append(") as jsonMeta");
			}

			// WHERE
			StringBuilder whereBuilder = new StringBuilder(" WHERE ").append(CampaignFormMeta.TABLE_NAME).append(".")
					.append(CampaignFormMeta.FORM_ID).append(" = :campaignFormMetaId");

			String cds = "";

			if (series.getFieldId() != null) {
				whereBuilder.append(" AND jsonData->>'").append(CampaignFormDataEntry.ID)
						.append("' = :campaignFormDataId").append(" AND jsonData->>'")
						.append(CampaignFormDataEntry.VALUE).append("' IS NOT NULL AND jsonData->>'")
						.append(CampaignFormDataEntry.ID).append("' = jsonMeta->>'").append(CampaignFormElement.ID)
						.append("'");
			}

			whereBuilder.append(areaFilter).append(regionFilter).append(districtFilter).append(campaignFilter);// .append(campaignPhaseFilter);

			// GROUP BY
			StringBuilder groupByBuilder = new StringBuilder(" GROUP BY ").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.UUID).append(",").append(CampaignFormMeta.TABLE_NAME)
					.append(".").append(CampaignFormMeta.FORM_ID);

			if (series.getFieldId() != null) {
				groupByBuilder.append(", jsonData->>'").append(CampaignFormDataEntry.ID).append("', jsonMeta->>'")
						.append(CampaignFormElement.CAPTION).append("', jsonMeta->>'").append(CampaignFormElement.TYPE)
						.append("', campaigns.name");
			}

			//@formatter:off
			Query seriesDataQuery = em.createNativeQuery(
					selectBuilder.toString() + " FROM " + CampaignFormData.TABLE_NAME + joinBuilder + whereBuilder + groupByBuilder);
			//@formatter:on

			seriesDataQuery.setParameter("campaignFormMetaId", series.getFormId());
			if (area != null) {
				seriesDataQuery.setParameter("areaUuid", area.getUuid());
			}
			if (region != null) {
				seriesDataQuery.setParameter("regionUuid", region.getUuid());
			}
			if (district != null) {
				seriesDataQuery.setParameter("districtUuid", district.getUuid());
			}
			if (campaign != null) {
				seriesDataQuery.setParameter("campaignUuid", campaign.getUuid());
			}

			if (series.getFieldId() != null) {
				seriesDataQuery.setParameter("campaignFormDataId", series.getFieldId());
			}

			System.out.println("getDiagramDataByGroups" + selectBuilder.toString() + " FROM "
					+ CampaignFormData.TABLE_NAME + " " + joinBuilder + " " + whereBuilder + " " + groupByBuilder);

			@SuppressWarnings("unchecked")
			List<Object[]> resultList = seriesDataQuery.getResultList();

			resultData.addAll(resultList.stream()
					.map((result) -> new CampaignDiagramDataDto((String) result[0], (String) result[1],
							(String) result[2], (String) result[3], result[4].toString(), // (String) result[5],
							// (String) result[6],
							// (String) result[7],
							series.getStack()))
					.collect(Collectors.toList()));
		}
		return resultData;
	}

	public void checkLastAnalytics() {
		// completionanalysisview_e
		int isLocked = isUpdateTrakerLocked("camapaigndata_main");
		if (isLocked == 0) {
			updateTrakerTableCoreTimeStamp("campaignformdata");

			boolean isAnalyticsOld = campaignStatisticsService.checkChangedDb("campaignformdata", "camapaigndata_main");

			if (isAnalyticsOld) {
				final String jpqlQueries = "REFRESH MATERIALIZED VIEW CONCURRENTLY camapaigndata_main;";
				final String jpqlQueries_ = "REFRESH MATERIALIZED VIEW CONCURRENTLY camapaigndata_admin;";

				try {
					updateTrakerTable("camapaigndata_main", true);
					em.createNativeQuery(jpqlQueries).executeUpdate();

				} catch (Exception e) {
					System.err.println(e.getStackTrace());
				} finally {
					updateTrakerTable("camapaigndata_main", false);

					try {
						updateTrakerTable("camapaigndata_admin", true);
						em.createNativeQuery(jpqlQueries_).executeUpdate();

					} catch (Exception e) {
						System.err.println(e.getStackTrace());
					} finally {
						updateTrakerTable("camapaigndata_admin", false);
					}
				}

			}
		}
	}

	private int isUpdateTrakerLocked(String tableToCheck) {
		// get the total size of the analysis
		final String joinBuilder = "select count(*) from tracktableupdates where table_name = '" + tableToCheck
				+ "' and isLocked = true;";

		return Integer.parseInt(em.createNativeQuery(joinBuilder).getSingleResult().toString());

	};

	private void updateTrakerTable(String tabled, boolean isLocked_) {
		// get the total size of the analysis
		System.out.println("111111111111111111updateTrakerTable(    :" + tabled);
		final String joinBuilder = "INSERT INTO tracktableupdates (table_name, last_updated, islocked)\n"
				+ "    VALUES ('" + tabled + "', NOW(), " + isLocked_ + ")\n" + "    ON CONFLICT (table_name)\n"
				+ "    DO UPDATE SET last_updated = NOW(), isLocked = " + isLocked_ + ";";

		System.out.println("```````111111111111111111updateTrakerTable(    :" + joinBuilder);

		em.createNativeQuery(joinBuilder).executeUpdate();

	};

	private void updateTrakerTableCoreTimeStamp(String coretabled) {
		// get the total size of the analysis
		final String joinBuilder = "update tracktableupdates set last_updated = (select lastupdated from campaignformdata ORDER BY lastupdated DESC limit 1) "
				+ "where table_name = 'campaignformdata';";

		em.createNativeQuery(joinBuilder).executeUpdate();

	};

	@Override
	public List<CampaignFormDataIndexDto> getByCompletionAnalysisAdmin(CampaignFormDataCriteria criteria, Integer first,
			Integer max, List<SortProperty> sortProperties, FormAccess frm) {

		int isLocked = isUpdateTrakerLocked("completionanalysisview_e");
		System.out.println(" =========ADMINNN===--------------------: " + isLocked);
		if (isLocked == 1) {
			// RESET TO LAST TIME CAMPAIGN FORM WAS SUBMITTED
			updateTrakerTableCoreTimeStamp("campaignformdata");

			boolean isAnalyticsOld = campaignStatisticsService.checkChangedDb("campaignformdata",
					"completionanalysisview_e");
			System.out.println(" =========ADMINNN===--------------------: " + isAnalyticsOld);
			if (isAnalyticsOld) {
				try {
					updateTrakerTable("completionanalysisview_e", true);
					int noUse = prepareAllCompletionAnalysis();
				} finally {
					updateTrakerTable("completionanalysisview_e", false);
				}
			}
		}
		boolean filterIsNull = criteria.getCampaign() == null;

		String joiner = "";

		if (!filterIsNull) {
			final CampaignReferenceDto campaign = criteria.getCampaign();
			final AreaReferenceDto area = criteria.getArea();
			final RegionReferenceDto region = criteria.getRegion();
			final DistrictReferenceDto district = criteria.getDistrict();

		//@formatter:off
		
		final String campaignFilter = campaign != null ? "analyticz.campaigns_uuid = '"+campaign.getUuid()+"'" : "";
		final String areaFilter = area != null ? "AND analyticz.areas_uuid = '"+area.getUuid()+"'" : "";
		final String regionFilter = region != null ? " AND analyticz.region_uuid = '"+region.getUuid()+"'" : "";
		final String districtFilter = district != null ? " AND analyticz.district_uuid = '"+district.getUuid()+"'" : "";
		
		joiner = "where " + campaignFilter + areaFilter + regionFilter + districtFilter ;
		
//		System.out.println(campaignFilter+" =========ADMINNN============ "+joiner);
		}
		
		

		String orderby = "";
//		System.out.println(" ====orderbyorderb++ "+sortProperties.size());
		
		
		
		if (sortProperties != null && sortProperties.size() > 0) {
			for (SortProperty sortProperty : sortProperties) {
				System.out.println(" ====orderbyorderb++ "+sortProperty.propertyName);
				switch (sortProperty.propertyName) {
				case "region":
					orderby = orderby.isEmpty() ? " order by analyticz.area " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.area " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "province":
					orderby = orderby.isEmpty() ? " order by analyticz.region " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.region " + (sortProperty.ascending ? "asc" : "desc");
				break;
					
				case "district":
					orderby = orderby.isEmpty() ? " order by analyticz.district " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.district " + (sortProperty.ascending ? "asc" : "desc");
				break;	
				
				case "cluster":
					orderby = orderby.isEmpty() ? " order by commut.\"name\" " + (sortProperty.ascending ? "asc" : "desc") : orderby+", commut.\"name\" " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "clusterNumberr":
					orderby = orderby.isEmpty() ? " order by commut.clusternumber " + (sortProperty.ascending ? "asc" : "desc") : orderby+", commut.clusternumber " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "ccode":
					orderby = orderby.isEmpty() ? " order by commut.externalid " + (sortProperty.ascending ? "asc" : "desc") : orderby+", commut.externalid " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "dayOne":
					orderby = orderby.isEmpty() ? " order by analyticz.day1 " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.day1 " + (sortProperty.ascending ? "asc" : "desc");
				break;
				
				case "dayTwo":
				orderby = orderby.isEmpty() ? " order by analyticz.day2 " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.day2 " + (sortProperty.ascending ? "asc" : "desc");
			break;
				case "dayThree":
				orderby = orderby.isEmpty() ? " order by analyticz.day3 " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.day3 " + (sortProperty.ascending ? "asc" : "desc");
			break;
				case "dayFour":
				orderby = orderby.isEmpty() ? " order by analyticz.day4 " + (sortProperty.ascending ? "asc" : "desc") : orderby+", analyticz.day4 " + (sortProperty.ascending ? "asc" : "desc");
			break;
				
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				
			}
		}
		
//		System.out.println(" ====orderbyorderbyderby====== "+orderby);
		
		
		
		final String joinBuilder = "select analyticz.area as area_, analyticz.region as region_, analyticz.district as district_, commut.\"name\" as communit_name, commut.clusternumber as clusternumber_, commut.externalid as ccode,\n"
				+ "analyticz.day1, analyticz.day2, analyticz.day3, analyticz.day4, analyticz.campaigns_uuid\n"
				+ "from camapaigndata_admin analyticz\n"
				+ "left outer join community commut on analyticz.community_uuid = commut.uuid\n"
				+ ""+joiner+"\n"
				+ orderby
				+ " limit "+max+" offset "+first+";";
		
//	System.out.println("=====seriesDataQueryADMINN======== "+joinBuilder);
		
		
		Query seriesDataQuery = em.createNativeQuery(joinBuilder);
		
		List<CampaignFormDataIndexDto> resultData = new ArrayList<>();
		
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList(); 
		
		
		resultData.addAll(resultList.stream()
				.map((result) -> new CampaignFormDataIndexDto((String) result[0].toString(), (String) result[1].toString(),
						(String) result[2].toString(), (String) result[3].toString(),
						(Integer) result[4], 
						((BigInteger) result[5]).longValue(),						
						((BigDecimal) result[6]).intValue(),
						((BigDecimal) result[7]).intValue(),
						((BigDecimal) result[8]).intValue(),
						((BigDecimal) result[9]).intValue()
				)).collect(Collectors.toList()));
		
	//	//System.out.println("ending...." +resultData.size());
	
	
//	System.out.println("resultData - "+ SQLExtractor.from(seriesDataQuery));
	return resultData;
	
	}

	@Override
	public String getByCompletionAnalysisCountAdmin(CampaignFormDataCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess frm) {

		
//		System.out.println(" ==============getByCompletionAnalysisCountAdmin======= ");
		boolean filterIsNull = false;
			if(criteria != null) {
				filterIsNull = criteria.getCampaign() == null;
			}
		String joiner = "";

		if (!filterIsNull && criteria != null) {
			final CampaignReferenceDto campaign = criteria.getCampaign();
			final AreaReferenceDto area = criteria.getArea();
			final RegionReferenceDto region = criteria.getRegion();
			final DistrictReferenceDto district = criteria.getDistrict();

			//@formatter:off
			final String campaignFilter = campaign != null ? "campaigns_uuid = '"+campaign.getUuid()+"'" : "";
			final String areaFilter = area != null ? " AND  areas_uuid = '"+area.getUuid()+"'" : "";
			final String regionFilter = region != null ? " AND region_uuid = '"+region.getUuid()+"'" : "";
			final String districtFilter = district != null ? " AND district_uuid = '"+district.getUuid()+"'" : "";
			joiner = "where "+campaignFilter +areaFilter + regionFilter + districtFilter ;
			
//			System.out.println(campaignFilter+" ===================== "+joiner);
		}
		
		final String joinBuilder = "select count(*)\n"
				+ "from camapaigndata_admin\n"
				+ ""+joiner+";";
		
		
		System.out.println(joinBuilder+" ===========cont query ========== ");

	return ((BigInteger) em.createNativeQuery(joinBuilder).getSingleResult()).toString();
	}
	
	
	public List<CampaignFormDataIndexDto> getCreatingUsersUserType(String username) {
		
		final String fetchUserTypeByUsername = "select usertype from users where username ilike '" + username + "';";
		
		Query seriesDataQuery =  em.createNativeQuery(fetchUserTypeByUsername);
		
List<CampaignFormDataIndexDto> resultData = new ArrayList<>();
		

@SuppressWarnings("unchecked")
List<String> resultList = seriesDataQuery.getResultList(); // Change the type to List<String>

resultData.addAll(resultList.stream()
        .map(result -> new CampaignFormDataIndexDto(result))
        .collect(Collectors.toList()));

		
//		@SuppressWarnings("unchecked")
//		List<String[]> resultList = seriesDataQuery.getResultList(); 
//	
//		
//		 resultData.addAll(resultList.stream()
//		            .map(result -> new CampaignFormDataIndexDto((String) result[0].toString()))
//		            .collect(Collectors.toList()));
		
		return resultData;
	}
	
	
	@Override
	public  void updateFormDataUnitAssignment(String formDataUuid, String clusterUuid) {
		// TODO Auto-generated method stub
		
		campaignFormDataService.updateFormDataUnitAssignment(formDataUuid, clusterUuid);
		
	}
	
	

	@LocalBean
	@Stateless
	public static class CampaignFormDataFacadeEjbLocal extends CampaignFormDataFacadeEjb {
		
		public CampaignFormDataFacadeEjbLocal() {
			
		}
		
		
	
	}




		
}