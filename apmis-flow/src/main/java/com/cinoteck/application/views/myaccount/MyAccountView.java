package com.cinoteck.application.views.myaccount;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;

import com.vaadin.flow.component.html.H1;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.component.notification.Notification;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;

import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;


import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
//import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.location.LocationDto;

import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;

import de.symeda.sormas.api.user.UserDto;
//import de.symeda.sormas.ui.utils.InternalPasswordChangeComponent;
//import de.symeda.sormas.ui.utils.VaadinUiUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PageTitle("My Account")
@Route(value = "useraccount", layout = MainLayout.class)

public class MyAccountView extends VerticalLayout implements RouterLayout {
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();

	public MyAccountView() {
		setSpacing(false);
		setPadding(false);
		Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);

		List<AreaReferenceDto> regionss;
		List<RegionReferenceDto> provincess;
		List<DistrictReferenceDto> districtss;
		List<CommunityReferenceDto> communitiess;

		UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();
		// currentUser.getFirstName();

		Div userentry = new Div();

		H3 infooo = new H3("Username");
		infooo.getStyle().set("color", "green");
		infooo.getStyle().set("font-size", "20px");
		infooo.getStyle().set("font-weight", "600");
		infooo.getStyle().set("margin-left", "20px");
		infooo.getStyle().set("margin-bottom", "15px");

		Paragraph infoood = new Paragraph(currentUser.getUserName());
		// Paragraph infoood = new Paragraph(currentUser.getUserRoles().size()+ "");
		infoood.getStyle().set("margin-left", "20px");
		infoood.getStyle().set("margin-bottom", "0px");

		// Div personalInfoo = new Div();
		H3 infoo = new H3("Personal Information");
		infoo.getStyle().set("color", "green");
		infoo.getStyle().set("font-size", "20px");
		infoo.getStyle().set("font-weight", "600");
		infoo.getStyle().set("margin-left", "20px");
		infoo.getStyle().set("margin-bottom", "0px");

		TextField firstnamee = new TextField("");
		firstnamee.setLabel("First Name");
		firstnamee.setValue(currentUser.getFirstName());
		// firstnamee.setClassName("fName");
		firstnamee.setId("my-disabled-textfield");
		firstnamee.getStyle().set("-webkit-text-fill-color", "green");
		firstnamee.setReadOnly(true);
		// binder.forField(firstnamee).asRequired("First Name is
		// Required").bind(UserDto::getFirstName, UserDto::setFirstName);

		TextField lastnamee = new TextField("");
		lastnamee.setLabel("Last Name");
		lastnamee.setValue(currentUser.getLastName());
		lastnamee.getStyle().set("-webkit-text-fill-color", "green");
		lastnamee.setReadOnly(true);
		// binder.forField(lastnamee).asRequired("Last Name is
		// Required").bind(UserDto::getLastName, UserDto::setLastName);

		TextField emailAddresss = new TextField("");
		emailAddresss.setLabel("Email address");
		if (currentUser.getUserEmail() == null) {
			emailAddresss.setPlaceholder("Email address");
		} else {
			emailAddresss.setValue(currentUser.getUserEmail());
		}
		emailAddresss.setReadOnly(true);
		binder.forField(emailAddresss).asRequired("Email Address is Required").bind(UserDto::getUserEmail,
				UserDto::setUserEmail);

		TextField phoneNumberr = new TextField();
		phoneNumberr.setLabel("Phone number");
		if (currentUser.getPhone() == null) {
			phoneNumberr.setPlaceholder("Phone Number");
		} else {
			phoneNumberr.setValue(currentUser.getPhone());
		}
		phoneNumberr.setReadOnly(true);
//		binder.forField(phoneNumberr).withValidator(e -> e.length() >= 10, "Enter a valid Phone Number")
//				.bind(UserDto::getPhone, UserDto::setPhone);

//		Select<String> positionn = new Select<>();
//		positionn.setLabel("Position");
//		positionn.setItems("");
//		positionn.setValue("");

		TextField positionn = new TextField();
		positionn.setLabel("Position");
		// positionn.setValue(currentUser.getUserPosition());
		if (currentUser.getPhone() == null) {
			positionn.setPlaceholder("Position");
		} else {
			positionn.setValue(currentUser.getUserPosition());
		}
		positionn.setReadOnly(true);
		// binder.forField(positionn).bind(UserDto::getUserPosition,
		// UserDto::setUserPosition);

//TODO:: add a model for address in userDto
		TextField addresss = new TextField();
		addresss.setLabel("Address");
		addresss.setReadOnly(true);
		// binder.forField(addresss).bind(UserDto::getAddress,UserDto::setAddress);

		FormLayout dataVieww = new FormLayout();
		dataVieww.add(firstnamee, lastnamee, emailAddresss, phoneNumberr, positionn, addresss);
		dataVieww.getStyle().set("margin-left", "20px");
		dataVieww.getStyle().set("margin-right", "20px");

