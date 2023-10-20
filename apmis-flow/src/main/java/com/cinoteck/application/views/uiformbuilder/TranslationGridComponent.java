package com.cinoteck.application.views.uiformbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;

import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;

public class TranslationGridComponent extends VerticalLayout {

	ComboBox<String> languageCode = new ComboBox<String>("Tranlation Language Code");
	TextField elementId = new TextField("Element Id");
	TextField caption = new TextField("Caption");

	CampaignFormMetaDto campaignFormMetaDto;	
	CampaignFormTranslations campaignFormTranslations = new CampaignFormTranslations();
	
	TranslationElement translationBeenEdited;
	TranslationElement newTranslation;
	List<TranslationElement> translationSet = new ArrayList<>();
	FormLayout formLayout = new FormLayout();
	
	HorizontalLayout vr3 = new HorizontalLayout();
	HorizontalLayout vr1 = new HorizontalLayout();

	private Grid<CampaignFormTranslations> outerGrid = new Grid<>(CampaignFormTranslations.class, false);
	private GridListDataView<CampaignFormTranslations> outerDataView;
	
	private Grid<TranslationElement> grid = new Grid<>(TranslationElement.class, false);
	private GridListDataView<TranslationElement> dataView;

	private boolean isNewForm = false;

	public TranslationGridComponent(CampaignFormMetaDto campaignFormMetaDto) {
		this.campaignFormMetaDto = campaignFormMetaDto;

		addClassName("translation-grid-component");
		setSizeFull();
		configureFields();
		add(getContent());
		configureGrid();
	}

	private void configureGrid() {

		ComponentRenderer<Span, CampaignFormTranslations> elementIdRenderer = new ComponentRenderer<>(event -> {
			List<TranslationElement> bf = event.getTranslations();
			String[] hf = String.valueOf(bf.toString()).replace("[", "").replace("]", "").replace("null,", "")
					.replace("null", "").split(",");
			Span id = new Span(hf[0].toString());
			return id;
		});

		ComponentRenderer<Span, CampaignFormTranslations> captionRenderer = new ComponentRenderer<>(event -> {
			List<TranslationElement> bf = event.getTranslations();
			String[] hf = String.valueOf(bf.toString()).replace("[", "").replace("]", "").replace("null,", "")
					.replace("null", "").split(",");
			Span id = new Span(hf[1].toString());
			return id;
		});
		
		outerGrid.setSelectionMode(SelectionMode.SINGLE);
		outerGrid.setMultiSort(true, MultiSortPriority.APPEND);
		outerGrid.setSizeFull();
		outerGrid.setColumnReorderingAllowed(true);

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		outerGrid.addColumn(CampaignFormTranslations::getLanguageCode).setHeader("Language Code").setSortable(true)
				.setResizable(true);
		
		List<CampaignFormTranslations> existingFormTranslations = campaignFormMetaDto.getCampaignFormTranslations();
		existingFormTranslations = existingFormTranslations == null ? new ArrayList<>() : existingFormTranslations;
		ListDataProvider<CampaignFormTranslations> outerDataprovider = DataProvider
				.fromStream(existingFormTranslations.stream());
		
//		grid.addColumn(TranslationElement::getElementId).setHeader("Element Id").setSortable(true).setResizable(true);
//		grid.addColumn(TranslationElement::getCaption).setHeader("Caption").setSortable(true).setResizable(true);		
//		
//		List<TranslationElement> allTranslations = existingFormTranslations.stream()
//			    .flatMap(abc -> abc.getTranslations().stream()) // Extract the translations and flatten the nested streams
//			    .collect(Collectors.toList());
//		allTranslations = allTranslations == null ? new ArrayList<>() : allTranslations;
//		ListDataProvider<TranslationElement> dataprovider = DataProvider
//				.fromStream(allTranslations.stream());

		outerDataView = outerGrid.setItems(outerDataprovider);
		outerGrid.setVisible(true);
		outerGrid.setAllRowsVisible(true);
		
//		dataView = grid.setItems(dataprovider);
//		grid.setVisible(false);
//		grid.setAllRowsVisible(true);
	}

	private void configureFields() {

		languageCode.setItems("ps_AF", "fa_AF");
		languageCode.setHelperText("You only need to select this field once");
	}

	private Component getContent() {

		VerticalLayout editorLayout = editForm();
		editorLayout.getStyle().remove("width");
		HorizontalLayout layout = new HorizontalLayout(outerGrid, grid, editorLayout);
		layout.setFlexGrow(4, grid);
		layout.setFlexGrow(0, editorLayout);
		layout.setSizeFull();
		return layout;
	}

