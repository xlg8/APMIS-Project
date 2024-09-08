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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataReferenceDto;
import de.symeda.sormas.api.campaign.data.MapCampaignDataDto;
import de.symeda.sormas.api.caze.MapCaseDto;
import de.symeda.sormas.api.caze.NewCaseDateType;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseQueryContext;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.facility.Facility;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.QueryHelper;
import de.symeda.sormas.utils.CaseJoins;

@Stateless
@LocalBean
public class CampaignFormDataDryRunService extends AdoServiceWithUserFilter<CampaignFormDataDryRun> {

	public CampaignFormDataDryRunService() {
		super(CampaignFormDataDryRun.class);
	}

	public Predicate createCriteriaFilter(CampaignFormDataCriteria criteria, CriteriaBuilder cb,
			Root<CampaignFormDataDryRun> root) {
		Join<CampaignFormDataDryRun, Campaign> campaignJoin = root.join(CampaignFormDataDryRun.CAMPAIGN, JoinType.LEFT);
		Join<CampaignFormDataDryRun, CampaignFormMeta> campaignFormJoin = root.join(CampaignFormDataDryRun.CAMPAIGN_FORM_META,
				JoinType.LEFT);
		Join<CampaignFormDataDryRun, Area> areaJoin = root.join(CampaignFormDataDryRun.AREA, JoinType.LEFT);
		Join<CampaignFormDataDryRun, Region> regionJoin = root.join(CampaignFormDataDryRun.REGION, JoinType.LEFT);
		Join<CampaignFormDataDryRun, District> districtJoin = root.join(CampaignFormDataDryRun.DISTRICT, JoinType.LEFT);
		Join<CampaignFormDataDryRun, Community> communityJoin = root.join(CampaignFormDataDryRun.COMMUNITY, JoinType.LEFT);
		Join<CampaignFormDataDryRun, User> userJoin = root.join(CampaignFormDataDryRun.CREATED_BY, JoinType.LEFT);
		Predicate filter = null;

		boolean isEoc = false;

		if (criteria.getUsertype().toString().equalsIgnoreCase(UserType.EOC_USER.toString())) {
			isEoc = true;
		}

		if (criteria.getCampaign() != null && criteria.getFormType() == null) {
			if (isEoc) {
				if (criteria.getFormType().toString().equalsIgnoreCase("post-campaign")) {
					filter = CriteriaBuilderHelper.and(cb, filter,
							cb.equal(campaignJoin.get(Campaign.UUID), criteria.getCampaign().getUuid()),

							cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)),
							cb.isTrue(root.get(CampaignFormDataDryRun.ISPUBLISHED)));
				} else {
					filter = CriteriaBuilderHelper.and(cb, filter,
							cb.equal(campaignJoin.get(Campaign.UUID), criteria.getCampaign().getUuid()),

							cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)));
				}

			} else {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.equal(campaignJoin.get(Campaign.UUID), criteria.getCampaign().getUuid()));
			}

		} else if (criteria.getCampaign() != null && criteria.getFormType() != null
				&& !"ALL PHASES".equals(criteria.getFormType())) {
			if (isEoc) {
				if(criteria.getFormType().toString().equalsIgnoreCase("post-campaign")) {
					filter = CriteriaBuilderHelper.and(cb, filter,
							cb.and(cb.equal(campaignFormJoin.get(CampaignFormMeta.FORM_TYPE),
									criteria.getFormType().toLowerCase())),
							cb.equal(campaignJoin.get(Campaign.UUID), criteria.getCampaign().getUuid()),
							cb.isFalse(campaignJoin.get(Campaign.ARCHIVED)),
							cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)),
							cb.isTrue(root.get(CampaignFormDataDryRun.ISPUBLISHED))

					);
				}else {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.and(cb.equal(campaignFormJoin.get(CampaignFormMeta.FORM_TYPE),
								criteria.getFormType().toLowerCase())),
						cb.equal(campaignJoin.get(Campaign.UUID), criteria.getCampaign().getUuid()),
						cb.isFalse(campaignJoin.get(Campaign.ARCHIVED)),
						cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)));
				}

			} else {

				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.and(cb.equal(campaignFormJoin.get(CampaignFormMeta.FORM_TYPE),
								criteria.getFormType().toLowerCase())),
						cb.equal(campaignJoin.get(Campaign.UUID), criteria.getCampaign().getUuid()),
						cb.isFalse(campaignJoin.get(Campaign.ARCHIVED))

				);
			}

		} else if (criteria.getCampaign() == null && criteria.getFormType() != null
				&& !"ALL PHASES".equals(criteria.getFormType())) {
			if (isEoc) {

				if(criteria.getFormType().toString().equalsIgnoreCase("post-campaign")) {
					filter = CriteriaBuilderHelper.and(cb, filter,
							cb.and(cb.equal(campaignFormJoin.get(CampaignFormMeta.FORM_TYPE),
									criteria.getFormType().toLowerCase()), cb.isFalse(campaignJoin.get(Campaign.ARCHIVED)),
									cb.isFalse(campaignJoin.get(Campaign.DELETED))),cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)),
							cb.isTrue(root.get(CampaignFormDataDryRun.ISPUBLISHED))
							);
				}else {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.and(cb.equal(campaignFormJoin.get(CampaignFormMeta.FORM_TYPE),
								criteria.getFormType().toLowerCase()), cb.isFalse(campaignJoin.get(Campaign.ARCHIVED)),
								cb.isFalse(campaignJoin.get(Campaign.DELETED))),
						cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)));
				}

			} else {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.and(cb.equal(campaignFormJoin.get(CampaignFormMeta.FORM_TYPE),
								criteria.getFormType().toLowerCase()), cb.isFalse(campaignJoin.get(Campaign.ARCHIVED)),
								cb.isFalse(campaignJoin.get(Campaign.DELETED))));
			}

		} else {
			if (isEoc) {
				if(criteria.getFormType().toString().equalsIgnoreCase("post-campaign")) {
					filter = CriteriaBuilderHelper.and(cb, filter,
							cb.or(cb.equal(campaignJoin.get(Campaign.ARCHIVED), false),
									cb.isNull(campaignJoin.get(Campaign.ARCHIVED))),
							cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)),
							cb.isTrue(root.get(CampaignFormDataDryRun.ISPUBLISHED))
							)
							
							;
//				}else {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(campaignJoin.get(Campaign.ARCHIVED), false),
								cb.isNull(campaignJoin.get(Campaign.ARCHIVED))),
						cb.isTrue(root.get(CampaignFormDataDryRun.ISVERIFIED)))

				;
				}

			} else {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(campaignJoin.get(Campaign.ARCHIVED), false),
								cb.isNull(campaignJoin.get(Campaign.ARCHIVED))));
			}

		}

		if (criteria.getCampaignFormMeta() != null) {
			// System.out.println("=======%%%%%%%%%%%%%%%=======
			// "+criteria.getCampaignFormMeta().getUuid());
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(campaignFormJoin.get(CampaignFormMeta.UUID), criteria.getCampaignFormMeta().getUuid()));

			filter = CriteriaBuilderHelper.and(cb, filter, cb.isFalse(root.get(CampaignFormDataDryRun.ARCHIVED)));
		}
		if (criteria.getArea() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(areaJoin.get(Area.UUID), criteria.getArea().getUuid()));
		}
		if (criteria.getRegion() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(regionJoin.get(Region.UUID), criteria.getRegion().getUuid()));
		}
		if (criteria.getDistrict() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(districtJoin.get(District.UUID), criteria.getDistrict().getUuid()));
		}
		if (criteria.getCommunity() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(communityJoin.get(Community.UUID), criteria.getCommunity().getUuid()));
		}
		if (criteria.getFormDate() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.greaterThanOrEqualTo(root.get(CampaignFormDataDryRun.FORM_DATE),
							DateHelper.getStartOfDay(criteria.getFormDate())),
					cb.lessThanOrEqualTo(root.get(CampaignFormDataDryRun.FORM_DATE),
							DateHelper.getEndOfDay(criteria.getFormDate())));

		}

		if (criteria.getIsVerified() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(root.get(CampaignFormDataDryRun.ISVERIFIED), criteria.getIsVerified()));
