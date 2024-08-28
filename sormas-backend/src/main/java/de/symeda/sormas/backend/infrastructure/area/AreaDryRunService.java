package de.symeda.sormas.backend.infrastructure.area;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.user.User;

import org.apache.commons.lang3.StringUtils;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.infrastructure.area.AreaCriteria;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.common.AbstractInfrastructureAdoService;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;

@Stateless
@LocalBean
public class AreaDryRunService extends AbstractInfrastructureAdoService<AreaDryRun> {

	public AreaDryRunService() {
		super(AreaDryRun.class);
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, AreaDryRun> from) {
		return null;
	}

	public List<AreaDryRun> getByName(String name, boolean includeArchivedEntities) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<AreaDryRun> cq = cb.createQuery(getElementClass());
		Root<AreaDryRun> from = cq.from(getElementClass());
		Predicate filter = CriteriaBuilderHelper.unaccentedIlikePrecise(cb, from.get(AreaDryRun.NAME), name.trim());
		if (!includeArchivedEntities) {
			filter = cb.and(filter, createBasicFilter(cb, from));
		}

		cq.where(filter);

		return em.createQuery(cq).getResultList();
	}
	
	public List<AreaDryRun> getByExternalID(Long ext_id, boolean includeArchivedEntities) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<AreaDryRun> cq = cb.createQuery(getElementClass());
		Root<AreaDryRun> from = cq.from(getElementClass());
		
		//System.out.println(form +" sddddddddddSEFASD");
		Predicate filter = cb.equal(from.get("externalId"), ext_id);
		if (!includeArchivedEntities) {
			filter = cb.and(filter, createBasicFilter(cb, from));
			}
		cq.where(filter);
		System.out.println("SSSSSSSSSS456754345 - "+SQLExtractor.from(em.createQuery(cq)));
		return em.createQuery(cq).getResultList();
	}

	public Predicate buildCriteriaFilter(AreaCriteria criteria, CriteriaBuilder cb, Root<AreaDryRun> areaRoot) {
		Predicate filter = null;
		if (StringUtils.isNotBlank(criteria.getTextFilter())) {
			String[] textFilters = criteria.getTextFilter().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = CriteriaBuilderHelper.unaccentedIlike(cb, areaRoot.get(Region.NAME), textFilter);
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (criteria.getRelevanceStatus() != null) {
			if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper
					.and(cb, filter, cb.or(cb.equal(areaRoot.get(Area.ARCHIVED), false), cb.isNull(areaRoot.get(Area.ARCHIVED))));
			} else if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(areaRoot.get(Area.ARCHIVED), true));
			}
		}
		
		if(this.getCurrentUser().getJurisdictionLevel() == JurisdictionLevel.AREA) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(areaRoot.get(Area.UUID), this.getCurrentUser().getArea().getUuid()));
		}
		return filter;
	}

	@Override
	public List<AreaDryRun> getByExternalId(Long externalId, boolean includeArchived) {
		return getByExternalId(externalId, Area.EXTERNAL_ID, includeArchived);
	}
	
	public Set<AreaDryRun> getByReferenceDto(Set<AreaReferenceDto> area) {
		Set<AreaDryRun> areas = new HashSet<AreaDryRun>();
		for (AreaReferenceDto com : area) {
			if (com != null && com.getUuid() != null) {
				AreaDryRun result = getByUuid(com.getUuid());
				if (result == null) {
					logger.warn("Could not find entity for " + com.getClass().getSimpleName() + " with uuid "
							+ com.getUuid());
				}
				areas.add(result);
			}
		}
		return areas;
	}
}
