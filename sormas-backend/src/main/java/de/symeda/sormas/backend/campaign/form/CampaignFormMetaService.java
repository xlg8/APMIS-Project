package de.symeda.sormas.backend.campaign.form;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.vladmihalcea.hibernate.type.util.SQLExtractor;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.campaign.Campaign;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.user.User;

@Stateless
@LocalBean
public class CampaignFormMetaService extends AdoServiceWithUserFilter<CampaignFormMeta> {

	public CampaignFormMetaService() {
		super(CampaignFormMeta.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, CampaignFormMeta> from) {
		return null;
	}

	public Predicate buildCriteriaFilter(CampaignFormCriteria campaignFormCriteria, CriteriaBuilder cb,
			Root<CampaignFormMeta> from) {

		Predicate filter = null;
		if (campaignFormCriteria.getRelevanceStatus() != null) {
			if (campaignFormCriteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter,
						cb.or(cb.equal(from.get(CampaignFormMeta.ARCHIVED), false),
								cb.isNull(from.get(CampaignFormMeta.ARCHIVED))));
			} else if (campaignFormCriteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(CampaignFormMeta.ARCHIVED), true));
			}
		}

		if (campaignFormCriteria.getFormCategory() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(CampaignFormMeta.FORM_CATEGORY), campaignFormCriteria.getFormCategory()));
		}

		if (campaignFormCriteria.getModality() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(CampaignFormMeta.MODALITY), campaignFormCriteria.getModality()));
		}

		if (campaignFormCriteria.getFormType() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter,
					cb.equal(from.get(CampaignFormMeta.FORM_TYPE), campaignFormCriteria.getFormType()));
		}

		if (campaignFormCriteria.getFormName() != null) {
			String[] textFilters = (campaignFormCriteria.getFormName().split("\\s+"));
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
						CriteriaBuilderHelper.unaccentedIlikeCustom(cb, from.get(CampaignFormMeta.FORM_NAME),
								textFilter),
						CriteriaBuilderHelper.ilike(cb, from.get(CampaignFormMeta.UUID), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		return filter;
	}

	public List<CampaignFormMeta> getAllAfter(Date since, User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMeta> cq = cb.createQuery(getElementClass());
		Root<CampaignFormMeta> root = cq.from(getElementClass());

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
			cq.where(filter);
		}
		cq.orderBy(cb.desc(root.get(AbstractDomainObject.CHANGE_DATE)));

		List<CampaignFormMeta> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public List<CampaignFormMeta> getAllFormElements(User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMeta> cq = cb.createQuery(getElementClass());
		Root<CampaignFormMeta> root = cq.from(getElementClass());

		Predicate filter = createUserFilter(cb, cq, root);
//		if (since != null) {
//			Predicate dateFilter = createChangeDateFilter(cb, root, since);
//			if (filter != null) {
//				filter = cb.and(filter, dateFilter);
//			} else {
//				filter = dateFilter;
//			}
//		}
		if (filter != null) {
			cq.where(filter);
		}
		cq.orderBy(cb.desc(root.get(AbstractDomainObject.CHANGE_DATE)));

		List<CampaignFormMeta> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaign(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignAndUserLanguage(String uuid,
			String userLanguage) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);

		if (userLanguage.equalsIgnoreCase("pashto")) {
			cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
					campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO));
		} else if (userLanguage.equalsIgnoreCase("dari")) {
			cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
					campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI));
		} else {
			cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
					campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));
		}

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignPashto(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignDari(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		// Predicate typefilter =
		// cb.notEqual(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
		// "intra-campaign"); //

		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignIntraCampaign(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate typefilter = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), "intra-campaign");// cb.and

		cq = cq.where(filter, typefilter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignPostCampaign(String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);

		Predicate filter = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate typefilter = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), "post-campaign");// cb.and
//		Predicate filterxx = cb.equal(campaignRoot.get(Campaign.PUBLISHED), true);
		cq = cq.where(filter, typefilter);// filterxx);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

//		System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" + SQLExtractor.from(em.createQuery(cq)));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRound(String round,
			String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndPashto(String round,
			String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_PASHTO),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" + SQLExtractor.from(em.createQuery(cq)));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundAndDari(String round,
			String uuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME_DARI),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_CATEGORY),
				campaignFormMetaJoin.get(CampaignFormMeta.DAYSTOEXPIRE));

		return em.createQuery(cq).getResultList();
	}

	public List<CampaignFormMetaReferenceDto> getCampaignFormMetasAsReferencesByCampaignandRoundandForm(String round,
			String uuid, Set<FormAccess> userFormAccess) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignFormMetaReferenceDto> cq = cb.createQuery(CampaignFormMetaReferenceDto.class);
		Root<Campaign> campaignRoot = cq.from(Campaign.class);
		Join<Campaign, CampaignFormMeta> campaignFormMetaJoin = campaignRoot.join(Campaign.CAMPAIGN_FORM_METAS);
		Predicate filterc = cb.equal(campaignRoot.get(Campaign.UUID), uuid);
		Predicate filterx = cb.equal(campaignFormMetaJoin.get(CampaignFormMeta.FORM_TYPE), round);

		Predicate filter = cb.and(filterc, filterx);
		// TODO: post campaign implementations
		cq = cq.where(filter);
		cq.multiselect(campaignFormMetaJoin.get(CampaignFormMeta.UUID),
				campaignFormMetaJoin.get(CampaignFormMeta.FORM_NAME));

		return em.createQuery(cq).getResultList();
	}
}
