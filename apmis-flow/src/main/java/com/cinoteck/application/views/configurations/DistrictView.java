package com.cinoteck.application.views.configurations;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Districts")
@Route(value = "districts", layout = ConfigurationsView.class)
public class DistrictView extends VerticalLayout {

	private static final long serialVersionUID = 1370022184569877189L;

	DistrictCriteria criteria;
	DistrictIndexDto districtIndexDto;
	DistrictDataProvider districtDataProvider = new DistrictDataProvider();

	ConfigurableFilterDataProvider<DistrictIndexDto, Void, DistrictCriteria> filteredDataProvider;

	Grid<DistrictIndexDto> grid = new Grid<>(DistrictIndexDto.class, false);

	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>("Region");

	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>("Province");

	TextField searchField = new TextField();

	Button resetFilters = new Button("Reset Filters");

	ComboBox<String> riskFilter = new ComboBox<>("Risk");
	ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>("Relevance Status");

	UserProvider currentUser = new UserProvider();

	@SuppressWarnings("deprecation")
	public DistrictView() {

		this.criteria = new DistrictCriteria();
		setSpacing(false);
		setHeightFull();

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(DistrictIndexDto::getAreaname).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true);
		grid.addColumn(DistrictIndexDto::getRegion).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getRegionexternalId).setHeader("PCode").setResizable(true).setSortable(true);
		grid.addColumn(DistrictIndexDto::getName).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getExternalId).setHeader("DCode").setResizable(true).setSortable(true);

		grid.setVisible(true);

		filteredDataProvider = districtDataProvider.withConfigurableFilter();

		grid.setDataProvider(filteredDataProvider);
		addFiltersLayout();

		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditDistrict(event.getValue());
			}
		});
		add(grid);
	}

	public Component addFiltersLayout() {

		HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "1em");
		displayFilters.addClickListener(e -> {
			if (layout.isVisible() == false) {
				layout.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				layout.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		layout.setPadding(false);

		regionFilter.setPlaceholder("All Regions");
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		if (currentUser.getUser().getArea() != null) {
			regionFilter.setValue(currentUser.getUser().getArea());
			filteredDataProvider.setFilter(criteria.area(currentUser.getUser().getArea()));
			provinceFilter.setItems(
					FacadeProvider.getRegionFacade().getAllActiveByArea(currentUser.getUser().getArea().getUuid()));
			regionFilter.setEnabled(false);
		}

		layout.add(searchField);

		layout.add(regionFilter);

		provinceFilter.setPlaceholder("All Provinces");
		if (currentUser.getUser().getRegion() != null) {
			provinceFilter.setValue(currentUser.getUser().getRegion());
			filteredDataProvider.setFilter(criteria.region(currentUser.getUser().getRegion()));
//			criteria.region(currentUser.getUser().getRegion());
			provinceFilter.setEnabled(false);
		}

		layout.add(provinceFilter);

		regionFilter.addValueChangeListener(e -> {

			AreaReferenceDto area = e.getValue();
			criteria.area(area);
			provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));

			filteredDataProvider.setFilter(criteria);
			resetFilters.setVisible(true);
		});

		provinceFilter.addValueChangeListener(e -> {
			filteredDataProvider.setFilter(criteria);
			RegionReferenceDto province = e.getValue();
			criteria.region(province);
			filteredDataProvider.refreshAll();
		});

		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("25%");
		searchField.addValueChangeListener(e -> {
			criteria.nameEpidLike(e.getValue());// nameLike(e.getValue());
			filteredDataProvider.setFilter(criteria);
			resetFilters.setVisible(true);
		});

		
		riskFilter.setItems("Low Risk (LR)", "Medium Risk (MR)", "High Risk (HR)");
		riskFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.risk(e.getValue().toString());
				filteredDataProvider.setFilter(criteria.risk(e.getValue().toString()));
			} else {
				criteria.risk(null);
			}

		});
		layout.add(riskFilter);

		relevanceStatusFilter.setItems(EntityRelevanceStatus.values());
		relevanceStatusFilter.setItemLabelGenerator(status -> {
			if (status == EntityRelevanceStatus.ARCHIVED) {
				return I18nProperties.getCaption(Captions.districtArchivedDistricts);
			} else if (status == EntityRelevanceStatus.ACTIVE) {
				return I18nProperties.getCaption(Captions.districtActiveDistricts);
			} else if (status == EntityRelevanceStatus.ALL) {
				return I18nProperties.getCaption(Captions.districtAllDistricts);
			}
			// Handle other enum values if needed
			return status.toString();
		});
		relevanceStatusFilter.addValueChangeListener(e -> {
			criteria.relevanceStatus((EntityRelevanceStatus) e.getValue());
			filteredDataProvider.setFilter(criteria.relevanceStatus((EntityRelevanceStatus) e.getValue()));
		});
		layout.add(relevanceStatusFilter);
		
		resetFilters.addClassName("resetButton");
		resetFilters.setVisible(false);
		resetFilters.addClickListener(e -> {

		});
//		layout.add(resetFilters);
		
		Button addNew = new Button("Add New Province");
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditDistrict(districtIndexDto);
		});
		layout.add(addNew);

		vlayout.add(displayFilters, layout);
		add(vlayout);
		return vlayout;
	}

	public void clearFilters() {

	}

	public boolean createOrEditDistrict(DistrictIndexDto districtIndexDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();
		
		TextField nameField = new TextField("Name");
		nameField.setValue(districtIndexDto.getName());
		TextField dCodeField = new TextField("DCode");
		ComboBox<RegionReferenceDto> provinceOfDistrict = new ComboBox<>("Province");
//		areaField.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		
		provinceOfDistrict.setItems(districtIndexDto.getRegion());
		provinceOfDistrict.setValue(districtIndexDto.getRegion());
		provinceOfDistrict.isReadOnly();
		provinceOfDistrict.setEnabled(false);
		
		ComboBox<String> risk = new ComboBox<>("Risk");
		risk.setItems("Low Risk (LW)", "Medium Risk (MD)", "High Risk (HR)");
		
		// this can generate null
		dCodeField.setValue(districtIndexDto.getExternalId().toString());
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button("Save");
		Button discardButton = new Button("Discard", e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String code = dCodeField.getValue();

			String uuids = districtIndexDto.getUuid();
			System.out.println(code + "________________" + uuids + "__________________" + name);
			if (name != null && code != null) {

				DistrictDto dce = FacadeProvider.getDistrictFacade().getDistrictByUuid(uuids);

				System.out.println(dce);

				System.out.println(dce.getCreationDate() + " ====== " + dce.getName() + "-----" + dce.getUuid());

				dce.setName(name);
				long rcodeValue = Long.parseLong(code);
				dce.setExternalId(rcodeValue);

				FacadeProvider.getDistrictFacade().save(dce, true);

				// Perform save operation or any desired logic here

				Notification.show("Saved: " + name + " " + code);
				dialog.close();

			} else {
				Notification.show("Not Valid Value: " + name + " " + code);
			}

		});

		dialog.setHeaderTitle("Edit " + districtIndexDto.getName());
		fmr.add(nameField, dCodeField, provinceOfDistrict, risk);
		dialog.add(fmr);
		dialog.getFooter().add(discardButton, saveButton);

//       getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}

}