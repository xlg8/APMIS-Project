package com.cinoteck.application.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.uiformbuilder.FormBuilderLayout.SaveEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.infrastructure.area.AreaReferenceDto;
import de.symeda.sormas.api.infrastructure.community.CommunityReferenceDto;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageDto;
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
	public TextArea messageContent;// = new TextArea("Message Content");
	MultiSelectComboBox<UserRole> userRoles; // = new ComboBox<UserRole>("User roles");
	ComboBox<UserType> userType; // = new ComboBox<UserType>("User Type");
	ComboBox<AreaReferenceDto> areaSelector; // = new ComboBox<AreaReferenceDto>("Region");
	ComboBox<RegionReferenceDto> regionSelector;// = new ComboBox<RegionReferenceDto>("Province");
	ComboBox<DistrictReferenceDto> districtSelector; // = new ComboBox<DistrictReferenceDto>("District");

	List<AreaReferenceDto> regions = FacadeProvider.getAreaFacade().getAllActiveAsReference();
	List<RegionReferenceDto> provinces = FacadeProvider.getRegionFacade().getAllActiveAsReference();
	List<DistrictReferenceDto> districts = FacadeProvider.getDistrictFacade().getAllActiveAsReference();
	List<CommunityReferenceDto> communities;

	Binder<MessageDto> binder = new BeanValidationBinder<>(MessageDto.class);
	FormLayout formLayout = new FormLayout();

	UserProvider userProvider = new UserProvider();

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

		TextArea messageContent = new TextArea("Message Content");
		MultiSelectComboBox<UserRole> userRoles = new MultiSelectComboBox<UserRole>("User roles");
		ComboBox<UserType> userType = new ComboBox<UserType>("User Type");
		ComboBox<AreaReferenceDto> areaSelector = new ComboBox<AreaReferenceDto>("Region");
		ComboBox<RegionReferenceDto> regionSelector = new ComboBox<RegionReferenceDto>("Province");
		ComboBox<DistrictReferenceDto> districtSelector = new ComboBox<DistrictReferenceDto>("District");

		List<UserType> userTypeConfig = new ArrayList<>();

		userTypeConfig.add(UserType.COMMON_USER);
		userTypeConfig.add(UserType.EOC_USER);
		userTypeConfig.add(UserType.WHO_USER);

		Set<UserRole> roles = FacadeProvider.getUserRoleConfigFacade().getEnabledUserRoles();
		roles.remove(UserRole.BAG_USER);
		roles.remove(UserRole.POE_INFORMANT);
		List<UserRole> userRoleConfig = new ArrayList<>(roles);

		userRoles.setItems(userRoleConfig);
		userType.setItems(userTypeConfig);
		areaSelector.setItems(regions);
		regionSelector.setItems(provinces);
		districtSelector.setItems(districts);

		binder.forField(messageContent).asRequired("Message Content is Required").bind(MessageDto::getMessageContent,
				MessageDto::setMessageContent);

		binder.forField(userRoles).asRequired("User Role is Required").bind(MessageDto::getUserRoles,
				MessageDto::setUserRoles);

		binder.forField(userType).asRequired("User Type is Required").bind(MessageDto::getUserTypes,
				MessageDto::setUserTypes);		

		binder.forField(areaSelector).bind(MessageDto::getArea, MessageDto::setArea);

		binder.forField(regionSelector).bind(MessageDto::getRegion, MessageDto::setRegion);

		binder.forField(districtSelector).bind(MessageDto::getDistrict, MessageDto::setDistrict);

		formLayout.add(messageContent, userRoles, userType, areaSelector, regionSelector, districtSelector);
		formLayout.setColspan(pushNotificationHeader, 2);

		final HorizontalLayout hr = new HorizontalLayout();

		Button discardChanges = new Button("Dicard Changes");
		Button saved = new Button("Save");
		hr.add(discardChanges, saved);
		add(formLayout, hr);

		discardChanges.addClickListener(e -> discardChanges());

		saved.addClickListener(e -> {
			validateAndSave();
		});

		areaSelector.addValueChangeListener(e -> {

			if (e.getValue() != null) {
				AreaReferenceDto area = e.getValue();
				regionSelector.clear();
				provinces = FacadeProvider.getRegionFacade().getAllActiveByArea(e.getValue().getUuid());

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
			if (e.getValue() != null) {
				RegionReferenceDto region = e.getValue();
				districtSelector.clear();

				districts = FacadeProvider.getDistrictFacade().getAllActiveByRegion(e.getValue().getUuid());
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
	}

	public void validateAndSave() {

		if (binder.validate().isOk()) {

			messageDto = binder.getBean();			
			UserReferenceDto userReferenceDto = new UserReferenceDto(userProvider.getUser().getUuid(), userProvider.getUser().getFirstName(),
					userProvider.getUser().getLastName(), userProvider.getUser().getUserRoles(), userProvider.getUser().getFormAccess(), 
					userProvider.getUser().getUsertype());
			
			messageDto.setCreatingUser(userReferenceDto);
			fireEvent(new SaveEvent(this, messageDto));

			System.out.println("from frontend " + messageDto.getCreatingUser().getFirstName());
			System.out.println("from frontend 2 " + messageDto.getUuid());
			UI.getCurrent().getPage().reload();

			Notification.show("New Message Created");
		} else {

			Notification.show("Unable to Save new Message");
		}
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
