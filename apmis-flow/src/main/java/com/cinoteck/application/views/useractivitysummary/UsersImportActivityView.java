package com.cinoteck.application.views.useractivitysummary;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.reports.ReportView;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignPhase;
import de.symeda.sormas.api.campaign.CampaignReferenceDto;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.statistics.CampaignStatisticsDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserActivitySummaryDto;
import de.symeda.sormas.api.user.UserType;

@SuppressWarnings("serial")
@Route(layout = UserActivitySummary.class)
public class UsersImportActivityView extends VerticalLayout implements RouterLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6692702413655392041L;
	private Grid<UserActivitySummaryDto> grid = new Grid<>(UserActivitySummaryDto.class, false);
	TextField searchField = new TextField();
	HorizontalLayout filterLayout = new HorizontalLayout();
	private ListDataProvider<UserActivitySummaryDto> dataProvider;
	private DatePicker startDatePicker = new DatePicker();
	private DatePicker endDatePicker = new DatePicker();
	Button displayFilters;

	private CampaignFormDataCriteria criteria;
	private CampaignFormMetaDto formMetaReference;
	private CampaignFormDataDto campaignFormDatadto;
	List<CampaignFormMetaReferenceDto> campaignForms;
	ComboBox<String> campaignYear = new ComboBox<>();
	ComboBox<CampaignReferenceDto> campaignz = new ComboBox<>();
	ComboBox<CampaignPhase> campaignPhase = new ComboBox<>();
	ComboBox<CampaignFormMetaReferenceDto> campaignFormCombo = new ComboBox<>();
	UserProvider userProvider = new UserProvider();
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	private String exportFileName;
	private GridExporter<UserActivitySummaryDto> exporter;
	Icon icon = VaadinIcon.UPLOAD_ALT.create();


	public UsersImportActivityView() {
		setSizeFull();
		setHeightFull();
		addFilters();
		confiureDataImportActivityGrid();
	}
	
	public void addFilters() {

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

		searchField.setLabel("Search");

		searchField.addClassName("searchField");
		searchField.setPlaceholder("Search Username");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClearButtonVisible(true);
		searchField.setWidth("145px");
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> {
			String searchTerm = e.getValue().trim();

			if (searchTerm != null && !searchTerm.isEmpty()) {
				dataProvider.setFilter(userActivity -> {
					// Check if the username contains the search term (case insensitive)
					boolean usernameMatches = userActivity.getCreatingUser_string().toLowerCase()
							.contains(searchTerm.toLowerCase());
					return usernameMatches;
				});
			} else {
				// Clear the filter when search field is empty
				dataProvider.clearFilters();
			}
		});

		// DatePickers for range filter
		endDatePicker.setLabel("To Date");

		startDatePicker.setPlaceholder("Start Date");
		startDatePicker.setClearButtonVisible(true);

		endDatePicker.setLabel("To Date");

		endDatePicker.setPlaceholder("End Date");
		endDatePicker.setClearButtonVisible(true);

		// Add value change listeners for the date pickers
		ValueChangeListener<AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>> dateRangeFilterListener = e -> {
			LocalDate startDate = startDatePicker.getValue();
			LocalDate endDate = endDatePicker.getValue();

			// Apply date range filter if both start and end dates are selected
			if (startDate != null && endDate != null) {
				dataProvider.setFilter(userActivity -> {
					LocalDate activityDate = userActivity.getActionDate().toInstant().atZone(ZoneId.systemDefault())
							.toLocalDate();
					return (activityDate.isEqual(startDate) || activityDate.isAfter(startDate))
							&& (activityDate.isEqual(endDate) || activityDate.isBefore(endDate));
				});
			} else {
				dataProvider.clearFilters(); // Clear the filter when no range is selected
			}
		};

		// Attach the listener to both date pickers
		startDatePicker.addValueChangeListener(dateRangeFilterListener);
		endDatePicker.addValueChangeListener(dateRangeFilterListener);

		campaignYear.setLabel(I18nProperties.getCaption(Captions.campaignYear));
		campaignYear.getStyle().set("padding-top", "0px !important");
		campaignYear.setClassName("col-sm-6, col-xs-6");

		campaignz.setLabel(I18nProperties.getCaption(Captions.Campaigns));
		campaignz.getStyle().set("padding-top", "0px !important");
		campaignz.setClassName("col-sm-6, col-xs-6");

		campaignPhase.setLabel(I18nProperties.getCaption(Captions.Campaign_phase));
		campaignPhase.getStyle().set("padding-top", "0px !important");
		campaignPhase.setClearButtonVisible(true);
		campaignz.setClassName("col-sm-6, col-xs-6");

		campaignFormCombo.setLabel(I18nProperties.getCaption(Captions.campaignCampaignForm));
		campaignFormCombo.getStyle().set("padding-top", "0px !important");
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");
		campaignFormCombo.setClassName("col-sm-6, col-xs-6");
		campaignFormCombo.setClearButtonVisible(true);

		campaignPhase.setItemLabelGenerator(this::getLabelForEnum);
		if (userProvider.getUser().getUsertype() == UserType.EOC_USER) {
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.INTRA);
		} else {
			campaignPhase.setItems(CampaignPhase.values());
			campaignPhase.setValue(CampaignPhase.PRE);
		}

		List<CampaignReferenceDto> campaigns = FacadeProvider.getCampaignFacade().getAllCampaignByStartDate();
		CampaignReferenceDto lastStarted = FacadeProvider.getCampaignFacade().getLastStartedCampaign();
		List<String> camYearList = campaigns.stream().map(CampaignReferenceDto::getCampaignYear).distinct()
				.collect(Collectors.toList());

		campaignYear.setItems(camYearList);

		campaignYear.setValue(lastStarted.getCampaignYear());

		List<CampaignReferenceDto> allCampaigns = campaigns.stream()
				.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());

		campaignz.setItems(allCampaigns);
		campaignz.setValue(lastStarted);
		campaignFormCombo.getStyle().set("--vaadin-combo-box-overlay-width", "350px");

		campaignPhase.setItemLabelGenerator(this::getLabelForEnum);

		campaignPhase.addValueChangeListener(e -> {

			if (e.getValue() != null) {

				campaignFormCombo.clear();
				campaignForms = FacadeProvider.getCampaignFormMetaFacade()
						.getAllCampaignFormMetasAsReferencesByRoundandCampaign(e.getValue().toString().toLowerCase(),
								campaignz.getValue().getUuid());
//				campaignFormCombo.setValue(campaignForms.get(0));
				campaignFormCombo.setItems(campaignForms);
				filterGridData();

			}

		});

		// Configure Comboboxes Value Change Listeners
		campaignYear.addValueChangeListener(e -> {
			campaignz.clear();
			List<CampaignReferenceDto> allCampaigns_ = campaigns.stream()
					.filter(c -> c.getCampaignYear().equals(campaignYear.getValue())).collect(Collectors.toList());
			campaignz.setItems(allCampaigns_);
			campaignz.setValue(allCampaigns_.get(0));
			filterGridData();

		});

		String language = userProvider.getUser().getLanguage().toString();

		Set<FormAccess> xx = new HashSet<FormAccess>();
		xx = userProvider.getUser().getFormAccess();

		List<CampaignFormMetaReferenceDto> filterdList = new ArrayList<>();

		campaignForms = FacadeProvider.getCampaignFormMetaFacade()
				.getAllCampaignFormMetasAsReferencesByRoundandCampaign(
						campaignPhase.getValue().toString().toLowerCase(), campaignz.getValue().getUuid());

		campaignForms.removeIf(e -> e.getFormCategory() == null);

		for (FormAccess n : xx) {
			boolean yn = campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
					.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()).size() > 0;
			if (yn) {
				filterdList.addAll(campaignForms.stream().filter(e -> !e.getFormCategory().equals(null))
						.filter(ee -> ee.getFormCategory().equals(n)).collect(Collectors.toList()));
			}
		}

		filterdList.sort(Comparator.comparing(CampaignFormMetaReferenceDto::getCaption));

		campaignz.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				campaignFormCombo.clear();
				filterGridData();
