package com.cinoteck.application.utils;

import com.cinoteck.application.UserProvider;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

public class FlowUtil {

	public FlowUtil() {
	}

	public static boolean permittedx(FeatureType feature, UserRight userRight) {
		return (feature == null || !FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(feature))
				&& (userRight == null || UserProvider.getCurrent().hasUserRight(userRight));
	}

	public static boolean permittedx(UserType userType) {
		boolean check = false;
		if (UserProvider.getCurrent().hasUserType(userType)) {
			check = true;
			return check;
		} else {
			return check;
		}
	}
	
	public static boolean permittedx(FormAccess formAccess) {
		boolean check = false;
		if (UserProvider.getCurrent().hasFormAccess(formAccess)) {
			check = true;
			return check;
		} else {
			return check;
		}
	}
	
	public static boolean permittedx(UserRole userrole) {
		boolean check = false;
		if (UserProvider.getCurrent().hasUserRole(userrole)) {
			check = true;
			return check;
		} else {
			return check;
		}
	}

	public static boolean permitted(UserRight userRight) {
		return permittedx(null, userRight);
	}
}
