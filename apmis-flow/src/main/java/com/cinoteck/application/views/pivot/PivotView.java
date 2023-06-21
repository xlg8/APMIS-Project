package com.cinoteck.application.views.pivot;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@PageTitle("Pivot")
@Route(value = "pivot", layout = MainLayout.class)
public class PivotView extends VerticalLayout {
	
	public  PivotView() {
		setSizeFull();
		 Html html = new Html("<iframe src='pivottablejs.html' style='width:100%; height:100%;'></iframe>");
	        add(html);
	        
	      
	}

}
