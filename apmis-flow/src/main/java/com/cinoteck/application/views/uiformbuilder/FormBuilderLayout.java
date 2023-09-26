package com.cinoteck.application.views.uiformbuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.user.FormAccess;

public class FormBuilderLayout extends VerticalLayout {

	List<CampaignFormElement> elementToBeSaved;

	H3 formBasics = new H3("Form Basics");
	TextField formName;
	TextField formId;
	ComboBox<CampaignPhase> formType;
	ComboBox<FormAccess> formCategory;
	ComboBox<Modality> modality;
	IntegerField daysExpired;
	ComboBox<Boolean> districtEntry;
	ComboBox<String> languageCode;

	CampaignFormMetaDto campaignFormMetaDto;

	UUID uuid = UUID.randomUUID();

	Binder<CampaignFormMetaDto> binder = new BeanValidationBinder<>(CampaignFormMetaDto.class);

	private Grid<CampaignFormElement> grid = new Grid<>(CampaignFormElement.class, false);
	private GridListDataView<CampaignFormElement> dataView;
	
	FormLayout formLayout = new FormLayout();

	public FormBuilderLayout(CampaignFormMetaDto campaignFormMetaDto) {

		configureGrid();
		configureFields();
	}

	void configureFields() {

		formName = new TextField("Form Name");
		formId = new TextField("Id");
		formType = new ComboBox<CampaignPhase>("Form Type");
		formType.setItems(CampaignPhase.values());
		formCategory = new ComboBox<FormAccess>("Form Category");
		formCategory.setItems(FormAccess.values());
		modality = new ComboBox<Modality>("Modality");
		modality.setItems(Modality.values());
		daysExpired = new IntegerField("Days Expired");
		districtEntry = new ComboBox<Boolean>("District Entry");
		districtEntry.setItems(true, false);
		languageCode = new ComboBox<String>("Language Code");
		languageCode.setItems("en_AF", "fa_AF", "ps_AF");
		
		elementToBeSaved = new LinkedList<>();

		binder.forField(formName).bind(CampaignFormMetaDto::getFormName, CampaignFormMetaDto::setFormName);

		binder.forField(formId).bind(CampaignFormMetaDto::getFormId, CampaignFormMetaDto::setFormId);

		binder.forField(formType).bind(CampaignFormMetaDto::getFormType, CampaignFormMetaDto::setFormType);

		binder.forField(formCategory).bind(CampaignFormMetaDto::getFormCategory, CampaignFormMetaDto::setFormCategory);

		binder.forField(modality).bind(CampaignFormMetaDto::getFormModality, CampaignFormMetaDto::setFormModality);

		binder.forField(daysExpired).bind(CampaignFormMetaDto::getDaysExpired, CampaignFormMetaDto::setDaysExpired);

		binder.forField(districtEntry).bind(CampaignFormMetaDto::isDistrictentry,
				CampaignFormMetaDto::setDistrictentry);

		binder.forField(languageCode).bind(CampaignFormMetaDto::getLanguageCode, CampaignFormMetaDto::setLanguageCode);

		formLayout.add(formBasics, formName, formId, formType, formCategory, modality, daysExpired, languageCode);

		formLayout.setColspan(formBasics, 2);
		HorizontalLayout hr = new HorizontalLayout();

		TabSheet sheet = new TabSheet();
		hr.add(sheet);

		Button cancel = new Button("Cancel");
		Button save = new Button("Save");
		
		VerticalLayout vrsub = new VerticalLayout();

		FormGridComponent formGridComponent = new FormGridComponent();

		FormLayout jsonForm = formGridComponent.configureFields();
		
		HorizontalLayout hr3 = new HorizontalLayout();
		VerticalLayout vr2 = new VerticalLayout();

		jsonForm.setVisible(false);
		vr2.setVisible(true);
		hr3.setVisible(false);

		Button plus = new Button("Add");
		Button del = new Button("Del");
		
		vr2.add(plus, del);
		hr3.add(save, cancel);

		vrsub.add(vr2, jsonForm, hr3);
		HorizontalLayout hr1 = new HorizontalLayout(grid, vrsub);
		sheet.add("Form Elements Data", hr1);
			
//		hr.setFlexGrow(4, grid);
//		hr.setFlexGrow(0, vrsub);
//		hr.setSizeFull();
		
		hr1.setFlexGrow(4, grid);
		hr1.setFlexGrow(0, vrsub);
		hr1.setSizeFull();
		
		add(formLayout);
		add(hr);

		Button discardChanges = new Button("Dicard Changes");
		Button saved = new Button("Save");
		
		HorizontalLayout buttonLayoout = new HorizontalLayout(discardChanges, saved);

		add(buttonLayoout);
		
		plus.addClickListener(e -> {
			
			jsonForm.setVisible(true);
			hr3.setVisible(true);
			vr2.setVisible(false);
		});
		
		cancel.addClickListener(e -> {

			vr2.setVisible(true);
			jsonForm.setVisible(false);
			hr3.setVisible(false);
		});
		
		save.addClickListener(e -> {

			vr2.setVisible(true);
			jsonForm.setVisible(false);
			hr3.setVisible(false);
		});
	}

	void configureGrid() {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(CampaignFormElement::getCaption).setHeader("Form Name").setSortable(true).setResizable(true);
		grid.addColumn(CampaignFormElement::getType).setHeader("Type").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getExpression).setHeader("Expression").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getMin).setHeader("Min").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getMax).setHeader("Max").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getStyles).setHeader("Styles").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getOptions).setHeader("Options").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::getConstraints).setHeader("Constraint").setSortable(true)
//				.setResizable(true);
		grid.addColumn(CampaignFormElement::getDependingOn).setHeader("Depending On").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormElement::getDependingOnValues).setHeader("Depending On Value").setSortable(true)
				.setResizable(true);
		grid.addColumn(CampaignFormElement::isImportant).setHeader("Important").setSortable(true).setResizable(true);
//		grid.addColumn(CampaignFormElement::isWarnonerror).setHeader("Warned Error").setSortable(true)
//				.setResizable(true);
//		grid.addColumn(CampaignFormElement::isIgnoredisable).setHeader("Ignoredisable").setSortable(true)
//				.setResizable(true);
//		grid.addColumn(CampaignFormElement::getDefaultvalue).setHeader("Default Value").setSortable(true)
//				.setResizable(true);
		grid.addColumn(CampaignFormElement::getErrormessage).setHeader("Error Message").setSortable(true)
				.setResizable(true);

//		List<CampaignFormElement> empty = (List<CampaignFormElement>) Stream.empty();
//		
//		ListDataProvider<CampaignFormElement> dataprovider = DataProvider.fromStream(empty.stream());
//
//		dataView = grid.setItems(dataprovider);
		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);
	}

	public void setCampaign(CampaignFormMetaDto formData) {
		binder.setBean(formData);
	}
}
