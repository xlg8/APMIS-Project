package de.symeda.sormas.api.infrastructure;

import java.io.Serializable;
import java.util.Date;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.user.UserDto;

public class ConfigurationChangeLogDto extends EntityDto {/**
	 * 
	 */
	private static final long serialVersionUID = -3648919702309257717L;


	public static final String I18N_PREFIX = "configurationchangelog";

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

	public ConfigurationChangeLogDto() {
		
	}
	



	public ConfigurationChangeLogDto(String creatingUser, String action_unit_type, String action_unit_name,
			Long unit_code, String action_logged) {
		super();
		this.creatinguser = creatingUser;
		this.action_unit_type = action_unit_type;
		this.action_unit_name = action_unit_name;
		this.unit_code = unit_code;
		this.action_logged = action_logged;
	}
	
	public ConfigurationChangeLogDto(String creatingUser, String action_unit_type, String action_unit_name,
			Long unit_code, String action_logged, Date changedate,  Date action_date) {
		this.creatinguser = creatingUser;
		this.action_unit_type = action_unit_type;
		this.action_unit_name = action_unit_name;
		this.unit_code = unit_code;
		this.action_logged = action_logged;
		setChangeDate(changedate);
		this.action_date = action_date;

	}




	public String getCreatinguser() {
		return creatinguser;
	}

	public void setCreatinguser(String creatinguser) {
		this.creatinguser = creatinguser;
	}

	public String getAction_unit_name() {
		return action_unit_name;
	}

	public void setAction_unit_name(String action_unit_name) {
		this.action_unit_name = action_unit_name;
	}

	public Long getUnit_code() {
		return unit_code;
	}

	public void setUnit_code(Long unit_code) {
		this.unit_code = unit_code;
	}

	public String getAction_logged() {
		return action_logged;
	}

	public void setAction_logged(String action_logged) {
		this.action_logged = action_logged;
	}

	public String getAction_unit_type() {
		return action_unit_type;
	}

	public void setAction_unit_type(String action_unit_type) {
		this.action_unit_type = action_unit_type;
	}


	public Date getAction_date() {
		return action_date;
	}


	public void setAction_date(Date action_date) {
		this.action_date = action_date;
	}
	
	
	
	
}
