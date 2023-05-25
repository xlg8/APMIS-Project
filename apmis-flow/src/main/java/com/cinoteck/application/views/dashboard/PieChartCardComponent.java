//package com.cinoteck.application.views.dashboard;
//
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.component.html.Anchor;
//import com.vaadin.flow.component.html.Div;
//import com.vaadin.flow.component.html.Label;
//import com.vaadin.flow.component.page.Page;
//
//public class PieChartCardComponent extends Div{
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -2258924119564312029L;
//
//	public PieChartCardComponent() {
//
//		addClassName("col-md-12");
//		addClassName("col-lg-6");
//
//		// Create the outer div
//		Div outerDiv = new Div();
//		outerDiv.addClassNames("mb-3", "card");
//
//		// Create the card header tab div
//		Div cardHeaderTabDiv = new Div();
//		cardHeaderTabDiv.addClassNames("card-header-tab", "card-header-tab-animation", "card-header");
//
//		// Create the card header title div
//		Div cardHeaderTitleDiv = new Div();
//		cardHeaderTitleDiv.addClassNames("card-header-title");
//
//		// Create the header icon element
//		Anchor headerIcon = new Anchor();
//		headerIcon.addClassNames("header-icon", "lnr-apartment", "icon-gradient", "bg-love-kiss");
//
//		// Create the sales report label
//		Label salesReportLabel = new Label("Sales Report");
//
//		// Add the header icon and sales report label to the card header title div
//		cardHeaderTitleDiv.add(headerIcon, salesReportLabel);
//
//		// Add the card header title div to the card header tab div
//		cardHeaderTabDiv.add(cardHeaderTitleDiv);
//
//		// Create the card body div
//		Div cardBodyDiv = new Div();
//		cardBodyDiv.addClassNames("card-body"); // padding: 0.25rem
//		cardBodyDiv.getStyle().set("padding", "0.25rem!important");
//
//		// Create the tab content div
//		Div tabContentDiv = new Div();
//		tabContentDiv.addClassNames("tab-content");
//
//		// Create the tab pane div
//		Div tabPaneDiv = new Div();
//		tabPaneDiv.addClassNames("tab-pane", "fade", "show", "active");
//		tabPaneDiv.setId("tabs-eg-77");
//
//		// Create the widget chart div
//		Div widgetChartDiv = new Div();
//		widgetChartDiv.addClassNames("card", "widget-chart", "widget-chart2", "text-left", "w-100");
//
//		// Create the widget chat wrapper outer div
//		Div widgetChatWrapperOuterDiv = new Div();
//		widgetChatWrapperOuterDiv.addClassNames("widget-chat-wrapper-outer");
//
//		// Create the widget chart wrapper div
//		Div widgetChartWrapperDiv = new Div();
//		widgetChartWrapperDiv.addClassNames("widget-chart-wrapper", "widget-chart-wrapper-lg", "opacity-10", "m-0");
//
//		// Create the canvas element<div id="container"></div>
//		Div chartManholder = new Div();
//		chartManholder.setId("container");
//
//		// Add the canvas element to the widget chart wrapper div
//		widgetChartWrapperDiv.add(chartManholder);
//
//		// Add the widget chart wrapper div to the widget chat wrapper outer div
//		widgetChatWrapperOuterDiv.add(widgetChartWrapperDiv);
//
//		// Add the widget chat wrapper outer div to the widget chart div
//		widgetChartDiv.add(widgetChatWrapperOuterDiv);
//
//		// Add the widget chart div to the tab pane div
//		tabPaneDiv.add(widgetChartDiv);
//
//		// Add the tab pane div to the tab content div
//		tabContentDiv.add(tabPaneDiv);
//
//		// Add the tab content div to the card body div
//		cardBodyDiv.add(tabContentDiv);
//
//		// Add the card header tab div and card body div to the outer div
//		outerDiv.add(cardHeaderTabDiv, cardBodyDiv);
//		add(outerDiv);
//
//		// Get the current page
//		Page page = UI.getCurrent().getPage();
//
//		// Execute the JavaScript code
//		page.executeJavaScript("Highcharts.chart('container', {\r\n"
//				+ "    chart: {\r\n"
//				+ "        plotBackgroundColor: null,\r\n"
//				+ "        plotBorderWidth: null,\r\n"
//				+ "        plotShadow: false,\r\n"
//				+ "        type: 'pie'\r\n"
//				+ "    },\r\n"
//				+ "    title: {\r\n"
//				+ "        text: 'Browser market shares in May, 2020',\r\n"
//				+ "        align: 'left'\r\n"
//				+ "    },\r\n"
//				+ "    tooltip: {\r\n"
//				+ "        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'\r\n"
//				+ "    },\r\n"
//				+ "    accessibility: {\r\n"
//				+ "        point: {\r\n"
//				+ "            valueSuffix: '%'\r\n"
//				+ "        }\r\n"
//				+ "    },\r\n"
//				+ "    plotOptions: {\r\n"
//				+ "        pie: {\r\n"
//				+ "            allowPointSelect: true,\r\n"
//				+ "            cursor: 'pointer',\r\n"
//				+ "            dataLabels: {\r\n"
//				+ "                enabled: true,\r\n"
//				+ "                format: '<b>{point.name}</b>: {point.percentage:.1f} %'\r\n"
//				+ "            }\r\n"
//				+ "        }\r\n"
//				+ "    },\r\n"
//				+ "    series: [{\r\n"
//				+ "        name: 'Brands',\r\n"
//				+ "        colorByPoint: true,\r\n"
//				+ "        data: [{\r\n"
//				+ "            name: 'Chrome',\r\n"
//				+ "            y: 70.67,\r\n"
//				+ "            sliced: true,\r\n"
//				+ "            selected: true\r\n"
//				+ "        }, {\r\n"
//				+ "            name: 'Edge',\r\n"
//				+ "            y: 14.77\r\n"
//				+ "        },  {\r\n"
//				+ "            name: 'Firefox',\r\n"
//				+ "            y: 4.86\r\n"
//				+ "        }, {\r\n"
//				+ "            name: 'Safari',\r\n"
//				+ "            y: 2.63\r\n"
//				+ "        }, {\r\n"
//				+ "            name: 'Internet Explorer',\r\n"
//				+ "            y: 1.53\r\n"
//				+ "        },  {\r\n"
//				+ "            name: 'Opera',\r\n"
//				+ "            y: 1.40\r\n"
//				+ "        }, {\r\n"
//				+ "            name: 'Sogou Explorer',\r\n"
//				+ "            y: 0.84\r\n"
//				+ "        }, {\r\n"
//				+ "            name: 'QQ',\r\n"
//				+ "            y: 0.51\r\n"
//				+ "        }, {\r\n"
//				+ "            name: 'Other',\r\n"
//				+ "            y: 2.6\r\n"
//				+ "        }]\r\n"
//				+ "    }]\r\n"
//				+ "});");
//
//	}
//
//}