	private VerticalLayout editForm() {

		VerticalLayout vrsub = new VerticalLayout();

		Button plus = new Button(new Icon(VaadinIcon.PLUS));
		Button del = new Button(new Icon(VaadinIcon.DEL_A));
		del.getStyle().set("background-color", "red!important");

		Button cancel = new Button("Cancel");
		Button save = new Button("Save");

		formLayout.setVisible(false);
		vr3.setVisible(false);

		vr1.add(plus, del);

		vr3.add(save, cancel);
		vr3.setJustifyContentMode(JustifyContentMode.END);
		vr3.setSpacing(true);

		vrsub.add(vr1, formLayout, vr3);
		
		outerGrid.addSelectionListener(eee -> {
			grid.setVisible(true);
			outerGrid.setVisible(false);	
			
			grid.addColumn(TranslationElement::getElementId).setHeader("Element Id").setSortable(true).setResizable(true);
			grid.addColumn(TranslationElement::getCaption).setHeader("Caption").setSortable(true).setResizable(true);		
			
			List<TranslationElement> allTranslations = eee.getFirstSelectedItem().get().getTranslations().stream()
//				    .flatMap(abc -> abc.getTranslations().stream()) // Extract the translations and flatten the nested streams
				    .collect(Collectors.toList());
			allTranslations = allTranslations == null ? new ArrayList<>() : allTranslations;
			ListDataProvider<TranslationElement> dataprovider = DataProvider
					.fromStream(allTranslations.stream());
			
			dataView = grid.setItems(dataprovider);
//			grid.setVisible(false);
			grid.setAllRowsVisible(true);
		});

		grid.addSelectionListener(ee -> {

			save.setText("Update");
			int size = ee.getAllSelectedItems().size();
			if (size > 0) {

				translationBeenEdited = ee.getFirstSelectedItem().get();
				boolean isSingleSelection = size == 1;
				vr1.setEnabled(isSingleSelection);
				vr3.setEnabled(isSingleSelection);

				formLayout.setVisible(true);
				vr3.setVisible(true);
				
				CampaignFormTranslations campaignFormTranslations = new CampaignFormTranslations();
				
//				
//				if (formBeenEdited.getTranslations() != null) {				
//					
//					TranslationElement translationElement = formBeenEdited.;
//					caption.setValue(translationElement.getCaption());
//					caption.setVisible(true);
//				}
//				
//				if (formBeenEdited.getCaption() != null) {
//					caption.setValue(formBeenEdited.getCaption());
//					caption.setVisible(true);
//				}
			}
		});

		plus.addClickListener(e -> {

			save.setText("Save");

			formLayout.setVisible(true);
			vr3.setVisible(true);
			vr1.setVisible(false);

			elementId.setValue("");
			caption.setValue("");
			
			newTranslation = new TranslationElement();

			if (campaignFormMetaDto == null) {
				campaignFormMetaDto = new CampaignFormMetaDto();
							
				grid.setItems(translationSet);
			} else {
				grid.setItems(translationSet);
			}

			grid.setHeight("auto !important");
		});

		del.addClickListener(e -> {

			translationBeenEdited = new TranslationElement();
			if (translationBeenEdited != null) {

				campaignFormMetaDto.getCampaignFormElements().remove(translationBeenEdited);
				Notification.show("Selected Form Translation Deleted");
				grid.setItems(translationBeenEdited);
			} else {

				Notification.show("No Form Translation Selected");
			}
		});

		cancel.addClickListener(e -> {

			vr1.setVisible(true);
			formLayout.setVisible(false);
			vr3.setVisible(false);

			elementId.setVisible(false);
			caption.setVisible(false);

			elementId.setValue("");
			caption.setValue("");

			save.setText("Save");
			grid.setItems(translationSet);
		});

		save.addClickListener(e -> {

//			CampaignFormTranslations newform = new CampaignFormTranslations();

			vr1.setVisible(true);
			formLayout.setVisible(false);
			vr3.setVisible(false);

			if (((Button) e.getSource()).getText().equals("Save")) {
				newTranslation = new TranslationElement();
				newTranslation.setElementId(elementId.getValue());
				newTranslation.setCaption(caption.getValue());
				
				translationSet.add(newTranslation);
				campaignFormTranslations.setLanguageCode(languageCode.getValue());
				campaignFormTranslations.setTranslations(translationSet);
				
//				campaignFormMetaDto.getCampaignFormTranslations().add(campaignFormTranslations);
				grid.setItems(translationSet);
			} else {

			}
		});

		formLayout.add(elementId, caption);

		formLayout.setColspan(languageCode, 2);
		formLayout.setColspan(elementId, 2);
		formLayout.setColspan(caption, 2);

		return vrsub;
	}

}
