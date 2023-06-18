package com.cinoteck.application.views.campaigndata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
//import com.cinoteck.application.views.campaign.MonthlyExpense.DailyExpenses;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;


import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Campaign Data")
@Route(value = "campaigndata", layout = MainLayout.class)
public class CampaignDataView extends VerticalLayout {

	private Grid<CampaignFormDataIndexDto> grid = new Grid<>(CampaignFormDataIndexDto.class, false);
	private GridListDataView<CampaignFormDataIndexDto> dataView;
	private CampaignFormDataCriteria criteria;
//    private CampaignDataFilter campaignDataFilter = new CampaignDataFilter();

	public CampaignDataView() {
		setHeightFull();
		criteria = new CampaignFormDataCriteria();
		createCampaignDataFilter();
		configureGrid(criteria);
	}

	private void configureGrid(CampaignFormDataCriteria criteria) {
		grid.removeAllColumns();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CampaignFormDataIndexDto.CAMPAIGN).setHeader("Campaign").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM).setHeader("Form").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.AREA).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.RCODE).setHeader("RCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.REGION).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.PCODE).setHeader("PCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.DISTRICT).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.DCODE).setHeader("DCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.COMMUNITY).setHeader("Cluster").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.COMMUNITYNUMBER).setHeader("Cluster Number").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.CCODE).setHeader("CCode").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM_DATE).setHeader("Form Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormDataIndexDto.FORM_TYPE).setHeader("Form Phase").setSortable(true).setResizable(true);

		List<String> filterValues = getFilterValues();
		List<String> jsonColumns = (filterValues);

		for (String column : jsonColumns) {
			grid.addColumn(dto -> getJSONColumnValue(dto, column)).setHeader(column).setSortable(true)
					.setResizable(true);
	        System.out.println("jfyttttyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy" + column);

		}

//		grid.setDataProvider(DataProvider.fromFilteringCallbacks(
//				query -> FacadeProvider.getCampaignFormDataFacade()
//						.getIndexList(criteria, query.getOffset(), query.getLimit(),
//								query.getSortOrders().stream()
//										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
//												sortOrder.getDirection() == SortDirection.ASCENDING))
//										.collect(Collectors.toList()))
//						.stream(),
//				query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria)));
//
//		add(grid);
		grid.setVisible(true);
	    grid.setWidthFull();
	    grid.setHeightFull();
	    grid.setAllRowsVisible(false);

	    List<CampaignFormDataIndexDto> campaigns = FacadeProvider.getCampaignFormDataFacade()
	            .getIndexList(criteria, 1, null, null).stream().collect(Collectors.toList());

	    dataView = grid.setItems(campaigns);

	    add(grid);
	}

	private List<String> getFilterValues() {
		// Replace with your logic to get the filter values based on your filter
		// components
		List<String> filterValues = new ArrayList<>();
		filterValues.add("filter1");
		filterValues.add("filter2");
		return filterValues;
	}

	private List<String> getJSONColumns(List<String> filterValues) {
		// Replace with your logic to retrieve JSON data and extract the necessary
		// column names based on the filter values
		List<String> jsonColumns = new ArrayList<>();
		// Add the necessary column names to the jsonColumns list based on the filter
		// values
		if (filterValues.contains("filter1")) {
			jsonColumns.add("column1");
		}
		if (filterValues.contains("filter2")) {
			jsonColumns.add("column2");
		}
		return jsonColumns;
	}

	private String getJSONColumnValue(CampaignFormDataIndexDto dto, String column) {
	    try {
	        // Get the form values from the CampaignFormDataIndexDto
	        List<CampaignFormDataEntry> formValues = dto.getFormValues();

	        // Find the CampaignFormDataEntry with the matching column name
	        Optional<CampaignFormDataEntry> matchingEntry = formValues.stream()
	                .filter(entry -> entry.getId().equals(column))
	                .findFirst();

	        if (matchingEntry.isPresent()) {
	            // Get the column value from the matching CampaignFormDataEntry
	            Object columnValue = matchingEntry.get().getValue();

	            // Convert the column value to a String and return it
	            return String.valueOf(columnValue);
	        }
	    } catch (Exception e) {
	        // Handle any exceptions that may occur during the column value extraction
	        e.printStackTrace();
	    }

	    // Return an empty string if the column value extraction fails or no matching entry is found
	    return "";
	}


	private boolean shouldAddColumn(CampaignFormDataIndexDto dto, String property) {
	    // Replace with your logic to determine if the column should be added
	    // based on the filter value or any other conditions
	    return dto.toString().contains("readonly");
	}

	private void createCampaignDataFilter() {
		setMargin(true);
		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);

		HorizontalLayout level1Filters = new HorizontalLayout();
		level1Filters.setPadding(false);
		level1Filters.setVisible(false);
		level1Filters.getStyle().set("margin-left", "12px");
		level1Filters.setAlignItems(Alignment.END);

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);
		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.addClickListener(e -> {
			if (!level1Filters.isVisible()) {
				level1Filters.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				level1Filters.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		ComboBox<String> campaignYear = new ComboBox<>();
		ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
		ComboBox<String> campaignPhase = new ComboBox<>();
		ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
		Button newForm = new Button("NEW FORM", new Icon(VaadinIcon.PLUS_CIRCLE));
		Button importData = new Button("IMPORT", new Icon(VaadinIcon.PLUS_CIRCLE));
		Button exportData = new Button("EXPORT", new Icon(VaadinIcon.DOWNLOAD));

		campaignYear.setLabel("Campaign Year");
		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());
		campaignYear.setItems(camYearList);
		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> stf = campaigns.stream()
					.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(stf);
			campaignz.setValue(stf.get(0));
		});

		List<CampaignFormMetaReferenceDto> campaignForms;
		campaignz.setLabel("Campaign");
		campaignz.addValueChangeListener(e -> {
			campaignPhase.setItems(CampaignPhase.values().toString());
			dataView.addFilter(t -> t.getCampaign().equals(campaignz.getValue()));
		});

		campaignPhase.setLabel("Campaign Phase");
		campaignPhase.addValueChangeListener(e -> {
			dataView.addFilter(t -> t.getFormType().equals(campaignPhase.getValue().toLowerCase()));
		});

		campaignFormCombo.setLabel("Form");
		campaignForms = FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences();
		campaignFormCombo.setItems(campaignForms);

		level1Filters.add(campaignYear, campaignz, campaignPhase, campaignFormCombo, newForm, importData, exportData);
		vlayout.add(displayFilters, level1Filters);
		add(vlayout);
	}

	private void setDataProvider() {
		DataProvider<CampaignFormDataIndexDto, CampaignFormDataCriteria> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCampaignFormDataFacade()
								.getIndexList(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()))
								.stream(),
						query -> (int) FacadeProvider.getCampaignFormDataFacade().count(criteria));
		grid.setDataProvider(dataProvider);
	}

	private void configureCampaignDataFilters() {
		// Existing code...
	}
}
