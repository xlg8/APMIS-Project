/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui;

import static de.symeda.sormas.ui.UiUtil.permitted;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.criteria.BaseCriteria;
import de.symeda.sormas.ui.campaign.AbstractCampaignView;
import de.symeda.sormas.ui.campaign.campaigndata.CampaignDataView;
import de.symeda.sormas.ui.campaign.campaigns.CampaignsView;
import de.symeda.sormas.ui.configuration.AbstractConfigurationView;
import de.symeda.sormas.ui.configuration.infrastructure.CommunitiesView;

import de.symeda.sormas.ui.configuration.infrastructure.DistrictsView;

import de.symeda.sormas.ui.configuration.infrastructure.RegionsView;

import de.symeda.sormas.ui.dashboard.AbstractDashboardView;
import de.symeda.sormas.ui.dashboard.campaigns.CampaignDashboardView;
import de.symeda.sormas.ui.report.CampaignReportView;
import de.symeda.sormas.ui.user.UserSettingsForm;
import de.symeda.sormas.ui.user.UsersView;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.VaadinUiUtil;

/**
 * Content of the UI when the user is logged in.
 */
@SuppressWarnings("serial")
//@StyleSheet("vaadin://map/trlImplementation.css")
public class MainScreen extends HorizontalLayout {

	// Add new views to this set to make sure that the right error page is shown
	private static final Set<String> KNOWN_VIEWS = initKnownViews();

	private final Menu menu;
	
