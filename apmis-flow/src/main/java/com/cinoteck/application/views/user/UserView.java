	package com.cinoteck.application.views.user;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.about.AboutView;
import com.cinoteck.application.views.campaign.CampaignForm;
import com.cinoteck.application.views.campaigndata.ImportCampaignsFormDataDialog;
import com.cinoteck.application.views.user.UserForm.SaveEvent;
import com.cinoteck.application.views.user.UserForm.UserRoleCustomComparator;
import com.cinoteck.application.views.utils.gridexporter.GridExporter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
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

@PageTitle("APMIS-User Management")
@Route(value = "usersmanegement", layout = UsersViewParent.class)
public class UserView extends VerticalLayout implements RouterLayout, BeforeEnterObserver {

	HorizontalLayout mainContainer = new HorizontalLayout();
	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class, false);
	boolean overide = false;
	private ComboBox<String> activeFilter;
	private MultiSelectComboBox<UserRole> userRolesFilter = new MultiSelectComboBox<UserRole>();
//	private ComboBox<UserRole> userRolesFilter = new ComboBox<>();

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

	private UsersDataProvider usersDataProvider = new UsersDataProvider();
	private ConfigurableFilterDataProvider<UserDto, Void, UserCriteria> filterDataProvider;

	UserForm userForm;

	MenuBar menuBar = new MenuBar();

	Button createUserButton = new Button(I18nProperties.getCaption(Captions.userNewUser));
//	Button exportUsersButton = new Button(I18nProperties.getCaption(Captions.export));
//	Button exportRolesButton = new Button(I18nProperties.getCaption(Captions.exportUserRoles));
	Button bulkModeButton = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
	Button leaveBulkModeButton = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
	TextField searchField = new TextField();
	Button exportUsers = new Button(I18nProperties.getCaption(Captions.export));
	Button importUsers = new Button(I18nProperties.getCaption(Captions.actionImport));

	Button displayFilters;

	ConfirmDialog confirmationPopup = new ConfirmDialog();

	private static final String CSV_FILE_PATH = "./result.csv";
	UserDto userDto;

	HorizontalLayout layout = new HorizontalLayout();
	Anchor anchor = new Anchor("", I18nProperties.getCaption(Captions.export));
	Paragraph countRowItems;
	boolean isNewUser = false;

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
		setSpacing(false);
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
		layout.getStyle().set("margin-right", "1rem");

		createUserButton = new Button(I18nProperties.getCaption(Captions.userNewUser));
		createUserButton.addClassName("createUserButton");
		createUserButton.getStyle().set("margin-left", "0.1rem");
		if (userProvider.hasUserRight(UserRight.USER_CREATE)) {
			layout.add(createUserButton);
		}
		Icon createIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
		createUserButton.setIcon(createIcon);
		createUserButton.addClickListener(e -> {
			editUser(true);
		});

		exportUsers = new Button(I18nProperties.getCaption(Captions.export));
		exportUsers.setIcon(new Icon(VaadinIcon.UPLOAD));
		exportUsers.addClickListener(e -> {
			anchor.getElement().callJsFunction("click");

		});

		importUsers = new Button(I18nProperties.getCaption(Captions.actionImport));
		importUsers.setIcon(new Icon(VaadinIcon.DOWNLOAD));
		importUsers.addClickListener(e -> {
			if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)
					&& userProvider.hasUserRight(UserRight.INFRASTRUCTURE_IMPORT)
					&& userProvider.hasUserRight(UserRight.USER_CREATE)) {

				ImportUsersDataDialog dialogx = new ImportUsersDataDialog();
				dialogx.open();
			}

			// anchor.getElement().callJsFunction("click");

		});

		anchor.getStyle().set("display", "none");
		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)) {
			layout.add(exportUsers, anchor, importUsers);
		}

		if (userProvider.hasUserRight(UserRight.INFRASTRUCTURE_EXPORT)
				&& userProvider.hasUserRight(UserRight.INFRASTRUCTURE_IMPORT)
				&& userProvider.hasUserRight(UserRight.USER_CREATE)) {
			layout.add(importUsers);
		}

