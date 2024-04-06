package de.symeda.sormas.api.messaging;

import java.io.Serializable;
import java.util.Set;

import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.IgnoreForUrl;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

@SuppressWarnings("serial")
public class MessageCriteria extends BaseCriteria implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8454377259026176092L;
	
	private UserRole userRole;
	private UserType userType;
	private AreaReferenceDto area;
	private RegionReferenceDto region;
	private DistrictReferenceDto district;
	private CommunityReferenceDto community;
	private FormAccess formAccess;
	private String freeText;	
	
	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public MessageCriteria userRole(UserRole userRole) {
		this.userRole = userRole;
		return this;
	}

	public UserRole getUserRole() {
		return userRole;
	}
	
	public MessageCriteria area(AreaReferenceDto area) {
		this.area = area;
		return this;
	}
	
	public AreaReferenceDto getArea() {
		return area;
	}

	public RegionReferenceDto getRegion() {
		return region;
	}

	public MessageCriteria region(RegionReferenceDto region) {
		this.region = region;
		return this;
	}

	public MessageCriteria district(DistrictReferenceDto district) {
		this.district = district;
		return this;
	}

	public CommunityReferenceDto getCommunity() {
		return community;
	}
	
	public MessageCriteria community(CommunityReferenceDto community) {
		this.community = community;
		return this;
	}

	public DistrictReferenceDto getDistrict() {
		return district;
	}

	public MessageCriteria formAccess(FormAccess formAccess) {
		this.formAccess = formAccess;
		return this;
	}
	
	public FormAccess getFormAccess() {
		return formAccess;
	}

	public MessageCriteria freeText(String freeText) {
		this.freeText = freeText;
		return this;
	}

	@IgnoreForUrl
	public String getFreeText() {
		return freeText;
	}
	
}
