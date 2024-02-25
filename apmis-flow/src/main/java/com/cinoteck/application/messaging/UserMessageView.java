package com.cinoteck.application.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;

@PageTitle("APMIS-Notification")
@Route(value = "UserNotification", layout = MainLayout.class)
public class UserMessageView extends VerticalLayout{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7370357967398473680L;

	UserProvider userProvider = new UserProvider();

	private Grid<MessageDto> grid = new Grid<>(MessageDto.class, false);

	private GridListDataView<MessageDto> dataView;

	MessageCriteria messageCriteria;
	Dialog dialog = new Dialog();

	public UserMessageView() {

		this.messageCriteria = new MessageCriteria();
		campaignsGrid();

		dialog = new Dialog();
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(false);
		dialog.setHeaderTitle("Notification");
		dialog.setWidth("800px");
		dialog.setHeight("400px");
		dialog.add(grid);
		Button closeButton = new Button("Close", e -> {
			dialog.close();
			getUI().ifPresent(ui -> ui.navigate("about"));
		});
		dialog.getFooter().add(closeButton);

		dialog.open();
		add(dialog);
	}

	private void campaignsGrid() {

		if(userProvider.getUser().getUserRoles().contains(UserRole.REST_USER)) {
			UserDto user = FacadeProvider.getUserFacade().getByUserName(userProvider.getUser().getUserName());
			if(user.getArea() != null) {
				messageCriteria.area(user.getArea());
			}
			if(user.getRegion() != null) {
				messageCriteria.region(user.getRegion());
			}
			if(user.getDistrict() != null) {
				messageCriteria.district(user.getDistrict());
			}			
		}

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		TextRenderer<MessageDto> changeDateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getChangeDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(timestamp);
		});

		grid.addColumn(MessageDto.MESSAGE_CONTENT).setHeader("Message").setSortable(true).setResizable(true);
		grid.addColumn(changeDateRenderer).setHeader("Broadcasted at").setSortable(true).setResizable(true);

		ListDataProvider<MessageDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getMessageFacade()
						.getMessageByUserRoles(messageCriteria, userProvider.getUser().getUsertype(), 0, 5,
								userProvider.getUser().getUserRoles(), userProvider.getUser().getFormAccess())
						.stream());
		dataView = grid.setItems(dataProvider);
	}
}
