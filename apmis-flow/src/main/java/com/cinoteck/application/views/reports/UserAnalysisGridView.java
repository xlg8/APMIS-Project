package com.cinoteck.application.views.reports;

import java.util.List;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.ErrorStatusEnum;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityCriteriaNew;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.CommunityUserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.SortProperty;

@Route(layout = UserAnalysisView.class)
public class UserAnalysisGridView extends VerticalLayout {

	private static final long serialVersionUID = 2199158503341966128L;

	private ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>();
	private ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>();
	private ComboBox<DistrictReferenceDto> districtFilter = new ComboBox<>();
	private Button resetButton;
	private ComboBox<ErrorStatusEnum> errorStatusFilter;

	private List<AreaReferenceDto> regions;
	private List<RegionReferenceDto> provinces;
	private List<DistrictReferenceDto> districts;

	private Grid<CommunityUserReportModelDto> grid = new Grid<>(CommunityUserReportModelDto.class, false);

	private CommunityCriteriaNew criteria;
	private UserProvider currentUser = new UserProvider();

//    Paragraph countRowItems;
	public UserAnalysisGridView(CommunityCriteriaNew criteria, FormAccess formAccess) {
		this.criteria = new CommunityCriteriaNew();

		this.criteria.area(currentUser.getUser().getArea());
		this.criteria.region(currentUser.getUser().getRegion());
		this.criteria.district(currentUser.getUser().getDistrict());

		setSizeFull();
		addFilter(formAccess);
		userAnalysisGrid(criteria, formAccess);

	}

	private void addFilter(FormAccess formAccess) {
//    	final UserDto user = UserProvider.getCurrent().getUser();
//		criteria.area(user.getArea());// .setArea(user.getArea());
//		criteria.region(user.getRegion());// .setRegion(user.getRegion());
//		criteria.district(user.getDistrict()); 

//        setMargin(true);
		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		filterLayout.setVisible(false);
		filterLayout.setAlignItems(Alignment.END);

		regionFilter.setLabel("Region");
		regionFilter.setPlaceholder("All Regions");
		regionFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());

		regionFilter.addValueChangeListener(e -> {
			AreaReferenceDto selectedArea = e.getValue();
			if (selectedArea != null) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(selectedArea.getUuid());
				provinceFilter.setItems(provinces);
				criteria.area(selectedArea);
				criteria.region(null);
				refreshGridData(formAccess);
			} else {
				criteria.area(null);
				refreshGridData(formAccess);
			}
//            updateText(formAccess);
		});

		provinceFilter.setLabel("Province");
		provinceFilter.setPlaceholder("All Province");
		provinceFilter.addValueChangeListener(e -> {
			RegionReferenceDto selectedRegion = e.getValue();
			if (selectedRegion != null) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(selectedRegion.getUuid());
				districtFilter.setItems(districts);
				criteria.region(selectedRegion);
				refreshGridData(formAccess);
			} else {
				criteria.region(null);
				refreshGridData(formAccess);
			}
//            updateText(formAccess);
		});

		districtFilter.setLabel("District");
		districtFilter.setPlaceholder("All District");
		districtFilter.addValueChangeListener(e -> {
			DistrictReferenceDto selectedDistrict = e.getValue();
			if (selectedDistrict != null) {
				criteria.district(selectedDistrict);
				refreshGridData(formAccess);
			} else {
				criteria.district(null);
				refreshGridData(formAccess);
			}
//            updateText(formAccess);
		});

		errorStatusFilter = new ComboBox<ErrorStatusEnum>();
		errorStatusFilter.setItems(ErrorStatusEnum.values());
		errorStatusFilter.setLabel("Error Status");
		errorStatusFilter.setPlaceholder("Error Status");
		errorStatusFilter.setId("errorStatusFilter");
		errorStatusFilter.setAllowCustomValue(false);

		errorStatusFilter.setItemLabelGenerator(this::getLabelForEnum);

		errorStatusFilter.addValueChangeListener(e -> {
			ErrorStatusEnum selectedErrorStatus = e.getValue();
//        	String stringg = "ErrorStatusEnum."+selectedErrorStatus;
			System.out.println(
					e.getValue() + " erorvalue chaged to " );

			if (e.getValue() != null) {

				criteria.errorStatusEnum((ErrorStatusEnum) selectedErrorStatus);
				refreshGridData(formAccess);
			} else {
				criteria.errorStatusEnum(ErrorStatusEnum.ALL_REPORT);
				refreshGridData(formAccess);
			}
//            updateText(formAccess);
		});

		resetButton = new Button("Reset Filters");
		resetButton.addClickListener(e -> {
			
//            errorStatusFilter.clear();
//            criteria.area(null);
			criteria.area(null);

			criteria.region(null);
			criteria.district(null);
			  regionFilter.clear();
				provinceFilter.clear();
				districtFilter.clear();
	          
//			criteria.errorStatusEnum(null);
			refreshGridData(formAccess);
//            updateText(formAccess);
		});

		Div countAndButtons = new Div();

		Button displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.addClickListener(e -> {
			filterLayout.setVisible(!filterLayout.isVisible());
			displayFilters.setText(filterLayout.isVisible() ? "Hide Filters" : "Show Filters");
		});

		HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(Alignment.END);
		layout.getStyle().set("margin-left", "15px");

		layout.add(displayFilters, filterLayout);

		filterLayout.setClassName("row pl-3");
		filterLayout.add(regionFilter, provinceFilter, districtFilter, errorStatusFilter, resetButton);
		countAndButtons.add(layout);
		add(countAndButtons);
	}
