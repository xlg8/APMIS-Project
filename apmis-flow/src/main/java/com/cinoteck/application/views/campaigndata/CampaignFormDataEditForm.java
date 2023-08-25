package com.cinoteck.application.views.campaigndata;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.FacadeProvider;

public class CampaignFormDataEditForm extends HorizontalLayout {

	Binder<CampaignFormDataDto> binder = new BeanValidationBinder<>(CampaignFormDataDto.class);

	CampaignFormDataDto formData;
	CampaignFormMetaDto campaignFormMetaDto;
	
	CampaignReferenceDto campaignReferenceDto;
	CampaignFormBuilder campaignFormBuilder;
	
	public CampaignFormDataEditForm(CampaignFormMetaReferenceDto campaignFormMetaReferenceDto, CampaignReferenceDto campaignReferenceDto, boolean openData, String uuidForm, Grid<CampaignFormDataIndexDto> grid) {
		
		
		setSizeFull();
		
		campaignFormMetaDto = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(campaignFormMetaReferenceDto.getUuid());
		
		
		campaignFormBuilder = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null, campaignReferenceDto, campaignFormMetaDto.getCampaignFormTranslations(), campaignFormMetaDto.getFormName(), campaignFormMetaReferenceDto, openData, uuidForm);
		
		Dialog dialog = new Dialog();
		dialog.add(campaignFormBuilder);
		dialog.setSizeFull();
		dialog.setCloseOnOutsideClick(false);

		
		Button deleteButton =new Button(I18nProperties.getCaption(Captions.actionCancel), (e) -> dialog.close());
		deleteButton.setIcon(new Icon(VaadinIcon.REFRESH));
		deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
		        ButtonVariant.LUMO_CONTRAST);
//		new Button("Cancle", (e) -> dialog.close());
//		deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
//		        ButtonVariant.LUMO_TERTIARY);
		deleteButton.getStyle().set("margin-right", "auto");
		dialog.getFooter().add(deleteButton);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));//, (e) -> dialog.close());
		saveButton.setIcon(new Icon(VaadinIcon.CHECK));
//		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
//		        ButtonVariant.LUMO_SUCCESS);
		dialog.getFooter().add(saveButton);
		
		
		saveButton.addClickListener(e -> {
			if(campaignFormBuilder.saveFormValues()) {
				dialog.close();
				grid.getDataProvider().refreshAll();
			}
			//showConfirmationDialog();
		});
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("formI");
		//dialog.getElement().setAttribute("theme", "my-custom-dialog");
		dialog.open();
	}
	
	private void showConfirmationDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        dialog.add(I18nProperties.getDescription(Descriptions.saveCampaignValidation));

        Button confirmButton = new Button(I18nProperties.getCaption(Captions.actionConfirm), event -> {
            // Perform save operation
            dialog.close();
        });

        Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), event -> dialog.close());

        dialog.add(confirmButton, cancelButton);
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