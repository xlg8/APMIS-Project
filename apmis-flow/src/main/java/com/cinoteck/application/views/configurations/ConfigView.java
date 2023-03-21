package com.cinoteck.application.views.configurations;

import java.util.Arrays;
import java.util.List;

import com.cinoteck.application.utils.StringUtils;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.about.AboutView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.infrastructure.InfrastructureType;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;

@PageTitle("Configurations")
@Route(value = "configurations", layout = MainLayout.class)
public class ConfigView extends VerticalLayout implements RouterLayout {

	private TabSheet tabSheet;

	public ConfigView() {
		// Create the TabSheet
		tabSheet = new TabSheet();
		tabSheet.setWidthFull();
		// Create tabs for each Grid
		List<InfrastructureType> tabLabels = Arrays.asList(InfrastructureType.values());
		for (InfrastructureType label : tabLabels) {
			// Create a tab with a lazy-loaded Grid
			Tab tab = new Tab(StringUtils.convertToTitleCaseIteratingChars(label.name()));
			tabSheet.add(tab, createLazyLoadedGrid(label.name())); // edit this to add a class instead.. that is where
																	// the grid will be populated
		}
		// Add the TabSheet to the layout
		add(tabSheet);
	}

	// Create a lazy-loaded Grid for a tab
	private Component createLazyLoadedGrid(String label) {
		if(label.equalsIgnoreCase(InfrastructureType.AREA.name())) {
			return new RegionView();
		}else {
			return new AboutView();
		}
	}

}
