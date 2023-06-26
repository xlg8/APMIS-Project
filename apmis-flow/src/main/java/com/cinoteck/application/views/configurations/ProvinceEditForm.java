package com.cinoteck.application.views.configurations;

import java.util.List;

import com.cinoteck.application.views.campaigndata.CampaignFormDataEditForm;
import com.cinoteck.application.views.campaigndata.CampaignFormDataEditForm.CampaignFormDataEditFormEvent;
import com.cinoteck.application.views.campaigndata.CampaignFormDataEditForm.SaveEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

@SuppressWarnings("serial")
public class ProvinceEditForm extends Dialog {

	
	
	ComboBox<AreaReferenceDto> regionCombo = new ComboBox<AreaReferenceDto>();
	
	Binder<RegionDto> binder = new BeanValidationBinder<>(RegionDto.class);
	
	List<AreaReferenceDto> areas = FacadeProvider.getAreaFacade().getAllActiveAsReference();

	Button archive = new Button();
	Button discard = new Button();
	Button save = new Button();

	public ProvinceEditForm(RegionDto formData) {

		configureFields(formData);
	}
	
	private void configureFields(RegionDto formData) {
		FormLayout form = new FormLayout();
		
		TextField regionfield = new TextField();
		binder.forField(regionfield).bind(RegionDto::getName, RegionDto::setName);
		regionfield.setValue(formData.getName());
		
		TextField rcodeField = new TextField();
		binder.forField(rcodeField).bind(RegionDto::getExternalIddummy, RegionDto::setExternalIddummy);
		regionfield.setValue(formData.getExternalIddummy());
		
		ComboBox<Object> area = new ComboBox<Object>(RegionDto.AREA);
		area.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReference());
		area.setValue(formData.getArea());
		form.add(regionfield, rcodeField, area);
		
		Dialog dialog = new Dialog();
		dialog.add(form);
//		dialog.setSizeFull();
		dialog.open();

	}
	
	@SuppressWarnings("unused")
	private void validateAndSave() {
		if (binder.isValid()) {
			fireEvent(new SaveEvent(this, binder.getBean()));
		}
	}

	public void setRegion(RegionDto region) {
		binder.setBean(region);
	}
	
	public static abstract class RegionEditFormEvent extends ComponentEvent<ProvinceEditForm> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4607965566484995874L;
		private RegionDto regionedit;

		protected RegionEditFormEvent(ProvinceEditForm source, RegionDto regionedit) {
			super(source, false);
			this.regionedit = regionedit;
		}

		public RegionDto getRegionedit() {
			return regionedit;
		}
	}
	
	
	
	public static class SaveEvent extends RegionEditFormEvent {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7410670816513753656L;

		SaveEvent(ProvinceEditForm source, RegionDto regionedit) {
			super(source, regionedit);
		}
	}
	
	
	
//
//	private void saveArea() {
//		if (binder.isValid()) {
//			RegionDto regionDto = binder.getBean();
//			String regionValue = regionField.getValue();
//			long rcodeValue = Long.parseLong(rcodeField.getValue());
//			AreaReferenceDto area = regionCombo.getValue();
//
//			regionDto.setName(regionValue);
//			regionDto.setExternalId(rcodeValue);
//			regionDto.setArea(area);
//
//		}
//	}

//	public void setRegion(RegionDto regionDto) {
//		regionField.setValue(regionDto.getName());
//		rcodeField.setValue(String.valueOf(regionDto.getExternalId()));
//		regionCombo.setValue(regionDto.getArea());
//		binder.setBean(regionDto);
//	}

}
