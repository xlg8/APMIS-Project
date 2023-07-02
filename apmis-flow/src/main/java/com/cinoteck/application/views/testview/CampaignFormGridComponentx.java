package com.cinoteck.application.views.testview;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Binding;
import javax.swing.plaf.basic.BasicOptionPaneUI.ButtonActionListener;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheet.SelectedChangeEvent;
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

	public class CampaignFormGridComponentx extends AbstractEditableGridx<CampaignFormMetaReferenceDto> {
		
		

	    public CampaignFormGridComponentx(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
	                                      List<CampaignFormMetaReferenceDto> allCampaignFormMetas) {
	        super(savedCampaignFormMetas, allCampaignFormMetas);
//	        setSizeFull();
	    }

	    @Override
	    protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
	        return event -> {
	            List<CampaignFormMetaReferenceDto> gridItems = getItems();
	            gridItems.add(new CampaignFormMetaReferenceDto(null, " --Please select--", null, 0));
	            grid.setItems(gridItems);
	            grid.getEditor().cancel();
//	            grid.getEditor().editRow(gridItems.size() - 1);
	            
	        };
	    }

	    public void ListnerCampaignFilter(SelectedChangeEvent event) {
	        List<CampaignFormMetaReferenceDto> gridItemss = getItems();
	        List<CampaignFormMetaReferenceDto> gridItems;

	        if (event.getSelectedTab().getLabel().equals("Pre-Campaign Phase")) {
	            gridItems = new ArrayList<>(gridItemss);
	            gridItems.removeIf(n -> n.getFormType().contains("Pre-Campaign"));
	            grid.setItems(gridItems);
	        } else if (event.getSelectedTab().getLabel().equals("Intra-Campaign Phase")) {
	            gridItems = new ArrayList<>(gridItemss);
	            gridItems.removeIf(n -> n.getFormType().contains("Intra-Campaign"));
	            grid.setItems(gridItems);
	        } else if (event.getSelectedTab().getLabel().equals("Post-Campaign Phase")) {
	            gridItems = new ArrayList<>(gridItemss);
	            gridItems.removeIf(n -> n.getFormType().contains("Post-Campaign"));
	            grid.setItems(gridItems);
	        }

	        grid.getDataProvider().refreshAll();
	    }

	    
	    @Override
		public Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
	
			// todo check if we can remove elements that are null
			final Binder<CampaignFormMetaReferenceDto> binder = new Binder<CampaignFormMetaReferenceDto>(CampaignFormMetaReferenceDto.class);
	
			// This is a bit hacky: The grid is used here to "select" the whole item instead
			// of editing properties
			// This is done by replacing uuid and caption of the item
	
			ComboBox<CampaignFormMetaReferenceDto> formCombo = new ComboBox<>(Strings.entityCampaignDataForm, allElements);
	
			TextField dateExpiring = new TextField("Date");
	
			dateExpiring.setEnabled(false);
	
			Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring).bind(
					campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(campaignFormMetaReferenceDto.getUuid(),
							campaignFormMetaReferenceDto.getDaysExpired()).toString(),
					(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
						bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
						bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
						grid.getDataProvider().refreshAll();
					});
	
			Binder.Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
					.withValidator(
							campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
									&& campaignFormMetaReferenceDto.getUuid() != null,
							I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
					.withValidator(campaignFormMetaReferenceDto -> {
						ArrayList<CampaignFormMetaReferenceDto> items = getItems();
						return !items.contains(campaignFormMetaReferenceDto);
					}, I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueDuplicate))
					.bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
							campaignFormMetaReferenceDto.getUuid(), campaignFormMetaReferenceDto.getCaption(),
							campaignFormMetaReferenceDto.getFormType()),
							(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
								bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta.getUuid());
								bindedCampaignFormMeta.setCaption(selectedCampaignFormMeta.getCaption());
								bindedCampaignFormMeta.setFormType(selectedCampaignFormMeta.getFormType());
								// workarround: grid doesn't refresh itself for unknown reason
								grid.getDataProvider().refreshAll();
							});
