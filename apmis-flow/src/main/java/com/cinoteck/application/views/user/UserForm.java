package com.cinoteck.application.views.user;

import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.cinoteck.application.UserProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.exception.ZeroException;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.selection.SelectionEvent;
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
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserHelper;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@Route(value = "/edit-user")
public class UserForm extends FormLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6918900346481904170L;

	private boolean isDistrictMulti = false;
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

//	ComboBox<AreaReferenceDto> userRegion = new ComboBox<>(I18nProperties.getCaption(Captions.area));
//	ComboBox<RegionReferenceDto> userProvince = new ComboBox<>(I18nProperties.getCaption(Captions.region));
//	ComboBox<DistrictReferenceDto> userDistrict = new ComboBox<>(I18nProperties.getCaption(Captions.district));
//	MultiSelectComboBox<CommunityReferenceDto> userCommunity = new MultiSelectComboBox<>(
//			I18nProperties.getCaption(Captions.community));

	ComboBox<AreaReferenceDto> region = new ComboBox<>(I18nProperties.getCaption(Captions.area));
	ComboBox<RegionReferenceDto> province = new ComboBox<>(I18nProperties.getCaption(Captions.region));
	ComboBox<DistrictReferenceDto> district = new ComboBox<>(I18nProperties.getCaption(Captions.district));

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
//	commusr.addClassName();
//	commusr.getStyle().set("display", "inline-flex");
	private boolean isCommonUser = false;
	MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>(
			I18nProperties.getCaption(Captions.User_userRoles));

	CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
//	CheckboxGroup<FormAccess> preCampformAccess = new CheckboxGroup<>();
//	CheckboxGroup<FormAccess> intraCampformAccess = new CheckboxGroup<>();
//	CheckboxGroup<FormAccess> postCampformAccess = new CheckboxGroup<>();

	ComboBox<Language> language = new ComboBox<>(I18nProperties.getCaption(Captions.language));

	CheckboxGroup districtMulti = new CheckboxGroup<>();
	CheckboxGroup clusterNo = new CheckboxGroup<>();

	Button save = new Button(I18nProperties.getCaption(Captions.actionSave));
	Button delete = new Button(I18nProperties.getCaption(Captions.actionDelete));
	Button close = new Button(I18nProperties.getCaption(Captions.actionCancel));

	Button createPassword = new Button(I18nProperties.getCaption(Captions.userResetPassword));

	ConfirmDialog _dialog = new ConfirmDialog();

	Map<String, Component> map = new HashMap<>();
	RegexpValidator patternValidator = new RegexpValidator("^[A-Za-z]+$", "Only letters are allowed");
	EmailValidator emailVal = new EmailValidator(I18nProperties.getCaption(Captions.notaValidEmail));

	String initialLastNameValue = "";
	UserDto usr = new UserDto();
	static UserProvider currentUser = new UserProvider();
	Set<UserRole> roles = new HashSet<UserRole>();
	Set<FormAccess> formAccessesList = new LinkedHashSet<FormAccess>();
	Set<FormAccess> preCampformAccessesList = new LinkedHashSet<FormAccess>();
	Set<FormAccess> intraCampformAccessesList = new LinkedHashSet<FormAccess>();
	Set<FormAccess> postCampformAccessesList = new LinkedHashSet<FormAccess>();
	private final UserProvider userProvider = new UserProvider();
	H2 pInfo = new H2(I18nProperties.getString(Strings.headingPersonData));
	H2 userData = new H2(I18nProperties.getString(Strings.headingUserData));

	boolean editmode = false;
	UserDto user;
