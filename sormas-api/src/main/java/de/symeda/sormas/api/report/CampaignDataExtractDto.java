package de.symeda.sormas.api.report;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;

import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.ReferenceDto;

public class CampaignDataExtractDto implements Serializable, Comparable<CampaignDataExtractDto> {
//c3.campaignyear, c3."name" as campagin, c2.formname, jd."key", jd.value, a."name" as area, r."name" as region, d."name" as district,
	// c."name" as community, c.clusternumber as clusternumber

	private String campaignyear;
	private String campaign;
	private String formname;
	private String key;
	private String value;
	private String area;
	private String region;
	private String district;
	private String cummunity;
	private Long clusternumber;

	public CampaignDataExtractDto(String campaignyear, String campaign, String formname, String key, String value,
			String area, String region, String district) {
		super();
		this.campaignyear = campaignyear;
		this.campaign = campaign;
		this.formname = formname;
		this.key = key;
		this.value = value;
		this.area = area;
		this.region = region;
		this.district = district;
//		this.cummunity = cummunity;
//		this.clusternumber = clusternumber;
	}
	public CampaignDataExtractDto(String campaignyear, String campaign, String formname, String key, String value,
			String area, String region, String district, String cummunity, Long clusternumber) {
		super();
		this.campaignyear = campaignyear;
		this.campaign = campaign;
		this.formname = formname;
		this.key = key;
		this.value = value;
		this.area = area;
		this.region = region;
		this.district = district;
		this.cummunity = cummunity;
		this.clusternumber = clusternumber;
	}
	
	

	public String getCampaignyear() {
		return campaignyear;
	}

	public void setCampaignyear(String campaignyear) {
		this.campaignyear = campaignyear;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String getFormname() {
		return formname;
	}

	public void setFormname(String formname) {
		this.formname = formname;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCummunity() {
		return cummunity;
	}

	public void setCummunity(String cummunity) {
		this.cummunity = cummunity;
	}

	public Long getClusternumber() {
		return clusternumber;
	}

	public void setClusternumber(Long clusternumber) {
		this.clusternumber = clusternumber;
	}

	@Override
	public int compareTo(CampaignDataExtractDto o) {
	    // Compare item1 first
	    int item1Comparison = ObjectUtils.compare(getCampaign(), o.getCampaign());
	    if (item1Comparison != 0) {
	        return item1Comparison;
	    }

	    // If item1 is equal, compare item2
	    int item2Comparison = ObjectUtils.compare(getFormname(), o.getFormname());
	    if (item2Comparison != 0) {
	        return item2Comparison;
	    }

	    // If item2 is also equal, compare item3
	    int item3Comparison = ObjectUtils.compare(getKey(), o.getKey());
	    if (item3Comparison != 0) {
	        return item3Comparison;
	    }

	    // Add more comparisons for additional getters
	    int additionalComparison = ObjectUtils.compare(getValue(), o.getValue());
	    if (additionalComparison != 0) {
	        return additionalComparison;
	    }

	    // If all comparisons are equal, the objects are equal
	    return 0;
	    
//			return ObjectUtils.compare(hashCode(), o.hashCode());
		
	}

	@Override
	public int hashCode() {
		return Objects.hash(area, campaign, campaignyear, clusternumber, cummunity, district, formname, key, region,
				value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignDataExtractDto other = (CampaignDataExtractDto) obj;
		return Objects.equals(area, other.area) && Objects.equals(campaign, other.campaign)
				&& Objects.equals(campaignyear, other.campaignyear)
				&& Objects.equals(clusternumber, other.clusternumber) && Objects.equals(cummunity, other.cummunity)
				&& Objects.equals(district, other.district) && Objects.equals(formname, other.formname)
				&& Objects.equals(key, other.key) && Objects.equals(region, other.region)
				&& Objects.equals(value, other.value);
	}

}
