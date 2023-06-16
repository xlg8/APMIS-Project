package com.cinoteck.application.views.myaccount;

import com.cinoteck.application.views.MainLayout;
//import com.cinoteck.application.views.admin.TestView2;
import com.cinoteck.application.views.admin.TestView3;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
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

//
//        Image img = new Image("images/empty-plant.png", "placeholder plant");
//        img.setWidth("200px");
//        add(img);
//
//        add(new H2("This place intentionally left empty"));
//        add(new Paragraph("It’s a place where you can grow your own UI 🤗"));
//
//        setSizeFull();
//        setJustifyContentMode(JustifyContentMode.CENTER);
//        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
//        getStyle().set("text-align", "center");
//
//
//
//    	Div userSet = new Div();
//    	userSet.setClassName("subtabBackground");
//    	 Tab userSettingsTab = new Tab("User settings");
//         Tab pwdSec = new Tab("Password & Security");
//
//         Tabs tabs = new Tabs(userSettingsTab, pwdSec);
//
//         userSet.add(tabs);
//
//         Div personalInfo = new Div();
//         Paragraph info =new Paragraph("Personal Information");
//
//         HorizontalLayout firstname  = new HorizontalLayout();
//         firstname.add("First Name");
//         firstname.getElement().appendChild(ElementFactory.createBr());
//         firstname.add("Jibril");
//
//         HorizontalLayout lastname  = new HorizontalLayout();
//         lastname.add("Last Name");
//         lastname.getElement().appendChild(ElementFactory.createBr());
//         lastname.add("Joanna");
//
//
//         HorizontalLayout emailAddress  = new HorizontalLayout();
//         emailAddress.add("Email Address");
//         emailAddress.getElement().appendChild(ElementFactory.createBr());
//         emailAddress.add("example@gmail.com");
//
//         HorizontalLayout phoneNumber  = new HorizontalLayout();
//         phoneNumber.add("Phone number");
//         phoneNumber.getElement().appendChild(ElementFactory.createBr());
//         phoneNumber.add("+93 0000 0000 000");
//
//         HorizontalLayout position  = new HorizontalLayout();
//         position.add("Position");
//         position.getElement().appendChild(ElementFactory.createBr());
//         position.add("Admin");
//
//         HorizontalLayout address  = new HorizontalLayout();
//         address.add("Address");
//         address.getElement().appendChild(ElementFactory.createBr());
//         address.add("Kabul, Afghanistan.");
//
//
//         FormLayout dataView = new FormLayout();
//         dataView.add( firstname,lastname, emailAddress, phoneNumber, position, address);
//
//
//
//
//
//         Div fieldInfo = new Div();
//         Paragraph infodata =new Paragraph("Field Information");
//
//         HorizontalLayout province  = new HorizontalLayout();
//         province.add("Province");
//         province.getElement().appendChild(ElementFactory.createBr());
//         province.add("Badhakshan");
//
//         HorizontalLayout region  = new HorizontalLayout();
//         region.add("Region");
//         region.getElement().appendChild(ElementFactory.createBr());
//         region.add("Badhakshan");
//
//
//         HorizontalLayout district  = new HorizontalLayout();
//         district.add("District");
//         district.getElement().appendChild(ElementFactory.createBr());
//         district.add("Yawan");
//
//         HorizontalLayout cluster  = new HorizontalLayout();
//         cluster.add("Cluster");
//         cluster.getElement().appendChild(ElementFactory.createBr());
//         cluster.add("Eefch");
//
//         HorizontalLayout street  = new HorizontalLayout();
//         street.add("Street");
//         street.getElement().appendChild(ElementFactory.createBr());
//         street.add("Example Street");
//
//         HorizontalLayout houseNum  = new HorizontalLayout();
//         houseNum.add("House Number");
//         houseNum.getElement().appendChild(ElementFactory.createBr());
//         houseNum.add("35");
//
//
//         HorizontalLayout addInfo  = new HorizontalLayout();
//         addInfo.add("Additional information");
//         addInfo.getElement().appendChild(ElementFactory.createBr());
//         addInfo.add("None");
//
//         HorizontalLayout postalCode  = new HorizontalLayout();
//         postalCode.add("Postal code");
//         postalCode.getElement().appendChild(ElementFactory.createBr());
//         postalCode.add("1004");
//
//         HorizontalLayout city  = new HorizontalLayout();
//         city.add("City");
//         city.getElement().appendChild(ElementFactory.createBr());
//         city.add("Badhakshan");
//
//
//         HorizontalLayout areaType  = new HorizontalLayout();
//         areaType.add("Area type (Urban/Rural)");
//         areaType.getElement().appendChild(ElementFactory.createBr());
//         areaType.add("Urban");
//
//         HorizontalLayout contacPerson  = new HorizontalLayout();
//         contacPerson.add("Cluster contact person");
//         contacPerson.getElement().appendChild(ElementFactory.createBr());
//         contacPerson.add("Admin");
//
//         HorizontalLayout gpslat  = new HorizontalLayout();
//         gpslat.add("GPS Latittude");
//         gpslat.getElement().appendChild(ElementFactory.createBr());
//         gpslat.add("0.44");
//
//         HorizontalLayout gpslong  = new HorizontalLayout();
//         gpslong.add("GPS Longitude");
//         gpslong.getElement().appendChild(ElementFactory.createBr());
//         gpslong.add("3.91");
//
//         HorizontalLayout gpsaccuracy  = new HorizontalLayout();
//         gpsaccuracy.add("GPS Accuracy in M");
//         gpsaccuracy.getElement().appendChild(ElementFactory.createBr());
//         gpsaccuracy.add("5");
//
//
//         FormLayout gpsdataView = new FormLayout();
//         gpsdataView.setResponsiveSteps(
//        	        // Use one column by default
//        	        new ResponsiveStep("0", 1),
//        	        // Use two columns, if the layout's width exceeds 320px
//        	        new ResponsiveStep("320px", 2),
//        	        // Use three columns, if the layout's width exceeds 500px
//        	        new ResponsiveStep("500px", 3));
//         gpsdataView.add( gpslat,gpslong,gpsaccuracy);
//
//
//
//         FormLayout fielddataView = new FormLayout();
//         fielddataView.setResponsiveSteps(
//        	        // Use one column by default
//        	        new ResponsiveStep("0", 1),
//        	        // Use two columns, if the layout's width exceeds 320px
//        	        new ResponsiveStep("320px", 2),
//        	        // Use three columns, if the layout's width exceeds 500px
//        	        new ResponsiveStep("500px", 3));
//         fielddataView.add( province,region, district, cluster, street, houseNum, addInfo, postalCode, city, areaType, contacPerson,gpsdataView );
//
//
//         Button editProfile = new Button("Edit Profile");
//         add(userSet,info,  dataView, infodata, fieldInfo, fielddataView , editProfile) ;
//
//
//
//
//
//
//
//
//
		Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);
		//Binder<LocationDto> binderLocale = new BeanValidationBinder<>(LocationDto.class);
		List<AreaReferenceDto> regionss;
		List<RegionReferenceDto> provincess;
		List<DistrictReferenceDto> districtss;
		List<CommunityReferenceDto> communitiess;

		UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();
		//currentUser.getFirstName();

		Div userentry = new Div();
		userentry.setClassName("subtabBackground");

		Paragraph infooo = new Paragraph("Username");
		infooo.getStyle().set("color", "green");
		infooo.getStyle().set("font-size", "20px");
		infooo.getStyle().set("font-weight", "600");
		infooo.getStyle().set("margin", "20px");

		Paragraph infoood = new Paragraph(currentUser.getUserName());
		infoood.getStyle().set("margin", "20px");

		Div personalInfoo = new Div();
		Paragraph infoo = new Paragraph("Personal Information");
		infoo.getStyle().set("color", "green");
		infoo.getStyle().set("font-size", "20px");
		infoo.getStyle().set("font-weight", "600");
		infoo.getStyle().set("margin", "20px");

		TextField firstnamee = new TextField("");
		firstnamee.setLabel("First Name");
		binder.forField(firstnamee).asRequired("First Name is Required").bind(UserDto::getFirstName, UserDto::setFirstName);

		TextField lastnamee = new TextField("");
		lastnamee.setLabel("Last Name");
		binder.forField(lastnamee).asRequired("Last Name is Required").bind(UserDto::getLastName, UserDto::setLastName);

		EmailField emailAddresss = new EmailField();
		emailAddresss.setLabel("Email address");
		binder.forField(emailAddresss).asRequired("Email Address is Required").bind(UserDto::getUserEmail, UserDto::setUserEmail);

		TextField phoneNumberr = new TextField();
		phoneNumberr.setLabel("Phone number");
		binder.forField(phoneNumberr).withValidator(e -> e.length() >= 10, "Enter a valid Phone Number")
		.bind(UserDto::getPhone, UserDto::setPhone);

