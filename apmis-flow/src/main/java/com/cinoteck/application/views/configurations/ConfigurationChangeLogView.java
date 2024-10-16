package com.cinoteck.application.views.configurations;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogCriteria;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;

@PageTitle("APMIS-Configuration Change Log")
@Route(value = "config-change-log", layout = ConfigurationsView.class)
public class ConfigurationChangeLogView extends VerticalLayout {

	private static final long serialVersionUID = 5091856954264511639L;
//	private GridListDataView<CommunityDto> dataView;

	private Grid<ConfigurationChangeLogDto> grid = new Grid<>(ConfigurationChangeLogDto.class, false);
	TextField searchField = new TextField();
	ComboBox<String> uintTypeCombo = new ComboBox<String>();
	ComboBox<String> actionTypeCombo = new ComboBox<String>();
	ConfigurationChangeLogCriteria criteria = new ConfigurationChangeLogCriteria();
	ListDataProvider<ConfigurationChangeLogDto> dataProvider;
	private GridListDataView<ConfigurationChangeLogDto> dataView;

	@SuppressWarnings("deprecation")
	public ConfigurationChangeLogView() {
		setSizeFull();
		setHeightFull();
		addFilters();
		configureConfigActivityGrid();

	}

	public Component addFilters() {

		HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);
		layout.setVisible(true);
		layout.setAlignItems(Alignment.END);

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);
		vlayout.getStyle().set("margin-left", "12px");

		vlayout.setAlignItems(Alignment.END);

		Button resetFilters = new Button(I18nProperties.getCaption(Captions.resetFilters));

		searchField.addClassName("filterBar");
		searchField.setPlaceholder(I18nProperties.getCaption(Captions.actionSearch));
		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setClearButtonVisible(true);

		uintTypeCombo.setLabel("Unit Type");
		uintTypeCombo.setItems("Region", "Province", "District", "Cluster");
		uintTypeCombo.setClearButtonVisible(true);
		uintTypeCombo.setClassName("col-sm-6, col-xs-6");

		actionTypeCombo.setLabel("Action Type");
		actionTypeCombo.setItems("Archive/De-Archive", "Create", "Edit", "Import");
		actionTypeCombo.setClearButtonVisible(true);
		actionTypeCombo.setClassName("col-sm-6, col-xs-6");

//		actionTypeCombo.addValueChangeListener(e -> {
//		    String searchTerm = actionTypeCombo.getValue();
//		    System.out.println(searchTerm + "searchTermsearchTermsearchTermsearchTerm");
//		    dataView.refreshAll(); // Refresh the data view to apply the filter changes
//		    dataView.setFilter(ex -> {
//		        if (searchTerm == null || searchTerm.isEmpty()) {
//		            return true;
//		        } else if (searchTerm.equals("Archive/De-Archive")) {
//		            return String.valueOf(ex.getAction_logged()).toLowerCase().contains("archive");
//		        } else {
//		            return String.valueOf(ex.getAction_logged()).toLowerCase().contains(searchTerm.toLowerCase());
//		        }
//		    });
//		});
//		
//		// Add a value change listener to the combo box
//		uintTypeCombo.addValueChangeListener(e -> {
//		    String searchTerm = uintTypeCombo.getValue(); // Retrieve the selected value from the combo box
//		        // Add a filter to the data view based on the selected search term
//		        dataView.refreshAll();
//
//		        dataView.setFilter(ex -> {
//		        	if (searchTerm == null || searchTerm.isEmpty()) {
//			            return true;
//			        }else {
//			        	String unitType = String.valueOf(ex.getAction_unit_type()).toLowerCase();
//			            return unitType.contains(searchTerm.toLowerCase());
//			        }
//		            
//		        });
//		    // Refresh the grid to reflect the changes in the data view
//		    grid.getDataProvider().refreshAll();
//		});
		// Add a value change listener to the action type combo box
		
		// Add a value change listener to the unit type combo box
		uintTypeCombo.addValueChangeListener(e -> {
		    String actionTypeSearchTerm = actionTypeCombo.getValue(); // Retrieve the selected value from the action type combo box
		    String unitTypeSearchTerm = uintTypeCombo.getValue(); // Retrieve the selected value from the unit type combo box

		    // Add a filter to the data view based on both search terms
		    dataView.setFilter(ex -> {
		        // Check if the action type search term is empty or matches the entry
		        boolean matchesActionType = actionTypeSearchTerm == null || actionTypeSearchTerm.isEmpty() ||
		                actionTypeSearchTerm.equals("Archive/De-Archive") && String.valueOf(ex.getAction_logged()).toLowerCase().contains("archive") ||
		                String.valueOf(ex.getAction_logged()).toLowerCase().contains(actionTypeSearchTerm.toLowerCase());

		        // Check if the unit type search term is empty or matches the entry
		        boolean matchesUnitType = unitTypeSearchTerm == null || unitTypeSearchTerm.isEmpty() ||
		                String.valueOf(ex.getAction_unit_type()).toLowerCase().contains(unitTypeSearchTerm.toLowerCase());

		        // Return true only if both filters match
		        return matchesActionType && matchesUnitType;
		    });

		    // Refresh the grid to reflect the changes in the data view
		    grid.getDataProvider().refreshAll();
		});

		
		actionTypeCombo.addValueChangeListener(e -> {
		    String actionTypeSearchTerm = actionTypeCombo.getValue(); // Retrieve the selected value from the action type combo box
		    String unitTypeSearchTerm = uintTypeCombo.getValue(); // Retrieve the selected value from the unit type combo box

		    // Add a filter to the data view based on both search terms
		    dataView.setFilter(ex -> {
		        // Check if the action type search term is empty or matches the entry
		        boolean matchesActionType = actionTypeSearchTerm == null || actionTypeSearchTerm.isEmpty() ||
		                actionTypeSearchTerm.equals("Archive/De-Archive") && String.valueOf(ex.getAction_logged()).toLowerCase().contains("archive") ||
		                String.valueOf(ex.getAction_logged()).toLowerCase().contains(actionTypeSearchTerm.toLowerCase());

		        // Check if the unit type search term is empty or matches the entry
		        boolean matchesUnitType = unitTypeSearchTerm == null || unitTypeSearchTerm.isEmpty() ||
		                String.valueOf(ex.getAction_unit_type()).toLowerCase().contains(unitTypeSearchTerm.toLowerCase());

		        // Return true only if both filters match
		        return matchesActionType && matchesUnitType;
		    });

		    // Refresh the grid to reflect the changes in the data view
		    grid.getDataProvider().refreshAll();
		});



