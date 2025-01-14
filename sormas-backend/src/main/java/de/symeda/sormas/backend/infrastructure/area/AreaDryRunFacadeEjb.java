package de.symeda.sormas.backend.infrastructure.area;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDryRunDto;
import de.symeda.sormas.api.infrastructure.area.AreaDryRunFacade;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaDryRunDto;
import de.symeda.sormas.api.infrastructure.area.AreaFacade;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.AbstractInfrastructureEjb;
import de.symeda.sormas.backend.infrastructure.ConfigurationChangeLog;
import de.symeda.sormas.backend.infrastructure.ConfigurationChangeLogService;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionService;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "AreaDryRunFacade")
public class AreaDryRunFacadeEjb extends AbstractInfrastructureEjb<AreaDryRun, AreaDryRunService> implements AreaDryRunFacade {

	@EJB
	private RegionService regionService;

	@EJB
	private AreaService areaService;
	
	@EJB
	private AreaDryRunService areaDryRunService;

	@EJB
	private ConfigurationChangeLogService configurationChangeLogService;

	@EJB
	private UserFacadeEjb.UserFacadeEjbLocal userServiceEjb;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	public AreaDryRunFacadeEjb() {
	}

	@Inject
	protected AreaDryRunFacadeEjb(AreaDryRunService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
		super(service, featureConfiguration);
	}

