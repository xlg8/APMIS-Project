package com.cinoteck.application.messaging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.gson.Gson;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.MultiSortPriority;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.FCMDto;
import de.symeda.sormas.api.messaging.FCMResponseDto;
import de.symeda.sormas.api.messaging.MessageCriteria;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.FormAccess;
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
	HorizontalLayout filterLayout;
	TextField search;
	ComboBox<UserType> userType;
	ComboBox<UserRole> userRole;
	private ComboBox<AreaReferenceDto> areaFilter;
	private ComboBox<RegionReferenceDto> regionFilter;
	private ComboBox<DistrictReferenceDto> districtFilter;
	private ComboBox<FormAccess> formAccessFilter;
	public MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>();

	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();

	Button newMessage;
	Button bulkModeButton;
	Button leaveBulkModeButton;

	MessageDto messageDto;

	UserProvider userProvider = new UserProvider();
	HorizontalLayout buttonLayout = new HorizontalLayout();
	HorizontalLayout filters = new HorizontalLayout();

	public Grid<MessageDto> grid = new Grid<>(MessageDto.class, false);
	public GridListDataView<MessageDto> dataView;

	public MessagingDataProvider messagingDataProvider = new MessagingDataProvider();
	public ConfigurableFilterDataProvider<MessageDto, Void, MessageCriteria> filterDataProvider;

	MessageCriteria criteria = new MessageCriteria();

	StringBuilder baseTopic = new StringBuilder();
	FCMDto fcmDto = new FCMDto();

	@Value("${fcm.secret.key}")
	private String fcmSecretKey;
	Paragraph countRowItems;

	public MessagingView() {

		this.setSizeFull();
		this.setHeightFull();
		this.setWidthFull();
		this.addClassName("notificationview");

		filterDataProvider = messagingDataProvider.withConfigurableFilter();

		configureView();
		configureGrid();

		buttonLayout.getStyle().set("margin-left", "10px");
		buttonLayout.setAlignItems(Alignment.END);
		buttonLayout.add(newMessage);

		filters.getStyle().set("margin-left", "10px");
		filters.setAlignItems(Alignment.END);
		filterLayout.add(search, userRole, formAccessFilter, areaFilter, regionFilter, districtFilter, countRowItems);
		filterLayout.setAlignItems(Alignment.START);
		filters.add(hideFilters, filterLayout);
		add(buttonLayout, filters, grid);
	}

	public void configureView() {

		int numberOfRows = filterDataProvider.size(new Query<>());
		countRowItems = new Paragraph(I18nProperties.getCaption(Captions.rows) + numberOfRows);
		countRowItems.setId("rowCount");
		countRowItems.getStyle().set("margin-top", "60px");
//		countRowItems.getStyle().set("padding-left", "150px");
		countRowItems.getStyle().set("margin-right", "20px");
		countRowItems.getStyle().set("margin-left", "auto");

		filterLayout = new HorizontalLayout();
		filterLayout.getStyle().set("margin-top", "10px");
		hideFilters = new Button("Hide Filters", new Icon(VaadinIcon.SLIDERS));
		hideFilters.addClickListener(e -> {
			if (filterLayout.isVisible() == false) {
				filterLayout.setVisible(true);
				hideFilters.setText("Hide Filter");
			} else {
				filterLayout.setVisible(false);
				hideFilters.setText("Show Filters");
			}
		});

		search = new TextField("Search");
		search.setClearButtonVisible(true);

		search.setValueChangeMode(ValueChangeMode.EAGER);
		search.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.freeText(e.getValue().toString());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			} else {

				criteria.freeText(null);
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			}
			updateRowCount();
		});

		userType = new ComboBox<>("User Type");
		userType.setItems(UserType.values());
		userType.setClearButtonVisible(true);

		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		List<UserRole> rolesz = new ArrayList<>(roles);
		roles.remove(UserRole.BAG_USER);
		roles.remove(UserRole.ADMIN);
		Set<UserRole> sortedUserRoless = new TreeSet<>(rolesz);
		userRole = new ComboBox<>("User Roles");
		userRole.setItems(sortedUserRoless);
		userRole.setClearButtonVisible(true);

		userRole.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.userRole(e.getValue());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			} else {

				criteria.userRole(null);
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			}
			updateRowCount();
		});

		formAccessFilter = new ComboBox<FormAccess>("Form Access");
		formAccessFilter.setItems(FormAccess.values());
		formAccessFilter.setClearButtonVisible(true);
		formAccessFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				criteria.formAccess(e.getValue());
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			} else {

				criteria.formAccess(null);
				filterDataProvider.setFilter(criteria);

				filterDataProvider.refreshAll();
			}
			updateRowCount();
		});

		areaFilter = new ComboBox<AreaReferenceDto>();
		areaFilter.setId("");
		areaFilter.setWidth("145px");

		areaFilter.setLabel(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.AREA));
		areaFilter.setPlaceholder(I18nProperties.getCaption(Captions.area));
		regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();

		if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
			areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferencePashto());
		} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
			areaFilter.setItems(FacadeProvider.getAreaFacade().getAllActiveAsReferenceDari());
		} else {
			areaFilter.setItems(regions);
		}

		areaFilter.setClearButtonVisible(true);
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

		regionFilter = new ComboBox<RegionReferenceDto>();
		regionFilter.setId(CaseDataDto.REGION);
		regionFilter.setWidth(145, Unit.PIXELS);
		regionFilter.setLabel(
				I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, I18nProperties.getCaption(Captions.region)));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.region));
		regionFilter.setClearButtonVisible(true);

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

		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		districtFilter.setWidth(145, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.district));
		districtFilter.setClearButtonVisible(true);
		districtFilter.setReadOnly(true);

		districtFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				DistrictReferenceDto district = e.getValue();
				community.setItems(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid()));
				criteria.district(district);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();

			} else {
				criteria.district(null);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();

			}
			updateRowCount();
		});

		newMessage = new Button("New Message", new Icon(VaadinIcon.PLUS_CIRCLE_O));
		newMessage.getStyle().set("margin-top", "10px");

		bulkModeButton = new Button("Enter Bulk Edit Mode");
		bulkModeButton.addClickListener(e -> {

		});

		newMessage.addClickListener(e -> {

			messageDto = new MessageDto();
			newMessage(messageDto);
		});
	}

	private String formAccessConfig(MessageDto messageDto) {
		String value = messageDto.getFormAccess().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String rolesConfig(MessageDto messageDto) {
		I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
		String value = messageDto.getUserRoles().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String areaConfig(MessageDto messageDto) {
		String value = messageDto.getArea().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String regionConfig(MessageDto messageDto) {
		String value = messageDto.getRegion().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String districtConfig(MessageDto messageDto) {
		String value = messageDto.getDistrict().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	private String communityConfig(MessageDto messageDto) {
		String value = messageDto.getCommunity().toString();
		return value.replace("[", "").replace("]", "").replace("null,", "").replace("null", "");
	}

	public void configureGrid() {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(MessageDto.MESSAGE_CONTENT).setHeader("Message Content").setSortable(true).setResizable(true);
		grid.addColumn(this::rolesConfig).setHeader("User Roles").setSortable(true).setResizable(true);
		grid.addColumn(this::areaConfig).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(this::regionConfig).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(this::districtConfig).setHeader("District").setSortable(true).setResizable(true);
		grid.addColumn(this::communityConfig).setHeader("Cluster").setSortable(true).setResizable(true);
		grid.addColumn(this::formAccessConfig).setHeader("Form Access").setSortable(true).setResizable(true);

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

		messagingLayout.addSaveListener(event -> {
			try {
				saveMessage(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
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

		messagingLayout.addSaveListener(event -> {
			try {
				saveMessage(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
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

	public void sendFcmSdk(MessageDto messageDto) throws IOException {

		List<String> tokens = FacadeProvider.getUserFacade().getUserForFCM(messageDto.getFormAccess(),
				messageDto.getArea(), messageDto.getRegion(), messageDto.getDistrict(), messageDto.getCommunity());

		tokens = tokens.stream().filter(element -> element != null).collect(Collectors.toList());

		Set<String> mySet = new HashSet<>(tokens);
		try {
			if (FirebaseApp.getApps().isEmpty()) {
				FileInputStream serviceAccount = new FileInputStream(
						"C:\\Users\\ABC\\Downloads\\sormasapp-9280a-firebase-adminsdk-p77y7-6d71da1dc3.json");
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

				FirebaseApp.initializeApp(options);
			}

			MulticastMessage multicastMessage = MulticastMessage.builder().setNotification(
					Notification.builder().setTitle("APMIS Update").setBody(messageDto.getMessageContent()).build())
					.addAllTokens(mySet).build();

			BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(multicastMessage);
		} catch (FirebaseMessagingException e) {
			e.getMessage();
		}
	}

	public void saveMessage(MessagingLayout.SaveEvent event) throws Exception {
		FacadeProvider.getMessageFacade().saveMessage(event.getMessage());

		if (event.getMessage().getUserRoles().contains(UserRole.REST_USER)) {
			sendFcmSdk(event.getMessage());
		}
	}

	private void updateRowCount() {

		int numberOfRows = filterDataProvider.size(new Query<>());
		String newText = I18nProperties.getCaption(Captions.rows) + numberOfRows;

		countRowItems.setText(newText);
		countRowItems.setId("rowCount");
	}
}
