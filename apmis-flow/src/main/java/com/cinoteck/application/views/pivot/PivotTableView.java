package com.cinoteck.application.views.pivot;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


@PageTitle("Pivot Table ")
@Route(value = "pivottable", layout = MainLayout.class)
public class PivotTableView  extends VerticalLayout{

	public PivotTableView() {
		setSizeFull();
		 Html html = new Html("<iframe src='pivot_trial/samples/01_init/07_grid_flat_mode.html' style='width:100%; height:100%;'></iframe>");
	        add(html);
	        
	}
}
