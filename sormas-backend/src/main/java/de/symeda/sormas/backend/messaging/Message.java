package de.symeda.sormas.backend.messaging;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.UniqueConstraint;

import de.symeda.auditlog.api.Audited;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.infrastructure.area.Area;
import de.symeda.sormas.backend.infrastructure.community.Community;
import de.symeda.sormas.backend.infrastructure.district.District;
import de.symeda.sormas.backend.infrastructure.region.Region;
import de.symeda.sormas.backend.user.User;

@Entity(name = "messages")
public class Message extends AbstractDomainObject{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9087365913720202358L;
	
	public static final String TABLE_NAME = "messages";
	public static final String TABLE_NAME_USERROLES = "users_userroles";
	public static final String TABLE_NAME_USERTYPES = "users_usertypes";
	
	public static final String MESSAGE_CONTENT = "messageContent";
	public static final String USER_TYPE = "userTypes";
	public static final String USER_ROLES = "userRoles";
	public static final String AREA = "area";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String COMMUNITY = "community";
	public static final String CREATED_BY = "creatingUser";
	
	private String messageContent;
	private UserType userTypes;
	private Set<UserRole> userRoles;
	private Area area;
	private Region region;
	private District district;
//	private Set<District> districts;
//	private Set<Community> community;
	private User creatingUser;
	
	@Column(name = "messagecontent", nullable = false)
	public String getMessageContent() {
		return messageContent;
	}
	
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}	
		
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "messages_usertypes",
	joinColumns = @JoinColumn(name = "messages_id", referencedColumnName = Message.ID, nullable = false),
	uniqueConstraints = @UniqueConstraint(columnNames = {
		"messages_id",
		"usertype" }))
	@Column(name = "usertype", nullable = false)
	public UserType getUsertype() {
		return userTypes;
	}

	public void setUsertype(UserType usertype) {
		this.userTypes = usertype;
	}		

	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "messages_userroles",
		joinColumns = @JoinColumn(name = "message_id", referencedColumnName = Message.ID, nullable = false),
		uniqueConstraints = @UniqueConstraint(columnNames = {
			"message_id",
			"userrole" }))
	@Column(name = "userrole", nullable = false)
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	@ManyToOne(cascade = {})
	public Area getArea() {
		return area;
	}
	
	public void setArea(Area area) {
		this.area = area;
	}
	
	@ManyToOne(cascade = {})
	public Region getRegion() {
		return region;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}
	
	@ManyToOne(cascade = {})
	public District getDistrict() {
		return district;
	}
	
	public void setDistrict(District district) {
		this.district = district;
	}
	
//	@ManyToMany(cascade = {})
//	public Set<District> getDistricts() {
//		return districts;
//	}
//
//	public void setDistricts(Set<District> districts) {
//		this.districts = districts;
//	}

//
//	@JoinTable(
//		    name = "messages_community",
//		    joinColumns = @JoinColumn(name = "message_id", referencedColumnName = Message.ID, nullable = false),
//		    inverseJoinColumns = @JoinColumn(name = "community_id", referencedColumnName = Community.ID, nullable = false),
//		    uniqueConstraints = @UniqueConstraint(columnNames = {
//		        "message_id",
//		        "community_id"
//		    })
//		)	
//	@ElementCollection(fetch = FetchType.EAGER)
//	@CollectionTable(name = "messages_community",
//		joinColumns = @JoinColumn(name = "message_id", referencedColumnName = Message.ID, nullable = false),
//		uniqueConstraints = @UniqueConstraint(columnNames = {
//			"message_id",
//			"community_id" }))
//	@ManyToMany(cascade = {})
//	public Set<Community> getCommunity() {
//		return community;
//	}
//	
//	public void setCommunity(Set<Community> community) {
//		this.community = community;
//	}
		
	@ManyToOne
	@JoinColumn(name ="creatinguser_id")
	public User getCreatingUser() {
		return creatingUser;
	}

	public void setCreatingUser(User creatingUser) {
		this.creatingUser = creatingUser;
	}
}
