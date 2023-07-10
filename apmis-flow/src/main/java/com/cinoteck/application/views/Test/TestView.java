package com.cinoteck.application.views.Test;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;

@PageTitle("yyyy")
@Route(value = "testing", layout = MainLayout.class)
public class TestView extends VerticalLayout{

	
	public static final String ACTIVE_FILTER = I18nProperties.getString(Strings.active);
	public static final String INACTIVE_FILTER = I18nProperties.getString(Strings.inactive);

	Binder<UserDto> binder = new BeanValidationBinder<>(UserDto.class);

	UserCriteria criteria;
	UserProvider userProvider = new UserProvider();
	private Grid<UserDto> grid = new Grid<>(UserDto.class, false);
	private ConfigurableFilterDataProvider<UserDto, Void, UserCriteria> filterDataProvider;
	private UsersDataProvider usersDataProvider = new UsersDataProvider();

	
	
	public TestView() {
		configureGrid();
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

	 grid.addColumn(activeRenderer).setHeader("Active").setSortable(true)
				.setResizable(true);
		grid.addColumn(userRolesRenderer).setHeader("User Roles").setSortable(true)
				.setResizable(true);
	 grid.addColumn(UserDto::getUserName).setHeader("Username").setSortable(true)
				.setResizable(true);
		 grid.addColumn(UserDto::getName).setHeader("Name").setSortable(true)
				.setResizable(true);
	 grid.addColumn(UserDto::getUserEmail).setHeader("Email").setSortable(true)
				.setResizable(true);
	 
	 grid.addColumn(UserDto::getUserPosition).setHeader("Organisation")
				.setSortable(true).setResizable(true);
	grid.addColumn(UserDto::getUserOrganisation).setHeader("Position")
				.setSortable(true).setResizable(true);

 grid.addColumn(UserDto::getArea).setHeader("Area").setResizable(true)
				.setSortable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setAllRowsVisible(false);
		filterDataProvider = usersDataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);

//		grid.asSingleSelect().addValueChangeListener(event -> editUser(event.getValue()));

//		dataProvider = DataProvider.fromFilteringCallbacks(this::fetchCampaignFormData, this::countCampaignFormData);
//		grid.setDataProvider(dataProvider);
		
		add(grid);
	}
	
//	UserForm form;

	
//	public void editUser(UserDto user) {
//
//		if (user == null) {
//			UserDto newUser = new UserDto();
//			form.setUser(newUser);
//			form.setVisible(true);
//			form.setSizeFull();
//			grid.setVisible(false);
//		} else {
//			form.setUser(user);
//			form.setVisible(true);
//			form.setSizeFull();
//			grid.setVisible(false);
//			addClassName("editing");
//		}
//	}
}
