package com.cinoteck.application.views.configurations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridLazyDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.district.DistrictIndexDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionCriteria;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("Clusters")
@Route(value = "clusters", layout = ConfigurationsView.class)
public class ClusterView extends Div {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5091856954264511639L;
//	private GridListDataView<CommunityDto> dataView;
	private CommunityCriteriaNew criteria;

	ClusterDataProvider clusterDataProvider = new ClusterDataProvider();
	
	ConfigurableFilterDataProvider<CommunityDto, Void, CommunityCriteriaNew> filteredDataProvider;

	Grid<CommunityDto> grid = new Grid<>(CommunityDto.class, false);
	
	public ClusterView() {
		this.criteria = new CommunityCriteriaNew();
		setHeightFull();
		//List<CommunityDto> clusters = FacadeProvider.getCommunityFacade().getAllCommunities();
	//	GridLazyDataView<CommunityDto> dataView;// = grid.setItems(clusters);

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
		add(grid);
	}

	// TODO: Hide the filter bar on smaller screens
	public Component addFilters() {

HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);
		layout.setVisible(false);
		layout.setAlignItems(Alignment.END);

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
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

		ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>("Region");
		regionFilter.setPlaceholder("All Regions");
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

		layout.add(regionFilter);

		ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>("Province");
		provinceFilter.setPlaceholder("All Provinces");
		provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		layout.add(provinceFilter);

		ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>("District");
		districtFilter.setPlaceholder("All Districts");
		districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		layout.add(districtFilter);

		ComboBox<RegionIndexDto> communityFilter = new ComboBox<>("Cluster");
		communityFilter.setPlaceholder("All Clusters");
		// communityFilter.setItems(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(null));
		layout.add(communityFilter);

		TextField searchField = new TextField();
		searchField.setWidth("10%");
		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> {

		});

		layout.add(searchField);

		regionFilter.addValueChangeListener(e -> {
			provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));
//			dataView.addFilter(f -> f.getAreaname().equalsIgnoreCase(regionFilter.getValue().getCaption()));
			// dataView.refreshAll();
			AreaReferenceDto area = e.getValue();
			criteria.area(area);
			filteredDataProvider.setFilter(criteria);
		});

		provinceFilter.addValueChangeListener(e -> {
					districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid()));
//					dataView.addFilter(f -> f.getRegion().getCaption().equalsIgnoreCase(provinceFilter.getValue().getCaption()));
					filteredDataProvider.setFilter(criteria);
					RegionReferenceDto province = e.getValue();
					criteria.region(province);
					filteredDataProvider.refreshAll();	
		});
		districtFilter.addValueChangeListener(e -> {
//			districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid()));
//			dataView.addFilter(f -> f.getDistrict().getCaption().equalsIgnoreCase(districtFilter.getValue().getCaption()));
			filteredDataProvider.setFilter(criteria);
			DistrictReferenceDto district = e.getValue();
			criteria.district(district);
			filteredDataProvider.refreshAll();	
		});

		Button primaryButton = new Button("Reset Filters");
		primaryButton.addClassName("resetButton");
		primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(primaryButton);
		
		vlayout.add(displayFilters, layout);
		add(vlayout);
		return vlayout;
	}
	
//	private void exportToCsvFile(Grid<CommunityDto> grid)
//	        throws FileNotFoundException, IOException {
//	    GridDataView<CommunityDto> dataView = grid.getGenericDataView();
//	    FileOutputStream fout = new FileOutputStream(new File("/tmp/export.csv"));
//
//	    dataView.getItems().forEach(person -> {
//	        try {
//	            fout.write((communityDto.getAreaname()+ ", " + communityDto.getExternalId() +"\n").getBytes());
//	        } catch (IOException ex) {
//	            throw new RuntimeException(ex);
//	        }
//	    });
//	    fout.close();
//	}
}
