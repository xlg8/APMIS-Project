package com.cinoteck.application.views.campaigndata;

import java.beans.Transient;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.FacadeProvider;

public class CampaignFormDataEditForm extends HorizontalLayout {

	Binder<CampaignFormDataDto> binder = new BeanValidationBinder<>(CampaignFormDataDto.class);

	CampaignFormDataDto formData;
	CampaignFormMetaDto campaignFormMetaDto;

	CampaignReferenceDto campaignReferenceDto;
	CampaignFormBuilder campaignFormBuilder;
	Dialog dialog;
	


	private final UserProvider usr  = new UserProvider();
	

	public CampaignFormDataEditForm(CampaignFormMetaReferenceDto campaignFormMetaReferenceDto,
			CampaignReferenceDto campaignReferenceDto, boolean openData, String uuidForm,
			Grid<CampaignFormDataIndexDto> grid, boolean campaignFormMetaDtox) {

		setSizeFull();

		campaignFormMetaDto = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(campaignFormMetaReferenceDto.getUuid());

		campaignFormBuilder = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null,
				campaignReferenceDto, campaignFormMetaDto.getCampaignFormTranslations(),
				campaignFormMetaDto.getFormName(), campaignFormMetaReferenceDto, openData, uuidForm, campaignFormMetaDtox);

		System.out.print(campaignFormMetaDto.isDistrictentry() + "district daa entry uuuuuuuuuuuuuuuuuu");
		
	
		if (campaignFormMetaDto.isDistrictentry()) {

			campaignFormBuilder.cbCommunity.setVisible(false);
//			campaignFormBuilder.checkDistrictEntry = true;

		}else {
			campaignFormBuilder.cbCommunity.setVisible(true);
//			campaignFormBuilder.checkDistrictEntry = true;
		}
		
		
		dialog = new Dialog();
		dialog.add(campaignFormBuilder);
		dialog.setSizeFull();
		dialog.setCloseOnOutsideClick(false);
		dialog.setHeaderTitle(campaignFormMetaDto.getFormName() + " | " + campaignFormBuilder.cbCampaign.getValue());
		
		System.out.println(openData + "open dataaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		
		Button verifyAndPublishButton = new Button(I18nProperties.getCaption("Verify & Publish"));
		Icon verifyAndPublishButtonIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
		verifyAndPublishButtonIcon.getStyle().set("color", "orange !important");
		verifyAndPublishButton.setIcon(verifyAndPublishButtonIcon);
		verifyAndPublishButton.getStyle().set("border", "1px solid orange");
		verifyAndPublishButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		System.out.println(campaignFormMetaDto.getFormType() + "Fror Tpoe " + campaignFormMetaReferenceDto.getFormType() );

		if(usr.getUser().getUsertype() == UserType.WHO_USER) {
			if(campaignFormMetaReferenceDto.getFormType().equalsIgnoreCase("post-campaign")) {
//				dialog.getFooter().add(verifyAndPublishButton);
			}	
		}
		verifyAndPublishButton.addClickListener(e -> {
			try {
				
			}catch(Exception exception){
				
			}
		});
		

		Button deleteButton = new Button(I18nProperties.getCaption(Captions.actionDelete));
		Icon deleteIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
		deleteIcon.getStyle().set("color", "red !important");
		deleteButton.setIcon(deleteIcon);
		deleteButton.getStyle().set("margin-right", "auto");
		deleteButton.getStyle().set("border", "1px solid red");
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		deleteButton.addClickListener(e -> {

			try {
				ConfirmDialog archiveDearchiveConfirmation = new ConfirmDialog();
//				archiveDearchiveConfirmation.setCancelable(true);
//				archiveDearchiveConfirmation.addCancelListener(ee -> archiveDearchiveConfirmation.close());
				archiveDearchiveConfirmation.setRejectable(true);
				archiveDearchiveConfirmation.setRejectText("Cancel");

				archiveDearchiveConfirmation.addRejectListener(ee -> archiveDearchiveConfirmation.close());
				archiveDearchiveConfirmation.setConfirmText("Yes, Delete Data");
				archiveDearchiveConfirmation.setHeader("Campaign Data Delete");
				archiveDearchiveConfirmation.setText("Are you sure you want to delete this Campaign Data?");
				archiveDearchiveConfirmation.open();
				archiveDearchiveConfirmation.addConfirmListener(ee -> {
					FacadeProvider.getCampaignFormDataFacade().deleteCampaignFormData(uuidForm);
					dialog.close();
					grid.getDataProvider().refreshAll();
				});

				System.out.println("deleted " + uuidForm);
			} catch (Exception ex) {

				Notification.show("Unable to delete Form at the moment, Try Again", 10, Position.MIDDLE);
			}
		});
		if (openData) {
			if (usr.hasUserRight(UserRight.CAMPAIGN_DELETE)) {
				dialog.getFooter().add(deleteButton);

			}
		}

		Button saveAndContinueButton = new Button(I18nProperties.getCaption(Captions.actionSaveAndAddNew));// , (e) ->
		// dialog.close());
		saveAndContinueButton.setIcon(new Icon(VaadinIcon.CHECK));
		saveAndContinueButton.getStyle().set("margin-right", "auto");
		if (!openData) {
			dialog.getFooter().add(saveAndContinueButton);
		}

		Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), (e) -> dialog.close());
		cancelButton.setIcon(new Icon(VaadinIcon.REFRESH));
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
		dialog.getFooter().add(cancelButton);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));// , (e) -> dialog.close());
		saveButton.setIcon(new Icon(VaadinIcon.CHECK));
		dialog.getFooter().add(saveButton);
		
		
		


		saveButton.addClickListener(e -> {
			if (campaignFormBuilder.saveFormValues()) {
				
				if (openData) {
					UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
					userActivitySummaryDto.setActionModule("Campaign Data");
					userActivitySummaryDto.setAction("Edited Data: " + campaignFormMetaDto.getFormName() + " in " + campaignReferenceDto.getCaption() );
					userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
					FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);
				}
				
				dialog.close();
				grid.getDataProvider().refreshAll();
			}
			// showConfirmationDialog();
		});

		saveAndContinueButton.addClickListener(e -> {
			if (campaignFormBuilder.saveFormValues()) {
				dialog.close();
				dialog = new Dialog();
				grid.getDataProvider().refreshAll();

				CampaignFormBuilder campaignFormBuilderx;
				campaignFormBuilderx = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null,
						campaignReferenceDto, campaignFormMetaDto.getCampaignFormTranslations(),
						campaignFormMetaDto.getFormName(), campaignFormMetaReferenceDto, openData, uuidForm, campaignFormMetaDtox);
				dialog.add(campaignFormBuilderx);
				dialog.setSizeFull();
				dialog.setCloseOnOutsideClick(false);
				dialog.getFooter().add(saveAndContinueButton);
				dialog.getFooter().add(cancelButton);
				dialog.getFooter().add(saveButton);

				dialog.open();
			}
			// showConfirmationDialog();
		});
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("formI");
		// dialog.getElement().setAttribute("theme", "my-custom-dialog");
		dialog.open();
	}
	


}