//	Button resetUserPassword = new Button();

	public UserForm(List<AreaReferenceDto> regions, List<RegionReferenceDto> provinces,
			List<DistrictReferenceDto> districts, UserDto user, boolean editmode) {

		this.user = user;
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
		System.out.println("____TRSTING LANGUAGE TRANSLATOR : " + I18nProperties.getUserLanguage());
		updatePasswordDialog();
	}

	public UserForm() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	public void configureFields(UserDto user) {

		pInfo = new H2(I18nProperties.getString(Strings.headingPersonData));
		this.setColspan(pInfo, 2);

		userData = new H2(I18nProperties.getString(Strings.headingUserData));
		this.setColspan(userData, 2);

		firstName = new TextField(I18nProperties.getCaption(Captions.firstName));
		binder.forField(firstName).asRequired(I18nProperties.getCaption(Captions.firstNameRequired))
				.bind(UserDto::getFirstName, UserDto::setFirstName);

		binder.forField(lastName).asRequired(I18nProperties.getCaption(Captions.lastNameRequired))
				.bind(UserDto::getLastName, UserDto::setLastName);

		binder.forField(userEmail)// .asRequired(I18nProperties.getCaption(Captions.emailRequired))
				.bind(UserDto::getUserEmail, UserDto::setUserEmail);
		// map.put("email", userEmail);

		binder.forField(phone).bind(UserDto::getPhone, UserDto::setPhone);

		binder.forField(userPosition).bind(UserDto::getUserPosition, UserDto::setUserPosition);

		binder.forField(userOrganisation).bind(UserDto::getUserOrganisation, UserDto::setUserOrganisation);

		binder.forField(userName).asRequired("Please Fill Out a First and Last Name").bind(UserDto::getUserName,
				UserDto::setUserName);

		this.setColspan(activeCheck, 2);
		activeCheck.setLabel("Active ?");
		activeCheck.setValue(active);
		binder.forField(activeCheck).bind(UserDto::isActive, UserDto::setActive);

//		commusr.setLabel("Common User ? ");

		binder.forField(commusr).bind(UserDto::isCommomUser, UserDto::setCommomUser);

//		binder.forField(userRoles).asRequired("User Role is Required").bind(UserDto::getUserRoles,
//				UserDto::setUserRoles);
//		this.setColspan(userRoles, 1);

		formAccess.setLabel(I18nProperties.getCaption(Captions.formAccess));
//		preCampformAccess.setLabel(I18nProperties.getCaption(Captions.preCampaign + " :"));
//		intraCampformAccess.setLabel(I18nProperties.getCaption(Captions.intraCampaign + " :"));
//		postCampformAccess.setLabel(I18nProperties.getCaption(Captions.postCampaign + " :"));
		binder.forField(formAccess).bind(UserDto::getFormAccess, UserDto::setFormAccess);

//		binder.forField(preCampformAccess).bind(UserDto::getFormAccess, UserDto::setFormAccess);
//
//		binder.forField(intraCampformAccess).bind(UserDto::getFormAccess, UserDto::setFormAccess);
//
//		binder.forField(postCampformAccess).bind(UserDto::getFormAccess, UserDto::setFormAccess);

		binder.forField(language).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		binder.forField(userRoles).withValidator(new UserRolesValidator())
				.asRequired(I18nProperties.getCaption(Captions.userRoleRequired))
				.bind(UserDto::getUserRoles, UserDto::setUserRoles);

		binder.forField(region).bind(UserDto::getArea, UserDto::setArea);

		binder.forField(province).bind(UserDto::getRegion, UserDto::setRegion);

		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);

		binder.forField(districtMulti);

		binder.bind(districtMulti, UserDto::getDistricts, UserDto::setDistricts);
		districtMulti.setVisible(true);

		binder.forField(clusterNo);

		binder.bind(clusterNo, UserDto::getCommunity, UserDto::setCommunity);

		binder.forField(userName).asRequired(I18nProperties.getCaption(Captions.pleaseFillOutFirstLastname))
				.bind(UserDto::getUserName, UserDto::setUserName);

		// TODO: Change implemenation to only add assignable roles sormas style.
//		userRoles.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);

		List<UserRole> rolesz = new ArrayList<>(roles); // Convert Set to List
		roles.remove(UserRole.BAG_USER);

		// Sorting the user roles usng comprtor
		Collections.sort(rolesz, new UserRoleCustomComparator());

		// then i'm converting back to a set for facade to handle save properly.
		Set<UserRole> sortedUserRoles = new TreeSet<>(rolesz);

		userRoles.setItems(sortedUserRoles);

		this.setColspan(userRoles, 1);
		userRoles.addValueChangeListener(e -> {
			updateFieldsByUserRole(e.getValue());
			validateUserRoles();
		});

