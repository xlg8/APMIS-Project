package de.symeda.sormas.backend.dashboard;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.dashboard.DashboardCriteria;
import de.symeda.sormas.api.dashboard.DashboardFacade;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb;

@Stateless(name = "DashboardFacade")
public class DashboardFacadeEjb implements DashboardFacade {

	@EJB
	private FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;

	@EJB
	private DashboardService dashboardService;

	@LocalBean
	@Stateless
	public static class DashboardFacadeEjbLocal extends DashboardFacadeEjb {

	}

	@Override
	public String getLastReportedDistrictName(DashboardCriteria dashboardCriteria) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countCasesConvertedFromContacts(DashboardCriteria dashboardCriteria) {
		// TODO Auto-generated method stub
		return 0;
	}
}
