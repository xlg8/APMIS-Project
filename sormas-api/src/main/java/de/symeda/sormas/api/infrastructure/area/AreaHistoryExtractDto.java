package de.symeda.sormas.api.infrastructure.area;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import de.symeda.sormas.api.report.CampaignDataExtractDto;

public class AreaHistoryExtractDto implements Serializable, Comparable<AreaHistoryExtractDto> {
	//@NotNull(message = "Please enter valid externalID")
	
	private String uuid_;
	private String name;
	private boolean archived;
	private Long externalId;
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	
	public AreaHistoryExtractDto(String uuid, String name, Boolean archived, Long externalID, LocalDateTime startDate, LocalDateTime endDate) {
	    this.uuid_ = uuid;
	    this.name = name;
	    this.archived = archived;
	    this.externalId = externalID;
	    this.startDate = startDate;
	    this.endDate = endDate;
	}
    public AreaHistoryExtractDto(String string, String string2, Boolean boolean1, long l, LocalDateTime localDateTime, Object object) {
        this.uuid_ = string;
        this.name = string2;
        this.archived = boolean1;
        this.externalId = l;
        this.startDate = localDateTime;
        this.endDate = (object instanceof LocalDateTime) ? (LocalDateTime) object : null;
    }

    
    

	
	public String getUuid_() {
		return uuid_;
	}
	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	public Long getExternalId() {
		return externalId;
	}
	public void setExternalId(Long externalId) {
		this.externalId = externalId;
	}
	public LocalDateTime getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}
	public LocalDateTime getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
	@Override
	public int hashCode() {
		return Objects.hash(uuid_, externalId,  name, archived, startDate, endDate);
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AreaHistoryExtractDto other = (AreaHistoryExtractDto) obj;
		return Objects.equals(uuid_, other.uuid_) 
				&& Objects.equals(externalId, other.externalId)
				&& Objects.equals(name, other.name) && Objects.equals(archived, other.archived)
				&& Objects.equals(startDate, other.startDate) && Objects.equals(endDate, other.endDate)
;
	}
	@Override
	public int compareTo(AreaHistoryExtractDto o) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
