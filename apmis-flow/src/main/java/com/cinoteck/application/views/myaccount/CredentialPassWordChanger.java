package com.cinoteck.application.views.myaccount;

import com.cinoteck.application.UserProvider;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserDto;

public class CredentialPassWordChanger extends Div {

	private static final long serialVersionUID = -928337100277917699L;
	
	UserDto userName;

	public CredentialPassWordChanger(UserDto usedto) {
		
		this.userName = usedto;

		ConfirmDialog _dialog = new ConfirmDialog();
		_dialog.setHeader("Password?");
		_dialog.setText("Do you really want to change your password?");

		_dialog.setCancelable(true);
		// _dialog.addCancelListener(event -> e -> dialog.close());
//
//		 	_dialog.setRejectable(true);
//		 	_dialog.setRejectText("Discard");
//		 	_dialog.addRejectListener(event -> setStatus("Discarded"));

		_dialog.setConfirmText("Continue");
		_dialog.addConfirmListener(event -> continuePasswrd());

		_dialog.open();

	}

	private void continuePasswrd() {

		Dialog dialog = new Dialog();
		dialog.setHeaderTitle(I18nProperties.getString(Strings.messageChangePassword));

		VerticalLayout layout = new VerticalLayout();
		
		UserProvider userProvider = new UserProvider();
		UserDto userxs = userProvider.getUser();
		Label c2Label = new Label("Editing: " + userxs.getUserName());
		// c2Label.addStyleNames(CssStyles.H2, CssStyles.VSPACE_NONE,
		// CssStyles.VSPACE_TOP_NONE, CssStyles.LABEL_PRIMARY);

		layout.add(c2Label);
		layout.add(new Label(""));

		layout.add(new Label(I18nProperties.getString(Strings.messageChangePassword)));

		layout.add(new Label("*Must be at least 8 characters"));
		layout.add(new Label("*Must contain 1 Uppercase and 1 special character "));
		layout.add(new Label(""));

		PasswordField passField1 = new PasswordField(I18nProperties.getString(Strings.headingNewPassword));
		passField1.setSizeFull();
		passField1.setPattern("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$");
		layout.add(passField1);

		PasswordField passField2 = new PasswordField("Confirm New Password");
		passField2.setSizeFull();
		layout.add(passField2);

		Button saveButton = new Button("Save");
//				changePassword.setStyleName(CssStyles.VAADIN_BUTTON);
//				changePassword.setStyleName(ValoTheme.BUTTON_PRIMARY);
//				changePassword.setStyleName(CssStyles.FLOAT_RIGHT);


		saveButton.addClickListener(e -> {
			String newpass1 = passField1.getValue();
			String newpass2 = passField2.getValue();

			if (newpass1.equals(newpass2)) {// && passField1.isValid()) {

				FacadeProvider.getUserFacade().changePassword(userName.getUserName(), newpass1);

				Notification.show("Password changed Successfully");
				UI.getCurrent().getPage().reload();

			} else {
				Notification.show("Password does not match");
			}
		});
		
		dialog.add(layout);

		Button cancelButton = new Button("Cancel", e -> dialog.close());
		dialog.getFooter().add(cancelButton);
		dialog.getFooter().add(saveButton);

		Button button = new Button("Show dialog", e -> dialog.open());

		
		getStyle().set("position", "fixed").set("top", "0").set("right", "0").set("bottom", "0").set("left", "0")
		.set("display", "flex").set("align-items", "center").set("justify-content", "center");
		
		
		add(dialog, button);

	}


}