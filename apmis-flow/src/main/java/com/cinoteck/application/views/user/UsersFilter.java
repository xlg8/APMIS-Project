package com.cinoteck.application.views.user;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import de.symeda.sormas.api.user.UserDto;

public class UsersFilter extends HorizontalLayout{
	
	String searchTerm;
	
	private Button createUserButton;
	private Button exportUsersButton;
	private Button exportRolesButton;
	private Button bulkModeButton;
	private Button leaveBulkModeButton;
	private TextField searchField;
	
	public UsersFilter() {
		add(createUserButton, exportUsersButton, exportRolesButton, bulkModeButton, leaveBulkModeButton, searchField);
		
	}
	
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
