/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
package de.symeda.sormas.backend.infrastructure.district;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import org.joda.time.LocalDateTime;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.ErrorStatusEnum;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictFacade;
import de.symeda.sormas.api.infrastructure.district.DistrictHistoryExtractDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.AbstractInfrastructureEjb;
import de.symeda.sormas.backend.infrastructure.PopulationDataFacadeEjb.PopulationDataFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntry;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "DistrictFacade")
public class DistrictFacadeEjb extends AbstractInfrastructureEjb<District, DistrictService> implements DistrictFacade {

	private ErrorStatusEnum errorStatusEnum;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private UserService userService;
	@EJB
	private AreaService areaService;
	@EJB
	private RegionService regionService;
	@EJB
	private PopulationDataFacadeEjbLocal populationDataFacade;

	public DistrictFacadeEjb() {
	}

	@Inject
	protected DistrictFacadeEjb(DistrictService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
		super(service, featureConfiguration);
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveAsReference() {
		return service.getAllActive(District.NAME, true).stream().map(f -> toReferenceDto(f))
				.collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveAsReferencePashto() {
		return service.getAllActive(District.PS_AF, true).stream().filter(d -> d.getPs_af() != null)
				.map(DistrictFacadeEjb::toReferenceDtoP).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveAsReferenceDari() {
		return service.getAllActive(District.FA_AF, true).stream().filter(d -> d.getFa_af() != null)
				.map(DistrictFacadeEjb::toReferenceDtoD).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveByArea(String areaUuid) {

		Area area = areaService.getByUuid(areaUuid);
		return service.getAllActiveByArea(area).stream().map(f -> toReferenceDto(f)).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveByRegion(String regionUuid) {

		Region region = regionService.getByUuid(regionUuid);
		return region.getDistricts().stream().filter(d -> !d.isArchived() && d.getName() != null && d.getExternalId() != null).map(f -> toReferenceDto(f))
				.collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveByRegionPashto(String regionUuid) {
		Region region = regionService.getByUuid(regionUuid);
		return region.getDistricts().stream().filter(d -> !d.isArchived() && d.getPs_af() != null)
				.map(DistrictFacadeEjb::toReferenceDtoP).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveByRegionDari(String regionUuid) {
		Region region = regionService.getByUuid(regionUuid);
		return region.getDistricts().stream().filter(d -> !d.isArchived() && d.getFa_af() != null)
				.map(DistrictFacadeEjb::toReferenceDtoD).collect(Collectors.toList());
	}

	@Override
	public List<DistrictDto> getAllAfter(Date date) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<DistrictDto> cq = cb.createQuery(DistrictDto.class);
		Root<District> district = cq.from(District.class);

		selectDtoFields(cq, district);

		Predicate filter = service.createChangeDateFilter(cb, district, date);

		if (filter != null) {
			cq.where(filter);
		}

		return em.createQuery(cq).getResultList();
	}

	// Need to be in the same order as in the constructor
	private void selectDtoFields(CriteriaQuery<DistrictDto> cq, Root<District> root) {

		Join<District, Region> region = root.join(District.REGION, JoinType.LEFT);

		cq.multiselect(root.get(District.CREATION_DATE), root.get(District.CHANGE_DATE), root.get(District.UUID),
				root.get(District.ARCHIVED), root.get(District.NAME), root.get(District.EPID_CODE),
				root.get(District.RISK), root.get(District.GROWTH_RATE), region.get(Region.UUID),
				region.get(Region.NAME), region.get(Region.EXTERNAL_ID), root.get(District.EXTERNAL_ID));
	}

	@Override
	public DistrictDto getByUuid(String uuid) {
		return toDto(service.getByUuid(uuid));
	}

	@Override
	public List<DistrictIndexDto> getIndexList(DistrictCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {

		// System.out.println("++++++++++++++++++: from"+ first +" -- "+max);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<District> cq = cb.createQuery(District.class);
		Root<District> district = cq.from(District.class);
		Join<District, Region> region = district.join(District.REGION, JoinType.LEFT);

		Predicate filter = null;
		if (criteria != null) {

//			System.out.println(criteria.getRelevanceStatus() + "zzzrelevamce status in Ejbbbbbbbb ");

			filter = service.buildCriteriaFilter(criteria, cb, district);
		} else {
//			System.out.println(criteria.getRelevanceStatus() + "relevamce status in Ejbbbbbbbb ");
		}

		Predicate filterx = cb.and(cb.isNotNull(district.get(District.EXTERNAL_ID)),
				cb.equal(district.get(District.ARCHIVED), false), cb.isNotNull(district.get(District.ARCHIVED)));
		Predicate filterxx = cb.and(cb.isNotNull(district.get(District.EXTERNAL_ID)),
				cb.equal(district.get(District.ARCHIVED), true), cb.isNotNull(district.get(District.ARCHIVED)));
		Predicate filterForAll = cb.and(cb.isNotNull(district.get(District.ARCHIVED)), cb.isNotNull(district.get(District.EXTERNAL_ID)));

		System.out.println(criteria.getRelevanceStatus() + "criteria relevance status ---------------------");

		if (filter != null) {
			
			if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				cq.where(filter, filterxx);

			} else if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE){
				cq.where(filter, filterx);

			} else if (criteria.getRelevanceStatus().equals("All")){
				cq.where(filter, filterForAll);

			}
//			cq.where(filter, filterx);
		} else {
			cq.where(filterx);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case District.NAME:
				case District.EPID_CODE:
				case District.RISK:
				case District.GROWTH_RATE:
				case District.EXTERNAL_ID:
					expression = district.get(sortProperty.propertyName);
					break;

				case District.REGION:
				case DistrictIndexDto.REGION_EXTERNALID:
				case DistrictIndexDto.AREA_EXTERNAL_ID:
				case DistrictIndexDto.AREA_NAME:
					expression = region.get(Region.NAME);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.asc(region.get(Region.NAME)), cb.asc(district.get(District.NAME)));
		}

		cq.select(district);

		System.out.println("+++++++++++++++ resultData - " + SQLExtractor.from(em.createQuery(cq)));

		return QueryHelper.getResultList(em, cq, first, max, this::toIndexDto);
	}

	public Page<DistrictIndexDto> getIndexPage(DistrictCriteria districtCriteria, Integer offset, Integer size,
			List<SortProperty> sortProperties) {
		List<DistrictIndexDto> districtIndexList = getIndexList(districtCriteria, offset, size, sortProperties);
		long totalElementCount = count(districtCriteria);
		return new Page<>(districtIndexList, offset, size, totalElementCount);
	}

	@Override
	public long count(DistrictCriteria criteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<District> root = cq.from(District.class);

		Predicate filter = null;

		if (criteria != null) {
			filter = service.buildCriteriaFilter(criteria, cb, root);

		}

		Predicate filterx = cb.and(cb.isNotNull(root.get(District.EXTERNAL_ID)),
				cb.equal(root.get(District.ARCHIVED), false), cb.isNotNull(root.get(District.ARCHIVED)));

		if (filter != null) {
			cq.where(filter);
		} else {
			cq.where(filterx);
		}

		cq.select(cb.count(root));

		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<String> getAllUuids() {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return service.getAllUuids();
	}

	@Override
	public int getCountByRegion(String regionUuid) {

		Region region = regionService.getByUuid(regionUuid);
		return service.getCountByRegion(region);
	}

	@Override
	public DistrictDto getDistrictByUuid(String uuid) {
		return toDto(service.getByUuid(uuid));
	}

	@Override
	public List<DistrictDto> getByUuids(List<String> uuids) {
		return service.getByUuids(uuids).stream().map(c -> toDto(c)).collect(Collectors.toList());
	}

	@Override
	public DistrictReferenceDto getDistrictReferenceByUuid(String uuid) {
		return toReferenceDto(service.getByUuid(uuid));
	}

	@Override
	public DistrictReferenceDto getDistrictReferenceById(long id) {
		return toReferenceDto(service.getById(id));
	}

	@Override
	public Map<String, String> getRegionUuidsForDistricts(List<DistrictReferenceDto> districts) {

		if (districts.isEmpty()) {
			return new HashMap<>();
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<District> root = cq.from(District.class);
		Join<District, Region> regionJoin = root.join(District.REGION, JoinType.LEFT);

		Predicate filter = root.get(District.UUID)
				.in(districts.stream().map(ReferenceDto::getUuid).collect(Collectors.toList()));
		cq.where(filter);
		cq.multiselect(root.get(District.UUID), regionJoin.get(Region.UUID));

		return em.createQuery(cq).getResultList().stream()
				.collect(Collectors.toMap(e -> (String) e[0], e -> (String) e[1]));
	}

	@Override
	public DistrictDto save(@Valid DistrictDto dto) throws ValidationRuntimeException {
		return save(dto, false);
	}

	@Override
	public DistrictDto save(@Valid DistrictDto dto, boolean allowMerge) throws ValidationRuntimeException {
		checkInfraDataLocked();

		if (dto.getRegion() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validRegion));
		}

		District district = service.getByUuid(dto.getUuid());

		if (district == null) {
			List<DistrictReferenceDto> duplicates = getByName(dto.getName(), dto.getRegion(), true);
			if (!duplicates.isEmpty()) {
				if (allowMerge) {
					String uuid = duplicates.get(0).getUuid();
					district = service.getByUuid(uuid);
					DistrictDto dtoToMerge = getDistrictByUuid(uuid);
					dto = DtoHelper.copyDtoValues(dtoToMerge, dto, true);
				} else {
					throw new ValidationRuntimeException(
							I18nProperties.getValidationError(Validations.importDistrictAlreadyExists));
				}
			}
		}

		district = fillOrBuildEntity(dto, district, true);
		service.ensurePersisted(district);
		return toDto(district);
	}

	@Override
	public List<DistrictReferenceDto> getByName(String name, RegionReferenceDto regionRef,
			boolean includeArchivedEntities) {

		return service.getByName(name, regionService.getByReferenceDto(regionRef), includeArchivedEntities).stream()
				.map(DistrictFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getByExternalID(Long ext_id, RegionReferenceDto regionRef,
			boolean includeArchivedEntities) {
		// System.out.println("++++REGION IN USE TO QUERY DISTRICT b
		// "+regionService.getByReferenceDto(regionRef));
		return service.getByExternalId(ext_id, regionService.getByReferenceDto(regionRef), includeArchivedEntities, 0)
				.stream().map(DistrictFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getByExternalId(Long externalId, boolean includeArchivedEntities) {

		return service.getByExternalId(externalId, includeArchivedEntities).stream()
				.map(DistrictFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	@Override
	public List<DistrictReferenceDto> getReferencesByName(String name, boolean includeArchived) {
		return getByName(name, null, false);
	}

	@Override
	public List<String> getNamesByIds(List<Long> districtIds) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<District> root = cq.from(District.class);

		Predicate filter = root.get(District.ID).in(districtIds);
		cq.where(filter);
		cq.select(root.get(District.NAME));
		return em.createQuery(cq).getResultList();
	}

	@Override
	public boolean isUsedInOtherInfrastructureData(Collection<String> districtUuids) {

		return service.isUsedInInfrastructureData(districtUuids, Community.DISTRICT, Community.class)
				|| service.isUsedInInfrastructureData(districtUuids, Facility.DISTRICT, Facility.class)
				|| service.isUsedInInfrastructureData(districtUuids, PointOfEntry.DISTRICT, PointOfEntry.class);
	}

	@Override
	public boolean hasArchivedParentInfrastructure(Collection<String> districtUuids) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<District> root = cq.from(District.class);
		Join<District, Region> regionJoin = root.join(District.REGION);

		cq.where(cb.and(cb.isTrue(regionJoin.get(Region.ARCHIVED)), root.get(District.UUID).in(districtUuids)));

		cq.select(root.get(District.ID));

		return QueryHelper.getFirstResult(em, cq) != null;
	}

	public static DistrictReferenceDto toReferenceDto(District entity) {

		if (entity == null) {
			return null;
		}

		DistrictReferenceDto dto = new DistrictReferenceDto(entity.getUuid(), entity.toString(),
				entity.getExternalId());
		return dto;
	}

	public static DistrictReferenceDto toReferenceDtoP(District entity) {

		if (entity == null) {
			return null;
		}

		DistrictReferenceDto dto = new DistrictReferenceDto(entity.getUuid(), entity.getPs_af());
		return dto;
	}

	public static DistrictReferenceDto toReferenceDtoD(District entity) {

		if (entity == null) {
			return null;
		}

		DistrictReferenceDto dto = new DistrictReferenceDto(entity.getUuid(), entity.getFa_af());
		return dto;
	}

	public DistrictDto toDto(District entity) {

		if (entity == null) {
			return null;
		}

		DistrictDto dto = new DistrictDto();
		DtoHelper.fillDto(dto, entity);

		dto.setName(entity.getName());
		dto.setFa_af(entity.getFa_af());
		dto.setPs_af(entity.getPs_af());
		dto.setEpidCode(entity.getEpidCode());
		dto.setRisk(entity.getRisk());
		dto.setGrowthRate(entity.getGrowthRate());
		dto.setRegion(RegionFacadeEjb.toReferenceDto(entity.getRegion()));
		dto.setArchived(entity.isArchived());
		dto.setExternalId(entity.getExternalId());

		return dto;
	}

	public static Set<DistrictReferenceDto> toReferenceDto(HashSet<District> districts) {
		Set<DistrictReferenceDto> dtos = new HashSet<DistrictReferenceDto>();
		for (District district : districts) {
			DistrictReferenceDto districtDto = new DistrictReferenceDto(district.getUuid(), district.toString(),
					district.getExternalId());
			dtos.add(districtDto);
		}

		return dtos;
	}

	public DistrictIndexDto toIndexDto(District entity) {

		if (entity == null) {
			return null;
		}

		DistrictIndexDto dto = new DistrictIndexDto();
		DtoHelper.fillDto(dto, entity);

		dto.setName(entity.getName());
		dto.setFa_af(entity.getFa_af());
		dto.setPs_af(entity.getPs_af());
		dto.setEpidCode(entity.getEpidCode());
		dto.setRisk(entity.getRisk());
		dto.setGrowthRate(entity.getGrowthRate());
		// dto.setPopulation(populationDataFacade.getDistrictPopulation(dto.getUuid()));
		// System.out.println("_______________________________________:
		// "+entity.getUuid());
		dto.setAreaexternalId(entity.getRegion().getArea().getExternalId());
		dto.setAreaname(entity.getRegion().getArea().getName());
		dto.setRegionexternalId(entity.getRegion().getExternalId());
		dto.setRegion(RegionFacadeEjb.toReferenceDto(entity.getRegion()));
		dto.setArchived(entity.isArchived());

		dto.setExternalId(entity.getExternalId());

		return dto;
	}

	private District fillOrBuildEntity(@NotNull DistrictDto source, District target, boolean checkChangeDate) {

		target = DtoHelper.fillOrBuildEntity(source, target, District::new, checkChangeDate);

		target.setName(source.getName());
		target.setEpidCode(source.getEpidCode());
		target.setRisk(source.getRisk());
		target.setGrowthRate(source.getGrowthRate());
		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setArchived(source.isArchived());
		target.setExternalId(source.getExternalId());

		return target;
	}

	@Override
	public String getFullEpidCodeForDistrict(String districtUuid) {
		District district = service.getByUuid(districtUuid);
		return getFullEpidCodeForDistrict(district);
	}

	private String getFullEpidCodeForDistrict(District district) {
		return (district.getRegion().getEpidCode() != null ? district.getRegion().getEpidCode() : "") + "-"
				+ (district.getEpidCode() != null ? district.getEpidCode() : "");
	}

	@LocalBean
	@Stateless
	public static class DistrictFacadeEjbLocal extends DistrictFacadeEjb {

		public DistrictFacadeEjbLocal() {
		}

		@Inject
		protected DistrictFacadeEjbLocal(DistrictService service,
				FeatureConfigurationFacadeEjbLocal featureConfiguration) {
			super(service, featureConfiguration);
		}
	}

	@Override
	public List<DistrictDto> getAllActiveAsReferenceAndPopulation(Long regionId, CampaignDto campaignDt) {
		String queryStringBuilder = "select a.name,"
				+ " SUM(CASE WHEN p.agegroup = 'AGE_0_4' THEN p.population ELSE 0 END) AS population_age_0_4,\n"
				+ "    SUM(CASE WHEN p.agegroup = 'AGE_5_10' THEN p.population ELSE 0 END) AS population_age_5_10,"
				+ " a.id, ar.uuid as umid, a.uuid as uimn, p.selected, p.modality, p.districtstatus from district a\n"
				+ " left outer join populationdata p on a.id = p.district_id\n"
				+ "left outer join region ar on ar.id = " + regionId + "\n"
				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and a.region_id = "
				+ regionId + " and ca.uuid = '" + campaignDt.getUuid() + "'\n"
				+ " group by a.name,  a.id, ar.uuid, a.uuid, p.selected, p.modality, p.districtstatus";

		System.out.println("::::::" + queryStringBuilder);
		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);

		List<DistrictDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		// System.out.println("starting....");

		resultData.addAll(resultList.stream()
				.map((result) -> new DistrictDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), ((BigInteger) result[3]).longValue(),
						(String) result[4].toString(), (String) result[5].toString(), (String) result[6].toString(),
						(String) result[7].toString(), (String) result[8].toString()))
//						,
//						(String) result[9].toString() ))
				.collect(Collectors.toList()));

//		resultData.addAll(resultList.stream()
//				.map((result) -> new DistrictDto(
//						(String) result[0].toString(), 
//						((Integer) result[1]).longValue(),
//						((BigInteger) result[2]).longValue(), 
//						(String) result[3].toString(),
//						(String) result[4].toString(), 
//						(String) result[5].toString(), 
//						(String) result[6].toString(),
//						(String) result[7].toString(),
//						(String) result[8].toString() ))
//				.collect(Collectors.toList()));

		// System.out.println("ending...." +resultData.size());

//		 System.out.println(//"resultData - "+ resultData.toString());
//				 "DUMBGFyyresultData - "+SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<DistrictDto> getAllActiveAsReferenceAndPopulationPashto(Long regionId, CampaignDto campaignDt) {
		String queryStringBuilder = "select a.\"ps_af\", SUM(CASE WHEN p.agegroup = 'AGE_0_4' THEN p.population ELSE 0 END) AS population_age_0_4,\n"
				+ "    SUM(CASE WHEN p.agegroup = 'AGE_5_10' THEN p.population ELSE 0 END) AS population_age_5_10,"
				+ " a.id, ar.uuid as umid, a.uuid as uimn, p.selected, p.modality, p.districtstatus from district a\n"
				+ " left outer join populationdata p on a.id = p.district_id\n"
				+ "left outer join region ar on ar.id = " + regionId + "\n"
				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and a.region_id = "
				+ regionId + " and ca.uuid = '" + campaignDt.getUuid() + "'\n"
				+ " group by a.name,  a.id, ar.uuid, a.uuid, p.selected, p.modality, p.districtstatus";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
		List<DistrictDto> resultData = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		resultData.addAll(resultList.stream()
				.map((result) -> new DistrictDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), ((BigInteger) result[3]).longValue(),
						(String) result[4].toString(), (String) result[5].toString(), (String) result[6].toString(),
						(String) result[7].toString(), (String) result[8].toString()))

				.collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public List<DistrictDto> getAllActiveAsReferenceAndPopulationDari(Long regionId, CampaignDto campaignDt) {

		String queryStringBuilder = "select a.\"fa_af\", SUM(CASE WHEN p.agegroup = 'AGE_0_4' THEN p.population ELSE 0 END) AS population_age_0_4,\n"
				+ "    SUM(CASE WHEN p.agegroup = 'AGE_5_10' THEN p.population ELSE 0 END) AS population_age_5_10,"
				+ " a.id, ar.uuid as umid, a.uuid as uimn, p.selected, p.modality, p.districtstatus from district a\n"
				+ " left outer join populationdata p on a.id = p.district_id\n"
				+ "left outer join region ar on ar.id = " + regionId + "\n"
				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and a.region_id = "
				+ regionId + " and ca.uuid = '" + campaignDt.getUuid() + "'\n"
				+ " group by a.name,  a.id, ar.uuid, a.uuid, p.selected, p.modality, p.districtstatus";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
		List<DistrictDto> resultData = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		resultData.addAll(resultList.stream()
				.map((result) -> new DistrictDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), ((BigInteger) result[3]).longValue(),
						(String) result[4].toString(), (String) result[5].toString(), (String) result[6].toString(),
						(String) result[7].toString(), (String) result[8].toString()))
				.collect(Collectors.toList()));

		return resultData;
	}

	public List<DistrictIndexDto> getAllDistricts() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<District> cq = cb.createQuery(District.class);
		Root<District> district = cq.from(District.class);
		Join<District, Region> region = district.join(District.REGION, JoinType.LEFT);
		Join<Region, Area> area = region.join(Region.AREA, JoinType.LEFT);

		Predicate filter = cb.equal(region.get(Region.ARCHIVED), false);
		cq.where(filter);
		cq.select(district);
		List<District> regions = em.createQuery(cq).getResultList();
		List<DistrictIndexDto> dtos = new ArrayList();

		for (District reg : regions) {
			if (!reg.equals(null)) {
				dtos.add(this.toIndexDto(reg));
			}
		}
		return dtos;

	}

	@Override
	public boolean isDistrictAllowed(String campaignUUID, String uuid) {

		String selectBuilder = "select count(*) \n" + "from district d\n"
				+ "inner join populationdata p on d.id = p.district_id\n"
				+ "inner join campaigns c on p.campaign_id = c.id\n" + "where c.uuid = '" + campaignUUID
				+ "' and p.selected = true and d.archived = false and d.uuid = '" + uuid + "';";

		List<Object> lstf = em.createNativeQuery(selectBuilder).getResultList();

		return lstf.size() > 0;
	}

	@Override
	public List<DistrictReferenceDto> getAllActiveByRegionAndSelectedInCampaign(String regionUuid,
			String campaignUuid) {
		String selectBuilder = "select distinct d.uuid, d.\"name\", d.externalid\r\n" + "from district d\r\n"
				+ "inner join region r on r.id = d.region_id\r\n"
				+ "inner join populationdata p on d.id = p.district_id\r\n"
				+ "inner join campaigns c on p.campaign_id = c.id\r\n" + "where c.uuid = '" + campaignUuid
				+ "' and p.selected = true and d.archived = false and r.uuid = '" + regionUuid + "' ;";

		Query seriesDataQuery = em.createNativeQuery(selectBuilder);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		List<DistrictReferenceDto> resultData = new ArrayList<>();
		resultData.addAll(resultList.stream().map((result) -> new DistrictReferenceDto((String) result[0],
				(String) result[1], ((BigInteger) result[2]).longValue()

		)).collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public String getMapDiagramDistrictHascByUuid(String districtUuid) {
		System.out.println(districtUuid + "UUID----");
		try {
			String selectBuilder = "select d.hasc from district d where d.uuid = '" + districtUuid + "';";

			Query seriesDataQuery = em.createNativeQuery(selectBuilder);
			return seriesDataQuery.getSingleResult().toString();
		} catch (NoResultException e) {
			return "";
		}
	}
	
	@Override
	public List<DistrictHistoryExtractDto> getDistrictsHistory(String uuid){
	List<DistrictHistoryExtractDto> resultData = new ArrayList<>();
		
		StringBuilder queryStringBuilder = new StringBuilder();
		queryStringBuilder.append("WITH current_data AS (")
		                  .append("SELECT id, uuid, archived, externalid, name, changedate AS start_date, ")
		                  .append("LEAD(changedate) OVER (PARTITION BY uuid ORDER BY changedate) AS end_date ")
		                  .append("FROM district_history ");

		if (uuid != null) {
		    queryStringBuilder.append("WHERE uuid = '").append(uuid).append("' ");
		}

		queryStringBuilder.append("), ")
		                  .append("updated_end_date AS (")
		                  .append("SELECT cd.id, cd.uuid, cd.archived, cd.externalid, cd.name, cd.start_date, ")
		                  .append("COALESCE(cd.end_date, (SELECT changedate FROM district WHERE district.uuid = cd.uuid)) AS end_date ")
		                  .append("FROM current_data cd) ")
		                  .append("SELECT uuid, name, archived, externalid,  start_date, end_date ")
		                  .append("FROM updated_end_date ")
		                  .append("ORDER BY start_date ASC;");
		
		String queryString = queryStringBuilder.toString();
		
		Query seriesDataQuery = em.createNativeQuery(queryString);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
			    .map(result -> new DistrictHistoryExtractDto(
			        (String) result[0], // UUID
			        (String) result[1], // Name
			        (Boolean) result[2], // Archived
			        result[3] != null ? ((BigInteger) result[3]).longValue() : 0L, // External ID
			        ((Timestamp) result[4]).toLocalDateTime(), // Start Date
			        result[5] != null ? ((Timestamp) result[5]).toLocalDateTime() : LocalDateTime.now() // End Date
			    ))
			    .collect(Collectors.toList()));

		return resultData;
	}

	public static Set<DistrictReferenceDto> toReferenceDto(Set<District> district) { // save

		Set<DistrictReferenceDto> dtos = new HashSet<DistrictReferenceDto>();
		for (District com : district) {
			DistrictReferenceDto dto = new DistrictReferenceDto(com.getUuid(), com.toString(), com.getExternalId());
			dtos.add(dto);
		}

		return dtos;
	}

}