//    private void updateText( FormAccess formAccess) {
//    	int numberOfRows = FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceCount(criteria, null, null, null, formAccess);
//        String newText = numberOfRows+"";
//        countRowItems.setText(newText);
//        Notification.show("Text updated: " + newText);
//    }

	private String getLabelForEnum(ErrorStatusEnum statusEnum) {
		switch (statusEnum) {
		case ERROR_REPORT:
			return "Error Reports";

		case ALL_REPORT:
			return "Non-Error Reports";

		default:
			return statusEnum.toString();
		}
	}

	private void userAnalysisGrid(CommunityCriteriaNew criteria, FormAccess formAccess) {
		int numberOfRows = FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceCount(null, null, null,
				null, formAccess);
//    	countRowItems =  new Paragraph(numberOfRows + "");
//    	add(countRowItems);
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CommunityUserReportModelDto::getArea).setHeader("Region").setSortProperty("region")
				.setSortable(true).setResizable(true).setTooltipGenerator(e -> "Region");
		grid.addColumn(CommunityUserReportModelDto::getRegion).setHeader("Province").setSortProperty("province")
				.setSortable(true).setResizable(true).setTooltipGenerator(e -> "Province");
		grid.addColumn(CommunityUserReportModelDto::getDistrict).setHeader("District").setSortProperty("district")
				.setSortable(true).setResizable(true).setTooltipGenerator(e -> "District");
		grid.addColumn(CommunityUserReportModelDto::getFormAccess).setHeader("Form Access")
				.setSortProperty("formAccess").setSortable(true).setResizable(true)
				.setTooltipGenerator(e -> "Form Access");
		grid.addColumn(CommunityUserReportModelDto::getClusterNumberr).setHeader("Cluster Number")
				.setSortProperty("clusterNumberr").setSortable(true).setResizable(true)
				.setTooltipGenerator(e -> "Cluster Number");
		grid.addColumn(CommunityUserReportModelDto::getcCode).setHeader("CCode").setSortProperty("ccode")
				.setSortable(true).setResizable(true).setTooltipGenerator(e -> "CCode");

		grid.addColumn(CommunityUserReportModelDto::getUsername).setHeader("Username").setSortProperty("username")
				.setSortable(true).setResizable(true).setTooltipGenerator(e -> "Username");
		grid.addColumn(CommunityUserReportModelDto::getMessage).setHeader("Message").setSortProperty("message")
				.setSortable(true).setResizable(true).setTooltipGenerator(e -> "Message");

		DataProvider<CommunityUserReportModelDto, CommunityCriteriaNew> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCommunityFacade()
								.getAllActiveCommunitytoRerenceFlow(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()),
										formAccess)
								.stream().filter(e -> e.getFormAccess() != null).collect(Collectors.toList()).stream(),
						query -> numberOfRows
//                        FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceCount(
//                        		criteria, query.getOffset(), query.getLimit(),
//                                query.getSortOrders().stream()
//                                        .map(sortOrder -> new SortProperty(sortOrder.getSorted(),
//                                                sortOrder.getDirection() == SortDirection.ASCENDING))
//                                        .collect(Collectors.toList()),
//                                formAccess)
				);

		grid.setDataProvider(dataProvider);
		grid.setPageSize(250);
		grid.setVisible(true);

		add(grid);
	}

	private void refreshGridData(FormAccess formAccess) {
		DataProvider<CommunityUserReportModelDto, CommunityCriteriaNew> dataProvider = DataProvider
				.fromFilteringCallbacks(
						query -> FacadeProvider.getCommunityFacade()
								.getAllActiveCommunitytoRerenceFlow(criteria, query.getOffset(), query.getLimit(),
										query.getSortOrders().stream()
												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
														sortOrder.getDirection() == SortDirection.ASCENDING))
												.collect(Collectors.toList()),
										formAccess)
								.stream().filter(e -> e.getFormAccess() != null).collect(Collectors.toList()).stream(),
						query -> FacadeProvider.getCommunityFacade().getAllActiveCommunitytoRerenceCount(criteria,
								query.getOffset(), query.getLimit(),
								query.getSortOrders().stream()
										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
												sortOrder.getDirection() == SortDirection.ASCENDING))
										.collect(Collectors.toList()),
								formAccess));

		grid.setDataProvider(dataProvider);
	}
}
