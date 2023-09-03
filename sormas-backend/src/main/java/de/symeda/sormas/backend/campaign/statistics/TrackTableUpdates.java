package de.symeda.sormas.backend.campaign.statistics;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


@Entity(name = "tracktableupdates")
public class TrackTableUpdates implements Serializable {
	
	private static final long serialVersionUID = 1493230294225573133L;
	
	public static final String TABLE_NAME = "table_name";
	public static final String LASTUPDATED = "last_updated";
	public static final String isLOCKED = "islocked";
	
	
	private String table_name;
    private LocalDateTime last_updated;
    private boolean islocked;
    
    @Id
    @Column
	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	
	@Column
	public LocalDateTime getLast_updated() {
		return last_updated;
	}
	public void setLast_updated(LocalDateTime last_updated) {
		this.last_updated = last_updated;
	}
	
	@Column
	public boolean isIslocked() {
		return islocked;
	}
	public void setIslocked(boolean islocked) {
		this.islocked = islocked;
	}
	
	
	
	
	



}
