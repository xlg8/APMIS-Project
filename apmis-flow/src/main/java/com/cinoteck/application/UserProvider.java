/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.cinoteck.application;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.context.SessionScoped;

import com.vaadin.flow.component.UI;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@SessionScoped
public class UserProvider implements Serializable{

	private UserDto user;
	private UserReferenceDto userReference;
	private Set<UserRight> userRights;
	private Set<FormAccess> formAccess;
	private UserType usertype;

	public UserDto getUser() {

		if (user == null) {
			user = FacadeProvider.getUserFacade().getCurrentUser();
		}
		return user;
	}

	public Set<UserRight> getUserRights() {

		if (userRights == null) {
			userRights = FacadeProvider.getUserRoleConfigFacade()
					.getEffectiveUserRights(getUser().getUserRoles().toArray(new UserRole[] {}));
		}
		return userRights;
	}

	public Set<UserRole> getUserRoles() {
		return getUser().getUserRoles();
	}

//	public UserType getUsertype() {
//		if (usertype == null) {
//			usertype = FacadeProvider.getUserRoleConfigFacade().get;
//		}
//		return usertype;
//	}

	public Set<FormAccess> getFormAccess() {
		return getUser().getFormAccess();
	}

	public boolean hasUserRole(UserRole userRole) {
		return getUser().getUserRoles().contains(userRole);
	}

	public boolean hasFormAccess(FormAccess formAccess) {
		return getUser().getFormAccess().contains(formAccess);
	}

	public boolean hasUserType(UserType userType) {
		return getUser().getUsertype().equals(userType);
	}

	/**
	 * Checks if the User possesses any of the specified userRoles
	 */
	public boolean hasAnyUserRole(UserRole... userRoles) {
		Set<UserRole> currentUserRoles = getUser().getUserRoles();
		return Arrays.stream(userRoles).anyMatch(currentUserRoles::contains);
	}

	/**
	 * Checks if the User possesses any of the specified formAccess
	 */
	public boolean hasAnyFormAccess(FormAccess... formAccess) {
		Set<FormAccess> currentFormAccess = getUser().getFormAccess();
		return Arrays.stream(formAccess).anyMatch(currentFormAccess::contains);
	}

	public boolean hasUserRight(UserRight userRight) {
		return getUserRights().contains(userRight);
	}

	public boolean hasAllUserRights(UserRight... userRights) {
		return getUserRights().containsAll(Arrays.asList(userRights));
	}

	public boolean hasNationalJurisdictionLevel() {
		return UserRole.getJurisdictionLevel(getCurrent().getUserRoles()) == JurisdictionLevel.NATION;
	}

	public boolean hasAreaJurisdictionLevel() {
		return UserRole.getJurisdictionLevel(getCurrent().getUserRoles()) == JurisdictionLevel.AREA;
	}

	public boolean hasRegionJurisdictionLevel() {
		return UserRole.getJurisdictionLevel(getCurrent().getUserRoles()) == JurisdictionLevel.REGION;
	}

	public boolean hasDistrictJurisdictionLevel() {
		return UserRole.getJurisdictionLevel(getCurrent().getUserRoles()) == JurisdictionLevel.DISTRICT;
	}
	
	public boolean hasCommunityJurisdictionLevel() {
		return UserRole.getJurisdictionLevel(getCurrent().getUserRoles()) == JurisdictionLevel.COMMUNITY;
	}

	public boolean hasRegion(RegionReferenceDto regionReference) {
		RegionReferenceDto userRegionReference = getCurrent().getUser().getRegion();
		return Objects.equals(userRegionReference, regionReference);
	}

	public UserReferenceDto getUserReference() {

		if (userReference == null) {
			userReference = getUser().toReference();
		}
		return userReference;
	}

	public String getUuid() {
		return getUser().getUuid();
	}

	public String getUserName() {
		return getUser().getName();
	}

	public boolean isCurrentUser(UserDto user) {
		return getUser().equals(user);
	}

	/**
	 * Gets the user to which the current UI belongs. This is automatically defined
	 * when processing requests to the server. In other cases, (e.g. from background
	 * threads), the current UI is not automatically defined.
	 *
	 * @see UI#getCurrent()
	 *
	 * @return the current user instance if available, otherwise <code>null</code>
	 */
	public static UserProvider getCurrent() {

		UI currentUI = UI.getCurrent();
		
		System.out.println(UI.getCurrent());
		
		if (currentUI instanceof HasUserProvider) {
			return ((HasUserProvider) currentUI).getUserProvider();
		}
		return null;
	}

	public interface HasUserProvider {

		UserProvider getUserProvider();
	}
}
