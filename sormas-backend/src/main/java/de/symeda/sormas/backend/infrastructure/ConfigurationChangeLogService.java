package de.symeda.sormas.backend.infrastructure;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.user.UserActivitySummary;

@Stateless
@LocalBean
public class ConfigurationChangeLogService  extends AbstractCoreAdoService<ConfigurationChangeLog>{
	
	public ConfigurationChangeLogService() {
		super(ConfigurationChangeLog.class);
	}

	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, ConfigurationChangeLog> from) {
		// TODO Auto-generated method stub
		return null;
	}

}
