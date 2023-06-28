package com.cinoteck.application.views.testview;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;

@SuppressWarnings("serial")
@PageTitle("tstigns")
@Route(value = "testt", layout = MainLayout.class)
public class TestView  extends VerticalLayout {
	private Grid<CampaignIndexDto> grid = new Grid<>(CampaignIndexDto.class, false);
	private GridListDataView<CampaignIndexDto> dataView;
	List<CampaignIndexDto> campaigns;
	private CampaignCriteria criteria;
	private CampaignFormx campaignForm;
	CampaignIndexDto campaignIndexDto;
	CampaignDto dto;
	public TestView() {

		 createGrid();
	}
	
	
	public void createGrid() {
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
		ListDataProvider<CampaignIndexDto> dataProvider =
				DataProvider.fromStream(FacadeProvider.getCampaignFacade().getIndexList(criteria, null, null, null).stream());
		System.out.println(criteria + "is not null");
		
//		campaigns = FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference();
		dataView = grid.setItems(dataProvider);
		
	
		grid.asSingleSelect().addValueChangeListener(event -> editCampaign(event.getValue()));

		add(grid);
		

		
		
	}
	
//	public void editCampaign(CampaignIndexDto campaignIndexDto) {
//		System.out.println(campaignIndexDto + "rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
//		this.dto = new CampaignDto();
//		campaignForm = new CampaignFormx(dto);
//		campaignForm.setCampaign(campaignIndexDto);
//		campaignForm.setVisible(true);
//		campaignForm.setSizeFull();
//		grid.setVisible(false);
//		addClassName("editing");
////		if (campaignIndexDto == null) {
////			campaignForm.setVisible(true);
////			campaignForm.setSizeFull();
////			grid.setVisible(false);
//////			setFiltersVisible(false);
////			addClassName("editing");
////		}
////		else {
////
////		campaignForm.setCampaign(campaignIndexDto);
////		campaignForm.setVisible(true);
////		campaignForm.setSizeFull();
////		grid.setVisible(false);
//////		setFiltersVisible(false);
////		addClassName("editing");
////		}
//
//	}
	
	private void editCampaign(CampaignIndexDto selected) {
		selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			CampaignDto formData = FacadeProvider.getCampaignFacade()
					.getByUuid(selected.getUuid());
			openFormLayout(formData);
		}
	}
	
	
	private void openFormLayout(CampaignDto formData) {


		CampaignFormx formLayout = new CampaignFormx(formData);
		formLayout.setCampaign(formData);
		Dialog dialog = new Dialog();
		dialog.add(formLayout);
		dialog.setSizeFull();
		dialog.open();
	}


}

