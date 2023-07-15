package com.cinoteck.application.views.user;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.utils.ExportEntityName;
import com.flowingcode.vaadin.addons.gridexporter.GridExporter;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
//import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
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
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;
//import de.symeda.sormas.ui.utils.DownloadUtil;
//

@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout {

	public static final String ACTIVE_FILTER = I18nProperties.getString(Strings.active);
	public static final String INACTIVE_FILTER = I18nProperties.getString(Strings.inactive);

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
	private GridListDataView<UserDto> filterDataProvider = grid.setItems(usersData);
//	private UsersDataProvider usersDataProvider = new UsersDataProvider();
//	private ConfigurableFilterDataProvider<UserDto, Void, UserCriteria> filterDataProvider;
//	private GridDataView<UserDto> dataViews = grid.setItems(filterDataProvider);

	UserForm form;
//	CreateUserForm createUserForm;

	MenuBar menuBar = new MenuBar();

	Button createUserButton = new Button("New User");
	Button exportUsersButton = new Button("Export");
	Button exportRolesButton = new Button("Export User Roles");
	Button bulkModeButton = new Button("Enter Bulk Edit Mode");
	Button leaveBulkModeButton = new Button("Leave Bulk Edit");
	TextField searchField = new TextField();

	Button displayFilters;

	private static final String CSV_FILE_PATH = "./result.csv";
	UserDto userDto;

	HorizontalLayout layout = new HorizontalLayout();
	Anchor anchor = new Anchor("", "Export");

	boolean isEditingMode;

	public UserView() {
	//	filterDataProvider = usersDataProvider.withConfigurableFilter();

		
		
		// {
//
//			Dialog dialog = new Dialog();
//			dialog.setModal(true);
//			dialog.addClassNames("dialog-alignment");
//			dialog.setDraggable(true);
//			dialog.setModal(false);
//			dialog.setHeaderTitle("CREATE NEW USER");
//
//			FormLayout dialogLayout = createDialogLayout();
//			dialog.add(dialogLayout);
//
//			Button closeButton = new Button(new Icon("lumo", "cross"), (e) -> dialog.close());
//			closeButton.getStyle().set("color", "green");
//			dialog.getHeader().add(closeButton);
//
//			Button cancelButton = new Button("DISCARD CHANGES", e -> dialog.close());
//			cancelButton.setHeightFull();
//			Button saveButton = new Button("SAVE");
//			saveButton.setHeightFull();
//			saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//
//			dialog.getFooter().add(cancelButton, saveButton);
////			createUserButton = new Button(Captions.userNewUser); //e -> dialog.open()); // event -> editUser(null));
//			createUserButton.addClickListener(e -> {
//				UserDto userDto = new UserDto();
//				editUser(userDto);
//			});
//
//			if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
//
////				bulkModeButton = new Button(Captions.actionEnterBulkEditMode);
////				leaveBulkModeButton = new Button(Captions.actionLeaveBulkEditMode);
//				menuBar = new MenuBar();
//			}
//			searchField = new TextField();
//		}
		if (userProvider.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			bulkModeButton = new Button("Enter Bulk Edit Mode");
			leaveBulkModeButton = new Button();
			menuBar = new MenuBar();
		}

		setHeightFull();
		addFilters();
		configureGrid();
		configureForm(userDto);
//		configureNewUserForm();
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

		Column<UserDto> activeColumn = grid.addColumn(activeRenderer).setHeader("Active").setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> userRolesColumn = grid.addColumn(userRolesRenderer).setHeader("User Roles").setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> usernameColumn = grid.addColumn(UserDto::getUserName).setHeader("Username").setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> nameColumn = grid.addColumn(UserDto::getName).setHeader("Name").setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> emailCoulmn = grid.addColumn(UserDto::getUserEmail).setHeader("Email").setSortable(true).setAutoWidth(true)
				.setResizable(true);
		Column<UserDto> userPositionColumn = grid.addColumn(UserDto::getUserPosition).setHeader("Organisation").setAutoWidth(true)
				.setSortable(true).setResizable(true);
		Column<UserDto> userOrgColumn = grid.addColumn(UserDto::getUserOrganisation).setHeader("Position").setAutoWidth(true)
				.setSortable(true).setResizable(true);
		Column<UserDto> userAreaColumn = grid.addColumn(UserDto::getArea).setHeader("Area").setResizable(true).setAutoWidth(true)
				.setSortable(true);
		GridExporter<UserDto> exporter = GridExporter.createFor(grid);
		exporter.setAutoAttachExportButtons(false);
		exporter.setTitle("Users");
		exporter.setFileName("APMIS_Users" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

//		ValueProvider<UserDto, String> userRolesValueProvider = (reportModelDto -> {
//			String value = String.valueOf(reportModelDto.getUserRoles()).replace("[", "").replace("]", "")
//					.replace("null,", "").replace("null", "");
//
//			return value;
//		});

//		ValueProvider<UserDto, String> activeValueProvider = (input -> {
//			boolean value = input.isActive();
//			if (value == true) {
//				String valuex = String.valueOf(input.isActive()).replace("true", "Active").replace("True", "Active");
//			}
//			else {
//				String valuex = String.valueOf(input.isActive())
//					.replace("false,", "Inactive").replace("False", "Inactive").replace("null", "Inactive");
//			}
//		return "";
//		});

//		exporter.setExportValue(activeColumn, p -> p.isActive() ? "Yes" : "No");
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
		//grid.setDataProvider(filterDataProvider);
		
		//grid.setItems(usersData);


		grid.addSelectionListener(event -> {
			System.out.println("__________cvcv______________"+event.getFirstSelectedItem());
			editUser(event.getFirstSelectedItem(), true);
		});

		return;

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
		form.setVisible(true);
		form.setSizeFull();
		grid.setVisible(false);
		setFiltersVisible(false);
	} 
	
	

	private void configureForm(UserDto user) {

		System.out.println(user + "userddddddddddddto in formconfigure");
		form = new UserForm(regions, provinces, districts);
		form.setSizeFull();

		form.addSaveListener(this::saveUser);

		form.addDeleteListener(this::deleteContact);
		form.addCloseListener(e -> closeEditor());
	}
//
	
//	private void configureNewUserForm() {
//		createUserForm = new CreateUserForm();
//		createUserForm.setSizeFull();
//		createUserForm.addSaveListener(this::saveNewUser);
////		createUserForm.addDeleteListener(this::deleteContact);
//		createUserForm.addCloseListener(e -> closeEditor());
//	}

	// TODO: Hide the filter bar on smaller screens
	public void addFilters() {
		criteria = new UserCriteria();

		layout.setMargin(false);
		layout.setPadding(false);
		layout.setWidthFull();

		createUserButton = new Button(I18nProperties.getCaption(Captions.userNewUser));
		createUserButton.addClassName("createUserButton");
		createUserButton.getStyle().set("margin-left", "12px");
		layout.add(createUserButton);
		Icon createIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
		createUserButton.setIcon(createIcon);
		createUserButton.addClickListener(e -> {
			editUser(false);
//			showNewUserForm(userDto);
		});
//
//		exportUsersButton.addClassName("exportUsersButton");
//		layout.add(exportUsersButton);
		layout.add(anchor);
//		Icon exportUsersButtonIcon = new Icon(VaadinIcon.UPLOAD_ALT);
//		exportUsersButton.setIcon(exportUsersButtonIcon);

//		exportRolesButton.addClassName("exportRolesButton");
//		Icon exportRolesButtonIcon = new Icon(VaadinIcon.USER_CHECK);
//		exportRolesButton.setIcon(exportRolesButtonIcon);
//		layout.add(exportRolesButton);
//		
//		
//		exportRolesButton.addClickListener(e->{
//			overide = true;
//			try {
//				String documentPath =	FacadeProvider.getUserRightsFacade().generateUserRightsDocument(overide);
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//});

//		
//		AbstractComponent exportUserRightsButton ;
//			
//		new FileDownloader(new StreamResource(() -> new DownloadUtil.DelayedInputStream((out) -> {
//			try {
//				String documentPath = FacadeProvider.getUserRightsFacade().generateUserRightsDocument(true);
//				IOUtils.copy(Files.newInputStream(new File(documentPath).toPath()), out);
//			} catch (IOException e) {
//				LoggerFactory.getLogger(DownloadUtil.class).error(e.getMessage(), e);
//				new Notification(
//					I18nProperties.getString(Strings.headingExportUserRightsFailed),
//					I18nProperties.getString(Strings.messageUserRightsExportFailed),
//					Notification.Type.ERROR_MESSAGE,
//					false).show(Page.getCurrent());
//			}
//		}, (e) -> {
//		}), createFileNameWithCurrentDate(ExportEntityName.USER_ROLES, ".xlsx"),
//				ACTIVE_FILTER));

		leaveBulkModeButton.setText("Enter Bulk Edit Mode");
		bulkModeButton.addClassName("bulkActionButton");
//		bulkModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		Icon bulkModeButtonnIcon = new Icon(VaadinIcon.CLIPBOARD_CHECK);
		bulkModeButton.setIcon(bulkModeButtonnIcon);
		layout.add(bulkModeButton);

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

//		layout.add(searchField);
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

		searchField.addClassName("searchField");
		searchField.setPlaceholder("Search Users");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
//		searchField.addValueChangeListener(e -> {
//
//			if (e.getValue() != null) {
//				criteria.freeText(e.getValue());
//			//	filterDataProvider.setFilter(criteria);
//				filterDataProvider.refreshAll();
//			}
//		});
		
searchField.addValueChangeListener(e -> filterDataProvider.addFilter(search -> {
			
			String searchTerm = searchField.getValue().trim();

          boolean matchUsername = String.valueOf(search.getUserName()).toLowerCase().contains(searchTerm.toLowerCase());
          boolean matchName = String.valueOf(search.getName()).toLowerCase().contains(searchTerm.toLowerCase());
          boolean matchEmail = String.valueOf(search.getUserEmail()).toLowerCase().contains(searchTerm.toLowerCase());
          boolean matchOrganisation = String.valueOf(search.getUserOrganisation()).toLowerCase().contains(searchTerm.toLowerCase());
          boolean matchPosition = String.valueOf(search.getUserPosition()).toLowerCase().contains(searchTerm.toLowerCase());
          
          return matchUsername || matchName || matchEmail || matchOrganisation || matchPosition;
		}));  


		filterLayout.add(searchField);
		activeFilter = new ComboBox<String>();
		activeFilter.setId(UserDto.ACTIVE);
		activeFilter.setWidth(200, Unit.PIXELS);
		activeFilter.setLabel(I18nProperties.getCaption(Captions.User_active));
		activeFilter.setPlaceholder("Active");
		activeFilter.getStyle().set("margin-left", "12px");
		activeFilter.getStyle().set("margin-top", "12px");
		activeFilter.setItems(ACTIVE_FILTER, INACTIVE_FILTER);
//		activeFilter.addValueChangeListener(e -> {
//
//			if (e.getValue().equals(ACTIVE_FILTER)) {
//				criteria.active(true);
//			} else if (e.getValue().equals(INACTIVE_FILTER)) {
//				criteria.active(false);
//			}
//
//		//	filterDataProvider.setFilter(criteria);
//			filterDataProvider.refreshAll();
//		});
		
		activeFilter.addValueChangeListener(e -> filterDataProvider.addFilter(s -> {
			
			String option = activeFilter.getValue().trim();	
			
			boolean matchActive = option.equals("Active") == (s.isActive() == true);
			return matchActive;				
		}));
		

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
//		userRolesFilter.addValueChangeListener(e -> {
//
//			UserRole userRole = e.getValue();
//			criteria.userRole(userRole);
//		//	filterDataProvider.setFilter(criteria);
//			filterDataProvider.refreshAll();
//		});
		
		
		userRolesFilter.addValueChangeListener(e -> filterDataProvider.addFilter(s -> {	
			
			boolean predicate = s.getUserRoles() == null ? false : s.getUserRoles().toString().toLowerCase().contains(e.getValue().toString().toLowerCase()); //areaValue.getUuid().equals(s.getArea().getUuid());
			
			System.out.println(predicate+ ": "+e.getValue().toString()+"  ~==  "+ String.valueOf(s.getUserRoles()).toLowerCase());
			
			
			return predicate;
		
			//boolean matchRole = String.valueOf(s.getUserRoles()).toLowerCase().contains(option.toLowerCase());																				
			//return matchRole;	
//			
			
//			if(e.getValue() != null) {
//				filterDataProvider.addFilter(s -> {	
//		
//			System.out.println(e.getValue() != null);
//			
//				if(e.getValue().name() != null) {
//					System.out.println(e.getValue().name());
//					String option = e.getValue().name();
//					
//					boolean matchRole = String.valueOf(s.getUserRoles()).toLowerCase().contains(option.toLowerCase());																				
//					return matchRole;	
//				}else {
//					return false;
//				}
//				
//				}); 
//			} else {
//				
//				filterDataProvider.addFilter(s -> {	
//						
//					return true;	
//				});
//				
//				
//			}
		}));

		
		
		

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
		if (userProvider.getUser() != null && userProvider.getUser().getArea() != null) {
			areaFilter.setValue(userProvider.getUser().getArea());
			if (regionFilter != null) {
				regionFilter.clear();
				if (userProvider.getUser().getArea().getUuid() != null) {
					regionFilter.setItems(FacadeProvider.getRegionFacade()
							.getAllActiveByArea(userProvider.getUser().getArea().getUuid()));
				}
			}
		//	filterDataProvider.setFilter(criteria.area(userProvider.getUser().getArea()));
			areaFilter.setEnabled(false);
		}
		
		areaFilter.addValueChangeListener(e -> filterDataProvider.addFilter(s -> {					
			
			AreaReferenceDto areaValue = (AreaReferenceDto) areaFilter.getValue();
				
//			if (areaValue == null)
//					return true;
			if (e.getValue() != null) {
				boolean predicate = s.getArea() == null ? false : areaValue.getUuid().equals(s.getArea().getUuid());
				
				regionFilter.clear();
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
				regionFilter.setItems(provinces);
				regionFilter.setReadOnly(false);
				districtFilter.clear();
				districtFilter.setReadOnly(true);
				
					return predicate;
			} else {
				regionFilter.clear();
				regionFilter.setReadOnly(true);
				
				districtFilter.clear();
				districtFilter.setReadOnly(true);
			
				return true;
			}
			}));
		
//		
//
//		areaFilter.addValueChangeListener(e -> {
//
//			if (e.getValue() != null) {
//				AreaReferenceDto area = e.getValue();
//				regionFilter.clear();
//				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());
//				regionFilter.setItems(provinces);
//				criteria.area(area);
//				regionFilter.setReadOnly(false);
//				districtFilter.clear();
//				districtFilter.setReadOnly(true);
//				criteria.region(null);
//				criteria.district(null);
//			} else {
//				regionFilter.clear();
//				regionFilter.setReadOnly(true);
////				AreaReferenceDto area = new AreaReferenceDto();
//				criteria.area(null);
//
//			}
//		//	filterDataProvider.setFilter(criteria);
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
//		regionFilter.setReadOnly(true);
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
		//	filterDataProvider.setFilter(criteria.region(userProvider.getUser().getRegion()));
			regionFilter.setEnabled(false);
		} else if (userProvider.getUser().getRegion() == null) {
//				regionFilter.clear();
//				regionFilter.setItems(FacadeProvider.getRegionFacade().getAllActiveByArea(areaFilter.getValue().getUuid()));

		}

		
		regionFilter.addValueChangeListener(e -> filterDataProvider.addFilter(search -> {					
			
			String option = areaFilter.getValue().toString();
				
				if (e.getValue().toString() != null)
					
					districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().toString());
//					districtFilter.setEnabled(true);
					districtFilter.setItems(districts);
				
					return true;
			}));
			
			filterLayout.add(regionFilter);
			
//		}
			
//			
//		regionFilter.addValueChangeListener(e -> {
//			if (e.getValue() != null) {
//				RegionReferenceDto region = e.getValue();
//				districtFilter.clear();
//				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
//				districtFilter.setItems(districts);
//				criteria.region(region);
//
//				districtFilter.setReadOnly(false);
//				criteria.district(null);
//			} else {
//				districtFilter.clear();
//				districtFilter.setReadOnly(true);
//				criteria.region(null);
//
//			}
//		//	filterDataProvider.setFilter(criteria);
//		});

		filterLayout.add(regionFilter);

		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		districtFilter.setWidth(200, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder("District");
		districtFilter.getStyle().set("margin-left", "12px");
		districtFilter.getStyle().set("margin-top", "12px");
		districtFilter.setClearButtonVisible(true);
		districtFilter.setReadOnly(true);
		if (userProvider.getUser() != null && userProvider.getUser().getDistrict() != null) {
			districtFilter.setItems(userProvider.getUser().getDistrict());

			districtFilter.setValue(userProvider.getUser().getDistrict());

		//	filterDataProvider.setFilter(criteria.region(userProvider.getUser().getRegion()));
			districtFilter.setEnabled(false);

		}
		districtFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				DistrictReferenceDto district = e.getValue();
				criteria.district(district);
			//	filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();
			} else {
				criteria.district(null);
			//	filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();

			}
		});

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

