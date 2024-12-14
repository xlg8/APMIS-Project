package de.symeda.sormas.api.campaign.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class CampaignFormMetaHistoryExtractDto implements Serializable, Comparable<CampaignFormMetaHistoryExtractDto> {

	private String uuid;
	private String formname;
	
//	private Map<String, Object> campaignFormElements = new HashMap<>();
//	 private List<CampaignFormElement> campaignFormElements; // To store JSON data
private String campaignFormElements;
	private String formid;
	private LocalDateTime start_date;
	private LocalDateTime end_date;

//	public CampaignFormMetaHistoryExtractDto(String uuid, String formName, Map<String, Object> campaignFormElements,
//			String formId, LocalDateTime startDate, Object endDate) {
//		this.uuid = uuid;
//		this.formname = formName;
//		 this.campaignFormElements = campaignFormElements != null 
//		            ? campaignFormElements 
//		            : new HashMap<>();																											// it's
//																											// never
//																											// null
//		this.formid = formId;
//		this.start_date = startDate;
//		this.end_date = (endDate instanceof LocalDateTime) ? (LocalDateTime) endDate : null;
//	}

//	public CampaignFormMetaHistoryExtractDto(String uuid, String formName, List<CampaignFormElement> campaignFormElements, String formId,
//			LocalDateTime startDate, Object endDate) {
//		this.uuid = uuid;
//		this.formname = formName;
//		this.campaignFormElements = campaignFormElements; // Ensure it's never null
//		this.formid = formId;
//		this.start_date = startDate;
//		this.end_date = (endDate instanceof LocalDateTime) ? (LocalDateTime) endDate : null;
//	}
	
//	 public CampaignFormMetaHistoryExtractDto(String uuid, String formName, 
//	            List<CampaignFormElement> campaignFormElements, // Change type
//	            String formId, LocalDateTime startDate, Object endDate) {
//	        this.uuid = uuid;
//	        this.formname = formName;
//	        this.campaignFormElements = campaignFormElements != null 
//	            ? campaignFormElements 
//	            : new ArrayList<>(); // Ensure never null
//	        this.formid = formId;
//	        this.start_date = startDate;
//	        this.end_date = (endDate instanceof LocalDateTime) 
//	            ? (LocalDateTime) endDate 
//	            : null;
//	    }
	 

	 public CampaignFormMetaHistoryExtractDto(String uuid, String formName, 
	            String campaignFormElements, // Change type
	            String formId, LocalDateTime startDate, Object endDate) {
	        this.uuid = uuid;
	        this.formname = formName;
	        this.campaignFormElements = campaignFormElements;  // Ensure never null
	        this.formid = formId;
	        this.start_date = startDate;
	        this.end_date = (endDate instanceof LocalDateTime) 
	            ? (LocalDateTime) endDate 
	            : null;
	    }


	

    // Getters and setters
    public String getCampaignFormElements() {
        return campaignFormElements;
    }

    public void setCampaignFormElements(String campaignFormElements) {
        this.campaignFormElements = campaignFormElements;
    }

    
    
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getFormname() {
		return formname;
	}

	public void setFormname(String formname) {
		this.formname = formname;
	}

	public String getFormid() {
		return formid;
	}

	public void setFormid(String formid) {
		this.formid = formid;
	}

	public LocalDateTime getStart_date() {
		return start_date;
	}

	public void setStart_date(LocalDateTime start_date) {
		this.start_date = start_date;
	}

	public LocalDateTime getEnd_date() {
		return end_date;
	}

	public void setEnd_date(LocalDateTime end_date) {
		this.end_date = end_date;
	}
//
//	public Map<String, Object> getCampaignFormElements() {
//		return campaignFormElements;
//	}
//
//	public void setCampaignFormElements(Map<String, Object> campaignFormElements) {
//		this.campaignFormElements = campaignFormElements;
//	}
//
//	public String getCampaignFormElementsx() {
//		return campaignFormElementsx;
//	}
//
//	public void setCampaignFormElementsx(String campaignFormElementsx) {
//		this.campaignFormElementsx = campaignFormElementsx;
//	}

	@Override
	public int hashCode() {
		return Objects.hash(campaignFormElements, end_date, formid, formname, start_date, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignFormMetaHistoryExtractDto other = (CampaignFormMetaHistoryExtractDto) obj;
		return Objects.equals(campaignFormElements, other.campaignFormElements)
				&& Objects.equals(end_date, other.end_date) && Objects.equals(formid, other.formid)
				&& Objects.equals(formname, other.formname) && Objects.equals(start_date, other.start_date)
				&& Objects.equals(uuid, other.uuid);
	}

	@Override
	public int compareTo(CampaignFormMetaHistoryExtractDto o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
