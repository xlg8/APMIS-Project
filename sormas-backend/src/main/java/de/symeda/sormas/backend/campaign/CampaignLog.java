package de.symeda.sormas.backend.campaign;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import de.symeda.auditlog.api.Audited;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.user.User;

@Entity(name = "campaignlog")
public class CampaignLog extends CoreAdo {

	public static final String TABLE_NAME = "CampaignLog";

	public static final String NAME = "campaign";
	public static final String AUDIT_USER = "creatingUser";
	public static final String ACTION = "action";
	public static final String ACTION_DATE = "actionDate";

	
	private Campaign campaign;
	private User creatingUser;
	private String action;
	private Date actionDate;
	private Long id;
	
	
	@ManyToOne
	@JoinColumn
	public User getCreatingUser() {
		return creatingUser;
	}

	public void setCreatingUser(User creatingUser) {
		this.creatingUser = creatingUser;
	}

	@ManyToOne
	@JoinColumn
	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
	
	@Column(name = "action_logged")
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	@Column(name = "lastupdated")
	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}
	
	
}
