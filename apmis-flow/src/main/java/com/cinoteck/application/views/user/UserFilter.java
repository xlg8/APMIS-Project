package com.cinoteck.application.views.user;

import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.user.UserDto;

public class UserFilter {
	
	String searchTerm;
	
	boolean matchUsername; // = String.valueOf(search.getUserName()).toLowerCase().contains(searchTerm.toLowerCase());
	boolean matchName; // = String.valueOf(search.getName()).toLowerCase().contains(searchTerm.toLowerCase());
	boolean matchEmail; // = String.valueOf(search.getUserEmail()).toLowerCase().contains(searchTerm.toLowerCase());
	boolean matchOrganisation; // = String.valueOf(search.getUserOrganisation()).toLowerCase().contains(searchTerm.toLowerCase());
	boolean matchPosition; //= String.valueOf(search.getUserPosition()).toLowerCase().contains(searchTerm.toLowerCase());

	boolean matchActive; // = option.equals("Active") == (s.isActive() == true);
	
	boolean matchRole; // = String.valueOf(s.getUserRoles()).toLowerCase().contains(option.toLowerCase());
	
	public boolean test(UserDto e) {
		boolean matchesFullName = matches(e.getName(), searchTerm);
		return matchesFullName;
	}
	
	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}
	
	private boolean matches(String value, String searchTerm) {
		return searchTerm == null || searchTerm.isEmpty() || value.toLowerCase().contains(searchTerm.toLowerCase());
	}
}
