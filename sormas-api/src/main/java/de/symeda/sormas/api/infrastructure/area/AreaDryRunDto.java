package de.symeda.sormas.api.infrastructure.area;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;
import javax.validation.constraints.Size;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaWithExpReferenceDto;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.FieldConstraints;

public class AreaDryRunDto extends EntityDto {

	public static final String I18N_PREFIX = "Area";
	public static final String NAME = "name";
	public static final String FA_AF = "fa_af";
	public static final String PS_AF = "ps_af";
	public static final String EXTERNAL_ID = "externalId";
	public static final String POPULATION_DATA = "populationData";
	public static final String UUID = "uuid";	

	private static final long serialVersionUID = -6241927331721175673L;

	@Size(max = FieldConstraints.CHARACTER_LIMIT_DEFAULT, message = Validations.textTooLong)
	private String name;
	@Size(max = FieldConstraints.CHARACTER_LIMIT_DEFAULT, message = Validations.textTooLong)
	private String fa_af;
	@Size(max = FieldConstraints.CHARACTER_LIMIT_DEFAULT, message = Validations.textTooLong)
	private String ps_af;

	//@NotNull(message = "Please enter valid externalID")
	private Long externalId;
	private boolean archived;
	private Long populationData;
	private Long areaid;
	private String uuid_;
	private boolean isDryRun;

  
  //TODO check if you want to leave this here 
	private List<RegionDto> regionData = new ArrayList<>();

	public static AreaDryRunDto build() {
		AreaDryRunDto area = new AreaDryRunDto();
		area.setUuid(DataHelper.createUuid());
		return area;
	}
	
	public AreaDryRunDto() {
		
	};

	public AreaDryRunDto(@Size(max = 512, message = "textTooLong") String name, Long populationData, Long areaid, String uuid_, Long cxternalID) {
		this.name = name;
		this.populationData = populationData;
		this.areaid = areaid;
		this.uuid_ = uuid_;
		this.externalId = cxternalID;
	}

	public AreaReferenceDto toReference() {
		return new AreaReferenceDto(getUuid());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFa_af() {
		return fa_af;
	}

	public void setFa_af(String fa_af) {
		this.fa_af = fa_af;
	}

	public String getPs_af() {
		return ps_af;
	}

	public void setPs_af(String ps_af) {
		this.ps_af = ps_af;
	}
	
	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	
	
	
	public boolean isDryRun() {
		return isDryRun;
	}

	public void setDryRun(boolean isDryRun) {
		this.isDryRun = isDryRun;
	}

	public String provideActiveStatus() {
		if(isArchived()) {
			return "Archived";

		}else {
			return "Active";

		}
		
	}



	public Long getAreaid() {
		return areaid;
	}

	public void setAreaid(Long areaid) {
		this.areaid = areaid;
	}


  


	public void setRegionData(List<RegionDto> regionData) {
		this.regionData = regionData;
	}
  

	public String getUuid_() {
		return uuid_;
	}

	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}
	
    public static AreaDryRunDto fromAreaDto(AreaDto areaDto) {
        if (areaDto == null) {
            return null;
        }
        
        AreaDryRunDto areaDryRunDto = new AreaDryRunDto();
        areaDryRunDto.setName(areaDto.getName());
        areaDryRunDto.setFa_af(areaDto.getFa_af());
        areaDryRunDto.setPs_af(areaDto.getPs_af());
        areaDryRunDto.setExternalId(areaDto.getExternalId());
        areaDryRunDto.setArchived(areaDto.isArchived());
//        areaDryRunDto.setPopulationData(areaDto.getPopulationData());
        areaDryRunDto.setAreaid(areaDto.getAreaid());
        areaDryRunDto.setUuid_(areaDto.getUuid());
        areaDryRunDto.setDryRun(true); // Assuming it's always a dry run for this conversion
        
        // Assuming RegionDto is similar in both classes
//        areaDryRunDto.setRegionData(areaDto.getRegionData());
        
        return areaDryRunDto;
    }

	
}
