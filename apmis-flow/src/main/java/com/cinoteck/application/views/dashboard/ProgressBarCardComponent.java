package com.cinoteck.application.views.dashboard;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.progressbar.ProgressBar;

public class ProgressBarCardComponent extends Div {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2666428998964017842L;

	public ProgressBarCardComponent(String randomxx, String value, String title, String color, boolean isPercentage) {

//		System.out.println(randomxx + "-------------BEGIN-----------------" + value);
//		// System.out.println(singleChartJsString);
//		System.out.println("-------------END-----------------");

		//setSizeFull();
		setWidthFull();

		Div card = new Div();
		card.addClassName("shadow");
		card.addClassName("mb-2");
		card.addClassName("widget-chart");
		card.addClassName("widget-chart2");
		card.addClassName("text-left");
		card.addClassName("card");
		card.getStyle().set("height", "80%");
		add(card);

		Div widgetContent = new Div();
		widgetContent.addClassName("widget-content");
		widgetContent.getStyle().set("background-color", "white");
		widgetContent.getStyle().set("border-radius", "0.2rem");
		card.add(widgetContent);

		Div widgetContentOuter = new Div();
		widgetContentOuter.addClassName("widget-content-outer");
		widgetContent.add(widgetContentOuter);

		Div widgetContentWrapper = new Div();
		widgetContentWrapper.addClassName("widget-content-wrapper");
		widgetContentOuter.add(widgetContentWrapper);

		Div widgetContentLeft = new Div();
		widgetContentLeft.addClassName("widget-content-left");
		widgetContentLeft.addClassName("pr-2");
		widgetContentLeft.addClassName("fsize-1");
		widgetContentWrapper.add(widgetContentLeft);

		Div widgetNumbers = new Div();
		widgetNumbers.addClassName("widget-numbers");
		widgetNumbers.addClassName("mt-0");
		widgetNumbers.addClassName("fsize-3");
		
		if(color != null) {
			if (color.isEmpty() || color.isBlank()) {
				widgetNumbers.addClassName("text-success");
			} else {
				widgetNumbers.getStyle().set("color", color);
			}
		} else {
				widgetNumbers.addClassName("text-success");
		}
		
		if(isPercentage) {
			if(value.contains(".")) {
			widgetNumbers.setText(value + "%");
			}else {
				widgetNumbers.setText(value);
			}
		} else {
				widgetNumbers.setText(value);
		}
		widgetContentLeft.add(widgetNumbers);
			
		if(isPercentage) {
			
			
		Div widgetContentRight = new Div();
		widgetContentRight.addClassName("widget-content-right");
		widgetContentRight.addClassName("w-100");
		widgetContentRight.getStyle().set("width", "70%!important");
		if(value.contains(".")) {
		widgetContentWrapper.add(widgetContentRight);
		}
		if(value.contains(".")) {
		ProgressBar progressBar = new ProgressBar();
		progressBar.addClassName("progress-bar-xs");
		progressBar.setMin(0);
		progressBar.setMax(100);
		Float fnl = 111.0f;
		
		
			String result = value.substring(0, value.indexOf("."));
			if(Integer.parseInt(result) < 100) {
				fnl = Float.parseFloat(value);
				progressBar.setValue(fnl);
			}else if(Integer.parseInt(result) > 100) {
				progressBar.setValue(100.0);
			}		
			
			progressBar.getStyle().set("width", "100%");
			progressBar.getStyle().set("height", "0.9rem");
			widgetContentRight.add(progressBar);
		}
		
		

		}
		
		Div widgetContentLeft2 = new Div();
		widgetContentLeft2.addClassName("widget-content-left");
		widgetContentLeft2.addClassName("fsize-1");
		widgetContentOuter.add(widgetContentLeft2);

		Div textMuted = new Div();
		textMuted.addClassName("text-muted");
		textMuted.addClassName("opacity-6");
		textMuted.setText(title);
		widgetContentLeft2.add(textMuted);
	}
}