	@Override
	public List<AreaReferenceDto> getAllActiveAndSelectedAsReference(String campaignUuid) {

		String selectBuilder = "select distinct a.uuid, a.\"name\", a.externalid\r\n" + "from areas a\r\n"
				+ "inner join region r on a.id = r.area_id\r\n"
				+ "inner join populationdata p on r.id = p.region_id\r\n"
				+ "inner join campaigns c on p.campaign_id = c.id\r\n" + "where c.uuid = '" + campaignUuid
				+ "' and p.selected = true and a.archived = false;";

		Query seriesDataQuery = em.createNativeQuery(selectBuilder);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

		List<AreaReferenceDto> resultData = new ArrayList<>();
		resultData.addAll(resultList.stream().map((result) -> new AreaReferenceDto((String) result[0],
				(String) result[1], ((BigInteger) result[2]).longValue()

		)).collect(Collectors.toList()));

		return resultData;
		//

		// return service.getAllActive(Area.NAME,
		// true).stream().map(AreaFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}

//	@Override
//	public List<AreaReferenceDto> getAllActiveAsReference() {
//		return service.getAllActive(AreaDryRun.NAME, true).stream().map(AreaDryRunFacadeEjb::toReferenceDto)
//				.collect(Collectors.toList());
//	}
//
//	@Override
//	public List<AreaReferenceDto> getAllActiveAsReferencePashto() {
//		return service.getAllActive(Area.PS_AF, true).stream().filter(area -> area.getPs_af() != null)
//				.map(AreaDryRunFacadeEjb::toReferenceDtoP).collect(Collectors.toList());
//	}
//
//	@Override
//	public List<AreaReferenceDto> getAllActiveAsReferenceDari() {
//		return service.getAllActive(Area.FA_AF, true).stream().filter(area -> area.getFa_af() != null)
//				.map(AreaDryRunFacadeEjb::toReferenceDtoD).collect(Collectors.toList());
//	}
//
	@Override
	public AreaDryRunDto getByUuid(String uuid) {
		return toDto(service.getByUuid(uuid));
	}

//	@Override
//	public AreaDryRunDto getByUuid(String uuid) {
//		return toDto(service.getByUuid(uuid));
//	}

//	@Override
//	public List<AreaDryRunDto> getIndexList(AreaCriteria criteria, Integer first, Integer max,
//			List<SortProperty> sortProperties) {
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//		CriteriaQuery<AreaDryRun> cq = cb.createQuery(Area.class);
//		Root<AreaDryRun> areaRoot = cq.from(Area.class);
//
//		Predicate filter = service.buildCriteriaFilter(criteria, cb, areaRoot);
//		if (filter != null) {
//			cq.where(filter);
//		}
//
//		if (sortProperties != null && sortProperties.size() > 0) {
//			List<Order> order = new ArrayList<>(sortProperties.size());
//			for (SortProperty sortProperty : sortProperties) {
//				Expression<?> expression;
//				switch (sortProperty.propertyName) {
//				case Area.NAME:
//				case Area.EXTERNAL_ID:
//					expression = areaRoot.get(sortProperty.propertyName);
//					break;
//				default:
//					throw new IllegalArgumentException(sortProperty.propertyName);
//				}
//				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
//			}
//			cq.orderBy(order);
//		} else {
//			cq.orderBy(cb.asc(areaRoot.get(Area.NAME)));
//		}
//
//		cq.select(areaRoot);
//
//		return QueryHelper.getResultList(em, cq, first, max, this::toDto);
//	}
//
//	@Override
//	public long count(AreaCriteria criteria) {
//		CriteriaBuilder cb = em.getCriteriaBuilder();
//		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
//		Root<AreaDryRun> areaRoot = cq.from(Area.class);
//
//		Predicate filter = service.buildCriteriaFilter(criteria, cb, areaRoot);
//		if (filter != null) {
//			cq.where(filter);
//		}
//
//		cq.select(cb.count(areaRoot));
//		return em.createQuery(cq).getSingleResult();
//	}

	@Override
	public AreaDryRunDto save(@Valid AreaDryRunDto dto) {
		return save(dto, false);
	}

	@Override
	public AreaDryRunDto save(@Valid AreaDryRunDto dto, boolean allowMerge) {
		checkInfraDataLocked();
		AreaDryRun area = service.getByUuid(dto.getUuid());

		if (area == null) {
			List<AreaDryRun> duplicates = service.getByName(dto.getName(), true);
			if (!duplicates.isEmpty()) {
				if (allowMerge) {
					area = duplicates.get(0);
					AreaDryRunDto dtoToMerge = getByUuid(area.getUuid());
					dto = DtoHelper.copyDtoValues(dtoToMerge, dto, true);
				} else {
					throw new ValidationRuntimeException(
							I18nProperties.getValidationError(Validations.importAreaAlreadyExists));
				}
			}
		}

		area = fromDto(dto, area, true);
		service.ensurePersisted(area);
		return toDto(area);
	}
//
//	@Override
//	public boolean isUsedInOtherInfrastructureData(Collection<String> areaUuids) {
//		return service.isUsedInInfrastructureData(areaUuids, Region.AREA, Region.class);
//	}
//
	@Override
	public List<AreaReferenceDto> getByName(String name, boolean includeArchived) {
		return service.getByName(name, includeArchived).stream().map(AreaDryRunFacadeEjb::toReferenceDto).collect(Collectors.toList());// .getByName(name, includeArchived).stream().map(AreaDryRunFacadeEjb::toReferenceDto).collect(Collectors.toList());
	}
//
//	@Override
//	public List<AreaReferenceDto> getByExternalID(Long ext_id, boolean includeArchived) {
//		return service.getByExternalID(ext_id, includeArchived).stream().map(AreaDryRunFacadeEjb::toReferenceDto)
//				.collect(Collectors.toList());
//	}

//	@Override
//	public List<AreaDryRunDto> getAllAfter(Date date) {
//		return service.getAll((cb, root) -> service.createChangeDateFilter(cb, root, date)).stream().map(this::toDto)
//				.collect(Collectors.toList());
//	}
//
//	@Override
//	public List<AreaDryRunDto> getByUuids(List<String> uuids) {
//		return service.getByUuids(uuids).stream().map(this::toDto).collect(Collectors.toList());
//	}
//
//	@Override
//	public List<String> getAllUuids() {
//		return service.getAllUuids();
//	}

	public AreaDryRun fromDto(@NotNull AreaDryRunDto source, AreaDryRun target, boolean checkChangeDate) {
		target = DtoHelper.fillOrBuildEntity(source, target, AreaDryRun::new, checkChangeDate);

		target.setName(source.getName());
		target.setExternalId(source.getExternalId());
		target.setArchived(source.isArchived());
		target.setDryrun(source.isDryRun());

		return target;
	}

	public ConfigurationChangeLog fromDto(@NotNull ConfigurationChangeLogDto configurationChangeLogDtos) {

		ConfigurationChangeLog configurationChangeLog = new ConfigurationChangeLog();
		configurationChangeLog.setAction_logged(configurationChangeLogDtos.getAction_logged());
		configurationChangeLog.setAction_unit_name(configurationChangeLogDtos.getAction_unit_name());
		configurationChangeLog.setAction_unit_type(configurationChangeLogDtos.getAction_unit_type());
		configurationChangeLog.setCreatinguser(configurationChangeLogDtos.getCreatinguser());
		configurationChangeLog.setUnit_code(configurationChangeLogDtos.getUnit_code());
		configurationChangeLog.setAction_date(configurationChangeLogDtos.getAction_date());

		return configurationChangeLog;
	}

	@Override
	public List<AreaReferenceDto> getByExternalId(Long externalId, boolean includeArchivedEntities) {

		return service.getByExternalId(externalId, includeArchivedEntities).stream().map(AreaDryRunFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	public AreaDryRunDto toDto(AreaDryRun source) {
		if (source == null) {
			return null;
		}
		AreaDryRunDto target = new AreaDryRunDto();
		DtoHelper.fillDto(target, source);

		target.setName(source.getName());
		target.setFa_af(source.getFa_af());
		target.setPs_af(source.getPs_af());
		target.setExternalId(source.getExternalId());
		target.setArchived(source.isArchived());
		target.setDryRun(source.isDryrun());

		return target;
	}

	public static AreaReferenceDto toReferenceDto(AreaDryRun entity) {
		if (entity == null) {
			return null;
		}
		return new AreaReferenceDto(entity.getUuid(), entity.toString());
	}

	public static AreaReferenceDto toReferenceDtox(Area entity) {
		if (entity == null) {
			return null;
		}
		return new AreaReferenceDto(entity.getUuid(), entity.toString(), entity.getExternalId());
	}

	public static AreaReferenceDto toReferenceDtoP(Area entity) {
		if (entity == null) {
			return null;
		}
		return new AreaReferenceDto(entity.getUuid(), entity.getPs_af());
	}

	public static AreaReferenceDto toReferenceDtoD(Area entity) {
		if (entity == null) {
			return null;
		}
		return new AreaReferenceDto(entity.getUuid(), entity.getFa_af());
	}

	@Override
	public List<AreaReferenceDto> getReferencesByName(String name, boolean includeArchived) {
		return getByName(name, includeArchived);
	}

	@LocalBean
	@Stateless
	public static class AreaDryRunFacadeEjbLocal extends AreaDryRunFacadeEjb {

		public AreaDryRunFacadeEjbLocal() {
		}

		@Inject
		protected AreaDryRunFacadeEjbLocal(AreaDryRunService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
			super(service, featureConfiguration);
		}
	}

	public static Set<AreaReferenceDto> toReferenceDto(HashSet<AreaDryRun> areas) {
		Set<AreaReferenceDto> dtos = new HashSet<AreaReferenceDto>();
		for (AreaDryRun area : areas) {
			AreaReferenceDto AreaDryRunDto = new AreaReferenceDto(area.getUuid(), area.toString(), area.getExternalId());
			dtos.add(AreaDryRunDto);
		}

		return dtos;
	}

	@Override
	public AreaReferenceDto getAreaReferenceByUuid(String uuid) {
		return toReferenceDto(areaDryRunService.getByUuid(uuid));
	}

//	@Override
//	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulation() {
//
//		String queryStringBuilder = "select a.\"name\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
//				+ "left outer join region r on r.area_id = a.id\n"
//				+ "left outer join populationdata p on r.id = p.region_id\r\n"
//				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10')\n"
//				+ "group by a.\"name\", a.id, a.uuid ";
//
//		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
//
//		List<AreaDryRunDto> resultData = new ArrayList<>();
//
//		@SuppressWarnings("unchecked")
//		List<Object[]> resultList = seriesDataQuery.getResultList();
//
////		System.out.println("starting....");
//
//		resultData.addAll(resultList.stream()
//				.map((result) -> new AreaDryRunDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
//						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
//						((BigInteger) result[4]).longValue()))
//				.collect(Collectors.toList()));
//
////		System.out.println("ending...." +resultData.size());
//
//		// System.out.println("resultData - "+ resultData.toString());
//		// //SQLExtractor.from(seriesDataQuery));
//		return resultData;
//	}
//
//	@Override
//	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulation(CampaignDto campaignDt) {
//		List<AreaDryRunDto> resultData = new ArrayList<>();
//		String queryStringBuilder = "select a.\"name\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
//				+ "left outer join region r on r.area_id = a.id\n"
//				+ "left outer join populationdata p on r.id = p.region_id\n"
//				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
//				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and ca.uuid = '"
//				+ campaignDt.getUuid() + "'\n" + "group by a.\"name\", a.id, a.uuid ";
//
////			System.out.println(queryStringBuilder + "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
//		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
//
//		@SuppressWarnings("unchecked")
//		List<Object[]> resultList = seriesDataQuery.getResultList();
//		resultData.addAll(resultList.stream()
//				.map((result) -> new AreaDryRunDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
//						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
//						((BigInteger) result[4]).longValue()))
//				.collect(Collectors.toList()));
//
//		return resultData;
//
//	}
//
//	@Override
//	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulationPashto(CampaignDto campaignDt) {
//		List<AreaDryRunDto> resultData = new ArrayList<>();
//		String queryStringBuilder = "select a.\"ps_af\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
//				+ "left outer join region r on r.area_id = a.id\n"
//				+ "left outer join populationdata p on r.id = p.region_id\n"
//				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
//				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and ca.uuid = '"
//				+ campaignDt.getUuid() + "'\n" + "group by a.\"name\", a.id, a.uuid ";
//
//		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
//
//		@SuppressWarnings("unchecked")
//		List<Object[]> resultList = seriesDataQuery.getResultList();
//		resultData.addAll(resultList.stream()
//				.map((result) -> new AreaDryRunDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
//						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
//						((BigInteger) result[4]).longValue()))
//				.collect(Collectors.toList()));
//
//		return resultData;
//
//	}
//
//	@Override
//	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulationsDari(CampaignDto campaignDt) {
//		List<AreaDryRunDto> resultData = new ArrayList<>();
//		String queryStringBuilder = "select a.\"fa_af\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
//				+ "left outer join region r on r.area_id = a.id\n"
//				+ "left outer join populationdata p on r.id = p.region_id\n"
//				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
//				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and ca.uuid = '"
//				+ campaignDt.getUuid() + "'\n" + "group by a.\"name\", a.id, a.uuid ";
//
//		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);
//
//		@SuppressWarnings("unchecked")
//		List<Object[]> resultList = seriesDataQuery.getResultList();
//		resultData.addAll(resultList.stream()
//				.map((result) -> new AreaDryRunDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
//						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
//						((BigInteger) result[4]).longValue()))
//				.collect(Collectors.toList()));
//
//		return resultData;
//
//	}

	public ConfigurationChangeLogDto saveAreaChangeLog(ConfigurationChangeLogDto configurationChangeLogDto) {
		ConfigurationChangeLog userActivitySummary = fromDto(configurationChangeLogDto);
		configurationChangeLogService.ensurePersisted(userActivitySummary);
		return toLogDto(userActivitySummary);
	}

	public ConfigurationChangeLogDto toLogDto(ConfigurationChangeLog source) {

		if (source == null) {
			return null;
		}
		ConfigurationChangeLogDto target = new ConfigurationChangeLogDto();

//		target.setCreatingUser(userServiceEBJ.getByUuid(source.getCreatingUser().getUuid()));
//		target.setAction(source.getAction());
//		target.setActionModule(source.getActionModule());
		return target;
	}

	public static Set<AreaReferenceDto> toReferenceDto(Set<AreaDryRun> area) { // save

		Set<AreaReferenceDto> dtos = new HashSet<AreaReferenceDto>();
		for (AreaDryRun com : area) {
			AreaReferenceDto dto = new AreaReferenceDto(com.getUuid(), com.toString(), com.getExternalId());
			dtos.add(dto);
		}

		return dtos;
	}


//	@Override
//	public List<AreaReferenceDto> getByExternalId(Long externalId, boolean includeArchivedEntities) {
//		// TODO Auto-generated method stub
//		return null;
//	}



	@Override
	public long count(AreaCriteria criteria) {
		// TODO Auto-generated method stub
		return 0;
	}


	



	@Override
	public List<String> getAllUuids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaReferenceDto> getAllActiveAsReference() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaReferenceDto> getAllActiveAsReferencePashto() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaReferenceDto> getAllActiveAsReferenceDari() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsedInOtherInfrastructureData(Collection<String> areaUuids) {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public List<AreaReferenceDto> getByName(String name, boolean includeArchived) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public List<AreaReferenceDto> getByExternalID(Long ext_id, boolean includeArchived) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getIndexList(AreaCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getAllAfter(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getByUuids(List<String> uuids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulation(CampaignDto campaignDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulationPashto(CampaignDto campaignDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulationsDari(CampaignDto campaignDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AreaDryRunDto> getAllActiveAsReferenceAndPopulation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearDryRunTable() {
		// TODO Auto-generated method stub
		
	    String truncateQuery = "TRUNCATE TABLE areas_dryrun";

	    // Create a native query
	    Query query = em.createNativeQuery(truncateQuery);

	    // Execute the query
	    query.executeUpdate();
		
	}
}
