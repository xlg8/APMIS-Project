package com.cinoteck.application.views.configurations;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

@PageTitle("Regions")
@Route(value = "regions", layout = MainLayout.class)
public class RegionView  extends Div {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8159316049907141477L;

	public RegionView() {
		RegionGrid grid = new RegionGrid();
	        // Add columns, data source, etc. to the grid
	     add(grid);
		
	}
}
