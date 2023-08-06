package com.cinoteck.application.views.user;

import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cinoteck.application.UserProvider;
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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
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
	TextField userEmail = new TextField("Email Address");
	TextField phone = new TextField("Phone Number");
	TextField userPosition = new TextField("Position");
	TextField userOrganisation = new TextField("Organisation");

	ComboBox<AreaReferenceDto> userRegion = new ComboBox<>("Region");
	ComboBox<RegionReferenceDto> userProvince = new ComboBox<>("Province");
	ComboBox<DistrictReferenceDto> userDistrict = new ComboBox<>("District");
	MultiSelectComboBox<CommunityReferenceDto> userCommunity = new MultiSelectComboBox<>("Cluster");

	ComboBox<AreaReferenceDto> region = new ComboBox<>("Region");
	ComboBox<RegionReferenceDto> province = new ComboBox<>("Province");
	ComboBox<DistrictReferenceDto> district = new ComboBox<>("District");
	MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>("Cluster");

	TextField street = new TextField("Street");
	TextField houseNumber = new TextField("House Number");
	TextField additionalInformation = new TextField("Additional Information");
	TextField postalCode = new TextField("Postal Code");
	ComboBox<AreaType> areaType = new ComboBox<>();
	TextField city = new TextField("City");
	TextField userName = new TextField("Username");
	Checkbox activeCheck = new Checkbox();
	private boolean active = true;

	Checkbox usertype = new Checkbox("Common User?");
	ComboBox<Language> language = new ComboBox<>("Language");
	CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
	MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>("User Role");

	Button save = new Button("Save");
	Button delete = new Button("Delete");
	Button close = new Button("Cancel");

	Map<String, Component> map = new HashMap<>();

	RegexpValidator patternValidator = new RegexpValidator("^[A-Za-z]+$", "Only letters are allowed");

	EmailValidator emailVal = new EmailValidator("Not a Valid Email");
	String initialLastNameValue = "";
UserDto usr = new UserDto();
	public UserForm(List<AreaReferenceDto> regions, List<RegionReferenceDto> provinces,
			List<DistrictReferenceDto> districts,UserDto user) {

		addClassName("contact-form");
		HorizontalLayout hor = new HorizontalLayout();
		Icon vaadinIcon = new Icon(VaadinIcon.ARROW_CIRCLE_LEFT_O);
		Span prefixText = new Span("All Users");
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

	

	private void configureFields(UserDto user) {
		this.usr = user;
		H2 pInfo = new H2("Personal Information");
		this.setColspan(pInfo, 2);

		H2 fInfo = new H2("Address");
		this.setColspan(fInfo, 2);

		H2 userData = new H2("User Data");
		this.setColspan(userData, 2);

		binder.forField(firstName).asRequired("First Name is Required").bind(UserDto::getFirstName,
				UserDto::setFirstName);

		binder.forField(lastName).asRequired("Last Name is Required").bind(UserDto::getLastName, UserDto::setLastName);
		
		binder.forField(userEmail).bind(UserDto::getUserEmail, UserDto::setUserEmail);
//		map.put("email", userEmail);

		binder.forField(phone).bind(UserDto::getPhone, UserDto::setPhone);

		binder.forField(userPosition).bind(UserDto::getUserPosition, UserDto::setUserPosition);

		binder.forField(userOrganisation).bind(UserDto::getUserOrganisation, UserDto::setUserOrganisation);

		binder.forField(language).bind(UserDto::getLanguage, UserDto::setLanguage);

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
		CheckboxGroup checkboxGroup = new CheckboxGroup<>();
		binder.bind(checkboxGroup, UserDto::getCommunity, UserDto::setCommunity);

		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);
		district.setItemLabelGenerator(DistrictReferenceDto::getCaption);
		district.addValueChangeListener(e -> {

			DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getValue();
			System.out.println(districtDto + " vvvvvvvddddddDISTRICT CHANGES!!ssssssssssefasdfa:" + e.getValue());

			if (e.getValue() != null) {
				checkboxGroup.setVisible(true);
				this.setColspan(checkboxGroup, 2);
				communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());

				community.setItemLabelGenerator(CommunityReferenceDto::getCaption);
				community.setItems(communities);

				checkboxGroup.setLabel("Cluster Numbers");
				UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();
				Set<CommunityReferenceDto> data = Collections.<CommunityReferenceDto>emptySet();
				currentUser.setCommunity(data);
				FacadeProvider.getUserFacade().saveUser(currentUser);

				if (districtDto != null) {
					List<CommunityReferenceDto> items = FacadeProvider.getCommunityFacade()
							.getAllActiveByDistrict(districtDto.getUuid());
					for (CommunityReferenceDto item : items) {
						item.setCaption(item.getNumber() != null ? item.getNumber().toString() : item.getCaption());
					}
					Collections.sort(items, CommunityReferenceDto.clusternumber);

					checkboxGroup.setItems(items);
//		             FieldHelper
//		                    .updateItems(community, districtDto != null ? items : null);    
				}
			}
		});
