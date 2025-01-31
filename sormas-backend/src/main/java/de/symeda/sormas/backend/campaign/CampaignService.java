package de.symeda.sormas.backend.campaign;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.infrastructure.PopulationDataDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.campaign.form.CampaignFormMetaExpDay;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.infrastructure.PopulationData;
import de.symeda.sormas.backend.user.User;

@Stateless
@LocalBean
public class CampaignService extends AbstractCoreAdoService<Campaign> {

	public CampaignService() {
		super(Campaign.class);
	}

	/**
	 * a user who has access to @CamnpaignView can read all campaigns
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, Campaign> from) {
		return null;
	}

	public Predicate buildCriteriaFilter(CampaignCriteria campaignCriteria, CriteriaBuilder cb, Root<Campaign> from) {

		Predicate filter = null;
		if (campaignCriteria.getDeleted() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Campaign.DELETED), campaignCriteria.getDeleted()));
		}
		if (campaignCriteria.getStartDateAfter() != null || campaignCriteria.getStartDateBefore() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.between(from.get(Campaign.START_DATE),
					campaignCriteria.getStartDateAfter(), campaignCriteria.getStartDateBefore()));
		}
		if (campaignCriteria.getEndDateAfter() != null || campaignCriteria.getEndDateBefore() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.between(from.get(Campaign.END_DATE),
					campaignCriteria.getEndDateAfter(), campaignCriteria.getEndDateAfter()));
		}
		if (campaignCriteria.getFreeText() != null) {
			String[] textFilters = campaignCriteria.getFreeText().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
						CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Campaign.NAME), textFilter),
						CriteriaBuilderHelper.ilike(cb, from.get(Campaign.UUID), textFilter),
						CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Campaign.CAMPAIGN_YEAR), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (campaignCriteria.getRelevanceStatus() != null) {
			if (campaignCriteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(from.get(Campaign.ARCHIVED), false), cb.isNull(from.get(Campaign.ARCHIVED))));
			} else if (campaignCriteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Campaign.ARCHIVED), true));
			}
		}	
		
		if (campaignCriteria.getCampaignYear() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(Campaign.CAMPAIGN_YEAR), campaignCriteria.getCampaignYear()));
		}	
		return filter;
	}

	public List<String> getAllActiveUuids() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Campaign> from = cq.from(getElementClass());

		Predicate filter = cb.and();

		if (getCurrentUser() != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, createActiveCampaignsFilterForMobile(cb, from), userFilter);
		}

		cq.where(filter);
		cq.select(from.get(Campaign.UUID));

		return em.createQuery(cq).getResultList();
	}

	public List<Campaign> getAllAfter(Date since, User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Campaign> cq = cb.createQuery(getElementClass());
		Root<Campaign> root = cq.from(getElementClass());

		Predicate filter = createUserFilter(cb, cq, root);
		if (since != null) {
			Predicate dateFilter = createChangeDateFilter(cb, root, since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}
		if (filter != null) {
			filter = cb.and(filter, createActiveCampaignsFilter(cb, root));
			cq.where(filter);
		} else {
			cq.where(createActiveCampaignsFilter(cb, root));
		}

		cq.orderBy(cb.desc(root.get(AbstractDomainObject.CHANGE_DATE)));

		return em.createQuery(cq).getResultList();
	}

	public List<Campaign> getAllActive() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Campaign> cq = cb.createQuery(getElementClass());
		Root<Campaign> root = cq.from(getElementClass());
		cq.where(cb.and(cb.isFalse(root.get(Campaign.ARCHIVED)), cb.isFalse(root.get(Campaign.DELETED))));
		cq.orderBy(cb.desc(root.get(AbstractDomainObject.CHANGE_DATE)));

		return em.createQuery(cq).getResultList();
	}
	
	public List<Campaign> getAllActiveByCampaignStartDate() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Campaign> cq = cb.createQuery(getElementClass());
		Root<Campaign> root = cq.from(getElementClass());
		cq.where(cb.and(cb.isFalse(root.get(Campaign.ARCHIVED)), cb.isFalse(root.get(Campaign.DELETED))));
		cq.orderBy(cb.desc(root.get(Campaign.START_DATE)));

		return em.createQuery(cq).getResultList();
	}
	
	


//	public String cloneForm(Campaign uuidx, Long userCreatingId) {
//
//		
//		CampaignDto dxc = new CampaignDto();;
//		
//		
//		
//		
//		
//		
//		
//		String cdv = "";
//		String cds = "";
//
//		final Long mill = ZonedDateTime.now().toInstant().toEpochMilli();
//
//		try {
//			cds = cloneFormx1x(uuidx, mill, userCreatingId);
//		} finally {
////			nextval('entity_seq') AS new_id,
////			create_new_uuid(uuid) AS new_uuid
//			
//		cdv = "insert into campaign_campaignformmeta  (SELECT " + mill + " as id, "
//				+ "cd.campaignformmeta_id, cd.sys_period FROM campaigns dc inner join campaign_campaignformmeta cd on (dc.id = cd.campaign_id) where dc.name='"
//				+ uuidx + "' and deleted = false)";
//		
//		int notused = em.createNativeQuery(cdv).executeUpdate();
//		}
//		return cds;
//	}
//	
//	public BigInteger countPopulationtoClone(Campaign uuidx) {
//		String cdv = " SELECT count(*) from populationdata WHERE campaign_id = "+uuidx.getId()+";";
//		return (BigInteger) em.createNativeQuery(cdv).getSingleResult();
//	}

	public List<PopulationData> clonePopulationData(Campaign uuidx, Campaign newCampaignUuid) {

		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<PopulationData> cq = cb.createQuery(PopulationData.class);
		Root<PopulationData> root = cq.from(PopulationData.class);
		cq.where(cb.and(cb.equal(root.get(PopulationData.CAMPAIGN), uuidx)));

		return em.createQuery(cq).getResultList();

	}
	
public List<CampaignFormMetaExpDay> cloneFormMetaExpiry(Campaign uuidx, Campaign newCampaignUuid) {

		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaExpDay> cq = cb.createQuery(CampaignFormMetaExpDay.class);
		Root<CampaignFormMetaExpDay> root = cq.from(CampaignFormMetaExpDay.class);
		cq.where(cb.and(cb.equal(root.get(CampaignFormMetaExpDay.CAMPAIGN), uuidx)));

		return em.createQuery(cq).getResultList();

	}

	public int campaignPublish(String uuidx, boolean published) {

		String cdvv = "";
		if (published) {
			cdvv = "update campaigns c set published = false where uuid = '" + uuidx + "' ";

		} else {
			cdvv = "update campaigns c set published = true where uuid = '" + uuidx + "' ";
		}
		System.out.println(cdvv);
		return em.createNativeQuery(cdvv).executeUpdate();
	}

	public int closeAndOpenForm(String uuidx, boolean close) {

		String cdvv = "";
		if (close) {
			cdvv = "update campaigns set openandclose = false where uuid = '" + uuidx + "'";
		} else {
			cdvv = "update campaigns set openandclose = true where uuid = '" + uuidx + "'";
		}
		System.out.println(cdvv);
		return em.createNativeQuery(cdvv).executeUpdate();
	}

//	public String cloneFormx1x(Campaign uuidx, Long mill, Long userCreatingId) {
//
//		UUID uuisd = UUID.randomUUID();
//
//		String cdc = "insert into campaigns (SELECT " + mill + " as id, '" + uuisd.toString().toUpperCase()
//				+ "' as uuid, changedate, creationdate, CONCAT(name,'-DUP'), description, startdate, enddate, "
//				+ userCreatingId
//				+ ", deleted, archived, sys_period, dashboardelements, cluster, round, campaignyear, openandclose FROM campaigns where name='"
//				+ uuidx + "' and archived = false and  deleted = false)";
//
//		System.out.println(cdc);
//
//		em.createNativeQuery(cdc).executeUpdate();
//
//		return uuisd.toString().toUpperCase();
//	}

	/*
	 * public int cloneFormx(Campaign uuidx, int unix) { String cdv =
	 * "insert into campaign_campaignformmeta (SELECT CAST(CONCAT('-1',dc.id,'"
	 * +unix+"') AS bigint) as id, cd.campaignformmeta_id, cd.sys_period FROM campaigns dc inner join campaign_campaignformmeta cd on (dc.id = cd.campaign_id) where dc.name='"
	 * +uuidx+"')"; return em.createNativeQuery(cdv).executeUpdate(); }
	 */

	public Predicate createActiveCampaignsFilter(CriteriaBuilder cb, Root<Campaign> root) {
		return cb.and(cb.isFalse(root.get(Campaign.ARCHIVED)), cb.isFalse(root.get(Campaign.DELETED)));
	}

	public Predicate createActiveCampaignsFilterForMobile(CriteriaBuilder cb, Root<Campaign> root) {
		return cb.and(cb.isFalse(root.get(Campaign.ARCHIVED)), cb.isTrue(root.get(Campaign.CLOSEOPEN)),
				cb.isFalse(root.get(Campaign.DELETED)));
	}
}
