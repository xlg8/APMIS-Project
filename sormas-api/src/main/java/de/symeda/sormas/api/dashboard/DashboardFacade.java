package de.symeda.sormas.api.dashboard;

import javax.ejb.Remote;

@Remote
public interface DashboardFacade {

	String getLastReportedDistrictName(DashboardCriteria dashboardCriteria);

	long countCasesConvertedFromContacts(DashboardCriteria dashboardCriteria);

}
