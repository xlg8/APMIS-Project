package com.cinoteck.application.views.dashboard;


import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;


public class NumberCardComponent extends Div{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5930826443777673054L;

	public NumberCardComponent(String randomxx, String value, String title, String color) {
	setSizeFull();

	Div card = new Div();
	card.addClassName("shadow");
	card.addClassName("mb-3");
	card.addClassName("widget-chart");
	card.addClassName("widget-chart2");
	card.addClassName("text-left");
	card.addClassName("card");

	
	// Create the card div
	Div cardDiv = new Div();
	cardDiv.addClassNames("widget-content");

	// Create the outer widget content div
	Div outerWidgetContentDiv = new Div();
	outerWidgetContentDiv.addClassNames("widget-content-outer");

	// Create the widget content wrapper div
	Div widgetContentWrapperDiv = new Div();
	widgetContentWrapperDiv.addClassNames("widget-content-wrapper");

	// Create the widget content left div
	Div widgetContentLeftDiv = new Div();
	widgetContentLeftDiv.addClassNames("widget-content-left d-flex flex-column");

	// Create the widget heading label
	Label widgetHeadingLabel = new Label(title);
	widgetHeadingLabel.addClassNames("widget-heading");

	// Create the widget subheading label
	Label widgetSubheadingLabel = new Label("---	");
	widgetSubheadingLabel.addClassNames("widget-subheading");

	// Add the heading and subheading labels to the widget content left div
	widgetContentLeftDiv.add(widgetHeadingLabel, widgetSubheadingLabel);

	// Create the widget content right div
	Div widgetContentRightDiv = new Div();
	widgetContentRightDiv.addClassNames("widget-content-right");

	// Create the widget numbers label
	Label widgetNumbersLabel = new Label(value);
	widgetNumbersLabel.addClassNames("widget-numbers", "text-success");

	// Add the widget numbers label to the widget content right div
	widgetContentRightDiv.add(widgetNumbersLabel);

	// Add the widget content left and right divs to the widget content wrapper div
	widgetContentWrapperDiv.add(widgetContentLeftDiv, widgetContentRightDiv);

	// Add the widget content wrapper div to the outer widget content div
	outerWidgetContentDiv.add(widgetContentWrapperDiv);

	// Add the outer widget content div to the card div
	cardDiv.add(outerWidgetContentDiv);

	// Add the card div to the outer div
	card.add(cardDiv);
	add(card);
	
	
	}
}
