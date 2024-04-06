package de.symeda.sormas.api.campaign;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.FieldConstraints;

public class CampaignTreeGridDtoImpl extends CampaignTreeGridDto {
	
	private Long populationData;
	private Long populationData5_10;

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
	
	
	 @Override
     public Long getPopulationData() {
         return populationData;
     }
	 
	 @Override
     public Long getPopulationData5_10() {
         return populationData5_10;
     }
	 
	 @Override
     public String getSavedData() {
         return savedSelectionData;
     }
	 
}
