package com.cinoteck.application.views;

import javax.validation.constraints.NotNull;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.UserProvider.HasUserProvider;
import com.cinoteck.application.ViewModelProviders;
import com.cinoteck.application.ViewModelProviders.HasViewModelProviders;
import com.cinoteck.application.components.appnav.AppNav;
import com.cinoteck.application.components.appnav.AppNavItem;
import com.cinoteck.application.utils.authentication.AccessControl;
import com.cinoteck.application.utils.authentication.AccessControlFactory;
import com.cinoteck.application.views.about.AboutView;
import com.cinoteck.application.views.campaign.CampaignsView;
import com.cinoteck.application.views.campaigndata.CampaignDataView;
import com.cinoteck.application.views.configurations.ConfigurationsView;
import com.cinoteck.application.views.dashboard.AnalyticsDashboardView;
import com.cinoteck.application.views.dashboard.DashboardView;
import com.cinoteck.application.views.myaccount.MyAccountView;
import com.cinoteck.application.views.pivot.PivotView;
import com.cinoteck.application.views.reports.ReportView;
import com.cinoteck.application.views.support.SupportView;
//import com.cinoteck.application.views.test.TestView;
import com.cinoteck.application.views.user.UserView;
import com.cinoteck.application.views.utils.IdleNotification;
import com.vaadin.flow.component.Component;
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

import com.cinoteck.application.utils.authentication.AccessControlFactory;

import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
//import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.TextAlignment;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
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

@StyleSheet("https://demo.dashboardpack.com/architectui-html-free/main.css")
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

	public MainLayout() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);

		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}

		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());

		rtlswitcher();
		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
		userProvider.getUser().getUsertype();
		// UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
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

		IdleNotification idleNotification = new IdleNotification();
		idleNotification.setMessage(
				"Your session will expire in " + IdleNotification.MessageFormatting.SECS_TO_TIMEOUT + " seconds.");
		idleNotification.addExtendSessionButton("Extend session");
		idleNotification.addRedirectButton("Logout now", "logout");
		idleNotification.addCloseButton();
		idleNotification.setExtendSessionOnOutsideClick(true);
		idleNotification.setRedirectAtTimeoutUrl("logout");

//		UI.getCurrent().add(idleNotification);

		// System.out.println("++++++++++++++++++++++++:"+VaadinSession.getCurrent().getSession().getMaxInactiveInterval());

		addToNavbar(true, toggle, titleLayout);
	}

	private void addDrawerContent() {
		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			imgApmis = new Image("images/APMIS_Neoc_Banner.jpg", "APMIS-LOGO");
		} else {
			imgApmis = new Image("images/APMIS_Horizontal_Logo 1.jpg", "APMIS-LOGO");

		}
		imgApmis.setMaxWidth("100%");
		Scroller scroller = new Scroller(createNavigation());

		Header header = new Header(imgApmis);

		addToDrawer(header, scroller);

//		LanguageSwitcher languageSwitcher = new LanguageSwitcher(Locale.ENGLISH,
//                new Locale("fa","IR", "فارسی"));
//		
//		languageSwitcher.setClassName("vieLangiuageSwitcher");

		addToDrawer(createFooter());

		// addToDrawer(header, scroller, createFooter());
	}

	private AppNav createNavigation() {
		// AppNav is not yet an official component.
		// For documentation, visit https://github.com/vaadin/vcf-nav#readme

		Button myButton = new Button();

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuDashboard), DashboardView.class,
					VaadinIcon.GRID_BIG_O, "navitem"));
		}

		nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuAnalyticsDashboard), AnalyticsDashboardView.class,
				VaadinIcon.GRID_BIG_O, "navitem"));

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER
				|| userProvider.getUser().getUsertype() == UserType.COMMON_USER) {

			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.campaignCampaignData), CampaignDataView.class,
					VaadinIcon.CLIPBOARD, "navitem"));
		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {

			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.campaignAllCampaigns), CampaignsView.class,
					VaadinIcon.CLIPBOARD_TEXT, "navitem"));
		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {

			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuConfiguration),
					ConfigurationsView.class, VaadinIcon.COG_O, "navitem"));

		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {
//			if ((permitted(UserRole.ADMIN) || permitted(UserRole.AREA_ADMIN_SUPERVISOR)
//					|| permitted(UserRole.ADMIN_SUPERVISOR) || permitted(UserRole.COMMUNITY_INFORMANT))) {
			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuUsers), UserView.class,
					VaadinIcon.USERS, "navitem"));
