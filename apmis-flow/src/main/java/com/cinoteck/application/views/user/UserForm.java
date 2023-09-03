package com.cinoteck.application.views.user;

import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinoteck.application.UserProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.exception.ZeroException;

import com.vaadin.flow.component.AbstractField;
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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;


import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.CountryHelper;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
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

@Route(value = "/edit-user")
public class UserForm extends FormLayout {
	Dialog neee;
	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);

	List<AreaReferenceDto> regions;
	List<RegionReferenceDto> provinces;
	List<DistrictReferenceDto> districts;
	List<CommunityReferenceDto> communities;
	List<CommunityReferenceDto> communitiesx;

	// TODO: Change labels to use IL8N names for internationalisation
	// NOTE: Fields should use the same naming convention as in UserDto.class
	TextField firstName = new TextField(I18nProperties.getCaption(Captions.firstName));
	TextField lastName = new TextField(I18nProperties.getCaption(Captions.lastName));

	TextField userEmail = new TextField(I18nProperties.getCaption(Captions.User_userEmail));
	TextField phone = new TextField(I18nProperties.getCaption(Captions.User_phone));
	TextField userPosition = new TextField(I18nProperties.getCaption(Captions.User_userPosition));
	TextField userOrganisation = new TextField(I18nProperties.getCaption(Captions.User_userOrganisation));

	ComboBox<AreaReferenceDto> userRegion = new ComboBox<>(I18nProperties.getCaption(Captions.area));
	ComboBox<RegionReferenceDto> userProvince = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	ComboBox<DistrictReferenceDto> userDistrict = new ComboBox<>(I18nProperties.getCaption(Captions.district));
	MultiSelectComboBox<CommunityReferenceDto> userCommunity = new MultiSelectComboBox<>(
			I18nProperties.getCaption(Captions.community));

	ComboBox<AreaReferenceDto> region = new ComboBox<>(I18nProperties.getCaption(Captions.area));
	ComboBox<RegionReferenceDto> province = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	ComboBox<DistrictReferenceDto> district = new ComboBox<>(I18nProperties.getCaption(Captions.district));
