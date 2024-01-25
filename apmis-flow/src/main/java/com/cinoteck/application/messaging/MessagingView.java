package com.cinoteck.application.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.uiformbuilder.FormBuilderDataProvider;
import com.cinoteck.application.views.uiformbuilder.FormBuilderLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormCriteria;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@PageTitle("APMIS-Notification")
@Route(value = "Notification", layout = MainLayout.class)
public class MessagingView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1327683446669581511L;

	Button hideFilters;
	TextField search;
	ComboBox<UserType> userType;
	ComboBox<UserRole> userRole;
	Button newMessage;
	Button bulkModeButton;
	Button leaveBulkModeButton;
	
	MessageDto messageDto;
	
	UserProvider userProvider = new UserProvider();
	HorizontalLayout hr = new HorizontalLayout();
	
	public Grid<MessageDto> grid = new Grid<>(MessageDto.class, false);
	public GridListDataView<MessageDto> dataView;
	
	public MessagingDataProvider messagingDataProvider = new MessagingDataProvider();
	public ConfigurableFilterDataProvider<MessageDto, Void, MessageCriteria> filterDataProvider;
	
	public MessagingView() {
		
		this.setSizeFull();
		this.setHeightFull();
		this.setWidthFull();
		this.addClassName("notificationview");
		
		filterDataProvider = messagingDataProvider.withConfigurableFilter();
		
		configureView();
		configureGrid();
		hr.getStyle().set("margin-left", "10px");
		hr.setAlignItems(Alignment.END);
		hr.add(hideFilters, search, userType, userRole, newMessage, bulkModeButton, leaveBulkModeButton);
		add(hr, grid);
	}
	
	public void configureView() {

		hideFilters = new Button("Hide Filters");

		search = new TextField("Search");
		search.setClearButtonVisible(true);
		
		userType = new ComboBox<>("User Type");
		userType.setItems(UserType.values());		
		userType.setClearButtonVisible(true);
		
		userRole = new ComboBox<>("User Roles");
		userRole.setItems(UserRole.values());
		userRole.setClearButtonVisible(true);
		
		newMessage = new Button("New Massage");
		
		bulkModeButton = new Button("Enter Bulk Edit Mode");
		leaveBulkModeButton = new Button("Leave Bulk Edit Mode");
		
		newMessage.addClickListener(e -> {

			messageDto = new MessageDto();
			newMessage(messageDto);
		});
	}
	
	public void configureGrid() {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(MessageDto.MESSAGE_CONTENT).setHeader("Message Content").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.USER_ROLES).setHeader("User Roles").setSortable(true)
				.setResizable(true);
		grid.addColumn(MessageDto.USER_TYPE).setHeader("User Type").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.AREA).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.REGION).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.DISTRICT).setHeader("District").setSortable(true).setResizable(true);
//		grid.addColumn(MessageDto.COMMUNITY).setHeader("Community").setSortable(true)
//				.setResizable(true);
		grid.addColumn(MessageDto::getCreatingUser).setHeader("Created By").setSortable(true)
		.setResizable(true);

		grid.setVisible(true);
		grid.setWidthFull();
		grid.setAllRowsVisible(true);

		grid.setDataProvider(filterDataProvider);
		if (userProvider.hasUserRight(UserRight.CAMPAIGN_EDIT)) {

			grid.asSingleSelect().addValueChangeListener(event -> editMessage(event.getValue()));
		}

	}
		
	public void newMessage(MessageDto messageDto) {

		MessagingLayout messagingLayout = new MessagingLayout(messageDto, true);
		messagingLayout.setMessage(messageDto);

		messagingLayout.addSaveListener(this::saveMessage);
		Dialog dialog = new Dialog();
		dialog.add(messagingLayout);
		dialog.setHeaderTitle("New Message");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("new-message");
	}
	
	public void editMessage(MessageDto messageDto) {

		MessagingLayout messagingLayout = new MessagingLayout(messageDto, false);
		messagingLayout.setMessage(messageDto);

		messagingLayout.addSaveListener(this::saveMessage);
		Dialog dialog = new Dialog();
		dialog.add(messagingLayout);
		dialog.setHeaderTitle("Edit Message");
		dialog.setSizeFull();
		dialog.open();
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);
		dialog.setModal(true);
		dialog.setClassName("edit-message");
	}
	
	public void saveMessage(MessagingLayout.SaveEvent event) {
		FacadeProvider.getMessageFacade().saveMessage(event.getMessage());
	}
}
