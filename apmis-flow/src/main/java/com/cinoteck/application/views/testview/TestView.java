package com.cinoteck.application.views.testview;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignActionButtons;
import com.cinoteck.application.views.user.UserForm;
import com.cinoteck.application.views.user.UserForm.SaveEvent;
import com.cinoteck.application.views.user.UserForm.UserFormEvent;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
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
import de.symeda.sormas.api.user.UserDto;

@SuppressWarnings("serial")
@PageTitle("tstigns")
@Route(value = "testt", layout = MainLayout.class)
public class TestView extends VerticalLayout{
	
	
	private Grid<CampaignIndexDto> grid = new Grid<>(CampaignIndexDto.class, false);
	private GridListDataView<CampaignIndexDto> dataView;
	List<CampaignIndexDto> campaigns;
	private CampaignCriteria criteria;
	private CampaignFormx campaignForm;
	CampaignDto dto;

	Button newBut = new Button("New");

	public TestView() {
		newBut.addClickListener(e -> {
			newCampaign(dto);
		});

		add(newBut);
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
		ListDataProvider<CampaignIndexDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getCampaignFacade().getIndexList(criteria, null, null, null).stream());

		dataView = grid.setItems(dataProvider);

		grid.asSingleSelect().addValueChangeListener(event -> editCampaign(event.getValue()));

		add(grid);

	}


	private void editCampaign(CampaignIndexDto selected) {
		selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			CampaignDto formData = FacadeProvider.getCampaignFacade().getByUuid(selected.getUuid());
			openFormLayout(formData);
		}
	}

	private void newCampaign(CampaignDto formData) {
		openFormLayout(formData);

	}

	private void openFormLayout(CampaignDto formData) {

		CampaignFormx formLayout = new CampaignFormx(formData);
		formLayout.setCampaign(formData);
		Dialog dialog = new Dialog();
		dialog.add(formLayout);
		dialog.setSizeFull();
		dialog.open();

		CampaignActionButtons actionButtons = new CampaignActionButtons();

	}
}