//		layout.add(anchor);
		layout.addClassNames("row pl-4");

		bulkModeButton = new Button(I18nProperties.getCaption(Captions.actionEnterBulkEditMode));
		leaveBulkModeButton = new Button(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
		leaveBulkModeButton.setText(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
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

		leaveBulkModeButton.setText(I18nProperties.getCaption(Captions.actionLeaveBulkEditMode));
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
			grid.getDataProvider().refreshAll();
		});

		menuBar.setVisible(false);
		MenuItem item = menuBar.addItem(I18nProperties.getCaption(Captions.bulkActions));
		SubMenu subMenu = item.getSubMenu();

		subMenu.addItem(I18nProperties.getCaption(Captions.actionEnableUsers), e -> enableUserPopup());
		subMenu.addItem(I18nProperties.getCaption(Captions.actionDisableUsers), e -> disableUserPopup());
		subMenu.addItem(I18nProperties.getCaption(Captions.actionBulkEditUserFormFields), e -> handleUserBulkEditDialog(grid.getSelectedItems(), userDto, filterDataProvider));

		menuBar.getStyle().set("margin-top", "5px");
//		enable.addClickListener(e -> enableUserPopup());
//		disable.addClickListener(e -> disableUserPopup());
		layout.add(menuBar);

		layout.setPadding(false);

		HorizontalLayout filterLayout = new HorizontalLayout();

		filterLayout.setPadding(false);
		filterLayout.setVisible(true);
		filterLayout.setMargin(false);
		filterLayout.setAlignItems(Alignment.END);
		filterLayout.setWidthFull();
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

		searchField.addClassName("searchField");
		searchField.setPlaceholder("Search Users");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setClearButtonVisible(true);
		searchField.setWidth("145px");
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
		activeFilter.setWidth("145px");

		activeFilter.setClearButtonVisible(true);
		activeFilter.setLabel(I18nProperties.getCaption(Captions.User_active));
		activeFilter.setPlaceholder(I18nProperties.getCaption(Captions.User_active));
		activeFilter.getStyle().set("margin-left", "12px");
		activeFilter.getStyle().set("margin-top", "12px");
		activeFilter.setItems("Active", "Inactive");
		activeFilter.addValueChangeListener(e -> {

			if (e.getValue().equals("Active")) {
				criteria.active(true);
			} else if (e.getValue().equals("Inactive")) {
				criteria.active(false);
			} else {
				criteria.active(null);
			}
			filterDataProvider.setFilter(criteria);
			filterDataProvider.refreshAll();
			updateRowCount();

		});

		filterLayout.add(activeFilter);

//		userRolesFilter = new ComboBox<UserRole>();
//		userRolesFilter.setWidth("145px");
//
//		userRolesFilter.setId(UserDto.USER_ROLES);
//		userRolesFilter.setLabel(I18nProperties.getPrefixCaption(UserDto.I18N_PREFIX, UserDto.USER_ROLES));
//		userRolesFilter.setPlaceholder(I18nProperties.getCaption(Captions.User_userRoles));
//		userRolesFilter.getStyle().set("margin-left", "0.1rem");
//		userRolesFilter.getStyle().set("padding-top", "0px!important");
//		userRolesFilter.setClearButtonVisible(true);
//	
//		
//		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
//		roles.remove(UserRole.BAG_USER);
//
//		List<UserRole> rolesz = new ArrayList<>(roles); // Convert Set to List
//		roles.remove(UserRole.BAG_USER);
//
//		// Sorting the user roles usng comprtor
//		Collections.sort(rolesz, new UserRoleCustomComparator());
//		Set<UserRole> sortedUserRoless = new TreeSet<>(rolesz);
//
//		userRolesFilter.setItems(sortedUserRoless);
//		userRolesFilter.addValueChangeListener(e -> {
//
//			UserRole userRole = e.getValue();
//			criteria.userRole(userRole);
//			filterDataProvider.setFilter(criteria);
//			filterDataProvider.refreshAll();
//			updateRowCount();
//
//		});
		
