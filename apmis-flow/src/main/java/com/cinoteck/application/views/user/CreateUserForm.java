package com.cinoteck.application.views.user;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.user.UserForm.CloseEvent;
import com.cinoteck.application.views.user.UserForm.DeleteEvent;
import com.cinoteck.application.views.user.UserForm.SaveEvent;
import com.cinoteck.application.views.user.UserForm.UserFormEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaType;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserHelper;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@PageTitle("Create New User")
@Route(value = "create-user", layout = MainLayout.class)
public class CreateUserForm extends FormLayout {

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);

	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;

	H3 createUserSubHeading = new H3("Personal Information");
	TextField firstName = new TextField("First Name");
	TextField lastName = new TextField("Last Name");
	TextField userEmail = new TextField("Email Address");
	TextField phone = new TextField("Phone Number");
	TextField userPosition = new TextField("Position");
	TextField userOrganisation = new TextField("Organisation");
	ComboBox<Language> language = new ComboBox<>();
	H3 createUserSubHeading2 = new H3("Address");
	ComboBox<AreaReferenceDto> region = new ComboBox<>();
	ComboBox<RegionReferenceDto> province = new ComboBox<>();
	ComboBox<DistrictReferenceDto> district = new ComboBox<>();
	MultiSelectComboBox<CommunityReferenceDto> cluster = new MultiSelectComboBox<>();
	TextField street = new TextField("Street");
	TextField houseNumber = new TextField("House Number");
	TextField additionalInformation = new TextField("Additional Information");
	TextField postalCode = new TextField("Postal Code");
	ComboBox<AreaType> areaType = new ComboBox<>();
	H3 createUserSubHeading3 = new H3("User Data");
	TextField city = new TextField("City");
	TextField userName = new TextField("Username");
	Checkbox active = new Checkbox();
	CheckboxGroup<UserType> userType = new CheckboxGroup<>();
	CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
	MultiSelectComboBox<UserRole> userRole = new MultiSelectComboBox<>();

	Button closeButton = new Button("Discard");

	Button saveButton = new Button("Save");

	public CreateUserForm() {

		addClassName("contact-form");
		HorizontalLayout pageLayout = new HorizontalLayout();
		pageLayout.setMargin(true);
		pageLayout.setSpacing(false);
		pageLayout.setSizeFull();
		pageLayout.add(createDialogLayout());
	}

	private Component createDialogLayout() {

		this.setColspan(createUserSubHeading, 2);
		this.add(createUserSubHeading);

		firstName.isRequired();
		firstName.addValueChangeListener(e -> suggestUserName());
		this.add(firstName);

		lastName.isRequired();
		lastName.addValueChangeListener(e -> suggestUserName());
		this.add(lastName);

		userEmail.setHelperText("Used to send Email Notification");
		this.add(userEmail);

		phone.setHelperText("Used to send SMS notification needs to contain Country code");
		this.add(phone);

		this.add(userPosition);

		this.add(userOrganisation);

		language.setLabel("Language");
		language.setItems(Language.getAssignableLanguages());
		this.add(language);

		this.setColspan(createUserSubHeading2, 2);
		this.add(createUserSubHeading2);

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		region.setLabel("Region");
		region.setItems(regions);
		this.add(region);

		province.setLabel("Province");
//		province.setItems(provinces);
		this.add(province);

		district.setLabel("District");
//		district.setItems(districts);
		this.add(district);

		cluster.setLabel("Cluster");
//		cluster.setItems();
		this.add(cluster);

		street.setPlaceholder("Enter street here");
		this.add(street);

		houseNumber.setPlaceholder("Enter House Number here");
		this.add(houseNumber);

		additionalInformation.setPlaceholder("Enter Additional Information here");
		this.add(additionalInformation);

		postalCode.setPlaceholder("Enter postal Code here");
		this.add(postalCode);

		city.setPlaceholder("Enter City here");
		this.add(city);

		areaType.setLabel("Area Type");
		areaType.setItems(AreaType.values());
		this.add(areaType);

		this.setColspan(createUserSubHeading3, 2);
		this.add(createUserSubHeading3);

//		userName.setReadOnly(true);
		userName.isRequired();
		userName.setRequiredIndicatorVisible(true);
		this.add(userName);

		active.setLabel("Active?");
		this.setColspan(active, 2);
		active.setValue(true);
		this.add(active);

		userType.setLabel("Type of Users");
		userType.setItems(UserType.COMMON_USER);
		this.setColspan(userType, 2);
		this.add(userType);

		formAccess.setLabel("Forms Access");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		formAccess.isRequired();
		formAccess.setRequiredIndicatorVisible(true);
		this.add(formAccess);

		userRole.setLabel("User Roles");
		userRole.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		userRole.isRequired();
		userRole.setRequiredIndicatorVisible(true);
		this.add(userRole);

		this.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		binder.forField(firstName).asRequired("First Name is Required").bind(UserDto::getFirstName,
				UserDto::setFirstName);

		binder.forField(lastName).asRequired("Last Name is Required").bind(UserDto::getLastName, UserDto::setLastName);

		binder.forField(userEmail).asRequired("Last Name is Required").bind(UserDto::getUserEmail,
				UserDto::setUserEmail);

		binder.forField(phone).withValidator(e -> e.length() >= 13, "Enter a valid Phone Number")
				.bind(UserDto::getPhone, UserDto::setPhone);

		binder.forField(userPosition).bind(UserDto::getUserPosition, UserDto::setUserPosition);

		binder.forField(userOrganisation).bind(UserDto::getUserOrganisation, UserDto::setUserOrganisation);

		binder.forField(region).bind(UserDto::getArea, UserDto::setArea);
		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		region.setItems(regions);
		region.setItemLabelGenerator(AreaReferenceDto::getCaption);
		region.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				province.setItems(provinces);
			}
		});

		binder.forField(province).bind(UserDto::getRegion, UserDto::setRegion);
		province.setItemLabelGenerator(RegionReferenceDto::getCaption);
		province.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				district.setItems(districts);
			}
		});

		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);
		district.setItemLabelGenerator(DistrictReferenceDto::getCaption);
		district.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
				cluster.setItemLabelGenerator(CommunityReferenceDto::getCaption);
				cluster.setItems(communities);
			}
		});

		binder.forField(cluster).bind(UserDto::getCommunity, UserDto::setCommunity);

		userRole.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		binder.forField(userRole).asRequired("User Role is Required").bind(UserDto::getUserRoles,
				UserDto::setUserRoles);
		userRole.addValueChangeListener(e -> {
			updateFieldsByUserRole(e.getValue());
		});

		formAccess.setLabel("Form Access");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		binder.forField(formAccess).bind(UserDto::getFormAccess, UserDto::setFormAccess);

		binder.forField(active).bind(UserDto::isActive, UserDto::setActive);

		userType.setItems(UserType.values());

		language.setItemLabelGenerator(Language::toString);
		language.setItems(Language.getAssignableLanguages());
		binder.forField(language).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		this.add(createButtonsLayout());

		return this;
	}

	private Component createButtonsLayout() {
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		closeButton.addClickShortcut(Key.ESCAPE);

		saveButton.addClickListener(event -> validateAndSave());
		closeButton.addClickListener(event -> fireEvent(new CloseEvent(this)));

		binder.addStatusChangeListener(e -> saveButton.setEnabled(binder.isValid()));
		HorizontalLayout horizontallayout = new HorizontalLayout(saveButton, closeButton);
		horizontallayout.setMargin(true);
		add(horizontallayout);
		this.setColspan(horizontallayout, 2);
		return horizontallayout;
	}

	private void suggestUserName() {
		;
		if (!firstName.isEmpty() && !lastName.isEmpty() && userName.isEmpty() && !userName.isReadOnly()) {
			userName.setValue(UserHelper.getSuggestedUsername(firstName.getValue(), lastName.getValue()));
		}
	}

	private void validateAndSave() {
//		String uuid = binder.getBean().getUuid();
//		String facadeUserName = binder.getBean().getUserName();
//		if(!StringUtils.isEmpty(uuid) && !StringUtils.isEmpty(facadeUserName)) {
			
//			boolean uniqueUsernameCheck= FacadeProvider.getUserFacade().isLoginUnique(uuid, facadeUserName);
		if (binder.validate().isOk()) { //  && uniqueUsernameCheck
			fireEvent(new SaveEvent(this, binder.getBean()));
		} else {
			
//			Notification.show("Your Username must be unique", 10000, Position.MIDDLE);
		}
//		} else {
//			Notification.show("You must fill all required fields", 10000, Position.MIDDLE);
//		}
	}

	public void setUser(UserDto user) {
		binder.setBean(user);
	}

	public static abstract class CreateUserFormEvent extends ComponentEvent<CreateUserForm> {
		private UserDto user;

		protected CreateUserFormEvent(CreateUserForm source, UserDto user) {
			super(source, false);
			this.user = user;
		}

		public UserDto getUser() {
			return user;
		}

	}

	public static class SaveEvent extends CreateUserFormEvent {
		SaveEvent(CreateUserForm createUserForm, UserDto user) {
			super(createUserForm, user);
		}
	}

	public static class CloseEvent extends CreateUserFormEvent {
		CloseEvent(CreateUserForm createUserForm) {
			super(createUserForm, null);
		}
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);
	}
	
	private void updateFieldsByUserRole(Set<UserRole> userRoles) {

		final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(userRoles);
		final boolean useCommunity = jurisdictionLevel == JurisdictionLevel.COMMUNITY;
		final boolean useDistrict = jurisdictionLevel == JurisdictionLevel.DISTRICT || useCommunity;
		final boolean useRegion = jurisdictionLevel == JurisdictionLevel.REGION || useDistrict;
		final boolean useArea = jurisdictionLevel == JurisdictionLevel.AREA || useRegion;

		if (useCommunity) {
			cluster.setVisible(true);
			district.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useDistrict) {
			cluster.setVisible(false);
			district.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useRegion) {
			cluster.setVisible(false);
			district.setVisible(false);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useArea) {
			cluster.setVisible(false);
			district.setVisible(false);
			province.setVisible(false);
			region.setVisible(true);
		} else {
			cluster.setVisible(false);
			district.setVisible(false);
			province.setVisible(false);
			region.setVisible(false);
		}

	}

}
