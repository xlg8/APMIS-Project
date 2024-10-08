package de.symeda.sormas.api.user;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.utils.IgnoreForUrl;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;

public class UserCriteria extends BaseCriteria implements Serializable {

	private static final long serialVersionUID = 1702083604616047628L;

	private Boolean active;
	private UserRole userRole;
	private Set<UserRole> userRoleSet;
	private AreaReferenceDto area;
	private RegionReferenceDto region;
	private DistrictReferenceDto district;
	private FormAccess formAccess;
	private String freeText;
	
    private Set<UserRole> userRoles;
    
    private Set<String> roleNames;
    
    // Method to set role names for filtering
    public void setRoleNames(Set<String> roleNames) {
        this.roleNames = roleNames;
    }

    // Getter method
    public Set<String> getRoleNames() {
        return this.roleNames;
    }

    public UserCriteria userRolesMulti(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
        return this;
    }


	public UserCriteria active(Boolean active) {
		this.active = active;
		return this;
	}

	public Boolean getActive() {
		return active;
	}

	public UserCriteria userRole(UserRole userRole) {
		this.userRole = userRole;
		return this;
	}
	
	public UserCriteria userRoleSet(Set<UserRole> userRoleSet) {
		
		this.userRoleSet = userRoleSet;
		return this;
	}
	
	public Set<UserRole> getUserRoleSet() {
		return userRoleSet;
	}

	public UserRole getUserRole() {
		return userRole;
	}

	public UserCriteria region(RegionReferenceDto region) {
		this.region = region;
		return this;
	}
	
	public UserCriteria area(AreaReferenceDto area) {
		this.area = area;
		return this;
	}

	public RegionReferenceDto getRegion() {
		return region;
	}
	
	public AreaReferenceDto getArea() {
		return area;
	}
	

	public UserCriteria district(DistrictReferenceDto district) {
		this.district = district;
		return this;
	}

	public DistrictReferenceDto getDistrict() {
		return district;
	}
	
	public UserCriteria formAccess(FormAccess formAccess) {
		this.formAccess = formAccess;
		return this;
	}
	
	public FormAccess getFormAccess() {
		return formAccess;
	}

	public UserCriteria freeText(String freeText) {
		this.freeText = freeText;
		return this;
	}

	@IgnoreForUrl
	public String getFreeText() {
		return freeText;
	}
	
    // Filtering logic based on role names in your data provider
    public boolean matches(UserDto user) {
        if (roleNames == null || roleNames.isEmpty()) {
            return true; // No filtering if no roles are selected
        }

        // Example logic: Check if user has any of the selected role names
        Set<String> userRoleNames = user.getUserRoles().stream()
            .map(UserRole::toString) // Again, use a specific method if needed
            .collect(Collectors.toSet());

        // Check if user's roles match any of the selected role names
        return !Collections.disjoint(userRoleNames, roleNames);
    }
}