//			formCombo.setEmptySelectionAllowed(false);
	
			Grid.Column<CampaignFormMetaReferenceDto> formColumn;
			formColumn = grid.addColumn(ReferenceDto::getCaption).setHeader("Form Name");
	
			Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn;
			deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Form Deadline (Days)");
	
//			formColumn.setEditorBinding(formBind);
//			
//			
//			deadlineColumn.setEditorBinding(dateBind);
	
			return binder;
		}
	    
//	    @Override
//	    protected Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
//	        Binder<CampaignFormMetaReferenceDto> binder = new Binder<>();
//
//	        ComboBox<CampaignFormMetaReferenceDto> formCombo = new ComboBox<>("Form", allElements);
//	        formCombo.setRequired(true);
//	        formCombo.setItemLabelGenerator(CampaignFormMetaReferenceDto::getCaption);
//
//	        TextField deadlineField = new TextField("Form Deadline (Days)");
//	        deadlineField.setEnabled(false);
//	        
////			Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring).bind(
////			campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(campaignFormMetaReferenceDto.getUuid(),
////					campaignFormMetaReferenceDto.getDaysExpired()).toString(),
////			(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
////				bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
////				bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
////				grid.getDataProvider().refreshAll();
////			});
//	        
//	        
//	        Binding<CampaignFormMetaReferenceDto, Integer> deadlineBind = binder.forField(deadlineField)
//	                .bind(CampaignFormMetaReferenceDto::getDaysExpired, null);
//
//	        Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
//	                .asRequired(I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
//	                .withValidator(campaignFormMetaReferenceDto -> !getItems().contains(campaignFormMetaReferenceDto),
//	                        I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueDuplicate))
//	                .bind(CampaignFormMetaReferenceDto::new, CampaignFormMetaReferenceDto::update);
//
//	        Grid.Column<CampaignFormMetaReferenceDto> formColumn = grid.addColumn(CampaignFormMetaReferenceDto::getCaption)
//	                .setHeader("Form");
//	        formColumn.setEditorComponent(formCombo);
//
//	        Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired)
//	                .setHeader("Form Deadline (Days)");
//	        deadlineColumn.setEditorComponent(deadlineField);
//
//	        formColumn.setEditorBinding(formBind);
//	        deadlineColumn.setEditorBinding(deadlineBind);
//
//	        return binder;
//	    }

	    @Override
	    protected String getHeaderString() {
	        return "Campaign Data";
	    }

	    @Override
	    protected void reorderGrid() {
	        // No specific reordering logic in this method
	    }

	    @Override
	    protected String getAdditionalRowCaption() {
	        return "Additional Form";
	    }
	}


//	public CampaignFormGridComponentx(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
//			List<CampaignFormMetaReferenceDto> allCampaignFormMetas, String phase) {
//		super(savedCampaignFormMetas, allCampaignFormMetas);
//
//	}
//}
//		Grid<RowData> grid = new Grid<>();
//		List<RowData> rowDataList = new ArrayList<>();
//		ListDataProvider<RowData> dataProvider = new ListDataProvider<>(rowDataList);
//		grid.setDataProvider(dataProvider);
////        grid.addColumn(CampaignFormMetaReferenceDto::getDateExpired).setHeader("Form Name");
//
//		grid.addComponentColumn(RowData::getComboBox).setHeader("Form Name");
//		grid.addComponentColumn(RowData::getDeadline).setHeader("Dead Line");
//
//		// Add a new row button
//		Button addRowButton = new Button("Add Row", event -> {
//			rowDataList.add(new RowData(cam.UUID));
//			dataProvider.refreshAll();
//		});

		// Add the grid and the add row button to the layout
//		add(grid, addRowButton);
		
