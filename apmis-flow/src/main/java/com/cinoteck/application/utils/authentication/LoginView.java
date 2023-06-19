package com.cinoteck.application.utils.authentication;

import com.cinoteck.application.LanguageSwitcher;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * UI content when the user is not logged in yet.
 */
@Route("")
@PageTitle("Login | APMIS")
//@CssImport("./styles/shared-styles.css")
public class LoginView extends FlexLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -597195523867494116L;

	private transient ResourceBundle resourceBundle = ResourceBundle.getBundle("MockDataWords",
			UI.getCurrent().getLocale() == Locale.US ? Locale.ROOT : UI.getCurrent().getLocale());

	private AccessControl accessControl;

	public LoginView() {
		accessControl = AccessControlFactory.getInstance().createAccessControl();
		buildUI();
	}

	private void buildUI() {
		setSizeFull();
		setClassName("login-screen");

		LoginForm loginForm = new LoginForm();
		loginForm.setI18n(createLoginI18n());
		loginForm.addLoginListener(this::login);
		loginForm.addForgotPasswordListener(event -> Notification.show(resourceBundle.getString("login_hint")));

		VerticalLayout loginInformation = new VerticalLayout();

		loginInformation.setSizeFull();
		loginInformation.setJustifyContentMode(JustifyContentMode.CENTER);
		loginInformation.setAlignItems(Alignment.CENTER);

		loginInformation.setClassName("login-information");
		Image imgApmis = new Image("images/apmislogo.png", "APMIS-LOGO");
		imgApmis.setClassName("apmis-login-logo");
		loginInformation.add(imgApmis);
		loginInformation.add(loginForm);
		loginInformation.add(new LanguageSwitcher(Locale.ENGLISH, new Locale("fa", "IR", "فارسی")));

		add(loginInformation);
	}

	private void login(LoginForm.LoginEvent event) {
		if (accessControl.signIn(event.getUsername(), event.getPassword())) {
			getUI().get().navigate("/dashboard");
		} else {
			event.getSource().setError(true);
		}
	}

	private LoginI18n createLoginI18n() {
		final LoginI18n i18n = LoginI18n.createDefault();

		i18n.setHeader(new LoginI18n.Header());
		i18n.getForm().setUsername(resourceBundle.getString("username"));
		i18n.getForm().setTitle(resourceBundle.getString("login"));
		i18n.getForm().setSubmit(resourceBundle.getString("login"));
		i18n.getForm().setPassword(resourceBundle.getString("password"));
		i18n.getForm().setForgotPassword(resourceBundle.getString("forgot_pass"));
		i18n.getErrorMessage().setTitle(resourceBundle.getString("login_error_title"));
		i18n.getErrorMessage().setMessage(resourceBundle.getString("login_error_msg"));
		return i18n;
	}
}

//
//import com.vaadin.flow.component.Html;
//import com.vaadin.flow.component.dependency.StyleSheet;
//import com.vaadin.flow.component.html.Div;
//import com.vaadin.flow.component.html.H1;
//import com.vaadin.flow.component.html.Image;
//import com.vaadin.flow.component.html.Paragraph;
//import com.vaadin.flow.component.login.LoginForm;
//import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//import com.vaadin.flow.router.BeforeEnterEvent;
//import com.vaadin.flow.router.BeforeEnterObserver;
//import com.vaadin.flow.router.PageTitle;
//import com.vaadin.flow.router.Route;
//
//@Route("") 
//@PageTitle("Login | APMIS")
//public class LoginView extends VerticalLayout{
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1551622595562424192L;
//
//	Div containerDiv = new Div();
//	
//	Div logoDiv = new Div();
//
//	
//	Image imgApmis = new Image("images/apmislogo.png", "APMIS-LOGO");
//
//	String content =  "<div class=" + "apmisDesc>" + "<p class=" + "apmisText>AFGHANISTAN POLIO MANAGEMENT INFORMATION SYSTEM.<p>"+"</div>";
//
//	Paragraph signInText = new Paragraph("Sign in to APMIS");
//	
//    Html html = new Html(content);
//    
//    private final LoginFormInput login = new LoginFormInput();
//    
//    
//	
//	public LoginView(){
//
//		addClassName("login-view");
//		addClassName("loginView");
//
//		setSizeFull(); 
//		setAlignItems(Alignment.CENTER);
//		setJustifyContentMode(JustifyContentMode.CENTER);
//
//		login.setClassName("loginfORM");
//		signInText.setId("signInText");
//
//		logoDiv.setClassName("logoContainer");
//		imgApmis.setWidth("159px");
//		imgApmis.setHeight("144px");
//		logoDiv.add(imgApmis);
//
//		containerDiv.setClassName("loginContainer");
//		containerDiv.add(logoDiv, html, signInText , login);
//		add(containerDiv); 	
//	}
//
//
//}
