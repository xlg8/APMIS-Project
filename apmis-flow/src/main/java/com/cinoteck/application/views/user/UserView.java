package com.cinoteck.application.views.user;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cinoteck.application.RowCount;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
//import com.cinoteck.application.views.configurations.RegionFilter;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridDataView;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.CampaignJurisdictionLevel;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.Descriptions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaType;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionIndexDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.report.CommunityUserReportModelDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.SortProperty;


@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)

public class UserView extends VerticalLayout {

	public static final String ACTIVE_FILTER = I18nProperties.getString(Strings.active);
	public static final String INACTIVE_FILTER = I18nProperties.getString(Strings.inactive);

	private ComboBox<String> activeFilter;
	private ComboBox<UserRole> userRolesFilter;
	private ComboBox<AreaReferenceDto> areaFilter;
	private ComboBox<RegionReferenceDto> regionFilter;
	private ComboBox<DistrictReferenceDto> districtFilter;
	public ComboBox communitiesFilter = new ComboBox<>();
//	List<CommunityReferenceDto> communities;

	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
	List<CommunityReferenceDto> communities;
	
	private DataProvider<UserDto, UserCriteria> dataProvider;

	UserCriteria criteria;
	UserProvider userProvider = new UserProvider();
	private Grid<UserDto> grid = new Grid<>(UserDto.class, false);
	List<UserDto> usersData = FacadeProvider.getUserFacade().getIndexList(null, null, null, null).stream()
			.collect(Collectors.toList());
//	private GridListDataView<UserDto> dataView = grid.setItems(usersData);
	private UsersDataProvider usersDataProvider = new UsersDataProvider();
	private ConfigurableFilterDataProvider<UserDto, Void, UserCriteria> filterDataProvider;
//	private GridDataView<UserDto> dataViews = grid.setItems(filterDataProvider);
	
	UserForm form;
	MenuBar menuBar;
//	RowCount rowsCount = new RowCount(Strings.labelNumberOfUsers, dataView.getItemCount());

	Button createUserButton;
	Button exportUsersButton;
	Button exportRolesButton;
	Button bulkModeButton;
	Button leaveBulkModeButton;
	TextField searchField;
	
	Button displayFilters;

	private static final String CSV_FILE_PATH = "./result.csv";

	public UserView() {

		if (userProvider.hasUserRight(UserRight.USER_CREATE)) {

			Dialog dialog = new Dialog();
			dialog.setModal(true);
			dialog.addClassNames("dialog-alignment");
			dialog.setDraggable(true);
			dialog.setModal(false);
			dialog.setHeaderTitle("CREATE NEW USER");

			VerticalLayout dialogLayout = createDialogLayout();
			dialog.add(dialogLayout);

			Button closeButton = new Button(new Icon("lumo", "cross"), (e) -> dialog.close());
			closeButton.getStyle().set("color", "green");
			dialog.getHeader().add(closeButton);

			Button cancelButton = new Button("DISCARD CHANGES", e -> dialog.close());
			cancelButton.setHeightFull();
			Button saveButton = new Button("SAVE");
			saveButton.setHeightFull();
			saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			
			dialog.getFooter().add(cancelButton, saveButton);
			createUserButton = new Button(Captions.userNewUser, e -> dialog.open());
			exportUsersButton = new Button(Captions.export);
			exportRolesButton = new Button(Captions.exportUserRoles);

			if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {

				bulkModeButton = new Button(Captions.actionEnterBulkEditMode);
				leaveBulkModeButton = new Button(Captions.actionLeaveBulkEditMode);
				menuBar = new MenuBar();
			}
			searchField = new TextField();
		}

		setHeightFull();
		addFilters();
		configureGrid();
		configureForm();
		add(getContent());
		closeEditor();
	}

	private Component getContent() {
		HorizontalLayout content = new HorizontalLayout();
		// content.setFlexGrow(2, grid);
		content.setFlexGrow(4, form);
		content.addClassNames("content");
		content.setSizeFull();
		content.add(grid, form);
		return content;
	}

