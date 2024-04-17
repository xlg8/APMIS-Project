package com.cinoteck.application.views.configurations;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.opencsv.CSVWriter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.HasItems;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserType;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;

@PageTitle("APMIS-Configurations")
@Route(value = "configurations", layout = MainLayout.class)
public class ConfigurationsView extends VerticalLayout implements RouterLayout {
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();
	Anchor anchor;
	HorizontalLayout configActionLayout = new HorizontalLayout();
	Button displayActionButtons = new Button("Show Action Buttons", new Icon(VaadinIcon.SLIDERS));

	UserProvider userProvider = new UserProvider();

	private Tabs createTabs() {
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.area)), new RegionView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.region)), new ProvinceView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.district)), new DistrictView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.community)), new ClusterView());

		if (userProvider.hasUserRight(UserRight.CONFIGURATION_CHANGE_LOG)) {
			tabComponentMap.put(new Tab(I18nProperties.getCaption("Change Log")), new ConfigurationChangeLogView());

		}

		return new Tabs(tabComponentMap.keySet().toArray(new Tab[] {}));
	}

	public ConfigurationsView() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		setSizeFull();
		HorizontalLayout campDatFill = new HorizontalLayout();
		campDatFill.setClassName("campDatFill");

		configActionLayout.getStyle().set("margin-right", "1em");
		configActionLayout.setMargin(false);
		configActionLayout.setJustifyContentMode(JustifyContentMode.END);

		Tabs tabs = createTabs();
		tabs.getStyle().set("background", "#434343");
		tabs.getStyle().set("background", "#434343");
		tabs.setSizeFull();
		Div contentContainer = new Div();
		contentContainer.setSizeFull();
		setSizeFull();
		tabs.addSelectedChangeListener(e -> {

			contentContainer.removeAll();

			contentContainer.add(tabComponentMap.get(e.getSelectedTab()));

			switch (tabs.getSelectedTab().getLabel()) {
			case "Regions":
				removeAnchorContent();
				createExcelLinkForRegion();
				// Notification.show("Region");
				break;
			case "Province":

				removeAnchorContent();
				createExcelLinkForProvince();

				// Notification.show("Province");
				break;
			case "District":
				removeAnchorContent();
				createExcelLinkForDistrict();
				// Notification.show("District");
				break;
			case "Cluster":
				removeAnchorContent();
				createExcelLinkForCluster();
				// Notification.show("Cluster");
				break;
			default:

				createExcelLinkForDefault();

				// Notification.show("default");
				break;
			}

		});
		// Set initial content
		contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
		createExcelLinkForDefault();

		Button importButton = new Button(I18nProperties.getCaption(Captions.actionImport),
				new Icon(VaadinIcon.DOWNLOAD_ALT));
		importButton.getStyle().set("color", "white");
		importButton.getStyle().set("background", "#0C5830");
		importButton.setVisible(false);
//        configActionLayout.add(importButton);

		Button exportButton = new Button(I18nProperties.getCaption(Captions.export), new Icon(VaadinIcon.UPLOAD_ALT));
		exportButton.getStyle().set("color", "white");
		exportButton.getStyle().set("background", "#0C5830");
		exportButton.setVisible(false);
		exportButton.addClickListener(e -> {
			RegionView reg = new RegionView();
//        	reg.exportArea();
		});

//        configActionLayout.add(exportButton);

		Button newEntryButton = new Button(I18nProperties.getCaption(Captions.actionNewEntry),
				new Icon(VaadinIcon.PLUS_CIRCLE_O));
		newEntryButton.getStyle().set("color", "white");
		newEntryButton.getStyle().set("background", "#0C5830");
		newEntryButton.setVisible(false);
//        configActionLayout.add(newEntryButton);

		Button bulkEditMode = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode),
				new Icon(VaadinIcon.CHECK));
		bulkEditMode.getStyle().set("color", "white");
		bulkEditMode.getStyle().set("background", "#0C5830");
		bulkEditMode.setVisible(false);
