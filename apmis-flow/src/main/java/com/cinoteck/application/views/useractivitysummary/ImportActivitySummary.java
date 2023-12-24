package com.cinoteck.application.views.useractivitysummary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cinoteck.application.views.reports.DataTimelinessReportTab;
import com.cinoteck.application.views.reports.ReportView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserActivitySummaryDto;

@SuppressWarnings("serial")
@Route(layout = UserActivitySummary.class)
public class ImportActivitySummary extends VerticalLayout implements RouterLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6692702413655392041L;
	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();


	private Tabs createTabs() {
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Data Import Log")),new CampaignDataImportActivityView());
		tabComponentMap.put(new Tab("Users Summary"), new PopulationDataImportActivityView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption("Data Edit Summary")), new UsersImportActivityView());
		
		return new Tabs(tabComponentMap.keySet().toArray(new Tab[] {}));

	}

	public ImportActivitySummary() {
		setSpacing(false);
		 HorizontalLayout importsummaryTabsheetLayout = new HorizontalLayout();
		 importsummaryTabsheetLayout.setClassName("campDatFill");
		 
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
	        importsummaryTabsheetLayout.add(tabs);
	        add(importsummaryTabsheetLayout,contentContainer);
	}



}
