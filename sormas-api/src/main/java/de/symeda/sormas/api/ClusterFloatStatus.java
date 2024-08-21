package de.symeda.sormas.api;

import de.symeda.sormas.api.i18n.I18nProperties;

public enum ClusterFloatStatus {

	FLOATING,
	NORMAL;

	public String toString() {
		return I18nProperties.getEnumCaption(this);
	}
}
