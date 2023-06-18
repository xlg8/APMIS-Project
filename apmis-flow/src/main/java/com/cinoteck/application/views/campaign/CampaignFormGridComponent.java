package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.Binding;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.ReferenceDto;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.user.UserDto;

public class CampaignFormGridComponent<T> extends VerticalLayout
{
	List<CampaignFormMetaReferenceDto> campaignFormMetaReferences = FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(null);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	CampaignDto cam;
	Grid<CampaignFormMetaReferenceDto> grid = new Grid<>(CampaignFormMetaReferenceDto.class, false);
	List<CampaignFormMetaReferenceDto> rowDataList ;//= FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences();
	GridListDataView<de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto> camapaignFormGridItem;

//	ListDataProvider<CampaignFormMetaReferenceDto> dataProvider = new ListDataProvider<>(rowDataList);
//	public CampaignFormGridComponent() {
//		// TODO Auto-generated constructor stub
//	}
	
//	public CampaignFormGridComponent(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
//			List<CampaignFormMetaReferenceDto> allCampaignFormMetasAsReferencesByRound) {
//		// TODO Auto-generated constructor stub
//		setHeightFull();
//		configureEditGrid(rowDataList);
//	}
	
	public CampaignFormGridComponent(String round) {
		super();
//		setHeightFull();
//////		super(savedCampaignFormMetas, allCampaignFormMetas);
////
//		configureEditGrid(campaignFormMetaReferences);
//
//		// Add a new row button
//		Button addRowButton = new Button("Add Row", event -> {
//			rowDataList.add(new CampaignFormMetaReferenceDto(cam.UUID));
//	
//		});
////
////		// Add the grid and the add row button to the layout
////		add(grid);
//		add(addRowButton);
		 List<CampaignFormMetaReferenceDto> campaignFormMetas = FacadeProvider.getCampaignFormMetaFacade()
	                .getAllCampaignFormMetasAsReferencesByRound(round);

		grid.setSelectionMode(SelectionMode.SINGLE);
	    grid.setMultiSort(true, MultiSortPriority.APPEND);
	    grid.setColumnReorderingAllowed(true);
	    
	    grid.addColumn(CampaignFormMetaReferenceDto::getCaption).setHeader("Form Name").setSortable(true).setResizable(true);
	    grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Days Expired").setSortable(true).setResizable(true);
	    
	    grid.setVisible(true);
	    grid.setWidthFull();
	    grid.setAllRowsVisible(false);
	    
	    grid.setItems(campaignFormMetas);
	    
	    add(grid);
	}
	
	

	

//	public void configureEditGrid(List<CampaignFormMetaReferenceDto> allLists) {
//		
//		grid.setSelectionMode(SelectionMode.SINGLE);
//		grid.setMultiSort(true, MultiSortPriority.APPEND);
////		grid.setSizeFull();
//		grid.setColumnReorderingAllowed(true);
//		
//		grid.addColumn(de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto::getCaption).setHeader("Form Name").setSortable(true).setResizable(true);
//		grid.addColumn(de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Days Expired").setSortable(true).setResizable(true);
//
//		grid.setVisible(true);
//		grid.setWidthFull();
////		grid.setHeightFull();
//		grid.setAllRowsVisible(false);
//		
////		List<CampaignFormMetaReferenceDto> regions = allLists ;
//		camapaignFormGridItem = grid.setItems(FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences());
// 
////		grid.asSingleSelect().addValueChangeListener(event -> editContact(event.getValue()));
//		add(grid);
//	}
	
//	public void configureEditGrid(List<CampaignFormMetaReferenceDto> allLists) {
//	    grid.setSelectionMode(SelectionMode.SINGLE);
//	    grid.setMultiSort(true, MultiSortPriority.APPEND);
//	    grid.setColumnReorderingAllowed(true);
//	    
//	    grid.addColumn(CampaignFormMetaReferenceDto::getCaption).setHeader("Form Name").setSortable(true).setResizable(true);
//	    grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Days Expired").setSortable(true).setResizable(true);
//	    
//	    grid.setVisible(true);
//	    grid.setWidthFull();
//	    grid.setAllRowsVisible(false);
//	    
//	    camapaignFormGridItem = grid.setItems(allLists);
//	    
//	    add(grid);
//	}



//
//
//	public void ListnerCampaignFilter(TabSheet.SelectedChangeEvent event) {
//		final ArrayList<CampaignFormMetaReferenceDto> gridItemss = (ArrayList<CampaignFormMetaReferenceDto>) getItems();
//
//		final ArrayList<CampaignFormMetaReferenceDto> gridItems;
//
//		// System.out.println(event.getTabSheet().getSelectedTab().getCaption() + " |
//		// ___________---______O___");
//
//		if (event.getSelectedTab().getLabel().equals("Pre-Campaign Phase")) {
//			gridItems = gridItemss;
//			gridItems.removeIf(n -> (n.getFormType().contains("Pre-Campaign")));
//
//			grid.setItems(gridItems);
//
//		} else if (event.getSelectedTab().getLabel().equals("Intra-Campaign Phase")) {
//			gridItems = gridItemss;
//			gridItems.removeIf(n -> (n.getFormType().contains("Intra-Campaign")));
//			grid.setItems(gridItems);
//
//		} else if (event.getSelectedTab().getLabel().equals("Post-Campaign Phase")) {
//			gridItems = gridItemss;
//			gridItems.removeIf(n -> (n.getFormType().contains("Post-Campaign")));
//			grid.setItems(gridItems);
//
//		}
//
//		grid.getDataProvider().refreshAll();
//
//	}

//	public ArrayList<T> getItems() {
//		return (ArrayList) ((ListDataProvider) ( ((Collection) this.grid).iterator().next()))
//			.getItems();
//	}
//	

//	@Override
//	public Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
//
//		// todo check if we can remove elements that are null
//		final Binder<CampaignFormMetaReferenceDto> binder = new Binder<>();
//
//		// This is a bit hacky: The grid is used here to "select" the whole item instead
//		// of editing properties
//		// This is done by replacing uuid and caption of the item
//
//		ComboBox<CampaignFormMetaReferenceDto> formCombo = new ComboBox<>(Strings.entityCampaignDataForm, allElements);
//
//		TextField dateExpiring = new TextField("Date");
//
//		dateExpiring.setEnabled(false);
//
//		com.vaadin.flow.data.binder.Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder
//				.forField(dateExpiring).bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
//
//						campaignFormMetaReferenceDto.getUuid(), campaignFormMetaReferenceDto.getDaysExpired())
//						.toString(), (bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
//							bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
//							bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
//							grid.getDataProvider().refreshAll();
//						});
//
//		Binder.Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
//				.withValidator(
//						campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
//								&& campaignFormMetaReferenceDto.getUuid() != null,
//						I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
//
//				.withValidator(campaignFormMetaReferenceDto -> {
//					ArrayList<CampaignFormMetaReferenceDto> items = getItems();
//					return !items.contains(campaignFormMetaReferenceDto);
//				}, I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueDuplicate))
//				.bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
//						campaignFormMetaReferenceDto.getUuid(), campaignFormMetaReferenceDto.getCaption(),
//						campaignFormMetaReferenceDto.getFormType()),
//						(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
//							bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta.getUuid());
//							bindedCampaignFormMeta.setCaption(selectedCampaignFormMeta.getCaption());
//							bindedCampaignFormMeta.setFormType(selectedCampaignFormMeta.getFormType());
//							// workarround: grid doesn't refresh itself for unknown reason
//							grid.getDataProvider().refreshAll();
//						});
////		formCombo.setEmptySelectionAllowed(false);
////
////		Grid.Column<CampaignFormMetaReferenceDto, String> formColumn;
////		formColumn = grid.addColumn(ReferenceDto::getCaption)
////				.setCaption(I18nProperties.getString(Strings.entityCampaignDataForm));
////		
////		Grid.Column<CampaignFormMetaReferenceDto, Integer> deadlineColumn;
////		deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired)
////				.setCaption("Form Deadline (Days)");
//		// formColumn =
//		// grid.addColumn(ReferenceDto::getFormType).setCaption(I18nProperties.getString(Strings.entityCampaignDataFormPhase));
////
////		formColumn.setId("formtb");
////		formColumn.setEditorBinding(formBind);
////		
////		
////		deadlineColumn.setEditorBinding(dateBind);
//
//		/*
//		 * Grid.Column<CampaignFormMetaReferenceDto, String> formColumnx =
//		 * grid.addColumn(ReferenceDto::getUuid)
//		 * .setCaption(I18nProperties.getString(Strings.entityCampaignDataForm));
//		 * formColumnx.setId("formtbv"); formColumnx.setEditorBinding(formBind);
//		 */
//
//		return binder;
//	}
//
//	public String getHeaderString() {
//		return Strings.headingCampaignData;
//	}
////
////	@Override
////	public void reorderGrid() {
////	}
//
//	public String getAdditionalRowCaption() {
//		return Captions.campaignAdditionalForm;
//	}

//	@Override
//	protected Button createButton(String additionalRowCaption, ComponentEventListener<ClickEvent<Button>> newRowEvent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected void reorderGrid() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	protected Button createButton(String additionalRowCaption, ComponentEventListener<ClickEvent<Button>> newRowEvent) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
