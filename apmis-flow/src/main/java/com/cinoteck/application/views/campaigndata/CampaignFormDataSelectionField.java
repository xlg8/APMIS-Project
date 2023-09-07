/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.cinoteck.application.views.campaigndata;


import com.cinoteck.application.views.utils.VaadinUiUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

public class CampaignFormDataSelectionField extends VerticalLayout {

	private static final long serialVersionUID = 1771273989939992148L;

	private final CampaignFormDataDto newData;
	private final CampaignFormDataDto existingData;
	private final Runnable cancelCallback;
	private final Runnable skipCallback;
	private final Runnable overwriteCallback;
	private final String infoText;

	public CampaignFormDataSelectionField(
		CampaignFormDataDto newData,
		CampaignFormDataDto existingData,
		String infoText,
		Runnable cancelCallback,
		Runnable skipCallback,
		Runnable overwriteCallback) {
		this.newData = newData;
		this.existingData = existingData;
		this.infoText = infoText;
		this.cancelCallback = cancelCallback;
		this.skipCallback = skipCallback;
		this.overwriteCallback = overwriteCallback;

		initialize();
	}

	private void initialize() {

		setSpacing(true);
		setMargin(true);
		setSizeUndefined();
		setWidthFull();

		addInfoComponent();
		addFormDataDetailsComponent();
		addButtons();
	}

	private void addInfoComponent() {
		add(new Label(infoText));
	}

	private void addFormDataDetailsComponent() {

		VerticalLayout formDataDetailsLayout = new VerticalLayout();
		formDataDetailsLayout.setMargin(false);

		H3 newFormDataHeading = new H3(I18nProperties.getString(Strings.headingCampaignFormDataDuplicateNew));
//		CssStyles.style(newFormDataHeading, CssStyles.H3);
		formDataDetailsLayout.add(newFormDataHeading);
		formDataDetailsLayout.add(buildFormDataDetailsComponent(newData, false));

		H3 existingFormDataHeading = new H3(I18nProperties.getString(Strings.headingCampaignFormDataDuplicateExisting));
		formDataDetailsLayout.add(existingFormDataHeading);
		formDataDetailsLayout.add(buildFormDataDetailsComponent(existingData, true));

		add(formDataDetailsLayout);
	}

	private void addButtons() {

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSizeUndefined();

		Button btnCancel = new Button(I18nProperties.getCaption(Captions.importCancelImport));
		btnCancel.addClickListener(e -> cancelCallback.run());
		buttonLayout.add(btnCancel);

		Button btnSkip = new Button(I18nProperties.getCaption(Captions.actionSkip));
		btnSkip.addClickListener(e -> skipCallback.run());
		buttonLayout.add(btnSkip);

		Button btnOverwrite = new Button(I18nProperties.getCaption(Captions.actionOverwrite));
		btnOverwrite.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

		btnOverwrite.addClickListener(e -> overwriteCallback.run());
		buttonLayout.add(btnOverwrite);

		add(buttonLayout);
		
		buttonLayout.setClassName("btn pull-right");
		
	}

	private HorizontalLayout buildFormDataDetailsComponent(CampaignFormDataDto formData, boolean existingData) {
		HorizontalLayout formDataLayout = new HorizontalLayout();
		formDataLayout.setSpacing(true);
		{
			Label fdRegion = new Label(formData.getRegion() != null ? formData.getRegion().toString() : "");
			fdRegion.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.REGION));
			fdRegion.setSizeUndefined();
			formDataLayout.add(fdRegion);

			Label fdDistrict = new Label(formData.getDistrict() != null ? formData.getDistrict().toString() : "");
			fdDistrict.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.DISTRICT));
			fdDistrict.setSizeUndefined();
			formDataLayout.add(fdDistrict);

			Label fdCommunity = new Label(formData.getCommunity() != null ? formData.getCommunity().toString() : "");
			fdCommunity.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.COMMUNITY));
			fdCommunity.setSizeUndefined();
			formDataLayout.add(fdCommunity);

			Label fdFormDate = new Label(formData.getFormDate().toString());
			fdFormDate.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.FORM_DATE));
			fdFormDate.setSizeUndefined();
			formDataLayout.add(fdFormDate);
			
			Label fdFormType = new Label(formData.getFormType() != null ? formData.getFormType().toString() : "");
			fdFormType.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.FORM_TYPE));
			fdFormType.setSizeUndefined();
			formDataLayout.add(fdFormType);

			if (existingData) {
				Label fdCreatingUser = new Label(formData.getCreatingUser() != null ? formData.getCreatingUser().toString() : "");
				fdCreatingUser.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.CREATING_USER));
				fdCreatingUser.setSizeUndefined();
				formDataLayout.add(fdCreatingUser);

				Label fdCreationDate = new Label(formData.getCreationDate().toString());//.formatDate(formData.getCreationDate()));
				fdCreationDate.setText(I18nProperties.getPrefixCaption(CampaignFormDataDto.I18N_PREFIX, CampaignFormDataDto.CREATION_DATE));
				fdCreationDate.setSizeUndefined();
				formDataLayout.add(fdCreationDate);
			}
		}

		return formDataLayout;
	}

}
