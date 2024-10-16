package com.cinoteck.application.views.useractivitysummary;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Predicate;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;

import de.symeda.sormas.api.user.UserActivitySummaryDto;

public class ActivityFilteringUtil {

	private final ListDataProvider<UserActivitySummaryDto> dataProvider;
	private final TextField searchField;
	private final DatePicker startDatePicker;
	private final DatePicker endDatePicker;

	private String currentSearchTerm = "";
	private LocalDate currentStartDate;
	private LocalDate currentEndDate;

	private Map<String, SerializablePredicate<UserActivitySummaryDto>> customFilters = new HashMap<>();

	public ActivityFilteringUtil(ListDataProvider<UserActivitySummaryDto> dataProvider, TextField searchField,
			DatePicker startDatePicker, DatePicker endDatePicker) {
		this.dataProvider = dataProvider;
		this.searchField = searchField;
		this.startDatePicker = startDatePicker;
		this.endDatePicker = endDatePicker;

		setupSearchField(dataProvider);
		setupDatePickers(dataProvider);
	}

	private void setupSearchField(ListDataProvider<UserActivitySummaryDto> dataProvider) {
		searchField.setLabel("Search");
		searchField.addClassName("searchField");
		searchField.setPlaceholder("Search Username");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClearButtonVisible(true);
		searchField.setWidth("145px");
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> {
			currentSearchTerm = e.getValue().trim().toLowerCase();
			applyFilters(dataProvider);
		});
	}

	private void setupDatePickers(ListDataProvider<UserActivitySummaryDto> dataProvider) {
		startDatePicker.setLabel("From Date");
		startDatePicker.setPlaceholder("Start Date");
		startDatePicker.setClearButtonVisible(true);

		endDatePicker.setLabel("To Date");
		endDatePicker.setPlaceholder("End Date");
		endDatePicker.setClearButtonVisible(true);

		ValueChangeListener<AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>> dateChangeListener = e -> {
			currentStartDate = startDatePicker.getValue();
			currentEndDate = endDatePicker.getValue();
			applyFilters(dataProvider);
		};

		startDatePicker.addValueChangeListener(dateChangeListener);
		endDatePicker.addValueChangeListener(dateChangeListener);
	}

	private void applyFilters(ListDataProvider<UserActivitySummaryDto> dataProvider) {
		System.out.println(dataProvider + "dataProviderdataProviderdataProviderdataProvider");
		dataProvider.setFilter(userActivity -> {
			boolean passesSearchFilter = currentSearchTerm.isEmpty()
					|| userActivity.getCreatingUser_string().toLowerCase().contains(currentSearchTerm);

			boolean passesDateFilter = true;
			if (currentStartDate != null && currentEndDate != null) {
				LocalDate activityDate = userActivity.getActionDate().toInstant().atZone(ZoneId.systemDefault())
						.toLocalDate();
				passesDateFilter = (activityDate.isEqual(currentStartDate) || activityDate.isAfter(currentStartDate))
						&& (activityDate.isEqual(currentEndDate) || activityDate.isBefore(currentEndDate));
			}

			boolean passesCustomFilters = customFilters.values().stream().allMatch(filter -> filter.test(userActivity));

			return passesSearchFilter && passesDateFilter;
		});
	}

	// Method to clear all filters
	public void clearAllFilters() {
		currentSearchTerm = "";
		currentStartDate = null;
		currentEndDate = null;
		searchField.clear();
		startDatePicker.clear();
		endDatePicker.clear();
		dataProvider.clearFilters();
	}

}