//			}
		}

		if (userProvider.getUser().getUsertype() == UserType.WHO_USER
				|| userProvider.getUser().getUsertype() == UserType.EOC_USER) {

			nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuReports), ReportView.class,
					VaadinIcon.CHART_LINE, "navitem"));
		}

		nav.addItem(new AppNavItem("Pivot", PivotView.class, VaadinIcon.TREE_TABLE, "navitem"));
		// nav.addItem(new AppNavItem("Pivot", PivotTableView.class,
		// VaadinIcon.TREE_TABLE, "navitem"));
		nav.addItem(new AppNavItem("User Profile", MyAccountView.class, VaadinIcon.USER, "navitem"));
//		nav.addItem(new AppNavItem("Language", VaadinIcon.USER, "navitem",myButton));
		nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.mainMenuSupport), SupportView.class,
				VaadinIcon.CHAT, "navitem"));
		nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.about), AboutView.class, VaadinIcon.INFO_CIRCLE_O,
				"navitem"));

//		nav.addItem(new AppNavItem(I18nProperties.getCaption(Captions.actionLogout), LogoutView.class,
//				VaadinIcon.SIGN_OUT_ALT, "navitem"));

//		 Router router = new Router();
//
//	        router.getRegistry().add(new RouteEntry("/dashboard", DashboardView.class));
//	       

		if (nav != null) {
			nav.addClassName("active");
		}

		return nav;

	}

	private boolean permitted(UserRole userrole) {
		boolean check = false;
		if (userProvider.getUser().getUserRoles() != null) {
			for (UserRole vv : userProvider.getUser().getUserRoles()) {
				userrole = vv;
				check = true;
			}
			return check;
		} else {
			return check;
		}
	}

	private boolean permitted(UserType userType) {
		boolean check = false;

		if (userProvider.getUser().getUsertype() != null) {
			check = true;
			userType = userProvider.getUser().getUsertype();
			return check;
		} else {
			return check;
		}
	}

	private Button createPopup() {
		Button confirmButton;
		Button cancelButton;

		Dialog dialog = new Dialog();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		VerticalLayout dialogHolderLayout = new VerticalLayout();

		Div apmisImageContainer = new Div();
		apmisImageContainer.getStyle().set("width", "100%");
		apmisImageContainer.getStyle().set("display", "flex");
		apmisImageContainer.getStyle().set("justify-content", "center");

		Image img = new Image("images/logout.png", "APMIS-LOGO");
		img.getStyle().set("max-height", "-webkit-fill-available");

		apmisImageContainer.add(img);

		Div aboutText = new Div();

		Paragraph text = new Paragraph("You are attempting to log out of APMIS");
		Paragraph confirmationText = new Paragraph("Are you sure you want to logout?");

		text.getStyle().set("color", "black");
		text.getStyle().set("font-size", "24px");
		confirmationText.getStyle().set("color", "green");
		confirmationText.getStyle().set("font-size", "18px");

		aboutText.getStyle().set("display", "flex");
		aboutText.getStyle().set("flex-direction", "column");
		aboutText.getStyle().set("align-items", "center");
		aboutText.add(text, confirmationText);

		Div logoutButtons = new Div();
		logoutButtons.getStyle().set("display", "flex");
		logoutButtons.getStyle().set("justify-content", "space-evenly");
		logoutButtons.getStyle().set("width", "100%");

		confirmButton = new Button("Confirm", event -> {
//			confirmButton.getUI().ifPresent(ui -> ui.navigate(""));
		});
		confirmButton.getStyle().set("width", "35%");
		cancelButton = new Button("Cancel", event -> {
			dialog.close();
//			cancelButton.getUI().ifPresent(ui -> ui.navigate("dashboard"));
		});
		cancelButton.getStyle().set("width", "35%");
		cancelButton.getStyle().set("background", "white");
		cancelButton.getStyle().set("color", "green");
		logoutButtons.add(confirmButton, cancelButton);

		dialogHolderLayout.add(apmisImageContainer, aboutText, logoutButtons);
		dialog.add(dialogHolderLayout);
		return cancelButton;

//		add(dialog);
//		return dialog;
	}

	private void rtlswitcher() {
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		I18nProperties.getUserLanguage();
		if (userProvider.getUser().getLanguage().toString() != null) {
			String userLanguage = userProvider.getUser().getLanguage().toString();
			if (userLanguage.equals("Pashto")) {
				UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
			} else if (userLanguage.equals("Dari")) {
				UI.getCurrent().setDirection(Direction.RIGHT_TO_LEFT);
			} else {

				UI.getCurrent().setDirection(Direction.LEFT_TO_RIGHT);
			}

		}
	}

	private Tabs getTabs() {
		Tabs tabs = new Tabs();
		tabs.add(createTab(VaadinIcon.COG_O, "Dashboard", DashboardView.class));
		tabs.add(createTab(VaadinIcon.CLIPBOARD, "Campaign Data", CampaignDataView.class));
		tabs.add(createTab(VaadinIcon.CLIPBOARD_TEXT, "All Campaigns", CampaignsView.class));
		tabs.add(createTab(VaadinIcon.COG_O, "Configurations", ConfigurationsView.class));
		tabs.add(createTab(VaadinIcon.USERS, "Users", UserView.class));
		tabs.add(createTab(VaadinIcon.CHART, "Report", ReportView.class));
		tabs.add(createTab(VaadinIcon.USER, "User Profile", MyAccountView.class));
		tabs.add(createTab(VaadinIcon.INFO_CIRCLE_O, "About", AboutView.class));
		tabs.add(createTab(VaadinIcon.SIGN_OUT_ALT, "Sign-Out", SupportView.class));
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.addClassName("tabs");
		return tabs;
	}

	// TODO: Move the styles into CSS classes for a cleaner code
	private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> viewClass) {
		Icon icon = viewIcon.create();
		icon.getStyle().set("box-sizing", "border-box").set("margin-inline-end", "var(--lumo-space-m)").set("padding",
				"var(--lumo-space-xs)");

		RouterLink link = new RouterLink();
		link.setRoute(viewClass);

		// Create a VerticalLayout to stack the icon and the Span vertically
		VerticalLayout verticalLayout = new VerticalLayout(icon, new Span(viewName));
		verticalLayout.setSpacing(false);
		verticalLayout.setPadding(false);

		// Center the elements vertically and horizontally within the VerticalLayout
		verticalLayout.getStyle().set("display", "flex").set("flex-direction", "column").set("align-items", "center")
				.set("justify-content", "center").set("color", "white").set("font-weight", "normal")
				.set("margin", "8px 0px");

		link.add(verticalLayout);

		return new Tab(link);
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
		confirmButton = new Button(I18nProperties.getCaption(Captions.actionAccept), event -> {
			UI.getCurrent().getSession().close();
			accessControl.signOut(intendedRoute);

		});
		confirmButton.getStyle().set("width", "35%");

		cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), event -> {
			dialog.close();
//			dialog.remove(dialogHolderLayout);
			cancelButton.getUI().ifPresent(ui -> ui.navigate(intendedRoute));
		});
		cancelButton.getStyle().set("width", "35%");
		cancelButton.getStyle().set("background", "white");
		cancelButton.getStyle().set("color", "green");
		logoutButtons.add(confirmButton, cancelButton);
		dialogHolderLayout.remove(apmisImageContainer, aboutText, logoutButtons);
		dialogHolderLayout.add(apmisImageContainer, aboutText, logoutButtons);

		dialog.add(dialogHolderLayout);

		logoutButton.addClickListener(event -> {
			if (I18nProperties.getUserLanguage() == null) {

				I18nProperties.setUserLanguage(Language.EN);
			} else {

				I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
				I18nProperties.getUserLanguage();
			}
			FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
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
		viewTitle.setText(getCurrentPageTitle());
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
			if (pageTitle.contains("flow/")) {
				intendedRoute = pageTitle.split("flow/")[1];
				System.out.println(
						"____LOOOOOOGGGGOOOUUUTt________/////______________////////_____________________________________: "
								+ String.format("Page title: '%s'", pageTitle.split("flow/")[1]));

				// JsonDatabase sdf = new JsonDatabase(pageTitle.split("flow/")[1]);

			}
//			Notification.show(String.format("Page title: '%s'", pageTitle));
		});

//		 VaadinServletRequest request = (VaadinServletRequest) VaadinService.getCurrentRequest();

	}

}