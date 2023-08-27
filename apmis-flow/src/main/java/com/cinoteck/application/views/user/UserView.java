package com.cinoteck.application.views.user;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.server.Page;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaType;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserHelper;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
//import de.symeda.sormas.ui.utils.DownloadUtil;

@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout {

//	public static final String ACTIVE_FILTER = I18nProperties.getString(Strings.active);
//	public static final String INACTIVE_FILTER = I18nProperties.getString(Strings.inactive);

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class, false);
	boolean overide = false;
	private ComboBox<String> activeFilter;
	private ComboBox<UserRole> userRolesFilter;
	private ComboBox<AreaReferenceDto> areaFilter;
	private ComboBox<RegionReferenceDto> regionFilter;
	private ComboBox<DistrictReferenceDto> districtFilter;
	public MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>();

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

	private UsersDataProvider usersDataProvider = new UsersDataProvider();
	private ConfigurableFilterDataProvider<UserDto, Void, UserCriteria> filterDataProvider;

	UserForm form;

	MenuBar menuBar = new MenuBar();

	Button createUserButton = new Button(I18nProperties.getCaption(Captions.userNewUser));
	Button exportUsersButton = new Button(I18nProperties.getCaption(Captions.export));
	Button exportRolesButton = new Button(I18nProperties.getCaption(Captions.exportUserRoles));
	Button bulkModeButton = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkModeButton = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	TextField searchField = new TextField();
	Button exportUsers = new Button("Export");

	Button displayFilters;

	private static final String CSV_FILE_PATH = "./result.csv";
	UserDto userDto;

	HorizontalLayout layout = new HorizontalLayout();
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Paragraph countRowItems;
	boolean isEditingMode;

	boolean isEditingModeActive;

	public UserView() {

		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		filterDataProvider = usersDataProvider.withConfigurableFilter();

//		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
//			bulkModeButton = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
//			leaveBulkModeButton = new Button();
//			menuBar = new MenuBar();
//		}

		setHeightFull();
		addFilters();
		configureGrid();
		configureForm(userDto);
		add(getContent());
		closeEditor();
	}

	public void addFilters() {
		criteria = new UserCriteria();

		int numberOfRows = filterDataProvider.size(new Query<>());
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + numberOfRows);
		countRowItems.setId("rowCount");

		layout.setMargin(true);
		layout.setPadding(false);
		layout.setWidthFull();

		createUserButton = new Button("New User");
		createUserButton.addClassName("createUserButton");
		createUserButton.getStyle().set("margin-left", "0.1rem");
		if (userProvider.hasUserRight(UserRight.USER_CREATE)) {
			layout.add(createUserButton);
		}
		Icon createIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
		createUserButton.setIcon(createIcon);
		createUserButton.addClickListener(e -> {
			editUser(false);
			isEditingModeActive = true;
		});

		exportUsers.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportUsers.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");

		});

		anchor.getStyle().set("display", "none");
		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			layout.add(exportUsers, anchor);
		}

//		layout.add(anchor);
		layout.addClassNames("row pl-4");

		leaveBulkModeButton.setText("Enter Bulk Edit Mode");
		bulkModeButton.addClassName("bulkActionButton");
//		bulkModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		bulkModeButton.setIcon(bulkModeButtonnIcon);

		bulkModeButton.addClickListener(e -> {
			grid.setSelectionMode(Grid.SelectionMode.MULTI);
			bulkModeButton.setVisible(false);
			leaveBulkModeButton.setVisible(true);
			menuBar.setVisible(true);
		});

		leaveBulkModeButton.setText("Leave Bulk Edit Mode");
		leaveBulkModeButton.addClassName("leaveBulkActionButton");
