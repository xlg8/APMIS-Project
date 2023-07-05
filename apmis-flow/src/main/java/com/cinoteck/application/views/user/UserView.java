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

import org.apache.commons.lang3.StringUtils;

import com.cinoteck.application.RowCount;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.data.provider.ListDataProvider;
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

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormDataIndexDto;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.area.AreaType;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
import de.symeda.sormas.api.utils.SortProperty;

@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout {

	public static final String ACTIVE_FILTER = I18nProperties.getString(Strings.active);
	public static final String INACTIVE_FILTER = I18nProperties.getString(Strings.inactive);

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class, false);

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
	GridExporter<UserDto> exporter = GridExporter.createFor(grid);
	UsersDataProvider usersDataProvider = new UsersDataProvider();
	ConfigurableFilterDataProvider<UserDto, Void, UserCriteria> filterDataProvider;
	UserForm form;
	CreateUserForm createUserForm;
	MenuBar menuBar = new MenuBar();

	Button createUserButton = new Button(Captions.userNewUser);
	Button exportUsersButton = new Button(Captions.export);
	Button exportRolesButton = new Button(Captions.exportUserRoles);
	Button bulkModeButton = new Button(Captions.actionEnterBulkEditMode);
	Button leaveBulkModeButton = new Button(Captions.actionLeaveBulkEditMode);
	TextField searchField = new TextField();

	Button displayFilters;

	UserProvider currentUser = new UserProvider();
	
	Anchor anchor;
	
	

	private static final String CSV_FILE_PATH = "./result.csv";

	public UserView() {

		Dialog dialog = new Dialog();
		dialog.setModal(true);
		dialog.addClassNames("dialog-alignment");
		dialog.setDraggable(true);
		dialog.setModal(false);
		dialog.setHeaderTitle("CREATE NEW USER");

		FormLayout dialogLayout = createDialogLayout();
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
//			createUserButton = new Button(Captions.userNewUser); //e -> dialog.open()); // event -> editUser(null));
		createUserButton.addClickListener(e -> {
			UserDto userDto = new UserDto();
			showNewUserForm(userDto);
		});

		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {

//				bulkModeButton = new Button(Captions.actionEnterBulkEditMode);
//				leaveBulkModeButton = new Button(Captions.actionLeaveBulkEditMode);
			menuBar = new MenuBar();
		}
		searchField = new TextField();

		setHeightFull();
		addFilters();
		configureGrid();
		configureForm();
		configureNewUserForm();
		add(getContent());
		closeEditor();
	}

	private Component getContent() {
		HorizontalLayout content = new HorizontalLayout();
		// content.setFlexGrow(2, grid);
		content.setFlexGrow(4, form);
		content.addClassNames("content");
		content.setSizeFull();
		content.add(grid, form, createUserForm);
		return content;
	}

	public void configureGrid() {

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

		this.criteria = new UserCriteria();
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(activeRenderer).setHeader("Active").setSortable(true).setResizable(true);
		grid.addColumn(userRolesRenderer).setHeader("User Roles").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserName).setHeader("Username").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getName).setHeader("Name").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserEmail).setHeader("Email").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserPosition).setHeader("Organisation").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserOrganisation).setHeader("Position").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getArea).setHeader("Area").setResizable(true).setSortable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setAllRowsVisible(false);