		Div fieldInfoo = new Div();
		H3 infodataa = new H3("Field Information");
		infodataa.getStyle().set("color", "green");
		infodataa.getStyle().set("font-size", "20px");
		infodataa.getStyle().set("font-weight", "600");
		infodataa.getStyle().set("margin-left", "20px");
		infodataa.getStyle().set("margin-bottom", "0px");

		// Select<String> regionn = new Select<>();
		ComboBox<AreaReferenceDto> regionn = new ComboBox<>("Region");
		// regionn.setLabel("Region");
		binder.forField(regionn).bind(UserDto::getArea, UserDto::setArea);
		regionss = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		regionn.setItems(regionss);
		regionn.setItemLabelGenerator(AreaReferenceDto::getCaption);
		// TODO: come back to add valuechangelistener

//		regionn.setItems("", "Region", "Region", "Region", "Region", "Region", "Region");
//		regionn.setValue("");

		// Select<String> provincee = new Select<>();
		ComboBox<RegionReferenceDto> provincee = new ComboBox<>("Province");
		// provincee.setLabel("Province");
		binder.forField(provincee).bind(UserDto::getRegion, UserDto::setRegion);
		provincee.setItemLabelGenerator(RegionReferenceDto::getCaption);
		// provincee.setItems("", "Province", "Province", "Province", "Province",
		// "Province");
//		provincee.setValue("");


		// Select<String> districtt = new Select<>();
		ComboBox<DistrictReferenceDto> districtt = new ComboBox<>("District");
		// districtt.setLabel("District");
		binder.forField(districtt).bind(UserDto::getDistrict, UserDto::setDistrict);
		districtt.setItemLabelGenerator(DistrictReferenceDto::getCaption);

//		districtt.setItems("", "District", "District", "District", "District", "District");
//		districtt.setValue("");

		// Select<String> clusterr = new Select();

		MultiSelectComboBox<CommunityReferenceDto> cluster = new MultiSelectComboBox<>("Community");
		cluster.setLabel("Cluster");
		binder.forField(cluster).bind(UserDto::getCommunity, UserDto::setCommunity);

		// clusterr.setItems("", "Cluster", "Cluster", "Cluster", "Cluster", "Cluster");

		TextField streett = new TextField();
		streett.setLabel("Street");

		TextField houseNumm = new TextField();
		houseNumm.setLabel("House Number");

		TextField addInfoo = new TextField();
		addInfoo.setLabel("Additional information");

		TextField postalCodee = new TextField();
		postalCodee.setLabel("Postal code");

		TextField cityy = new TextField();
		cityy.setLabel("City");

		Select<String> areaTypee = new Select<>();
		areaTypee.setLabel("Area type (Urban/Rural)");
		areaTypee.setItems("", "Urban", "Rural");
		areaTypee.setValue("");

		TextField contacPersonn = new TextField();
		contacPersonn.setLabel("Cluster contact person");

		FormLayout fielddataVieww = new FormLayout();
		fielddataVieww.setResponsiveSteps(
				// Use one column by default
				new ResponsiveStep("0", 1),
				// Use two columns, if the layout's width exceeds 320px
				new ResponsiveStep("320px", 2),
				// Use three columns, if the layout's width exceeds 500px
				new ResponsiveStep("500px", 3));
		fielddataVieww.add(provincee, regionn, districtt, cluster, streett, houseNumm, addInfoo, postalCodee, cityy,
				areaTypee, contacPersonn);
		fielddataVieww.getStyle().set("margin-left", "20px");
		fielddataVieww.getStyle().set("margin-right", "20px");

		H3 security = new H3("Password & Accesibility");
		security.getStyle().set("color", "green");
		security.getStyle().set("font-size", "20px");
		security.getStyle().set("font-weight", "600");
		security.getStyle().set("margin-left", "20px");
		security.getStyle().set("margin-bottom", "15px");
		security.getStyle().set("margin-top", "16px !important");

//<<<<<<< HEAD
		Dialog passwordDialog = new Dialog();
		// change password button. set it to figma design
		//Button openPasswordPopupButton = new Button("Change Password");
//		passwordDialog.getStyle().set("margin-bottom", "0px");
//		passwordDialog.getStyle().set("margin-top", "12px !important");
		//openPasswordPopupButton.addClickListener(event -> passwordDialog.open());
//=======
		//Dialog passwordDialog = new Dialog();
		// change password button. set it to figma design
		Button openPasswordPopupButton = new Button("Change Password");
//		passwordDialog.getStyle().set("margin-bottom", "0px");
//		passwordDialog.getStyle().set("margin-top", "12px !important");
		
		
		
		openPasswordPopupButton.addClickListener(event -> {
			CredentialPassWordChanger sev = new CredentialPassWordChanger(currentUser);
			});
//>>>>>>> branch 'development' of https://github.com/abahsuccess/APMIS-Project.git
		add();

		VerticalLayout pwdSecc = new VerticalLayout();
		pwdSecc.setClassName("superDiv");