//		leaveBulkModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		leaveBulkModeButton.setVisible(false);
		Icon leaveBulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		leaveBulkModeButton.setIcon(leaveBulkModeButtonnIcon);
		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			layout.add(bulkModeButton);
			layout.add(leaveBulkModeButton);
		}
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

		layout.setPadding(false);

		HorizontalLayout filterLayout = new HorizontalLayout();

		filterLayout.setPadding(false);
		filterLayout.setVisible(false);
		filterLayout.setMargin(false);
		filterLayout.setAlignItems(Alignment.END);
		filterLayout.setWidthFull();
		HorizontalLayout vlayout = new HorizontalLayout();
		vlayout.setPadding(false);

		vlayout.setAlignItems(Alignment.END);

		displayFilters = new Button(I18nProperties.getCaption(Captions.showFilters), new Icon(VaadinIcon.SLIDERS));
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

		searchField.addClassName("searchField");
		searchField.setPlaceholder("Search Users");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.freeText(e.getValue());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
				updateRowCount();
			}
		});
		filterLayout.add(searchField);
		activeFilter = new ComboBox<String>();
		activeFilter.setId(UserDto.ACTIVE);
		activeFilter.setLabel(I18nProperties.getCaption(Captions.User_active));
		activeFilter.setPlaceholder("Active");
		activeFilter.getStyle().set("margin-left", "12px");
		activeFilter.getStyle().set("margin-top", "12px");
		activeFilter.setItems("Active", "Inactive");
		activeFilter.addValueChangeListener(e -> {

			if (e.getValue().equals("Active")) {
				criteria.active(true);
			} else if (e.getValue().equals("Inactive")) {
				criteria.active(false);
			}
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
			updateRowCount();

		});

		filterLayout.add(activeFilter);

		userRolesFilter = new ComboBox<UserRole>();
		userRolesFilter.setId(UserDto.USER_ROLES);
		userRolesFilter.setLabel(I18nProperties.getPrefixCaption(UserDto.I18N_PREFIX, UserDto.USER_ROLES));
		userRolesFilter.setPlaceholder("User Roles");
		userRolesFilter.getStyle().set("margin-left", "0.1rem");
		userRolesFilter.getStyle().set("padding-top", "0px!important");
		userRolesFilter.setClearButtonVisible(true);
		userRolesFilter
				.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		userRolesFilter.addValueChangeListener(e -> {

			UserRole userRole = e.getValue();
			criteria.userRole(userRole);
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
			updateRowCount();

		});

		filterLayout.add(userRolesFilter);

		areaFilter = new ComboBox<AreaReferenceDto>();
		areaFilter.setId(CaseDataDto.AREA);
		// areaFilter.setWidth(200, Unit.PIXELS);
		areaFilter.setLabel(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.AREA));
		areaFilter.setPlaceholder("Region");
		areaFilter.getStyle().set("margin-left", "0.1rem");
		areaFilter.getStyle().set("padding-top", "0px!important");
		areaFilter.setItems(regions);
		areaFilter.setClearButtonVisible(true);
		if (userProvider.getUser() != null && userProvider.getUser().getArea() != null) {
			areaFilter.setValue(userProvider.getUser().getArea());
			if (regionFilter != null) {
				regionFilter.clear();
				if (userProvider.getUser().getArea().getUuid() != null) {
					regionFilter.setItems(FacadeProvider.getRegionFacade()
							.getAllActiveByArea(userProvider.getUser().getArea().getUuid()));
				}
			}
			filterDataProvider.setFilter(criteria.area(userProvider.getUser().getArea()));
			areaFilter.setEnabled(false);

		}

		areaFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				AreaReferenceDto area = e.getValue();
				regionFilter.clear();
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				regionFilter.setItems(provinces);
				criteria.area(area);
				regionFilter.setReadOnly(false);
				districtFilter.clear();
				districtFilter.setReadOnly(true);
				criteria.region(null);
				criteria.district(null);
			} else {
				regionFilter.clear();
				regionFilter.setReadOnly(true);
				criteria.area(null);

			}
			filterDataProvider.setFilter(criteria);
			updateRowCount();

		});

		filterLayout.add(areaFilter);

		regionFilter = new ComboBox<RegionReferenceDto>();
		regionFilter.setId(CaseDataDto.REGION);
		regionFilter.setWidth(200, Unit.PIXELS);
		regionFilter.setLabel(
				I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, I18nProperties.getCaption(Captions.region)));
		regionFilter.setPlaceholder("Province");
		regionFilter.getStyle().set("margin-left", "0.1rem");
		regionFilter.getStyle().set("padding-top", "0px!important");
		regionFilter.setClearButtonVisible(true);
		if (userProvider.getUser() != null && userProvider.getUser().getRegion() != null) {
			regionFilter.setItems(userProvider.getUser().getRegion());
			regionFilter.setValue(userProvider.getUser().getRegion());
			if (districtFilter != null) {
				districtFilter.clear();
				if (userProvider.getUser().getRegion().getUuid() != null) {
					districtFilter.setItems(FacadeProvider.getDistrictFacade()
							.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid()));
				}
			}
			filterDataProvider.setFilter(criteria.region(userProvider.getUser().getRegion()));
			regionFilter.setEnabled(false);
		} else if (userProvider.getUser().getRegion() == null) {

		}

		regionFilter.addValueChangeListener(e -> {
			if (e.getValue() != null) {
				RegionReferenceDto region = e.getValue();
				districtFilter.clear();
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				districtFilter.setItems(districts);
				criteria.region(region);

				districtFilter.setReadOnly(false);
				criteria.district(null);
			} else {
				districtFilter.clear();
				districtFilter.setReadOnly(true);
				criteria.region(null);

			}
			filterDataProvider.setFilter(criteria);
			updateRowCount();

		});

		filterLayout.add(regionFilter);

		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		// districtFilter.setWidth(200, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder("District");
		districtFilter.getStyle().set("margin-left", "0.1rem");
		districtFilter.getStyle().set("padding-top", "0px!important");
		districtFilter.setClearButtonVisible(true);
		districtFilter.setReadOnly(true);
		if (userProvider.getUser() != null && userProvider.getUser().getDistrict() != null) {
			districtFilter.setItems(userProvider.getUser().getDistrict());

			districtFilter.setValue(userProvider.getUser().getDistrict());

			filterDataProvider.setFilter(criteria.region(userProvider.getUser().getRegion()));
			districtFilter.setEnabled(false);

		}
		districtFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				DistrictReferenceDto district = e.getValue();
				criteria.district(district);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();
				updateRowCount();

			} else {
				criteria.district(null);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();
				updateRowCount();

			}
		});
		HorizontalLayout coluntLay = new HorizontalLayout();
		coluntLay.setJustifyContentMode(JustifyContentMode.END);
		coluntLay.setWidth("20%");
		coluntLay.add(countRowItems);
		filterLayout.setClassName("row pl-3");
		filterLayout.add(districtFilter, coluntLay);
		vlayout.add(displayFilters, filterLayout);
		vlayout.setWidth("98%");
		add(layout, vlayout);
	}

	private void configureGrid() {

		ComponentRenderer<Span, UserDto> userRolesRenderer = new ComponentRenderer<>(reportModelDto -> {
			String value = String.valueOf(reportModelDto.getUserRoles()).replace("[", "").replace("]", "")
					.replace("null,", "").replace("null", "");
			Span label = new Span(value);
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

		Column<UserDto> activeColumn = grid.addColumn(activeRenderer)
				.setHeader(I18nProperties.getCaption(Captions.User_active)).setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> userRolesColumn = grid.addColumn(userRolesRenderer)
				.setHeader(I18nProperties.getCaption(Captions.User_userRoles)).setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> usernameColumn = grid.addColumn(UserDto::getUserName)
				.setHeader(I18nProperties.getCaption(Captions.User_userName)).setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> nameColumn = grid.addColumn(UserDto::getName)
				.setHeader(I18nProperties.getCaption(Captions.name)).setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> emailCoulmn = grid.addColumn(UserDto::getUserEmail)
				.setHeader(I18nProperties.getCaption(Captions.User_userEmail)).setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> userPositionColumn = grid.addColumn(UserDto::getUserPosition)
				.setHeader(I18nProperties.getCaption(Captions.User_userPosition)).setAutoWidth(true).setSortable(true)
				.setResizable(true);
		Column<UserDto> userOrgColumn = grid.addColumn(UserDto::getUserOrganisation)
				.setHeader(I18nProperties.getCaption(Captions.User_userOrganisation)).setAutoWidth(true)
				.setSortable(true).setResizable(true);
		Column<UserDto> userAreaColumn = grid.addColumn(UserDto::getArea)
				.setHeader(I18nProperties.getCaption(Captions.area)).setResizable(true).setAutoWidth(true)
				.setSortable(true);

		GridExporter<UserDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle(I18nProperties.getCaption(Captions.mainMenuUsers));
		exporter.setFileName("APMIS_Users" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

		exporter.setExportValue(activeColumn, p -> p.isActive() ? "Yes" : "No");
		exporter.setExportValue(userRolesColumn, p -> {
			Set<UserRole> value = p.getUserRoles();
			String valueString = value.toString();
			return value != null ? valueString : "";
		});
		exporter.setExportValue(usernameColumn, p -> {
			String valueString = p.getUserName();
			return valueString != null ? valueString : "";
		});
		exporter.setExportValue(nameColumn, p -> {
			String valueString = p.getName();
			return valueString != null ? valueString : "";
		});
		exporter.setExportValue(emailCoulmn, p -> {
			String valueString = p.getUserEmail();
			return valueString != null ? valueString : "";
		});
		exporter.setExportValue(userPositionColumn, p -> {
			String valueString = p.getUserPosition();
			return valueString != null ? valueString : "";
		});
		exporter.setExportValue(userOrgColumn, p -> {
			String valueString = p.getUserOrganisation();
			return valueString != null ? valueString : "";
		});

		exporter.setExportValue(userAreaColumn, p -> {
			AreaReferenceDto value = p.getArea();
			String valueString;
			if (value == null) {
				valueString = "";
			} else {
				valueString = value.toString();
			}
			return value != null ? valueString : "";
		});

		anchor.setHref(exporter.getCsvStreamResource());
		anchor.getElement().setAttribute("download", true);
		anchor.setClassName("exportJsonGLoss");
		Icon icon = VaadinIcon.UPLOAD_ALT.create();
		icon.getStyle().set("margin-right", "8px");
		icon.getStyle().set("font-size", "10px");

		anchor.getElement().insertChild(0, icon.getElement());

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setAllRowsVisible(false);

		grid.setDataProvider(filterDataProvider);

		if (userProvider.hasUserRight(UserRight.USER_EDIT)) {
			grid.addSelectionListener(event -> {
				editUser(event.getFirstSelectedItem(), true);
			});
		}

//		grid.asSingleSelect(e->{
//			editUser(e.getFirstSelectedItem(), true);
//		});

		return;

	}

	private void configureForm(UserDto user) {

		System.out.println(user + "userddddddddddddto in formconfigure");
		form = new UserForm(regions, provinces, districts, user);
		form.setSizeFull();
		form.addUserFieldValueChangeEventListener(this::suggestUserName);
//	    form.addResetPasswordListener(event -> resetUserPassword(event, user)); // Use the resetUserPassword method
		form.addResetPasswordListener(this::resetUserPassWord);
		form.addSaveListener(this::saveUser);
		form.addDeleteListener(this::deleteContact);
		form.addCloseListener(e -> {
			closeEditor();
			UI.getCurrent().getPage().reload();
		});

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

	public void editUser(Optional<UserDto> userr, boolean isEdMode) {
		UserDto user;
		isEditingMode = isEdMode;
		if (userr.isPresent()) {
			user = userr.get();
			form.setUser(user);
			form.setVisible(true);
			form.setSizeFull();
			grid.setVisible(false);
			setFiltersVisible(false);
			addClassName("editing");

		}
	}

	public void editUser(boolean isEdMode) {
		isEditingMode = isEdMode;

		UserDto user = new UserDto();
		form.setUser(user);
		form.addUserFieldValueChangeEventListener(this::suggestUserName);
		form.setVisible(true);
		form.setSizeFull();
		grid.setVisible(false);
		setFiltersVisible(false);

		isEditingModeActive = true;

		System.out.println(isEditingModeActive + "isEditingModeActive");
	}

	private void updateRowCount() {

		int numberOfRows = filterDataProvider.size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void closeEditor() {
		form.setVisible(false);
		setFiltersVisible(true);
		grid.setVisible(true);
		removeClassName("editing");
		form.setUser(new UserDto());
	}

	private void setFiltersVisible(boolean state) {
		displayFilters.setVisible(state);
		createUserButton.setVisible(state);
		exportUsersButton.setVisible(state);
		exportRolesButton.setVisible(state);
		bulkModeButton.setVisible(state);
		exportUsers.setVisible(state);
		searchField.setVisible(state);
		anchor.setVisible(state);
		activeFilter.setVisible(state);
		userRolesFilter.setVisible(state);
		areaFilter.setVisible(state);
		regionFilter.setVisible(state);
		countRowItems.setVisible(state);
		districtFilter.setVisible(state);
	}

	private void saveUser(UserForm.SaveEvent event) {

		UserDto dto = new UserDto();
		UserProvider userProvider = new UserProvider();

		if ((event.getContact().getUsertype() == null || event.getContact().getUsertype() != null)
				&& event.getSource().commusr.getValue() == true) {
			event.getContact().setUsertype(UserType.COMMON_USER);
		} else if ((event.getContact().getUsertype() == null || event.getContact().getUsertype() != null)
				&& event.getSource().commusr.getValue() == false) {

			event.getContact().setUsertype(userProvider.getUser().getUsertype());
		}
		System.out.println(event.getContact().getUsertype() + "event user type" + event.getSource().commusr.getValue());
		dto = FacadeProvider.getUserFacade().saveUser(event.getContact());

		if (!isEditingMode) {
			makeInitialPassword(dto.getUuid(), dto.getUserEmail(), dto.getUserName());

		}
		grid.getDataProvider().refreshAll();
		closeEditor();
	}

	private void resetUserPassWord(UserForm.ResetPasswordEvent event) {

		UserForm formLayout = (UserForm) event.getSource();

		System.out.println(isEditingModeActive + "___________isEditingModeActive");
		formLayout.suggestUserName(isEditingModeActive);
		if (isEditingModeActive) {
			event.getSource().lastName.addValueChangeListener(e -> {
				System.out.println("i kicked ___________isEditingModeActive");

				if (formLayout.userName.isEmpty()) {
					formLayout.userName.setValue(UserHelper.getSuggestedUsername(formLayout.firstName.getValue(),
							formLayout.lastName.getValue()));
				}
			});
		}

	}

	private void suggestUserName(UserForm.UserFieldValueChangeEvent event) {

		UserForm formLayout = (UserForm) event.getSource();

		System.out.println(isEditingModeActive + "___________isEditingModeActive");
		formLayout.suggestUserName(isEditingModeActive);
		if (isEditingModeActive) {
			event.getSource().resetUserPassword.addClickListener(e -> {
				System.out.println("i kicked ___________isEditingModeActive");
				ConfirmDialog resetDialog = new ConfirmDialog();
				resetDialog.setHeader("User Password Reset");
				

			});
		}

	}

	private void deleteContact(UserForm.DeleteEvent event) {

		closeEditor();
	}

	public void makeInitialPassword(String userUuid, String userEmail, String userName) {
		if (StringUtils.isBlank(userEmail)
				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {
			String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);

			Dialog newUserPop = new Dialog();
			newUserPop.setClassName("passwordsDialog");
			VerticalLayout infoLayout = new VerticalLayout();

			newUserPop.setHeaderTitle("New User Password");
			newUserPop.getElement().executeJs("this.$.overlay.setAttribute('theme', 'center');"); // Center the dialog
																									// content

			Paragraph infoText = new Paragraph("Please , copy this password, it is shown only once.");
			newUserPop.setHeaderTitle("New User Password");
			H3 username = new H3(I18nProperties.getCaption(Captions.Login_username) + " : " + userName);
			username.getStyle().set("color", "#0D6938");

			H3 password = new H3(I18nProperties.getCaption(Captions.Login_password) + " : " + newPassword);
			password.getStyle().set("color", "#0D6938");

			infoLayout.add(username, password);

			newUserPop.add(infoLayout);

			newUserPop.setOpened(true);
		}

	}

	public void makeNewPassword(String userUuid, String userEmail, String userName) {
		String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);

		if (StringUtils.isBlank(userEmail)
				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {

			Dialog newUserPop = new Dialog();
			newUserPop.setClassName("passwordsDialog");
			VerticalLayout infoLayout = new VerticalLayout();

			newUserPop.setHeaderTitle("New User Password");
			newUserPop.getElement().executeJs("this.$.overlay.setAttribute('theme', 'center');"); // Center the dialog
																									// content

			Paragraph infoText = new Paragraph("Please , copy this password, it is shown only once.");
			newUserPop.setHeaderTitle("New User Password");
			H3 username = new H3(I18nProperties.getCaption(Captions.Login_username) + " : " + userName);
			username.getStyle().set("color", "#0D6938");

			H3 password = new H3(I18nProperties.getCaption(Captions.Login_password) + " : " + newPassword);
			password.getStyle().set("color", "#0D6938");

			infoLayout.add(username, password);

			newUserPop.add(infoLayout);

			newUserPop.setOpened(true);
		}
	}

	public void enableUser(Collection<UserDto> selectedRows) {

		if (selectedRows.size() == 0) {

			Notification notification = Notification.show(I18nProperties.getString(Strings.headingNoUsersSelected) + " "
					+ I18nProperties.getString(Strings.messageNoUsersSelected));
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		} else {

			List<String> uuids = selectedRows.stream().map(UserDto::getUuid).collect(Collectors.toList());
			FacadeProvider.getUserFacade().enableUsers(uuids);
			System.out.println("Activated");

			Notification notification = Notification.show(I18nProperties.getString(Strings.headingUsersEnabled) + "  "
					+ I18nProperties.getString(Strings.messageUsersEnabled));
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		}
	}

	public void disableUser(Collection<UserDto> selectedRows) {

		if (selectedRows.size() == 0) {

			Notification notification = Notification.show(I18nProperties.getString(Strings.headingNoUsersSelected)
					+ "  " + I18nProperties.getString(Strings.messageNoUsersSelected));
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		} else {

			List<String> uuids = selectedRows.stream().map(UserDto::getUuid).collect(Collectors.toList());
			FacadeProvider.getUserFacade().disableUsers(uuids);
			System.out.println("Deactivated");

			Notification notification = Notification.show(I18nProperties.getString(Strings.headingUsersDisabled) + "  "
					+ I18nProperties.getString(Strings.messageUsersDisabled));
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		}
	}

}