//		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
		filterDataProvider = usersDataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);

		grid.asSingleSelect().addValueChangeListener(event -> editUser(event.getValue()));
		
		
		GridExporter<UserDto> exporter = GridExporter.createFor(grid);

		exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
        exporter.setTitle("Region");
        exporter.setFileName("APMIS_Regions" + new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));

        
       
        
		add(grid);

	}


	private void configureForm() {
		form = new UserForm(regions, provinces, districts);
		form.setSizeFull();
		form.addSaveListener(this::saveUser);
		form.addDeleteListener(this::deleteContact);
		form.addCloseListener(e -> closeEditor());
	}

	private void configureNewUserForm() {
		createUserForm = new CreateUserForm();
		createUserForm.setSizeFull();
		createUserForm.setVisible(false);
		createUserForm.addSaveListener(this::saveNewUser);
		createUserForm.addCloseListener(e -> closeNewUserForm());
	}

	// TODO: Hide the filter bar on smaller screens
	public Component addFilters() {

		this.criteria = new UserCriteria();
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(false);
		layout.setPadding(false);
		layout.setWidthFull();

		createUserButton.addClassName("createUserButton");
		createUserButton.getStyle().set("margin-left", "12px");
		layout.add(createUserButton);
		Icon createIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
		createUserButton.setIcon(createIcon);

		exportUsersButton.addClassName("exportUsersButton");
		
//		layout.add(anchor);
		
        
		exportUsersButton.addClickListener(e -> {
//	
			
	        anchor.getElement().callFunction("click");

			Notification.show("File download initiated", 3000, Notification.Position.BOTTOM_CENTER)
					.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
		});
		
		layout.add(exportUsersButton);


		Icon exportUsersButtonIcon = new Icon(VaadinIcon.UPLOAD_ALT);
		exportUsersButton.setIcon(exportUsersButtonIcon);

		exportRolesButton.addClassName("exportRolesButton");
		Icon exportRolesButtonIcon = new Icon(VaadinIcon.USER_CHECK);
		exportRolesButton.setIcon(exportRolesButtonIcon);
		layout.add(exportRolesButton);

		bulkModeButton = new Button(Captions.actionEnterBulkEditMode);
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

			if (e.getValue() != null) {
				criteria.freeText(e.getValue());
				filterDataProvider.setFilter(criteria);
				

//				usersDataProvider.withConfigurableFilter().refreshAll();
			}
		});

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
		activeFilter.addValueChangeListener(e -> {

			if (e.getValue().equals(ACTIVE_FILTER)) {
				criteria.active(true);
			} else if (e.getValue().equals(INACTIVE_FILTER)) {
				criteria.active(false);
			}

			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
		});

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

		if (userProvider.getUser().getArea() == null) {
			areaFilter.setItems(regions);
			AreaReferenceDto area = (AreaReferenceDto) userProvider.getUser().getArea();
			areaFilter.setValue(area);
			criteria.area(area);
			filterDataProvider.setFilter(criteria);

			if (userProvider.getUser().getRegion() != null) {
				regionFilter.setItems(FacadeProvider.getRegionFacade()
						.getAllActiveByArea(userProvider.getUser().getArea().getUuid()));
			}
			areaFilter.setEnabled(false);
		} else {
			areaFilter.clear();
			areaFilter.setValue(userProvider.getUser().getArea());

			criteria.area(areaFilter.getValue());

			
			areaFilter.setEnabled(false);
		}