//	MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>(
//			I18nProperties.getCaption(Captions.community));

	TextField street = new TextField(I18nProperties.getCaption(Captions.Location_street));
	TextField houseNumber = new TextField(I18nProperties.getCaption(Captions.Location_houseNumber));
	TextField additionalInformation = new TextField(I18nProperties.getCaption(Captions.Location_additionalInformation));
	TextField postalCode = new TextField(I18nProperties.getCaption(Captions.Location_postalCode));
	ComboBox<AreaType> areaType = new ComboBox<>(I18nProperties.getCaption(Captions.Location_areaType));
	TextField city = new TextField(I18nProperties.getCaption(Captions.city));

	TextField userName = new TextField(I18nProperties.getCaption(Captions.User_userName));
	Checkbox activeCheck = new Checkbox();
	private boolean active = true;

	Checkbox commusr = new Checkbox(I18nProperties.getCaption(Captions.User_commonUser));
	private boolean isCommonUser = false;
	MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>(
			I18nProperties.getCaption(Captions.User_userRoles));

	CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
	ComboBox<Language> language = new ComboBox<>(I18nProperties.getCaption(Captions.language));

	CheckboxGroup clusterNo = new CheckboxGroup<>();

	Button save = new Button(I18nProperties.getCaption(Captions.actionSave));
	Button delete = new Button(I18nProperties.getCaption(Captions.actionDelete));
	Button close = new Button(I18nProperties.getCaption(Captions.actionCancel));

	Anchor createPassword = new Anchor("", I18nProperties.getCaption(Captions.userResetPassword));

	Map<String, Component> map = new HashMap<>();

	RegexpValidator patternValidator = new RegexpValidator("^[A-Za-z]+$", "Only letters are allowed");

	EmailValidator emailVal = new EmailValidator(I18nProperties.getCaption(Captions.notaValidEmail));

	String initialLastNameValue = "";
	UserDto usr = new UserDto();
	static UserProvider currentUser = new UserProvider();
	Set<UserRole> roles = new HashSet<UserRole>();
	Set<FormAccess> formAccessesList = new HashSet<FormAccess>();
	private final UserProvider userProvider = new UserProvider();

	boolean editmode = false;

	public UserForm(List<AreaReferenceDto> regions, List<RegionReferenceDto> provinces,
			List<DistrictReferenceDto> districts, UserDto user) {

		addClassName("contact-form");
		HorizontalLayout hor = new HorizontalLayout();
		Icon vaadinIcon = new Icon(VaadinIcon.ARROW_CIRCLE_LEFT_O);
		Span prefixText = new Span(I18nProperties.getCaption(Captions.allUsers));
		prefixText.setClassName("backButtonText");
		HorizontalLayout layout = new HorizontalLayout(vaadinIcon, prefixText);
		vaadinIcon.setClassName("backButton");
		hor.setJustifyContentMode(JustifyContentMode.START);
//		hor.setWidthFull();
		hor.add(layout);
		hor.setHeight("5px");
		hor.setId("backLayout");
		hor.getStyle().set("width", "none !important");
//		this.setColspan(hor, 0);
		layout.addClickListener(event -> fireEvent(new CloseEvent(this)));
		add(hor);
		// Configure what is passed to the fields here
		configureFields(user);
	}

	@SuppressWarnings("unchecked")
	public void configureFields(UserDto user) {
		System.out.println(user + " userrrr value in configure field ");

		H2 pInfo = new H2(I18nProperties.getString(Strings.headingPersonData));

		this.setColspan(pInfo, 2);

		H2 fInfo = new H2(I18nProperties.getCaption(Captions.address));
		this.setColspan(fInfo, 2);

		H2 userData = new H2(I18nProperties.getString(Strings.headingUserData));
		this.setColspan(userData, 2);

		binder.forField(firstName).asRequired(I18nProperties.getString(Strings.headingUserData))
				.bind(UserDto::getFirstName, UserDto::setFirstName);

		binder.forField(lastName).asRequired(I18nProperties.getCaption(Captions.lastNameRequired))
				.bind(UserDto::getLastName, UserDto::setLastName);

		binder.forField(userEmail).asRequired(I18nProperties.getCaption(Captions.emailRequired))
				.bind(UserDto::getUserEmail, UserDto::setUserEmail);
		map.put("email", userEmail);

		binder.forField(phone)
				.withValidator(e -> e.length() >= 13, I18nProperties.getCaption(Captions.enterValidPhoneNumber))
				.bind(UserDto::getPhone, UserDto::setPhone);

		binder.forField(userPosition).bind(UserDto::getUserPosition, UserDto::setUserPosition);

		binder.forField(userOrganisation).bind(UserDto::getUserOrganisation, UserDto::setUserOrganisation);

		binder.forField(userName).asRequired("Please Fill Out a First and Last Name").bind(UserDto::getUserName,
				UserDto::setUserName);

		this.setColspan(activeCheck, 2);
		activeCheck.setLabel("Active ?");
		activeCheck.setValue(active);
		binder.forField(activeCheck).bind(UserDto::isActive, UserDto::setActive);

		commusr.setLabel("Common User ? ");

		binder.forField(commusr).bind(UserDto::isCommomUser, UserDto::setCommomUser);

//		binder.forField(userRoles).asRequired("User Role is Required").bind(UserDto::getUserRoles,
//				UserDto::setUserRoles);
//		this.setColspan(userRoles, 1);

		formAccess.setLabel(I18nProperties.getCaption(Captions.formAccess));
		binder.forField(formAccess).asRequired("Please Fill Out a FormAccess").bind(UserDto::getFormAccess,
				UserDto::setFormAccess);

		binder.forField(language).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		binder.forField(region).bind(UserDto::getArea, UserDto::setArea);

		binder.forField(province).bind(UserDto::getRegion, UserDto::setRegion);

		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);

		binder.forField(clusterNo);

		binder.bind(clusterNo, UserDto::getCommunity, UserDto::setCommunity);

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		region.setItems(regions);
		region.setItemLabelGenerator(AreaReferenceDto::getCaption);
		region.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				province.setItems(provinces);
			}
		});

		province.setItemLabelGenerator(RegionReferenceDto::getCaption);
		province.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				district.setItems(districts);
			}
		});

		district.setItemLabelGenerator(DistrictReferenceDto::getCaption);
		district.addValueChangeListener(e -> {

			DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getValue();
			System.out.println(districtDto + " vvvvvvvddddddDISTRICT CHANGES!!ssssssssssefasdfa:" + e.getValue());

			if (e.getValue() != null) {
				clusterNo.setVisible(true);
				this.setColspan(clusterNo, 2);
				communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());

//				community.setItemLabelGenerator(CommunityReferenceDto::getCaption);
//				community.setItems(communities);

				clusterNo.setLabel(I18nProperties.getCaption(Captions.clusterNumber));

				//commented out not sure what 
				//UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();
				Set<CommunityReferenceDto> data = Collections.<CommunityReferenceDto>emptySet();
				//currentUser.setCommunity(data);
				//FacadeProvider.getUserFacade().saveUser(currentUser);

				if (districtDto != null) {

					List<CommunityReferenceDto> items = FacadeProvider.getCommunityFacade()
							.getAllActiveByDistrict(districtDto.getUuid());
					for (CommunityReferenceDto item : items) {
						if(item.getNumber() == null)
							Notification.show("Cluster Number cannot be empty, please contact support"); //I18nProperties.getString(Strings.clustNot)  )
						
						item.setCaption(item.getNumber() != null ? item.getNumber().toString() : null);
					}
					Collections.sort(items, CommunityReferenceDto.clusternumber);

					clusterNo.setItems(items);
					clusterNo.getChildren().forEach(checkbox -> {
//			            checkbox.getElement().setProperty("id", "checkbox-" + checkbox.getLabel());
						checkbox.getElement().getClassList().add("custom-checkbox-class");

					});
//		            
				}
			}
		});


