package com.cinoteck.application.views.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinoteck.application.UserProvider;
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

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
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
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.aggregateReport)),new AggregateReportView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.dataCompleteness)), new CompletionAnalysisView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.mobileUsers)), new UserAnalysisView());
		tabComponentMap.put(new Tab("Admin Data Completion"), new AdminCompletionAnalysisView());
		
		return new Tabs(tabComponentMap.keySet().toArray(new Tab[] {}));

	}
	
	UserProvider userProvider = new UserProvider();
	
	public ReportView() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);			
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
//		setSizeFull();
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
