package com.cinoteck.application.views.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.dashboard.CampaignSummaryGridView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.user.FormAccess;

@PageTitle("APMIS-Reports")
@Route(value = "reports", layout = MainLayout.class)
@StyleSheet("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css")
@JavaScript("https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js")
public class ReportView extends VerticalLayout implements RouterLayout{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4815769166958388369L;
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();


	private Tabs createTabs() {
		//tabComponentMap.put(new Tab("Aggregate Report"),new JsonDIctionaryGridView());
		tabComponentMap.put(new Tab("Completion Analysis"), new CompletionAnalysisView());
		tabComponentMap.put(new Tab("User Analysis"), new UserAnalysisView());
		
		return new Tabs(tabComponentMap.keySet().toArray(new Tab[] {}));

	}
	
	
	
	public ReportView() {
		 HorizontalLayout reportTabsheetLayout = new HorizontalLayout();
		 reportTabsheetLayout.setClassName("campDatFill");
	        
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
     // Set initial content
        contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
        reportTabsheetLayout.add(tabs);
        add(reportTabsheetLayout,contentContainer);
	}

}
