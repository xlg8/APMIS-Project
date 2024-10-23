package com.cinoteck.application.views.useractivitysummary;

	import com.vaadin.flow.data.provider.ListDataProvider;
	import com.vaadin.flow.function.SerializablePredicate;

import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.user.UserActivitySummaryDto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
	import java.util.Map;

	public class ExtendedctivityFilteringUtil {
	    private ListDataProvider<UserActivitySummaryDto> dataProvider;
	    private String currentSearchTerm = "";
	    private LocalDate currentStartDate;
	    private LocalDate currentEndDate;
	    private String campaignYear;
	    private CampaignReferenceDto selectedCampaign;
	    private CampaignPhase campaignPhase;
	    private CampaignFormMetaReferenceDto selectedCampaignForm;
	    private Map<String, SerializablePredicate<UserActivitySummaryDto>> customFilters = new HashMap<>();

	    public ExtendedctivityFilteringUtil(ListDataProvider<UserActivitySummaryDto> dataProvider) {
	        this.dataProvider = dataProvider;
	        if (this.dataProvider == null) {
	            throw new IllegalArgumentException("DataProvider cannot be null");
	        }
	    }

	    public void setSearchTerm(String searchTerm) {
	        this.currentSearchTerm = searchTerm != null ? searchTerm.toLowerCase() : "";
	        applyFilters();
	    }

	    public void setDateRange(LocalDate startDate, LocalDate endDate) {
	        this.currentStartDate = startDate;
	        this.currentEndDate = endDate;
	        applyFilters();
	    }

	    public void setCampaignYear(String year) {
	        this.campaignYear = year;
	        applyFilters();
	    }

	    public void setSelectedCampaign(CampaignReferenceDto campaign) {
	        this.selectedCampaign = campaign;
	        applyFilters();
	    }

	    public void setCampaignPhase(CampaignPhase phase) {
	        this.campaignPhase = phase;
	        applyFilters();
	    }

	    public void setSelectedCampaignForm(CampaignFormMetaReferenceDto form) {
	        this.selectedCampaignForm = form;
	        applyFilters();
	    }

	    public void addCustomFilter(String filterName, SerializablePredicate<UserActivitySummaryDto> filter) {
	        if (filter != null) {
	            customFilters.put(filterName, filter);
	            applyFilters();
	        }
	    }

	    public void removeCustomFilter(String filterName) {
	        customFilters.remove(filterName);
	        applyFilters();
	    }

	    public void clearAllFilters() {
	        currentSearchTerm = "";
	        currentStartDate = null;
	        currentEndDate = null;
	        campaignYear = null;
	        selectedCampaign = null;
	        campaignPhase = null;
	        selectedCampaignForm = null;
	        customFilters.clear();
	        applyFilters();
	    }

	    private void applyFilters() {
	        dataProvider.setFilter(userActivity -> {
	            if (userActivity == null) {
	                return false;
	            }

	            boolean passesSearchFilter = currentSearchTerm.isEmpty() ||
	                (userActivity.getCreatingUser_string() != null &&
	                 userActivity.getCreatingUser_string().toLowerCase().contains(currentSearchTerm));

	            boolean passesDateFilter = true;
	            if (currentStartDate != null && currentEndDate != null && userActivity.getActionDate() != null) {
	                LocalDate activityDate = userActivity.getActionDate().toInstant()
	                    .atZone(ZoneId.systemDefault()).toLocalDate();
	                passesDateFilter = (activityDate.isEqual(currentStartDate) || activityDate.isAfter(currentStartDate))
	                    && (activityDate.isEqual(currentEndDate) || activityDate.isBefore(currentEndDate));
	            }

//	            boolean passesCampaignYearFilter = campaignYear == null || 
//	                (userActivity.getCampaignYear() != null && userActivity.getCampaignYear().equals(campaignYear));
//
//	            boolean passesCampaignFilter = selectedCampaign == null || 
//	                (userActivity.getCampaign() != null && userActivity.getCampaign().equals(selectedCampaign));
//
//	            boolean passesCampaignPhaseFilter = campaignPhase == null || 
//	                (userActivity.getCampaignPhase() != null && userActivity.getCampaignPhase().equals(campaignPhase));
//
//	            boolean passesCampaignFormFilter = selectedCampaignForm == null || 
//	                (userActivity.getCampaignForm() != null && userActivity.getCampaignForm().equals(selectedCampaignForm));

	            boolean passesCustomFilters = customFilters.isEmpty() || customFilters.values().stream()
	                .allMatch(filter -> filter.test(userActivity));

	            return passesSearchFilter && passesDateFilter
//	            		&& passesCampaignYearFilter && 
//	                   passesCampaignFilter && passesCampaignPhaseFilter && passesCampaignFormFilter 
	                   && 
	                   passesCustomFilters;
	        });
	    }
	}

