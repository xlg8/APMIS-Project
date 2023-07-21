package com.cinoteck.application.views.campaign;

import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import com.vaadin.flow.component.textfield.IntegerField;


public class CampaignFormGridComponent extends VerticalLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5040277864446152755L;
	List<CampaignFormMetaReferenceDto> savedCampaignFormMetas;
	List<CampaignFormMetaReferenceDto> allCampaignFormMetas;
	Grid<CampaignFormMetaReferenceDto> grid = new Grid<>(CampaignFormMetaReferenceDto.class, false);
	CampaignDto capaingDto;
	private CampaignFormMetaReferenceDto formBeenEdited;
	private String campaignPhase;

	public CampaignFormGridComponent(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
			List<CampaignFormMetaReferenceDto> allCampaignFormMetas, CampaignDto capaingDto, String campaignPhase) {
		this.savedCampaignFormMetas = savedCampaignFormMetas;
		this.allCampaignFormMetas = allCampaignFormMetas;
		this.capaingDto = capaingDto;
		this.campaignPhase = campaignPhase;
		

		grid.addColumn(CampaignFormMetaReferenceDto::getCaption).setHeader("Form Name");
		grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired).setHeader("Expiry");
		grid.setItems(savedCampaignFormMetas);
		addClassName("list-view");
		setSizeFull();
		add(getContent());
	}

	private Component getContent() {
		VerticalLayout formx = editorForm();
		formx.getStyle().remove("width");
		HorizontalLayout content = new HorizontalLayout(grid, formx);
		content.setFlexGrow(4, grid);
		content.setFlexGrow(0, formx);
		content.addClassNames("content");
		content.setSizeFull();
		return content;
	}

	private VerticalLayout editorForm() {
		
		FormLayout formx = new FormLayout();
		VerticalLayout vert = new VerticalLayout();
		
		Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
		plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		plusButton.setTooltipText("Add new form");
		
		
		 Button deleteButton = new Button(new Icon(VaadinIcon.DEL_A));
		 deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		 deleteButton.getStyle().set("background-color", "red!important");
		 deleteButton.setTooltipText("Remove this form");
	        
	        Button saveButton = new Button("Save",
	                new Icon(VaadinIcon.CHECK));
	        
	        Button cacleButton = new Button("Cancel",
	                new Icon(VaadinIcon.REFRESH));
		
		ComboBox<CampaignFormMetaReferenceDto> forms = new ComboBox<CampaignFormMetaReferenceDto>();
		forms.setLabel("Form");
		forms.setItems(allCampaignFormMetas);
		// if its a clicked action set the value from the item....TODO

		
		IntegerField daysExpire = new IntegerField();
		daysExpire.setLabel("Days to Expiry");
		String datd = "";
		if(capaingDto != null) {
			datd = capaingDto.getStartDate().toLocaleString();
		}
		daysExpire.setHelperText("Max 60 days from Campaign Start Date "+datd);
		daysExpire.setMin(1);
		daysExpire.setMax(60);
		daysExpire.setStepButtonsVisible(true);
		// if its a clicked action set the value from the item....TODO
		
		
		 HorizontalLayout buttonLay = new HorizontalLayout(plusButton, deleteButton);
		 
		// buttonLay.setEnabled(false);
		 
		 HorizontalLayout buttonAfterLay = new HorizontalLayout(saveButton, cacleButton);
		 buttonAfterLay.getStyle().set("flex-wrap", "wrap");
		 buttonAfterLay.setJustifyContentMode(JustifyContentMode.END);
		 buttonLay.setSpacing(true);
		
		 grid.addSelectionListener(ee -> {
			
			    int size = ee.getAllSelectedItems().size();
			    if(size > 0) {
			    	 CampaignFormMetaReferenceDto selectedCamp = ee.getFirstSelectedItem().get();
					 formBeenEdited = selectedCamp;
			    boolean isSingleSelection = size == 1;
			    buttonLay.setEnabled(isSingleSelection);
			    buttonAfterLay.setEnabled(isSingleSelection);
			    
			    formx.setVisible(true);
				buttonAfterLay.setVisible(true);
				
			    //delete.setEnabled(size != 0);
			    forms.setValue(selectedCamp);
			    saveButton.setText("Update");
			    daysExpire.setValue(selectedCamp.getDaysExpired());
			    } else {
			    	formBeenEdited = new CampaignFormMetaReferenceDto();
			    }
			});
		 
		 deleteButton.addClickListener(dex->{
			 if(formBeenEdited == null) {
				 Notification.show("Please select a form first");
			 } else {

			 capaingDto.getCampaignFormMetas().remove(formBeenEdited);
			// FacadeProvider.getCampaignFacade().saveCampaign(capdto); 
			 Notification.show(formBeenEdited+" was removed from the Campaign");
			 grid.setItems(capaingDto.getCampaignFormMetas());
			 }
			 grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
		 });
		 
		 plusButton.addClickListener(ce->{
			 CampaignFormMetaReferenceDto newcampform = new CampaignFormMetaReferenceDto();
			 
			 formx.setVisible(true);
			 buttonAfterLay.setVisible(true);
			 
			 try {
				 forms.setValue(newcampform);
			 }finally {
				 saveButton.setText("Save");
				 daysExpire.setValue(5);
			 }
			 grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
			 grid.setHeight("auto !important");
			 
		 });
		 
		 cacleButton.addClickListener(ees -> {
			 CampaignFormMetaReferenceDto newcampform = new CampaignFormMetaReferenceDto();
			 
			 formx.setVisible(false);
			 buttonAfterLay.setVisible(false);
			 
			 forms.setValue(newcampform);
			 saveButton.setText("Save");
			 daysExpire.setValue(0);
			 grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
			 grid.setHeight("");
		 });
		 
		 
		 saveButton.addClickListener(e->{ 
			 
			 if(((Button) e.getSource()).getText().equals("Save")) {
				 CampaignFormMetaReferenceDto newCampForm = forms.getValue();
				
				 capaingDto.getCampaignFormMetas().add(newCampForm);
				 
				// FacadeProvider.getCampaignFacade().saveCampaign(capdto);
				 
				 Notification.show("New Form Added Successfully");
				 grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
			 } else {
				 //formBeenEdited
				 if(formBeenEdited != null) {
				 CampaignFormMetaReferenceDto newCampForm = forms.getValue();
				 CampaignDto capdto = capaingDto;
				 capaingDto.getCampaignFormMetas().remove(formBeenEdited);
				 capaingDto.getCampaignFormMetas().add(newCampForm);
				 //FacadeProvider.getCampaignFacade().saveCampaign(capdto);
				 grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
				 
				 Notification.show("Campaign Updated");
				 } else {
					 Notification.show("Please select a form before you update");
				 }
			 }
			 grid.setHeight("");
		 });
		 
		 
		formx.add(forms, daysExpire);
		formx.setColspan(forms, 1);
		formx.setColspan(daysExpire, 1);
		formx.setVisible(false);
		buttonAfterLay.setVisible(false);
		
		vert.add(buttonLay, formx, buttonAfterLay);
		
		return vert;
	}
	
	public CampaignDto getModifiedDto() {
		
		return capaingDto;
	}