		ComboBox<Language> languagee = new ComboBox<>("Language");
		languagee.setItemLabelGenerator(Language::toString);
		languagee.setItems(Language.getAssignableLanguages());
		languagee.getStyle().set("margin-bottom", "0px");
		languagee.getStyle().set("margin-top", "-15px !important");
//<<<<<<< HEAD
		binder.forField(languagee).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);
//=======
		languagee.setRequired(true);
		//binder.forField(languagee).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);
		languagee.setValue(currentUser.getLanguage());
		
		languagee.getStyle().set("width", "400px");
//>>>>>>> branch 'development' of https://github.com/abahsuccess/APMIS-Project.git

//<<<<<<< HEAD
		languagee.getStyle().set("width", "400px");

//=======
//>>>>>>> branch 'development' of https://github.com/abahsuccess/APMIS-Project.git
		Div anch = new Div();
		anch.setClassName("anchDiv");
		pwdSecc.getStyle().set("margin-left", "20px");
		
		pwdSecc.add(openPasswordPopupButton, languagee, anch);

		Div actionss = new Div();

		Icon vadIc = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		vadIc.setId("fghf");
		vadIc.getStyle().set("color", "green !important");

		Icon vadIcc = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		vadIc.getStyle().set("color", "white");

		Button discard = new Button("Discard Changes", vadIc);
		discard.getStyle().set("margin-right", "20px");
		discard.getStyle().set("color", "green");
		discard.getStyle().set("background", "white");
		discard.getStyle().set("border", "1px solid green");

		Button savee = new Button("Save", vadIcc);
		savee.addClickListener(e -> {
			UserDto currentUserToSave = FacadeProvider.getUserFacade().getCurrentUser();
			if(languagee.getValue() != null) {
				
			currentUserToSave.setLanguage(languagee.getValue());
			FacadeProvider.getUserFacade().saveUser(currentUserToSave);
			
			Notification.show("Language setting saved successfully");
			} else {
				
				Notification.show("Choose prefered language: "+languagee.isInvalid());
			}
			
			
		});
		actionss.getStyle().set("margin", "20px");
		actionss.add(discard, savee);
		userentry.add(infooo, infoood, infoo, dataVieww, infodataa, fieldInfoo, fielddataVieww, security, pwdSecc,
				actionss);

		add(userentry);

//<<<<<<< HEAD
		// initial idea for a change of password
//		Dialog dialog = new Dialog();
//		dialog.setCloseOnEsc(false);
//		dialog.setCloseOnOutsideClick(false);
//
//		Label messageLabel = new Label("Update Password");
//		
//		TextField newPasswordField = new TextField("New Password");
//		TextField confirmNewPasswordField = new TextField("Confirm New Password");
//		
//		Label instructionLabel = new Label("Choose a new password for your account\r\n <br>"
//				+ "*Must be at least 8 characters\r\n <br>"
//				+ "*Must contain 1 Uppercase and 1 special character\r\n"
//				+ "");
//		instructionLabel.getElement().setProperty("innerHTML", instructionLabel.getText());
//		
//		Button closeButton = new Button("Close");
//		closeButton.addClickListener(event -> dialog.close());
//		
//		VerticalLayout layout = new VerticalLayout();
//		layout.add(messageLabel, newPasswordField);
//		layout.add(confirmNewPasswordField);
//		
//		dialog.add(layout, instructionLabel, closeButton);
//
//	//show popup	
//		Button openDialogButton = new Button("Change Password");
//		openDialogButton.addClickListener(event -> dialog.open());
//
//		add(openDialogButton);

		// trying out a new change password field

		passwordDialog.setCloseOnEsc(false);
		passwordDialog.setCloseOnOutsideClick(false);

		FormLayout formLayout = new FormLayout();

		PasswordField newPasswordField = new PasswordField("New Password");
		newPasswordField.setRevealButtonVisible(true);
		PasswordField confirmPasswordField = new PasswordField("Confirm Password");
		confirmPasswordField.setRevealButtonVisible(true);

		Label instructionLabel = new Label(
				"Choose a new password for your account\r\n <br>" + "*Must be at least 8 characters\r\n <br>"
						+ "*Must contain 1 Uppercase and 1 special character\r\n" + "");
		instructionLabel.getElement().setProperty("innerHTML", instructionLabel.getText());

		// setting action buttons for password change
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickListener(event -> passwordDialog.close());

		Button saveButton = new Button("Save");
		saveButton.addClickListener(event -> {
			// Perform password validation and saving logic here
			passwordDialog.close();
		});

		// setting css for the actions buttons
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.add(cancelButton, new Div(), saveButton); // Add an empty Div for spacing

		cancelButton.addClickListener(event -> passwordDialog.close());

		buttonLayout.getStyle().set("margin-top", "1em");

		passwordDialog.add(formLayout, buttonLayout);

		// setting layout for textfields
		VerticalLayout layout = new VerticalLayout();
		layout.add(newPasswordField);
		layout.add(confirmPasswordField);
		formLayout.add(newPasswordField, confirmPasswordField, instructionLabel);

//=======
//>>>>>>> branch 'development' of https://github.com/abahsuccess/APMIS-Project.git
	}


}