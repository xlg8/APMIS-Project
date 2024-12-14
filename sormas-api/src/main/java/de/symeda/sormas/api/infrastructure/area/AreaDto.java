package de.symeda.sormas.api.infrastructure.area;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

public class AreaDto extends EntityDto {

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
	
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	

  
  //TODO check if you want to leave this here 
	private List<RegionDto> regionData = new ArrayList<>();

	public static AreaDto build() {
		AreaDto area = new AreaDto();
		area.setUuid(DataHelper.createUuid());
		return area;
	}
	
	public AreaDto() {
		
	};

	public AreaDto(@Size(max = 512, message = "textTooLong") String name, Long populationData, Long areaid, String uuid_, Long cxternalID) {
		this.name = name;
		this.populationData = populationData;
		this.areaid = areaid;
		this.uuid_ = uuid_;
		this.externalId = cxternalID;
	}
	
	public AreaDto(String uuid, String name, Boolean archived, Long externalID, LocalDateTime startDate, LocalDateTime endDate) {
	    this.uuid_ = uuid;
	    this.name = name;
	    this.archived = archived;
	    this.externalId = externalID;
	    this.startDate = startDate;
	    this.endDate = endDate;
	}
    public AreaDto(String string, String string2, Boolean boolean1, long l, LocalDateTime localDateTime, Object object) {
        this.uuid_ = string;
        this.name = string2;
        this.archived = boolean1;
        this.externalId = l;
        this.startDate = localDateTime;
        this.endDate = (object instanceof LocalDateTime) ? (LocalDateTime) object : null;
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

//	public Long getPopulationData() {
//		return populationData;
//	}
//
//	public void setPopulationData(Long populationData) {
//		this.populationData = populationData;
//	}


	public Long getAreaid() {
		return areaid;
	}

	public void setAreaid(Long areaid) {
		this.areaid = areaid;
	}

//	public List<RegionDto> getRegionData() {
//		List<RegionDto> regions_ = FacadeProvider.getRegionFacade().getAllActiveAsReferenceAndPopulation(getAreaid(), campignUuid);
//		regionData.addAll(regions_);
//		return regionData;
//	}
//
//	public void setRegionData(List<RegionDto> regionData) {
//		this.regionData = regionData;
//	}
  


	public void setRegionData(List<RegionDto> regionData) {
		this.regionData = regionData;
	}
  

	public String getUuid_() {
		return uuid_;
	}

	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}

//
//	public String getCampignUuid() {
//		return campignUuid;
//	}
//
//	public void setCampignUuid(String campignUuid) {
//		this.campignUuid = campignUuid;
//	}
	
}
