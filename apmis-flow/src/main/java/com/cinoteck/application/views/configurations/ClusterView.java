package com.cinoteck.application.views.configurations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
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
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Clusters")
@Route(value = "clusters", layout = ConfigurationsView.class)
public class ClusterView extends Div {

	private static final long serialVersionUID = 5091856954264511639L;
//	private GridListDataView<CommunityDto> dataView;
	private CommunityCriteriaNew criteria;

	ClusterDataProvider clusterDataProvider = new ClusterDataProvider();
	CommunityDto communityDto;
	ConfigurableFilterDataProvider<CommunityDto, Void, CommunityCriteriaNew> filteredDataProvider;

	Grid<CommunityDto> grid = new Grid<>(CommunityDto.class, false);
	Anchor anchor = new Anchor("", "Export");
	UserProvider currentUser = new UserProvider();

	public ClusterView() {
		this.criteria = new CommunityCriteriaNew();
		setHeightFull();
		// List<CommunityDto> clusters =
		// FacadeProvider.getCommunityFacade().getAllCommunities();
		// GridLazyDataView<CommunityDto> dataView;// = grid.setItems(clusters);

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(CommunityDto::getAreaname).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getRegion).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getRegionexternalId).setHeader("PCode").setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getDistrict).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getDistrictexternalId).setHeader("DCode").setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getName).setHeader("Cluster").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getExternalId).setHeader("CCode").setResizable(true).setSortable(true);

		grid.setVisible(true);
		filteredDataProvider = clusterDataProvider.withConfigurableFilter();

		grid.setDataProvider(filteredDataProvider);
		addFilters();
		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				createOrEditCluster(event.getValue());
			}
		});

		add(grid);
		
		GridExporter<CommunityDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Users");
		exporter.setFileName("APMIS_Regions" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));
		
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("exportCluster");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());
	}

	// TODO: Hide the filter bar on smaller screens
	public Component addFilters() {

		HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);
		
		HorizontalLayout relevancelayout = new HorizontalLayout();
		relevancelayout.setPadding(false);
		relevancelayout.setVisible(false);
		relevancelayout.setAlignItems(Alignment.END);
		relevancelayout.setJustifyContentMode(JustifyContentMode.END );
		relevancelayout.setWidth("54%");


		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "1em");
		displayFilters.addClickListener(e -> {
			if (layout.isVisible() == false) {
				layout.setVisible(true);
				relevancelayout.setVisible(true);

				displayFilters.setText("Hide Filters");
			} else {
				layout.setVisible(false);
				relevancelayout.setVisible(false);

				displayFilters.setText("Show Filters");
			}
		});

		layout.setPadding(false);

		TextField searchField = new TextField();
		ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>("Region");
		ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>("Province");
		ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>("District");
		Button resetFilters = new Button("Reset Filters");
		ComboBox<EntityRelevanceStatus> relevanceStatusFilter = new ComboBox<>("Relevance Status");

		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		Icon searchIcon = new Icon(VaadinIcon.SEARCH);
		searchIcon.getStyle().set("color", "#0D6938");
		searchField.setPrefixComponent(searchIcon);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.setWidth("25%");
		layout.add(searchField);

		regionFilter.setPlaceholder("All Regions");
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		if (currentUser.getUser().getArea() != null) {
			regionFilter.setValue(currentUser.getUser().getArea());
			filteredDataProvider.setFilter(criteria.area(currentUser.getUser().getArea()));
			provinceFilter.setItems(
					FacadeProvider.getRegionFacade().getAllActiveByArea(currentUser.getUser().getArea().getUuid()));
			regionFilter.setEnabled(false);
		}

		layout.add(regionFilter);

		provinceFilter.setPlaceholder("All Provinces");
//		provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		if (currentUser.getUser().getRegion() != null) {
			provinceFilter.setValue(currentUser.getUser().getRegion());
			filteredDataProvider.setFilter(criteria.region(currentUser.getUser().getRegion()));
			districtFilter.setItems(FacadeProvider.getDistrictFacade()
					.getAllActiveByRegion(currentUser.getUser().getRegion().getUuid()));
			provinceFilter.setEnabled(false);
		}
		layout.add(provinceFilter);

		districtFilter.setPlaceholder("All Districts");
		districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		if (currentUser.getUser().getDistrict() != null) {
			districtFilter.setValue(currentUser.getUser().getDistrict());
			filteredDataProvider.setFilter(criteria.district(currentUser.getUser().getDistrict()));

			districtFilter.setEnabled(false);
		}
		layout.add(districtFilter);

		relevanceStatusFilter.setItems(EntityRelevanceStatus.values());

		relevanceStatusFilter.setItemLabelGenerator(status -> {
			if (status == EntityRelevanceStatus.ARCHIVED) {
				return I18nProperties.getCaption(Captions.communityArchivedCommunities);
			} else if (status == EntityRelevanceStatus.ACTIVE) {
				return I18nProperties.getCaption(Captions.communityActiveCommunities);
			} else if (status == EntityRelevanceStatus.ALL) {
				return I18nProperties.getCaption(Captions.communityAllCommunities);
			}
			// Handle other enum values if needed
			return status.toString();
		});

		layout.add(resetFilters);
		

		searchField.addValueChangeListener(e -> {
			criteria.nameLike(e.getValue());
			filteredDataProvider.setFilter(criteria);
			resetFilters.setVisible(true);

		});

		regionFilter.addValueChangeListener(e -> {
			provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));
			AreaReferenceDto area = e.getValue();
			criteria.area(area);
			filteredDataProvider.setFilter(criteria);
			resetFilters.setVisible(true);

		});

		provinceFilter.addValueChangeListener(e -> {
			districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid()));
			filteredDataProvider.setFilter(criteria);
			RegionReferenceDto province = e.getValue();
			criteria.region(province);
			filteredDataProvider.refreshAll();
		});
		districtFilter.addValueChangeListener(e -> {

			filteredDataProvider.setFilter(criteria);
			DistrictReferenceDto district = e.getValue();
			criteria.district(district);
			filteredDataProvider.refreshAll();
		});

		relevanceStatusFilter.addValueChangeListener(e -> {
			criteria.relevanceStatus((EntityRelevanceStatus) e.getValue());
			filteredDataProvider.setFilter(criteria.relevanceStatus((EntityRelevanceStatus) e.getValue()));
		});

		resetFilters.addClassName("resetButton");
