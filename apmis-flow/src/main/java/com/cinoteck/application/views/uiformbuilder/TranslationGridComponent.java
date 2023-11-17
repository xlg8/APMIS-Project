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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;

import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;

public class TranslationGridComponent extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1204658853656142982L;
	ComboBox<String> languageCode = new ComboBox<String>("Tranlation Language Code");
	TextField elementId = new TextField("Element Id");
	TextField caption = new TextField("Caption");

	CampaignFormMetaDto campaignFormMetaDto;
	CampaignFormTranslations campaignFormTranslations = new CampaignFormTranslations();
	List<CampaignFormTranslations> campaignFormTranslationsList = new ArrayList<>();

	TranslationElement translationBeenEdited;
	TranslationElement newTranslation;
	List<TranslationElement> translationSet = new ArrayList<>();

	FormLayout mainLayout = new FormLayout();
	FormLayout formLayout = new FormLayout();

	HorizontalLayout vr3 = new HorizontalLayout();
	HorizontalLayout vr1 = new HorizontalLayout();

	private Grid<CampaignFormTranslations> outerGrid = new Grid<>(CampaignFormTranslations.class, false);
	private GridListDataView<CampaignFormTranslations> outerDataView;

	private Grid<TranslationElement> grid = new Grid<>(TranslationElement.class, false);
	private GridListDataView<TranslationElement> dataView;
	private ListDataProvider<TranslationElement> dataprovider;

	private boolean isNewForm = false;
	ListDataProvider<CampaignFormTranslations> outerDataprovider;

	public TranslationGridComponent(CampaignFormMetaDto campaignFormMetaDto) {
		this.campaignFormMetaDto = campaignFormMetaDto;

		addClassName("translation-grid-component");
		setSizeFull();
		configureFields();
		add(getContent());
		configureGrid();
	}

	private void configureGrid() {

		outerGrid.setSelectionMode(SelectionMode.SINGLE);
		outerGrid.setMultiSort(true, MultiSortPriority.APPEND);
		outerGrid.setSizeFull();
		outerGrid.setColumnReorderingAllowed(true);

		outerGrid.addColumn(CampaignFormTranslations::getLanguageCode).setHeader("Language Code").setSortable(true)
				.setResizable(true);
		outerGrid.addColumn(CampaignFormTranslations::getTranslations).setHeader("Translation Element")
				.setSortable(true).setResizable(true);

		grid.addColumn(TranslationElement::getElementId).setHeader("Element Id").setSortable(true).setResizable(true);
		grid.addColumn(TranslationElement::getCaption).setHeader("Caption").setSortable(true).setResizable(true);

		List<CampaignFormTranslations> existingFormTranslations = campaignFormMetaDto.getCampaignFormTranslations();
		existingFormTranslations = existingFormTranslations == null ? new ArrayList<>() : existingFormTranslations;
//		ListDataProvider<CampaignFormTranslations> 
		outerDataprovider = DataProvider.fromStream(existingFormTranslations.stream());

		outerDataView = outerGrid.setItems(outerDataprovider);
		outerGrid.setVisible(true);
		outerGrid.setAllRowsVisible(true);

	}

	private void configureFields() {

		languageCode.setItems("ps_AF", "fa_AF");
		languageCode.setHelperText("You only need to select this field once");
	}

	private Component getContent() {

		VerticalLayout editorLayout = editForm();
		editorLayout.getStyle().remove("width");
		HorizontalLayout layout = new HorizontalLayout(outerGrid, grid, editorLayout);
		layout.setFlexGrow(4, outerGrid);
		layout.setFlexGrow(4, grid);
		layout.setFlexGrow(0, editorLayout);
		layout.setSizeFull();
		return layout;
	}

	private VerticalLayout editForm() {

		VerticalLayout vrsub = new VerticalLayout();

		Button back = new Button(new Icon(VaadinIcon.BACKWARDS));
		back.setVisible(false);
		Button plus = new Button(new Icon(VaadinIcon.PLUS));
		plus.setId("main");
		Button del = new Button(new Icon(VaadinIcon.DEL_A));
		del.setId("main");
		del.getStyle().set("background-color", "red !important");

		Button cancel = new Button("Cancel");
		cancel.setId("main");
		Button save = new Button("Save");
		save.setId("main");

		vr1.add(back, plus, del);
		vr3.add(save, cancel);
		vr3.setJustifyContentMode(JustifyContentMode.END);
		vr3.setSpacing(true);

		mainLayout.setVisible(false);
		formLayout.setVisible(false);
		vr3.setVisible(false);
		back.setVisible(false);

		vrsub.add(vr1, formLayout, mainLayout, vr3);

		outerGrid.addSelectionListener(eee -> {

			grid.setSelectionMode(SelectionMode.SINGLE);
			grid.setMultiSort(true, MultiSortPriority.APPEND);
			grid.setSizeFull();
			grid.setColumnReorderingAllowed(true);
			grid.setAllRowsVisible(true);

			plus.setId("sub");
			del.setId("sub");
			cancel.setId("sub");
			save.setId("sub");

			if (mainLayout.isVisible()) {
				mainLayout.setVisible(false);
			}

			if (vr3.isVisible()) {
				vr3.setVisible(false);
			}

			if (!vr1.isVisible()) {
				vr1.setVisible(true);
			}

			outerGrid.setVisible(false);
			grid.setVisible(true);
			back.setVisible(true);

			List<TranslationElement> allTranslations = eee.getFirstSelectedItem().get().getTranslations().stream()
					.collect(Collectors.toList());
			allTranslations = allTranslations == null ? new ArrayList<>() : allTranslations;
			dataprovider = DataProvider.fromStream(allTranslations.stream());
			dataView = grid.setItems(dataprovider);
		});

		grid.addSelectionListener(ee -> {

			int size = ee.getAllSelectedItems().size();
			if (size > 0) {

				translationBeenEdited = ee.getFirstSelectedItem().get();
				boolean isSingleSelection = size == 1;
				vr1.setEnabled(isSingleSelection);
				vr3.setEnabled(isSingleSelection);

				if (mainLayout.isVisible()) {
					mainLayout.setVisible(false);
				}

				if (vr3.isVisible()) {
					vr3.setVisible(false);
				}

				vr1.setVisible(false);
				formLayout.setVisible(true);
				vr3.setVisible(true);

				if (translationBeenEdited.getElementId() != null) {
					elementId.setValue(translationBeenEdited.getElementId());
				}

				if (translationBeenEdited.getCaption() != null) {
					caption.setValue(translationBeenEdited.getCaption());
				}
				save.setText("Update");
			}
		});

		back.addClickListener(e -> {

			plus.setId("main");
			del.setId("main");
			cancel.setId("main");
			save.setId("main");

			if (formLayout.isVisible()) {
				formLayout.setVisible(false);
			}

			if (vr3.isVisible()) {
				vr3.setVisible(false);
			}
			back.setVisible(false);
			grid.setVisible(false);
			outerGrid.setVisible(true);

		});

		plus.addClickListener(e -> {

			save.setText("Save");
			if (((Button) e.getSource()).getId().get().equals("main")) {

				languageCode.clear();
				mainLayout.setVisible(true);
				vr3.setVisible(true);
				vr1.setVisible(false);
				System.out.println("mainnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
			} else {
				if (((Button) e.getSource()).getId().get().equals("sub")) {

					System.out.println("subbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
					formLayout.setVisible(true);
					vr3.setVisible(true);
					vr1.setVisible(false);

					elementId.setValue("");
					caption.setValue("");

					newTranslation = new TranslationElement();

					if (campaignFormMetaDto == null) {
						campaignFormMetaDto = new CampaignFormMetaDto();
						grid.setItems(dataprovider);
					} else {
						grid.setItems(dataprovider);
					}
				}
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

			if (((Button) e.getSource()).getId().get().equals("main")) {

				mainLayout.setVisible(false);
				vr3.setVisible(false);
				vr1.setVisible(true);
			} else {
				if (((Button) e.getSource()).getId().get().equals("sub")) {

					vr1.setVisible(true);
					formLayout.setVisible(false);
					vr3.setVisible(false);

					elementId.setValue("");
					caption.setValue("");
					
					save.setText("Save");
					grid.setItems(dataprovider);
				}
			}
		});

		save.addClickListener(e -> {

			if (((Button) e.getSource()).getId().get().equals("main")) {

				CampaignFormTranslations campaignFormTranslations = new CampaignFormTranslations();
				List<CampaignFormTranslations> campaignFormTranslationsList = new ArrayList<>();
				List<TranslationElement> translationSet = new ArrayList<>();
				if (languageCode.getValue() != null) {

					System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
					campaignFormTranslations.setLanguageCode(languageCode.getValue());
					campaignFormTranslations.setTranslations(translationSet);
					
					if (campaignFormMetaDto == null) {

						campaignFormMetaDto = new CampaignFormMetaDto();
						campaignFormTranslationsList.add(campaignFormTranslations);
						campaignFormMetaDto.setCampaignFormTranslations(campaignFormTranslationsList);
					}

					campaignFormMetaDto.getCampaignFormTranslations().add(campaignFormTranslations);
					outerGrid.setItems(campaignFormMetaDto.getCampaignFormTranslations());
//					getGridData();
					
					vr1.setVisible(true);
					mainLayout.setVisible(false);
					vr3.setVisible(false);
					Notification.show("New Language Translation Saved");
				} else {
					Notification.show("Choose a Language Translation Type you want to add for Form");
				}
			} else {
				if (((Button) e.getSource()).getId().get().equals("sub")) {

					vr1.setVisible(true);
					formLayout.setVisible(false);
					vr3.setVisible(false);

					if (elementId.getValue() != null && caption.getValue() != null) {

					}
				}

			}
		});

		mainLayout.add(languageCode);
		mainLayout.setColspan(languageCode, 2);

		formLayout.add(elementId, caption);
		formLayout.setColspan(elementId, 2);
		formLayout.setColspan(caption, 2);

		return vrsub;
	}

	public List<CampaignFormTranslations> getGridData() {
		return outerGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
	}

}