	public MainScreen(SormasUI ui) {

		CssLayout viewContainer = new CssLayout();
		viewContainer.setSizeFull();
		viewContainer.addStyleName("sormas-content");
		viewContainer.setId("sormas-oya");

		final Navigator navigator = new Navigator(ui, viewContainer);
		navigator.setErrorProvider(new ViewProvider() {

			@Override
			public String getViewName(String viewAndParameters) {
				return viewAndParameters;
			}

			@Override // screen.css
			public View getView(String viewName) {
				try {
					Class<? extends View> errViewType;
					if (KNOWN_VIEWS.contains(viewName)) {
						errViewType = AccessDeniedView.class;
					} else {
						errViewType = ErrorView.class;
					}
					return errViewType.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		menu = new Menu(navigator);
		ControllerProvider.getDashboardController().registerViews(navigator);
		if (permitted(FeatureType.CAMPAIGNS, UserRight.DASHBOARD_CAMPAIGNS_ACCESS)) {
			menu.addView(CampaignDashboardView.class, AbstractDashboardView.ROOT_VIEW_NAME,
					I18nProperties.getCaption(Captions.mainMenuDashboard), VaadinIcons.GRID_SMALL_O);
		}
		
//		if (permitted(FeatureType.AGGREGATE_REPORTING, UserRight.AGGREGATE_REPORT_VIEW)) {
//			menu.addView(AggregateReportsView.class, AggregateReportsView.VIEW_NAME,
//					I18nProperties.getCaption(Captions.mainMenuAggregateReports), VaadinIcons.GRID_SMALL);
//		}

		if (permitted(FeatureType.CAMPAIGNS, UserRight.CAMPAIGN_VIEW)) {
			AbstractCampaignView.registerViews(navigator);
			menu.addView(CampaignDataView.class, AbstractCampaignView.ROOT_VIEW_NAME,
					I18nProperties.getCaption(Captions.mainMenuCampaigns), VaadinIcons.CLIPBOARD_CHECK);
			menu.addView(CampaignReportView.class, CampaignReportView.VIEW_NAME, I18nProperties.getCaption("Report"),
					VaadinIcons.CHART);

		}

//		if (permitted(FeatureType.WEEKLY_REPORTING, UserRight.WEEKLYREPORT_VIEW)) {
//			menu.addView(ReportsView.class, ReportsView.VIEW_NAME, I18nProperties.getCaption(Captions.mainMenuReports),
//					VaadinIcons.FILE_TEXT);
//		}

		if (permitted(UserRight.CONFIGURATION_ACCESS)) {
			if ((permitted(UserType.WHO_USER) || permitted(UserType.EOC_USER))) {
				AbstractConfigurationView.registerViews(navigator);
				menu.addView(
						RegionsView.class,
						AbstractConfigurationView.ROOT_VIEW_NAME,
						I18nProperties.getCaption(Captions.mainMenuConfiguration), VaadinIcons.COG_O);
			}
		}

		if ((permitted(UserRole.ADMIN) || permitted(UserRole.AREA_ADMIN_SUPERVISOR)
				|| permitted(UserRole.ADMIN_SUPERVISOR) || permitted(UserRole.COMMUNITY_INFORMANT))) {
			menu.addView(UsersView.class, UsersView.VIEW_NAME, I18nProperties.getCaption(Captions.mainMenuUsers),
					VaadinIcons.USERS);
		}

		menu.createViewButtonx(Captions.actionSettings, I18nProperties.getCaption(Captions.language),
				VaadinIcons.GLOBE_WIRE);

		menu.createAccountViewButton(Captions.actionSettings, I18nProperties.getCaption(Captions.Account),
				VaadinIcons.CLIPBOARD_USER);

		// menu.addView(LanguageView.class, LanguageView.VIEW_NAME, "DashboardTest",
		// VaadinIcons.GLOBE_WIRE);

		menu.addView(AboutView.class, AboutView.VIEW_NAME, I18nProperties.getCaption(Captions.mainMenuAbout),
				VaadinIcons.INFO_CIRCLE_O);

		// menu.addView(CampaignGisView.class, CampaignGisView.VIEW_NAME,
		// I18nProperties.getCaption("GIS"),
		// VaadinIcons.MAP_MARKER);

		menu.addView(LogoutView.class, LogoutView.VIEW_NAME,
				I18nProperties.getCaption(Captions.actionLogout) + " | " + UserProvider.getCurrent().getUserName(),
				VaadinIcons.POWER_OFF);

		menu.addViewx(LogoutTimeoutView.class, LogoutTimeoutView.VIEW_NAME);

		/*
		 * //trying to include a javascript from this method MainScreenAbstract dd = new
		 * MainScreenAbstract(); menu.addComponent(dd);
		 * 
		 * UI.getCurrent().setResponsive(true);
		 * 
		 * JavaScript.getCurrent().execute("setTimeout(myStopFunction, 4000);" +
		 * "function myStopFunction() {" + "console.log('adding transalator div');" +
		 * "		const h2 = document.getElementById('dashboard_logout');\n" +
		 * "		let html = \"<div id='google_translate_element'></div>\";\n" +
		 * "		h2.insertAdjacentHTML('afterend', html);" + "console.log('done');" +
		 * "}"
		 * 
		 * 
		 * + "" + "setTimeout(googleTranslateElementInitLer, 5000);" +
		 * "function googleTranslateElementInitLer(){" + "googleTranslateElementInit_()"
		 * + "}" + " function googleTranslateElementInit_() { \n" +
		 * "            new google.translate.TranslateElement({pageLanguage: 'en', layout: google.translate.TranslateElement.InlineLayout.SIMPLE}, 'google_translate_element'"
		 * + "            ); \n" + "        } ");
		 * 
		 * 
		 */
		navigator.addViewChangeListener(viewChangeListener);

		// Add GDPR window
		// possible to desactivate it with check
		UserDto user = UserProvider.getCurrent().getUser();
		if (FacadeProvider.getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.GDPR_CONSENT_POPUP)
				&& !user.isHasConsentedToGdpr()) {
			Window subWindowGdpR = new Window(
					I18nProperties.getPrefixCaption(UserDto.I18N_PREFIX, UserDto.HAS_CONSENTED_TO_GDPR));
			VerticalLayout subContentGdpr = new VerticalLayout();
			subWindowGdpR.setContent(subContentGdpr);
			subWindowGdpR.center();
			subWindowGdpR.setWidth("40%");
			subWindowGdpR.setModal(true);
			subWindowGdpR.setClosable(true);

			Label textGdpr = new Label();
			textGdpr.setWidth("80%");
			textGdpr.setSizeFull();
			textGdpr.setValue(I18nProperties.getString(Strings.messageGdpr));
			subContentGdpr.addComponent(textGdpr);

			CheckBox checkBoxGdpr = new CheckBox(I18nProperties.getString(Strings.messageGdprCheck));

			HorizontalLayout buttonLayout = new HorizontalLayout();
			buttonLayout.setMargin(false);
			buttonLayout.setSpacing(true);
			buttonLayout.setWidth(100, Unit.PERCENTAGE);
			Button buttonGdpr = ButtonHelper.createButton(I18nProperties.getCaption(Captions.actionConfirm), event -> {
				if (checkBoxGdpr.getValue()) {
					user.setHasConsentedToGdpr(true);
					FacadeProvider.getUserFacade().saveUser(user);
					navigator.getUI().removeWindow(subWindowGdpR);
				}
				navigator.getUI().removeWindow(subWindowGdpR);
			}, ValoTheme.BUTTON_PRIMARY);
			buttonLayout.addComponent(buttonGdpr);
			subContentGdpr.addComponent(checkBoxGdpr);
			subContentGdpr.addComponent(buttonLayout);
			buttonLayout.setComponentAlignment(buttonGdpr, Alignment.BOTTOM_RIGHT);
			buttonLayout.setExpandRatio(buttonGdpr, 0);
			navigator.getUI().addWindow(subWindowGdpR);
		}

		ui.setNavigator(navigator);

		addComponent(menu);
		addComponent(viewContainer);

		// Add some css for printable version
		menu.addStyleName(CssStyles.PRINT_MENU);
		viewContainer.addStyleName(CssStyles.PRINT_VIEW_CONTAINER);
		addStyleName(CssStyles.PRINT_CONTAINER);

		setExpandRatio(viewContainer, 1);
		setSpacing(false);
		setMargin(false);
		setSizeFull();
		
//		Page.getCurrent().getJavaScript().execute("\n" + "var timeleft = 1800;\n" + "\n"
//				+ "        function resetTimer() {\n"
//				+ "            return timeleft = (1800 - timeleft) + timeleft; //reset back to 35 seconds \n"
//				+ "        }\n" + "\n" + "        function setupReset() {\n"
//				+ "            document.addEventListener(\"mousedown\", resetTimer);\n"
//				+ "            document.addEventListener(\"keypress\", resetTimer);\n"
//				+ "            document.addEventListener(\"touchmove\", resetTimer);\n"
//				+ "            document.addEventListener(\"onscroll\", resetTimer);\n" + "            \n"
//				+ "        }\n" + "\n" + "        var pageTimer = setInterval(function () {\n"
//				+ "            timeleft --;\n" + "            setupReset();\n"
//
//				+ "            if (timeleft > 600) {\n" + "                \n"
//				+ "            } else if (timeleft == 600) {\n"
//
//				+ "               if (confirm(\"You've been idle for 20 minutes. Are you still working on the system? You will be logged out in 10 minutes after getting this message. Click OK to Logout now!\"))\n"
//				+ "              { window.location.href = window.location.origin + \"sormas-ui/#!logouttimer\"}\n"
//				+ "              else{window.location.reload();};\n" + "            } else if (timeleft ==5) {\n"
//				+ "                alert(\"Logging you out.\");\n"
//
//				+ "            } else if (timeleft == 0) {\n"
//
//				+ "                window.location.href = window.location.origin+\"/sormas-ui/#!logouttimer\";\n"
//				+ "            }\n" + "\n" + "        }, 1000);");
		// Define a JavaScript function to display the modal popup with OK and Cancel buttons after 20 seconds of inactivity
		

		
		Page.getCurrent().getJavaScript().execute(
			    "var timeleft = 1800; " +
			    "function resetTimer() { " +
			    "   return timeleft = (1800 - timeleft) + timeleft; " +
			    "} " +
			    "function setupReset() { " +
			    "   document.addEventListener('mousedown', resetTimer); " +
			    "   document.addEventListener('keypress', resetTimer); " +
			    "   document.addEventListener('touchmove', resetTimer); " +
			    "   document.addEventListener('onscroll', resetTimer); " +
			    "} " +
			    "function showModal() { " +
			    "   var modal = document.createElement('div'); " +
			    "   modal.style.position = 'fixed'; " +
			    "   modal.style.top = '0'; " +
			    "   modal.style.left = '0'; " +
			    "   modal.style.width = '100%'; " +
			    "   modal.style.height = '100%'; " +
			    "   modal.style.border = '5px solid red'; " +
			    "   modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)'; " +
			    "   modal.style.zIndex = '9999'; " +
			    "   var message = document.createElement('div'); " +
			    "   message.innerHTML = 'You have been idle for 20 minutes. You will automatically be <br>logged out 10 minutes after getting this message.'; " +
			    "   message.style.position = 'absolute'; " +
			    "   message.style.top = '50%'; " +
			    "   message.style.left = '50%'; " +
			    "   message.style.height = '160px'; " +
			    "   message.style.transform = 'translate(-50%, -50%)'; " +
			    "   message.style.backgroundColor = '#fff'; " +
			    "   message.style.border = '1px solid green'; " +
			    "   message.style.borderRadius = '4px'; " +
			    
			    "   message.style.padding = '1em'; " +
			    "   message.style.paddingTop = '30px'; " +
			    "   modal.appendChild(message); " +
			    "   var okButton = document.createElement('button'); " +
			    "   okButton.innerHTML = 'Logout Now'; " +
			    "   okButton.style.marginRight = '0.5em'; " +
			    "   okButton.style.position = 'absolute'; " +
			    "   okButton.style.top = '95px'; " +
			    "   okButton.style.right = '200px'; " +
			    "   okButton.style.backgroundColor = 'red'; " +
			    "   okButton.style.border = '1px solid red'; " +
			    "   okButton.style.borderRadius = '4px'; " +
			    "   okButton.style.width = '110px'; " +
			    "   okButton.style.height = '35px'; " +
			    "   okButton.style.color = 'white'; " +
			    "   okButton.addEventListener('click', function () { " +
			    "   window.location.href = window.location.origin + \"/sormas-ui/#!logouttimer\"; " +
			    "   }); " +
			    "   message.appendChild(okButton); " +
			    "   var cancelButton = document.createElement('button'); " +
			    "   cancelButton.innerHTML = 'Stay Logged In'; " +
			    "   cancelButton.style.position = 'absolute'; " +
			    "   cancelButton.style.top = '95px'; " +
			    "   cancelButton.style.right = '80px'; " +
			    "   cancelButton.style.backgroundColor = 'white'; " +
			    "   cancelButton.style.border = '1px solid #0E693A'; " +
			    "   cancelButton.style.borderRadius = '4px'; " +
			    "   cancelButton.style.width = '110px'; " +
			    "   cancelButton.style.height = '35px'; " +
			    "   cancelButton.style.color = 'green'; " +
			    "   cancelButton.addEventListener('click', function () { " +
			    "   document.body.removeChild(modal); " +
			    "   resetTimer()}); " +
			    "   message.appendChild(cancelButton); " +
			    "   document.body.appendChild(modal); " +
			    "} " +
			    "setInterval(function () { " +
			    "   timeleft--; " +
			   
			    "   setupReset(); " +
			    "   if (timeleft > 600) { " +
//			    "   setupReset(document.body.removeChild(modal)); " +
			    "   } else if (timeleft == 600) { " +
			    "       showModal(); " +
			    "   }  else if (timeleft == 0) { " +
			    "   window.location.href = window.location.origin + \"/sormas-ui/#!logouttimer\"; " +
			    "   } " +
			    "}, 1000);"
			);

		
	}
	


	private void showSettingsPopup() {

		Window window = VaadinUiUtil.createPopupWindow();
		window.setCaption(I18nProperties.getString(Strings.headingUserSettings));
		window.setModal(true);

		CommitDiscardWrapperComponent<UserSettingsForm> component = ControllerProvider.getUserController()
				.getUserSettingsComponent(() -> window.close());

		window.setContent(component);
		UI.getCurrent().addWindow(window);
	}

	private static Set<String> initKnownViews() {
		final Set<String> views = new HashSet<>(Arrays.asList(CampaignsView.VIEW_NAME, CampaignDataView.VIEW_NAME, // CampaignStatisticsView.VIEW_NAME,
				/*ReportsView.VIEW_NAME,*/ UsersView.VIEW_NAME, RegionsView.VIEW_NAME,
				DistrictsView.VIEW_NAME, CommunitiesView.VIEW_NAME
				));

		if (permitted(FeatureType.CAMPAIGNS, UserRight.DASHBOARD_CAMPAIGNS_ACCESS)) {
			views.add(CampaignDashboardView.VIEW_NAME);
		}

		return views;
	}

	// notify the view menu about view changes so that it can display which view
	// is currently active
	ViewChangeListener viewChangeListener = new ViewChangeListener() {

		@Override
		public boolean beforeViewChange(ViewChangeEvent event) {

			// Would be better to do this check BEFORE the view is created, but the
			// Navigator can't be extended that way

			if (!event.getParameters().contains("?")) {
				StringBuilder urlParams = new StringBuilder();
				Collection<Object> viewModels = ViewModelProviders.of(event.getNewView().getClass()).getAll();
				for (Object viewModel : viewModels) {
					if (viewModel instanceof BaseCriteria) {
						if (urlParams.length() > 0) {
							urlParams.append('&');
						}
						urlParams.append(((BaseCriteria) viewModel).toUrlParams());
						if (urlParams.length() > 0 && urlParams.charAt(urlParams.length() - 1) == '&') {
							urlParams.deleteCharAt(urlParams.length() - 1);
						}
					}
				}
				if (urlParams.length() > 0) {
					String url = event.getViewName() + "/";
					if (!DataHelper.isNullOrEmpty(event.getParameters())) {
						url += event.getParameters();
					}
					url += "?" + urlParams;
					SormasUI.get().getNavigator().navigateTo(url);
					return false;
				}
			}

			if (event.getViewName().isEmpty()) {
				// redirect to default view
				String defaultView;

				if (permitted(FeatureType.CAMPAIGNS, UserRight.DASHBOARD_CAMPAIGNS_ACCESS)) {
					defaultView = CampaignDashboardView.VIEW_NAME;
				} else {
					defaultView = AboutView.VIEW_NAME;
				}
				SormasUI.get().getNavigator().navigateTo(defaultView);
				return false;
			}
			return true;
		}

		@Override
		public void afterViewChange(ViewChangeEvent event) {
			menu.setActiveView(event.getViewName());
		}
	};

}
