package com.cinoteck.application.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.CampaignCriteria;
import de.symeda.sormas.api.campaign.CampaignIndexDto;
import de.symeda.sormas.api.campaign.CampaignLogDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

@PageTitle("APMIS-Notification")
@Route(value = "UserNotification")
public class UserMessageView extends VerticalLayout implements BeforeEnterObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7370357967398473680L;

	UserProvider userProvider = new UserProvider();
	private String intendedRoute;

	private Grid<MessageDto> grid = new Grid<>(MessageDto.class, false);

	private GridListDataView<MessageDto> dataView;

	MessageCriteria messageCriteria;

	public UserMessageView() {

		this.messageCriteria = new MessageCriteria();
		campaignsGrid();

		Dialog dialog = new Dialog();
		dialog.setCloseOnEsc(true);
		dialog.setCloseOnOutsideClick(false);
		dialog.setHeaderTitle("Notification");
		dialog.setWidth("700px");
		dialog.setHeight("400px");
		dialog.add(grid);
		Button closeButton = new Button("Close", e -> dialog.close());
		dialog.getFooter().add(closeButton);

		dialog.open();
		add(dialog);
	}

	private void campaignsGrid() {

		messageCriteria.area(userProvider.getUser().getArea());
		messageCriteria.region(userProvider.getUser().getRegion());
		messageCriteria.district(userProvider.getUser().getDistrict());	

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);
		
		TextRenderer<MessageDto> changeDateRenderer = new TextRenderer<>(dto -> {
			Date timestamp = dto.getChangeDate();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			return dateFormat.format(timestamp);
		});

		grid.addColumn(MessageDto.MESSAGE_CONTENT).setHeader("Message Content").setSortable(true).setResizable(true);
		grid.addColumn(changeDateRenderer).setHeader("Sent at").setSortable(true).setResizable(true);

		ListDataProvider<MessageDto> dataProvider = DataProvider
				.fromStream(FacadeProvider.getMessageFacade().getMessageByUserRoles(messageCriteria, userProvider.getUser().getUsertype(), 0, 5, userProvider.getUser().getUserRoles()).stream());

		dataView = grid.setItems(dataProvider);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		UI.getCurrent().getPage().executeJs("return document.location.pathname").then(String.class, pageTitle -> {
			if (pageTitle.contains("main/")) {
				intendedRoute = pageTitle.split("main/")[1];
				System.out.println(
						"____LOOOOOOGGGGOOOUUUTt: "
								+ String.format("Page title: '%s'", pageTitle.split("main/")[1]));

			}
		});
	}

}
