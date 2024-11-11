package com.cinoteck.application.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.scheduling.annotation.Scheduled;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.UserProvider.HasUserProvider;
import com.cinoteck.application.ViewModelProviders;
import com.cinoteck.application.ViewModelProviders.HasViewModelProviders;
import com.cinoteck.application.components.appnav.AppNav;
import com.cinoteck.application.components.appnav.AppNavItem;
import com.cinoteck.application.messaging.MessagingView;
import com.cinoteck.application.messaging.UserMessageView;
import com.cinoteck.application.utils.authentication.AccessControl;
import com.cinoteck.application.utils.authentication.AccessControlFactory;
import com.cinoteck.application.views.about.AboutView;
import com.cinoteck.application.views.campaign.CampaignsView;
import com.cinoteck.application.views.campaigndata.CampaignDataView;
import com.cinoteck.application.views.configurations.ConfigurationsView;
import com.cinoteck.application.views.dashboard.AnalyticsDashboardView;
//import com.cinoteck.application.views.dashboard.NewDashboardView;
import com.cinoteck.application.views.myaccount.MyAccountView;
//import com.cinoteck.application.views.pivot.PivotTableView;
//import com.cinoteck.application.views.pivot.PivotView;
import com.cinoteck.application.views.reports.ReportView;
import com.cinoteck.application.views.support.SupportView;
import com.cinoteck.application.views.uiformbuilder.FormBuilderView;
import com.cinoteck.application.views.user.UserView;
//import com.cinoteck.application.views.user.UserView;
import com.cinoteck.application.views.user.UsersViewParent;
import com.cinoteck.application.views.useractivitysummary.UserActivitySummary;
import com.cinoteck.application.views.utils.IdleNotification;
//import com.cinoteck.application.views.utils.InactivityHandler;
import com.vaadin.flow.component.Direction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

/**
 * The main view is a top-level placeholder for other views. //password
 */

@NpmPackage(value = "lumo-css-framework", version = "^4.0.10")
@NpmPackage(value = "line-awesome", version = "1.3.0")
@NpmPackage(value = "@vaadin-component-factory/vcf-nav", version = "1.0.6")
@JavaScript(value = "https://code.jquery.com/jquery-3.6.4.min.js")
@StyleSheet("https://cdn.jsdelivr.net/npm/@vaadin/vaadin-lumo-styles@24.0.0/")

@CssImport(value = "/styles/mainapmis.css")

//@StyleSheet("https://demo.dashboardpack.com/architectui-html-free/main.css")
@JavaScript("https://code.jquery.com/jquery-3.6.3.min.js")
@JavaScript("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js")
@StyleSheet("https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.2.1/css/fontawesome.min.css")
@StyleSheet("https://cdnjs.cloudflare.com/ajax/libs/lato-font/3.0.0/css/lato-font.min.css")

@CssImport(value = "/styles/lato-font.css", themeFor = "vaadin-text-field")

public class MainLayout extends AppLayout implements HasUserProvider, HasViewModelProviders, BeforeEnterObserver {

	private H1 viewTitle;

	private final UserProvider userProvider = new UserProvider();
	private final ViewModelProviders viewModelProviders = new ViewModelProviders();
	boolean isToggleOpen = false;

	Image imgApmis = new Image();
	AppNav nav = new AppNav();
	String intendedRoute;
	final AccessControl accessControl = AccessControlFactory.getInstance().createAccessControl();
	private Button confirmButton;
	private Button cancelButton;
	Dialog dialog = new Dialog();
	Div aboutText = new Div();
	Button notification = new Button("Notification");
	IdleNotification idleNotification = new IdleNotification();
	private MessageCriteria messageCriteria;
	Date usersPreviousLoginDate;
	List<MessageDto> messageSize = new ArrayList<>();
//	private InactivityHandler inactivityHandler;
	private String currentRoute = UI.getCurrent().getInternals().getActiveViewLocation().getPath();
	private AppNavItem campaignNavItem;
	private AppNavItem about;