//		checkboxGroup.setValue(UserDto.getCommunitynos());
		binder.forField(community).bind(UserDto::getCommunity, UserDto::setCommunity);

//		checkboxGroup.setItems(communitiesx);

		checkboxGroup.addValueChangeListener(event -> {
			// Do something with the selected options (if multiple selection is allowed)
			CommunityReferenceDto communityDto = (CommunityReferenceDto) event.getValue();
			
//			if (event.getValue() != null) {
//				for (CommunityReferenceDto item : communityDto) {
//					item.setCaption(item.getNumber() != null ? item.getNumber().toString() : item.getCaption());
//				}
//			Set<CommunityReferenceDto> data = Collections.sort(items, CommunityReferenceDto.clusternumber);
//			}
		});

		street.setPlaceholder("Enter street here");
		houseNumber.setPlaceholder("Enter House Number here");
		additionalInformation.setPlaceholder("Enter Additional Information here");
		postalCode.setPlaceholder("Enter postal Code here");
		city.setPlaceholder("Enter City here");
		areaType.setLabel("Area Type");
		areaType.setItems(AreaType.values());
//		binder.forField(street).bind(UserDto::getAddress, UserDto::setAddress);

		binder.forField(userName).asRequired("Please Fill Out a First and Last Name").bind(UserDto::getUserName,
				UserDto::setUserName);

		// TODO: Change implemenation to only add assignable roles sormas style.
		userRoles.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		binder.forField(userRoles).asRequired("User Role is Required").bind(UserDto::getUserRoles,
				UserDto::setUserRoles);
		this.setColspan(userRoles, 1);
		userRoles.addValueChangeListener(e -> updateFieldsByUserRole(e.getValue()));

		formAccess.setLabel("Form Access");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		binder.forField(formAccess).asRequired("Please Fill Out a FormAccess").bind(UserDto::getFormAccess,
				UserDto::setFormAccess);

		this.setColspan(activeCheck, 2);
		activeCheck.setLabel("Active ?");
		activeCheck.setValue(active);
		binder.forField(activeCheck).bind(UserDto::isActive, UserDto::setActive);

//		usertype.setItems(UserType.values());
		UserProvider currentUser = UserProvider.getCurrent();
//		binder.forField(usertype).bind(UserD);
		
