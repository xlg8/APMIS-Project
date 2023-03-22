package com.cinoteck.application.views.configurations;

import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

public class RegionFilter {
	
	private String searchTerm;

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public boolean test(RegionIndexDto e) {
        boolean matchesFullName = matches(e.getName(), searchTerm);
        return matchesFullName;
    }
    
    private boolean matches(String value, String searchTerm) {
        return searchTerm == null || searchTerm.isEmpty()
                || value.toLowerCase().contains(searchTerm.toLowerCase());
    }
}