//		resetFilters.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		resetFilters.setVisible(false);

		Button addNew = new Button("Add New Cluster");
		addNew.getElement().getStyle().set("white-space", "normal");
		addNew.getStyle().set("color", "white");
		addNew.getStyle().set("background", "#0D6938");
		addNew.addClickListener(event -> {
			createOrEditCluster(communityDto);
		});
		layout.add(addNew, anchor);
		relevancelayout.add(relevanceStatusFilter);
		vlayout.setWidth("99%");
		vlayout.add(displayFilters, layout, relevancelayout);
		add(vlayout);
		return vlayout;
	}

	public boolean createOrEditCluster(CommunityDto communityDto) {
		Dialog dialog = new Dialog();
		FormLayout fmr = new FormLayout();
		TextField nameField = new TextField("Name");
//		
		TextField clusterNumber = new TextField("Cluster Number");

		TextField cCodeField = new TextField("CCode");
		ComboBox<RegionReferenceDto> provinceOfDistrict = new ComboBox<>("Province");
		ComboBox<DistrictReferenceDto> districtOfCluster = new ComboBox<>("District");
		provinceOfDistrict.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());

		provinceOfDistrict.addValueChangeListener(e-> {
//			districtOfCluster.clear();
			districtOfCluster.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(provinceOfDistrict.getValue().getUuid()));
		});
		if (communityDto != null) {
			nameField.setValue(communityDto.getName());
			clusterNumber.setValue(communityDto.getClusterNumber().toString());
			cCodeField.setValue(communityDto.getExternalId().toString());
			provinceOfDistrict.setItems(communityDto.getRegion());
			provinceOfDistrict.setValue(communityDto.getRegion());
			provinceOfDistrict.setEnabled(false);
			districtOfCluster.setItems(communityDto.getDistrict());
			districtOfCluster.setValue(communityDto.getDistrict());
			districtOfCluster.setEnabled(false);
		}

		// this can generate null
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		Button saveButton = new Button("Save");
		Button discardButton = new Button("Discard", e -> dialog.close());
		saveButton.getStyle().set("margin-right", "10px");
		saveButton.addClickListener(saveEvent -> {

			String name = nameField.getValue();
			String clusterNum = clusterNumber.getValue();
			String code = cCodeField.getValue();

			String uuids = "";
			if (communityDto != null) {
				uuids = communityDto.getUuid();
			}
			
			if (name != null && code != null) {

				CommunityDto dce = FacadeProvider.getCommunityFacade().getByUuid(uuids);
				if (dce != null) {
				dce.setName(name);
				int clusternumbervalue = Integer.parseInt(clusterNum);
				dce.setClusterNumber(clusternumbervalue);
				long ccodeValue = Long.parseLong(code);
				dce.setExternalId(ccodeValue);
				dce.setRegion(provinceOfDistrict.getValue());
				dce.setDistrict(districtOfCluster.getValue());

				FacadeProvider.getCommunityFacade().save(dce, true);

				Notification.show("Saved: " + name + " " + code);
				dialog.close();
				 refreshGridData();
				}else {
					CommunityDto dcex = new CommunityDto();
					dcex.setName(name);
					int clusternumbervalue = Integer.parseInt(clusterNum);
					dcex.setClusterNumber(clusternumbervalue);
					long ccodeValue = Long.parseLong(code);
					dcex.setExternalId(ccodeValue);
					dcex.setRegion(provinceOfDistrict.getValue());
					dcex.setDistrict(districtOfCluster.getValue());

					FacadeProvider.getCommunityFacade().save(dcex, true);

					Notification.show("Saved: " + name + " " + code);
					dialog.close();
					 refreshGridData();
				}
			} else {
				Notification.show("Not Valid Value: " + name + " " + code);
			}

		});

//		dialog.setHeaderTitle("Edit " + communityDto.getName());
		if (communityDto == null) {
			dialog.setHeaderTitle("Add New Cluster");
		} else {
			dialog.setHeaderTitle("Edit " + communityDto.getName());

		}
		fmr.add(nameField, cCodeField, clusterNumber, provinceOfDistrict, districtOfCluster);
		dialog.add(fmr);
		dialog.getFooter().add(discardButton, saveButton);

//      getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
//		.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		dialog.open();

		return true;
	}
	
	private void refreshGridData() {
		ListDataProvider<CommunityDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getCommunityFacade().getIndexList(criteria, null, null, null).stream());
		
		grid.setDataProvider(filteredDataProvider);
//		dataView = grid.setItems(dataProvider);
	}

}
