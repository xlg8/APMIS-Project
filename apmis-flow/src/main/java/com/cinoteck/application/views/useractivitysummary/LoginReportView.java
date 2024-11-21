package com.cinoteck.application.views.useractivitysummary;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.cinoteck.application.views.reports.ReportView;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.ConfigurationChangeLogDto;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserDto;

@SuppressWarnings("serial")
@Route(layout = UserActivitySummary.class)
public class LoginReportView extends VerticalLayout implements RouterLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6692702413655392041L;
	private Grid<UserActivitySummaryDto> grid = new Grid<>(UserActivitySummaryDto.class, false);
	TextField searchField = new TextField();
	HorizontalLayout filterLayout = new HorizontalLayout();
	private DatePicker startDatePicker = new DatePicker();
	private DatePicker endDatePicker = new DatePicker();
	Button displayFilters;
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	private String exportFileName;
	private GridExporter<UserActivitySummaryDto> exporter;
	Icon icon = VaadinIcon.UPLOAD_ALT.create();

	List<UserActivitySummaryDto> userActivityList = FacadeProvider.getUserFacade().getUsersActivityByModule("login");
	private ListDataProvider<UserActivitySummaryDto> dataProvider = new ListDataProvider<>(userActivityList);;

	public LoginReportView() {
		setSizeFull();
		setHeightFull();
		addFilters(dataProvider);
		confiureLoginActivityGrid();
	}

	public void addFilters(ListDataProvider<UserActivitySummaryDto> dataProvider) {

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		displayFilters = new Button(I18nProperties.getCaption(Captions.hideFilters), new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "10px");
		displayFilters.addClickListener(e -> {
			if (filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				displayFilters.setText(I18nProperties.getCaption(Captions.hideFilters));
			} else {
				filterLayout.setVisible(false);
				displayFilters.setText(I18nProperties.getCaption(Captions.showFilters));
			}
		});

		ActivityFilteringUtil filteringSystem = new ActivityFilteringUtil(dataProvider, searchField, startDatePicker,
				endDatePicker);

		Button exportButton = new Button(I18nProperties.getCaption(Captions.export));

		exportButton.setIcon(new Icon(VaadinIcon.UPLOAD));

		exportButton.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});
		anchor.getStyle().set("display", "none");

		filterLayout.getStyle().set("align-items", "flex-end");

		Button resetFiltersButton = new Button(I18nProperties.getCaption(Captions.resetFilters));

		resetFiltersButton.addClickListener(e -> {
			filteringSystem.clearAllFilters();
		});

		filterLayout.add(searchField, startDatePicker, endDatePicker, exportButton, anchor, resetFiltersButton);

		vlayout.add(displayFilters, filterLayout);
		vlayout.getStyle().set("margin-right", "1rem");
		vlayout.setWidth("98%");

		add(vlayout);

	}

	public void confiureLoginActivityGrid() {
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setHeightFull();
		grid.setColumnReorderingAllowed(true);

		TextRenderer<UserActivitySummaryDto> actionDateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getActionDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return dateFormat.format(timestamp);
		});


		Column<UserActivitySummaryDto> userActionDateColumn = grid.addColumn(actionDateRenderer)
				.setHeader(I18nProperties.getCaption("Timestamp")).setSortable(true)
				.setComparator(Comparator.comparing(UserActivitySummaryDto::getActionDate)).setResizable(true);

		grid.addColumn(UserActivitySummaryDto::getCreatingUser_string).setHeader(I18nProperties.getCaption("Username"))
				.setSortable(true).setResizable(true);
		grid.addColumn(UserActivitySummaryDto.ACTION_logged).setHeader(I18nProperties.getCaption("Action"))
				.setSortable(false).setResizable(true);

		// Retrieve the data from your data source (e.g., from a facade)

		grid.setItems(dataProvider);

		exporter = GridExporter.createFor(grid);

		exporter.setExportValue(userActionDateColumn, dto -> {
			Date timestamp = dto.getActionDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return dateFormat.format(timestamp);
		});
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));

		exportFileName = "Login_Report_"
//				+ campaignFormCombo.getValue().toString().replaceAll("[^a-zA-Z0-9]+", " ") + "_"
				+ new SimpleDateFormat("yyyyddMM").format(Calendar.getInstance().getTime());
		exporter.setFileName(exportFileName);
		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		anchor.setId("campDatAnchor");
		anchor.getStyle().set("width", "100px");

		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");
		anchor.getElement().insertChild(0, icon.getElement());

		add(grid);
	}

}
