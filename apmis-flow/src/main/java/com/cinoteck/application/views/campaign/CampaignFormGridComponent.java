package com.cinoteck.application.views.campaign;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDashboardElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaExpiryDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;

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
	List<CampaignFormMetaReferenceDto> allElements;

	public CampaignFormGridComponent(List<CampaignFormMetaReferenceDto> savedCampaignFormMetas,
			List<CampaignFormMetaReferenceDto> allCampaignFormMetas, CampaignDto capaingDto, String campaignPhase) {
		this.savedCampaignFormMetas = savedCampaignFormMetas;
		this.allCampaignFormMetas = allCampaignFormMetas;
		this.capaingDto = capaingDto;
		this.campaignPhase = campaignPhase;

		grid.addColumn(CampaignFormMetaReferenceDto::getCaption)
				.setHeader(I18nProperties.getCaption(Captions.formname));
		grid.addColumn(CampaignFormMetaReferenceDto::getDaysExpired)
				.setHeader(I18nProperties.getCaption(Captions.expiry)+" (default)");
		grid.addColumn(this::getDaysExpiredEditable)
		.setHeader(I18nProperties.getCaption(Captions.expiry) +" custom days (may not update until saved)");
		
		grid.setItems(savedCampaignFormMetas);
		addClassName("list-view");
		setSizeFull();
		add(getContent());
		
	}
	
	private int getDaysExpiredEditable(CampaignFormMetaReferenceDto item) {
		return FacadeProvider.getCampaignFacade().getCampaignFormExp(item.getUuid(), capaingDto.getUuid());
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
		plusButton.setTooltipText(I18nProperties.getCaption(Captions.addNewForm));

		Button deleteButton = new Button(new Icon(VaadinIcon.DEL_A));
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ICON);
		deleteButton.getStyle().set("background-color", "red!important");
		deleteButton.setTooltipText(I18nProperties.getCaption(Captions.removeThisForm));

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionAdd), new Icon(VaadinIcon.CHECK));

		Button cacleButton = new Button(I18nProperties.getCaption(Captions.actionCancel), new Icon(VaadinIcon.REFRESH));

		ComboBox<CampaignFormMetaReferenceDto> forms = new ComboBox<CampaignFormMetaReferenceDto>();
		forms.setLabel(I18nProperties.getCaption(Captions.campaignCampaignForm));
		forms.setItems(allCampaignFormMetas);
		// if its a clicked action set the value from the item....TODO

		IntegerField daysExpire = new IntegerField();
		daysExpire.setLabel(I18nProperties.getCaption(Captions.daysTOExpiry));
		String datd = "";
		
		if (capaingDto != null && capaingDto.getStartDate() != null) {
			datd = capaingDto.getStartDate().toLocaleString();
		} else if(capaingDto != null && capaingDto.getStartDate() == null) {
			
		}
		daysExpire.setHelperText(I18nProperties.getString(Strings.max60DaysFromStartDate) +" ("+ datd +")");
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
			if (size > 0) {
				CampaignFormMetaReferenceDto selectedCamp = ee.getFirstSelectedItem().get();
				formBeenEdited = selectedCamp;
				boolean isSingleSelection = size == 1;
				buttonLay.setEnabled(isSingleSelection);
				buttonAfterLay.setEnabled(isSingleSelection);

				formx.setVisible(true);
				buttonAfterLay.setVisible(true);

				// delete.setEnabled(size != 0);
				forms.setValue(selectedCamp);
				saveButton.setText(I18nProperties.getCaption(Captions.update));
				int dayz = getDaysExpiredEditable(selectedCamp);
				daysExpire.setValue(dayz == 0 ? selectedCamp.getDaysExpired() : dayz);
			} else {
				formBeenEdited = new CampaignFormMetaReferenceDto();
			}
		});

		deleteButton.addClickListener(dex -> {
			if (formBeenEdited == null) {
				Notification.show(I18nProperties.getString(Strings.pleaseSelectFormFirst));
			} else {

				capaingDto.getCampaignFormMetas().remove(formBeenEdited);
				// FacadeProvider.getCampaignFacade().saveCampaign(capdto);
				Notification.show(formBeenEdited + I18nProperties.getString(Strings.wasRemovedFromCampaign));
				grid.setItems(capaingDto.getCampaignFormMetas());
			}
			grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
		});

		plusButton.addClickListener(ce -> {
			CampaignFormMetaReferenceDto newcampform = new CampaignFormMetaReferenceDto();

			formx.setVisible(true);
			buttonAfterLay.setVisible(true);

			try {
				forms.setValue(newcampform);
			} finally {
				saveButton.setText(I18nProperties.getCaption(Captions.actionAdd));
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
			saveButton.setText(I18nProperties.getCaption(Captions.actionSave));
			daysExpire.setValue(0);
			grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
			grid.setHeight("");
		});

		saveButton.addClickListener(e -> {

			if (((Button) e.getSource()).getText().equals("Add")) {
				CampaignFormMetaReferenceDto newCampForm = forms.getValue();
				CampaignFormMetaExpiryDto camFormExp = new CampaignFormMetaExpiryDto(capaingDto, forms.getValue(), daysExpire.getValue());
				
//				Set<CampaignFormMetaReferenceDto> formSet = new HashSet<>();
				
				
				newCampForm.setCaption(forms.getValue().toString());
				newCampForm.setDaysExpired(daysExpire.getValue());
			
//				if (capaingDto == null) {
//					capaingDto = new CampaignDto();
//					System.out.println(capaingDto.getCampaignFormMetas() + "dtooooooooooooooooooooooooooooo22222");
//					formSet.add(newCampForm);
//					capaingDto.setCampaignFormMetas(formSet);
//					System.out.println(capaingDto.getCampaignFormMetas() + "dtooooooooooooooooooooooooooooo211111");
//
//				}
				
				capaingDto.getCampaignFormMetas().add(newCampForm);
				capaingDto.getCampaignFormMetaExpiryDto().add(camFormExp);
				// FacadeProvider.getCampaignFacade().saveCampaign(capdto);
				allCampaignFormMetas.removeAll(capaingDto.getCampaignFormMetas());

				forms.setItems(allCampaignFormMetas);

				Notification.show(I18nProperties.getString(Strings.newFormAddedSucces));
				grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
//				getSavedElements();

			} else {
				// formBeenEdited
				if (formBeenEdited != null) {
					CampaignFormMetaReferenceDto newCampForm = forms.getValue();
//					CampaignDto capdto = capaingDto;
					capaingDto.getCampaignFormMetas().remove(formBeenEdited);
					capaingDto.getCampaignFormMetas().add(newCampForm);
					List<CampaignFormMetaExpiryDto> camFormExp_ = new ArrayList<>();
					
					camFormExp_ = capaingDto.getCampaignFormMetaExpiryDto().stream().filter(ew -> ew.getFormId() == formBeenEdited).collect(Collectors.toList());
					CampaignFormMetaExpiryDto camFormExp_i = new CampaignFormMetaExpiryDto(capaingDto, forms.getValue(), daysExpire.getValue());
					
					if(camFormExp_.size() > 0)
					capaingDto.getCampaignFormMetaExpiryDto().remove(camFormExp_.get(0));
					
					capaingDto.getCampaignFormMetaExpiryDto().add(camFormExp_i);
					
					// FacadeProvider.getCampaignFacade().saveCampaign(capdto);
					grid.setItems(capaingDto.getCampaignFormMetas(campaignPhase));
					getSavedElements();

					Notification.show(I18nProperties.getString(Strings.campaignUpdated));
				} else {
					Notification.show(I18nProperties.getString(Strings.pleaseSelectaFormBeforeUpdate));
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
	
	 public List<CampaignFormMetaReferenceDto> getSavedElements() {
	         return grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
	    }
	 
}