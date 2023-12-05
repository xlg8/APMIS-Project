package com.cinoteck.application.views.uiformbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Modality;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.user.FormAccess;

public class FormBuilderLayout extends VerticalLayout {

	CampaignFormMetaDto campaignFormMetaDto;
	List<CampaignFormElement> campaignFormElements;
	List<CampaignFormElement> savedCampaignFormElements;

	H3 formBasics = new H3("Form Basics");
	TextField formName;
	TextField formId;
	ComboBox<CampaignPhase> formType;
	ComboBox<FormAccess> formCategory;
	ComboBox<Modality> modality;
	IntegerField daysExpired;
	ComboBox<Boolean> districtEntry;
	ComboBox<String> languageCode;

	FormGridComponent formGridComponent;
	TranslationGridComponent translationGridComponent;

	Binder<CampaignFormMetaDto> binder = new BeanValidationBinder<>(CampaignFormMetaDto.class);

	FormLayout formLayout = new FormLayout();

	private boolean isNew = false;

	public FormBuilderLayout(CampaignFormMetaDto campaignFormMetaDto_, boolean isNew) {

		this.isNew = isNew;
		if (isNew) {
			CampaignFormMetaDto campMeta = new CampaignFormMetaDto();

			this.campaignFormMetaDto = campMeta.build();
		} else {
			this.campaignFormMetaDto = campaignFormMetaDto_;
		}
		formGridComponent = new FormGridComponent(campaignFormMetaDto);
		translationGridComponent = new TranslationGridComponent(campaignFormMetaDto);
		configureFields();
	}

	private List<CampaignFormElement> getFormListDashboard() {

		List<CampaignFormMetaDto> empty = FacadeProvider.getCampaignFormMetaFacade().getAllFormElement().stream()
				.collect(Collectors.toList());

		List<CampaignFormElement> list = new ArrayList<>();
		for (CampaignFormMetaDto campaignFormElement : empty) {

			CampaignFormElement makeList = new CampaignFormElement();
			makeList.setId(campaignFormElement.getFormId());
			list.add(makeList);
		}
		return list;
	}

	private void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
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
		languageCode.setItems("en", "fa_AF", "ps_AF");

		binder.forField(formName).asRequired("Form Name is Required").bind(CampaignFormMetaDto::getFormName,
				CampaignFormMetaDto::setFormName);

		binder.forField(formId).asRequired("Form Id is Required").bind(CampaignFormMetaDto::getFormId,
				CampaignFormMetaDto::setFormId);

		binder.forField(formType).asRequired("Form Type is Required").bind(CampaignFormMetaDto::getFormType,
				CampaignFormMetaDto::setFormType);

		binder.forField(formCategory).asRequired("Form Category is Required").bind(CampaignFormMetaDto::getFormCategory,
				CampaignFormMetaDto::setFormCategory);

		binder.forField(modality).asRequired("Modality is Required").bind(CampaignFormMetaDto::getModality,
				CampaignFormMetaDto::setModality);

		binder.forField(daysExpired).asRequired("Days Expired is Required").bind(CampaignFormMetaDto::getDaysExpired,
				CampaignFormMetaDto::setDaysExpired);

		binder.forField(districtEntry).asRequired("Dsitrict Entry is Required")
				.bind(CampaignFormMetaDto::isDistrictentry, CampaignFormMetaDto::setDistrictentry);

		binder.forField(languageCode).asRequired("Language Code is Required").bind(CampaignFormMetaDto::getLanguageCode,
				CampaignFormMetaDto::setLanguageCode);

		formLayout.add(formBasics, formName, formId, formType, formCategory, modality, daysExpired, languageCode,
				districtEntry);

		formLayout.setColspan(formBasics, 2);

		final HorizontalLayout hr = new HorizontalLayout();
		hr.setWidthFull();

		TabSheet sheet = new TabSheet();		
		
		VerticalLayout tab1 = new VerticalLayout(formGridComponent);
		VerticalLayout tab2 = new VerticalLayout(translationGridComponent);
		
		sheet.add("Form Elements", tab1);
		sheet.add("Form Translations", tab2);
		
		sheet.setSizeFull();
		
		hr.add(sheet);
		
		add(formLayout);
		add(hr);

		Button discardChanges = new Button("Dicard Changes");
		Button saved = new Button("Save");

		HorizontalLayout buttonLayout = new HorizontalLayout(discardChanges, saved);

		add(buttonLayout);

		discardChanges.addClickListener(e -> discardChanges());
		saved.addClickListener(e -> {
			validateAndSave();
		});
	}

	public void setForm(CampaignFormMetaDto formData) {
		binder.setBean(formData);
	}

	private void validateAndSave() {

		if (binder.validate().isOk()) {

			campaignFormMetaDto = binder.getBean();
			campaignFormMetaDto.setCampaignFormElements(formGridComponent.getGridData());

			fireEvent(new SaveEvent(this, campaignFormMetaDto));

			UI.getCurrent().getPage().reload();

			Notification.show("Form Saved");
		} else {

			Notification.show("Unable to Save Form");
		}
	}

	public static abstract class FormBuilderEvent extends ComponentEvent<FormBuilderLayout> {
		private CampaignFormMetaDto form;

		protected FormBuilderEvent(FormBuilderLayout source, CampaignFormMetaDto form) {
			super(source, false);
			this.form = form;
		}

		public CampaignFormMetaDto getForm() {
			if (form == null) {
				form = new CampaignFormMetaDto();
				return form;
			} else {
				return form;
			}
		}
	}

	public static class SaveEvent extends FormBuilderEvent {
		SaveEvent(FormBuilderLayout source, CampaignFormMetaDto form) {
			super(source, form);
		}
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

}