//		Select<String> positionn = new Select<>();
//		positionn.setLabel("Position");
//		positionn.setItems("");
//		positionn.setValue("");

		TextField positionn = new TextField();
		positionn.setLabel("Position");
		binder.forField(positionn).bind(UserDto::getUserPosition, UserDto::setUserPosition);

//TODO:: add a model for address in userDto
		TextField addresss = new TextField();
		addresss.setLabel("Address");
		// binder.forField(addresss).bind(UserDto::getAddress,UserDto::setAddress);

		FormLayout dataVieww = new FormLayout();
		dataVieww.add(firstnamee, lastnamee, emailAddresss, phoneNumberr, positionn, addresss);
		dataVieww.getStyle().set("margin", "20px");

		Div fieldInfoo = new Div();
		Paragraph infodataa = new Paragraph("Field Information");
		infodataa.getStyle().set("color", "green");
		infodataa.getStyle().set("font-size", "20px");
		infodataa.getStyle().set("font-weight", "600");
		infodataa.getStyle().set("margin", "20px");

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
		//provincee.setLabel("Province");
		binder.forField(provincee).bind(UserDto::getRegion, UserDto::setRegion);
		provincee.setItemLabelGenerator(RegionReferenceDto::getCaption);
		// provincee.setItems("", "Province", "Province", "Province", "Province",
		// "Province");
