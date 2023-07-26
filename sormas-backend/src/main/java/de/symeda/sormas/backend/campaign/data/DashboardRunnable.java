package de.symeda.sormas.backend.campaign.data;


import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.symeda.sormas.backend.campaign.statistics.CampaignStatisticsService;
import de.symeda.sormas.backend.util.ModelConstants;

public class DashboardRunnable implements Runnable {
	
	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;
	
	@EJB
	private CampaignStatisticsService campaignStatisticsService;
	
	
    public void run() {
    	boolean isAnalyticsOld = campaignStatisticsService.checkChangedDb("campaignformdata", "camapaigndata_main");
    	
    	if(isAnalyticsOld) {
    	final String jpqlQueries = "REFRESH MATERIALIZED VIEW CONCURRENTLY camapaigndata_main;";
    			
    			try {
    				
    			     em.createNativeQuery(jpqlQueries).executeUpdate();
    			     updateTrakerTable();
    			} catch (Exception e) {
    			   System.err.println(e.getStackTrace());
    			}
    	}
    }
    
    private void updateTrakerTable() {
		//get the total size of the analysis
		final String joinBuilder = "INSERT INTO tracktableupdates (table_name, last_updated)\n"
				+ "    VALUES ('camapaigndata_main', NOW())\n"
				+ "    ON CONFLICT (table_name)\n"
				+ "    DO UPDATE SET last_updated = NOW();";
				
		em.createNativeQuery(joinBuilder).executeUpdate();
		
	};
	
	
}