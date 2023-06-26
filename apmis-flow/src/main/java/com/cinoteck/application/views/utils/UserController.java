package com.cinoteck.application.views.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Page;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserDto;


public class UserController {
	
	
	public void enableAllSelectedItems(Collection<UserDto> selectedRows) {

		if (selectedRows.size() == 0) {
			Notification notification = new Notification();
			notification.show("You have not Selected any User to Enable", 10000, Notification.Position.MIDDLE);
//			new Notification(I18nProperties.getString(Strings.headingNoUsersSelected),
//					I18nProperties.getString(Strings.messageNoUsersSelected), Notification.Type.WARNING_MESSAGE, false)
//					.show(Page.getCurrent());
		} else {
//			Notification.show(I18nProperties.getString(Strings.headingConfirmEnabling),
//					new com.vaadin.flow.component.html.Label(String.format(I18nProperties.getString(Strings.confirmationEnableUsers),
//							selectedRows.size())),
//					I18nProperties.getString(Strings.yes), I18nProperties.getString(Strings.no), null, confirmed -> {
//						if (!confirmed) {
//							return;
//						}
//
						List<String> uuids = selectedRows.stream().map(UserDto::getUuid).collect(Collectors.toList());
						FacadeProvider.getUserFacade().enableUsers(uuids);
						
						Notification notification = new Notification();
						notification.show("You have Enabled " + selectedRows.size() + "Users", 10000, Notification.Position.MIDDLE);
//						callback.run();
//						new Notification(I18nProperties.getString(Strings.headingUsersEnabled),
//								I18nProperties.getString(Strings.messageUsersEnabled),
//								Notification.Type.HUMANIZED_MESSAGE, false).show(Page.getCurrent());
//					});
		}
	}
	
	
	public void disableAllSelectedItems(Collection<UserDto> selectedRows) {
		  
		if (selectedRows.size() == 0) {
			Notification notification = new Notification();
			notification.show("You have not Selected any User to Disable", 10000, Notification.Position.MIDDLE);
		} else {
//			Notification(I18nProperties.getString(Strings.headingConfirmDisabling),
//					new com.vaadin.flow.component.html.Label(String.format(I18nProperties.getString(Strings.confirmationDisableUsers),
//							selectedRows.size())),
//					I18nProperties.getString(Strings.yes), I18nProperties.getString(Strings.no), null, confirmed -> {
//						if (!confirmed) {
//							return;
//						}

						List<String> uuids = selectedRows.stream().map(UserDto::getUuid).collect(Collectors.toList());
						FacadeProvider.getUserFacade().disableUsers(uuids);
						
						Notification notification = new Notification();
						notification.show("You have Disabled " + selectedRows.size() + "Users", 10000, Notification.Position.MIDDLE);
//						callback.run();
//						new Notification(I18nProperties.getString(Strings.headingUsersDisabled),
//								I18nProperties.getString(Strings.messageUsersDisabled),
//								Notification.Type.HUMANIZED_MESSAGE, false).show(Page.getCurrent());
//					});
		}
	}

}
