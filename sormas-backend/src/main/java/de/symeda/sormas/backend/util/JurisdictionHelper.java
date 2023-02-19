/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.symeda.sormas.backend.util;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import de.symeda.sormas.api.user.JurisdictionLevel;

public class JurisdictionHelper {

	private JurisdictionHelper() {
	}

	public static JurisdictionLevel getSuperordinateJurisdiction(JurisdictionLevel jurisdition) {
		switch (jurisdition) {
		case NONE:
			return JurisdictionLevel.NONE;
		case NATION:
			return JurisdictionLevel.NONE;
		case REGION:
			return JurisdictionLevel.NATION;
		case DISTRICT:
			return JurisdictionLevel.REGION;
		case COMMUNITY:
		case POINT_OF_ENTRY:
		case HEALTH_FACILITY:
			return JurisdictionLevel.DISTRICT;
		case LABORATORY:
		case EXTERNAL_LABORATORY:
		default:
			return JurisdictionLevel.NONE;
		}
	}

	

	public static Expression<Object> booleanSelector(CriteriaBuilder cb, Predicate jurisdictionPredicate) {
		return cb.selectCase().when(jurisdictionPredicate, cb.literal(true)).otherwise(cb.literal(false));
	}
}