//		binder.forField(community).bind(UserDto::getCommunity, UserDto::setCommunity);
		street.setPlaceholder(I18nProperties.getCaption(Captions.enterStreetHere));
		houseNumber.setPlaceholder(I18nProperties.getCaption(Captions.enterHouseNumberHere));
		additionalInformation.setPlaceholder(I18nProperties.getCaption(Captions.enterAdditionalInformationHere));
		postalCode.setPlaceholder(I18nProperties.getCaption(Captions.enterPostalCodeHere));
		city.setPlaceholder(I18nProperties.getCaption(Captions.enterCityHere));

		areaType.setLabel(I18nProperties.getCaption(Captions.Location_areaType));
		areaType.setItems(AreaType.values());
//		binder.forField(street).bind(UserDto::getAddress, UserDto::setAddress);

		binder.forField(userName).asRequired(I18nProperties.getCaption(Captions.pleaseFillOutFirstLastname))
				.bind(UserDto::getUserName, UserDto::setUserName);

		// TODO: Change implemenation to only add assignable roles sormas style.
//		userRoles.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		System.out.println(roles + "tttttttttttttttttttttt");
		userRoles.setItems(roles);

		formAccessesList = UserUiHelper.getAssignableForms();

		// TODO: Change implemenation to only add assignable roles sormas style.
//		userRoles.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));

		binder.forField(userRoles).asRequired(I18nProperties.getCaption(Captions.userRoleRequired))
				.bind(UserDto::getUserRoles, UserDto::setUserRoles);
		this.setColspan(userRoles, 1);
		userRoles.addValueChangeListener(e -> updateFieldsByUserRole(e.getValue()));

		formAccess.setLabel(I18nProperties.getCaption(Captions.formAccess));
		formAccess.setItems(UserUiHelper.getAssignableForms());
		binder.forField(formAccess).asRequired(I18nProperties.getCaption(Captions.pleaseFillFormAccess))
				.bind(UserDto::getFormAccess, UserDto::setFormAccess);

		this.setColspan(activeCheck, 2);
		activeCheck.setLabel(I18nProperties.getCaption(Captions.User_active));
		activeCheck.setValue(active);
		binder.forField(activeCheck).bind(UserDto::isActive, UserDto::setActive);

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER) {
			formAccess.setItems(formAccessesList);
		} else {
			formAccessesList.remove(FormAccess.TRAINING);
			formAccessesList.remove(FormAccess.PCA);
			formAccessesList.remove(FormAccess.LQAS);
			formAccessesList.remove(FormAccess.FMS);
			formAccessesList.remove(FormAccess.FLW);
			formAccess.setItems(formAccessesList);
		}
