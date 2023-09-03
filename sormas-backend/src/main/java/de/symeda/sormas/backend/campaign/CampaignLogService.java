package de.symeda.sormas.backend.campaign;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;

@Stateless
@LocalBean
public class CampaignLogService extends AbstractCoreAdoService<CampaignLog> {

	public CampaignLogService() {
		super(CampaignLog.class);
	}

	public List<CampaignLog> getAllActiveLogs(CampaignReferenceDto camp) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<CampaignLog> cq = cb.createQuery(getElementClass());
		Root<CampaignLog> root = cq.from(getElementClass());
		cq.where(cb.equal(root.get(CampaignLog.NAME), camp));
		cq.orderBy(cb.desc(root.get(CampaignLog.ACTION_DATE)));

		return em.createQuery(cq).getResultList();
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, CampaignLog> from) {
		// TODO Auto-generated method stub
		return null;
	}


}
