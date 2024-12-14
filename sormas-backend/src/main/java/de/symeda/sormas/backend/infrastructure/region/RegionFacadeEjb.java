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
package de.symeda.sormas.backend.infrastructure.region;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDateTime;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaHistoryExtractDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionFacade;
import de.symeda.sormas.api.infrastructure.region.RegionHistoryExtractDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.AbstractInfrastructureEjb;
import de.symeda.sormas.backend.infrastructure.PopulationDataFacadeEjb.PopulationDataFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.area.AreaFacadeEjb;
import de.symeda.sormas.backend.infrastructure.area.AreaService;
import de.symeda.sormas.backend.infrastructure.community.CommunityFacadeEjb;
import de.symeda.sormas.backend.infrastructure.country.Country;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb.CountryFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.country.CountryService;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.infrastructure.pointofentry.PointOfEntry;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "RegionFacade")
public class RegionFacadeEjb extends AbstractInfrastructureEjb<Region, RegionService> implements RegionFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private UserService userService;
	@EJB
	private PopulationDataFacadeEjbLocal populationDataFacade;
	@EJB
	private AreaService areaService;
	@EJB
	private CountryService countryService;
	@EJB
	private CountryFacadeEjbLocal countryFacade;

	public RegionFacadeEjb() {
	}

	@Inject
	protected RegionFacadeEjb(RegionService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
		super(service, featureConfiguration);
	}

	@Override
	public List<RegionReferenceDto> getAllActiveByServerCountry() {
		CountryReferenceDto serverCountry = countryFacade.getServerCountry();

		return getAllActiveByPredicate((cb, root) -> {
			if (serverCountry != null) {
				Path<Object> countryUuid = root.join(Region.COUNTRY, JoinType.LEFT).get(Country.UUID);
				return CriteriaBuilderHelper.or(cb, cb.isNull(countryUuid),
						cb.equal(countryUuid, serverCountry.getUuid()));
			}

			return null;
		});
	}

	@Override
	public List<RegionDto> getAllActiveAsReferenceAndPopulationPashto(Long areaId, String campaignDt) {
		String queryStringBuilder = "select a.\"ps_af\", sum(p.population), a.id, ar.uuid as umid, a.uuid as uimn from region a\n"
				+ "left outer join populationdata p on a.id = p.region_id\n" + "left outer join areas ar on ar.id = "
				+ areaId + "\n" + "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and a.area_id = " + areaId + " and ca.uuid = '"
				+ campaignDt + "'\n" + "group by a.\"name\", a.id, ar.uuid, a.uuid";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
		List<RegionDto> resultData = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
				.map((result) -> new RegionDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						(String) result[4].toString()))
				.collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public List<RegionDto> getAllActiveAsReferenceAndPopulationDari(Long areaId, String campaignDt) {
		String queryStringBuilder = "select a.\"fa_af\", sum(p.population), a.id, ar.uuid as umid, a.uuid as uimn from region a\n"
				+ "left outer join populationdata p on a.id = p.region_id\n" + "left outer join areas ar on ar.id = "
				+ areaId + "\n" + "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and a.area_id = " + areaId + " and ca.uuid = '"
				+ campaignDt + "'\n" + "group by a.\"name\", a.id, ar.uuid, a.uuid";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
		List<RegionDto> resultData = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
				.map((result) -> new RegionDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						(String) result[4].toString()))
				.collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public List<RegionDto> getAllActiveAsReferenceAndPopulation(Long areaId, String campaignDt) {
		String queryStringBuilder = "select a.\"name\", sum(p.population), a.id, ar.uuid as umid, a.uuid as uimn from region a\n"
				+ "left outer join populationdata p on a.id = p.region_id\n" + "left outer join areas ar on ar.id = "
				+ areaId + "\n" + "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and a.area_id = " + areaId + " and ca.uuid = '"
				+ campaignDt + "'\n" + "group by a.\"name\", a.id, ar.uuid, a.uuid";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);

		List<RegionDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		// System.out.println("starting....");

		resultData.addAll(resultList.stream()
				.map((result) -> new RegionDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						(String) result[4].toString()))
				.collect(Collectors.toList()));

		// System.out.println("ending...." +resultData.size());

		// System.out.println("resultData - "+ resultData.toString());
		// //SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<RegionReferenceDto> getAllActiveByCountry(String countryUuid) {
		return getAllActiveByPredicate((cb, root) -> cb.equal(root.get(Region.COUNTRY).get(Country.UUID), countryUuid));
	}

	@Override
	public List<RegionReferenceDto> getAllActiveByArea(String areaUuid) {
		Area area = areaService.getByUuid(areaUuid);
		
		return area.getRegions().stream().filter(d -> !d.isArchived() && d.getName() != null && d.getExternalId() != null).map(RegionFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
//				getAllActiveByPredicate((cb, root) -> cb.equal(root.get(Region.AREA).get(Area.UUID), areaUuid));
	}

	@Override
	public List<RegionReferenceDto> getAllActiveByAreaPashto(String areaUuid) {
		Area area = areaService.getByUuid(areaUuid);
		return area.getRegions().stream().filter(d -> !d.isArchived() && d.getPs_af() != null && d.getExternalId() != null).map(RegionFacadeEjb::toReferenceDtoP)
				.collect(Collectors.toList());
	}

	@Override
	public List<RegionReferenceDto> getAllActiveByAreaDari(String areaUuid) {
		Area area = areaService.getByUuid(areaUuid);
		return area.getRegions().stream().filter(d -> !d.isArchived() && d.getFa_af() != null && d.getExternalId() != null).map(RegionFacadeEjb::toReferenceDtoD)
				.collect(Collectors.toList());
	}

	@Override
	public List<RegionReferenceDto> getAllActiveAsReference() {
		return service.getAllActive(Region.NAME, true).stream().map(f -> toReferenceDto(f))
				.collect(Collectors.toList());
	}

	@Override
	public List<RegionReferenceDto> getAllActiveAsReferencePashto() {
		return service.getAllActive(Region.PS_AF, true).stream().filter(r -> r.getPs_af() != null)
				.map(RegionFacadeEjb::toReferenceDtoP).collect(Collectors.toList());
	}

	@Override
	public List<RegionReferenceDto> getAllActiveAsReferenceDari() {
		return service.getAllActive(Region.FA_AF, true).stream().filter(r -> r.getFa_af() != null)
				.map(RegionFacadeEjb::toReferenceDtoD).collect(Collectors.toList());
	}

	@Override
	public List<RegionDto> getAllAfter(Date date) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RegionDto> cq = cb.createQuery(RegionDto.class);
		Root<Region> region = cq.from(Region.class);

		selectDtoFields(cq, region);

		Predicate filter = service.createChangeDateFilter(cb, region, date);

		if (filter != null) {
			cq.where(filter);
		}

		return em.createQuery(cq).getResultList();
	}

	// Need to be in the same order as in the constructor
	private void selectDtoFields(CriteriaQuery<RegionDto> cq, Root<Region> root) {

		Join<Region, Country> country = root.join(Region.COUNTRY, JoinType.LEFT);
		Join<Region, Area> area = root.join(Region.AREA, JoinType.LEFT);

		cq.multiselect(root.get(Region.CREATION_DATE), root.get(Region.CHANGE_DATE), root.get(Region.UUID),
				root.get(Region.ARCHIVED), root.get(Region.NAME), root.get(Region.EPID_CODE),
				root.get(Region.GROWTH_RATE), root.get(Region.EXTERNAL_ID), country.get(Country.UUID),
				country.get(Country.DEFAULT_NAME), country.get(Country.ISO_CODE), area.get(Area.UUID)); // AreaIndex
	}

	@Override
	public List<RegionIndexDto> getIndexList(RegionCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Region> cq = cb.createQuery(Region.class);
		Root<Region> region = cq.from(Region.class);
		Join<Region, Area> area = region.join(Region.AREA, JoinType.LEFT);
		Join<Region, Country> country = region.join(Region.COUNTRY, JoinType.LEFT);

		Predicate filter = null;
		if (criteria != null) {
			filter = service.buildCriteriaFilter(criteria, cb, region);
		}
		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case Region.NAME:
				case Region.EPID_CODE:
				case Region.GROWTH_RATE:
				case Region.EXTERNAL_ID:
					expression = region.get(sortProperty.propertyName);
					break;
				case Region.AREA:
				case RegionIndexDto.REGION_EXTERNAL_ID:
					expression = area.get(Area.NAME);
					break;

				case RegionIndexDto.COUNTRY:
					expression = country.get(Country.DEFAULT_NAME);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.asc(region.get(Region.NAME)));
		}

		cq.select(region);

		return QueryHelper.getResultList(em, cq, first, max, this::toIndexDto);
	}

	public Page<RegionIndexDto> getIndexPage(RegionCriteria regionCriteria, Integer offset, Integer size,
			List<SortProperty> sortProperties) {
		List<RegionIndexDto> regionIndexList = getIndexList(regionCriteria, offset, size, sortProperties);
		long totalElementCount = count(regionCriteria);
		return new Page<>(regionIndexList, offset, size, totalElementCount);
	}

	@Override
	public long count(RegionCriteria criteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Region> root = cq.from(Region.class);

		Predicate filter = null;

		if (criteria != null) {
			filter = service.buildCriteriaFilter(criteria, cb, root);
		}

		if (filter != null) {
			cq.where(filter);
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
	public RegionDto getByUuid(String uuid) {
		return toDto(service.getByUuid(uuid));
	}

	@Override
	public List<RegionDto> getByUuids(List<String> uuids) {
		return service.getByUuids(uuids).stream().map(this::toDto).collect(Collectors.toList());
	}

	@Override
	public RegionReferenceDto getRegionReferenceByUuid(String uuid) {
		return toReferenceDto(service.getByUuid(uuid));
	}

	@Override
	public RegionReferenceDto getRegionReferenceById(int id) {
		return toReferenceDto(service.getById(id));
	}

	@Override
	public List<String> getNamesByIds(List<Long> regionIds) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Region> root = cq.from(Region.class);

		Predicate filter = root.get(Region.ID).in(regionIds);
		cq.where(filter);
		cq.select(root.get(Region.NAME));
		return em.createQuery(cq).getResultList();
	}

	@Override
	public boolean isUsedInOtherInfrastructureData(Collection<String> regionUuids) {

		return service.isUsedInInfrastructureData(regionUuids, District.REGION, District.class)
				|| service.isUsedInInfrastructureData(regionUuids, Facility.REGION, Facility.class)
				|| service.isUsedInInfrastructureData(regionUuids, PointOfEntry.REGION, PointOfEntry.class);
	}

	public static RegionReferenceDto toReferenceDto(Region entity) {

		if (entity == null) {
			return null;
		}
		return new RegionReferenceDto(entity.getUuid(), entity.toString(), entity.getExternalId());
	}

	public static RegionReferenceDto toReferenceDtoP(Region entity) {

		if (entity == null) {
			return null;
		}
		return new RegionReferenceDto(entity.getUuid(), entity.getPs_af());
	}

	public static RegionReferenceDto toReferenceDtoD(Region entity) {

		if (entity == null) {
			return null;
		}
		return new RegionReferenceDto(entity.getUuid(), entity.getPs_af());
	}

	public RegionDto toDto(Region entity) {

		if (entity == null) {
			return null;
		}
		RegionDto dto = new RegionDto();
		DtoHelper.fillDto(dto, entity);

		dto.setName(entity.getName());
		dto.setPs_af(entity.getPs_af());
		dto.setFa_af(entity.getFa_af());
		dto.setEpidCode(entity.getEpidCode());
		dto.setGrowthRate(entity.getGrowthRate());
		dto.setArchived(entity.isArchived());
		dto.setExternalId(entity.getExternalId());
		dto.setArea(AreaFacadeEjb.toReferenceDto(entity.getArea()));
		dto.setCountry(CountryFacadeEjb.toReferenceDto(entity.getCountry()));

		return dto;
	}

	public RegionIndexDto toIndexDto(Region entity) {

		if (entity == null) {
			return null;
		}
		RegionIndexDto dto = new RegionIndexDto();
		DtoHelper.fillDto(dto, entity);

		dto.setName(entity.getName());
		dto.setFa_af(entity.getFa_af());
		dto.setPs_af(entity.getPs_af());
		dto.setEpidCode(entity.getEpidCode());
		// dto.setPopulation(populationDataFacade.getRegionPopulation(dto.getUuid()));
		dto.setGrowthRate(entity.getGrowthRate());
		dto.setExternalId(entity.getExternalId());
		if (entity.getArea() != null) {
			dto.setAreaexternalId(entity.getArea().getExternalId());
		}

		dto.setArea(AreaFacadeEjb.toReferenceDtox(entity.getArea()));
//		dto.setRegionexternalId(entity.getRegion().getExternalId());

//		System.out.println(entity.getArea().getExternalId() + "aread external id froIndex dto ");
//		dto.setAreaexternalId(entity.getArea().getExternalId());
		dto.setArchived(entity.isArchived());
		dto.setCountry(CountryFacadeEjb.toReferenceDto(entity.getCountry()));

		return dto;
	}

	@Override
	public RegionDto save(@Valid RegionDto dto) throws ValidationRuntimeException {
		return save(dto, false);
	}

	@Override
	public RegionDto save(@Valid RegionDto dto, boolean allowMerge) throws ValidationRuntimeException {
		checkInfraDataLocked();

		Region region = service.getByUuid(dto.getUuid());

		if (region == null) {
			List<Region> duplicates = service.getByName(dto.getName(), true);
			if (!duplicates.isEmpty()) {
				if (allowMerge) {
					region = duplicates.get(0);
					RegionDto dtoToMerge = getByUuid(region.getUuid());
					dto = DtoHelper.copyDtoValues(dtoToMerge, dto, true);
				} else {
					throw new ValidationRuntimeException(
							I18nProperties.getValidationError(Validations.importRegionAlreadyExists));
				}
			}
		}

		region = fillOrBuildEntity(dto, region, true);
		service.ensurePersisted(region);
		return toDto(region);
	}

	@Override
	public List<RegionReferenceDto> getReferencesByName(String name, boolean includeArchivedEntities) {
		return service.getByName(name, includeArchivedEntities).stream().map(RegionFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	public List<RegionDto> getByName(String name, boolean includeArchivedEntities) {
		return service.getByName(name, includeArchivedEntities).stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<RegionDto> getByExternalId(Long ext_id, boolean includeArchivedEntities, int notUsed) {
		return service.getByExternalId(ext_id, includeArchivedEntities, 0).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<RegionReferenceDto> getByExternalId(Long externalId, boolean includeArchivedEntities) {
		return service.getByExternalId(externalId, includeArchivedEntities).stream()
				.map(RegionFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

	private List<RegionReferenceDto> getAllActiveByPredicate(
			BiFunction<CriteriaBuilder, Root<Region>, Predicate> buildPredicate) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Region> cq = cb.createQuery(Region.class);
		Root<Region> root = cq.from(Region.class);

//		System.out.println("ololllllllllllllllllllllllllllllllllllllllllll " + SQLExtractor.from(em.createQuery(cq)));
		Predicate basicFilter = service.createBasicFilter(cb, root);
		cq.where(CriteriaBuilderHelper.and(cb, basicFilter, buildPredicate.apply(cb, root)));

		cq.orderBy(cb.asc(root.get(Region.NAME)));

		return em.createQuery(cq).getResultList().stream().map(RegionFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	private List<RegionReferenceDto> getAllActiveByPredicatePashto(
			BiFunction<CriteriaBuilder, Root<Region>, Predicate> buildPredicate) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Region> cq = cb.createQuery(Region.class);
		Root<Region> root = cq.from(Region.class);

		Predicate basicFilter = service.createBasicFilter(cb, root);
		cq.where(CriteriaBuilderHelper.and(cb, basicFilter, buildPredicate.apply(cb, root)));

		cq.orderBy(cb.asc(root.get(Region.PS_AF)));

//		System.out.println("ololllllllllllllllllllllllllllllllllllllllllll " + SQLExtractor.from(em.createQuery(cq)));
		return em.createQuery(cq).getResultList().stream().map(RegionFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	private Region fillOrBuildEntity(@NotNull RegionDto source, Region target, boolean checkChangeDate) {

		target = DtoHelper.fillOrBuildEntity(source, target, Region::new, checkChangeDate);

		target.setName(source.getName());
		target.setEpidCode(source.getEpidCode());
		target.setGrowthRate(source.getGrowthRate());
		target.setArchived(source.isArchived());
		target.setExternalId(source.getExternalId());
		target.setArea(areaService.getByReferenceDto(source.getArea()));
		target.setCountry(countryService.getByReferenceDto(source.getCountry()));

		return target;
	}

	@LocalBean
	@Stateless
	public static class RegionFacadeEjbLocal extends RegionFacadeEjb {

		public RegionFacadeEjbLocal() {
		}

		@Inject
		protected RegionFacadeEjbLocal(RegionService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
			super(service, featureConfiguration);
		}
	}

	public static Set<RegionReferenceDto> toReferenceDto(HashSet<Region> regions) {
		Set<RegionReferenceDto> dtos = new HashSet<RegionReferenceDto>();
		for (Region region : regions) {
			RegionReferenceDto regionDto = new RegionReferenceDto(region.getUuid(), region.toString(),
					region.getExternalId());
			dtos.add(regionDto);
		}
		return dtos;
	}

	@Override
	public List<RegionIndexDto> getAllRegions() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Region> cq = cb.createQuery(Region.class);
		Root<Region> region = cq.from(Region.class);
		Join<Region, Area> area = region.join(Region.AREA, JoinType.LEFT);

		Predicate filter = cb.equal(region.get(Region.ARCHIVED), false);
		cq.where(filter);
		cq.select(region);
		List<Region> regions = em.createQuery(cq).getResultList();
		List<RegionIndexDto> dtos = new ArrayList();

		for (Region reg : regions) {
			if (!reg.equals(null)) {
				dtos.add(this.toIndexDto(reg));
			}
		}
		return dtos;
	}

	@Override
	public List<RegionReferenceDto> getAllActiveByAreaAndSelectedInCampaign(String areaUuid, String campaignUuid) {
		String selectBuilder = "select distinct r.uuid, r.\"name\", r.externalid\r\n" + "from region r\r\n"
				+ "inner join areas a on a.id = r.area_id\r\n" + "inner join populationdata p on r.id = p.region_id\r\n"
				+ "inner join campaigns c on p.campaign_id = c.id\r\n" + "where c.uuid = '" + campaignUuid
				+ "' and p.selected = true and r.archived = false and a.uuid = '" + areaUuid + "' ;";

		Query seriesDataQuery = em.createNativeQuery(selectBuilder);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		List<RegionReferenceDto> resultData = new ArrayList<>();
		resultData.addAll(resultList.stream().map((result) -> new RegionReferenceDto((String) result[0],
				(String) result[1], ((BigInteger) result[2]).longValue()

		)).collect(Collectors.toList()));

		return resultData;
	}

	@Override
	public String getMapDiagramRegionHascByUuid(String regionUuid) {

		String selectBuilder = "select d.hasc from region d where d.uuid = '" + regionUuid + "';";

		Query seriesDataQuery = em.createNativeQuery(selectBuilder);
		try {
			return seriesDataQuery.getSingleResult().toString();
		} catch (NoResultException e) {
			return "";
		}
	}
	
	@Override
	public List<RegionHistoryExtractDto> getProvincesHistory(String uuid){
		List<RegionHistoryExtractDto> resultData = new ArrayList<>();
		
		StringBuilder queryStringBuilder = new StringBuilder();
		queryStringBuilder.append("WITH current_data AS (")
		                  .append("SELECT id, uuid, archived, externalid, name, changedate AS start_date, ")
		                  .append("LEAD(changedate) OVER (PARTITION BY uuid ORDER BY changedate) AS end_date ")
		                  .append("FROM region_history ");

		if (uuid != null) {
		    queryStringBuilder.append("WHERE uuid = '").append(uuid).append("' ");
		}

		queryStringBuilder.append("), ")
		                  .append("updated_end_date AS (")
		                  .append("SELECT cd.id, cd.uuid, cd.archived, cd.externalid, cd.name, cd.start_date, ")
		                  .append("COALESCE(cd.end_date, (SELECT changedate FROM region WHERE region.uuid = cd.uuid)) AS end_date ")
		                  .append("FROM current_data cd) ")
		                  .append("SELECT uuid, name, archived, externalid,  start_date, end_date ")
		                  .append("FROM updated_end_date ")
		                  .append("ORDER BY start_date ASC;");
		
		String queryString = queryStringBuilder.toString();
		
		Query seriesDataQuery = em.createNativeQuery(queryString);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
			    .map(result -> new RegionHistoryExtractDto(
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
	
	public static Set<RegionReferenceDto> toReferenceDto(Set<Region> region) { // save

		Set<RegionReferenceDto> dtos = new HashSet<RegionReferenceDto>();
		for (Region com : region) {
			RegionReferenceDto dto = new RegionReferenceDto(com.getUuid(), com.toString(), com.getExternalId());
			dtos.add(dto);
		}

		return dtos;
	}
}