//		commusr.setValue(isCommonUser);

		userRoles.addValueChangeListener(e -> updateFieldsByUserRole(e.getValue()));

		ComboBox<UserType> userTypes = new ComboBox<UserType>();

		userTypes.setItems(UserType.values());

		commusr.addValueChangeListener(e -> {

			UserProvider currentUser = new UserProvider();

			System.out.println((boolean) e.getValue());
			if ((boolean) e.getValue() == true) {
				userTypes.setValue(UserType.COMMON_USER);
				roles.remove(UserRole.ADMIN);
				roles.remove(UserRole.COMMUNITY_INFORMANT);
				roles.remove(UserRole.AREA_ADMIN_SUPERVISOR);
				roles.remove(UserRole.ADMIN_SUPERVISOR);
				roles.remove(UserRole.BAG_USER);
				roles.remove(UserRole.REST_USER);
				roles.add(UserRole.REST_USER);

				userRoles.setItems(roles);
			} else {
				roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
				roles.remove(UserRole.BAG_USER);
				userRoles.setItems(roles);
			}

		});

		language.setItemLabelGenerator(Language::toString);
		language.setItems(Language.getAssignableLanguages());

		binder.forField(language).asRequired(I18nProperties.getString(Strings.languageRequired))
				.bind(UserDto::getLanguage, UserDto::setLanguage);

		add(pInfo, firstName, lastName, userEmail, phone, userPosition, userOrganisation, fInfo, userRegion,
				userProvince, userDistrict, userCommunity, street, houseNumber, additionalInformation, postalCode, city,
				areaType, userData, userName, activeCheck, commusr, userRoles, formAccess, language, region, province,
				district, clusterNo, createPassword);

		createButtonsLayout();
	}