//	protected String createFileNameWithCurrentDate(ExportEntityName entityName, String fileExtension) {
//		return DownloadUtil.createFileNameWithCurrentDate(entityName, fileExtension);
//	}

	private void closeEditor() {

		form.setVisible(false);
		setFiltersVisible(true);
		grid.setVisible(true);
		removeClassName("editing");
		form.setUser(new UserDto());
	}

	private void closeNewUserForm() {

		setFiltersVisible(true);
		grid.setVisible(true);
		form.setVisible(false);
		form.setUser(new UserDto());
//		createUserForm.setVisible(false);
		removeClassName("editing");
	}

	private void setFiltersVisible(boolean state) {
		displayFilters.setVisible(state);
		createUserButton.setVisible(state);
		exportUsersButton.setVisible(state);
		exportRolesButton.setVisible(state);
		bulkModeButton.setVisible(state);
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
		System.out.println(isEditingMode + "eeeeeeeeeeeddddddddddddddittttttttttt");

		UserDto dto = new UserDto();
		dto = FacadeProvider.getUserFacade().saveUser(event.getContact());

		if (!isEditingMode) {
			makeInitialPassword(dto.getUuid(), dto.getUserEmail(), dto.getUserName());

		}
//		//		 updateList();
		grid.getDataProvider().refreshAll();
		
		closeEditor();
	}

	private void saveNewUser(UserForm.SaveEvent event) {

		UserDto dto = new UserDto();
		dto = FacadeProvider.getUserFacade().saveUser(event.getContact());
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		Date now = new Date();
		dto.setCreationDate(now);
		makeInitialPassword(dto.getUuid(), dto.getUserEmail(), dto.getUserName());
		grid.getDataProvider().refreshAll();

		closeEditor();
	}

	private void deleteContact(UserForm.DeleteEvent event) {
		// FacadeProvider.getUserFacade(). .getContact());
		// updateList();
		grid.getDataProvider().refreshAll();
		
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
			newUserPop.getElement().executeJs("this.$.overlay.setAttribute('theme', 'center');"); // Center the dialog content

			Paragraph infoText = new Paragraph("Please , copy this password, it is shown only once.");
			newUserPop.setHeaderTitle("New User Password");
			H3 username = new H3("Username : " + userName);
			username.getStyle().set("color", "#0D6938");
			
			H3 password = new H3("Password : " + newPassword);
			password.getStyle().set("color", "#0D6938");
					
			infoLayout.add(username,password);
			
			newUserPop.add(infoLayout);
			
			newUserPop.setOpened(true);
		}
