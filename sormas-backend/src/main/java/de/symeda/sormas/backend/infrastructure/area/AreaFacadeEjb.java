package de.symeda.sormas.backend.infrastructure.area;

import java.math.BigInteger;
import java.sql.Timestamp;
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

import org.joda.time.LocalDateTime;

import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaFacade;
import de.symeda.sormas.api.infrastructure.area.AreaHistoryExtractDto;
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

@Stateless(name = "AreaFacade")
public class AreaFacadeEjb extends AbstractInfrastructureEjb<Area, AreaService> implements AreaFacade {

	@EJB
	private RegionService regionService;

	@EJB
	private AreaService areaService;

	@EJB
	private ConfigurationChangeLogService configurationChangeLogService;

	@EJB
	private UserFacadeEjb.UserFacadeEjbLocal userServiceEjb;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	public AreaFacadeEjb() {
	}

	@Inject
	protected AreaFacadeEjb(AreaService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
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

	@Override
	public List<AreaReferenceDto> getAllActiveAsReference() {
		return service.getAllActive(Area.NAME, true).stream().map(AreaFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<AreaReferenceDto> getAllActiveAsReferencePashto() {
		return service.getAllActive(Area.PS_AF, true).stream().filter(area -> area.getPs_af() != null)
				.map(AreaFacadeEjb::toReferenceDtoP).collect(Collectors.toList());
	}

	@Override
	public List<AreaReferenceDto> getAllActiveAsReferenceDari() {
		return service.getAllActive(Area.FA_AF, true).stream().filter(area -> area.getFa_af() != null)
				.map(AreaFacadeEjb::toReferenceDtoD).collect(Collectors.toList());
	}

	@Override
	public AreaDto getByUuid(String uuid) {
		return toDto(service.getByUuid(uuid));
	}

//	@Override
//	public AreaDto getByUuid(String uuid) {
//		return toDto(service.getByUuid(uuid));
//	}

	@Override
	public List<AreaDto> getIndexList(AreaCriteria criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Area> cq = cb.createQuery(Area.class);
		Root<Area> areaRoot = cq.from(Area.class);

		Predicate filter = service.buildCriteriaFilter(criteria, cb, areaRoot);
		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case Area.NAME:
				case Area.EXTERNAL_ID:
					expression = areaRoot.get(sortProperty.propertyName);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.asc(areaRoot.get(Area.NAME)));
		}

		cq.select(areaRoot);

		return QueryHelper.getResultList(em, cq, first, max, this::toDto);
	}

	@Override
	public long count(AreaCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Area> areaRoot = cq.from(Area.class);

		Predicate filter = service.buildCriteriaFilter(criteria, cb, areaRoot);
		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.count(areaRoot));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public AreaDto save(@Valid AreaDto dto) {
		return save(dto, false);
	}

	@Override
	public AreaDto save(@Valid AreaDto dto, boolean allowMerge) {
		checkInfraDataLocked();
		Area area = service.getByUuid(dto.getUuid());

		if (area == null) {
			List<Area> duplicates = service.getByName(dto.getName(), true);
			if (!duplicates.isEmpty()) {
				if (allowMerge) {
					area = duplicates.get(0);
					AreaDto dtoToMerge = getByUuid(area.getUuid());
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

	@Override
	public boolean isUsedInOtherInfrastructureData(Collection<String> areaUuids) {
		return service.isUsedInInfrastructureData(areaUuids, Region.AREA, Region.class);
	}

	@Override
	public List<AreaReferenceDto> getByName(String name, boolean includeArchived) {
		return service.getByName(name, includeArchived).stream().map(AreaFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<AreaReferenceDto> getByExternalID(Long ext_id, boolean includeArchived) {
		return service.getByExternalID(ext_id, includeArchived).stream().map(AreaFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<AreaDto> getAllAfter(Date date) {
		return service.getAll((cb, root) -> service.createChangeDateFilter(cb, root, date)).stream().map(this::toDto)
				.collect(Collectors.toList());
	}

	@Override
	public List<AreaDto> getByUuids(List<String> uuids) {
		return service.getByUuids(uuids).stream().map(this::toDto).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllUuids() {
		return service.getAllUuids();
	}

	public Area fromDto(@NotNull AreaDto source, Area target, boolean checkChangeDate) {
		target = DtoHelper.fillOrBuildEntity(source, target, Area::new, checkChangeDate);

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

		return service.getByExternalId(externalId, includeArchivedEntities).stream().map(AreaFacadeEjb::toReferenceDto)
				.collect(Collectors.toList());
	}

	public AreaDto toDto(Area source) {
		if (source == null) {
			return null;
		}
		AreaDto target = new AreaDto();
		DtoHelper.fillDto(target, source);

		target.setName(source.getName());
		target.setFa_af(source.getFa_af());
		target.setPs_af(source.getPs_af());
		target.setExternalId(source.getExternalId());
		target.setArchived(source.isArchived());
		target.setDryRun(source.isDryrun());

		return target;
	}

	public static AreaReferenceDto toReferenceDto(Area entity) {
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
	public static class AreaFacadeEjbLocal extends AreaFacadeEjb {

		public AreaFacadeEjbLocal() {
		}

		@Inject
		protected AreaFacadeEjbLocal(AreaService service, FeatureConfigurationFacadeEjbLocal featureConfiguration) {
			super(service, featureConfiguration);
		}
	}

	public static Set<AreaReferenceDto> toReferenceDto(HashSet<Area> areas) {
		Set<AreaReferenceDto> dtos = new HashSet<AreaReferenceDto>();
		for (Area area : areas) {
			AreaReferenceDto areaDto = new AreaReferenceDto(area.getUuid(), area.toString(), area.getExternalId());
			dtos.add(areaDto);
		}

		return dtos;
	}

	@Override
	public AreaReferenceDto getAreaReferenceByUuid(String uuid) {
		return toReferenceDto(areaService.getByUuid(uuid));
	}

	@Override
	public List<AreaDto> getAllActiveAsReferenceAndPopulation() {

		String queryStringBuilder = "select a.\"name\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
				+ "left outer join region r on r.area_id = a.id\n"
				+ "left outer join populationdata p on r.id = p.region_id\r\n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10')\n"
				+ "group by a.\"name\", a.id, a.uuid ";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);

		List<AreaDto> resultData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();

//		System.out.println("starting....");

		resultData.addAll(resultList.stream()
				.map((result) -> new AreaDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						((BigInteger) result[4]).longValue()))
				.collect(Collectors.toList()));

//		System.out.println("ending...." +resultData.size());

		// System.out.println("resultData - "+ resultData.toString());
		// //SQLExtractor.from(seriesDataQuery));
		return resultData;
	}

	@Override
	public List<AreaDto> getAllActiveAsReferenceAndPopulation(CampaignDto campaignDt) {
		List<AreaDto> resultData = new ArrayList<>();
		String queryStringBuilder = "select a.\"name\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
				+ "left outer join region r on r.area_id = a.id\n"
				+ "left outer join populationdata p on r.id = p.region_id\n"
				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and ca.uuid = '"
				+ campaignDt.getUuid() + "'\n" + "group by a.\"name\", a.id, a.uuid ";

//			System.out.println(queryStringBuilder + "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");
		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
				.map((result) -> new AreaDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						((BigInteger) result[4]).longValue()))
				.collect(Collectors.toList()));

		return resultData;

	}

	@Override
	public List<AreaDto> getAllActiveAsReferenceAndPopulationPashto(CampaignDto campaignDt) {
		List<AreaDto> resultData = new ArrayList<>();
		String queryStringBuilder = "select a.\"ps_af\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
				+ "left outer join region r on r.area_id = a.id\n"
				+ "left outer join populationdata p on r.id = p.region_id\n"
				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and ca.uuid = '"
				+ campaignDt.getUuid() + "'\n" + "group by a.\"name\", a.id, a.uuid ";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
				.map((result) -> new AreaDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						((BigInteger) result[4]).longValue()))
				.collect(Collectors.toList()));

		return resultData;

	}

	@Override
	public List<AreaDto> getAllActiveAsReferenceAndPopulationsDari(CampaignDto campaignDt) {
		List<AreaDto> resultData = new ArrayList<>();
		String queryStringBuilder = "select a.\"fa_af\", sum(p.population), a.id, a.uuid as mdis, a.externalid as exter  from areas a \n"
				+ "left outer join region r on r.area_id = a.id\n"
				+ "left outer join populationdata p on r.id = p.region_id\n"
				+ "left outer join campaigns ca on p.campaign_id = ca.id \n"
				+ "where a.archived = false and (p.agegroup = 'AGE_0_4' or p.agegroup = 'AGE_5_10') and ca.uuid = '"
				+ campaignDt.getUuid() + "'\n" + "group by a.\"name\", a.id, a.uuid ";

		Query seriesDataQuery = em.createNativeQuery(queryStringBuilder);

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
				.map((result) -> new AreaDto((String) result[0].toString(), ((BigInteger) result[1]).longValue(),
						((BigInteger) result[2]).longValue(), (String) result[3].toString(),
						((BigInteger) result[4]).longValue()))
				.collect(Collectors.toList()));

		return resultData;

	}

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

	public static Set<AreaReferenceDto> toReferenceDto(Set<Area> area) { // save

		Set<AreaReferenceDto> dtos = new HashSet<AreaReferenceDto>();
		for (Area com : area) {
			AreaReferenceDto dto = new AreaReferenceDto(com.getUuid(), com.toString(), com.getExternalId());
			dtos.add(dto);
		}

		return dtos;
	}

	@Override
	public List<AreaHistoryExtractDto> getAreasHistory(String external){
		
		List<AreaHistoryExtractDto> resultData = new ArrayList<>();
		
//		if(external != null) {
//			String whereclause = "WHERE  uuid = ' " + external + "'";
//		}
//		String queryStringBuilder = "WITH current_data AS (select id, uuid, archived, externalid, name, changedate AS start_date, \n"
//				+ "LEAD(changedate) OVER (PARTITION BY uuid ORDER BY changedate) AS end_date FROM areas_history \n"
//				+ (external != null ? "WHERE uuid = ? " : "")
//				+ "),"
//				+ "updated_end_date AS (SELECT  cd.id,  cd.uuid, cd.archived, cd.externalid,  cd.name, cd.start_date, \n"
//				+ "COALESCE(cd.end_date, (SELECT changedate FROM areas WHERE areas.uuid = cd.uuid)) AS end_date  FROM  current_data cd ) \n"
//				+ "SELECT  id, uuid, archived, externalid, name, start_date, end_date FROM  updated_end_date \n"
//				+ "ORDER BY  start_date ASC;";
		
		StringBuilder queryStringBuilder = new StringBuilder();
		queryStringBuilder.append("WITH current_data AS (")
		                  .append("SELECT id, uuid, archived, externalid, name, changedate AS start_date, ")
		                  .append("LEAD(changedate) OVER (PARTITION BY uuid ORDER BY changedate) AS end_date ")
		                  .append("FROM areas_history ");

		if (external != null) {
		    queryStringBuilder.append("WHERE uuid = '").append(external).append("' ");
		}

		queryStringBuilder.append("), ")
		                  .append("updated_end_date AS (")
		                  .append("SELECT cd.id, cd.uuid, cd.archived, cd.externalid, cd.name, cd.start_date, ")
		                  .append("COALESCE(cd.end_date, (SELECT changedate FROM areas WHERE areas.uuid = cd.uuid)) AS end_date ")
		                  .append("FROM current_data cd) ")
		                  .append("SELECT uuid, name, archived, externalid,  start_date, end_date ")
		                  .append("FROM updated_end_date ")
		                  .append("ORDER BY start_date ASC;");
		
		String queryString = queryStringBuilder.toString();

		Query seriesDataQuery = em.createNativeQuery(queryString);
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = seriesDataQuery.getResultList();
		resultData.addAll(resultList.stream()
			    .map(result -> new AreaHistoryExtractDto(
			        (String) result[0], // UUID
			        (String) result[1], // Name
			        (Boolean) result[2], // Archived
			        result[3] != null ? ((BigInteger) result[3]).longValue() : 0L, // External ID
			        ((Timestamp) result[4]).toLocalDateTime(), // Start Date
			        result[5] != null ? ((Timestamp) result[5]).toLocalDateTime() : LocalDateTime.now() // End Date
			    ))
			    .collect(Collectors.toList()));

//		resultData.addAll(resultList.stream()
//			    .map(result -> new AreaDto(
//			        (String) result[0].toString(),
//			        (String) result[1].toString(),
//			        (Boolean) result[2],
//			        result[3] != null ? ((BigInteger) result[3]).longValue() : 0L,
//			        ((Timestamp) result[4]).toLocalDateTime(),
//			        result[5] != null ? ((Timestamp) result[5]).toLocalDateTime() : ""
//			    ))
//			    .collect(Collectors.toList()));

		
		


		return resultData;
		
	}
}