//		uintTypeCombo.addValueChangeListener(e -> {
//		    String searchTerm = e.getValue(); // Retrieve the selected value from the combo box
//
//		    // If the search term is empty, return true to include all items
//		    if (searchTerm.isEmpty()) {
//		        dataView.removeFilters(); // Clear any existing filters
//		        return;
//		    }
//
//		    // Add a filter to the data view based on the selected search term
//		    dataView.addFilter(ex -> {
//		        String unitType = String.valueOf(ex.getAction_unit_type()).toLowerCase();
//		        return unitType.contains(searchTerm.toLowerCase());
//		    });
//		});

//		uintTypeCombo.addValueChangeListener(e -> {
//			dataView.addFilter(ex -> {
//				String searchTerm = uintTypeCombo.getValue();
//
//				if (searchTerm.isEmpty()) {
//					return true;
//				} else {
//					boolean matchesUnitType = String.valueOf(ex.getAction_unit_type()).toLowerCase()
//							.contains(searchTerm.toLowerCase());
//					return matchesUnitType;
//
//				}
//
//			});
//
//		});

		searchField.addValueChangeListener(e -> dataView.addFilter(search -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty()) {
				return true;
			} else {
				boolean matchesUnitName = String.valueOf(search.getAction_unit_name()).toLowerCase()
						.contains(searchTerm.toLowerCase());
				if (matchesUnitName) {
					return true; // If there's a match, return true
				}

				boolean matchesUintCode = Long.toString(search.getUnit_code()).contains(searchTerm);
				return matchesUintCode; // Return the result of this check
			}
		})

		);

		layout.add(searchField, uintTypeCombo, actionTypeCombo);
		vlayout.setWidth("99%");
		vlayout.add(layout);
		vlayout.getStyle().set("margin-right", "0.5rem");

		add(vlayout);

		return vlayout;

	}

	private void refreshGridData() {
		dataProvider = DataProvider.fromStream(
				FacadeProvider.getUserFacade().getUsersConfigurationChangeLog(criteria, null, null, null).stream());
		dataView = grid.setItems(dataProvider);
//		dataView = grid.setItems(dataProvider);
//		itemCount = dataProvider.getItems().size();
//
//		String newText = I18nProperties.getCaption(Captions.rows) + itemCount;
//		countRowItems.setText(newText);
//		countRowItems.setId("rowCount");

//		dataView = grid.setItems(dataProvider);
	}

	public void configureConfigActivityGrid() {
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setHeightFull();
		grid.setColumnReorderingAllowed(true);

		TextRenderer<ConfigurationChangeLogDto> dateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getAction_date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			if (timestamp != null) {
				return dateFormat.format(timestamp);

			} else {
				return "";
			}
		});

		grid.addColumn(dateRenderer).setHeader(I18nProperties.getCaption("Date")).setSortable(true)
		.setComparator(Comparator.comparing(ConfigurationChangeLogDto::getAction_date)).setResizable(true);

		grid.addColumn(ConfigurationChangeLogDto.AUDIT_USER).setHeader(I18nProperties.getCaption("Username"))
		.setSortable(true).setResizable(true);		
		

		grid.addColumn(ConfigurationChangeLogDto.ACTION_UNIT_TYPE).setHeader(I18nProperties.getCaption("Unit Type"))
				.setSortable(true).setResizable(true);
		
	
		grid.addColumn(ConfigurationChangeLogDto.ACTION_UNIT_NAME).setHeader(I18nProperties.getCaption("Unit Name"))
				.setSortable(true).setResizable(true);
		
		grid.addColumn(ConfigurationChangeLogDto.UNIT_CODE).setHeader(I18nProperties.getCaption("Unit Code"))
				.setSortable(true).setResizable(true);

		grid.addColumn(ConfigurationChangeLogDto.ACTION_LOGGED).setHeader(I18nProperties.getCaption("Action"))
				.setSortable(true).setResizable(true);

		dataProvider = DataProvider.fromStream(
				FacadeProvider.getUserFacade().getUsersConfigurationChangeLog(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);

		add(grid);
	}

}