//		if(currentUser.getUser().getUsertype().equals(UserType.WHO_USER)) {
//			
//		}
		

		usertype.addValueChangeListener(e -> {
        	System.out.println((boolean) e.getValue());
        	if ((boolean) e.getValue() ==  true ) {
//        		usertype.setValue(UserType.COMMON_USER);
        		userRoles.clear();
//            	 final OptionGroup userRolesRemoval = (OptionGroup) getFieldGroup().getField(UserDto.USER_ROLES);
//            	 UserDto userDto = FacadeProvider.getUserFacade().getCurrentUser();
//            	 userRolesRemoval.removeAllItems();
        		userRoles.setItems(UserUiHelper.getAssignableRoles(currentUser.getUser().getUserRoles() ));
//        		userRoles.re .remove(UserRole.COMMUNITY_INFORMANT);
//            	 userRoles.getEl;
//            	 userRolesRemoval.removeItem(UserRole.COMMUNITY_INFORMANT);
//            	 userRolesRemoval.removeItem(UserRole.AREA_ADMIN_SUPERVISOR);
//            	 userRolesRemoval.removeItem(UserRole.ADMIN_SUPERVISOR);
                 
    		}
    		else {
//    			 userTypes.setValue(UserProvider.getCurrent().getUser().getUsertype());
        		userRoles.setItems(UserUiHelper.getAssignableRoles(currentUser.getUser().getUserRoles() ));

//    			 final OptionGroup userRolesRemoval = (OptionGroup) getFieldGroup().getField(UserDto.USER_ROLES);
//            	 UserDto userDto = FacadeProvider.getUserFacade().getCurrentUser();
//            	 userRolesRemoval.removeAllItems();
//            	 userRolesRemoval.addItems(UserUiHelper.getAssignableRoles(userDto.getUserRoles() ));
            	// userRolesRemoval.removeItem(UserRole.ADMIN);
    		} 	
        	
        });

		// this.setColspan(usertype, 2);
		language.setItemLabelGenerator(Language::toString);
		language.setItems(Language.getAssignableLanguages());
		binder.forField(language).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		add(pInfo, firstName, lastName, userEmail, phone, userPosition, userOrganisation, fInfo, userRegion,
				userProvince, userDistrict, userCommunity, street, houseNumber, additionalInformation, postalCode, city,
				areaType, userData, userName, activeCheck, usertype, userRoles, formAccess, language, region, province,
				district, community, checkboxGroup);
		createButtonsLayout();
	}

	public void suggestUserName() {
		if (userName.isEmpty()) {
		userName.setValue(UserHelper.getSuggestedUsername(firstName.getValue(), lastName.getValue()));
		}
	}
	private void createButtonsLayout() {
//		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addClickShortcut(Key.ESCAPE);

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
		map.forEach((key, value) -> {
			Component formField = map.get(key);
			if (value instanceof TextField) {

				TextField formFieldxx = (TextField) value;
				ValidationResult requiredValidation = emailVal.apply(formFieldxx.getValue(), null);
//				ValidationResult secondRequiredValidation = patternValidator.apply(formFieldxx.getValue(), null);
				if (requiredValidation.isError()) {

					// Handle required field validation error
					formFieldxx.setInvalid(true);
					formFieldxx.setErrorMessage(requiredValidation.getErrorMessage());
				} else {
					fireEvent(new SaveEvent(this, binder.getBean()));
				}
			}

		});
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

	public Registration addDeleteListener(ComponentEventListener<DeleteEvent> listener) {
		return addListener(DeleteEvent.class, listener);
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

	public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
		return addListener(CloseEvent.class, listener);
	}

	// TODO: This algorithm can be written better for good time and space complexity
	private void updateFieldsByUserRole(Set<UserRole> userRoles) {

		final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(userRoles);
		final boolean useCommunity = jurisdictionLevel == JurisdictionLevel.COMMUNITY;
		final boolean useDistrict = jurisdictionLevel == JurisdictionLevel.DISTRICT || useCommunity;
		final boolean useRegion = jurisdictionLevel == JurisdictionLevel.REGION || useDistrict;
		final boolean useArea = jurisdictionLevel == JurisdictionLevel.AREA || useRegion;

		if (useCommunity) {
			community.setVisible(true);
			district.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useDistrict) {
			community.setVisible(false);
			district.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useRegion) {
			community.setVisible(false);
			district.setVisible(false);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useArea) {
			community.setVisible(false);
			district.setVisible(false);
			province.setVisible(false);
			region.setVisible(true);
		} else {
			community.setVisible(false);
			district.setVisible(false);
			province.setVisible(false);
			region.setVisible(false);
		}

	}
}