//		else {
//			showAccountCreatedSuccessful();
//		}
	}

	private FormLayout createDialogLayout() {

		FormLayout formLayout = new FormLayout();

		H3 createUserSubHeading = new H3("Personal Information");
		formLayout.setColspan(createUserSubHeading, 2);
		formLayout.add(createUserSubHeading);

		TextField firstName = new TextField("First Name");
		firstName.isRequired();
		formLayout.add(firstName);

		TextField lastName = new TextField("Last Name");
		lastName.isRequired();
		formLayout.add(lastName);

		TextField userEmail = new TextField("Email Address");
		userEmail.setHelperText("Used to send Email Notification");
		formLayout.add(userEmail);

		TextField phone = new TextField("Phone Number");
		phone.setHelperText("Used to send SMS notification needs to contain Country code");
		formLayout.add(phone);

		TextField userPosition = new TextField("Position");
		TextField userOrganisation = new TextField("Organisation");
		formLayout.add(userPosition, userOrganisation);

		ComboBox<Language> language = new ComboBox<>();
		language.setLabel("Language");
		language.setItems(Language.getAssignableLanguages());
		formLayout.add(language);

		H3 createUserSubHeading2 = new H3("Address");
		formLayout.setColspan(createUserSubHeading2, 2);
		formLayout.add(createUserSubHeading2);

		ComboBox<AreaReferenceDto> region = new ComboBox<>();
		region.setLabel("Region");
		region.setItems(regions);
		formLayout.add(region);

		ComboBox<RegionReferenceDto> province = new ComboBox<>();
		province.setLabel("Province");
		province.setItems(provinces);
		formLayout.add(province);

		ComboBox<DistrictReferenceDto> district = new ComboBox<>();
		district.setLabel("District");
		district.setItems(districts);
		formLayout.add(district);

		ComboBox<CommunityReferenceDto> cluster = new ComboBox<>();
		cluster.setLabel("Cluster");
		cluster.setItems();
		formLayout.add(cluster);

		TextField street = new TextField("Street");
		street.setPlaceholder("Enter street here");
		formLayout.add(street);

		TextField houseNumber = new TextField("House Number");
		houseNumber.setPlaceholder("Enter House Number here");
		formLayout.add(houseNumber);

		TextField additionalInformation = new TextField("Additional Information");
		additionalInformation.setPlaceholder("Enter Additional Information here");
		formLayout.add(additionalInformation);

		TextField postalCode = new TextField("Postal Code");
		postalCode.setPlaceholder("Enter postal Code here");
		formLayout.add(postalCode);

		TextField city = new TextField("City");
		city.setPlaceholder("Enter City here");
		formLayout.add(city);

		ComboBox<AreaType> areaType = new ComboBox<>();
		areaType.setLabel("Area Type");
		areaType.setItems(AreaType.values());
		formLayout.add(areaType);

		H3 createUserSubHeading3 = new H3("User Data");
		formLayout.setColspan(createUserSubHeading3, 2);
		formLayout.add(createUserSubHeading3);

		TextField userName = new TextField("Username");
		userName.isRequired();
		userName.setRequiredIndicatorVisible(true);
		formLayout.add(userName);

		Checkbox active = new Checkbox();
		active.setLabel("Active?");
		formLayout.setColspan(active, 2);
		active.setValue(true);
		formLayout.add(active);

		CheckboxGroup<UserType> userType = new CheckboxGroup<>();
		userType.setLabel("Type of Users");
		userType.setItems(UserType.values());
		formLayout.setColspan(userType, 2);
		formLayout.add(userType);

		CheckboxGroup<FormAccess> formAccess = new CheckboxGroup<>();
		formAccess.setLabel("Forms Access");
		formAccess.setItems(UserUiHelper.getAssignableForms());
		formAccess.isRequired();
		formAccess.setRequiredIndicatorVisible(true);
		formLayout.add(formAccess);

		CheckboxGroup<UserRole> userRole = new CheckboxGroup<>();
		userRole.setLabel("User Roles");
		userRole.setItems(UserRole.getAssignableRoles(FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles()));
		userRole.isRequired();
		userRole.setRequiredIndicatorVisible(true);
		formLayout.add(userRole);

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