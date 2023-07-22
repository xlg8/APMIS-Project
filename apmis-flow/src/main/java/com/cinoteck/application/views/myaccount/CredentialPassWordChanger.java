package com.cinoteck.application.views.myaccount;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.utils.authentication.AccessControl;
import com.cinoteck.application.utils.authentication.AccessControlFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.FacadeProvider;
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

		this.userName = usedto;

		_dialog = new ConfirmDialog();
		_dialog.setHeader(I18nProperties.getCaption(Captions.Login_password)+"?");
		_dialog.setText("Do you really want to change your password?");
		_dialog.setCloseOnEsc(false);
		_dialog.setCancelable(true);
		// _dialog.addCancelListener(event -> e -> dialog.close());
//
//		 	_dialog.setRejectable(true);
//		 	_dialog.setRejectText("Discard");
//		 	_dialog.addRejectListener(event -> setStatus("Discarded"));

		_dialog.setConfirmText(I18nProperties.getCaption(Captions.actionContinue));
		_dialog.addConfirmListener(event -> continuePasswrd());

		_dialog.open();

	}

	private void continuePasswrd() {
		// _dialog.close();
		 accessControl = AccessControlFactory.getInstance().createAccessControl();
		Dialog dialog = new Dialog();
		dialog.addClassName("custom-dialog");
		dialog.setHeaderTitle(I18nProperties.getString(Strings.messageChangePassword));
		dialog.setCloseOnEsc(false);
		dialog.setCloseOnOutsideClick(false);

		FormLayout layout = new FormLayout();

		UserProvider userProvider = new UserProvider();
		UserDto userxs = userProvider.getUser();
		Label c2Label = new Label("Editing: " + userxs.getUserName());
		// c2Label.addStyleNames(CssStyles.H2, CssStyles.VSPACE_NONE,
		// CssStyles.VSPACE_TOP_NONE, CssStyles.LABEL_PRIMARY);

		layout.add(c2Label);

		layout.add(new Label());

		PasswordField oldPassField = new PasswordField(I18nProperties.getString("Old Password"));
		oldPassField.setSizeFull();
		layout.add(oldPassField);

		PasswordField passField1 = new PasswordField(I18nProperties.getString(Strings.headingNewPassword));
		passField1.setSizeFull();
		passField1.setPattern("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");
		layout.add(passField1);

		PasswordField passField2 = new PasswordField("Confirm New Password");

//		layout.add(new Label("*Must be at least 8 characters"));
//		layout.add(new Label("*Must contain 1 Uppercase and 1 special character "));

		Label instructionLabel = new Label("* Must be at least 8 characters\r\n <br>"
				+ "* Must contain 1 Uppercase and 1 special character\r\n" + "");
		instructionLabel.getElement().setProperty("innerHTML", instructionLabel.getText());
		instructionLabel.getElement().getStyle().set("font-size", "12px");

		passField2.setSizeFull();
		layout.add(passField2, instructionLabel);

		Button saveButton = new Button(I18nProperties.getCaption(Captions.actionSave));
//				changePassword.setStyleName(CssStyles.VAADIN_BUTTON);
//				changePassword.setStyleName(ValoTheme.BUTTON_PRIMARY);
//				changePassword.setStyleName(CssStyles.FLOAT_RIGHT);

		saveButton.addClickListener(e -> {
			String oldPass = oldPassField.getValue();
			String newpass1 = passField1.getValue();
			String newpass2 = passField2.getValue();

			if (oldPass != null || oldPassField.getValue().isEmpty()) {
		
				if ((accessControl.upDatePassWordCheck(currentUser.getUser().getUserName(), oldPass)) && (newpass1.equals(newpass2))) {

						FacadeProvider.getUserFacade().changePassword(userName.getUserName(), newpass1);

						UI.getCurrent().getPage().reload();

						Notification.show("Password changed Successfully");

				}else {
						Notification.show("Password does not match");
					}
				}
		
		});

		dialog.add(layout);

		Button cancelButton = new Button(I18nProperties.getCaption(Captions.actionCancel), e -> dialog.close());
		dialog.getFooter().add(cancelButton);
		dialog.getFooter().add(saveButton);

		// Button button = new Button("Show dialog", e -> dialog.open());
		dialog.open();

		getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
				.set("display", "flex").set("align-items", "center").set("justify-content", "center");

		// add(dialog);

	}

}