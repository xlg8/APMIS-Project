package com.cinoteck.application.utils.authentication;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.utils.IdleNotification;
import com.google.api.client.util.DateTime;
import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.server.Page;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.server.Page;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * UI content when the user is not logged in yet.
 */
@Route("")
@PageTitle("APMIS-Login")
//@CssImport("./styles/shared-styles.css")
public class LoginView extends FlexLayout implements BeforeEnterObserver {
	private String intendedRoute;

	private final UserProvider userProvider = new UserProvider();

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
		loginForm.addForgotPasswordListener(event -> {
			 com.vaadin.flow.component.page.Page page = UI.getCurrent().getPage();
			    page.executeJs("window.location.href = 'http://afghanistan-apmis.com/forgot-password'");
			});
		
//		Router router = RouteConfiguration.forSessionScope().getRouter();
//
//		String url = VaadinServletService.getCurrentServletRequest().getRequestURI();
//		boolean isStagingInUrl = url.contains("localhost");
		
		UI.getCurrent().getPage().executeJs("return window.location.href;").then(String.class, url -> {
		    if (url != null) {
		        boolean isStagingInUrl = url.contains("staging");
		        boolean isTestInUrl = url.contains("test");


		        if (isStagingInUrl || isTestInUrl) {
				    // The string "staging" is found in the URL
					loginForm.setForgotPasswordButtonVisible(false);
				} else {
				    // The string "staging" is not found in the URL
					loginForm.setForgotPasswordButtonVisible(true);

				}

		    }
		});
		

		VerticalLayout loginInformation = new VerticalLayout();

		loginInformation.setSizeFull();
		loginInformation.setJustifyContentMode(JustifyContentMode.CENTER);
		loginInformation.setAlignItems(Alignment.CENTER);
//Todo make the image include the text to avoid the problems of wrapping 
		loginInformation.setClassName("login-information");
		Image imgApmis = new Image("images/apmisheaderbanner.png", "APMIS-LOGO");
		imgApmis.setClassName("apmis-login-logo");
		Div imageDiv = new Div();
		imageDiv.getStyle().set("display", "flex").set("justify-content", "center").set("width", "240px");
		imageDiv.add(imgApmis);

		VerticalLayout loginFormCarrier = new VerticalLayout();
		loginFormCarrier.add(imageDiv, loginForm);
		loginFormCarrier.setClassName("login-form-carrier");
		
		loginInformation.add(loginFormCarrier);
		add(loginInformation);
	}

	private void login(LoginForm.LoginEvent event) {
		WrappedSession httpSession = VaadinSession.getCurrent().getSession();

		if (httpSession.getAttribute("intendedRoute") != null) {
			intendedRoute = (String) httpSession.getAttribute("intendedRoute");
		}

		if (accessControl.signIn(event.getUsername(), event.getPassword())) {


			VaadinSession.getCurrent().getSession().setMaxInactiveInterval((int) TimeUnit.MINUTES.toSeconds(20));

			if (intendedRoute != null) {
//				loginNavigationControl();
				if (userProvider.getUser().getUsertype() == UserType.COMMON_USER && intendedRoute.equals("dashboard")) {
					getUI().get().navigate("/campaigndata");
				}
				if (intendedRoute.equals("logout")) {
					getUI().get().navigate("/dashboard");
				} else if (intendedRoute.equals("/")) {
					getUI().get().navigate("/dashboard");
				} else {
					getUI().get().navigate("/" + intendedRoute);
				}
				UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
				userActivitySummaryDto.setActionModule("Login");
				userActivitySummaryDto.setAction("User Logged In");
				userActivitySummaryDto.setCreatingUser_string(event.getUsername());

				FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);
				System.out.println("Kickkkkkkkkkkkkkkkkkkkkkkedddddddddddd111111111111111");
			} else {

				if (userProvider.getUser().getUsertype() == UserType.COMMON_USER) {
					getUI().get().navigate("/campaigndata");
				} else {
					getUI().get().navigate("/dashboard");
				}
				
				UserActivitySummaryDto userActivitySummaryDto = new UserActivitySummaryDto();
				userActivitySummaryDto.setActionModule("Login");
				userActivitySummaryDto.setAction("User Logged In");
				userActivitySummaryDto.setCreatingUser_string(event.getUsername());
				FacadeProvider.getUserFacade().saveUserActivitySummary(userActivitySummaryDto);
				System.out.println("Kickkkkkkkkkkkkkkkkkkkkkkedddddddddddd2222222222222222222");

			}

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

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// Store the intended route in the UI instance before navigating to the login
		// page
		UI.getCurrent().getPage().executeJs("return document.location.pathname").then(String.class, pageTitle -> {
			if (pageTitle.contains("main/")) {
				
				if(pageTitle.split("main/").length > 0)
				intendedRoute = pageTitle.split("main/")[1];
			}
		});


	}
}
