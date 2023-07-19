package com.cinoteck.application.views.configurations;

import java.util.List;
import java.util.stream.Stream;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

@PageTitle("Province")
@Route(value = "province", layout = ConfigurationsView.class)
public class ProvinceView extends VerticalLayout implements RouterLayout {

	private static final long serialVersionUID = 8159316049907141477L;
	Grid<RegionIndexDto> grid = new Grid<>(RegionIndexDto.class, false);
	List<RegionIndexDto> regions = FacadeProvider.getRegionFacade().getAllRegions();
	GridListDataView<RegionIndexDto> dataView;
	RegionIndexDto regionDto;
	private RegionCriteria criteria;
//	ProvinceDataProvider provinceDataProvider = new ProvinceDataProvider();	
//	ConfigurableFilterDataProvider<RegionIndexDto, Void, RegionCriteria> filteredDataProvider;

	final static TextField regionField = new TextField("Region");
	final static TextField rcodeField = new TextField("RCode");
	final static ComboBox<AreaReferenceDto> area = new ComboBox();
	Binder<RegionIndexDto> binder = new BeanValidationBinder<>(RegionIndexDto.class);
	private Button saveButton;

	UserProvider currentUser = new UserProvider();

	public ProvinceView() {
		setSpacing(false);
		setHeightFull();
		setSizeFull();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(RegionIndexDto::getArea).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(RegionIndexDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true);
		grid.addColumn(RegionIndexDto::getName).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(RegionIndexDto::getExternalId).setHeader("PCode").setSortable(true).setResizable(true);
//		grid.setItemDetailsRenderer(createAreaEditFormRenderer());

		grid.setVisible(true);
//		grid.setAllRowsVisible(true);
//		grid.setHeight("76vh");
		dataView = grid.setItems(regions);
//		filteredDataProvider = provinceDataProvide.withConfigurableFilter());

//		grid.setDataProvider(filteredDataProvider);

		// VerticalLayout layout = new VerticalLayout(searchField, grid);
		// layout.setPadding(false);
		configureProvinceFilters();

		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditProvince(event.getValue());
			}
		});
		add(grid);

	}

	private void saveArea() {
		if (binder.isValid()) {
			RegionIndexDto regionDto = binder.getBean();
			String regionValue = regionField.getValue();
			long rcodeValue = Long.parseLong(rcodeField.getValue());
			AreaReferenceDto areaValue = area.getValue();

			regionDto.setName(regionValue);
			regionDto.setExternalId(rcodeValue);
			regionDto.setArea(areaValue);
//			grid.getDataProvider().refreshItem(regionDto);
		}
	}

	public void setArea(RegionIndexDto regionDto) {
		regionField.setValue(regionDto.getName());
		rcodeField.setValue(String.valueOf(regionDto.getExternalId()));
		area.setValue(regionDto.getArea());
		binder.setBean(regionDto);

	}

	public void configureProvinceFilters() {
//		setMargin(true);
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

		TextField searchField = new TextField();
		searchField.setWidth("30%");
		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> dataView.addFilter(search -> {
			String searchTerm = searchField.getValue().trim();

			if (searchTerm.isEmpty())
				return true;

			boolean matchRegion = String.valueOf(search.getArea().getCaption()).toLowerCase()
					.contains(searchTerm.toLowerCase());
			boolean matchRcode = String.valueOf(search.getAreaexternalId()).toLowerCase()
					.contains(searchTerm.toLowerCase());
			boolean matchProvince = String.valueOf(search.getName()).toLowerCase().contains(searchTerm.toLowerCase());
			boolean matchPCode = String.valueOf(search.getExternalId()).toLowerCase()
					.contains(searchTerm.toLowerCase());
//          boolean matchPosition = String.valueOf(search.getUserPosition()).toLowerCase().contains(searchTerm.toLowerCase());

			return matchRegion || matchRcode || matchProvince || matchPCode;
			// || matchEmail || matchOrganisation || matchPosition;
		}));

		layout.add(searchField);

		ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
		regionFilter.setLabel("Regions");
		regionFilter.setPlaceholder("All Regions");
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		if (currentUser.getUser().getArea() != null) {
			regionFilter.setValue(currentUser.getUser().getArea());
			dataView.addFilter(f -> f.getArea().getCaption().equalsIgnoreCase(regionFilter.getValue().getCaption()));
			regionFilter.setEnabled(false);
		}

		regionFilter.addValueChangeListener(e -> {
			dataView.addFilter(f -> f.getArea().getCaption().equalsIgnoreCase(regionFilter.getValue().getCaption()));
		});

		layout.add(regionFilter);

		Button resetButton = new Button("Reset Filters");
		resetButton.addClassName("resetButton");
		resetButton.addClickListener(e -> {
			searchField.clear();
			if (regionFilter.getValue() != null) {
				regionFilter.clear();
			}
		});

		layout.add(resetButton);

		Button addNew = new Button("Add New Province");
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditProvince(regionDto);
		});
		layout.add(addNew);

		vlayout.add(displayFilters, layout);
		add(vlayout);
	}

	private void reloadGrid() {
		dataView.refreshAll();
	}

	public boolean createOrEditProvince(RegionIndexDto regionDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();
		TextField nameField = new TextField("Name");
		TextField pCodeField = new TextField("PCode");
		ComboBox<AreaReferenceDto> areaField = new ComboBox("Region");
		areaField.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		
		if (regionDto != null) {
			nameField.setValue(regionDto.getName());
			pCodeField.setValue(regionDto.getExternalId().toString());
			areaField.setItems(regionDto.getArea());
			areaField.setValue(regionDto.getArea());
			areaField.setEnabled(false);
		}

		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button("Save");
		Button discardButton = new Button("Discard", e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String code = pCodeField.getValue();

			String uuids = "";
			
			if (regionDto != null) {
				uuids = regionDto.getUuid();
			}
			if (name != null && code != null) {
				RegionDto dce = FacadeProvider.getRegionFacade().getByUuid(uuids);
				if (dce != null) {
				dce.setName(name);
				long rcodeValue = Long.parseLong(code);
				dce.setExternalId(rcodeValue);
				dce.setArea(areaField.getValue());

				FacadeProvider.getRegionFacade().save(dce, true);
				Notification.show("Saved: " + name + " " + code);
				dialog.close();
				grid.getDataProvider().refreshAll();

			} else {
				RegionDto dcex = new RegionDto();
				dcex.setName(name);
				long rcodeValue = Long.parseLong(code);
				dcex.setExternalId(rcodeValue);
				dcex.setArea(areaField.getValue());
				FacadeProvider.getRegionFacade().save(dcex, true);
				Notification.show("Saved New Region: " + name + " " + code);
				dialog.close();
				grid.getDataProvider().refreshAll();
			}
			}
				else {
				Notification.show("Not Valid Value: " + name + " " + code);
			}

		});

//		dialog.setHeaderTitle("Edit " + regionDto.getName());

//		fmr.setResponsiveSteps(
//		        new FormLayout.ResponsiveStep("0", 1),
//		        new FormLayout.ResponsiveStep("600px", 2)
//		);

//		fmr.setColspan(nameField, 0);
//		fmr.setColspan(pCodeField, 0);
//		fmr.setColspan(areaField, 2);
		HorizontalLayout fiels = new HorizontalLayout(nameField, pCodeField, areaField);

		fmr.add(fiels);

		
		if (regionDto == null) {
			dialog.setHeaderTitle("Add New Province");
		} else {
			dialog.setHeaderTitle("Edit " + regionDto.getName());

		}
		dialog.add(fmr);
		dialog.getFooter().add(discardButton, saveButton);
		
//        getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}
}