	private void configureGrid() {

		ComponentRenderer<Label, UserDto> userRolesRenderer = new ComponentRenderer<>(reportModelDto -> {
			String value = String.valueOf(reportModelDto.getUserRoles()).replace("[", "").replace("]", "")
					.replace("null,", "").replace("null", "");
			Label label = new Label(value);
			label.getStyle().set("color", "var(--lumo-body-text-color) !important");
			return label;
		});

		ComponentRenderer<Checkbox, UserDto> activeRenderer = new ComponentRenderer<>(input -> {
			boolean value = input.isActive();
			Checkbox checkbox = new Checkbox();
			if (value == true)
				checkbox.setValue(true);

			return checkbox;
		});

		criteria = new UserCriteria();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		Column<UserDto> activeCol = grid.addColumn(activeRenderer).setHeader("Active").setSortable(true).setResizable(true);
		Column<UserDto> userRolesCol = grid.addColumn(userRolesRenderer).setHeader("User Roles").setSortable(true).setResizable(true);
		Column<UserDto> usernameCol = grid.addColumn(UserDto::getUserName).setHeader("Username").setSortable(true).setResizable(true);
		Column<UserDto> nameCol = grid.addColumn(UserDto::getName).setHeader("Name").setSortable(true).setResizable(true);
		Column<UserDto> emailCol = grid.addColumn(UserDto::getUserEmail).setHeader("Email").setSortable(true).setResizable(true);
		Column<UserDto> organisationCol = grid.addColumn(UserDto::getUserPosition).setHeader("Organisation").setSortable(true).setResizable(true);
		Column<UserDto> positionCol = grid.addColumn(UserDto::getUserOrganisation).setHeader("Position").setSortable(true).setResizable(true);
		Column<UserDto> areaCol = grid.addColumn(UserDto::getArea).setHeader("Area").setResizable(true).setSortable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setAllRowsVisible(false);
		filterDataProvider = usersDataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);

