package com.cinoteck.application.views.myaccount;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.utils.authentication.AccessControl;
import com.cinoteck.application.utils.authentication.AccessControlFactory;
import com.cinoteck.application.utils.authentication.CurrentUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.PasswordField;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserDto;

public class CredentialPassWordChanger extends Div {

	private static final long serialVersionUID = -928337100277917699L;

	UserDto userName;
	ConfirmDialog _dialog;
	private AccessControl accessControl;

	UserProvider currentUser = new UserProvider();

	public CredentialPassWordChanger(UserDto usedto) {

		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(currentUser.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(currentUser.getUser().getLanguage());

		this.userName = usedto;

		_dialog = new ConfirmDialog();
		_dialog.setHeader(I18nProperties.getCaption(Captions.Login_password));
		_dialog.setText(I18nProperties.getString(Strings.doYouReallyWantToChangeYourPassword));
		_dialog.setCloseOnEsc(false);
		_dialog.setCancelable(true);
		// _dialog.addCancelListener(event -> e -> dialog.close());
//
//		 	_dialog.setRejectable(true);
//		 	_dialog.setRejectText("Discard");
//		 	_dialog.addRejectListener(event -> setStatus("Discarded"));

		_dialog.setConfirmText(I18nProperties.getCaption(Captions.actionContinue));
//		_dialog.addConfirmListener(event -> continuePasswrd());

//		_dialog.open();

	}

	protected void continuePasswrd() {

		accessControl = AccessControlFactory.getInstance().createAccessControl();
		UserProvider userProvider = new UserProvider();
		UserDto userxs = userProvider.getUser();
		Dialog dialog = new Dialog();
		dialog.addClassName("custom-dialog");
		dialog.setHeaderTitle(I18nProperties.getString(Strings.changePasswordForUser) + " " + userxs.getUserName());
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		FormLayout layout = new FormLayout();

//		PasswordField oldPassField = new PasswordField(I18nProperties.getCaption(Captions.oldPassword));
//		oldPassField.setSizeFull();

		PasswordField passField1 = new PasswordField(I18nProperties.getString(Strings.headingNewPassword));
		passField1.setSizeFull();
		passField1.setPattern("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");

		PasswordField passField2 = new PasswordField(I18nProperties.getString(Strings.confirmPassword));

//		layout.add(new Label("*Must be at least 8 characters"));
//		layout.add(new Label("*Must contain 1 Uppercase and 1 special character "));

		Label instructionLabel = new Label(I18nProperties.getString(Strings.mustBeAt8Char) + " \r\n <br>"
				+ I18nProperties.getString(Strings.mustContain1UppercaseChar) + " \r\n" + "");
		instructionLabel.getElement().setProperty("innerHTML", instructionLabel.getText());
		instructionLabel.getElement().getStyle().set("font-size", "12px");

		passField2.setSizeFull();
		layout.add(passField1, passField2, instructionLabel);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
//				changePassword.setStyleName(CssStyles.VAADIN_BUTTON);
//				changePassword.setStyleName(ValoTheme.BUTTON_PRIMARY);
//				changePassword.setStyleName(CssStyles.FLOAT_RIGHT);

		saveButton.addClickListener(e -> {
//			String oldPass = oldPassField.getValue().trim();
			String newpass1 = passField1.getValue().trim();
			String newpass2 = passField2.getValue().trim();

			if (newpass1.equals(newpass2)) {
				if (accessControl.upDatePassWordCheck(userxs.getUserName(), newpass1)) {

					FacadeProvider.getUserFacade().changePassword(userxs.getUserName(), newpass1);
					UI.getCurrent().getPage().reload();
					Notification.show(I18nProperties.getString(Strings.passwordChangedSuccessfully));
				}
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

				Paragraph text = new Paragraph(I18nProperties.getString(Strings.passwordDoesNotMatch));

				HorizontalLayout errorLayout = new HorizontalLayout(text, closeButton);
				errorLayout.setAlignItems(Alignment.CENTER);

				notification.add(errorLayout);
				notification.open();
			}
		});

		dialog.add(layout);

		Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), e -> dialog.close());
		dialog.getFooter().add(cancelButton);
		dialog.getFooter().add(saveButton);
		dialog.open();

		getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
				.set("display", "flex").set("align-items", "center").set("justify-content", "center");

	}

}