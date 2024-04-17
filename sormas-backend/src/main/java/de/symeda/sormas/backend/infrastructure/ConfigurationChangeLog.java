package de.symeda.sormas.backend.infrastructure;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import de.symeda.auditlog.api.Audited;
import de.symeda.sormas.backend.common.CoreAdo;

@Entity(name = "configurationchangelog")
@Audited
public class ConfigurationChangeLog extends CoreAdo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "configurationchangelog";
	
	public static final String AUDIT_USER = "creatinguser";
	public static final String ACTION_UNIT_TYPE = "action_unit_type";
	public static final String ACTION_UNIT_NAME = "action_unit_name";
	public static final String UNIT_CODE = "unit_code";
	public static final String ACTION_LOGGED = "action_logged";
	public static final String ACTION_DATE = "action_date";

	
	private String creatinguser;
	private String action_unit_type;
	private String action_unit_name;
	private Long unit_code;
	private String action_logged;
	private Date action_date;

	
	@Column(name = "creatinguser")
	public String getCreatinguser() {
		return creatinguser;
	}
	public void setCreatinguser(String creatinguser) {
		this.creatinguser = creatinguser;
	}

	@Column(name = "action_unit_type")
	public String getAction_unit_type() {
		return action_unit_type;
	}
	
	public void setAction_unit_type(String action_unit_type) {
		this.action_unit_type = action_unit_type;
	}
	
	@Column(name = "action_unit_name")
	public String getAction_unit_name() {
		return action_unit_name;
	}
	public void setAction_unit_name(String action_unit_name) {
		this.action_unit_name = action_unit_name;
	}
	
	@Column(name = "unit_code")
	public Long getUnit_code() {
		return unit_code;
	}
	public void setUnit_code(Long unit_code) {
		this.unit_code = unit_code;
	}
	
	@Column(name = "action_logged")
	public String getAction_logged() {
		return action_logged;
	}
	public void setAction_logged(String action_logged) {
		this.action_logged = action_logged;
	}
	
	@Column(name = "action_date")
	public Date getAction_date() {
		return action_date;
	}
	public void setAction_date(Date action_date) {
		this.action_date = action_date;
	}
	
	
	


}