		grid.asSingleSelect().addValueChangeListener(event -> editUser(event.getValue()));
		
//		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
//		grid.setDataProvider(dataProvider);
	}
	
	private Stream<UserDto> fetchCampaignFormData(
			Query<UserDto, UserCriteria> query) {
		return FacadeProvider.getUserFacade()
				.getIndexList(criteria, query.getOffset(), query.getLimit(), query.getSortOrders().stream()
						.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
								sortOrder.getDirection() == SortDirection.ASCENDING))
						.collect(Collectors.toList()))
				.stream();
	}

	private int countCampaignFormData(Query<UserDto, UserCriteria> query) {
		return (int) FacadeProvider.getUserFacade().count(criteria);
	}
	
	private void setDataProvider() {
//		DataProvider<UserDto, UserCriteria> dataProvider = DataProvider
//				.fromFilteringCallbacks(
//						query -> FacadeProvider.getUserFacade()
//								.getIndexList(criteria, query.getOffset(), query.getLimit(),
//										query.getSortOrders().stream()
//												.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
//														sortOrder.getDirection() == SortDirection.ASCENDING))
//												.collect(Collectors.toList()))
//								.stream(),
//						query -> (int) FacadeProvider.getUserFacade().count(criteria));
		DataProvider<UserDto, UserCriteria> dataProvider = DataProvider
				.fromFilteringCallbacks(query -> FacadeProvider.getUserFacade()
						.getIndexList(query.getFilter().orElse(null), query.getOffset(), query.getLimit(),
								query.getSortOrders().stream()
										.map(sortOrder -> new SortProperty(sortOrder.getSorted(),
												sortOrder.getDirection() == SortDirection.ASCENDING))
										.collect(Collectors.toList()))
						.stream()
						, query -> {
							return (int) FacadeProvider.getUserFacade().count(query.getFilter().orElse(null));
						});
		grid.setDataProvider(dataProvider);
	}

	private void configureForm() {
		form = new UserForm(regions, provinces, districts);
		form.setSizeFull();
		form.addSaveListener(this::saveUser);
		form.addDeleteListener(this::deleteContact);
		form.addCloseListener(e -> closeEditor());
	}

	// TODO: Hide the filter bar on smaller screens
	public void addFilters() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setWidthFull();

		createUserButton.addClassName("createUserButton");
		createUserButton.getStyle().set("margin-left", "12px");
		layout.add(createUserButton);
		Icon createIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
		createUserButton.setIcon(createIcon);
		createUserButton.addClickListener(e -> {
		});

		exportUsersButton.addClassName("exportUsersButton");
		layout.add(exportUsersButton);

		exportUsersButton.addClickListener(e -> {
			// Retrieve the data from the Vaadin Grid
			String data = getDataAsString(grid);

			// Format the data in the desired format (e.g., CSV)
			String formattedData = formatDataAsCsv(data);

			// Create a temporary stream to write the formatted data
			InputStream stream = new ByteArrayInputStream(formattedData.getBytes(StandardCharsets.UTF_8));

			// Create a StreamResource to handle the file download
			StreamResource resource = new StreamResource("data.csv", () -> stream);

			// Trigger the file download in the user's browser
			resource.setContentType("text/csv");
			resource.setCacheTime(0);
//	            resource.setBufferSize(1024);

//	            FileDownloader downloader = new FileDownloader(resource);
//	            downloader.download();

			Anchor downloadLink = new Anchor(resource, "");
			downloadLink.getElement().setAttribute("download", true);
			downloadLink.getElement().getStyle().set("display", "none");

			Notification.show("File download initiated", 3000, Notification.Position.BOTTOM_CENTER)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});

		Icon exportUsersButtonIcon = new Icon(VaadinIcon.UPLOAD_ALT);
		exportUsersButton.setIcon(exportUsersButtonIcon);

		exportRolesButton.addClassName("exportRolesButton");
		Icon exportRolesButtonIcon = new Icon(VaadinIcon.USER_CHECK);
		exportRolesButton.setIcon(exportRolesButtonIcon);
		layout.add(exportRolesButton);

		bulkModeButton.addClassName("bulkActionButton");
		bulkModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		bulkModeButton.setIcon(bulkModeButtonnIcon);
		layout.add(bulkModeButton);

		bulkModeButton.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			bulkModeButton.setVisible(false);
			leaveBulkModeButton.setVisible(true);
			menuBar.setVisible(true);
		});

		leaveBulkModeButton.addClassName("leaveBulkActionButton");
		leaveBulkModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		leaveBulkModeButton.setVisible(false);
		Icon leaveBulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		leaveBulkModeButton.setIcon(leaveBulkModeButtonnIcon);
		layout.add(leaveBulkModeButton);

		leaveBulkModeButton.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.SINGLE);
			bulkModeButton.setVisible(true);
			leaveBulkModeButton.setVisible(false);
			menuBar.setVisible(false);
		});

		menuBar.setVisible(false);
		MenuItem item = menuBar.addItem(Captions.bulkActions);
		SubMenu subMenu = item.getSubMenu();
		subMenu.addItem(new Checkbox(Captions.actionEnable));
		subMenu.addItem(new Checkbox(Captions.actionDisable));
		menuBar.getStyle().set("margin-top", "5px");
		layout.add(menuBar);

		searchField.addClassName("searchField");
		searchField.setPlaceholder("Search Users");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> {
			
			String search = e.getValue();
			criteria.freeText(search);
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});
//		searchField.addValueChangeListener(e -> dataProvider.setFilter(search -> {
//
//			String searchTerm = searchField.getValue().trim();
//			
//			if (searchTerm.isEmpty())
//				return true;
//			
//			boolean matchUserRole = String.valueOf(search.getUserRoles()).toLowerCase()
//					.contains(searchTerm.toLowerCase());
//			boolean matchUsername = String.valueOf(search.getUserName()).toLowerCase()
//					.contains(searchTerm.toLowerCase());
//			boolean matchName = String.valueOf(search.getName()).toLowerCase().contains(searchTerm.toLowerCase());
//			boolean matchEmail = String.valueOf(search.getUserEmail()).toLowerCase().contains(searchTerm.toLowerCase());
//			boolean matchOrganisation = String.valueOf(search.getUserOrganisation()).toLowerCase()
//					.contains(searchTerm.toLowerCase());
//			boolean matchPosition = String.valueOf(search.getUserPosition()).toLowerCase()
//					.contains(searchTerm.toLowerCase());
//			boolean matchArea = String.valueOf(search.getArea()).toLowerCase()
//					.contains(searchTerm.toLowerCase());
//
//			return matchUserRole || matchUsername || matchName || matchEmail || matchOrganisation || matchPosition || matchArea;
//		}));

		layout.add(searchField);
		layout.setPadding(false);

		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setPadding(false);
		filterLayout.setVisible(false);
		filterLayout.setMargin(false);
		filterLayout.setAlignItems(Alignment.END);

		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		displayFilters = new Button("Show Filters", new Icon(VaadinIcon.SLIDERS));
		displayFilters.getStyle().set("margin-left", "10px");
		displayFilters.addClickListener(e -> {
			if (filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				displayFilters.setText("Hide Filters");
			} else {
				filterLayout.setVisible(false);
				displayFilters.setText("Show Filters");
			}
		});

		activeFilter = new ComboBox<String>();
		activeFilter.setId(UserDto.ACTIVE);
		activeFilter.setWidth(200, Unit.PIXELS);
		activeFilter.setLabel(I18nProperties.getCaption(Captions.User_active));
		activeFilter.setPlaceholder("Active");
		activeFilter.getStyle().set("margin-left", "12px");
		activeFilter.getStyle().set("margin-top", "12px");
		activeFilter.setItems(ACTIVE_FILTER, INACTIVE_FILTER);
		activeFilter.setClearButtonVisible(true);
		activeFilter.addValueChangeListener(e -> {
			boolean active;
			if(e.getValue().equals("Active" )) {
				active = true;
			} else {
				active = false;
			}
			
			criteria.active(active);
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});
//		activeFilter.addValueChangeListener(e -> {
//			dataView.removeFilters();
//
//			if (e.getValue() != null) {
//				dataView.addFilter(s -> {
//
//					String option = activeFilter.getValue().trim();
//
//					boolean matchActive = option.equals("Active") == (s.isActive() == true);
//					return matchActive;
//				});
//			}
//		});

		filterLayout.add(activeFilter);

		userRolesFilter = new ComboBox<UserRole>();
		userRolesFilter.setId(UserDto.USER_ROLES);
		userRolesFilter.setWidth(200, Unit.PIXELS);
		userRolesFilter.setLabel(I18nProperties.getPrefixCaption(UserDto.I18N_PREFIX, UserDto.USER_ROLES));
		userRolesFilter.setPlaceholder("User Roles");
		userRolesFilter.getStyle().set("margin-left", "12px");
		userRolesFilter.getStyle().set("margin-top", "12px");
		userRolesFilter.setClearButtonVisible(true);
		userRolesFilter
				.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
