package com.cinoteck.application.views.testview;

import java.util.List;
import java.util.stream.Collectors;

import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.utils.SortProperty;

@SuppressWarnings("serial")
@PageTitle("tstigns")
@Route(value = "testt", layout = MainLayout.class)
public class TestView  extends VerticalLayout {
	private Grid<CampaignIndexDto> grid = new Grid<>(CampaignIndexDto.class, false);
	private GridListDataView<CampaignIndexDto> dataView;
	List<CampaignIndexDto> campaigns;
	private CampaignCriteria criteria;
	private CampaignForm campaignForm;

	
	
	public TestView() {
		this.criteria = new CampaignCriteria();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		grid.addColumn(CampaignIndexDto.NAME).setHeader("Name").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.CAMPAIGN_STATUS).setHeader("Status").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.START_DATE).setHeader("Start Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.END_DATE).setHeader("End Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignIndexDto.CAMPAIGN_YEAR).setHeader("Campaign Year").setSortable(true).setResizable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);

//		DataProvider<CampaignIndexDto, CampaignCriteria> dataProvider = DataProvider.fromFilteringCallbacks(
//				query -> FacadeProvider.getCampaignFacade()
//						.getIndexList(query.getFilter().orElse(null), query.getOffset(), query.getLimit(),
//								query.getSortOrders().stream()
//										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
//												sortOrder.getDirection() == SortDirection.ASCENDING))
//										.collect(Collectors.toList()))
//						.stream(),
//				query -> (int) FacadeProvider.getCampaignFacade().count(query.getFilter().orElse(null)));
//		
//		s
//		grid.setDataProvider(dataProvider);
		ListDataProvider<CampaignIndexDto> dataProvider =
				DataProvider.fromStream(FacadeProvider.getCampaignFacade().getIndexList(criteria, null, null, null).stream());
		System.out.println(criteria + "is not null");
		
//		campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		dataView = grid.setItems(dataProvider);
		
		grid.asSingleSelect().addValueChangeListener(event -> editCampaign(event.getValue()));

		add(grid);
		
		
	}
	
	public void editCampaign(CampaignIndexDto campaignIndexDto) {
		if (campaignIndexDto == null) {
			campaignForm.setVisible(true);
			campaignForm.setSizeFull();
			grid.setVisible(false);
//			setFiltersVisible(false);
			addClassName("editing");
		}
		else {

//		campaignForm.setCampaign(campaignIndexDto);
		campaignForm.setVisible(true);
		campaignForm.setSizeFull();
		grid.setVisible(false);
//		setFiltersVisible(false);
		addClassName("editing");
		}

	}
}