//		filterDataProvider.setFilter(criteria);
		

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
//		regionFilter.setReadOnly(false);
		
		if (userProvider.getUser().getRegion() == null) {
			regionFilter.setItems(provinces);
			RegionReferenceDto region = (RegionReferenceDto) userProvider.getUser().getRegion();
			regionFilter.setValue(region);
			criteria.region(region);
//			filterDataProvider.setFilter(criteria);

			if (userProvider.getUser().getDistrict() != null) {
				districtFilter.setItems(FacadeProvider.getDistrictFacade()
						.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid()));
			}
			regionFilter.setEnabled(false);
		} else {
			regionFilter.clear();
			regionFilter.setItems(userProvider.getUser().getRegion());
			regionFilter.setValue(userProvider.getUser().getRegion());

			criteria.region(userProvider.getUser().getRegion());
//			if (userProvider.getUser().getDistrict() != null) {
//				districtFilter.setItems(FacadeProvider.getDistrictFacade()
//						.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid()));
//			}
			usersDataProvider.withConfigurableFilter().setFilter(criteria.region(userProvider.getUser().getRegion()));
			regionFilter.setEnabled(false);
		}
		
		
		
		filterLayout.add(regionFilter);

		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		districtFilter.setWidth(200, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder("District");
		districtFilter.getStyle().set("margin-left", "12px");
		districtFilter.getStyle().set("margin-top", "12px");
		districtFilter.setClearButtonVisible(true);
//		districtFilter.setReadOnly(true);
		if (userProvider.getUser().getDistrict() == null) {
			districtFilter.setItems(districts);
			DistrictReferenceDto districts = (DistrictReferenceDto) userProvider.getUser().getDistrict();
			districtFilter.setValue(districts);
			criteria.district(districts);
//			filterDataProvider.setFilter(criteria);

			districtFilter.setEnabled(false);
		} else {
			districtFilter.clear();
			districtFilter.setItems(userProvider.getUser().getDistrict());
			districtFilter.setValue(userProvider.getUser().getDistrict());

			criteria.district(userProvider.getUser().getDistrict());
		
			usersDataProvider.withConfigurableFilter().setFilter(criteria.district(userProvider.getUser().getDistrict()));
			districtFilter.setEnabled(false);
		}
		areaFilter.addValueChangeListener(e -> {

			AreaReferenceDto area = e.getValue();

			regionFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid()));
			criteria.area(area);

			regionFilter.setReadOnly(false);

//			filterDataProvider.setFilter(criteria.area(area));
			criteria.district(userProvider.getUser().getDistrict());
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();

		});

		
		regionFilter.addValueChangeListener(e -> {
			//
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
					});

		
		
		districtFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				DistrictReferenceDto district = e.getValue();
				criteria.district(district);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();
			} else {
				criteria.district(null);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();

			}
		});

		filterLayout.add(districtFilter);

		vlayout.add(displayFilters, filterLayout);
		add(layout, vlayout);
		return vlayout;
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
			UserDto newUser = new UserDto();
			form.setUser(newUser);
			form.setVisible(true);
			form.setSizeFull();
			grid.setVisible(false);
//			setFiltersVisible(false);
		} else {
			form.setUser(user);
			form.setVisible(true);
			form.setSizeFull();
			grid.setVisible(false);
//			setFiltersVisible(false);
			addClassName("editing");
		}
	}

	public void showNewUserForm(UserDto user) {

		if (user == null) {
			user = new UserDto();
			createUserForm.setUser(user);
			createUserForm.setSizeFull();
			createUserForm.setVisible(true);
			form.setVisible(false);
			grid.setVisible(false);
			setFiltersVisible(false);
		} else {
			user = new UserDto();
			createUserForm.setVisible(true);
			createUserForm.setSizeFull();
			form.setVisible(false);
			setFiltersVisible(false);
			grid.setVisible(false);
		}
	}

	private void closeEditor() {

		form.setVisible(false);
//		setFiltersVisible(true);
		grid.setVisible(true);
		removeClassName("editing");
		form.setUser(new UserDto());
	}

	private void closeNewUserForm() {

		setFiltersVisible(true);
		grid.setVisible(true);
		form.setVisible(false);
		form.setUser(new UserDto());
		createUserForm.setVisible(false);
		removeClassName("editing");
	}

	private void setFiltersVisible(boolean state) {

		createUserButton.setVisible(state);
		exportUsersButton.setVisible(state);
		exportRolesButton.setVisible(state);
//		bulkModeButton.setVisible(state);
		searchField.setVisible(state);
		activeFilter.setVisible(state);
		userRolesFilter.setVisible(state);
		areaFilter.setVisible(state);
		regionFilter.setVisible(state);
		;
		districtFilter.setVisible(state);
		;
//		displayFilters.setVisible(state);
	}

