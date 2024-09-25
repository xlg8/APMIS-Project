package de.symeda.sormas.backend.infrastructure.community;

import java.math.BigInteger;
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
import javax.print.attribute.standard.MediaSize.ISO;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.ClusterFloatStatus;
import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.ErrorStatusEnum;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDryRunDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDryRunFacade;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityFacade;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.CommunityUserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.campaign.statistics.CampaignStatisticsService;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.AbstractInfrastructureEjb;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.district.DistrictFacadeEjb;
import de.symeda.sormas.backend.infrastructure.district.DistrictService;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.infrastructure.region.RegionFacadeEjb;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;

@Stateless(name = "CommunityDryRunFacade")
public class CommunityDryRunFacadeEjb extends AbstractInfrastructureEjb<CommunityDryRun, CommunityDryRunService>
		implements CommunityDryRunFacade {

	private FormAccess frmsAccess;
	private ErrorStatusEnum errorStatusEnum;

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private UserService userService;

	@EJB
	private DistrictService districtService;

	@EJB
	private CampaignStatisticsService campaignStatisticsService;

	public CommunityDryRunFacadeEjb() {
	}

	@Inject
	protected CommunityDryRunFacadeEjb(CommunityDryRunService service,
			FeatureConfigurationFacadeEjbLocal featureConfiguration) {
		super(service, featureConfiguration);
	}

	@Override
	public List<CommunityReferenceDto> getReferencesByName(String name, boolean includeArchived) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public CommunityDryRunDto save(@Valid CommunityDryRunDto dto) throws ValidationRuntimeException {
		return save(dto, false);
	}

	@Override
	public CommunityDryRunDto save(@Valid CommunityDryRunDto dto, boolean allowMerge) throws ValidationRuntimeException {
		checkInfraDataLocked();

		if (dto.getDistrict() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validDistrict));
		}

		CommunityDryRun community = service.getByUuid(dto.getUuid());

		if (community == null) {
			List<CommunityReferenceDto> duplicates = getByName(dto.getName(), dto.getDistrict(), true);
			if ((duplicates == null || duplicates.isEmpty()) && (dto.getName() != null || dto.getDistrict() != null)) {

			}else if (!duplicates.isEmpty()  && (dto.getName() != null || dto.getDistrict() != null)) {
				if (allowMerge) {
					String uuid = duplicates.get(0).getUuid();
					community = service.getByUuid(uuid);
					CommunityDryRunDto dtoToMerge = getByUuid(uuid);
					dto = DtoHelper.copyDtoValues(dtoToMerge, dto, true);
				} else {
					throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.importCommunityAlreadyExists));
				}
			}
		}
		community = fillOrBuildEntity(dto, community, true);
		
		System.out.println(dto.getFloating() + "============================dto.getFloating()===============" + community.getFloating()); 
		service.ensurePersisted(community);
		return toDto(community);
	}
	
	private CommunityDryRunDto toDto(CommunityDryRun entity) {

		if (entity == null) {
			return null;
		}
		CommunityDryRunDto dto = new CommunityDryRunDto();
		DtoHelper.fillDto(dto, entity);

		dto.setName(entity.getName());
		dto.setFa_af(entity.getFa_af());
		dto.setPs_af(entity.getPs_af());
		dto.setGrowthRate(entity.getGrowthRate());
		dto.setDistrict(DistrictFacadeEjb.toReferenceDto(entity.getDistrict()));
		dto.setDistrictexternalId(entity.getDistrict().getExternalId());
		dto.setRegion(RegionFacadeEjb.toReferenceDto(entity.getDistrict().getRegion()));
		dto.setRegionexternalId(entity.getDistrict().getRegion().getExternalId());
		dto.setArchived(entity.isArchived());
		dto.setExternalId(entity.getExternalId());
		dto.setClusterNumber(entity.getClusterNumber());
		dto.setAreaname(entity.getDistrict().getRegion().getArea().getName());
		dto.setAreaexternalId(entity.getDistrict().getRegion().getArea().getExternalId());
		dto.setFloating(entity.getFloating());
		return dto;
	}

	private CommunityDryRun fillOrBuildEntity(@NotNull CommunityDryRunDto source, CommunityDryRun target, boolean checkChangeDate) {

			target = DtoHelper.fillOrBuildEntity(source, target, CommunityDryRun::new, checkChangeDate);
	
			target.setName(source.getName());
			target.setGrowthRate(source.getGrowthRate());
			target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));
			target.setArchived(source.isArchived());
			target.setExternalId(source.getExternalId());
			target.setClusterNumber(source.getClusterNumber());
			target.setFloating(source.getFloating());
			return target;
		}

	@Override
	public List<CommunityReferenceDto> getByExternalId(Long externalId, boolean includeArchivedEntities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommunityDryRunDto getByUuid(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityDryRunDto> getIndexList(CommunityCriteriaNew criteria, Integer first, Integer max,
			List<SortProperty> sortProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(CommunityCriteriaNew criteria) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<CommunityDryRunDto> getAllAfter(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityDryRunDto> getByUuids(List<String> uuids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllUuids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityReferenceDto> getAllActiveByDistrict(String districtUuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<CommunityDryRunDto> getIndexPage(CommunityCriteriaNew communityCriteria, Integer offset, Integer size,
			List<SortProperty> sortProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommunityReferenceDto getCommunityReferenceByUuid(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommunityReferenceDto getCommunityReferenceById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityUserReportModelDto> getAllActiveCommunitytoRerence(CommunityCriteriaNew criteria,
			Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getAllActiveCommunitytoRerenceCount(CommunityCriteriaNew criteria, Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess formacc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityUserReportModelDto> getAllActiveCommunitytoRerencexx(CommunityCriteriaNew criteria,
			Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityReferenceDto> getByName(String name, DistrictReferenceDto districtRef,
			boolean includeArchivedEntities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityReferenceDto> getByExternalID(Long ext_id, DistrictReferenceDto districtRef,
			boolean includeArchivedEntities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsedInOtherInfrastructureData(Collection<String> communityUuids) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasArchivedParentInfrastructure(Collection<String> communityUuids) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, String> getDistrictUuidsForCommunities(List<CommunityReferenceDto> communities) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countReportGrid(CommunityCriteriaNew criteria, FormAccess formacc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<CommunityUserReportModelDto> getAllActiveCommunitytoRerenceNew(Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess formacc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityDryRunDto> getAllCommunities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityUserReportModelDto> getAllActiveCommunitytoRerenceFlow(CommunityCriteriaNew criteria,
			Integer first, Integer max, List<SortProperty> sortProperties, FormAccess formacc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getAllActiveCommunitytoRerenceFLowCount(CommunityCriteriaNew criteria, Integer first, Integer max,
			List<SortProperty> sortProperties, FormAccess formacc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CommunityDryRunDto> getAllAfterWithDistrict(Date date, Set<DistrictReferenceDto> rDistdto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearDryRunTable() {	
	// TODO Auto-generated method stub
		
	    String truncateQuery = "TRUNCATE TABLE communitydryrun";

	    // Create a native query
	    Query query = em.createNativeQuery(truncateQuery);

	    // Execute the query
	    query.executeUpdate();
	}

}