//				campaignFormCombo.setValue(campaignForms.get(0));

			}
		});

		campaignFormCombo.setItems(filterdList);
		campaignFormCombo.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				System.out.println("Grid Filtered from campaign form combo ");
				filterGridData();

			} else {

			}

		});

		Button exportButton = new Button(I18nProperties.getCaption(Captions.export));
		exportButton.setIcon(new Icon(VaadinIcon.UPLOAD));

		exportButton.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");
		});
		anchor.getStyle().set("display", "none");

		
		filterLayout.getStyle().set("align-items", "flex-end");
		
		Button resetFiltersButton = new Button(I18nProperties.getCaption(Captions.resetFilters));
//		resetFiltersButton.setIcon(new Icon(VaadinIcon.UPLOAD));

		resetFiltersButton.addClickListener(e -> {
			startDatePicker.clear();
			endDatePicker.clear();
			searchField.clear();
			
			List<UserActivitySummaryDto> userActivityList = FacadeProvider.getUserFacade()
					.getUsersActivityByModule("Campaign Data Import");
			// Wrap the List<UserActivitySummaryDto> in a ListDataProvider
			dataProvider = new ListDataProvider<>(userActivityList);

			grid.setItems(dataProvider);

		});

		filterLayout.add(searchField, startDatePicker, endDatePicker, exportButton, anchor, resetFiltersButton);// , campaignYear,
																							// campaignz, campaignPhase,
		// campaignFormCombo);

		vlayout.add(displayFilters, filterLayout);
		vlayout.getStyle().set("margin-right", "1rem");
		vlayout.setWidth("98%");

		add(vlayout);

	}

	private String getLabelForEnum(CampaignPhase campaignPhase) {
		if (campaignPhase != null) {
			switch (campaignPhase) {
			case PRE:
				return "Pre-Campaign";

			case POST:
				return "Post-Campaign";

			case INTRA:
				return "Intra-Campaign";

			default:
				return campaignPhase.toString();
			}
		} else {
			return "Intra-Campaign";
		}

	}
	
	private void filterGridData() {
		CampaignReferenceDto selectedCampaign = campaignz.getValue() != null ? campaignz.getValue() : null;
		String selectedForm = campaignFormCombo.getValue() != null ? campaignFormCombo.getValue().toString() : "";

		// Apply the filter only if a campaign is selected
		dataProvider.setFilter(userActivity -> {
			String actionDescription = userActivity.getAction_logged(); // Example: "Edited Data: LQAS - Cluster Data
																		// Collection Form (5-10) in Nov SNID 2023"

			// Extract the substring after "in " (case-sensitive, can adjust if needed)
			String[] splitDescription = actionDescription.split(" in ");
			String campaignName = splitDescription.length > 1 ? splitDescription[1].trim() : "";
			String formName = splitDescription.length > 1 ? splitDescription[0].trim() : "";

			String[] splitFormNameDescription = splitDescription[0].trim().split("User Attempted ");

			System.out.println("splitDescription[0].trim()" + splitFormNameDescription[1].trim()
					+ "splitDescription[1].trim()" + splitFormNameDescription[0].trim()
					+ splitFormNameDescription.toString().equalsIgnoreCase(selectedForm.toString()));

			String extraxtedFormName = splitFormNameDescription[1].trim();
			// Match the campaign name with the selected campaign
			boolean matchesCampaign = selectedCampaign == null
					|| campaignName.toString().equalsIgnoreCase(selectedCampaign.toString());

			System.out.println(selectedForm.toString() + "====" + splitFormNameDescription.toString());
			boolean matchesCampaignForm = selectedForm == null
					|| extraxtedFormName.toString().equalsIgnoreCase(selectedForm.toString());

			return matchesCampaign && matchesCampaignForm;
		});
	}

	public void confiureDataImportActivityGrid() {
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setHeightFull();
		grid.setColumnReorderingAllowed(true);

//		grid.addColumn(UserActivitySummaryDto.ACTION_MODULE).setHeader(I18nProperties.getCaption(Captions.Campaign_endDate))
//				.setSortable(true).setResizable(true);
		
		TextRenderer<UserActivitySummaryDto> actionDateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getActionDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return dateFormat.format(timestamp);
		});
		
		Column<UserActivitySummaryDto> userActionDateColumn = grid.addColumn(actionDateRenderer).setHeader(I18nProperties.getCaption("Timestamp"))
		.setSortable(false).setResizable(true);
		grid.addColumn(UserActivitySummaryDto::getCreatingUser_string).setHeader(I18nProperties.getCaption("Username"))
		.setSortable(true).setResizable(true);
		grid.addColumn(UserActivitySummaryDto.ACTION_logged).setHeader(I18nProperties.getCaption("Action"))
				.setSortable(false).setResizable(true);
		
		List<UserActivitySummaryDto> userActivityList = FacadeProvider.getUserFacade().getUsersActivityByModule("Users Import");

		dataProvider = new ListDataProvider<>(userActivityList);
		grid.setItems(dataProvider);
		
		exporter = GridExporter.createFor(grid);

		exporter.setExportValue(userActionDateColumn, dto -> {
			Date timestamp = dto.getActionDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return dateFormat.format(timestamp);
		});
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.campaignDataInformation));

		exportFileName = "User_Data_Import_Report_"
//						+ campaignFormCombo.getValue().toString().replaceAll("[^a-zA-Z0-9]+", " ") + "_"
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
