package com.cinoteck.application.views.configurations;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;

import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

public class ClusterEditForm extends HorizontalLayout{
	private boolean create;
	
	Binder<CommunityDto> binder = new BeanValidationBinder<CommunityDto>(CommunityDto.class);
	
	
	public ClusterEditForm(boolean create) {
		
		this.create = create;
		Dialog dialog = new Dialog();
		dialog.add();
		dialog.setSizeFull();
		
		Button deleteButton = new Button("Cancel", (e) -> dialog.close());
		deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
		        ButtonVariant.LUMO_TERTIARY);
		deleteButton.getStyle().set("margin-right", "auto");
		dialog.getFooter().add(deleteButton);

		Button saveButton = new Button("Save Data");//, (e) -> dialog.close());
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
		
	TextField name = new TextField("Name");
	name.setRequired(true);
	
	
	TextField clusterNumber = new TextField("Cluster Number");
	
	
	TextField cCode = new TextField("CCode");
	cCode.setRequired(true);
	
	
	ComboBox<AreaReferenceDto> regionFilter = new ComboBox<>("Region");
	regionFilter.setRequired(true);
	
	ComboBox<RegionReferenceDto> provinceFilter = new ComboBox<>("Province");
	provinceFilter.setRequired(true);
	
	
	
	formLayout.add(name, clusterNumber, cCode, regionFilter, provinceFilter);
	

	
	
		
	}
	

}