//					cb.equal(communityJoin.get(Community.UUID), criteria.getCommunity().getUuid()));
		}
		
		if (criteria.getIsPublished() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(root.get(CampaignFormDataDryRun.ISPUBLISHED), criteria.getIsPublished()));
//					cb.equal(communityJoin.get(Community.UUID), criteria.getCommunity().getUuid()));
		}

		return filter;
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, CampaignFormDataDryRun> campaignPath) {
		final User currentUser = getCurrentUser();
		if (currentUser == null) {
			return null;
		}

		Predicate filter = null;

		final JurisdictionLevel jurisdictionLevel = currentUser.getJurisdictionLevel();
		if (jurisdictionLevel != JurisdictionLevel.NATION) {
			switch (jurisdictionLevel) {
			case AREA:
				final Area area = currentUser.getArea();
				if (area != null) {
					filter = CriteriaBuilderHelper.or(cb, filter,
							cb.equal(campaignPath.get(CampaignFormDataDryRun.AREA).get(Area.ID), area.getId()));
				}
				break;
			case REGION:
				final Region region = currentUser.getRegion();
				if (region != null) {
					filter = CriteriaBuilderHelper.or(cb, filter,
							cb.equal(campaignPath.get(CampaignFormDataDryRun.REGION).get(Region.ID), region.getId()));
				}
				break;
			case DISTRICT:
				final District district = currentUser.getDistrict();
				if (district != null) {
					filter = CriteriaBuilderHelper.or(cb, filter,
							cb.equal(campaignPath.get(CampaignFormDataDryRun.DISTRICT).get(District.ID), district.getId()));
				}
				break;
			case COMMUNITY:
				final Set<Community> community = currentUser.getCommunity();
				if (community != null) {
					for (Community com : community) {
						filter = CriteriaBuilderHelper.or(cb, filter,
								cb.equal(campaignPath.get(CampaignFormDataDryRun.COMMUNITY).get(Community.ID), com.getId()));
					}
					// filter = CriteriaBuilderHelper.or(cb, filter, cb
					// .isMember(community.stream().map(Community::getId).collect(Collectors.toList()),
					// campaignPath.get(CampaignFormDataDryRun.COMMUNITY).get(Community.ID)));
				}
				break;
			default:
				return null;
			}
		}


		return filter;
	}

	public List<String> getAllActiveUuids() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<CampaignFormDataDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.and();

		if (getCurrentUser() != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, cb.isFalse(from.get(CampaignFormDataDryRun.ARCHIVED)), userFilter);
		}

		cq.where(filter);
		cq.select(from.get(Campaign.UUID));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormDataDryRun> getAllActiveAfter(Date date, List<Long> idList, List<Long> commIdList, List<Long> distrIdList) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataDryRun> cq = cb.createQuery(CampaignFormDataDryRun.class);
		Root<CampaignFormDataDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.and();

		if (getCurrentUser() != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, cb.isFalse(from.get(CampaignFormDataDryRun.ARCHIVED)), userFilter);
		}

		if (date != null) {
			Predicate dateFilter = createChangeDateFilter(cb, from, DateHelper.toTimestampUpper(date));
			if (dateFilter != null) {
				filter = cb.and(filter, dateFilter);
			}
		}
		
		 // Filter based on IDs
	    if (idList != null && !idList.isEmpty()) {
	        filter = cb.and(filter, from.get(CampaignFormDataDryRun.CAMPAIGN_FORM_META).in(idList));
	    }
	    
	    if (commIdList != null && !commIdList.isEmpty()) {
	        filter = cb.and(filter, from.get(CampaignFormDataDryRun.COMMUNITY).in(commIdList));
	    }
	    
	    //TODO improve this by adding isDistrict too campaignformdata while saving the form so that it will be easier to filter district entered data
	    if (distrIdList != null && !distrIdList.isEmpty()) {
	        filter = cb.and(filter, from.get(CampaignFormDataDryRun.DISTRICT).in(distrIdList));
	    }

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(AbstractDomainObject.CHANGE_DATE)));

		
		logger.debug("ttttttttttttttttttttttttttyyyy "+ SQLExtractor.from(em.createQuery(cq)));
		
		
		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormDataDryRun> getAllActive() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataDryRun> cq = cb.createQuery(CampaignFormDataDryRun.class);
		Root<CampaignFormDataDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.and();

		if (getCurrentUser() != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, cb.isFalse(from.get(CampaignFormDataDryRun.ARCHIVED)), userFilter);
		}

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(AbstractDomainObject.CREATION_DATE)));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormDataDryRun> getAllActiveData(Integer first, Integer max, Boolean includeArchived) {

		List<CampaignFormDataDryRun> emptyList = new ArrayList<>();
		if (max > 1000 || max == null || first == null || max < 0 || first < 0) {
			return emptyList;
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataDryRun> cq = cb.createQuery(CampaignFormDataDryRun.class);
		Root<CampaignFormDataDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.and();
		
		if (includeArchived != null) {
			if (!includeArchived) {
				filter = CriteriaBuilderHelper.and(cb, cb.isFalse(from.get(CampaignFormDataDryRun.ARCHIVED)));
				cq.where(filter);
			}

		}

		cq.orderBy(cb.asc(from.get(AbstractDomainObject.ID)));
		logger.debug("ttttttttttttttttttttttttttyyyy " + first + "<-----firsy max ----->" + max
				+ SQLExtractor.from(em.createQuery(cq)));
		return QueryHelper.getResultList(em, cq, first, max);

		// return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormDataDryRun> getByCampaignFormMeta_id(Long meta_id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataDryRun> cq = cb.createQuery(CampaignFormDataDryRun.class);
		Root<CampaignFormDataDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.and();

		// if (getCurrentUser() != null) {
		Predicate userFilter = createUserFilter(cb, cq, from);
		filter = CriteriaBuilderHelper.and(cb, cb.isFalse(from.get(CampaignFormDataDryRun.ARCHIVED)), userFilter);
		// }

		Predicate predicateForMetaId = cb.equal(from.get(CampaignFormMeta.ID), meta_id);

		Predicate finalPredicate = cb.and(filter, predicateForMetaId);

		cq.where(finalPredicate);
		cq.orderBy(cb.desc(from.get(AbstractDomainObject.CHANGE_DATE)));

		return em.createQuery(cq).getResultList();
	}

	public List<MapCampaignDataDto> getCampaignFormDataForMap() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<MapCampaignDataDto> cq = cb.createQuery(MapCampaignDataDto.class);
		Root<CampaignFormDataDryRun> caze = cq.from(getElementClass());

		List<MapCampaignDataDto> result;

		cq.multiselect(caze.get(CampaignFormDataDryRun.UUID), caze.get(CampaignFormDataDryRun.LAT), caze.get(CampaignFormDataDryRun.LON));

		result = em.createQuery(cq).getResultList();

		return result;
	}

	public List<CampaignFormDataIndexDto> getAllActiveRef() {
		// TODO Auto-generated method stub
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormDataIndexDto> cq = cb.createQuery(CampaignFormDataIndexDto.class);
		Root<CampaignFormDataDryRun> from = cq.from(getElementClass());

		Predicate filter = cb.and();
		cq.where(filter);
		cq.orderBy(cb.desc(from.get(AbstractDomainObject.CREATION_DATE)));

		return em.createQuery(cq).getResultList();
	}

	public int verify(String uuidx) {
		// TODO Auto-generated method stub
		String cdvv = "";
		cdvv = "UPDATE campaignformdata SET isverified = true where uuid = '" + uuidx + "';";
		System.err.println(cdvv + "Query from the service ");
		return em.createNativeQuery(cdvv).executeUpdate();
	}

	public int unVerifyData(String uuidx) {
		// TODO Auto-generated method stub
		String cdvv = "";
		cdvv = "UPDATE campaignformdata SET isverified = false where uuid = '" + uuidx + "';";
		System.err.println(cdvv + "Query to unverify from the service ");
		return em.createNativeQuery(cdvv).executeUpdate();
	}

	public int publishData(String uuidx) {
		// TODO Auto-generated method stub
		String cdvv = "";
		cdvv = "UPDATE campaignformdata SET ispublished = true where uuid = '" + uuidx + "';";
		System.err.println(cdvv + "Query to publish from the service ");
		return em.createNativeQuery(cdvv).executeUpdate();
	}

	public int unPublishData(String uuidx) {
		// TODO Auto-generated method stub
		String cdvv = "";
		cdvv = "UPDATE campaignformdata SET ispublished = false where uuid = '" + uuidx + "';";
		System.err.println(cdvv + "Query to unpublishing from the service ");
		return em.createNativeQuery(cdvv).executeUpdate();
	}
	
	public  int updateReassignedDistrictData(String uuidx) {
		// TODO Auto-generated method stub
		String cdvv = "";
		cdvv = "UPDATE campaignformdata c SET region_id = d.region_id, area_id = d.area_id \n"
				+ "FROM (SELECT dx.id, dx.region_id, r.area_id \n"
				+ "FROM district dx \n"
				+ "LEFT JOIN region r ON dx.region_id = r.id \n"
				+ "WHERE dx.uuid = '" + uuidx + "') AS d \n"
				+ "WHERE c.district_id  = d.id;";
		
//		System.err.println(cdvv + "Query to unpublishing from the service ");
		return em.createNativeQuery(cdvv).executeUpdate();
	}

}
