package de.symeda.sormas.backend.infrastructure.area;

import static de.symeda.sormas.api.utils.FieldConstraints.CHARACTER_LIMIT_DEFAULT;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import de.symeda.sormas.backend.common.InfrastructureAdo;
import de.symeda.sormas.backend.infrastructure.region.Region;

@Entity(name = "areas")
public class Area extends InfrastructureAdo {

	private static final long serialVersionUID = 1076938355128939661L;

	public static final String TABLE_NAME = "areas";
	public static final String REGION = "regions";
	public static final String NAME = "name";
	public static final String FA_AF = "fa_af";
	public static final String PS_AF = "ps_af";
	public static final String EXTERNAL_ID = "externalId";
	public static final String DRY_RUN = "dryrun";


	private String name;
	private String fa_af;
	private String ps_af;
	private List<Region> regions;
	private Long externalId;
	private boolean dryrun;


	@Column(length = CHARACTER_LIMIT_DEFAULT)
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
	
	@OneToMany(mappedBy = Region.AREA, cascade = {}, fetch = FetchType.LAZY) //AreaReferenceDto externalID
	@OrderBy(Region.NAME)
	public List<Region> getRegions() {
		return regions;
	}

	public void setRegions(List<Region> regions) {
		this.regions = regions;
	}

	@NotNull(message="PCode is mandatory")
	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}
	
	
 
	public boolean isDryrun() {
		return dryrun;
	}

	public void setDryrun(boolean dryrun) {
		this.dryrun = dryrun;
	}

	@Override
	public String toString() {
		return getName();
	}
}