//	public void makeNewPassword(String userUuid, String userEmail, String userName) {
//		String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);
//
//		if (StringUtils.isBlank(userEmail)
//				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {
//			Dialog ttt = new Dialog();
//			ttt.add("UserName : " + userName);
//			ttt.add("Password : " + newPassword);
//			ttt.open();
//
////			showPasswordResetInternalSuccessPopup(newPassword, userName);
//		} else {
////			showPasswordResetExternalSuccessPopup();
//		}
//	}

	public void showPasswordResetInternalSuccessPopup(String newPassword, String userName) {
		neee = new Dialog();
		Label vvv = new Label(I18nProperties.getString(Strings.messageCopyPassword));

		VerticalLayout layout = new VerticalLayout();
		layout.add(new Label(I18nProperties.getString(Strings.messageCopyPassword)));
		Label passwordLabel = new Label("Password:  " + newPassword);
		Label userNameLabel = new Label("Username:  " + userName);

		layout.add(userNameLabel);
		layout.add(passwordLabel);
		Dialog popupWindow = new Dialog();
		popupWindow.setHeaderTitle(I18nProperties.getString(Strings.headingNewPassword));
		layout.setMargin(true);
	}



	public void suggestUserName(boolean editMode) {

//		fireEvent(new UserFieldValueChangeEvent(this, binder.getBean()));
		if (editMode) {

			System.out.println(lastName.getValue() + "xxxxxxxxxchecking edit mode eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

			lastName.addValueChangeListener(e -> {

				if (userName.isEmpty()) {
					userName.setValue(UserHelper.getSuggestedUsername(firstName.getValue(), lastName.getValue()));
				}
			});

		}
	}

	private void createButtonsLayout() {
//		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addClickShortcut(Key.ESCAPE);

		lastName.addValueChangeListener(e -> {
			suggestUserName(true);
		});

		save.addClickListener(event -> validateAndSave());
		delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
		close.setEnabled(true);
		close.addClickListener(event -> fireEvent(new CloseEvent(this)));

		HorizontalLayout horizontallayout = new HorizontalLayout(save, close);
		horizontallayout.setJustifyContentMode(JustifyContentMode.END);
		horizontallayout.setMargin(true);
		add(horizontallayout);
		this.setColspan(horizontallayout, 2);
	}

	private void validateAndSave() {
//		map.forEach((key, value) -> {
//			Component formField = map.get(key);
//			if (value instanceof TextField) {
//
//				TextField formFieldxx = (TextField) value;
//				ValidationResult requiredValidation = emailVal.apply(formFieldxx.getValue(), null);
////				ValidationResult secondRequiredValidation = patternValidator.apply(formFieldxx.getValue(), null);
//				if (requiredValidation.isError()) {
//
//					// Handle required field validation error
//					formFieldxx.setInvalid(true);
//					formFieldxx.setErrorMessage(requiredValidation.getErrorMessage());
//				} else {
		fireEvent(new SaveEvent(this, binder.getBean()));
//				}
//			}
//
//		});
	}
	
	private void resetpassword() {
		fireEvent(new ResetPasswordEvent(this, binder.getBean()));

	}



	public void setUser(UserDto user) {
		binder.setBean(user);
	}

	public static abstract class UserFormEvent extends ComponentEvent<UserForm> {
		private UserDto user;

		protected UserFormEvent(UserForm source, UserDto user) {
			super(source, false);
			this.user = user;
		}

		public UserDto getContact() {
			return user;
		}
	}

	public static class SaveEvent extends UserFormEvent {
		SaveEvent(UserForm source, UserDto user) {
			super(source, user);
		}
	}

	public static class UserFieldValueChangeEvent extends UserFormEvent {
		UserFieldValueChangeEvent(UserForm source, UserDto user) {
			super(source, user);
		}
	}

	public static class DeleteEvent extends UserFormEvent {
		DeleteEvent(UserForm source, UserDto user) {
			super(source, user);
		}

	}

	public static class CloseEvent extends UserFormEvent {
		CloseEvent(UserForm source) {
			super(source, new UserDto());
		}
	}

	public static class ResetPasswordEvent extends UserFormEvent {
		ResetPasswordEvent(UserForm source , UserDto user) {
			super(source, new UserDto());
		}
	}

	public Registration addResetPasswordListener(ComponentEventListener<ResetPasswordEvent> listener) {
		return addListener(ResetPasswordEvent.class, listener);
	}

	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
		return addListener(DeleteEvent.class, listener);
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);
	}

	public Registration addUserFieldValueChangeEventListener(
			ComponentEventListener<UserFieldValueChangeEvent> listener) {
		return addListener(UserFieldValueChangeEvent.class, listener);
	}

	// TODO: This algorithm can be written better for good time and space complexity
	private void updateFieldsByUserRole(Set<UserRole> userRoles) {

		final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(userRoles);
		final boolean useCommunity = jurisdictionLevel == JurisdictionLevel.COMMUNITY;
		final boolean useDistrict = jurisdictionLevel == JurisdictionLevel.DISTRICT || useCommunity;
		final boolean useRegion = jurisdictionLevel == JurisdictionLevel.REGION || useDistrict;
		final boolean useArea = jurisdictionLevel == JurisdictionLevel.AREA || useRegion;

		if (useCommunity) {
//			community.setVisible(true);
			district.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useDistrict) {
//			community.setVisible(false);
			district.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useRegion) {
//			community.setVisible(false);
			district.setVisible(false);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useArea) {
//			community.setVisible(false);
			district.setVisible(false);
			province.setVisible(false);
			region.setVisible(true);
		} else {
//			community.setVisible(false);
			district.setVisible(false);
			province.setVisible(false);
			region.setVisible(false);
		}

	}
	
	public static void updateItems(CheckboxGroup select, Set<?> items) {
		Set<?> value = select.getSelectedItems();
		boolean readOnly = select.isReadOnly();
		select.setReadOnly(false);
		select.removeAll();
		if (items != null) {
			select.setItems(items);
		}
		select.setValue(value);
		select.setReadOnly(readOnly);
	}

	public static Set<UserRole> getAssignableRoles(Set<UserRole> assignedUserRoles) {

		final Set<UserRole> assignedRoles = assignedUserRoles == null ? Collections.emptySet() : assignedUserRoles;

		try {
			System.out.println(currentUser.getUser().getUserRoles() + "uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
		} catch (Exception e) {
			e.printStackTrace();
		}

		Set<UserRole> allRoles = UserRole.getAssignableRoles(UserProvider.getCurrent().getUserRoles());

		if (!FacadeProvider.getConfigFacade().isConfiguredCountry(CountryHelper.COUNTRY_CODE_SWITZERLAND)) {
			allRoles.remove(UserRole.BAG_USER);
		}

		Set<UserRole> enabledUserRoles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();

		allRoles.removeIf(userRole -> !enabledUserRoles.contains(userRole) && !assignedRoles.contains(userRole));

		return allRoles;
	}

}