//		provincee.setValue("");

		// TODO: find how flow pulls data into select
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

//          TextField gpslatt = new TextField();
//          gpslatt.setLabel("GPS Longitude");
//
//
//
//
//
//
//          TextField gpslongg = new TextField();
//          gpslongg.setLabel("GPS Longitude");
//
//
//
//
//          TextField gpsaccuracyy = new TextField();
//          gpsaccuracyy.setLabel("GPS Accuracy in M");
//

//
//          FormLayout gpsdataVieww = new FormLayout();
//          gpsdataVieww.setResponsiveSteps(
//         	        // Use one column by default
//         	        new ResponsiveStep("0", 1),
//         	        // Use two columns, if the layout's width exceeds 320px
//         	        new ResponsiveStep("320px", 2),
//         	        // Use three columns, if the layout's width exceeds 500px
//         	        new ResponsiveStep("500px", 3));
//          gpsdataVieww.add( gpslatt,gpslongg,gpsaccuracyy);

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
		fielddataVieww.getStyle().set("margin", "20px");

		Paragraph security = new Paragraph("Password & Security");
		security.getStyle().set("color", "green");
		security.getStyle().set("font-size", "20px");
		security.getStyle().set("font-weight", "600");
		security.getStyle().set("margin", "20px");

		Div pwdSecc = new Div();
		pwdSecc.setClassName("superDiv");
		
//		Button changePasswordButton = new Button();
//		changePasswordButton.setIcon(VaadinIcons.UNLOCK);
//		changePasswordButton.setCaption("Change Password");
//		InternalPasswordChangeComponent PasswordChangeConfirmationComponent = getPasswordChangeConfirmationComponent(
//				this.getField(UserDto.USER_NAME).getValue().toString());
//
//		changePasswordButton.addClickListener(e -> {
//			Window popupWindow = VaadinUiUtil.showPopupWindow(PasswordChangeConfirmationComponent); // Password Changed
//																									// Successfully
//			PasswordChangeConfirmationComponent.addDoneListener(() -> popupWindow.close());
//			PasswordChangeConfirmationComponent.getCancelButton().addClickListener(new ClickListener() {
//
//				private static final long serialVersionUID = 1L;
//
//				@Override
//				public void buttonClick(ClickEvent event) {
//					popupWindow.close();
//				}
//			});
//			popupWindow.setCaption(I18nProperties.getString(Strings.headingUpdatePassword));
//		});
//		changePasswordButton.addStyleName(ValoTheme.BUTTON_LINK);
//		getContent().addComponent(changePasswordButton, PASSWORD_BUTTON);

		Div lang = new Div();
		lang.setClassName("langDiv");
		//Select<String> language = new Select<>();
		ComboBox<Language> languagee = new ComboBox<>("Language");

		//language.setLabel("Language");
//		language.setItems("", "English", "Dari", "Pashto");
//		language.setValue("");
		languagee.setItemLabelGenerator(Language::toString);
		languagee.setItems(Language.getAssignableLanguages());
		binder.forField(languagee).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		languagee.getStyle().set("width", "350%");
		lang.getStyle().set("margin", "20px");

		lang.add(languagee);
		Div anch = new Div();
		anch.setClassName("anchDiv");
		Anchor changePwd = new Anchor();
		changePwd.setText("Change Password");
		anch.add(changePwd);

		pwdSecc.add(lang, anch);

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

		actionss.getStyle().set("margin", "20px");
		actionss.add(discard, savee);
		add(userentry, infooo, infoood, infoo, dataVieww, infodataa, fieldInfoo, fielddataVieww, security, pwdSecc,
				actionss);
	}
// Tabs tabs = createTabs();
// Div contentContainer = new Div();
//        contentContainer.setWidth("100%");
// add(tabs, contentContainer);
//
//        tabs.addSelectedChangeListener(e -> {
//  contentContainer.removeAll();
//  contentContainer.add(tabComponentMap.get(e.getSelectedTab()));
// });
// // Set initial content
//        contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
//}
//
// private Tabs createTabs() {
//  tabComponentMap.put(new Tab("Show some text"), new UserReadView());
//  tabComponentMap.put(new Tab("Show a Combo Box"), new TestView2());
//  tabComponentMap.put(new Tab("Show a button"), new TestView3());
//  return new Tabs(tabComponentMap.keySet().toArray(new Tab[]{}));
// }

}