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
package com.cinoteck.application.views.user;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserRole.UserRoleValidationException;

import java.util.Collection;

public final class UserRolesValidator implements Validator<Collection<UserRole>> {

	@Override
	public ValidationResult apply(Collection<UserRole> value, ValueContext context) {
		try {
			System.out.println(value + "Value collection ");
			if (value.size() == 1 && value.contains(UserRole.PUBLISH_USER)) {
				return ValidationResult.error("Publish User Cannot Be Selected as a standalone role");
			} else {
				UserRole.validate(value);
				return ValidationResult.ok();
			}

		} catch (UserRoleValidationException e) {
//        	Notification.show(e.getMessage());
			return ValidationResult.error(e.getMessage());
		}
	}
}