	public MainLayout() {
		if (I18nProperties.getUserLanguage() == null) {
			I18nProperties.setUserLanguage(Language.EN);
			FacadeProvider.getI18nFacade().setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
			FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		}

		rtlswitcher();
		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	private void addHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		toggle.getElement().setAttribute("aria-label", "Menu toggle");
		toggle.getStyle().set("color", "white");

		toggle.getStyle().set("z-index", "10000000");

		viewTitle = new H1();
		viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
		viewTitle.setId("pageHeader");
		HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.add(viewTitle);
		titleLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		titleLayout.setWidth("86%");
		titleLayout.getStyle().set("position", "relative");
		titleLayout.getStyle().set("left", "-4%");
		toggle.setId("togglecollapse");
		toggle.addClickListener(event -> {
			if (event.getSource() instanceof DrawerToggle) {
				DrawerToggle toggleButton = (DrawerToggle) event.getSource();

				if (isToggleOpen) {
					titleLayout.setWidth("100%");
				} else {
					titleLayout.setWidth("86%");
				}

				isToggleOpen = !isToggleOpen;
			}
		});

		idleNotification.setMessage(
				"Your session will expire in " + IdleNotification.MessageFormatting.SECS_TO_TIMEOUT + " seconds.");
		idleNotification.addExtendSessionButton("Extend session");
		idleNotification.addRedirectButton("Logout now", "logout");
		idleNotification.addCloseButton();
		idleNotification.setExtendSessionOnOutsideClick(true);
		idleNotification.setRedirectAtTimeoutUrl("./");

		UI.getCurrent().getSession().setAttribute(MainLayout.class, this);
		UI.getCurrent().add(idleNotification);

//		addToNavbar(true, toggle, titleLayout);
//
//		inactivityHandler = new InactivityHandler();
//		inactivityHandler.setWidth("0%");
//		addToNavbar(inactivityHandler);

		addToNavbar(true, toggle, titleLayout);
	}

//	public InactivityHandler getInactivityHandler() {
//        return inactivityHandler;
//    }

	public IdleNotification getIdleNotification() {
		return idleNotification;
	}

	private void addDrawerContent() {
		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			imgApmis = new Image("images/APMIS_Neoc_Banner.jpg", "APMIS-LOGO");
		} else {
			imgApmis = new Image("images/APMIS_Horizontal_Logo_1.jpg", "APMIS-LOGO");
		}
		imgApmis.setMaxWidth("100%");
		Scroller scroller = new Scroller(createNavigation());

		Header header = new Header(imgApmis);

		Span versionadd = new Span();

		String releaseDate = FacadeProvider.getInfoFacade().getApmisReleaseDate();

		String webAppVersionNumber = FacadeProvider.getInfoFacade().getWebAppVersionNumber();

		versionadd.getElement().setProperty("innerHTML",
				"<p>" + I18nProperties.getCaption(Captions.apmisVersionNumber) + ": " + webAppVersionNumber + "</p> <p>"
						+ I18nProperties.getCaption(Captions.releaseDate) + ": " + releaseDate + "</p>");
		versionadd.getStyle().set("background-color", "#0d6938");
		versionadd.getStyle().set("color", "#16c400");
		versionadd.getStyle().set("padding-left", "0.7rem");

		addToDrawer(header, scroller, versionadd);

		addToDrawer(createFooter());
	}

	private AppNav createNavigation() {
		// AppNav is not yet an official component.
		// For documentation, visit https://github.com/vaadin/vcf-nav#readme

		Button myButton = new Button();

		if (userProvider.hasUserRight(UserRight.CAMPAIGN_VIEW)) {
			campaignNavItem = new AppNavItem(I18nProperties.getCaption(Captions.campaignCampaignData),
					CampaignDataView.class, VaadinIcon.CLIPBOARD, "navitem");		
			nav.addItem(campaignNavItem);
		}

		if (userProvider.hasUserRight(UserRight.CAMPAIGN_VIEW)) {
			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.campaignAllCampaigns), CampaignsView.class,
					VaadinIcon.CLIPBOARD_TEXT, "navitem"));
		}

//		if (userProvider.hasUserRight(UserRight.DASHBOARD_CAMPAIGNS_ACCESS)) {
		// NOTE : On the long run if we would not be using an external link here
		// remeber that we can pass the subdomain url here to open in a new tab
		//
		AppNavItem newDashboardNavItem = new AppNavItem(I18nProperties.getCaption(Captions.mainMenuDashboard),
				VaadinIcon.GRID_BIG_O, "https://dashboard.afghanistan-apmis.com/", "navitem");

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")
				|| userProvider.getUser().getLanguage().toString().equals("Dari")) {
			newDashboardNavItem.getElement().getStyle().set("display", "math");
		}

		// Handle the middle-click and modify the context menu behavior
//		newDashboardNavItem.getElement().executeJs("const link = $0;" + "link.addEventListener('mousedown', (e) => {"
//				+ "  if (e.button === 1 || e.button === 2) {" + // Middle click or right click
//				"    e.preventDefault();" + "    window.open('https://dashboard.afghanistan-apmis.com/', '_blank');"
//				+ "  }" + "});" +
//				// Override the href just before the context menu appears
//				"link.addEventListener('contextmenu', (e) => {"
//				+ "  link.href = 'https://dashboard.afghanistan-apmis.com/';" + "});",
//				newDashboardNavItem.getElement());
		
		newDashboardNavItem.getElement().executeJs(
			    "const link = $0;" +
			    // Prevent right-click menu from showing
			    "link.addEventListener('contextmenu', (e) => { e.preventDefault(); });" +
			    // Handle middle-click (mouse button 1)
			    "link.addEventListener('mousedown', (e) => {" +
			    "  if (e.button === 1) {" +
			    "    e.preventDefault();" +
			    "    window.open('https://dashboard.afghanistan-apmis.com/', '_blank');" +
			    "  }" +
			    "});",
			    newDashboardNavItem.getElement()
			);

		nav.addItem(newDashboardNavItem);
