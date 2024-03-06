package com.cinoteck.application.views.configurations;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;

@PageTitle("APMIS-Configuration Change Log")
@Route(value = "config-change-log", layout = ConfigurationsView.class)
public class ConfigurationChangeLogView extends VerticalLayout {

	private static final long serialVersionUID = 5091856954264511639L;
//	private GridListDataView<CommunityDto> dataView;

	private Grid<ConfigurationChangeLogDto> grid = new Grid<>(ConfigurationChangeLogDto.class, false);

	@SuppressWarnings("deprecation")
	public ConfigurationChangeLogView() {
		setSizeFull();
		setHeightFull();
		configureConfigActivityGrid();

	}

	public void configureConfigActivityGrid() {
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setHeightFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(ConfigurationChangeLogDto.ACTION_DATE).setHeader(I18nProperties.getCaption("Date"))
				.setSortable(true).setResizable(true);
		grid.addColumn(ConfigurationChangeLogDto.AUDIT_USER).setHeader(I18nProperties.getCaption("Username"))
		.setSortable(true).setResizable(true);

		grid.addColumn(ConfigurationChangeLogDto.ACTION_UNIT_TYPE).setHeader(I18nProperties.getCaption("Unit Type"))
				.setSortable(true).setResizable(true);
		grid.addColumn(ConfigurationChangeLogDto.ACTION_UNIT_NAME).setHeader(I18nProperties.getCaption("Unit Name"))
				.setSortable(true).setResizable(true);
		grid.addColumn(ConfigurationChangeLogDto.UNIT_CODE).setHeader(I18nProperties.getCaption("Unit Code"))
				.setSortable(true).setResizable(true);

		grid.addColumn(ConfigurationChangeLogDto.ACTION_LOGGED).setHeader(I18nProperties.getCaption("Action"))
				.setSortable(true).setResizable(true);

		List<ConfigurationChangeLogDto> dataProvider = FacadeProvider.getUserFacade().getUsersConfigurationChangeLog();
//
		grid.setItems(dataProvider);

		add(grid);
	}

}