//		 List<CampaignFormMetaReferenceDto> campaignFormMetas = FacadeProvider.getCampaignFormMetaFacade()
//	                .getAllCampaignFormMetasAsReferencesByRound(phase);
//		 
//		ComboBox<CampaignFormMetaReferenceDto> formcomboBox;
//		List<CampaignFormMetaReferenceDto> campaigns;
//		campaigns = FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferencesByRound(phase);
////		System.out.println(round + "campaign rounds sssss");
//		formcomboBox.setItems(campaigns);
//		formcomboBox.setPlaceholder("--Please Select--");
//
//		grid.setSelectionMode(SelectionMode.SINGLE);
//	    grid.setMultiSort(true, MultiSortPriority.APPEND);
//	    grid.setColumnReorderingAllowed(true);
//	    
//	    grid.addComponentColumn(CampaignFormMetaReferenceDto::getCaption).setHeader("Form Name").setSortable(true).setResizable(true);
//	    grid.addComponentColumn(CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Days Expired").setSortable(true).setResizable(true);
//	    
//	    grid.setVisible(true);
//	    grid.setWidthFull();
//	    grid.setAllRowsVisible(false);
//	    
//	    grid.setItems(campaignFormMetas);
//	    
//	    add(grid);
//	}


	// Data class representing each row in the grid
//	public static class RowData {
//		private ComboBox<CampaignFormMetaReferenceDto> formcomboBox;
//		private TextField deadline;
//		private List<CampaignFormMetaReferenceDto> campaigns;
//
//		public RowData(String round) {
//			this.formcomboBox = new ComboBox<>();
//			this.deadline = new TextField();
//			
//			campaigns = FacadeProvider.getCampaignFormMetaFacade().getAllCampaignFormMetasAsReferences();
//			System.out.println(round + "campaign rounds sssss");
//			formcomboBox.setItems(campaigns);
//			formcomboBox.setPlaceholder("--Please Select--");
//			deadline.setPlaceholder("--Enter Deadline--");
//
//		}
//
//		public ComboBox<CampaignFormMetaReferenceDto> getComboBox() {
////        	formcomboBox.setWidthFull();
//			return formcomboBox;
//		}
//
//		public TextField getDeadline() {
////        	deadline.setWidthFull();
//			return deadline;
//		}
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
//		Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring).bind(
//				campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(campaignFormMetaReferenceDto.getUuid(),
//						campaignFormMetaReferenceDto.getDaysExpired()).toString(),
//				(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
//					bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
//					bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
//					grid.getDataProvider().refreshAll();
//				});
//
//		Binder.Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
//				.withValidator(
//						campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
//								&& campaignFormMetaReferenceDto.getUuid() != null,
//						I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
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
//
//		Grid.Column<CampaignFormMetaReferenceDto> formColumn;
//		formColumn = grid.addColumn(ReferenceDto::getCaption).setHeader("Form Name");
//
//		Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn;
//		deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Form Deadline (Days)");
//
////		formColumn.setEditorBinding(formBind);
////		
////		
////		deadlineColumn.setEditorBinding(dateBind);
//
//		return binder;
//	}
//
//	public String getHeaderString() {
//		return Strings.headingCampaignData;
//	}
//
//	@Override
//	public void reorderGrid() {
//	}
//
//	public String getAdditionalRowCaption() {
//		return Captions.campaignAdditionalForm;
//	}
//
//	@Override
//	protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Button createButton(String additionalRowCaption, ComponentEventListener<ClickEvent<Button>> newRowEvent) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected Class<CampaignFormMetaReferenceDto> getBeanType() {
//		return CampaignFormMetaReferenceDto.class;
//	}

//	@Override
//	protected void configureBindings() {
//
//		Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(grid.getColumnByKey("daysExpired")).bind(
//				campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(campaignFormMetaReferenceDto.getUuid(),
//						campaignFormMetaReferenceDto.getDaysExpired()).toString(),
//				(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
//					bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
//					bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
//					grid.getDataProvider().refreshAll();
//				});
//		Binder.Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
//				.withValidator(
//						campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
//								&& campaignFormMetaReferenceDto.getUuid() != null,
//						I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
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
//
//	}

//	@Override
//	protected CampaignFormMetaReferenceDto createNewItem() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	protected void configureColumns() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected void configureBindings() {
//		// TODO Auto-generated method stub
//		
//	}
	
	 