//	@Override
//	protected ComponentEventListener<ClickEvent<Button>> newRowEvent() {
//
//		return event -> {
//			// CampaignFormMetaReferenceDto campaignFormMetaReferenceDto = new
//			// CampaignFormMetaReferenceDto();
//
//			final ArrayList<CampaignFormMetaReferenceDto> gridItems = getItems();
//			// gridItems.add(campaignFormMetaReferenceDto);
//			gridItems.add(new CampaignFormMetaReferenceDto(null, " --Please select--", null, 0));
//
//			grid.setItems(gridItems);
//
//			grid.getEditor().cancel();
//
//			grid.getEditor().editItem(gridItems.get(gridItems.size() - 1));
//
//		};
//	}
//
//	// public void listenerCampaignFilter(SelectedChangeEvent event) {
////    	final ArrayList<CampaignFormMetaReferenceDto> gridItems = getItems();
////    	final ArrayList<CampaignFormMetaReferenceDto> gridFilteredItems = new ArrayList<>(gridItems);
////
////        if (event.getSelectedTab().getLabel().equals("Pre-Campaign Phase")) {
////            gridFilteredItems.removeIf(n -> n.getFormType().contains("Pre-Campaign"));
////        } else if (event.getSelectedTab().getLabel().equals("Intra-Campaign Phase")) {
////            gridFilteredItems.removeIf(n -> n.getFormType().contains("Intra-Campaign"));
////        } else if (event.getSelectedTab().getLabel().equals("Post-Campaign Phase")) {
////            gridFilteredItems.removeIf(n -> n.getFormType().contains("Post-Campaign"));
////        }
////
////        grid.setItems(gridFilteredItems);
////        grid.getDataProvider().refreshAll();
////    }
//
//	public void ListnerCampaignFilter(TabSheet.SelectedTabChangeEvent event) {
//		final ArrayList<CampaignFormMetaReferenceDto> gridItemss = getItems();
//
//		final ArrayList<CampaignFormMetaReferenceDto> gridItems;
//
//		// System.out.println(event.getTabSheet().getSelectedTab().getCaption() + " |
//		// ___________---______O___");
//
//		if (event.getTabSheet().getSelectedTab().getCaption().equals("Pre-Campaign Phase")) {
//			gridItems = gridItemss;
//			gridItems.removeIf(n -> (n.getFormType().contains("Pre-Campaign")));
//
//			grid.setItems(gridItems);
//
//		} else if (event.getTabSheet().getSelectedTab().getCaption() == "Intra-Campaign Phase") {
//			gridItems = gridItemss;
//			gridItems.removeIf(n -> (n.getFormType().contains("Intra-Campaign")));
//			grid.setItems(gridItems);
//
//		} else if (event.getTabSheet().getSelectedTab().getCaption() == "Post-Campaign Phase") {
//			gridItems = gridItemss;
//			gridItems.removeIf(n -> (n.getFormType().contains("Post-Campaign")));
//			grid.setItems(gridItems);
//
//		}
//
//		grid.getDataProvider().refreshAll();
//
//	}
//
////    @Override
////    protected Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
////        final Binder<CampaignFormMetaReferenceDto> binder = new Binder<>();
////
////        ComboBox<CampaignFormMetaReferenceDto> formCombo = new ComboBox<>(Strings.entityCampaignDataForm, allElements);
////
////        TextField dateExpiring = new TextField("Date");
////        dateExpiring.setEnabled(false);
////
////
////        Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring)
////        		 .withValidator(
////                         campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
////                                 && campaignFormMetaReferenceDto != null,
////                         I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
////				.bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
////						
////						campaignFormMetaReferenceDto.getUuid(),campaignFormMetaReferenceDto.getDaysExpired()).toString(),
////						(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
////							bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta);
////							bindedCampaignFormMeta.setDateExpired(selectedCampaignFormMeta);
//////							grid.getDataProvider().refreshAll();
////						});
////	
////		
////		
////
////        Binder.Binding<CampaignFormMetaReferenceDto, CampaignFormMetaReferenceDto> formBind = binder.forField(formCombo)
////                .withValidator(
////                        campaignFormMetaReferenceDto -> campaignFormMetaReferenceDto != null
////                                && campaignFormMetaReferenceDto.getUuid() != null,
////                        I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueNull))
////                .withValidator(campaignFormMetaReferenceDto -> {
////                    ArrayList<CampaignFormMetaReferenceDto> items = getItems();
////                    return !items.contains(campaignFormMetaReferenceDto);
////                }, I18nProperties.getValidationError(Validations.campaignDashboardDataFormValueDuplicate))
////                .bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
////                                campaignFormMetaReferenceDto.getUuid(), campaignFormMetaReferenceDto.getCaption(),
////                                campaignFormMetaReferenceDto.getFormType()),
////                        (bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
////                            bindedCampaignFormMeta.setUuid(selectedCampaignFormMeta.getUuid());
////                            bindedCampaignFormMeta.setCaption(selectedCampaignFormMeta.getCaption());
////                            bindedCampaignFormMeta.setFormType(selectedCampaignFormMeta.getFormType());
////                            // workaround: grid doesn't refresh itself for an unknown reason
//////                            grid.getDataProvider().refreshAll();
////                        });
////
////        Grid.Column<CampaignFormMetaReferenceDto> formColumn = grid.addColumn(ReferenceDto::getCaption)
////                .setHeader(I18nProperties.getString(Strings.entityCampaignDataForm))
////                .setEditorComponent(column -> {
////                    formCombo.setRequired(false); // Allow empty selection
////                    formCombo.setItems(allElements);
////                    formCombo.addValueChangeListener(event -> {
////                    	if(event.getValue() != null) {
////	                        String newItem = event.getValue().toString();
////	                        if (newItem != null && !allElements.contains(newItem)) {
////	//                            allElements.add(newItem);
////	                            formCombo.setItems(allElements);
////	                        }
////                    	}
////                    });                    
////                    formCombo.getElement().addEventListener("keydown", event -> formCombo.setOpened(true));
////                    formCombo.getElement().addEventListener("click", event -> formCombo.setOpened(true));
////                    return formCombo;
////                });
////
////        Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired)
////                .setHeader("Form Deadline (Days)")
////                .setEditorComponent(column -> {
////                    dateExpiring.setRequired(false); // Allow empty selection
////                    return dateExpiring;
////                });
////
//////        Editor<CampaignFormMetaReferenceDto> editor = grid.getEditor();
//////        editor.setBinder(binder);
//////        editor.setBuffered(true);
////////        editor.setSaveCaption("Save");
//////
//////        editor.addSaveListener(event -> {
//////            // Save logic
//////        });
//////
//////        editor.addCancelListener(event -> {
//////            // Cancel logic
//////        });
//////
//////        editor.editItem(new CampaignFormMetaReferenceDto(null, "--Please Select --", null,  0));
////        
////        return binder;
////    }
//
//	@Override
//	protected Binder<CampaignFormMetaReferenceDto> addColumnsBinder(List<CampaignFormMetaReferenceDto> allElements) {
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
//		Binder.Binding<CampaignFormMetaReferenceDto, String> dateBind = binder.forField(dateExpiring)
//				.bind(campaignFormMetaReferenceDto -> new CampaignFormMetaReferenceDto(
//
//						campaignFormMetaReferenceDto.getUuid(), campaignFormMetaReferenceDto.getDaysExpired())
//								.toString(),
//
//						(bindedCampaignFormMeta, selectedCampaignFormMeta) -> {
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
//
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
//		Grid.Column<CampaignFormMetaReferenceDto> formColumn;
//		formColumn = grid.addColumn(ReferenceDto::getCaption);
//		// .setCaption(I18nProperties.getString(Strings.entityCampaignDataForm));
//
//		Grid.Column<CampaignFormMetaReferenceDto> deadlineColumn;
//		deadlineColumn = grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired);
//		// .setCaption("Form Deadline (Days)");
//		// formColumn =
//		// grid.addColumn(ReferenceDto::getFormType).setCaption(I18nProperties.getString(Strings.entityCampaignDataFormPhase));
//
//		formColumn.setId("formtb");
//
////		formColumn.setEditorBinding(null);
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
//	@Override
//	protected Button createButton(ComponentEventListener<ClickEvent<Button>> newRowEvent) {
//		Button additionalRow = new Button("Add New Row");
//		additionalRow.addClickListener(newRowEvent);
//		return additionalRow;
//	}
//
//	protected CampaignFormMetaReferenceDto createNewItem() {
//		CampaignFormMetaReferenceDto xx = new CampaignFormMetaReferenceDto();
//
//		return xx;
//	}
//
//	@Override
//	public void reorderGrid() {
//		// Implement the reorderGrid method as needed
//	}
//
//	public String getAdditionalRowCaption() {
//		return Captions.campaignAdditionalForm;
//	}
//
//	@Override
//	protected String getHeaderString() {
//		return Strings.headingCampaignData;
//	}
}
