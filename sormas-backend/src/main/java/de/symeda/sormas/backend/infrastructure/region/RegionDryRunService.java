/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.backend.infrastructure.region;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
//import de.symeda.sormas.api.infrastructure.region.RegionDryRunCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.common.AbstractInfrastructureAdoService;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.country.Country;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb.CountryFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.district.District;

@Stateless
@LocalBean
public class RegionDryRunService extends AbstractInfrastructureAdoService<RegionDryRun> {

	@EJB
	private CountryFacadeEjbLocal countryFacade;

	public RegionDryRunService() {
		super(RegionDryRun.class);
	}

	public List<RegionDryRun> getByName(String name, boolean includeArchivedEntities) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RegionDryRun> cq = cb.createQuery(getElementClass());
		Root<RegionDryRun> from = cq.from(getElementClass());

		Predicate filter = CriteriaBuilderHelper.unaccentedIlikePrecise(cb, from.get(RegionDryRun.NAME), name.trim());
		if (!includeArchivedEntities) {
			filter = cb.and(filter, createBasicFilter(cb, from));
		}

		cq.where(filter);

		return em.createQuery(cq).getResultList();
	}

	public List<RegionDryRun> getByExternalId(Long ext_id, boolean includeArchivedEntities, int noUsed) {
		System.out.println("####################################3 " + ext_id);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<RegionDryRun> cq = cb.createQuery(getElementClass());
		Root<RegionDryRun> from = cq.from(getElementClass());
		// System.out.println("SSSSSSDDDDDSSSSS45 -
		// "+SQLExtractor.from(em.createQuery(cq)));
		Predicate filter = cb.equal(from.get("externalId"), ext_id);

		if (!includeArchivedEntities) {
			filter = cb.and(filter, createBasicFilter(cb, from));
		}

		cq.where(filter);
		// System.out.println("SSSSSSDDDDD56754345 -
		// "+SQLExtractor.from(em.createQuery(cq)));
		return em.createQuery(cq).getResultList();
	}

	public List<RegionDryRun> getByExternalId(Long externalId, boolean includeArchivedEntities) {
		return getByExternalId(externalId, RegionDryRun.EXTERNAL_ID, includeArchivedEntities);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, RegionDryRun> from) {
		// no filter by user needed
		return null;
	}

	public Predicate buildCriteriaFilter(RegionCriteria criteria, CriteriaBuilder cb, Root<RegionDryRun> from) {
		Join<RegionDryRun, Area> area = from.join(RegionDryRun.AREA, JoinType.LEFT);
		Predicate filter = null;

		if (criteria.getArea() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(area.get(Area.UUID), criteria.getArea().getUuid()));
		}

		if (criteria.getNameEpidLike() != null) {
			String[] textFilters = criteria.getNameEpidLike().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb
						.or(CriteriaBuilderHelper.unaccentedIlike(cb, from.get(RegionDryRun.NAME), textFilter));// ,
				// CriteriaBuilderHelper.ilike(cb, from.get(RegionDryRun.EPID_CODE), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (criteria.getRelevanceStatus() != null) {
			if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(from.get(RegionDryRun.ARCHIVED), false), cb.isNull(from.get(RegionDryRun.ARCHIVED))));
			} else if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(RegionDryRun.ARCHIVED), true));
			}
		}

		CountryReferenceDto country = criteria.getCountry();
		if (country != null) {
			CountryReferenceDto serverCountry = countryFacade.getServerCountry();

			Path<Object> countryUuid = from.join(RegionDryRun.COUNTRY, JoinType.LEFT).get(Country.UUID);
			Predicate countryFilter = cb.equal(countryUuid, country.getUuid());

			if (country.equals(serverCountry)) {
				filter = CriteriaBuilderHelper.and(cb, filter,
						CriteriaBuilderHelper.or(cb, countryFilter, countryUuid.isNull()));
			} else {
				filter = CriteriaBuilderHelper.and(cb, filter, countryFilter);
			}
		}

		if (this.getCurrentUser().getArea() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(area.get(Area.UUID), this.getCurrentUser().getArea().getUuid()));
		}

		return filter;
	}

	public Set<RegionDryRun> getByReferenceDto(Set<RegionReferenceDto> region) {
		Set<RegionDryRun> regions = new HashSet<RegionDryRun>();
		for (RegionReferenceDto com : region) {
			if (com != null && com.getUuid() != null) {
				RegionDryRun result = getByUuid(com.getUuid());
				if (result == null) {
					logger.warn("Could not find entity for " + com.getClass().getSimpleName() + " with uuid "
							+ com.getUuid());
				}
				regions.add(result);
			}
		}
		return regions;
	}	
}
