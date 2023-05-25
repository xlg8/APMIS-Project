package com.cinoteck.application.views.dashboard;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDataDto;
import de.symeda.sormas.api.campaign.diagram.CampaignDiagramDefinitionDto;

public class BarChartCardComponent extends Div {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1469748692188293859L;

	@SuppressWarnings("deprecation")
	public BarChartCardComponent(String singleChartJsString, String randomxx) {
	
setSizeFull();

		// Create the outer div
		Div outerDiv = new Div();
		outerDiv.addClassNames("mb-3", "card");

		// Create the card header tab div
		Div cardHeaderTabDiv = new Div();
		cardHeaderTabDiv.addClassNames("card-header-tab", "card-header-tab-animation", "card-header");

		// Create the card header title div
		Div cardHeaderTitleDiv = new Div();
		cardHeaderTitleDiv.addClassNames("card-header-title");

		// Create the header icon element
		Anchor headerIcon = new Anchor();
		headerIcon.addClassNames("header-icon", "lnr-apartment", "icon-gradient", "bg-love-kiss");

		// Create the sales report label
		Label salesReportLabel = new Label("Sales Report");

		// Add the header icon and sales report label to the card header title div
		cardHeaderTitleDiv.add(headerIcon, salesReportLabel);

		// Add the card header title div to the card header tab div
		cardHeaderTabDiv.add(cardHeaderTitleDiv);

		// Create the card body div
		Div cardBodyDiv = new Div();
		cardBodyDiv.addClassNames("card-body"); // padding: 0.25rem
		cardBodyDiv.getStyle().set("padding", "0.25rem!important");

		// Create the tab content div
		Div tabContentDiv = new Div();
		tabContentDiv.addClassNames("tab-content");

		// Create the tab pane div
		Div tabPaneDiv = new Div();
		tabPaneDiv.addClassNames("tab-pane", "fade", "show", "active");
		tabPaneDiv.setId("tabs-eg-77");

		// Create the widget chart div
		Div widgetChartDiv = new Div();
		widgetChartDiv.addClassNames("text-left", "w-100");//"card", "widget-chart", "widget-chart2", "text-left", "w-100");

		// Create the widget chat wrapper outer div
		Div widgetChatWrapperOuterDiv = new Div();
		widgetChatWrapperOuterDiv.addClassNames("widget-chat-wrapper-outer");

		// Create the widget chart wrapper div
		Div widgetChartWrapperDiv = new Div();
		widgetChartWrapperDiv.addClassNames("widget-chart-wrapper", "widget-chart-wrapper-lg", "opacity-10", "m-0");

		// Create the canvas element<div id="container"></div>
		Div chartManholder = new Div();
		chartManholder.setId(randomxx);

		// Add the canvas element to the widget chart wrapper div
		widgetChartWrapperDiv.add(chartManholder);

		// Add the widget chart wrapper div to the widget chat wrapper outer div
		widgetChatWrapperOuterDiv.add(widgetChartWrapperDiv);

		// Add the widget chat wrapper outer div to the widget chart div
		widgetChartDiv.add(widgetChatWrapperOuterDiv);

		// Add the widget chart div to the tab pane div
		tabPaneDiv.add(widgetChartDiv);

		// Add the tab pane div to the tab content div
		tabContentDiv.add(tabPaneDiv);

		// Add the tab content div to the card body div
		cardBodyDiv.add(tabContentDiv);

		// Add the card header tab div and card body div to the outer div
		outerDiv.add(cardHeaderTabDiv, cardBodyDiv);
		add(outerDiv);
		
		VaadinSession.getCurrent().getUIs().forEach(ui -> {
            ui.getPage().executeJs(singleChartJsString);

		// Get the current page
		//Page page = UI.getCurrent().getPage();

		// Execute the JavaScript code
		//page.(singleChartJsString);

		 });
	

	}

}
