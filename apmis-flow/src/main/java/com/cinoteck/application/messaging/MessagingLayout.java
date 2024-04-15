package com.cinoteck.application.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageDto;
import de.symeda.sormas.api.user.FormAccess;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserType;

public class MessagingLayout extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6287328469071497747L;

	MessageDto messageDto;

	H3 pushNotificationHeader = new H3("Push Notication Configuration");
	TextField titleField;
	public TextArea messageContent;
	MultiSelectComboBox<UserRole> userRoles;
	ComboBox<UserType> userType;
	MultiSelectComboBox<FormAccess> formAccessSelector;
	MultiSelectComboBox<AreaReferenceDto> areaSelector;
	MultiSelectComboBox<RegionReferenceDto> regionSelector;
	MultiSelectComboBox<DistrictReferenceDto> districtSelector;
	MultiSelectComboBox<CommunityReferenceDto> communitySelector;

	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
	List<CommunityReferenceDto> communities;

	Binder<MessageDto> binder = new BeanValidationBinder<>(MessageDto.class);
	FormLayout formLayout = new FormLayout();

	UserProvider userProvider = new UserProvider();

	List<RegionReferenceDto> regionHolder;
	List<DistrictReferenceDto> districtHolder;
	List<CommunityReferenceDto> communityiesHolder;

	Icon savePreviewIcon = new Icon(VaadinIcon.PROGRESSBAR);
	Button savePreviewButton = new Button("Send", savePreviewIcon);

	private boolean isNew = false;

	public MessagingLayout(MessageDto messageDto_, boolean isNew) {

		this.isNew = isNew;
		if (isNew) {
			MessageDto messageDtoNew = new MessageDto();

			this.messageDto = messageDtoNew.build();
		} else {
			this.messageDto = messageDto_;
		}
		configureFields();
	}

	public void discardChanges() {
		UI currentUI = UI.getCurrent();
		if (currentUI != null) {
			Dialog dialog = (Dialog) this.getParent().get();
			dialog.close();
		}
	}

	public void configureFields() {

		TextField titleField = new TextField("Title");
		TextArea messageContent = new TextArea("Message Content");
		MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<UserRole>("User roles");
		ComboBox<UserType> userType = new ComboBox<UserType>("User Type");
		MultiSelectComboBox<FormAccess> formAccessSelector = new MultiSelectComboBox<FormAccess>("Form Access");
		MultiSelectComboBox<AreaReferenceDto> areaSelector = new MultiSelectComboBox<AreaReferenceDto>("Region");
		MultiSelectComboBox<RegionReferenceDto> regionSelector = new MultiSelectComboBox<RegionReferenceDto>(
				"Province");
		MultiSelectComboBox<DistrictReferenceDto> districtSelector = new MultiSelectComboBox<DistrictReferenceDto>(
				"District");
		MultiSelectComboBox<CommunityReferenceDto> communitySelector = new MultiSelectComboBox<CommunityReferenceDto>(
				"Cluster");

		List<UserType> userTypeConfig = new ArrayList<>();

		userTypeConfig.add(UserType.COMMON_USER);
		userTypeConfig.add(UserType.EOC_USER);
		userTypeConfig.add(UserType.WHO_USER);

		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		roles.remove(UserRole.POE_INFORMANT);
		roles.remove(UserRole.ADMIN);
		List<UserRole> userRoleConfig = new ArrayList<>(roles);

		userRoles.setItems(userRoleConfig);
		userType.setItems(userTypeConfig);
		areaSelector.setItems(regions);
		regionSelector.setItems(provinces);
		districtSelector.setItems(districts);
		formAccessSelector.setItems(FormAccess.values());
		formAccessSelector.setClearButtonVisible(true);

		binder.forField(messageContent).asRequired("Message Content is Required").bind(MessageDto::getMessageContent,
				MessageDto::setMessageContent);

		binder.forField(userRoles).asRequired("User Role is Required").bind(MessageDto::getUserRoles,
				MessageDto::setUserRoles);

		binder.forField(formAccessSelector).bind(MessageDto::getFormAccess, MessageDto::setFormAccess);

		binder.forField(areaSelector).bind(MessageDto::getArea, MessageDto::setArea);

		binder.forField(regionSelector).bind(MessageDto::getRegion, MessageDto::setRegion);

		binder.forField(districtSelector).bind(MessageDto::getDistrict, MessageDto::setDistrict);

		binder.forField(communitySelector).bind(MessageDto::getCommunity, MessageDto::setCommunity);

		formLayout.add(messageContent, userRoles, formAccessSelector, areaSelector, regionSelector, districtSelector,
				communitySelector);
		formLayout.setColspan(pushNotificationHeader, 2);

		final HorizontalLayout hr = new HorizontalLayout();
		formLayout.setColspan(messageContent, 2);
		messageContent.setHeight("300px");

		Icon discardIcon = new Icon(VaadinIcon.CLOSE_CIRCLE_O);
		Button discardChanges = new Button("Discard Changes", discardIcon);
		
		Icon saveIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
		Button saved = new Button("Send", saveIcon);
		hr.add(discardChanges, saved);
		add(formLayout, hr);

		discardChanges.addClickListener(e -> discardChanges());

		saved.addClickListener(e -> {
			if (messageContent.getValue() != null && !messageContent.isEmpty()) {
				preView(binder.getBean());
			} else {
				Notification notification = new Notification();
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
				notification.setPosition(Position.MIDDLE);
				Button closeButton = new Button(new Icon("lumo", "cross"));
				closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
				closeButton.getElement().setAttribute("aria-label", "Close");
				closeButton.addClickListener(event -> {
					notification.close();
				});

				Paragraph text = new Paragraph("Message cannot be left Empty");

				HorizontalLayout layout = new HorizontalLayout(text, closeButton);
				layout.setAlignItems(Alignment.CENTER);

				notification.add(layout);
				notification.open();
			}
		});

		savePreviewButton.addClickListener(e -> {
			validateAndSave();
		});

		areaSelector.addValueChangeListener(e -> {
			regionHolder = new ArrayList<>();
			if (e.getValue() != null) {
				for (

				AreaReferenceDto eachArea : e.getValue()) {
					regionHolder.addAll(FacadeProvider.getRegionFacade().getAllActiveByArea(eachArea.getUuid()));
				}
				regionSelector.clear();
				provinces = regionHolder;

//				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
//					regionFilter.setItems(
//							FacadeProvider.getRegionFacade().getAllActiveByAreaPashto(e.getValue().getUuid()));
//				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
//					regionFilter
//							.setItems(FacadeProvider.getRegionFacade().getAllActiveByAreaDari(e.getValue().getUuid()));
//				} else {
				regionSelector.setItems(provinces);
//				}

//				criteria.area(area);
//				regionFilter.setReadOnly(false);
//				districtFilter.clear();
//				districtFilter.setReadOnly(true);
//				criteria.region(null);
//				criteria.district(null);
			}
//			else {
//				regionFilter.clear();
//				regionFilter.setReadOnly(true);
//				criteria.area(null);
//
//			}
//			filterDataProvider.setFilter(criteria);

		});

		regionSelector.addValueChangeListener(e -> {
			districtHolder = new ArrayList();
			if (e.getValue() != null) {
				for (RegionReferenceDto eachRegion : e.getValue()) {
					districtHolder
							.addAll(FacadeProvider.getDistrictFacade().getAllActiveByRegion(eachRegion.getUuid()));
				}

				districtSelector.clear();

				districts = districtHolder;
//				if (userProvider.getUser().getLanguage().toString().equals("Pashto")) {
//					districtFilter.setItems(
//							FacadeProvider.getDistrictFacade().getAllActiveByRegionPashto(e.getValue().getUuid()));
//				} else if (userProvider.getUser().getLanguage().toString().equals("Dari")) {
//					districtFilter.setItems(
//							FacadeProvider.getDistrictFacade().getAllActiveByRegionDari(e.getValue().getUuid()));
//				} else {
				districtSelector.setItems(districts);
//				}
//
//				criteria.region(region);
//				districtFilter.setReadOnly(false);
//				criteria.district(null);
			}
//			else {
//				districtFilter.clear();
//				districtFilter.setReadOnly(true);
//				criteria.region(null);
//
//			}
//			filterDataProvider.setFilter(criteria);

		});

		// = new ArrayList<>();
		districtSelector.addValueChangeListener(e -> {
			communityiesHolder = new ArrayList<>();
			if (e.getValue() != null) {
				for (DistrictReferenceDto eachDsitrict : e.getValue()) {

					communityiesHolder
							.addAll(FacadeProvider.getCommunityFacade().getAllActiveByDistrict(eachDsitrict.getUuid()));
				}
				communitySelector.clear();

				communities = communityiesHolder;
				communitySelector.setItems(communities);
//				criteria.district(district);
//				filterDataProvider.setFilter(criteria);
//				filterDataProvider.refreshAll();
//				updateRowCount();

			} else {
//				criteria.district(null);
//				filterDataProvider.setFilter(criteria);
//				filterDataProvider.refreshAll();
//				updateRowCount();

			}
		});
	}

	public void validateAndSave() {

		if (binder.validate().isOk()) {

			messageDto = binder.getBean();
			UserReferenceDto userReferenceDto = new UserReferenceDto(userProvider.getUser().getUuid(),
					userProvider.getUser().getFirstName(), userProvider.getUser().getLastName(),
					userProvider.getUser().getUserRoles(), userProvider.getUser().getFormAccess(),
					userProvider.getUser().getUsertype());

			messageDto.setCreatingUser(userReferenceDto);
			fireEvent(new SaveEvent(this, messageDto));

			Notification notification = new Notification("New Message Created", 3000, Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
			notification.open();			
			UI.getCurrent().getPage().reload();			
		} else {
			Notification notification = new Notification();
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			notification.setPosition(Position.MIDDLE);
			Button closeButton = new Button(new Icon("lumo", "cross"));
			closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			closeButton.getElement().setAttribute("aria-label", "Close");
			closeButton.addClickListener(event -> {
				notification.close();
			});

			Paragraph text = new Paragraph("Unable to Create a Message at the Moment");

			HorizontalLayout layout = new HorizontalLayout(text, closeButton);
			layout.setAlignItems(Alignment.CENTER);

			notification.add(layout);
			notification.open();
		}
	}

	public void preView(MessageDto messageDto) {

		TextArea message = new TextArea("Message");
		message.setValue(messageDto.getMessageContent());
		message.setReadOnly(true);
		message.getStyle().set("margin", "10px");
		message.setHeight("250px");

		MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<>("Userroles");
		userRoles.setItems(messageDto.getUserRoles());
		userRoles.setValue(messageDto.getUserRoles());
		userRoles.setReadOnly(true);
		userRoles.getStyle().set("margin", "10px");

		MultiSelectComboBox<FormAccess> formAccess = new MultiSelectComboBox<>("FormAccess");
		formAccess.setItems(messageDto.getFormAccess());
		formAccess.setValue(messageDto.getFormAccess());
		formAccess.setReadOnly(true);
		userRoles.getStyle().set("margin", "10px");

		MultiSelectComboBox<AreaReferenceDto> areas = new MultiSelectComboBox<>("Regions");
		areas.setItems(messageDto.getArea());
		areas.setValue(messageDto.getArea());
		areas.setReadOnly(true);
		areas.getStyle().set("margin", "10px");

		MultiSelectComboBox<RegionReferenceDto> region = new MultiSelectComboBox<>("Provinces");
		region.setItems(messageDto.getRegion());
		region.setValue(messageDto.getRegion());
		region.setReadOnly(true);
		region.getStyle().set("margin", "10px");

		MultiSelectComboBox<DistrictReferenceDto> district = new MultiSelectComboBox<>("Districts");
		district.setItems(messageDto.getDistrict());
		district.setValue(messageDto.getDistrict());
		district.setReadOnly(true);
		district.getStyle().set("margin", "10px");

		MultiSelectComboBox<CommunityReferenceDto> community = new MultiSelectComboBox<>("Clusters");
		community.setItems(messageDto.getCommunity());
		community.setValue(messageDto.getCommunity());
		community.setReadOnly(true);
		community.getStyle().set("margin", "10px");

		FormLayout preViewContent = new FormLayout();
		preViewContent.add(message, userRoles, formAccess, areas, region, district, community);
		preViewContent.setColspan(message, 2);
		Dialog preViewDialog = new Dialog();
		preViewDialog.setWidth("900px");
		preViewDialog.setHeight("700px");
		Button closePreviewButton = new Button("Cancel", e -> preViewDialog.close());
		Icon backIcon = new Icon(VaadinIcon.BACKWARDS);
		closePreviewButton.setIcon(backIcon);
		preViewDialog.add(preViewContent);
		preViewDialog.setHeaderTitle("Send Message");
		preViewDialog.open();
		preViewDialog.setCloseOnEsc(false);
		preViewDialog.setCloseOnOutsideClick(false);
		preViewDialog.setModal(true);
		preViewDialog.setClassName("notification-preview");
		preViewDialog.getFooter().add(closePreviewButton, savePreviewButton);
	}

	public void setMessage(MessageDto messageDto) {
		messageDto.setCreatingUser(userProvider.getUser().toReference());
		binder.setBean(messageDto);
	}

	public static abstract class MessageEvent extends ComponentEvent<MessagingLayout> {
		private MessageDto messageDto;

		protected MessageEvent(MessagingLayout source, MessageDto messageDto) {
			super(source, false);
			this.messageDto = messageDto;
		}

		public MessageDto getMessage() {
			if (messageDto == null) {
				messageDto = new MessageDto();
				return messageDto;
			} else {
				return messageDto;
			}
		}
	}

	public static class SaveEvent extends MessageEvent {
		SaveEvent(MessagingLayout source, MessageDto messageDto) {
			super(source, messageDto);
		}
	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}
}