//	private void addContact() {
//
//		grid.asSingleSelect().clear();
//		editUser(new UserDto());
//	}

	private void saveUser(UserForm.SaveEvent event) {
		FacadeProvider.getUserFacade().saveUser(event.getContact());
//		 updateList();
		grid.getDataProvider().refreshAll();
		closeEditor();
	}

	private void saveNewUser(CreateUserForm.SaveEvent event) {

		UserDto dto = new UserDto();
		dto = FacadeProvider.getUserFacade().saveUser(event.getUser());
		makeInitialPassword(dto.getUuid(), dto.getUserEmail(), dto.getUserName());
		closeNewUserForm();
	}

	private void deleteContact(UserForm.DeleteEvent event) {
		// FacadeProvider.getUserFacade(). .getContact());
		// updateList();
		closeEditor();
	}

	public void makeInitialPassword(String userUuid, String userEmail, String userName) {
		if (StringUtils.isBlank(userEmail)
				|| AuthProvider.getProvider(FacadeProvider.getConfigFacade()).isDefaultProvider()) {
			String newPassword = FacadeProvider.getUserFacade().resetPassword(userUuid);

			Dialog newUserPop = new Dialog();
			newUserPop.setHeaderTitle("New User Password");
			newUserPop.add(userEmail);
			newUserPop.add(newPassword);
			newUserPop.setOpened(true);
		}
//		else {
//			showAccountCreatedSuccessful();
//		}
	}

	private FormLayout createDialogLayout() {

		UserForm userForm = new UserForm(regions, provinces, districts);

		FormLayout formLayout = new FormLayout();

		formLayout.add(userForm);

		H3 createUserSubHeading = new H3("Personal Information");
		formLayout.setColspan(createUserSubHeading, 2);
		// formLayout.add(createUserSubHeading);

		TextField firstName = new TextField("First Name");
		firstName.isRequired();
		// formLayout.add(firstName);

		TextField lastName = new TextField("Last Name");
		lastName.isRequired();
		// formLayout.add(lastName);

		TextField userEmail = new TextField("Email Address");
		userEmail.setHelperText("Used to send Email Notification");
		// formLayout.add(userEmail);

		TextField phone = new TextField("Phone Number");
		phone.setHelperText("Used to send SMS notification needs to contain Country code");
		// formLayout.add(phone);

		TextField userPosition = new TextField("Position");
		TextField userOrganisation = new TextField("Organisation");
		// formLayout.add(userPosition, userOrganisation);

		ComboBox<Language> language = new ComboBox<>();
		language.setLabel("Language");
		language.setItems(Language.getAssignableLanguages());
		// formLayout.add(language);

		H3 createUserSubHeading2 = new H3("Address");
		formLayout.setColspan(createUserSubHeading2, 2);
		// formLayout.add(createUserSubHeading2);

		ComboBox<AreaReferenceDto> region = new ComboBox<>();
		region.setLabel("Region");
		region.setItems(regions);
		// formLayout.add(region);

		ComboBox<RegionReferenceDto> province = new ComboBox<>();
		province.setLabel("Province");
		province.setItems(provinces);
		// formLayout.add(province);

		ComboBox<DistrictReferenceDto> district = new ComboBox<>();
		district.setLabel("District");
		district.setItems(districts);
		// formLayout.add(district);

		ComboBox<CommunityReferenceDto> cluster = new ComboBox<>();
		cluster.setLabel("Cluster");
		cluster.setItems();
		// formLayout.add(cluster);

		TextField street = new TextField("Street");
		street.setPlaceholder("Enter street here");
		// formLayout.add(street);

		TextField houseNumber = new TextField("House Number");
		houseNumber.setPlaceholder("Enter House Number here");
		// formLayout.add(houseNumber);

		TextField additionalInformation = new TextField("Additional Information");
		additionalInformation.setPlaceholder("Enter Additional Information here");
		// formLayout.add(additionalInformation);

		TextField postalCode = new TextField("Postal Code");
		postalCode.setPlaceholder("Enter postal Code here");
		// formLayout.add(postalCode);

		TextField city = new TextField("City");
		city.setPlaceholder("Enter City here");
		// formLayout.add(city);

		ComboBox<AreaType> areaType = new ComboBox<>();
		areaType.setLabel("Area Type");
		areaType.setItems(AreaType.values());
		// formLayout.add(areaType);

		H3 createUserSubHeading3 = new H3("User Data");
		formLayout.setColspan(createUserSubHeading3, 2);
		// formLayout.add(createUserSubHeading3);

		TextField userName = new TextField("Username");
		userName.isRequired();
		userName.setRequiredIndicatorVisible(true);
		// formLayout.add(userName);

		Checkbox active = new Checkbox();
		active.setLabel("Active?");
		formLayout.setColspan(active, 2);
		active.setValue(true);
		// formLayout.add(active);

		CheckboxGroup<UserType> userType = new CheckboxGroup<>();
		userType.setLabel("Type of Users");
		userType.setItems(UserType.values());
		formLayout.setColspan(userType, 2);
		// formLayout.add(userType);

		CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
		formAccess.setLabel("Forms Access");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		formAccess.isRequired();
		formAccess.setRequiredIndicatorVisible(true);
		// formLayout.add(formAccess);

		CheckboxGroup<UserRole> userRole = new CheckboxGroup<>();
		userRole.setLabel("User Roles");
		userRole.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		userRole.isRequired();
		userRole.setRequiredIndicatorVisible(true);
		// formLayout.add(userRole);

		formLayout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));

		binder.forField(firstName).asRequired("First Name is Required").bind(UserDto::getFirstName,
				UserDto::setFirstName);

		binder.forField(lastName).asRequired("Last Name is Required").bind(UserDto::getLastName, UserDto::setLastName);

		binder.forField(userEmail).asRequired("Last Name is Required").bind(UserDto::getUserEmail,
				UserDto::setUserEmail);

		binder.forField(phone).withValidator(e -> e.length() >= 10, "Enter a valid Phone Number")
				.bind(UserDto::getPhone, UserDto::setPhone);

		binder.forField(userPosition).bind(UserDto::getUserPosition, UserDto::setUserPosition);

		binder.forField(userOrganisation).bind(UserDto::getUserOrganisation, UserDto::setUserOrganisation);

		binder.forField(region).bind(UserDto::getArea, UserDto::setArea);
		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
		region.setItems(regions);
		region.setItemLabelGenerator(AreaReferenceDto::getCaption);
		region.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				province.setItems(provinces);
			}
		});

		binder.forField(province).bind(UserDto::getRegion, UserDto::setRegion);
		province.setItemLabelGenerator(RegionReferenceDto::getCaption);
		province.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
				district.setItems(districts);
			}
		});

		binder.forField(district).bind(UserDto::getDistrict, UserDto::setDistrict);
		district.setItemLabelGenerator(DistrictReferenceDto::getCaption);
		district.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				communities = FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid());
				community.setItemLabelGenerator(CommunityReferenceDto::getCaption);
				community.setItems(communities);
			}
		});

		binder.forField(community).bind(UserDto::getCommunity, UserDto::setCommunity);

//		// TODO: Change implemenation to only add assignable roles sormas style.
		userRole.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		binder.forField(userRole).asRequired("User Role is Required").bind(UserDto::getUserRoles,
				UserDto::setUserRoles);
		formLayout.setColspan(userRole, 1);
//		userRole.addValueChangeListener(e -> updateFieldsByUserRole(e.getValue()));

		formAccess.setLabel("Form Access");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		binder.forField(formAccess).bind(UserDto::getFormAccess, UserDto::setFormAccess);

		binder.forField(active).bind(UserDto::isActive, UserDto::setActive);

		userType.setItems(UserType.values());

		language.setItemLabelGenerator(Language::toString);
		language.setItems(Language.getAssignableLanguages());
		binder.forField(language).asRequired("Language is Required").bind(UserDto::getLanguage, UserDto::setLanguage);

		return formLayout;
	}

}