//        configActionLayout.add(bulkEditMode);
		anchor.setVisible(false);
		displayActionButtons.addClickListener(e -> {
			if (!bulkEditMode.isVisible()) {
				importButton.setVisible(true);
				exportButton.setVisible(true);
				newEntryButton.setVisible(true);
				bulkEditMode.setVisible(true);
				anchor.setVisible(true);
				displayActionButtons.setText("Hide Action Buttons");
			} else {
				importButton.setVisible(false);
				exportButton.setVisible(false);
				newEntryButton.setVisible(false);
				bulkEditMode.setVisible(false);
				anchor.setVisible(false);
				displayActionButtons.setText("Show Action Buttons");
			}
		});
		configActionLayout.add(displayActionButtons);

		campDatFill.add(tabs);// , configActionLayout);

		add(campDatFill, contentContainer);
	}

	private void downloadGridDataAsCsv() {

//        Tabs tabs = createTabs();

		Tab selectedTab = createTabs().getSelectedTab();
		Component selectedComponent = tabComponentMap.get(selectedTab);
//
//        if (selectedComponent instanceof HasItems) {
//
//            HasItems grid = (HasItems) selectedComponent;
//
//            StringWriter writer = new StringWriter();
//            CSVWriter csvWriter = new CSVWriter(writer);
//
//            // Write CSV header
//            List<String> headerRow = ((Grid<?>) grid).getColumns().stream()
//                    .map(Grid.Column::getKey)
//                    .collect(Collectors.toList());
//            csvWriter.writeNext(headerRow.toArray(new String[0]));
//
//            // Write CSV data rows
//            List<?> data = ((GridListDataView<?>) ((Grid<?>) grid).getListDataView()).getItems().collect(Collectors.toList());
//            data.forEach(item -> {
//                List<String> dataRow = grid.getColumns().stream()
//                        .map(column -> column.getValueProvider().apply(item))
//                        .map(Object::toString)
//                        .collect(Collectors.toList());
//                csvWriter.writeNext(dataRow.toArray(new String[0]));
//            });
//
//
//            // Download the CSV file
//            String filename = "grid_data.csv";
//            String csvData = writer.toString();
//            ByteArrayResource resource = new ByteArrayResource(csvData.getBytes(), filename);
//            anchor.setHref(resource);
//            anchor.getElement().setAttribute("download", true);
//            anchor.getElement().executeJs("this.click()");
//        }
	}

	private Anchor createExcelLinkForRegion() {
		Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);
		GridListDataView<AreaDto> dataView;
		List<AreaDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation();
		dataView = grid.setItems(regions);
		grid.addColumn(AreaDto::getName).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
				.setResizable(true);
		grid.addColumn(AreaDto::getExternalId).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
				.setResizable(true).setSortable(true);

		GridExporter<AreaDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.area));
		exporter.setFileName(
				"APMIS_Regions" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		configActionLayout.add(anchor);
		configActionLayout.add(displayActionButtons);
		return anchor;
	}

	private Anchor createExcelLinkForDefault() {
		Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);
		GridListDataView<AreaDto> dataView;
		List<AreaDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation();
		dataView = grid.setItems(regions);
		grid.addColumn(AreaDto::getName).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
				.setResizable(true);
		grid.addColumn(AreaDto::getExternalId).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
				.setResizable(true).setSortable(true);

		GridExporter<AreaDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.area));
		exporter.setFileName(
				"APMIS_Regions" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		configActionLayout.add(anchor);
		configActionLayout.add(displayActionButtons);
		return anchor;
	}

	private Anchor createExcelLinkForProvince() {
		Grid<RegionIndexDto> grid = new Grid<>(RegionIndexDto.class, false);
		List<RegionIndexDto> regions = FacadeProvider.getRegionFacade().getAllRegions();
		GridListDataView<RegionIndexDto> dataView;
		dataView = grid.setItems(regions);
		grid.addColumn(RegionIndexDto::getArea).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
				.setResizable(true);
		grid.addColumn(RegionIndexDto::getAreaexternalId).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
				.setResizable(true).setSortable(true);
		grid.addColumn(RegionIndexDto::getName).setHeader(I18nProperties.getCaption(Captions.region)).setSortable(true)
				.setResizable(true);
		grid.addColumn(RegionIndexDto::getExternalId).setHeader(I18nProperties.getCaption(Captions.Region_externalID))
				.setSortable(true).setResizable(true);

		GridExporter<RegionIndexDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.area));
		exporter.setFileName(
				"APMIS_Provinces" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");

		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		configActionLayout.add(anchor);
		configActionLayout.add(displayActionButtons);
		return anchor;
	}

	private Anchor createExcelLinkForDistrict() {
		Grid<DistrictIndexDto> grid = new Grid<>(DistrictIndexDto.class, false);
		DistrictDataProvider districtDataProvider = new DistrictDataProvider();
		ConfigurableFilterDataProvider<DistrictIndexDto, Void, DistrictCriteria> filteredDataProvider;
		GridListDataView<DistrictIndexDto> dataView;
		grid.addColumn(DistrictIndexDto::getAreaname).setHeader(I18nProperties.getCaption(Captions.area))
				.setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getAreaexternalId)
				.setHeader(I18nProperties.getCaption(Captions.Area_externalId)).setResizable(true).setSortable(true);
		grid.addColumn(DistrictIndexDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region))
				.setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getRegionexternalId)
				.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setResizable(true).setSortable(true);
		grid.addColumn(DistrictIndexDto::getName).setHeader(I18nProperties.getCaption(Captions.district))
				.setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getExternalId)
				.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setResizable(true)
				.setSortable(true);
		filteredDataProvider = districtDataProvider.withConfigurableFilter();

		grid.setDataProvider(filteredDataProvider);

		GridExporter<DistrictIndexDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.area));
		exporter.setFileName(
				"APMIS_Districts" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");

		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		configActionLayout.add(anchor);
		configActionLayout.add(displayActionButtons);
		return anchor;
	}

	private Anchor createExcelLinkForCluster() {
		Grid<CommunityDto> grid = new Grid<>(CommunityDto.class, false);
		ClusterDataProvider clusterDataProvider = new ClusterDataProvider();

		ConfigurableFilterDataProvider<CommunityDto, Void, CommunityCriteriaNew> filteredDataProvider;
		GridListDataView<CommunityDto> dataView;
		grid.addColumn(CommunityDto::getAreaname).setHeader(I18nProperties.getCaption(Captions.area)).setSortable(true)
				.setResizable(true);
		grid.addColumn(CommunityDto::getAreaexternalId).setHeader(I18nProperties.getCaption(Captions.Area_externalId))
				.setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region)).setSortable(true)
				.setResizable(true);
		grid.addColumn(CommunityDto::getRegionexternalId)
				.setHeader(I18nProperties.getCaption(Captions.Region_externalID)).setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getDistrict).setHeader(I18nProperties.getCaption(Captions.district))
				.setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getDistrictexternalId)
				.setHeader(I18nProperties.getCaption(Captions.District_externalID)).setResizable(true)
				.setSortable(true);
		grid.addColumn(CommunityDto::getName).setHeader(I18nProperties.getCaption(Captions.community)).setSortable(true)
				.setResizable(true);
		grid.addColumn(CommunityDto::getExternalId).setHeader(I18nProperties.getCaption(Captions.Community_externalID))
				.setResizable(true).setSortable(true);

		filteredDataProvider = clusterDataProvider.withConfigurableFilter();

		grid.setDataProvider(filteredDataProvider);

		GridExporter<CommunityDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.area));
		exporter.setFileName(
				"APMIS_Clusters" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");

		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		configActionLayout.add(anchor);
		configActionLayout.add(displayActionButtons);
		return anchor;
	}

	private void removeAnchorContent() {
		if (anchor != null) {
			configActionLayout.remove(anchor);
			configActionLayout.remove(displayActionButtons);
			anchor = null; // Optional: Set the anchor reference to null
		}
	}
}