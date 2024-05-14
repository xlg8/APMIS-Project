package com.cinoteck.application.views.configurations;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

public class ClusterEditForm extends HorizontalLayout{
	private boolean create;
	
	Binder<CommunityDto> binder = new BeanValidationBinder<CommunityDto>(CommunityDto.class);
	UserProvider userProvider = new UserProvider();
	
	public ClusterEditForm(boolean create) {
		
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		this.create = create;
		Dialog dialog = new Dialog();
		dialog.add();
		dialog.setSizeFull();
		
		Button deleteButton = new Button(I18nProperties.getCaption(Captions.actionCancel), (e) -> dialog.close());
		deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
		        ButtonVariant.LUMO_TERTIARY);
		deleteButton.getStyle().set("margin-right", "auto");
		dialog.getFooter().add(deleteButton);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));//, (e) -> dialog.close());
		saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
		dialog.getFooter().add(saveButton);
		
		
		saveButton.addClickListener(e -> {
			
			//dialog.close();
			//showConfirmationDialog();
		});
		
		dialog.open();
		
		
	}
	
	
	public void addFields() {
		
		FormLayout formLayout = new FormLayout();
		
	TextField name = new TextField(I18nProperties.getCaption(Captions.name));
	name.setRequired(true);
	
	
	TextField clusterNumber = new TextField(I18nProperties.getCaption(Captions.clusterNumber));
	
	
	TextField cCode = new TextField(I18nProperties.getCaption(Captions.Community_externalID));
	cCode.setRequired(true);
	
	
	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>(I18nProperties.getCaption(Captions.area));
	regionFilter.setRequired(true);
	
	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	provinceFilter.setRequired(true);
	
	
	
	formLayout.add(name, clusterNumber, cCode, regionFilter, provinceFilter);
	

	
	
		
	}
	

}