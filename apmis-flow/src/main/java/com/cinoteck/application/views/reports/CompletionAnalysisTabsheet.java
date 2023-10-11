package com.cinoteck.application.views.reports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;

@Route(layout = ReportView.class)

public class CompletionAnalysisTabsheet extends VerticalLayout implements RouterLayout {

	private Map<Tab, Component> tabComponentMap = new LinkedHashMap<>();
	CompletionAnalysisView completionAnalysisView = new CompletionAnalysisView();
	List<Tab> tabList = new ArrayList<>(tabComponentMap.keySet());

	private Tabs createTabs() {
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.dataCompleteness)),
				new CompletionAnalysisView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.adminDataCompleteness)), new AdminCompletionAnalysisView());
		tabComponentMap.put(new Tab(I18nProperties.getCaption(Captions.syncErrors)), new FlwErrorAnalysisView());
//		tabComponentMap.put(new Tab("Admin Data Completion"), new AdminCompletionAnalysisView());
		tabList = new ArrayList<>(tabComponentMap.keySet());
		    Tabs tabs = new Tabs(tabList.toArray(new Tab[] {}));

		    return tabs;
	}

	public CompletionAnalysisTabsheet() {
		HorizontalLayout reportTabsheetLayout = new HorizontalLayout();
		reportTabsheetLayout.setClassName("campDatFill");

		Tabs tabs = createTabs();
		tabs.setSizeFull();
		tabs.getStyle().set("background", "rgba(255, 255, 255, 0)");
		Div contentContainer = new Div();
		contentContainer.setSizeFull();
		setSizeFull();
		for (Tab tab : tabList) {
			tab.getElement().getStyle().set("color", "green");// ThemeList().add("custom-tab-caption");
		}
		tabs.addSelectedChangeListener(e -> {
			contentContainer.removeAll();
			contentContainer.add(tabComponentMap.get(e.getSelectedTab()));

		});
		// Set initial content
//		tabs.getSelectedTab().getStyle().set("color", "red");
		contentContainer.add(tabComponentMap.get(tabs.getSelectedTab()));
		reportTabsheetLayout.add(tabs);
		add(reportTabsheetLayout, contentContainer);
	}

}
