package com.cinoteck.application.views.user;

import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.user.UserDto;

public class UserFilter {
	
	String searchTerm;

	public boolean test(UserDto e) {
		boolean matchesFullName = matches(e.getName(), searchTerm);
		return matchesFullName;
	}
	
//	public void setSearchTerm(String searchTerm) {
//		this.searchTerm = searchTerm;
//	}
	
	private boolean matches(String value, String searchTerm) {
		return searchTerm == null || searchTerm.isEmpty() || value.toLowerCase().contains(searchTerm.toLowerCase());
	}
}
