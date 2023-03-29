package com.cinoteck.application.utils;

import java.util.Collections;
import java.util.Set;

import de.symeda.sormas.api.CountryHelper;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRole;


public class UserUiHelper {

	public static Set<UserRole> getAssignableRoles(Set<UserRole> assignedUserRoles) {

		final Set<UserRole> assignedRoles = assignedUserRoles == null ? Collections.emptySet() : assignedUserRoles;

		Set<UserRole> allRoles = UserRole.getAssignableRoles(UserProvider.getCurrent().getUserRoles());

		if (!FacadeProvider.getConfigFacade().isConfiguredCountry(CountryHelper.COUNTRY_CODE_SWITZERLAND)) {
			allRoles.remove(UserRole.BAG_USER);
		}

		Set<UserRole> enabledUserRoles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();

		allRoles.removeIf(userRole -> !enabledUserRoles.contains(userRole) && !assignedRoles.contains(userRole));

		return allRoles;
	}
	
	
	public static Set<FormAccess> getAssignableForms() {

		Set<FormAccess> allRoles = FormAccess.getAssignableForms();

		return allRoles;
	}
}
