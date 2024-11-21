package com.cinoteck.application.views.campaigndata;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.cinoteck.application.views.campaign.CampaignForm.CampaignFormEvent;
import com.cinoteck.application.views.campaign.CampaignForm.PublishUnpublishEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.campaign.CampaignDto;
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
	Button verifyButton;
	Button unVerifyButton;
	Button publishButton;
	Button unPublishButton;
	boolean dataPublishNotificationCheck = false;

	private final UserProvider usr = new UserProvider();
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public CampaignFormDataEditForm(CampaignFormMetaReferenceDto campaignFormMetaReferenceDto,
			CampaignReferenceDto campaignReferenceDto, boolean openData, String uuidForm,
			Grid<CampaignFormDataIndexDto> grid, boolean campaignFormMetaDtox) {

		setSizeFull();

		campaignFormMetaDto = FacadeProvider.getCampaignFormMetaFacade()
				.getCampaignFormMetaByUuid(campaignFormMetaReferenceDto.getUuid());

		if (usr.getUser().getLanguage().toString().equals("Pashto")) {
			campaignFormBuilder = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null,
					campaignReferenceDto, campaignFormMetaDto.getCampaignFormTranslations(),
					campaignFormMetaDto.getFormname_ps_af(), campaignFormMetaReferenceDto, openData, uuidForm,
					campaignFormMetaDtox);
		} else if (usr.getUser().getLanguage().toString().equals("Dari")) {
			campaignFormBuilder = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null,
					campaignReferenceDto, campaignFormMetaDto.getCampaignFormTranslations(),
					campaignFormMetaDto.getFormname_fa_af(), campaignFormMetaReferenceDto, openData, uuidForm,
					campaignFormMetaDtox);
		} else {
			campaignFormBuilder = new CampaignFormBuilder(
					campaignFormMetaDto.getCampaignFormElements(),
					null,
					campaignReferenceDto, 
					campaignFormMetaDto.getCampaignFormTranslations(),
					campaignFormMetaDto.getFormName(), 
					campaignFormMetaReferenceDto, 
					openData, 
					uuidForm,
					campaignFormMetaDtox);
		}

		System.out.print(campaignFormMetaDto.isDistrictentry() + "district daa entry uuuuuuuuuuuuuuuuuu");

		if (campaignFormMetaDto.isDistrictentry()) {

			campaignFormBuilder.cbCommunity.setVisible(false);
//			campaignFormBuilder.checkDistrictEntry = true;

		} else {
			campaignFormBuilder.cbCommunity.setVisible(true);
//			campaignFormBuilder.checkDistrictEntry = true;
		}

		dialog = new Dialog();
		dialog.add(campaignFormBuilder);
		dialog.setSizeFull();
		dialog.setCloseOnOutsideClick(false);

		if (usr.getUser().getLanguage().toString().equals("Pashto")) {
			dialog.setHeaderTitle(
					campaignFormMetaDto.getFormname_ps_af() + " | " + campaignFormBuilder.cbCampaign.getValue());
		} else if (usr.getUser().getLanguage().toString().equals("Dari")) {
			dialog.setHeaderTitle(
					campaignFormMetaDto.getFormname_fa_af() + " | " + campaignFormBuilder.cbCampaign.getValue());
		} else {
			dialog.setHeaderTitle(
					campaignFormMetaDto.getFormName() + " | " + campaignFormBuilder.cbCampaign.getValue());
		}

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

//				System.out.println("deleted " + uuidForm);
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
			if( openData && campaignFormBuilder.updateFormDataUnitAssignment.isVisible() && !campaignFormBuilder.cbCommunity.isReadOnly()) {
//				 Notification notification = new Notification("Warning: You have unsaved changes in the cluster selection. Please click the Update Form "
//				 		+ "Data Unit button to save your changes, or they will be lost.", 3000); // Duration is 3000 ms
//				    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//				    notification.setPosition(Notification.Position.MIDDLE); // Center the notification
//				    notification.open();
				    
				    
					ConfirmDialog unsavedClusterEditWarninig = new ConfirmDialog();
//					archiveDearchiveConfirmation.setCancelable(true);
//					archiveDearchiveConfirmation.addCancelListener(ee -> archiveDearchiveConfirmation.close());
					unsavedClusterEditWarninig.setRejectable(false);
//					unsavedClusterEditWarninig.setRejectText("I understand");
//					unsavedClusterEditWarninig.addRejectListener(ee -> unsavedClusterEditWarninig.close());

					unsavedClusterEditWarninig.setConfirmText("I understand");
//					unsavedClusterEditWarninig.setHeader("Warn");
					unsavedClusterEditWarninig.setText("Warning: You have unsaved changes in the cluster selection. To save these changes, please click 'Update Form Data Unit,' to update selection or select 'Cancel' to discard them, or you will be unable to save.");
					unsavedClusterEditWarninig.open();
					unsavedClusterEditWarninig.addConfirmListener(ee -> unsavedClusterEditWarninig.close());
			}else {
				if (campaignFormBuilder.saveFormValues()) {

					if (openData) {
						UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
						userActivitySummaryDto.setActionModule("Campaign Data");
						userActivitySummaryDto.setAction("Edited Data: " + campaignFormMetaDto.getFormName() + " in "
								+ campaignReferenceDto.getCaption());
						userActivitySummaryDto.setCreatingUser_string(usr.getUser().getUserName());
						FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);
					}

					dialog.close();
					grid.getDataProvider().refreshAll();
				}
			}

			// showConfirmationDialog();
		});