//		}

		if (userProvider.hasUserRight(UserRight.CONFIGURATION_ACCESS)) {
			if (userProvider.getUser().getUsertype() == UserType.WHO_USER
					|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {
				nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuConfiguration),
						ConfigurationsView.class, VaadinIcon.GLOBE, "navitem"));
			}

		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			if (userProvider.hasUserRight(UserRight.USER_VIEW)) {

				nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuUsers), UserView.class,
						VaadinIcon.USERS, "navitem"));
			}
//			
//			if (userProvider.hasUserRight(UserRight.USER_VIEW)) {
//				nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuUsers), UserView.class,
//						VaadinIcon.USERS, "navitem"));
//			}
//			if ((permitted(UserRole.ADMIN) || permitted(UserRole.AREA_ADMIN_SUPERVISOR)
//					|| permitted(UserRole.ADMIN_SUPERVISOR) || permitted(UserRole.COMMUNITY_INFORMANT))) {

//			}
		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {

			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuReports), ReportView.class,
					VaadinIcon.CHART_LINE, "navitem"));
		}

		nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.userProfile), MyAccountView.class,
				VaadinIcon.USER, "navitem"));

		nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.support), SupportView.class, VaadinIcon.CHAT,
				"navitem"));
		
		about = new AppNavItem(I18nProperties.getCaption(Captions.about), AboutView.class, VaadinIcon.INFO_CIRCLE_O,
				"navitem");		
		nav.addItem(about);

		if ((userProvider.getUser().getUsertype() == UserType.WHO_USER)
				&& userProvider.hasUserRight(UserRight.FORM_BUILDER_ACCESS)) {
			nav.addItem(new AppNavItem("Form Manager", FormBuilderView.class, VaadinIcon.BUILDING, "navitem"));
		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER) {
			if (userProvider.hasUserRight(UserRight.USER_ACTIVITY_SUMMARYVIEW)) {
				nav.addItem(new AppNavItem(I18nProperties.getCaption("User Activity Summary"),
						UserActivitySummary.class, VaadinIcon.CHART_LINE, "navitem"));
			}
		}

		if ((userProvider.getUser().getUsertype() == UserType.WHO_USER)
				&& userProvider.hasUserRight(UserRight.PUSH_NOTIFICATION_ACCESS)) {
			nav.addItem(new AppNavItem("Notification", MessagingView.class, VaadinIcon.SERVER, "navitem"));
		}

//		if (!messageSize.isEmpty() || messageSize != null) {
//			if (userProvider.hasUserRight(UserRight.NON_ADMIN_ACCESS)) {
//				nav.addItem(new AppNavItem("Notification", VaadinIcon.SERVER, "notification", notification,
//						UserMessageView.class));
//				System.out.println("deyyyyyyyyyyyyyyyyyyyyyy");
//			}
//		} else {
		if (userProvider.hasUserRight(UserRight.NON_ADMIN_ACCESS)) {
			nav.addItem(
					new AppNavItem("Notification", VaadinIcon.SERVER, "navitem", notification, UserMessageView.class));
		}
