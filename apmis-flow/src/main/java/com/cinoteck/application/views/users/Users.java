package com.cinoteck.application.views.users;

import java.util.List;
import java.util.stream.Collectors;

import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;

@Route(value = "users", layout = MainLayout.class)
public class Users extends VerticalLayout {
	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();

	
	Grid<UserDto> grid = new Grid<>(UserDto.class, false);
	UserForm form;

	public Users() {
		addFilters();
		configureGrid();
		configureForm();
		add(getContent());
		closeEditor();
	}

	private Component getContent() {
		HorizontalLayout content = new HorizontalLayout();
		content.setFlexGrow(2, grid);
		content.setFlexGrow(1, form);
		content.addClassNames("content");
		content.setSizeFull();
		content.add(grid, form);
		return content;
	}

	private void configureGrid() {
		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(UserDto::getUserRoles).setHeader("User Roles").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserName).setHeader("Username").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getName).setHeader("Name").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserEmail).setHeader("Email").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserPosition).setHeader("Organisation").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getUserOrganisation).setHeader("Position").setSortable(true).setResizable(true);
		grid.addColumn(UserDto::getRegion).setHeader("Region").setResizable(true).setSortable(true);

		grid.setVisible(true);
		grid.setAllRowsVisible(true);

		// Implement User level view after implementing User Roles/Security
//			if (UserProvider.getCurrent().hasUserRole(UserRole.ADMIN_SUPERVISOR)) {
//				dataProvider = DataProvider.fromStream(
//						FacadeProvider.getUserFacade().getIndexList(getCriteria(), null, null, null).stream().filter(e -> e.getDistrict().equals(UserProvider.getCurrent().getUser().getDistrict())));
//			} else if (UserProvider.getCurrent().hasUserRole(UserRole.ADMIN_SUPERVISOR)) {
//				dataProvider = DataProvider.fromStream(
//						FacadeProvider.getUserFacade().getIndexList(getCriteria(), null, null, null).stream().filter(null));
//			} else {
//				dataProvider = DataProvider.fromStream(
//						FacadeProvider.getUserFacade().getIndexList(getCriteria(), null, null, null).stream().filter(e -> e.getDistrict().equals(UserProvider.getCurrent().getUser().getDistrict())));
//			}

		List<UserDto> regions = FacadeProvider.getUserFacade().getIndexList(null, null, null, null).stream()
				.collect(Collectors.toList());
		GridListDataView<UserDto> dataView = grid.setItems(regions);

		grid.asSingleSelect().addValueChangeListener(event -> editContact(event.getValue()));
	}

	private void configureForm() {
		form = new UserForm(regions, provinces, districts);
		form.setWidth("25em");
		form.addSaveListener(this::saveContact);
		form.addDeleteListener(this::deleteContact);
		form.addCloseListener(e -> closeEditor());
	}

	// TODO: Hide the filter bar on smaller screens
	public void addFilters() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setPadding(false);

		Button createUserButton = new Button("New User");
		createUserButton.addClassName("resetButton");
		createUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(createUserButton);

		Button exportUsersButton = new Button("Export Users");
		exportUsersButton.addClassName("resetButton");
		exportUsersButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(exportUsersButton);

		Button exportRolesButton = new Button("Export User Roles");
		exportRolesButton.addClassName("resetButton");
		exportRolesButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(exportRolesButton);

		Button bulkModeButton = new Button("Enter Bulk Mode");
		bulkModeButton.addClassName("resetButton");
		bulkModeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		layout.add(bulkModeButton);

		TextField searchField = new TextField();
		searchField.setWidth("10%");
		searchField.addClassName("filterBar");
		searchField.setPlaceholder("Search");
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.setValueChangeMode(ValueChangeMode.EAGER);
		searchField.addValueChangeListener(e -> {

		});

		layout.add(searchField);

		add(layout);

		createUserButton.addClickListener(click -> addContact());

	}

	public void editContact(UserDto contact) {
		if (contact == null) {
			closeEditor();
		} else {
			form.setUser(contact);
			form.setVisible(true);
			addClassName("editing");
		}
	}

	private void closeEditor() {
		form.setUser(null);
		form.setVisible(false);
		removeClassName("editing");
	}

	private void addContact() {
		grid.asSingleSelect().clear();
		editContact(new UserDto());
	}

	private void saveContact(UserForm.SaveEvent event) {
		FacadeProvider.getUserFacade().saveUser(event.getContact());
		// updateList();
		closeEditor();
	}

	private void deleteContact(UserForm.DeleteEvent event) {
		// FacadeProvider.getUserFacade(). .getContact());
		// updateList();
		closeEditor();
	}

}
