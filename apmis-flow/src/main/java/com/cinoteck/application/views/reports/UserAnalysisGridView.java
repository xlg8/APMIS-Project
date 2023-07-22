package com.cinoteck.application.views.reports;

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.ErrorStatusEnum;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.CommunityUserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.utils.SortProperty;


@Route(layout =UserAnalysisView.class)
public class UserAnalysisGridView extends VerticalLayout {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = 2199158503341966128L;
	public ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
	public ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>();
	public ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>();
	public Button resetButton;
	private ComboBox errorStatusFilter;
	private ErrorStatusEnum errorStatusEnum;
	
	
	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	Grid<CommunityUserReportModelDto> grid = new Grid<>(CommunityUserReportModelDto.class, false);
	//GridListDataView<CommunityUserReportModelDto> dataView;
	
	public UserAnalysisGridView(CommunityCriteriaNew criteria, FormAccess formAccess) {
//		List<CommunityUserReportModelDto> analysis = FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceFlow(criteria,
//				null,null, null, formAccess);
		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		filterLayout.setVisible(false);
		filterLayout.setAlignItems(Alignment.END);


		regionFilter.setLabel(I18nProperties.getCaption(Captions.area));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.areaAllAreas));
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		regionFilter.addValueChangeListener(e -> {
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			provinceFilter.setItems(provinces);
		});

		
		provinceFilter.setLabel(I18nProperties.getCaption(Captions.region));
		provinceFilter.setPlaceholder(I18nProperties.getCaption(Captions.regionAllRegions));
		provinceFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		provinceFilter.addValueChangeListener(e -> {
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			districtFilter.setItems(districts);
		});
		
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.districtAllDistricts));
		districtFilter.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
		
		resetButton =  new Button(I18nProperties.getCaption(Captions.actionResetFilters));
		resetButton.addClickListener(e->{
			provinceFilter.clear();
			districtFilter.clear();
			regionFilter.clear();
	
		});
		
		Div countAndButtons = new Div();
		
		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.addClickListener(e->{
			if(filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				displayFilters.setText("Hide Filters");
			}else {
				filterLayout.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});
		
//		H2 counter = new H2("Number of rows: ");
//		counter.add("567890");
		
		filterLayout.add( regionFilter, provinceFilter, districtFilter, resetButton);
		countAndButtons.add(displayFilters);
		add(countAndButtons,filterLayout);
		setSizeFull();
		userAnalysisGrid(criteria, formAccess);
		
	}


	@SuppressWarnings("deprecation")
	private void userAnalysisGrid(CommunityCriteriaNew criteria, FormAccess formAccess) {
//		List<CommunityUserReportModelDto> analysis = FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceFlow(criteria,
//				null,null, null, formAccess);
		
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CommunityUserReportModelDto::getArea).setHeader(I18nProperties.getCaption(Captions.area)).setSortProperty("region").setSortable(true).setResizable(true);
		grid.addColumn(CommunityUserReportModelDto::getRegion).setHeader(I18nProperties.getCaption(Captions.region)).setSortProperty("province").setSortable(true).setResizable(true);
		grid.addColumn(CommunityUserReportModelDto::getDistrict).setHeader(I18nProperties.getCaption(Captions.district)).setSortProperty("district").setSortable(true).setResizable(true);
		grid.addColumn(CommunityUserReportModelDto::getFormAccess).setHeader("Form Access").setSortProperty("formAccess").setSortable(true).setResizable(true);
		grid.addColumn(CommunityUserReportModelDto::getClusterNumberr).setHeader(I18nProperties.getCaption(Captions.clusterNumber)).setSortProperty("clusterNumberr").setSortable(true).setResizable(true);
		grid.addColumn(CommunityUserReportModelDto::getcCode).setHeader(I18nProperties.getCaption(Captions.Community_externalID)).setSortProperty("ccode").setSortable(true).setResizable(true);

		grid.addColumn(CommunityUserReportModelDto::getUsername).setHeader(I18nProperties.getCaption(Captions.User_userName)).setSortProperty("username").setSortable(true).setResizable(true);
		grid.addColumn(CommunityUserReportModelDto::getMessage).setHeader("Message").setSortProperty("message").setSortable(true).setResizable(true);
		
		
		int numberOfRows = FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceCount(null, null, null, null, formAccess);
		DataProvider<CommunityUserReportModelDto, CommunityCriteriaNew> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCommunityFacade()
						.getAllActiveCommunitytoRerenceFlow(criteria, query.getOffset(),
										query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()), formAccess)
								.stream(), 
						query -> numberOfRows//numberOfRowsx
//						Integer.parseInt(FacadeProvider.getCampaignFormDataFacade()
//								.getByCompletionAnalysisCount(criteria, query.getOffset(), //query.getFilter().orElse(null)
//										query.getLimit(),
//										query.getSortOrders().stream()
//												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
//														sortOrder.getDirection() == SortDirection.ASCENDING))
//												.collect(Collectors.toList()), null))
								);
		
		grid.setDataProvider(dataProvider);
		grid.setPageSize(250);
		grid.setVisible(true);
		
		add(grid);		
	}


	

}