//		userRolesFilter.setItems(UserUiHelper.getAssignableRoles(Collections.emptySet()));
		userRolesFilter.addValueChangeListener(e -> {
			
			UserRole userRole = e.getValue();
			criteria.userRole(userRole);
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});
//		userRolesFilter.addValueChangeListener(e -> {
//			dataView.removeFilters();
//
//			if (e.getValue() != null) {
//
//				dataView.addFilter(s -> {
//
//					boolean matchRole = s.getUserRoles().contains(e.getValue());
//					return matchRole;
//				});
//			}
//		});

		filterLayout.add(userRolesFilter);

		areaFilter = new ComboBox<AreaReferenceDto>();
		areaFilter.setId(CaseDataDto.AREA);
		areaFilter.setWidth(200, Unit.PIXELS);
		areaFilter.setLabel(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.AREA));
		areaFilter.setPlaceholder("Region");
		areaFilter.getStyle().set("margin-left", "12px");
		areaFilter.getStyle().set("margin-top", "12px");
		areaFilter.setItems(regions);
		areaFilter.setClearButtonVisible(true);
		areaFilter.addValueChangeListener(e -> {
			
			AreaReferenceDto area = e.getValue();
			provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
			regionFilter.setItems(provinces);
			criteria.area(area);
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});
//		areaFilter.addValueChangeListener(e -> {
//			dataView.removeFilters();
//
//			if (e.getValue() != null) {
//				dataView.addFilter(s -> {
//
//					AreaReferenceDto areaValue = (AreaReferenceDto) e.getValue();
//
//					if (areaValue == null)
//						return true;
//
//					provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
//
//					regionFilter.setItems(provinces);
//					boolean predicate = s.getArea() == null ? false : areaValue.getUuid().equals(s.getArea().getUuid());
//
//					return predicate;
//				});
//			}
//		});

		filterLayout.add(areaFilter);

		regionFilter = new ComboBox<RegionReferenceDto>();
		regionFilter.setId(CaseDataDto.REGION);
		regionFilter.setWidth(200, Unit.PIXELS);
		regionFilter.setLabel(
				I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, I18nProperties.getCaption(Captions.region)));
		regionFilter.setPlaceholder("Province");
		regionFilter.getStyle().set("margin-left", "12px");
		regionFilter.getStyle().set("margin-top", "12px");
		regionFilter.setClearButtonVisible(true);
		regionFilter.addValueChangeListener(e -> {
			
			RegionReferenceDto region = e.getValue();
			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
			districtFilter.setItems(districts);
			criteria.region(region);
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});
//		regionFilter.addValueChangeListener(e -> dataView.addFilter(s -> {
//
//			RegionReferenceDto regionValue = (RegionReferenceDto) e.getValue();
//
//			if (regionValue == null)
//				return true;
//
//			districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
//			districtFilter.setItems(districts);
//
//			boolean predicate = s.getDistrict() == null ? false
//					: regionValue.getUuid().equals(s.getDistrict().getUuid());
//
//			return predicate;
//		}));

		filterLayout.add(regionFilter);

		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		districtFilter.setWidth(200, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder("District");
		districtFilter.getStyle().set("margin-left", "12px");
		districtFilter.getStyle().set("margin-top", "12px");
		districtFilter.setClearButtonVisible(true);
		districtFilter.addValueChangeListener(e -> {
			
			DistrictReferenceDto district = e.getValue();
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});
//		districtFilter.addValueChangeListener(e -> dataView.addFilter(s -> {
//
//			DistrictReferenceDto districtValue = (DistrictReferenceDto) e.getValue();
//
//			if (districtValue == null)
//				return true;
//			
//			communities =FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
//			communitiesFilter.setItems(communities);
//
//			boolean predicate = s.getCommunity() == null ? false 
//					: districtValue.getUuid().equals(s.getCommunity());
//
//			return predicate;
//		}));

		filterLayout.add(districtFilter);

		vlayout.add(displayFilters, filterLayout);
		add(layout, vlayout);
	}

	private String formatDataAsCsv(String data) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getDataAsString(Grid<UserDto> grid2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void editUser(UserDto user) {
		if (user == null) {
			closeEditor();
		} else {
						
			form.setUser(user);
			form.setVisible(true);
			form.setSizeFull();
			grid.setVisible(false);
			setFiltersVisible(false);
			displayFilters.setVisible(false);
			addClassName("editing");
		}
	}

	private void closeEditor() {
		form.setUser(null);
		form.setVisible(false);
		setFiltersVisible(true);
		grid.setVisible(true);
		
		removeClassName("editing");
	}

	private void setFiltersVisible(boolean state) {
		createUserButton.setVisible(state);
		exportUsersButton.setVisible(state);
		exportRolesButton.setVisible(state);
		bulkModeButton.setVisible(state);
		searchField.setVisible(state);
		activeFilter.setVisible(state);
		userRolesFilter.setVisible(state);
		areaFilter.setVisible(state);
		regionFilter.setVisible(state);
		districtFilter.setVisible(state);
	}

//	private void addContact() {
//		
//		grid.asSingleSelect().clear();
//		editUser(new UserDto());
//	}

	private void saveUser(UserForm.SaveEvent event) {
		FacadeProvider.getUserFacade().saveUser(event.getContact());
		// updateList();
		closeEditor();
		grid.getDataProvider().refreshAll();
	}

	private void deleteContact(UserForm.DeleteEvent event) {
		// FacadeProvider.getUserFacade(). .getContact());
		// updateList();
		closeEditor();
	}

	private VerticalLayout createDialogLayout() {

		VerticalLayout verticalLayoutMethod = new VerticalLayout();
		HorizontalLayout textFieldSet1 = new HorizontalLayout();

		H3 createUserSubHeading = new H3("Personal Information");
		verticalLayoutMethod.add(createUserSubHeading);

		TextField firstName = new TextField("First Name");
		firstName.setWidth("350px");
		firstName.isRequired();
		firstName.setRequiredIndicatorVisible(true);
		TextField lastName = new TextField("Last Name");
		lastName.setWidth("350px");
		textFieldSet1.add(firstName, lastName);
		verticalLayoutMethod.add(textFieldSet1);

		HorizontalLayout textFieldSet2 = new HorizontalLayout();

		TextField userEmail = new TextField("Email Address");
		userEmail.setWidth("350px");
		userEmail.setHelperText("Used to send Email Notification");
		TextField phone = new TextField("Phone Number");
		phone.setHelperText("Used to send SMS notification needs to contain Country code");
		phone.setWidth("350px");
		textFieldSet2.add(userEmail, phone);
		verticalLayoutMethod.add(textFieldSet2);

		HorizontalLayout textFieldSet3 = new HorizontalLayout();

		TextField userPosition = new TextField("Position");
		userPosition.setWidth("350px");
		TextField userOrganisation = new TextField("Organisation");
		userOrganisation.setWidth("350px");
		textFieldSet3.add(userPosition, userOrganisation);
		verticalLayoutMethod.add(textFieldSet3);

		ComboBox<Language> language = new ComboBox<>();
		language.setLabel("Language");
		language.setItems(Language.getAssignableLanguages());
		language.setWidth("350px");
		verticalLayoutMethod.add(language);

		H3 createUserSubHeading2 = new H3("Address");
//		createUserSubHeading2.getStyle().set("color", "green");
		verticalLayoutMethod.add(createUserSubHeading2);

		HorizontalLayout textFieldSet4 = new HorizontalLayout();
		ComboBox<AreaReferenceDto> region = new ComboBox<>();
		region.setLabel("Region");
		region.setItems(regions);
		region.setWidth("200px");
		textFieldSet4.add(region);

		verticalLayoutMethod.add(textFieldSet4);

		HorizontalLayout textFieldSet5 = new HorizontalLayout();
		ComboBox<RegionReferenceDto> province = new ComboBox<>();
		province.setLabel("Province");
		province.setItems(provinces);
		province.setWidth("200px");
		textFieldSet5.add(province);

		ComboBox<DistrictReferenceDto> district = new ComboBox<>();
		district.setLabel("District");
		district.setItems(districts);
		district.setWidth("200px");
		textFieldSet5.add(district);

		ComboBox<CommunityReferenceDto> cluster = new ComboBox<>();
		cluster.setLabel("Cluster");
//		cluster.setItems();
		cluster.setWidth("200px");
		textFieldSet5.add(cluster);

		verticalLayoutMethod.add(textFieldSet5);

		HorizontalLayout textFieldSet6 = new HorizontalLayout();
		TextField street = new TextField("Street");
		street.setPlaceholder("Enter street here");
		street.setWidth("230px");
		textFieldSet6.add(street);

		TextField houseNumber = new TextField("House Number");
		houseNumber.setPlaceholder("Enter House Number here");
		houseNumber.setWidth("230px");
		textFieldSet6.add(houseNumber);

		TextField additionalInformation = new TextField("Additional Information");
		additionalInformation.setPlaceholder("Enter Additional Information here");
		additionalInformation.setWidth("230px");
		textFieldSet6.add(additionalInformation);

		verticalLayoutMethod.add(textFieldSet6);

		HorizontalLayout textFieldSet7 = new HorizontalLayout();

		TextField postalCode = new TextField("Postal Code");
		postalCode.setPlaceholder("Enter postal Code here");
		postalCode.setWidth("230px");
		textFieldSet7.add(postalCode);

		TextField city = new TextField("City");
		city.setPlaceholder("Enter City here");
		city.setWidth("230px");
		textFieldSet7.add(city);

		ComboBox<AreaType> areaType = new ComboBox<>();
		areaType.setLabel("Area Type");
		areaType.setItems(AreaType.values());
		areaType.setWidth("230px");
		textFieldSet7.add(areaType);

		verticalLayoutMethod.add(textFieldSet7);

		HorizontalLayout textFieldSet8 = new HorizontalLayout();
		CheckboxGroup<UserType> userType = new CheckboxGroup<>();
		userType.setLabel("Type of Users");
		userType.setItems(UserType.values());
//		userType.select(UserType.COMMON_USER);
		textFieldSet8.add(userType);

		verticalLayoutMethod.add(textFieldSet8);

		VerticalLayout textFieldSet9 = new VerticalLayout();
		H3 createUserSubHeading3 = new H3("User Data");
		textFieldSet9.add(createUserSubHeading3);

		Checkbox active = new Checkbox();
		active.setLabel("Active?");
		active.getStyle().set("margin-bottom", "0px");
		active.setValue(true);
		textFieldSet9.add(active);

		TextField UserName = new TextField("Username");
		UserName.setWidth("350px");

		textFieldSet9.add(UserName);
		
		verticalLayoutMethod.add(textFieldSet9);

		HorizontalLayout textFieldSet10 = new HorizontalLayout();
		CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
		formAccess.setLabel("Forms Type");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		textFieldSet10.add(formAccess);

		CheckboxGroup<UserRole> userRole = new CheckboxGroup<>();
		userRole.setLabel("User Roles");
		userRole.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		textFieldSet10.add(userRole);

		verticalLayoutMethod.add(textFieldSet10);

		return verticalLayoutMethod;
	}

}