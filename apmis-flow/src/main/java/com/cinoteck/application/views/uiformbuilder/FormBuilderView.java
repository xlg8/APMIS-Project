package com.cinoteck.application.views.uiformbuilder;

import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.FormAccess;

@PageTitle("APMIS-UI-Builder")
@Route(value = "UI-Builder", layout = MainLayout.class)
public class FormBuilderView extends VerticalLayout {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	Button hideFilters;
	TextField search;
	ComboBox<CampaignPhase> formPhase;
	ComboBox<FormAccess> formAccess;
	Button newForm;
	
	CampaignFormMetaDto campaignFormMetaDto;

	HorizontalLayout hr = new HorizontalLayout();

	private Grid<CampaignFormMetaDto> grid = new Grid<>(CampaignFormMetaDto.class, false);

	private GridListDataView<CampaignFormMetaDto> dataView;

	public FormBuilderView() {

		this.setSizeFull();
		this.setHeightFull();
		this.setWidthFull();
		this.addClassName("uibuilderview");

		configureView();
		configureGrid();

		hr.setAlignItems(Alignment.END);
		hr.add(hideFilters, search, formPhase, formAccess, newForm);
		add(hr, grid);
	}

	public void configureView() {

		hideFilters = new Button("Hide Filters");
		search = new TextField("Search");
		formPhase = new ComboBox<>("Campaign Phase");
		formPhase.setItems(CampaignPhase.values());
		formAccess = new ComboBox<>("Form Access");
		formAccess.setItems(FormAccess.values());
		newForm = new Button("New Forms");
		
		
	}

	public void configureGrid() {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CampaignFormMetaDto.FORM_ID).setHeader("Form Name").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormMetaDto.FORM_CATEGORY).setHeader("Form Category").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormMetaDto.CREATION_DATE).setHeader("Creation Date").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormMetaDto.CHANGE_DATE).setHeader("Change Date").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormMetaDto.DAYSTOEXPIRE).setHeader("Days To Expire").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormMetaDto.DISTRICTENTRY).setHeader("District Entry").setSortable(true)
				.setResizable(true);

		ListDataProvider<CampaignFormMetaDto> dataprovider = DataProvider
				.fromStream(FacadeProvider.getCampaignFormMetaFacade().getAllFormElement().stream());

		dataView = grid.setItems(dataprovider);
		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);
	}
	
}
