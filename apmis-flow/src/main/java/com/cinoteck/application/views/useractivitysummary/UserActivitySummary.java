package com.cinoteck.application.views.useractivitysummary;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.reports.AggregateReportView;
import com.cinoteck.application.views.reports.CompletionAnalysisTabsheet;
import com.cinoteck.application.views.reports.DataTimelinessReportTab;
import com.cinoteck.application.views.reports.UserAnalysisView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;

@PageTitle("APMIS-User Activity Summary")
@Route(value = "useractivity", layout = MainLayout.class)
public class UserActivitySummary extends VerticalLayout implements RouterLayout{

	private static final long serialVersionUID = -4815769166958388369L;
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();


	private Tabs createTabs() {
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Login Summary")),new LoginReportView());
		tabComponentMap.put(new Tab("Users Summary"), new UserModuleActionSummaryView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Data Edit Summary")), new CampaignDataEditActivityView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Import Activity Log")), new ImportActivitySummary());
		
		return new Tabs(tabComponentMap.keySet().toArray(new Tab[] {}));

	}
	
	public UserActivitySummary() {
		setSpacing(false);
		 HorizontalLayout summaryTabsheetLayout = new HorizontalLayout();
		 summaryTabsheetLayout.setClassName("campDatFill");
		 
		 Tabs tabs = createTabs();
			tabs.setSizeFull();
			tabs.getStyle().set("background", "#434343");
	        Div contentContainer = new Div();
	        contentContainer.setSizeFull();
	        setSizeFull();
	        tabs.addSelectedChangeListener(e -> {
	            contentContainer.removeAll();
	            contentContainer.add(tabComponentMap.get(e.getSelectedTab()));
	        });
	        
	        contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
	        summaryTabsheetLayout.add(tabs);
	        add(summaryTabsheetLayout,contentContainer);
	}
}
