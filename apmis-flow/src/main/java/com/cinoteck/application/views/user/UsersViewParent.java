package com.cinoteck.application.views.user;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.configurations.ClusterView;
import com.cinoteck.application.views.configurations.ConfigurationChangeLogView;
import com.cinoteck.application.views.configurations.DistrictView;
import com.cinoteck.application.views.configurations.ProvinceView;
import com.cinoteck.application.views.configurations.RegionView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserRight;

@PageTitle("APMIS-User Management")
@Route(value = "usersmanagement", layout = MainLayout.class)
public class UsersViewParent extends VerticalLayout implements RouterLayout {
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();
	Anchor anchor;
	HorizontalLayout userActionLayout = new HorizontalLayout();
	Button displayActionButtons = new Button("Show Action Buttons", new Icon(VaadinIcon.SLIDERS));

	UserProvider userProvider = new UserProvider();
	MainLayout mainLayout = new MainLayout();

	private Tabs createTabs() {
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Users")), new UserView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Disabled Users")), new disabledUsersView());

		return new Tabs(tabComponentMap.keySet().toArray(new Tab[] {}));
	}

	public UsersViewParent() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		setSizeFull();
		HorizontalLayout campDatFill = new HorizontalLayout();
		campDatFill.setClassName("campDatFill");

		Tabs tabs = createTabs();
		tabs.getStyle().set("background", "#434343");
		tabs.getStyle().set("background", "#434343");
		tabs.setSizeFull();
		Div contentContainer = new Div();
		contentContainer.setSizeFull();
		setSizeFull();
		contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
		tabs.addSelectedChangeListener(e -> {
			contentContainer.removeAll();

			contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
		});
		campDatFill.add(tabs);

		add(campDatFill, contentContainer);
	}
}
