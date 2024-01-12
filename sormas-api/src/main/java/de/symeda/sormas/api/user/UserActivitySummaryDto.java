package de.symeda.sormas.api.user;

import java.io.Serializable;
import java.util.Date;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.user.UserDto;

public class UserActivitySummaryDto extends EntityDto {/**
	 * 
	 */
	private static final long serialVersionUID = -3648919702309257717L;

/**
	 * 
	 */

// implements Serializable, Cloneable {


	public static final String I18N_PREFIX = "UserActivitySummary";

	public static final String ACTION_MODULE = "actionModule";
	public static final String AUDIT_USER = "creatingUser";
	public static final String ACTION = "action";
	public static final String ACTION_DATE = "actionDate";
	public static final String ACTION_logged = "action_logged";



	
	private String actionModule;
	private UserDto creatingUser;
	private String action;
	private Date actionDate;
//	private String campaign_string;
	private String creatingUser_string;
	private String action_logged;
	private Date creationdate;
	
	public UserActivitySummaryDto() {
		
	}
	
	public UserActivitySummaryDto(String action_logged, String actionModule, String creatingUser_string ,  Date actionDate) {
		super();
		this.action_logged = action_logged;
		this.actionDate = actionDate;
		this.actionModule = actionModule;
		this.creatingUser_string = creatingUser_string;
	}
	
	
	public String getActionModule() {
		return actionModule;
	}

	public void setActionModule(String actionModule) {
		this.actionModule = actionModule;
	}

	public UserDto getCreatingUser() {
		return creatingUser;
	}
	public void setCreatingUser(UserDto userDto) {
		this.creatingUser = userDto;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Date getActionDate() {
		return actionDate;
	}
	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	public String getCreatingUser_string() {
		return creatingUser_string;
	}
	public void setCreatingUser_string(String creatingUser_string) {
		this.creatingUser_string = creatingUser_string;
	}

	public String getAction_logged() {
		return action_logged;
	}

	public void setAction_logged(String action_logged) {
		this.action_logged = action_logged;
	}
	


}
