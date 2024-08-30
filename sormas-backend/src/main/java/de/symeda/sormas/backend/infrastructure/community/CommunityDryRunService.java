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
package de.symeda.sormas.backend.infrastructure.community;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.country.CountryReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.common.AbstractInfrastructureAdoService;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.country.Country;
import de.symeda.sormas.backend.infrastructure.country.CountryFacadeEjb.CountryFacadeEjbLocal;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.region.Region;

@Stateless
@LocalBean
public class CommunityDryRunService extends AbstractInfrastructureAdoService<CommunityDryRun> {

	@EJB
	private CountryFacadeEjbLocal countryFacade;

	public CommunityDryRunService() {
		super(CommunityDryRun.class);
	}

	public List<CommunityDryRun> getByName(String name, District district, boolean includeArchivedEntities) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CommunityDryRun> cq = cb.createQuery(getElementClass());
		Root<CommunityDryRun> from = cq.from(getElementClass());

		Predicate filter = CriteriaBuilderHelper.unaccentedIlikePrecise(cb, from.get(CommunityDryRun.NAME), name.trim());
		if (!includeArchivedEntities) {
			filter = cb.and(filter, createBasicFilter(cb, from));
		}
		if (district != null) {
			filter = cb.and(filter, cb.equal(from.get(CommunityDryRun.DISTRICT), district));
		}

		cq.where(filter);

		return em.createQuery(cq).getResultList();
	}

	public List<CommunityDryRun> getByAll() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CommunityDryRun> cq = cb.createQuery(getElementClass());
		Root<CommunityDryRun> from = cq.from(getElementClass());

		return em.createQuery(cq).getResultList();
	}

	public List<CommunityDryRun> getByExternalId(Long ext_id, District district_ext, boolean includeArchivedEntities) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CommunityDryRun> cq = cb.createQuery(getElementClass());
		Root<CommunityDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.equal(from.get("externalId"), ext_id);

		if (!includeArchivedEntities) {
			filter = cb.and(filter, createBasicFilter(cb, from));
		}
		if (district_ext != null) {
			filter = cb.and(filter, cb.equal(from.get(CommunityDryRun.DISTRICT), district_ext));
		}

		cq.where(filter);

		return em.createQuery(cq).getResultList();
	}

	public List<CommunityDryRun> getByExternalId(Long externalId, boolean includeArchivedEntities) {
		return getByExternalId(externalId, CommunityDryRun.EXTERNAL_ID, includeArchivedEntities);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, CommunityDryRun> from) {
		// no filter by user needed
		return null;
	}

	public Predicate buildCriteriaFilter(CommunityCriteriaNew criteria, CriteriaBuilder cb, Root<CommunityDryRun> from) {
		Join<CommunityDryRun, District> district = from.join(CommunityDryRun.DISTRICT, JoinType.LEFT);
		Join<District, Region> region = district.join(District.REGION, JoinType.LEFT);
		Join<Region, Area> area = region.join(Region.AREA, JoinType.LEFT);
		Predicate filter = null;

		CountryReferenceDto country = criteria.getCountry();

		if (country != null) {
			System.out.println("countrycountrycountryzzzzzzzzzzzz");

			CountryReferenceDto serverCountry = countryFacade.getServerCountry();

			Path<Object> countryUuid = from.join(CommunityDryRun.DISTRICT, JoinType.LEFT).join(District.REGION, JoinType.LEFT)
					.join(Region.COUNTRY, JoinType.LEFT).get(Country.UUID);
			Predicate countryFilter = cb.equal(countryUuid, country.getUuid());

			if (country.equals(serverCountry)) {

				filter = CriteriaBuilderHelper.and(cb, filter,
						CriteriaBuilderHelper.or(cb, countryFilter, countryUuid.isNull()));
			} else {
				filter = CriteriaBuilderHelper.and(cb, filter, countryFilter);
			}
		}

		AreaReferenceDto aread = criteria.getArea(); // == null

//		System.out.println(aread.getUuid() + "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz "+criteria.getArea()); //== null

		if (criteria.getArea() != null) { // why passing?
			System.out.println("zzzzzzzzzzzzzzzzzzzz" + aread.getUuid());
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(area.get(Area.UUID), aread.getUuid()));
		}

		if (criteria.getRegion() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(region.get(Region.UUID), criteria.getRegion().getUuid()));
		}
		if (criteria.getDistrict() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(district.get(District.UUID), criteria.getDistrict().getUuid()));
		}
		if (criteria.getNameLike() != null) {
			String[] textFilters = criteria.getNameLike().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = CriteriaBuilderHelper.unaccentedIlike(cb, from.get(District.NAME), textFilter);
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (criteria.getRelevanceStatus() != null) {
//			boolean relavenceStatus = true;
			if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(from.get(CommunityDryRun.ARCHIVED), false),
								cb.isNull(from.get(CommunityDryRun.ARCHIVED))));
			} else if (criteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
			
			
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(CommunityDryRun.ARCHIVED ),  true));
			}else {
				filter = CriteriaBuilderHelper.and(cb, filter);

			}

		}

		if (this.getCurrentUser().getArea() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(area.get(Area.UUID), this.getCurrentUser().getArea().getUuid()));
		}
		return filter;
	}

	public List<CommunityReferenceDto> getByDistrict(DistrictReferenceDto district) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CommunityReferenceDto> cq = cb.createQuery(CommunityReferenceDto.class);
		Root<CommunityDryRun> root = cq.from(CommunityDryRun.class);
		Join<CommunityDryRun, District> communityDistrictJoin = root.join(CommunityDryRun.DISTRICT);

		Predicate filter = cb.equal(communityDistrictJoin.get(District.UUID), district.getUuid());
		cq.where(filter);
		cq.multiselect(root.get(CommunityDryRun.UUID), root.get(CommunityDryRun.NAME));

		TypedQuery query = em.createQuery(cq);
		return query.getResultList();
	}

	public Set<CommunityDryRun> getByReferenceDto(Set<CommunityReferenceDto> community) {
		Set<CommunityDryRun> communities = new HashSet<CommunityDryRun>();
		for (CommunityReferenceDto com : community) {
			if (com != null && com.getUuid() != null) {
				CommunityDryRun result = getByUuid(com.getUuid());
				if (result == null) {
					logger.warn("Could not find entity for " + com.getClass().getSimpleName() + " with uuid "
							+ com.getUuid());
				}
				communities.add(result);
			}
		}

		return communities;
	}

}