//		System.out.println(openData + "open dataaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

		verifyButton = new Button(I18nProperties.getCaption("Verify Data"));
		publishButton = new Button(I18nProperties.getCaption(" Publish Data"));
		Icon verifyButtonIcon = new Icon(VaadinIcon.CHECK_SQUARE_O);
		verifyButton.setIcon(verifyButtonIcon);

		Icon publishButtonIcon = new Icon(VaadinIcon.PRINT);
		publishButton.setIcon(publishButtonIcon);

		unVerifyButton = new Button(I18nProperties.getCaption("Un-Verify Data"));
		Icon unVerifyButtonIcon = new Icon(VaadinIcon.CHECK_SQUARE_O);
		unVerifyButton.setIcon(unVerifyButtonIcon);

		unPublishButton = new Button(I18nProperties.getCaption("Un-Publish Data"));
		Icon unPublishButtonIcon = new Icon(VaadinIcon.CHECK_SQUARE_O);
		unPublishButton.setIcon(unPublishButtonIcon);

		if (usr.getUser().getUsertype() == UserType.WHO_USER) {
			
			if(usr.getUser().getUserRoles().contains(UserRole.PUBLISH_USER)) {
				if (campaignFormMetaReferenceDto.getFormType().equalsIgnoreCase("post-campaign")) {

//					System.out.println(uuidForm + "uuuuuuuuuuuuuuuuuuiddddddddddddddddddddddddddd from view ");
					if (uuidForm != null) {
						if (!FacadeProvider.getCampaignFormDataFacade().getVerifiedStatus(uuidForm)) {
							dialog.getFooter().add(verifyButton);
							// data is not verified
							dataPublishNotificationCheck = false;

						} else {
							dialog.getFooter().add(unVerifyButton);
							if (!FacadeProvider.getCampaignFormDataFacade().getPublishedStatus(uuidForm)) {
								dialog.getFooter().add(publishButton);
							} else {
								dialog.getFooter().add(unPublishButton);
							}

						}
					} else {

					}

				}
			}
			
		}

		verifyButton.addClickListener(e -> {
			try {
				List<String> uuidList = new ArrayList<>();
				uuidList.add(uuidForm);
				FacadeProvider.getCampaignFormDataFacade().verifyCampaignData(uuidList, false);

			} catch (Exception exception) {
				logger.debug(" ============xxxxxxxxxxxxx============== " + "Could Not Verify CampaignData ");
			} finally {

				Notification.show("Data verified successfully");
				verifyButton.setVisible(false);
				grid.getDataProvider().refreshAll();
			}
		});

		publishButton.addClickListener(e -> {
			try {
				List<String> uuidList = new ArrayList<>();
				uuidList.add(uuidForm);
				FacadeProvider.getCampaignFormDataFacade().publishCampaignData(uuidList, false);

			} catch (Exception exception) {
				logger.debug(" ============xxxxxxxxxxxxx============== " + "Could Not Publish CampaignData ");
			} finally {

				Notification.show("Data published successfully");
				publishButton.setVisible(false);
				grid.getDataProvider().refreshAll();
			}
		});

		unVerifyButton.addClickListener(e -> {
			try {
				List<String> uuidList = new ArrayList<>();
				uuidList.add(uuidForm);
				FacadeProvider.getCampaignFormDataFacade().verifyCampaignData(uuidList, true);
				for (String eachUuid : uuidList) {
					if (FacadeProvider.getCampaignFormDataFacade().getPublishedStatus(eachUuid)) {
						FacadeProvider.getCampaignFormDataFacade().publishCampaignData(uuidList, true);
						unPublishButton.setVisible(false);
					}
				}

			} catch (Exception exception) {
				logger.debug(" ============xxxxxxxxxxxxx============== " + "Could Not Un-Verify CampaignData ");
			} finally {

				Notification.show("Data un-verified successfully");
				unVerifyButton.setVisible(false);
				grid.getDataProvider().refreshAll();
//					dialog.getFooter().add(verifyButton);

			}
		});

		unPublishButton.addClickListener(e -> {
			try {
				List<String> uuidList = new ArrayList<>();
				uuidList.add(uuidForm);
				FacadeProvider.getCampaignFormDataFacade().publishCampaignData(uuidList, true);

			} catch (Exception exception) {
				logger.debug(" ============xxxxxxxxxxxxx============== " + "Could Not Un-Publish CampaignData ");
			} finally {

				Notification.show("Data un-published successfully");
				unPublishButton.setVisible(false);
				grid.getDataProvider().refreshAll();
//					dialog.getFooter().add(verifyButton);

			}
		});

		saveAndContinueButton.addClickListener(e -> {
			if (campaignFormBuilder.saveFormValues()) {
				dialog.close();
				dialog = new Dialog();
				grid.getDataProvider().refreshAll();

				CampaignFormBuilder campaignFormBuilderx;
				campaignFormBuilderx = new CampaignFormBuilder(campaignFormMetaDto.getCampaignFormElements(), null,
						campaignReferenceDto, campaignFormMetaDto.getCampaignFormTranslations(),
						campaignFormMetaDto.getFormName(), campaignFormMetaReferenceDto, openData, uuidForm,
						campaignFormMetaDtox);
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

//	public void parseViewInstance(CampaignDataView campaignDataView) {
//		campaignDataView.reload();
//	}

}