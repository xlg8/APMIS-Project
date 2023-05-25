package com.cinoteck.application.views.dashboard;

import java.util.Objects;

public class CampaignDashboardTotalsReference {

	private final Object key;
	private final String stack;

	public CampaignDashboardTotalsReference(Object key, String stack) {
		this.key = key;
		this.stack = stack;
	}

	public Object getKey() {
		return key;
	}

	public String getStack() {
		return stack;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CampaignDashboardTotalsReference that = (CampaignDashboardTotalsReference) o;
		return Objects.equals(key, that.key) && Objects.equals(stack, that.stack);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, stack);
	}
}
