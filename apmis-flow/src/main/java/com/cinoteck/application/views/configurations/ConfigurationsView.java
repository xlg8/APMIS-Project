package com.cinoteck.application.views.configurations;

import com.cinoteck.application.views.MainLayout;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictCriteria;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@PageTitle("Configurations")
@Route(value = "configurations", layout = MainLayout.class)
public class ConfigurationsView extends VerticalLayout implements RouterLayout {
    private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();
    Anchor anchor;
    HorizontalLayout configActionLayout = new HorizontalLayout();
    Button displayActionButtons = new Button("Show Action Buttons", new Icon(VaadinIcon.SLIDERS));


    private Tabs createTabs() {
        tabComponentMap.put(new Tab("Regions"), new RegionView());
        tabComponentMap.put(new Tab("Province"), new ProvinceView());
        tabComponentMap.put(new Tab("District"), new DistrictView());
        tabComponentMap.put(new Tab("Cluster"), new ClusterView());

        return new Tabs(tabComponentMap.keySet().toArray(new Tab[]{}));
    }

    public ConfigurationsView() {
        setSizeFull();
        HorizontalLayout campDatFill = new HorizontalLayout();
        campDatFill.setClassName("campDatFill");

        configActionLayout.getStyle().set("margin-right", "1em");
        configActionLayout.setMargin(false);
        configActionLayout.setJustifyContentMode(JustifyContentMode.END);

        Tabs tabs = createTabs();
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
                    Notification.show("Region");
                    break;
                case "Province":
                	
                	removeAnchorContent();
                	createExcelLinkForProvince();
                	
                    Notification.show("Province");
                    break;
                case "District":
                	removeAnchorContent();
                	createExcelLinkForDistrict();
                    Notification.show("District");
                    break;
                case "Cluster":
                	removeAnchorContent();
                	createExcelLinkForCluster();
                    Notification.show("Cluster");
                    break;
                default:
                	
                   createExcelLinkForDefault();
                 
                    Notification.show("default");
                    break;
            }
            

        });
        // Set initial content
        contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));


        Button importButton = new Button("Import", new Icon(VaadinIcon.DOWNLOAD_ALT));
        importButton.getStyle().set("color", "white");
        importButton.getStyle().set("background", "#0C5830");
        importButton.setVisible(false);
        configActionLayout.add(importButton);

        Button exportButton = new Button("Export", new Icon(VaadinIcon.UPLOAD_ALT));
        exportButton.getStyle().set("color", "white");
        exportButton.getStyle().set("background", "#0C5830");
        exportButton.setVisible(false);
        configActionLayout.add(exportButton);

        Button newEntryButton = new Button("New Entry", new Icon(VaadinIcon.PLUS_CIRCLE_O));
        newEntryButton.getStyle().set("color", "white");
        newEntryButton.getStyle().set("background", "#0C5830");
        newEntryButton.setVisible(false);
        configActionLayout.add(newEntryButton);

        Button bulkEditMode = new Button("Enter Bulk Mode", new Icon(VaadinIcon.CHECK));
        bulkEditMode.getStyle().set("color", "white");
        bulkEditMode.getStyle().set("background", "#0C5830");
        bulkEditMode.setVisible(false);
        configActionLayout.add(bulkEditMode);

        displayActionButtons.addClickListener(e -> {
            if (!bulkEditMode.isVisible()) {
                importButton.setVisible(true);
                exportButton.setVisible(true);
                newEntryButton.setVisible(true);
                bulkEditMode.setVisible(true);
//                excelLink.setVisible(true);
                displayActionButtons.setText("Hide Action Buttons");
            } else {
                importButton.setVisible(false);
                exportButton.setVisible(false);
                newEntryButton.setVisible(false);
                bulkEditMode.setVisible(false);
//                excelLink.setVisible(false);
                displayActionButtons.setText("Show Action Buttons");
            }
        });
        configActionLayout.add(displayActionButtons);

        campDatFill.add(tabs, configActionLayout);

        add(campDatFill, contentContainer);
    }

    private Anchor createExcelLinkForRegion() {
        Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);
        GridListDataView<AreaDto> dataView;
        List<AreaDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation();
        dataView = grid.setItems(regions);
        grid.addColumn(AreaDto::getName).setHeader("Region").setSortable(true).setResizable(true);
        grid.addColumn(AreaDto::getExternalId).setHeader("Rcode").setResizable(true).setSortable(true);

        GridExporter<AreaDto> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle("Region");
        exporter.setFileName("APMIS_Regions" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));

        anchor = new Anchor("", "Export");
        anchor.setHref(exporter.getCsvStreamResource());
        anchor.getElement().setAttribute("download", true);
        anchor.setClassName("exportJsonGLoss");
        configActionLayout.add(anchor);
        configActionLayout.add(displayActionButtons);
        return anchor;
    }

    private Anchor createExcelLinkForDefault() {
        Grid<AreaDto> grid = new Grid<>(AreaDto.class, false);
        GridListDataView<AreaDto> dataView;
        List<AreaDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReferenceAndPopulation();
        dataView = grid.setItems(regions);
        grid.addColumn(AreaDto::getName).setHeader("Region").setSortable(true).setResizable(true);
        grid.addColumn(AreaDto::getExternalId).setHeader("Rcode").setResizable(true).setSortable(true);

        GridExporter<AreaDto> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle("Region");
        exporter.setFileName("APMIS_Regions" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));

        anchor = new Anchor("", "Export");
        anchor.setHref(exporter.getCsvStreamResource());
        anchor.getElement().setAttribute("download", true);
        anchor.setClassName("exportJsonGLoss");
        configActionLayout.add(anchor);
        configActionLayout.add(displayActionButtons);
        return anchor;
    }
    
    private Anchor createExcelLinkForProvince() {
    	Grid<RegionIndexDto> grid = new Grid<>(RegionIndexDto.class, false);
    	List<RegionIndexDto> regions = FacadeProvider.getRegionFacade().getAllRegions();
    	GridListDataView<RegionIndexDto> dataView;
        dataView = grid.setItems(regions);
        grid.addColumn(RegionIndexDto::getArea).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(RegionIndexDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true);
		grid.addColumn(RegionIndexDto::getName).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(RegionIndexDto::getExternalId).setHeader("PCode").setSortable(true).setResizable(true);

        GridExporter<RegionIndexDto> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle("Region");
        exporter.setFileName("APMIS_Provinces" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));

        anchor = new Anchor("", "Export");
        anchor.setHref(exporter.getCsvStreamResource());
        anchor.getElement().setAttribute("download", true);
        anchor.setClassName("exportJsonGLoss");
        configActionLayout.add(anchor);
        configActionLayout.add(displayActionButtons);
        return anchor;
    }
    
    private Anchor createExcelLinkForDistrict() {
    	Grid<DistrictIndexDto> grid = new Grid<>(DistrictIndexDto.class, false);
    	DistrictDataProvider districtDataProvider = new DistrictDataProvider();
    	ConfigurableFilterDataProvider<DistrictIndexDto, Void, DistrictCriteria> filteredDataProvider;
    	GridListDataView<DistrictIndexDto> dataView;
        grid.addColumn(DistrictIndexDto::getAreaname).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true);
		grid.addColumn(DistrictIndexDto::getRegion).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getRegionexternalId).setHeader("PCode").setResizable(true).setSortable(true);
		grid.addColumn(DistrictIndexDto::getName).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(DistrictIndexDto::getExternalId).setHeader("DCode").setResizable(true).setSortable(true);
		filteredDataProvider = districtDataProvider.withConfigurableFilter();

		grid.setDataProvider(filteredDataProvider);

        GridExporter<DistrictIndexDto> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle("Region");
        exporter.setFileName("APMIS_Districts" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));

        anchor = new Anchor("", "Export ");
        anchor.setHref(exporter.getCsvStreamResource());
        anchor.getElement().setAttribute("download", true);
        anchor.setClassName("exportJsonGLoss");
        configActionLayout.add(anchor);
        configActionLayout.add(displayActionButtons);
        return anchor;
    }
    
    private Anchor createExcelLinkForCluster() {
    	Grid<CommunityDto> grid = new Grid<>(CommunityDto.class, false);
    	ClusterDataProvider clusterDataProvider = new ClusterDataProvider();
    	
    	ConfigurableFilterDataProvider<CommunityDto, Void, CommunityCriteriaNew> filteredDataProvider;
    	GridListDataView<CommunityDto> dataView;
    	grid.addColumn(CommunityDto::getAreaname).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getAreaexternalId).setHeader("Rcode").setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getRegion).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getRegionexternalId).setHeader("PCode").setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getDistrict).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getDistrictexternalId).setHeader("DCode").setResizable(true).setSortable(true);
		grid.addColumn(CommunityDto::getName).setHeader("Cluster").setSortable(true).setResizable(true);
		grid.addColumn(CommunityDto::getExternalId).setHeader("CCode").setResizable(true).setSortable(true);

		filteredDataProvider = clusterDataProvider.withConfigurableFilter();

		grid.setDataProvider(filteredDataProvider);

        GridExporter<CommunityDto> exporter = GridExporter.createFor(grid);
        exporter.setAutoAttachExportButtons(false);
        exporter.setTitle("Region");
        exporter.setFileName("APMIS_Clusters" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));

        anchor = new Anchor("", "Export");
        anchor.setHref(exporter.getCsvStreamResource());
        anchor.getElement().setAttribute("download", true);
        anchor.setClassName("exportJsonGLoss");
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