//		Recieve FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles(); into an appropriate collection 
//		convert the system into a list 
//		sort the items in the list and add them back 
//		as fo
//		if current user - who user remoe baguser and admin 
//		while if current user is eoc remove cluster cordinatoe 
//		Remove the role BagUser
		
		

		userRolesFilter = new MultiSelectComboBox<>();
		userRolesFilter.setWidth("145px");
		userRolesFilter.setId(UserDto.USER_ROLES);
		userRolesFilter.setLabel(I18nProperties.getPrefixCaption(UserDto.I18N_PREFIX, UserDto.USER_ROLES));
		userRolesFilter.setPlaceholder(I18nProperties.getCaption(Captions.User_userRoles));
		userRolesFilter.getStyle().set("margin-left", "0.1rem");
		userRolesFilter.getStyle().set("padding-top", "0px!important");
		userRolesFilter.setClearButtonVisible(true);

		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		List<UserRole> rolesList = new ArrayList<>(roles);

		// Sorting the user roles using comparator
		Collections.sort(rolesList, new UserRoleCustomComparator());
		Set<UserRole> sortedUserRoles = new LinkedHashSet<>(rolesList);

		userRolesFilter.setItems(sortedUserRoles);

		
		userRolesFilter.addValueChangeListener(e -> {
		    Set<UserRole> selectedRoles = e.getValue();

		    // Convert UserRole to String (if needed, use another method to get role names)
		    Set<String> roleNames = selectedRoles.stream()
		        .map(UserRole::toString) // You could use .getName() if UserRole has a specific method for names
		        .collect(Collectors.toSet());

		    // If no roles are selected, clear the filter criteria
		    if (selectedRoles.isEmpty()) {
		        criteria.userRole(null);
		        criteria.userRoleSet(null);
		    } else {
		        // Clear existing filters
		        criteria.userRole(null);
		        criteria.userRoleSet(null);
		        criteria.userRoleSet(selectedRoles);

		        // Now set the filter using the role names
		        criteria.setRoleNames(roleNames); // Ensure you have a setRoleNames method in your criteria
		    }

		    // Debugging output
		    System.out.println("Selected roles: " + roleNames);

		    // Apply the filter to the data provider
		    filterDataProvider.setFilter(criteria);
		    filterDataProvider.refreshAll();
		    updateRowCount();
		});

		
		
