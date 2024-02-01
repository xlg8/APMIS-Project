package com.cinoteck.application.messaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
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
import de.symeda.sormas.api.user.UserDto;
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
	private ComboBox<AreaReferenceDto> areaFilter;
	private ComboBox<RegionReferenceDto> regionFilter;
	private ComboBox<DistrictReferenceDto> districtFilter;
	public MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>();
	
	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
	List<CommunityReferenceDto> communities;
	
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
	
	MessageCriteria criteria = new MessageCriteria();
	
	@Value("${fcm.secret.key}")
	private String fcmSecretKey;

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
		hr.add(hideFilters, search, userRole, areaFilter, regionFilter, districtFilter, community, newMessage);
		add(hr, grid);
	}

	public void configureView() {

		hideFilters = new Button("Hide Filters");

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
		});
		
		areaFilter = new ComboBox<AreaReferenceDto>();
		areaFilter.setId("");
		areaFilter.setWidth("145px");

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
//			updateRowCount();

		});
		
		regionFilter = new ComboBox<RegionReferenceDto>();
		regionFilter.setId(CaseDataDto.REGION);
		regionFilter.setWidth(145, Unit.PIXELS);
		regionFilter.setLabel(
				I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, I18nProperties.getCaption(Captions.region)));
		regionFilter.setPlaceholder(I18nProperties.getCaption(Captions.region));
		regionFilter.getStyle().set("margin-left", "0.1rem");
		regionFilter.getStyle().set("padding-top", "0px!important");
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
//			updateRowCount();

		});
		
		districtFilter = new ComboBox<DistrictReferenceDto>();
		districtFilter.setId(CaseDataDto.DISTRICT);
		districtFilter.setWidth(145, Unit.PIXELS);
		districtFilter.setLabel(I18nProperties.getCaption(Captions.district));
		districtFilter.setPlaceholder(I18nProperties.getCaption(Captions.district));
		districtFilter.getStyle().set("margin-left", "0.1rem");
		districtFilter.getStyle().set("padding-top", "0px!important");
		districtFilter.setClearButtonVisible(true);
		districtFilter.setReadOnly(true);
		
		districtFilter.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				DistrictReferenceDto district = e.getValue();
				community.setItems(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(e.getValue().getUuid()));
				criteria.district(district);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();
//				updateRowCount();

			} else {
				criteria.district(null);
				filterDataProvider.setFilter(criteria);
				filterDataProvider.refreshAll();
//				updateRowCount();

			}
		});
		
		newMessage = new Button("New Massage");

		bulkModeButton = new Button("Enter Bulk Edit Mode");
		bulkModeButton.addClickListener(e -> {
			
//			if()
		});

		newMessage.addClickListener(e -> {

			messageDto = new MessageDto();
			newMessage(messageDto);
		});
	}
	
	private String rolesConf(MessageDto usrdto) {
		UserProvider usrProv  = new UserProvider();
		I18nProperties.setUserLanguage(usrProv.getUser().getLanguage());
		String value = usrdto.getUserRoles().toString();
		//System.out.println(I18nProperties.getUserLanguage() + "o//: "+value);
		return value.replace("[", "").replace("]", "")
				.replace("null,", "").replace("null", "");
	}

	public void configureGrid() {

		grid.setSelectionMode(SelectionMode.SINGLE);
		grid.setMultiSort(true, MultiSortPriority.APPEND);
		grid.setSizeFull();
		grid.setColumnReorderingAllowed(true);

		grid.addColumn(MessageDto.MESSAGE_CONTENT).setHeader("Message Content").setSortable(true).setResizable(true);
		grid.addColumn(this::rolesConf).setHeader("User Roles").setSortable(true).setResizable(true);
//		grid.addColumn(MessageDto.USER_TYPE).setHeader("User Type").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.AREA).setHeader("Region").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.REGION).setHeader("Province").setSortable(true).setResizable(true);
		grid.addColumn(MessageDto.DISTRICT).setHeader("District").setSortable(true).setResizable(true);
//		grid.addColumn(MessageDto.COMMUNITY).setHeader("Community").setSortable(true)
//				.setResizable(true);
//		grid.addColumn(MessageDto::getCreatingUser).setHeader("Created By").setSortable(true).setResizable(true);

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

	public void sendFCM(FCMDto fcmDto) throws Exception {		
		FCMResponseDto fcmResponseDto = null;
		try {
			Gson gson = new Gson();
			StringEntity postingString = new StringEntity(gson.toJson(fcmDto));
			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
			post.setEntity(postingString);
			post.addHeader("Content-type", "application/json");
			post.addHeader("Authorization", "key=");
			StringBuilder result = new StringBuilder();
			CloseableHttpResponse response = client.execute(post);

			if (response.getStatusLine().getStatusCode() == 200) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				String line;
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
			} else {
				throw new Exception("Status Code of response not 200");
			}
			
			ObjectMapper mapper = new ObjectMapper();
			fcmResponseDto = mapper.readValue(result.toString(), FCMResponseDto.class);
		} catch (Throwable ex) {
			throw new Exception("Unable to make Successful Request");
		}
	}

	public void saveMessage(MessagingLayout.SaveEvent event) throws Exception {
		FacadeProvider.getMessageFacade().saveMessage(event.getMessage());

		if (event.getMessage().getUserRoles().contains(UserRole.REST_USER)) {
			FCMDto fcmDto = new FCMDto();
			fcmDto.setTo("/topics/allDevices");

			FCMDto.Notification notification = fcmDto.new Notification();
			notification.setTitle("APMIS Update");
			notification.setBody(event.getMessage().getMessageContent());
			fcmDto.setNotification(notification);

			sendFCM(fcmDto);
		}
	}
}
