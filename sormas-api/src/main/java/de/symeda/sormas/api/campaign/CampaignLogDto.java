package de.symeda.sormas.api.campaign;

import java.io.Serializable;
import java.util.Date;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.user.UserDto;

public class CampaignLogDto extends EntityDto {// implements Serializable, Cloneable {

	private static final long serialVersionUID = 8301363182762462920L;

	public static final String I18N_PREFIX = "AuditLog";

	public static final String NAME = "campaign";
	public static final String AUDIT_USER = "creatingUser";
	public static final String ACTION = "action";
	public static final String ACTION_DATE = "actionDate";

	
	private CampaignDto campaign;
	private UserDto creatingUser;
	private String action;
	private Date actionDate;
	private String campaign_string;
	private String creatingUser_string;
	
	public CampaignLogDto() {
		
	}
	
	public CampaignLogDto(String action, Date actionDate, String campaign_string, String creatingUser_string) {
		super();
		this.action = action;
		this.actionDate = actionDate;
		this.campaign_string = campaign_string;
		this.creatingUser_string = creatingUser_string;
	}
	
	public CampaignDto getCampaign() {
		return campaign;
	}
	public void setCampaign(CampaignDto campaign) {
		this.campaign = campaign;
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
	public String getCampaign_string() {
		return campaign_string;
	}
	public void setCampaign_string(String campaign_string) {
		this.campaign_string = campaign_string;
	}
	public String getCreatingUser_string() {
		return creatingUser_string;
	}
	public void setCreatingUser_string(String creatingUser_string) {
		this.creatingUser_string = creatingUser_string;
	}
	


}
