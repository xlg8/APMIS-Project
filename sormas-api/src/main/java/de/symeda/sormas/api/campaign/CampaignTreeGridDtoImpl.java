package de.symeda.sormas.api.campaign;

import java.io.Serializable;

@SuppressWarnings("serial")

 public class CampaignTreeGridDtoImpl extends CampaignTreeGridDto implements Serializable{
    private static final long serialVersionUID = 1L;


	private Long populationData;
	private Long populationData5_10;
	private Long populationDataTotal;
	private String savedSelectionData;
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData) {
        super(name, id, parentUuid, uuid, levelAssessed);
        this.populationData = populationData;
        this.savedSelectionData = savedSelectionData;
    }
	

//	public CampaignTreeGridDtoImpl(String name, Long populationData, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus) {
//        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
//        this.populationData = populationData;
//        this.savedSelectionData = savedSelectionData;
//    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus, String ageGroup) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
        this.populationData = populationData;
        this.savedSelectionData = savedSelectionData;
    }
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;

        this.savedSelectionData = savedSelectionData;
    }
	
	public CampaignTreeGridDtoImpl(String name, Long populationData, Long populationData5_10, Long id, String parentUuid, String uuid, String levelAssessed, String savedSelectionData, String districtModality, String districtStatus, Long populationDataTotal) {
        super(name, id, parentUuid, uuid, levelAssessed, districtModality, districtStatus);
        this.populationData = populationData;
        this.populationData5_10 = populationData5_10;

        this.savedSelectionData = savedSelectionData;
        this.populationDataTotal = populationData + populationData5_10;
    }
	
	
	 @Override
     public Long getPopulationData() {
         return populationData;
     }
	 
	 @Override
     public Long getPopulationData5_10() {
         return populationData5_10;
     }
	 
	 public Long getPopulationDataTotal() {
         return populationDataTotal;
     }
	 
	 @Override
     public String getSavedData() {
         return savedSelectionData;
     }
	 
}