//		userRolesFilter.addValueChangeListener(e -> {
//		    Set<UserRole> selectedRoles =  new HashSet(); //.getValue();
//		    selectedRoles = e.getValue();
//
//		    String names = selectedRoles.stream()
//		    	    .map(UserRole::toString) // or use another method to get a specific string representation of UserRole
//		    	    .collect(Collectors.joining(","));
//		    if (selectedRoles.isEmpty()) {
//		        criteria.userRole(null);
//		        criteria.userRoleSet(null);
//		    } else {
//		    	criteria.userRole(null);
//		        criteria.userRoleSet(null);
//		        criteria.userRoleSet(selectedRoles);
////		        selectedRolesLabel.setText("Selected: " + String.join(", ", selectedRoles.stream().map(UserRole::toString).collect(Collectors.toList())));
//		    }
//		    
//		    System.out.println(criteria.getUserRole() + "user roles =====set " + criteria.getUserRoleSet());
//		    filterDataProvider.setFilter(criteria);
//		    filterDataProvider.refreshAll();
//		    updateRowCount();
//		});
//		
//		// Add a custom label to show all selected roles
//		Div selectedRolesLabel = new Div();
//		selectedRolesLabel.getStyle().set("font-size", "0.8em");
//		selectedRolesLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
//
//		userRolesFilter.addValueChangeListener(e -> {
//		    Set<UserRole> selectedRoles = e.getValue();
//		    if (selectedRoles.isEmpty()) {
//		        selectedRolesLabel.setText("");
//		    } else {
//		        selectedRolesLabel.setText("Selected: " + String.join(", ", selectedRoles.stream().map(UserRole::toString).collect(Collectors.toList())));
//		    }
//		});

		filterLayout.add(userRolesFilter);

		areaFilter = new ComboBox<AreaReferenceDto>();
		areaFilter.setId(CaseDataDto.AREA);
		areaFilter.setWidth("145px");

		// areaFilter.setWidth(200, Unit.PIXELS);
		areaFilter.setLabel(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.AREA));
		areaFilter.setPlaceholder(I18nProperties.getCaption(Captions.area));
		areaFilter.getStyle().set("margin-left", "0.1rem");
		areaFilter.getStyle().set("padding-top", "0px!important");
		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			areaFilter.setItems(regions);
		}

		areaFilter.setClearButtonVisible(true);
		if (userProvider.getUser() != null && userProvider.getUser().getArea() != null) {
			areaFilter.setValue(userProvider.getUser().getArea());
			if (regionFilter != null) {
				regionFilter.clear();
				if (userProvider.getUser().getArea().getUuid() != null) {
					if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
						regionFilter.setItems(FacadeProvider.getRegionFacade()
								.getAllActiveByAreaPashto(userProvider.getUser().getArea().getUuid()));
					} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
						regionFilter.setItems(FacadeProvider.getRegionFacade()
								.getAllActiveByAreaDari(userProvider.getUser().getArea().getUuid()));
					} else {
						regionFilter.setItems(FacadeProvider.getRegionFacade()
								.getAllActiveByArea(userProvider.getUser().getArea().getUuid()));
					}
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

				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					regionFilter.setItems(
							FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					regionFilter
							.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid()));
				} else {
					regionFilter.setItems(provinces);
				}

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
		regionFilter.setWidth(145, Unit.PIXELS);
		regionFilter.setLabel(
				I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, I18nProperties.getCaption(Captions.region)));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.region));
		regionFilter.getStyle().set("margin-left", "0.1rem");
		regionFilter.getStyle().set("padding-top", "0px!important");
		regionFilter.setClearButtonVisible(true);
		if (userProvider.getUser() != null && userProvider.getUser().getRegion() != null) {
			regionFilter.setItems(userProvider.getUser().getRegion());
			regionFilter.setValue(userProvider.getUser().getRegion());
			if (districtFilter != null) {
				districtFilter.clear();
				if (userProvider.getUser().getRegion().getUuid() != null) {
//					districtFilter.setItems();
					if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
						districtFilter.setItems(FacadeProvider.getDistrictFacade()
								.getAllActiveByRegionPashto(userProvider.getUser().getRegion().getUuid()));
					} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
						districtFilter.setItems(FacadeProvider.getDistrictFacade()
								.getAllActiveByRegionDari(userProvider.getUser().getRegion().getUuid()));
					} else {
						districtFilter.setItems(FacadeProvider.getDistrictFacade()
								.getAllActiveByRegion(userProvider.getUser().getRegion().getUuid()));
					}
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
				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid()));
				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
					districtFilter.setItems(
							FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid()));
				} else {
					districtFilter.setItems(districts);
				}

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
		districtFilter.setWidth(145, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.district));
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
		vlayout.getStyle().set("margin-right", "1rem");
		vlayout.setWidth("98%");
		add(layout, vlayout);
	}

	public void handleUserBulkEditDialog(Set<UserDto> selectedItems, UserDto userDto, ConfigurableFilterDataProvider filterDataProvider) {
		if(selectedItems.size() == 0 || selectedItems.size() <1) {

			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Position.MIDDLE);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(event -> {
				notification.close();
			});

			Paragraph text = new Paragraph("Error : Please Select at least 1 user to proceed with the bulk edit process.");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
			return;
		}else {
			BulkUsersEditDataDialog bulkUsersEditDataDialog = new BulkUsersEditDataDialog(selectedItems, userDto, filterDataProvider);
			bulkUsersEditDataDialog.open();

		}

	}

	private String rolesConf(UserDto usrdto) {
		UserProvider usrProv = new UserProvider();
		I18nProperties.setUserLanguage(usrProv.getUser().getLanguage());
		String value = usrdto.getUserRoles().toString();
		// System.out.println(I18nProperties.getUserLanguage() + "o//: "+value);
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private void configureGrid() {

		ComponentRenderer<Span, UserDto> userRolesRenderer = new ComponentRenderer<>(userroles -> {
			String value = String.valueOf(userroles.getUserRoles()).replace("[", "").replace("]", "")
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
		Column<UserDto> userRolesColumn = grid.addColumn(this::rolesConf)
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
		exporter.setFileName(
				"APMIS_Users_" + new SimpleDateFormat("ddMMyyyy").format(Calendar.getInstance().getTime()));

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

		grid.setItems(filterDataProvider);

		if (userProvider.hasUserRight(UserRight.USER_EDIT)) {
			
			grid.addSelectionListener(event -> {
			    event.getFirstSelectedItem().ifPresent(item -> {
			        editUser(item, false);
			        grid.deselectAll(); // Deselect all items after processing the selected item
			    });
			});

//			grid.addSelectionListener(event -> {
//				editUser(event.getFirstSelectedItem().get(), false);
//				grid.deselectAll();
//			});
		}

		return;

	}
	

	private void configureForm(UserDto user) {

		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		userForm = new UserForm(regions, provinces, districts, user, false);
		userForm.setSizeFull();
//		form.addUserFieldValueChangeEventListener(this::suggestUserName);
//	    form.addResetPasswordListener(event -> resetUserPassword(event, user)); // Use the resetUserPassword method
		userForm.addResetPasswordListener(this::resetUserPassWord);
		userForm.addSaveListener(this::saveUser);
		userForm.addDeleteListener(this::deleteContact);
		userForm.addCloseListener(e -> {
//			UI.getCurrent().getPage().reload();
			closeEditor();
			
//			UI.getCurrent().getPage().reload();

//			grid.deselectAll();
		});

	}

	private Component getContent() {

		// content.setFlexGrow(2, grid);
		mainContainer.setFlexGrow(4, userForm);
		mainContainer.addClassNames("content");
		mainContainer.setSizeFull();
		mainContainer.add(grid, userForm);
		return mainContainer;
	}

	// editing existing user :open dialog with data in it
	public void editUser(UserDto userr, boolean isEdMode) {

		mainContainer.remove(userForm);
		configureForm(userr);
		mainContainer.add(userForm);

		isNewUser = false;
		// configureForm(userr);//this make sure the userform dialog is a new container
		userForm.setUser(userr);
		userForm.setVisible(true);
		userForm.setSizeFull();
		grid.setVisible(false);
		setFiltersVisible(false);
		addClassName("editing");
		String initialUserName = userr.getUserName();
		userForm.save.addClickListener(event -> userForm.validateAndSaveEdit(userr, initialUserName));
	}

	// new user... dialog with no data in it
//	public void editUser(boolean isEdMode) {
//
//		isNewUser = true;
//		UserDto user = new UserDto();
//
//		mainContainer.remove(userForm);
//		configureForm(user);
//		mainContainer.add(userForm);
//
//		// configureForm(user); //this make sure the userform dialog is a new container
//		userForm.createPassword.setVisible(false);
//		userForm.setUser(user);
////		form.addUserFieldValueChangeEventListener(this::suggestUserName);
//		userForm.setVisible(true);
//		userForm.setSizeFull();
//		grid.setVisible(false);
//		setFiltersVisible(false);
//		userForm.binder.forField(userForm.userName)
//		.withValidator(e->validateUserName(userForm.userName.getValue(), userForm.save))
//		.asRequired(I18nProperties.getCaption(Captions.pleaseFillOutFirstLastname))
//		.bind(UserDto::getUserName, UserDto::setUserName);
//		userForm.save.addClickListener(event -> userForm.validateAndSaveNew());
//		userForm.firstName.addValueChangeListener(e -> suggestUserNameWorking());
//		userForm.lastName.addValueChangeListener(e -> suggestUserNameWorking());
//		userForm.userName.addValueChangeListener(e -> checkIfUserNameExists());
//	}
//	
//	public static ValidationResult validateUserName(String value, Button save) {
//	try {
//		System.out.println(value + "Value collection ");
//		UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade().getByUserName(value);
//
//		if (checkNewusernamefromDB != null) {
//			save.setEnabled(false);
//			return ValidationResult.error("Username Exists");
//		} else {
////			UserRole.validate(value);
//			save.setEnabled(true);
//
//			return ValidationResult.ok();
//		}
//
//	} catch (Exception e) {
////    	Notification.show(e.getMessage());
//		return ValidationResult.error("Username culd not be validated ");
//	}
//}
	
	public void editUser(boolean isEditMode) {
	    isNewUser = true;
	    UserDto user = new UserDto();

	    mainContainer.remove(userForm);
	    configureForm(user);
	    mainContainer.add(userForm);

	    userForm.createPassword.setVisible(false);
	    userForm.setUser(user);
	    userForm.setVisible(true);
	    userForm.setSizeFull();
	    grid.setVisible(false);
	    setFiltersVisible(false);

	    // Set up the binder for the username field
	    userForm.binder.forField(userForm.userName)
	        .withValidator(this::validateUserName)
	        .asRequired(I18nProperties.getCaption(Captions.pleaseFillOutFirstLastname))
	        .bind(UserDto::getUserName, UserDto::setUserName);

	    userForm.save.addClickListener(event -> userForm.validateAndSaveNew());
	    userForm.firstName.addValueChangeListener(e -> suggestUserNameWorking());
	    userForm.lastName.addValueChangeListener(e -> suggestUserNameWorking());
	    userForm.userName.addValueChangeListener(e -> checkIfUserNameIsExisting());
	}

	public ValidationResult validateUserName(String value, ValueContext context) {
	    try {
	        UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade().getByUserName(value);

	        if (checkNewusernamefromDB != null) {
	            userForm.save.setEnabled(false);
	            userForm.userName.clear();
//	            ValidationResult.error("Username Exists");
	            return ValidationResult.error("Username Exists");
	        } else {
	            userForm.save.setEnabled(true);
	            return ValidationResult.ok();
	        }
	    } catch (Exception e) {
	        return ValidationResult.error("Username could not be validated");
	    }
	}

	public void checkIfUserNameIsExisting() {
	    ValidationResult result = validateUserName(userForm.userName.getValue(), new ValueContext());
	    if (result.isError()) {
	    	Notification notification = Notification.show("Username Exists", 5000, Position.MIDDLE);
	    	notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

	        userForm.userName.setErrorMessage(result.getErrorMessage());
	        userForm.userName.setInvalid(true);
	        userForm.save.setEnabled(false);
	    } else {
	        userForm.userName.setInvalid(false);
	        userForm.save.setEnabled(true);
	    }
	}


	private void updateRowCount() {

		int numberOfRows = filterDataProvider.size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}

	private void closeEditor() {
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		UserDto userr = new UserDto();
		mainContainer.remove(userForm);
		configureForm(userr);
		mainContainer.add(userForm);

		userForm.setVisible(false);
		setFiltersVisible(true);
		grid.setVisible(true);
		removeClassName("editing");
		userForm.setUser(new UserDto());
		grid.deselectAll();
	}

	private void setFiltersVisible(boolean state) {
		displayFilters.setVisible(state);
		createUserButton.setVisible(state);
//		exportUsersButton.setVisible(state);
		importUsers.setVisible(state);
//		exportRolesButton.setVisible(state);
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

		if (isNewUser) {
			makeInitialPassword(dto.getUuid(), dto.getUserEmail(), dto.getUserName());
		}
		grid.getDataProvider().refreshAll();
		closeEditor();
//		}

	}

	private void resetUserPassWord(UserForm.ResetPasswordEvent event) {

		UserForm formLayout = (UserForm) event.getSource();

		formLayout.suggestUserName(!isNewUser);

		if (!isNewUser) {
			event.getSource().lastName.addValueChangeListener(e -> {
				if (formLayout.userName.isEmpty()) {
					formLayout.userName.setValue(UserHelper.getSuggestedUsername(formLayout.firstName.getValue(),
							formLayout.lastName.getValue()));
				}
			});
		}

	}
//
//	private void suggestUserName(UserForm.UserFieldValueChangeEvent event) {
//
//		UserForm formLayout = (UserForm) event.getSource();
//
//		formLayout.suggestUserName(isEditingModeActive);
//		
//	}

	private void suggestUserNameWorking() {

		if (!userForm.firstName.isEmpty() && !userForm.lastName.isEmpty() && userForm.userName.isEmpty()) {
			
			UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade()
					.getByUserName(userForm.userName.getValue());
			if (checkNewusernamefromDB == null) {
//				fireEvent(new SaveEvent(this, binder.getBean()));
				userForm.userName.setValue(
						UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue()));
			} else {
//				System.out.println("else11111111111 kicked ----------------------------");

				userForm.userName.setErrorMessage("Username exists");
			}
			
		}else {
			
//			System.out.println("else kicked ----------------------------+ userForm.lastName.getValue()" + userForm.lastName.getValue());
//			System.out.println("else kicked ---------------------------- userForm.userName.getValue() +" + userForm.userName.getValue() );

			UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade()
					.getByUserName(userForm.userName.getValue() + userForm.lastName.getValue());
			
//			System.out.println("else kicked ----------------------------" + checkNewusernamefromDB +  "yyyy "+userForm.userName.getValue() + "xxx" + userForm.lastName.getValue());

			
			if (checkNewusernamefromDB == null) {
//				fireEvent(new SaveEvent(this, binder.getBean()));
				userForm.userName.setValue(
						UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue()));
				userForm.save.setEnabled(true);
			} else {
				
//				System.out.println("222222222222222 kicked ----------------------------");
				userForm.userName.setValue(
						UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue()));
				userForm.save.setEnabled(false);
				Notification notification = Notification.show("Username Exists", 5000, Position.MIDDLE);
		    	notification.addThemeVariants(NotificationVariant.LUMO_ERROR);				
//				Notification.show("Username Exists");
				
				userForm.userName.addValueChangeListener(e->{
					UserDto checkNewusernamefromDBx = FacadeProvider.getUserFacade()
							.getByUserName(e.getValue());
					
					if (checkNewusernamefromDBx == null) {
						userForm.save.setEnabled(true);
					}else {
						userForm.save.setEnabled(false);
						Notification.show("Username Exists", 5000, Position.MIDDLE);
					}
					
				});
				
				
//				userForm.save.setTooltipText("Username exists");
//				userForm.userName.setThemeName("error");
//				userForm.userName.setErrorMessage("Username exists");
			}
			
		}

	}
	
	
	private void checkIfUserNameExists() {

		if (!userForm.userName.isEmpty()) {
			
			UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade()
					.getByUserName(userForm.userName.getValue());
			
			
			if (checkNewusernamefromDB == null) {
//				fireEvent(new SaveEvent(this, binder.getBean()));
				userForm.userName.setValue(
						UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue()));
			} else {
				System.out.println("else11111111111 kicked ----------------------------");

				userForm.userName.setErrorMessage("Username exists");
				Notification.show("Eror Userame exists " );
				
				userForm.userName.addValueChangeListener(e->{
					if (userForm.userName.getValue().toString() != UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue())){
						UserDto checkNewusernamefromDBc = FacadeProvider.getUserFacade()
								.getByUserName(e.getValue());
						
						if(checkNewusernamefromDBc == null ) {
							userForm.userName.setValue(e.getValue());
							userForm.save.setEnabled(true);
							return;
						}
						
				}
					
				});
				
			}
			
		}else {
			
			System.out.println("else kicked ----------------------------");
			
			UserDto checkNewusernamefromDB = FacadeProvider.getUserFacade()
					.getByUserName(userForm.userName.getValue());
			
			System.out.println("else kicked ----------------------------" + checkNewusernamefromDB +  "yyyy "+userForm.userName.getValue() + "xxx" + userForm.lastName.getValue());

			
			if (checkNewusernamefromDB == null) {
//				fireEvent(new SaveEvent(this, binder.getBean()));
				userForm.userName.setValue(
						UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue()));
				userForm.save.setEnabled(true);
			} else {
				
				System.out.println("222222222222222 kicked ----------------------------");
				userForm.userName.setValue(
						UserHelper.getSuggestedUsername(userForm.firstName.getValue(), userForm.lastName.getValue()));
				userForm.save.setEnabled(false);
				Notification.show("Username Exists", 5000, Position.MIDDLE);
//				NotificationVariant.LUMO_ERROR;
				
//				Notification.show("Username Exists");
				
				userForm.userName.addValueChangeListener(e->{
					UserDto checkNewusernamefromDBx = FacadeProvider.getUserFacade()
							.getByUserName(e.getValue());
					
					if (checkNewusernamefromDBx == null) {
						userForm.save.setEnabled(true);
					}else {
						userForm.save.setEnabled(false);
						Notification.show("Username Exists", 5000, Position.MIDDLE);
					}
					
				});
				
				
//				userForm.save.setTooltipText("Username exists");
//				userForm.userName.setThemeName("error");
//				userForm.userName.setErrorMessage("Username exists");
			}
			
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

			newUserPop.setHeaderTitle(I18nProperties.getString(Strings.newUserPassword));
			newUserPop.getElement().executeJs("this.$.overlay.setAttribute('theme', 'center');"); // Center the dialog
																									// content

			Paragraph infoText = new Paragraph(I18nProperties.getString(Strings.pleaseCopyPassword));
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
			newUserPop.getElement().executeJs("this.$.overlay.setAttribute('theme', 'center');");

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

			Notification notification = Notification.show(I18nProperties.getString(Strings.headingUsersDisabled) + "  "
					+ I18nProperties.getString(Strings.messageUsersDisabled));
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.open();
		}
	}

	void enableUserPopup() {

		confirmationPopup.setHeader("Enable User");

		confirmationPopup.setText("You are about to Enable " + grid.getSelectedItems().size() + " User");
		confirmationPopup.setCloseOnEsc(false);
		confirmationPopup.setCancelable(true);
		confirmationPopup.addCancelListener(e -> confirmationPopup.close());

		confirmationPopup.setRejectable(true);
		confirmationPopup.setRejectText("Cancel");
		confirmationPopup.addRejectListener(e -> confirmationPopup.close());

		confirmationPopup.setConfirmText("Enable");
		confirmationPopup.addConfirmListener(e -> enableUser(grid.getSelectedItems()));
		confirmationPopup.open();
	}

	void disableUserPopup() {

		confirmationPopup.setHeader("Disable User");

		confirmationPopup.setText("You are about to Disable " + grid.getSelectedItems().size() + " User");
		confirmationPopup.setCloseOnEsc(false);
		confirmationPopup.setCancelable(true);
		confirmationPopup.addCancelListener(e -> confirmationPopup.close());

		confirmationPopup.setRejectable(true);
		confirmationPopup.setRejectText("Cancel");
		confirmationPopup.addRejectListener(e -> confirmationPopup.close());

		confirmationPopup.setConfirmText("Disable");
		confirmationPopup.addConfirmListener(e -> disableUser(grid.getSelectedItems()));
		confirmationPopup.open();
	}

	class UserRoleCustomComparator implements Comparator<UserRole> {
		private final String[] customOrder = {"Admin", "National Data Manager", "National Officer",
				"National Observer / Partner", "Regional Observer", "Regional Data Manager", "Regional Officer",
				"Provincial Observer", "Provincial Data Clerk", "Provincial Officer", "District Officer",
				"District Observer" };

		@Override
		public int compare(UserRole role1, UserRole role2) {
			// Get the indexes of the roles in the custom order
			int index1 = indexOfRole(role1);
			int index2 = indexOfRole(role2);

			// Compare based on their indexes in the custom order
			return Integer.compare(index1, index2);
		}

		private int indexOfRole(UserRole role) {
			for (int i = 0; i < customOrder.length; i++) {
				if (customOrder[i].equals(role.name())) {
					return i;
				}
			}
			return customOrder.length; // Role not found, place it at the end
		}
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {

		UserProvider usrP = new UserProvider();
		System.out.println("trying ti use camp data " + usrP.getUser().getUserRoles());

		if (!userProvider.hasUserRight(UserRight.USER_VIEW)) {
			event.rerouteTo(AboutView.class);
		}

	}
}