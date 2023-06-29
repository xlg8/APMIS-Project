package com.cinoteck.application.views.campaigndata;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.cinoteck.application.views.utils.CampaignFormBuilder;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataEntry;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.FacadeProvider;

public class CampaignFormDataEditForm extends FormLayout {

	Binder<CampaignFormDataDto> binder = new BeanValidationBinder<>(CampaignFormDataDto.class);

	ComboBox<CampaignFormDataDto> cbCampaign = new ComboBox<>(CampaignFormDataDto.CAMPAIGN);

	ComboBox<CampaignFormDataDto> cbRegion = new ComboBox<>();
	ComboBox<CampaignFormDataDto> cbArea = new ComboBox<>();
	ComboBox<CampaignFormDataDto> cbDistrict = new ComboBox<>();
	ComboBox<CampaignFormDataDto> cbCommunity = new ComboBox<>();

	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	CampaignFormDataDto formData;
	CampaignFormMetaDto campaignFormMetaDto;
	
	CampaignReferenceDto campaignReferenceDto;
	CampaignFormBuilder campaignFormBuilder;
	
	public CampaignFormDataEditForm(CampaignFormMetaReferenceDto campaignFormMetaReferenceDto, CampaignReferenceDto campaignReferenceDto) {
		
		
		HorizontalLayout hor = new HorizontalLayout();
		Icon vaadinIcon = new Icon("lumo", "cross");
		hor.setJustifyContentMode(JustifyContentMode.END);
		hor.setWidthFull();
		hor.add(vaadinIcon);
		hor.setHeight("5px");
		this.setColspan(hor, 2);
		//vaadinIcon.addClickListener(event -> fireEvent(new CloseEvent(this)));
		add(hor);
		
		
		FormLayout formLayout = new FormLayout(); 
		formLayout.getStyle().set("width", "100%!important");
		formLayout.setId("ssssssssst");
		
		ComboBox<Object> cbCampaign = new ComboBox<>(CampaignFormDataDto.CAMPAIGN);
		
		cbCampaign.setItems(FacadeProvider.getCampaignFacade().getAllActiveCampaignsAsReference());
		cbCampaign.setValue(campaignReferenceDto);
		cbCampaign.setEnabled(false);
		
		
		Date date = new Date();

		DatePicker formDate = new DatePicker();
		formDate.setLabel(CampaignFormDataDto.FORM_DATE);
//		formDate.setValue(localDate);
		
		//
		ComboBox<Object> cbArea = new ComboBox<>(CampaignFormDataDto.AREA);
		cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
//		cbArea.setValue(formData.getArea());
//	        cbArea.setItems(FacadeProvider.getAreaFacade().getAllActiveAndSelectedAsReference(campaignUUID));

		ComboBox<Object> cbRegion = new ComboBox<>(CampaignFormDataDto.REGION);
		cbRegion.setItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
//		cbRegion.setValue(formData.getRegion());

		ComboBox<Object> cbDistrict = new ComboBox<>(CampaignFormDataDto.DISTRICT);
		cbDistrict.setItems(FacadeProvider.getDistrictFacade().getAllActiveAsReference());
//		cbDistrict.setValue(formData.getDistrict());

		ComboBox<Object> cbCommunity = new ComboBox<>(CampaignFormDataDto.COMMUNITY);
		cbCommunity.setItems(FacadeProvider.getCommunityFacade().getAllCommunities());
//		cbCommunity.setValue(formData.getCommunity());

		formLayout.add(cbCampaign, formDate, cbArea, cbRegion, cbDistrict, cbCommunity);
		
//		CampaignFormBuilder(List<CampaignFormElement> formElements, List<CampaignFormDataEntry> formValues,
//				VerticalLayout campaignFormLayout, List<CampaignFormTranslations> translations)
		
		
		
		campaignFormMetaDto = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(campaignFormMetaReferenceDto.getUuid());
		
		
		campaignFormBuilder = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null, formLayout, campaignFormMetaDto.getCampaignFormTranslations());
		
		
		Dialog dialog = new Dialog();
		dialog.add(campaignFormBuilder);
		dialog.setSizeFull();
		dialog.open();
	}

//	private void validateAndSave() {
//		if (binder.isValid()) {
//			fireEvent(new SaveEvent(this, binder.getBean()));
//		}
//	}
//
//	public void setCampaignFormData(CampaignFormDataDto user) {
//		binder.setBean(user);
//	}
//
//	public static abstract class CampaignFormDataEditFormEvent extends ComponentEvent<CampaignFormDataEditForm> {
//		private CampaignFormDataDto campaignedit;
//
//		protected CampaignFormDataEditFormEvent(CampaignFormDataEditForm source, CampaignFormDataDto campaignedit) {
//			super(source, false);
//			this.campaignedit = campaignedit;
//		}
//
//		public CampaignFormDataDto getCampaignedit() {
//			return campaignedit;
//		}
//	}
//
//	public static class SaveEvent extends CampaignFormDataEditFormEvent {
//		SaveEvent(CampaignFormDataEditForm source, CampaignFormDataDto campaignedit) {
//			super(source, campaignedit);
//		}
//	}
//
//	public static class DeleteEvent extends CampaignFormDataEditFormEvent {
//		DeleteEvent(CampaignFormDataEditForm source, CampaignFormDataDto campaignedit) {
//			super(source, campaignedit);
//		}
//
//	}
//
//	public static class CloseEvent extends CampaignFormDataEditFormEvent {
//		CloseEvent(CampaignFormDataEditForm source) {
//			super(source, null);
//		}
//	}
//
//	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
//		return addListener(DeleteEvent.class, listener);
//	}
//
//	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
//		return addListener(SaveEvent.class, listener);
//	}
//
//	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
//		return addListener(CloseEvent.class, listener);
//	}

}
