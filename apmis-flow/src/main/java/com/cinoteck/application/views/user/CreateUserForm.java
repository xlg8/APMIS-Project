package com.cinoteck.application.views.user;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@Route(value="create-user")
public class CreateUserForm extends FormLayout{

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);

	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;

	
	// TODO: Change labels to use IL8N names for internationalisation
	// NOTE: Fields should use the same naming convention as in UserDto.class
	H2 heading = new H2();
	TextField firstName = new TextField("First name");
	TextField lastName = new TextField("Last name");
	TextField userEmail = new TextField("Email Address");
	TextField phone = new TextField("Phone Number");
	TextField userPosition = new TextField("Position");
	TextField userOrganisation = new TextField("Organisation");

	ComboBox<AreaReferenceDto> region = new ComboBox<>("Region");
	ComboBox<RegionReferenceDto> province = new ComboBox<>("Province");
	ComboBox<DistrictReferenceDto> district = new ComboBox<>("District");
	MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>("Community");

	MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>("User Role");
	// CheckboxGroup<UserRole> userRoles = new CheckboxGroup<>();
	CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
	Checkbox activeCheck = new Checkbox();
	private boolean active = true;

	CheckboxGroup<UserType> usertype = new CheckboxGroup("Common User?");
	ComboBox<Language> language = new ComboBox<>("Language");

	Button save = new Button("Save");
	Button delete = new Button("Delete");
	Button close = new Button("Cancel");
	
	public CreateUserForm() {
		
		
		
	}
	
}