//		}

		if (nav != null) {
			nav.addClassName("active");
		}

		return nav;
	}

	private void rtlswitcher() {
		if (userProvider.getUser().getLanguage() != null) {
			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();

			String userLanguage = userProvider.getUser().getLanguage().toString();
			if (userLanguage.equals("Pashto")) {
				UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
				nav.getStyle().set("width", "auto");
				nav.getStyle().set("text-align", "justify");
			} else if (userLanguage.equals("Dari")) {
				UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
				nav.getStyle().set("width", "auto");
				nav.getStyle().set("text-align", "justify");
			} else {

				UI.getCurrent().setDirection(Direction.LEFT_TO_RIGHT);
			}

		} else {
			I18nProperties.setUserLanguage(Language.EN);
			I18nProperties.getUserLanguage();

			userProvider.getUser().setLanguage(Language.EN);
			UI.getCurrent().setDirection(Direction.LEFT_TO_RIGHT);
		}
	}

	private Footer createFooter() {
		Footer layout = new Footer();
		Button logoutButton = new Button(I18nProperties.getCaption(Captions.actionLogout) + " | "
				+ userProvider.getUser().getFirstName() + " " + userProvider.getUser().getLastName());
		Icon logoutIcon = new Icon(VaadinIcon.SIGN_OUT_ALT);
		logoutIcon.getStyle().set("font-size", "18px");
		logoutButton.setIcon(logoutIcon);
		logoutButton.setId("lougoutButton");
		logoutButton.getStyle().set("font-size", "14px");
		logoutButton.getStyle().set("font-weight", "400");
		logoutButton.getStyle().set("width", "110%");
		logoutButton.getStyle().set("opacity", "none");
		logoutButton.getStyle().set("box-shadow", "none");
		logoutButton.getStyle().set("margin-left", "-14px");
		logoutButton.getStyle().set("margin-top", "0px");
		logoutButton.getStyle().set("height", "45px");
		logoutButton.getStyle().set("border-radius", "0px !important");
		logoutButton.getStyle().set("margin-bottom", "0px !important");

		logoutButton.getStyle().set("padding-left", "0px");

		VerticalLayout dialogHolderLayout = new VerticalLayout();

		Div apmisImageContainer = new Div();
		apmisImageContainer.getStyle().set("width", "100%");
		apmisImageContainer.getStyle().set("display", "flex");
		apmisImageContainer.getStyle().set("justify-content", "center");

		Image img = new Image("images/logout.png", "APMIS-LOGO");
		img.getStyle().set("max-height", "-webkit-fill-available");
		apmisImageContainer.remove(img);
		apmisImageContainer.add(img);

		Paragraph text = new Paragraph(I18nProperties.getString(Strings.areSureYouWantToLogout));
		text.getStyle().set("color", "black");
		text.getStyle().set("font-size", "24px");
		aboutText.getStyle().set("display", "flex");
		aboutText.getStyle().set("flex-direction", "column");
		aboutText.getStyle().set("align-items", "center");
		aboutText.remove(text);

		aboutText.add(text);

		Div logoutButtons = new Div();
		logoutButtons.getStyle().set("display", "flex");
		logoutButtons.getStyle().set("justify-content", "space-evenly");
		logoutButtons.getStyle().set("width", "100%");

		final AccessControl accessControl = AccessControlFactory.getInstance().createAccessControl();
		dialog = new Dialog();
		// TODO make this check the sesssion and invalidate it... it terms of Spring..
		// let use another method
		confirmButton = new Button(I18nProperties.getCaption(Captions.logoutnow), event -> {
			UI.getCurrent().getSession().close();
			accessControl.signOut(intendedRoute);

		});
		confirmButton.getStyle().set("width", "35%");

		cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), event -> {
			dialog.close();
		});
		cancelButton.getStyle().set("width", "35%");
		cancelButton.getStyle().set("background", "white");
		cancelButton.getStyle().set("color", "green");
		logoutButtons.add(confirmButton, cancelButton);
		dialogHolderLayout.remove(apmisImageContainer, aboutText, logoutButtons);
		dialogHolderLayout.add(apmisImageContainer, aboutText, logoutButtons);

		dialog.add(dialogHolderLayout);

		logoutButton.addClickListener(event -> {

			dialog.add(dialogHolderLayout);

			dialog.setCloseOnEsc(false);
			dialog.setCloseOnOutsideClick(false);
			dialog.open();
		});

		layout.add(logoutButton);

		return layout;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		rtlswitcher();
		viewTitle.setText(getCurrentPageTitle());
		currentRoute = UI.getCurrent().getInternals().getActiveViewLocation().getPath();
		
		/*
		 * The condition below handles setting backgroud color for default nav on users first login
		 */
		if (currentRoute.equalsIgnoreCase("campaigndata")) {
	        campaignNavItem.getElement().getStyle().set("background", "#F08F3E");
	        about.getElement().getStyle().remove("background");
	    } else if(currentRoute.equalsIgnoreCase("about")) {
	    	about.getElement().getStyle().set("background", "#F08F3E");
	    	campaignNavItem.getElement().getStyle().remove("background");
	    } else {	       
	        campaignNavItem.getElement().getStyle().remove("background");
	        about.getElement().getStyle().remove("background");
	    }
	}

	private String getCurrentPageTitle() {
		PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);

		System.out.println("|" + title.value().split("APMIS-")[1] + "|");
		String seg = "";

		if (title != null && title.value().contains("APMIS-")) {
			seg = title.value().split("APMIS-")[1];
		}

		return seg;
	}

//	@Override
	public void configurePage(InitialPageSettings settings) {
		settings.addLink("shortcut icon", "icons/icon.png");
		settings.addFavIcon("icon", "icons/icon.png", "192x192");
	}

	@Override
	public @NotNull ViewModelProviders getViewModelProviders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserProvider getUserProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		// Store the intended route in the UI instance before navigating to the login
		// page

		UI.getCurrent().getPage().executeJs("return document.location.pathname").then(String.class, pageTitle -> {
			if (pageTitle.contains("main/")) {
				intendedRoute = pageTitle.split("main/")[1];

			}
		});

	}

}