//		userRoles.addValueChangeListener(e -> updateFieldsByUserRole(e.getValue()));

		ComboBox<UserType> userTypes = new ComboBox<UserType>();

		userTypes.setItems(UserType.values());

		language.setItemLabelGenerator(Language::toString);
		language.setItems(Language.getAssignableLanguages());

		binder.forField(language).asRequired(I18nProperties.getString(Strings.languageRequired))
				.bind(UserDto::getLanguage, UserDto::setLanguage);

		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			region.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			region.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			region.setItems(regions);
		}

		region.setItemLabelGenerator(AreaReferenceDto::getCaption);
		region.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				districtMulti.setVisible(false);
				clusterNo.setVisible(false);
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					province.setItems(
							FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					province.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid()));
				} else {
					province.setItems(provinces);
				}
			}
		});

		province.setItemLabelGenerator(RegionReferenceDto::getCaption);
		province.addValueChangeListener(e -> {

			if (e.getValue() != null && userRoles.getValue() != null) {

				final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(userRoles.getValue());
				System.out.println((jurisdictionLevel == JurisdictionLevel.DISTRICT) + " +++___________111"
						+ userRoles.getValue());
				if (jurisdictionLevel == JurisdictionLevel.DISTRICT) {

					districtMulti.setVisible(true);
					district.setVisible(false);
					clusterNo.setVisible(false);
					this.setColspan(districtMulti, 2);
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());

					districtMulti.setLabel(I18nProperties.getCaption(Captions.district));

					for (DistrictReferenceDto item : districts) {
						System.out.println((jurisdictionLevel == JurisdictionLevel.DISTRICT) + " +++___________222: "
								+ item.getCaption());

						if (item.getCaption() == null) {

							Notification notification = new Notification();
							notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
							notification.setPosition(Position.MIDDLE);
							Button closeButton = new Button(new Icon("lumo", "cross"));
							closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
							closeButton.getElement().setAttribute("aria-label", "Close");
							closeButton.addClickListener(event -> {
								notification.close();
							});

							Paragraph text = new Paragraph("District cannot be empty, please contact support");

							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();
						}

//							item.setCaption(item.getNumber() != null ? item.getNumber().toString() : null);
					}
//						Collections.sort(items, CommunityReferenceDto.clusternumber);

					districtMulti.setItems(districts);
					district.setItems(districts);
					isDistrictMulti = true;
					districtMulti.getChildren().forEach(checkbox -> {
//				            checkbox.getElement().setProperty("id", "checkbox-" + checkbox.getLabel());
						checkbox.getElement().getClassList().add("custom-checkbox-class");

					});
//			            

				} else {

					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
					System.out.println(" +++___________333333: ");
					if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
						district.setItems(
								FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid()));
					} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
						district.setItems(
								FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid()));
					} else {
						district.setItems(districts);
					}
				}
			}

		});

		district.setItemLabelGenerator(DistrictReferenceDto::getCaption);
		district.addValueChangeListener(e -> {
			if (!isDistrictMulti) {
				DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getValue();
				System.out.println(districtDto + " vvvvvvvddddddDISTRICT CHANGES!!ssssssssssefasdfa:" + e.getValue());

				if (e.getValue() != null) {
					clusterNo.setVisible(true);
					this.setColspan(clusterNo, 2);
					communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());

					clusterNo.setLabel(I18nProperties.getCaption(Captions.clusterNumber));

					if (districtDto != null) {

						List<CommunityReferenceDto> items = FacadeProvider.getCommunityFacade()
								.getAllActiveByDistrict(districtDto.getUuid());
						for (CommunityReferenceDto item : items) {
							if (item.getNumber() == null) {

								Notification notification = new Notification();
								notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
								notification.setPosition(Position.MIDDLE);
								Button closeButton = new Button(new Icon("lumo", "cross"));
								closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
								closeButton.getElement().setAttribute("aria-label", "Close");
								closeButton.addClickListener(event -> {
									notification.close();
								});

								Paragraph text = new Paragraph(
										"Cluster Number cannot be empty, please contact support");

								HorizontalLayout layout = new HorizontalLayout(text, closeButton);
								layout.setAlignItems(Alignment.CENTER);

								notification.add(layout);
								notification.open();
							}

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
			} else {
				district.clear();
				district.setVisible(false);
				clusterNo.clear();
				clusterNo.setVisible(false);
			}
		});

		commusr.addValueChangeListener(e -> {

			System.out.println((boolean) e.getValue());
			if ((boolean) e.getValue() == true) {
				userTypes.setValue(UserType.COMMON_USER);
				sortedUserRoles.remove(UserRole.ADMIN);
				sortedUserRoles.remove(UserRole.COMMUNITY_INFORMANT);
				sortedUserRoles.remove(UserRole.AREA_ADMIN_SUPERVISOR);
				sortedUserRoles.remove(UserRole.ADMIN_SUPERVISOR);
				sortedUserRoles.remove(UserRole.BAG_USER);
				sortedUserRoles.remove(UserRole.REST_USER);

				userRoles.setItems(sortedUserRoles);
			}

			if ((boolean) e.getValue() == false) {
				Set<UserRole> sortedUserRoless = new TreeSet<>(rolesz);
				userRoles.setItems(sortedUserRoless);
			}
		});

		formAccess.setLabel(I18nProperties.getCaption(Captions.formAccess));
//		preCampformAccess.setLabel(I18nProperties.getCaption(Captions.preCampaign) + " : ");
//		intraCampformAccess.setLabel(I18nProperties.getCaption(Captions.intraCampaign) + " : ");
//		postCampformAccess.setLabel(I18nProperties.getCaption(Captions.postCampaign) + " : ");

		formAccess.setItemLabelGenerator(FormAccess::getDisplayName);
//		preCampformAccess.setItemLabelGenerator(FormAccess::getDisplayName);
//		intraCampformAccess.setItemLabelGenerator(FormAccess::getDisplayName);
//		postCampformAccess.setItemLabelGenerator(FormAccess::getDisplayName);

//		formAccess.addSelectionListener(this::handleSelectionChange);
//		preCampformAccess.addSelectionListener(this::handleSelectionChange);
//		intraCampformAccess.addSelectionListener(this::handleSelectionChange);
//		postCampformAccess.addSelectionListener(this::handleSelectionChange);

		this.setColspan(activeCheck, 2);
		activeCheck.setLabel(I18nProperties.getCaption(Captions.User_active));
		activeCheck.setValue(active);
		binder.forField(activeCheck).bind(UserDto::isActive, UserDto::setActive);

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER) {
			formAccessesList.add(FormAccess.ARCHIVE);
			formAccessesList.add(FormAccess.FLW);
			formAccessesList.add(FormAccess.TRAINING);
			formAccessesList.add(FormAccess.ICM);
			formAccessesList.add(FormAccess.ADMIN);
			formAccessesList.add(FormAccess.EAG_ICM);
			formAccessesList.add(FormAccess.EAG_ADMIN);
			formAccessesList.add(FormAccess.PCA);
			formAccessesList.add(FormAccess.FMS);
			formAccessesList.add(FormAccess.LQAS);
			formAccessesList.add(FormAccess.EAG_PCA);
			formAccessesList.add(FormAccess.EAG_FMS);
			formAccessesList.add(FormAccess.EAG_LQAS);
			formAccess.setItems(formAccessesList);
			//preCampformAccess.setItems(preCampformAccessesList);
		//	intraCampformAccess.setItems(intraCampformAccessesList);
		//	postCampformAccess.setItems(postCampformAccessesList);
		} else {
			formAccessesList.remove(FormAccess.FLW);
			formAccessesList.remove(FormAccess.TRAINING);
			formAccessesList.remove(FormAccess.PCA);
			formAccessesList.remove(FormAccess.LQAS);
			formAccessesList.remove(FormAccess.FMS);

			formAccessesList.add(FormAccess.ARCHIVE);
		//	formAccess.setItems(formAccessesList);
			//preCampformAccess.setVisible(false);
			formAccessesList.add(FormAccess.ICM);
			formAccessesList.add(FormAccess.ADMIN);
			formAccessesList.add(FormAccess.EAG_ICM);
			formAccessesList.add(FormAccess.EAG_ADMIN);
			//intraCampformAccess.setItems(intraCampformAccessesList);
			//postCampformAccess.setVisible(false);
			
			formAccess.setItems(formAccessesList);
		}
//		commusr.setValue(isCommonUser);
		
		Div formAccessCheckers = new Div();
//		Div formAccessPre = new Div();
//		Div formAccessIntra = new Div();
//		Div formAccessPost = new Div();
//		
//		formAccessesList.add(FormAccess.ARCHIVE);
//		formAccessesList.remove(FormAccess.FLW);
//		formAccessesList.remove(FormAccess.TRAINING);
//		
//		
//		formAccessPre.add(formAccessesList.stream().filter(null);
		formAccessCheckers.add(formAccess);
		
		
		
		
		language.setWidthFull();
		region.setWidthFull();
		province.setWidthFull();
		district.setWidthFull();

		VerticalLayout otherFormField = new VerticalLayout(language, region, province, district);
		formAccess.setId("formaccesschkid");
		VerticalLayout formAccessParentLayout = new VerticalLayout(formAccess);//, preCampformAccess, intraCampformAccess,
			//	postCampformAccess);
		formAccessParentLayout.setPadding(false);
		formAccessParentLayout.setSpacing(false);
		HorizontalLayout allFieldLayout = new HorizontalLayout(formAccessParentLayout, otherFormField);
		this.setColspan(allFieldLayout, 2);

		add(pInfo, firstName, lastName, userEmail, phone, userPosition, userOrganisation, userData, userName,
				activeCheck, commusr, userRoles, allFieldLayout, districtMulti, clusterNo);

		createButtonsLayout();
		String checkBoxDivider =
				"var parentDiv = document.getElementById(\"formaccesschkid\");\n"
				+ "if (parentDiv) {\n"
				+ "var children = parentDiv.querySelectorAll(\"vaadin-checkbox\");\n"
				+ "for (var i = 0; i < children.length; i++) {\n"
				+ "console.log(children[i]);\n"
				+ "if (i === 0) {\n"
				+ "var newContentL = '<hr><b class=\"formaccesschkbox\">Pre-Campaign</b><hr>';\n"
				+ "children[i].insertAdjacentHTML('afterend', newContentL);\n"
				+ "}\n"
				
				+ "if (i === 2) {\n"
				+ "var newContentL = '<hr><b class=\"formaccesschkbox\">Intra-Campaign</b><hr>';\n"
				+ "children[i].insertAdjacentHTML('afterend', newContentL);\n"
				+ "}\n"
							
								
				+ "if (i === 6) {\n"
				+ "var newContentL = '<hr><b class=\"formaccesschkbox\">Post-Campaign</b><hr>';\n"
				+ "children[i].insertAdjacentHTML('afterend', newContentL);\n"
				+ "}\n"
				
				+"}}";
		
		
		
		UI.getCurrent().getPage().executeJs(checkBoxDivider);
		
		
	}

	public void updatePasswordDialog() {

		_dialog.setHeader("Update Password");

		_dialog.setCloseOnEsc(false);
		_dialog.setCancelable(true);
		_dialog.addCancelListener(e -> _dialog.close());

		_dialog.setRejectable(true);
		_dialog.setRejectText("Cancel");
		_dialog.addRejectListener(e -> _dialog.close());

		_dialog.setConfirmText("Really Update Password");
		_dialog.addConfirmListener(e -> makeNewPassword(binder.getBean().getUuid(), binder.getBean().getUserEmail(),
				binder.getBean().getUserName()));
	}

	public void makeNewPassword(String userUuid, String userEmail, String userName) {
		String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);

		if (StringUtils.isBlank(userEmail)
				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {

			Dialog newUserPop = new Dialog();
			newUserPop.setClassName("passwordsDialog");
			VerticalLayout infoLayout = new VerticalLayout();

			Paragraph infoText = new Paragraph("Please , copy this password, it is shown only once.");
			newUserPop.setHeaderTitle("Password Updated");

			H3 username = new H3(I18nProperties.getCaption(Captions.Login_username) + " : " + userName);
			username.getStyle().set("color", "#0D6938");

			H3 password = new H3(I18nProperties.getCaption(Captions.Login_password) + " : " + newPassword);
			password.getStyle().set("color", "#0D6938");

			infoLayout.add(username, password);

			newUserPop.add(infoLayout);

			newUserPop.setOpened(true);

		} else {
			Dialog newUserPop = new Dialog();
			newUserPop.setClassName("passwordsDialog");
			VerticalLayout infoLayout = new VerticalLayout();

			Paragraph infoText = new Paragraph("Please , copy this password, it is shown only once.");
			newUserPop.setHeaderTitle("Password Updated");

			H3 username = new H3(I18nProperties.getCaption(Captions.Login_username) + " : " + userName);
			username.getStyle().set("color", "#0D6938");

			H3 password = new H3(I18nProperties.getCaption(Captions.Login_password) + " : " + newPassword);
			password.getStyle().set("color", "#0D6938");

			infoLayout.add(username, password);

			newUserPop.add(infoLayout);

			newUserPop.setOpened(true);
		}
	}

	public void suggestUserName(boolean editMode) {

//		fireEvent(new UserFieldValueChangeEvent(this, binder.getBean()));
		if (editMode) {

			lastName.addValueChangeListener(e -> {

				if (userName.isEmpty()) {
					userName.setValue(UserHelper.getSuggestedUsername(firstName.getValue(), lastName.getValue()));
				}
			});

		}
	}

	private void handleSelectionChange(SelectionEvent<CheckboxGroup<FormAccess>, FormAccess> event) {
	
//		if(formAccess.getSelectedItems().size() > 0){
//			formAccess.deselectAll();
//		}
//		if(preCampformAccess.getSelectedItems().size() > 0){
//			preCampformAccess.deselectAll();
//		}
//		if(intraCampformAccess.getSelectedItems().size() > 0){
//			intraCampformAccess.deselectAll();
//		}
//		if(postCampformAccess.getSelectedItems().size() > 0){
//			postCampformAccess.deselectAll();
//		}

	}

	private void createButtonsLayout() {
		createPassword.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addThemeVariants(ButtonVariant.LUMO_ERROR);
		close.addClickShortcut(Key.ESCAPE);

		delete.addClickListener(event -> fireEvent(new DeleteEvent(this, binder.getBean())));
		close.setEnabled(true);
		close.addClickListener(event -> fireEvent(new CloseEvent(this)));

		createPassword.addClickListener(event -> {
			_dialog.open();
		});

		HorizontalLayout horizontallayout = new HorizontalLayout();

		createPassword.getStyle().set("margin-right", "auto");
		save.getStyle().set("margin-left", "auto");

		horizontallayout.add(createPassword, save, close);
		add(horizontallayout);
		this.setColspan(horizontallayout, 2);
	}

	public void validateAndSaveEdit(UserDto originalUser, String preceedingUsername) {

		List<FormAccess> formAccesses = new ArrayList<>(binder.getBean().getFormAccess());

		System.out.println(
				formAccesses + "TFOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO" + formAccesses.size());

		if (formAccesses.size() == 0 || formAccesses.size() < 1) {

			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Position.MIDDLE);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(eventx -> {
				notification.close();
			});
			Paragraph text = new Paragraph("Error : Form Access is Required, Please Fill Out a FormAccess to proceed.");
			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);
			notification.add(layout);
			notification.open();

		} else {
			if (binder.validate().isOk()) {

				boolean isErrored = false;

				// userName

				if (binder.getBean().getUserEmail() != null || binder.getBean().getUserEmail() != "") {

					UserDto anyEmailFromDb = FacadeProvider.getUserFacade().getByEmail(binder.getBean().getUserEmail());

					if (anyEmailFromDb == null) {

						isErrored = false;

					} else {

						if (anyEmailFromDb.getUserName().trim().equals(originalUser.getUserName().trim())
								&& !originalUser.getUserName().isEmpty()) {

							isErrored = false;

						} else {

							isErrored = true;
							Notification notification = new Notification();
							notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
							notification.setPosition(Position.MIDDLE);
							Button closeButton = new Button(new Icon("lumo", "cross"));
							closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
							closeButton.getElement().setAttribute("aria-label", "Close");
							closeButton.addClickListener(event -> {
								notification.close();
							});

							Paragraph text = new Paragraph("Error : Email already in the system.");

							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();

							return;

						}
					}
				}

				if (binder.getBean().getUserName().contains(" ")) {
					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("Error : Username cannot contain white space");

					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
					isErrored = true;
					return;
				} else {

					if (binder.getBean().getUserName() != null || userName != null) {

						UserDto retrieveBinderUserFromDb = FacadeProvider.getUserFacade()
								.getByUserName(binder.getBean().getUserName());
						String originalUserx = originalUser == null ? binder.getBean().getUserName()
								: originalUser.getUserName().trim();
						if (retrieveBinderUserFromDb == null) {
							if (binder.getBean().getUserName().contains(" ")) {
								Notification notification = new Notification();
								notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
								notification.setPosition(Position.MIDDLE);
								Button closeButton = new Button(new Icon("lumo", "cross"));
								closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
								closeButton.getElement().setAttribute("aria-label", "Close");
								closeButton.addClickListener(event -> {
									notification.close();
								});

								Paragraph text = new Paragraph("Error : Username cannot contain white space");

								HorizontalLayout layout = new HorizontalLayout(text, closeButton);
								layout.setAlignItems(Alignment.CENTER);

								notification.add(layout);
								notification.open();
								isErrored = true;
								return;
							} else if (!preceedingUsername.equals(originalUserx)) {
								System.out
										.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
								fireEvent(new SaveEvent(this, binder.getBean()));

							} else {
								Notification notification = new Notification();
								notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
								notification.setPosition(Position.MIDDLE);
								Button closeButton = new Button(new Icon("lumo", "cross"));
								closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
								closeButton.getElement().setAttribute("aria-label", "Close");
								closeButton.addClickListener(event -> {
									notification.close();
								});

								Paragraph text = new Paragraph(
										"Error : Unknow error surrounding the supplied username");

								HorizontalLayout layout = new HorizontalLayout(text, closeButton);
								layout.setAlignItems(Alignment.CENTER);

								notification.add(layout);
								notification.open();
								isErrored = true;
								return;
							}
						} else if (retrieveBinderUserFromDb.getUserName().trim().equalsIgnoreCase(originalUserx)
								&& !originalUserx.isEmpty() && !isErrored) {
							if (preceedingUsername.equals(originalUserx)) {
								fireEvent(new SaveEvent(this, binder.getBean()));

							} else {
								UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade()
										.getByUserName(binder.getBean().getUserName());
								if (checkNewusernamefromDB == null) {
									fireEvent(new SaveEvent(this, binder.getBean()));
								} else {
									Notification notification = new Notification();
									notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
									notification.setPosition(Position.MIDDLE);
									Button closeButton = new Button(new Icon("lumo", "cross"));
									closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
									closeButton.getElement().setAttribute("aria-label", "Close");
									closeButton.addClickListener(event -> {
										notification.close();
									});

									Paragraph text = new Paragraph("Error : Username not unique");

									HorizontalLayout layout = new HorizontalLayout(text, closeButton);
									layout.setAlignItems(Alignment.CENTER);

									notification.add(layout);
									notification.open();

									return;
								}
							}

						} else {

							if (FacadeProvider.getUserFacade().getByUserName(binder.getBean().getUserName()) != null) {

								Notification notification = new Notification();
								notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
								notification.setPosition(Position.MIDDLE);
								Button closeButton = new Button(new Icon("lumo", "cross"));
								closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
								closeButton.getElement().setAttribute("aria-label", "Close");
								closeButton.addClickListener(event -> {
									notification.close();
								});

								Paragraph text = new Paragraph("Error : Username not unique");

								HorizontalLayout layout = new HorizontalLayout(text, closeButton);
								layout.setAlignItems(Alignment.CENTER);

								notification.add(layout);
								notification.open();

								return;
							}
						}
					}
				}
			}
		}

	}

	public void validateAndSaveNew() {
		boolean isErrored = false;

		List<FormAccess> formAccesses = new ArrayList<>(binder.getBean().getFormAccess());
		List<FormAccess> formAccessexs = new ArrayList<>();
		formAccessexs.addAll(formAccesses);

		if (formAccessexs.size() == 0 || formAccessexs.size() < 1) {

			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Position.MIDDLE);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(eventx -> {
				notification.close();
			});
			Paragraph text = new Paragraph("Error : Form Access is Required, Please Fill Out a FormAccess to proceed.");
			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);
			notification.add(layout);
			notification.open();

		} else {

			if (binder.validate().isOk()) {
				System.out.println(binder.getBean().getUserEmail() != null
						+ " validateAndSaveNew++++++++++++++++++++++++++++++++++++ " + binder.getBean().getUserEmail());

				if (binder.getBean().getUserEmail() != null) {

					UserDto binderEmailValidation = FacadeProvider.getUserFacade()
							.getByEmail(binder.getBean().getUserEmail());

					if (binderEmailValidation == null) {

//					isErrored = false;
//					fireEvent(new SaveEvent(this, binder.getBean()));

					} else {

						if (binderEmailValidation.getUserName().trim().equals(binder.getBean().getUserName().trim())
								&& !binder.getBean().getUserName().isEmpty()) {
//email has not changed
//						fireEvent(new SaveEvent(this, binder.getBean()));
						} else {

							Notification notification = new Notification();
							notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
							notification.setPosition(Position.MIDDLE);
							Button closeButton = new Button(new Icon("lumo", "cross"));
							closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
							closeButton.getElement().setAttribute("aria-label", "Close");
							closeButton.addClickListener(event -> {
								notification.close();
							});

							Paragraph text = new Paragraph("Error : Email already in the system...");

							HorizontalLayout layout = new HorizontalLayout(text, closeButton);
							layout.setAlignItems(Alignment.CENTER);

							notification.add(layout);
							notification.open();
							isErrored = true;
							return;

						}
					}
				}

				if (FacadeProvider.getUserFacade().getByUserName(binder.getBean().getUserName()) != null) {

					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("Error : Username not unique");

					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
					isErrored = true;
					return;
				} else if (binder.getBean().getUserName().contains(" ")) {
					Notification notification = new Notification();
					notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					notification.setPosition(Position.MIDDLE);
					Button closeButton = new Button(new Icon("lumo", "cross"));
					closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					closeButton.getElement().setAttribute("aria-label", "Close");
					closeButton.addClickListener(event -> {
						notification.close();
					});

					Paragraph text = new Paragraph("Error : Username cannot contain white space");

					HorizontalLayout layout = new HorizontalLayout(text, closeButton);
					layout.setAlignItems(Alignment.CENTER);

					notification.add(layout);
					notification.open();
					isErrored = true;
					return;
				} else {
					if (!isErrored) {

						fireEvent(new SaveEvent(this, binder.getBean()));

						UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
						userActivitySummaryDto.setActionModule("Users");
						userActivitySummaryDto.setAction("Created User: " + binder.getBean().getUserName());
						userActivitySummaryDto.setCreatingUser_string(userProvider.getUser().getUserName());
						FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);

					}
				}
			}
		}
	}

	class UserRoleCustomComparator implements Comparator<UserRole> {
		private final String[] customOrder = { "Admin", "National Data Manager", "National Officer",
				"National Observer / Partner", "Regional Observer", "Regional Data Manager", "Regional Officer",
				"Provincial Observer", "Provincial Data Clerk", "Provincial Officer", "District Officer",
				"District Observer" };

		@Override
		public int compare(UserRole role1, UserRole role2) {
			// Get the indexes of the roles in the custom order
			int index1 = indexOfRole(role1);
			int index2 = indexOfRole(role2);

			// Compare based on their indexes in the custom order
			return Integer.compare(index1, index2);
		}

		private int indexOfRole(UserRole role) {
			for (int i = 0; i < customOrder.length; i++) {
				if (customOrder[i].equals(role.name())) {
					return i;
				}
			}
			return customOrder.length; // Role not found, place it at the end
		}
	}

	class IndexedUserRole implements Comparable<IndexedUserRole> {
		private UserRole role;
		private int index;

		public IndexedUserRole(UserRole role, int index) {
			this.role = role;
			this.index = index;
		}

		public UserRole getRole() {
			return role;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public int compareTo(IndexedUserRole other) {
			// Compare based on the assigned indexes
			return Integer.compare(this.index, other.index);
		}
	}

	protected void validateUserRoles() {
		map.forEach((key, value) -> {
			Component formField = value;

			if (formField instanceof MultiSelectComboBox) {
				MultiSelectComboBox<UserRole> formFieldxx = (MultiSelectComboBox<UserRole>) formField;
				UserRolesValidator userRolesValidator = new UserRolesValidator();
				ValidationResult validation = userRolesValidator.apply(formFieldxx.getValue(),
						new ValueContext(formFieldxx));

				if (validation.isError()) {
					formFieldxx.setInvalid(true);
					formFieldxx.setErrorMessage(validation.getErrorMessage());
					formFieldxx.setTooltipText(validation.getErrorMessage());
				} else {

				}
			}
		});
	}

	private void resetpassword() {
		fireEvent(new ResetPasswordEvent(this, binder.getBean()));

	}

	public void setUser(UserDto user) {
		this.user = user;
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
		ResetPasswordEvent(UserForm source, UserDto user) {
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
	protected void updateFieldsByUserRole(Set<UserRole> userRoles) {
		final JurisdictionLevel jurisdictionLevel = UserRole.getJurisdictionLevel(userRoles);
		final boolean useCommunity = jurisdictionLevel == JurisdictionLevel.COMMUNITY;
		final boolean useDistrictOnly = jurisdictionLevel == JurisdictionLevel.DISTRICT;
		final boolean useDistrict = jurisdictionLevel == JurisdictionLevel.DISTRICT || useCommunity;
		final boolean useRegion = jurisdictionLevel == JurisdictionLevel.REGION || useDistrict;
		final boolean useArea = jurisdictionLevel == JurisdictionLevel.AREA || useRegion;
		if (useCommunity) {
			clusterNo.setVisible(true);
			district.setVisible(true);
			districtMulti.clear();
			districtMulti.setVisible(false);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useDistrictOnly) {
			clusterNo.clear();
			clusterNo.setVisible(false);
//			district.clear();
			district.setVisible(false);
			districtMulti.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useDistrict) {
			clusterNo.clear();
			clusterNo.setVisible(false);
			district.clear();
			district.setVisible(false);
			districtMulti.setVisible(true);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useRegion) {
			clusterNo.clear();
			clusterNo.setVisible(false);
			district.clear();
			district.setVisible(false);
			districtMulti.clear();
			districtMulti.setVisible(false);
			province.setVisible(true);
			region.setVisible(true);
		} else if (useArea) {
			clusterNo.clear();
			clusterNo.setVisible(false);
			district.clear();
			district.setVisible(false);
			districtMulti.clear();
			districtMulti.setVisible(false);
			province.clear();
			province.setVisible(false);
			region.setVisible(true);
		} else {
			clusterNo.clear();
			clusterNo.setVisible(false);
			district.clear();
			district.setVisible(false);
			districtMulti.clear();
			districtMulti.setVisible(false);
			province.clear();
			province.setVisible(false);
			region.clear();
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