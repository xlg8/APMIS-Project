package de.symeda.sormas.api.messaging;

import java.util.Set;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;

public class MessageDto extends EntityDto {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8755227013973393504L;
	public static final String TABLE_NAME = "messages";
	public static final String TABLE_NAME_USERROLES = "messages_userroles";
	public static final String TABLE_NAME_USERTYPES = "messages_usertypes";
	public static final String TABLE_NAME_COMMUNITY = "messages_community";
	
	public static final String MESSAGE_CONTENT = "messageContent";
//	public static final String USER_TYPE = "userTypes";
	public static final String USER_ROLES = "userRoles";
	public static final String FORM_ACCESS = "formAccess";
	public static final String AREA = "area";
	public static final String REGION = "region";
	public static final String DISTRICT = "district";
	public static final String COMMUNITY = "community";
	public static final String COMMUNITY_NOS = "communitynos";
	public static final String CREATED_BY = "creatingUser";
	
	private String title;
	private String messageContent;
//	private UserType userTypes;
	private Set<UserRole> userRoles;
	private Set<FormAccess> formAccess;
	private Set<AreaReferenceDto> area;
	private Set<RegionReferenceDto> region;
	private Set<DistrictReferenceDto> district;			
	private Set<CommunityReferenceDto> community;
	private Set<String> communitynos;
	private UserReferenceDto creatingUser;
	
	public static MessageDto build() {
		MessageDto messageDto = new MessageDto();
		messageDto.setUuid(DataHelper.createUuid());
		return messageDto;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
//	public UserType getUserTypes() {
//		return userTypes;
//	}
//	public void setUserTypes(UserType userTypes) {
//		this.userTypes = userTypes;
//	}
	public Set<UserRole> getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}
	public Set<FormAccess> getFormAccess() {
		return formAccess;
	}

	public void setFormAccess(Set<FormAccess> formAccess) {
		this.formAccess = formAccess;
	}
	public Set<AreaReferenceDto> getArea() {
		return area;
	}
	public void setArea(Set<AreaReferenceDto> area) {
		this.area = area;
	}
	public Set<RegionReferenceDto> getRegion() {
		return region;
	}
	public void setRegion(Set<RegionReferenceDto> region) {
		this.region = region;
	}
	public Set<DistrictReferenceDto> getDistrict() {
		return district;
	}
	public void setDistrict(Set<DistrictReferenceDto> district) {
		this.district = district;
	}
	public Set<CommunityReferenceDto> getCommunity() {
		return community;
	}
	public void setCommunity(Set<CommunityReferenceDto> community) {
		this.community = community;
	}	
	public UserReferenceDto getCreatingUser() {
		return creatingUser;
	}
	public void setCreatingUser(UserReferenceDto creatingUser) {
		this.creatingUser = creatingUser;
	}
	
}
