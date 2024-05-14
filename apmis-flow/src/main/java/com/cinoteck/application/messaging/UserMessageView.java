package com.cinoteck.application.messaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.cinoteck.application.views.uiformbuilder.FormBuilderLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
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
	Date thirtyDaysAgo;

	public UserMessageView() {

		thirtyDaysAgo = subtractDaysFromDate(new Date(), 30);
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

		List<MessageDto> listOfMessagesToRemoveExpiredMessages = FacadeProvider.getMessageFacade()
				.getMessageByUserRoles(messageCriteria, userProvider.getUser().getUsertype(), 0, 10,
						userProvider.getUser().getUserRoles(), userProvider.getUser().getFormAccess());
		
		List<MessageDto> mainMessagesList = new ArrayList<>();
		
		for (MessageDto messages : listOfMessagesToRemoveExpiredMessages) {
            if (messages.getChangeDate().after(thirtyDaysAgo) || messages.getChangeDate().equals(thirtyDaysAgo)) {
            	mainMessagesList.add(messages);
            }
        }
		
		ListDataProvider<MessageDto> dataProvider = DataProvider
				.fromStream(mainMessagesList.stream());
		
		dataView = grid.setItems(dataProvider);
		
		grid.asSingleSelect().addValueChangeListener(event -> showMessage(event.getValue()));
	}		
	
	private void showMessage(MessageDto messageDto) {

		TextArea message = new TextArea("Message");
		message.setValue(messageDto.getMessageContent());
		message.setReadOnly(true);
		message.getStyle().set("margin", "10px");
		message.setHeight("300px");
		message.setWidth("700px");
		
		Dialog messageDetails = new Dialog();
		messageDetails.setWidth("800px");
		messageDetails.setHeight("400px");
		Button closePreviewButton = new Button("Back", e -> messageDetails.close());
		Icon backIcon = new Icon(VaadinIcon.BACKWARDS);
		closePreviewButton.setIcon(backIcon);
		messageDetails.add(message);
		messageDetails.setHeaderTitle("Message Details");
		messageDetails.open();
		messageDetails.setCloseOnEsc(false);
		messageDetails.setCloseOnOutsideClick(false);
		messageDetails.setModal(true);
		messageDetails.setClassName("show-message");
		messageDetails.getFooter().add(closePreviewButton);
	}
	
	public static Date subtractDaysFromDate(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTime();
    }
	
}