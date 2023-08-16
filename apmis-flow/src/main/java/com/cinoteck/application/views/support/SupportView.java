package com.cinoteck.application.views.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.security.GeneralSecurityException;

import com.cinoteck.application.UserProvider;
import com.cinoteck.application.views.MainLayout;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;

@PageTitle("Support")
@Route(value = "support", layout = MainLayout.class)
public class SupportView extends VerticalLayout {
	TextField firstName = new TextField("First name");
	TextField lastName = new TextField("Last name");
	TextField email = new TextField("Email");
	TextArea message = new TextArea();
	FormLayout feedbackForm = new FormLayout();

	private static final String APPLICATION_NAME = "Your Application Name";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	private static final String SPREADSHEET_ID = "your-spreadsheet-id";

	private Sheets sheetsService;
	UserProvider userProvider = new UserProvider();
	
	public SupportView() {
		if (I18nProperties.getUserLanguage() == null) {

			I18nProperties.setUserLanguage(Language.EN);
		} else {

			I18nProperties.setUserLanguage(userProvider.getUser().getLanguage());
			I18nProperties.getUserLanguage();
		}
		FacadeProvider.getI18nFacade().setUserLanguage(userProvider.getUser().getLanguage());
		setSizeFull();
		setSizeFull();

		Div aboutView = new Div();

		aboutView.setId("aboutView");
		aboutView.getStyle().set("height", "100%");
		aboutView.getStyle().set("padding-left", "90px");
		aboutView.getStyle().set("padding-right", "90px");

		Div apmisImageContainer = new Div();
		aboutView.setId("apmisImageContainer");
		apmisImageContainer.getStyle().set("height", "140px");
		apmisImageContainer.getStyle().set("display", "flex");
		apmisImageContainer.getStyle().set("justify-content", "center");

		Image img = new Image("images/apmislogo.png", "APMIS-LOGO");
		img.getStyle().set("max-height", "-webkit-fill-available");

		apmisImageContainer.add(img);

		Div aboutText = new Div();
//			aboutText.getStyle().set("height", "121px");
		Paragraph text = new Paragraph(I18nProperties.getString(Strings.supportPageHeaderText));
		text.getStyle().set("color", "black");
		text.getStyle().set("font-size", "20px");
		text.getStyle().set("text-align", "justify");
		text.getStyle().set("padding-top", "30px");

		aboutText.add(text);

		Html html = new Html(
				"<iframe src='feedbackform/feedbackforminput.html' style='width:100%; height:79vh; border: 0px;'></iframe>");

		Paragraph versionNum = new Paragraph("APMIS Version Number : APMIS 5.0.0");
		versionNum.getStyle().set("font-size", "15px");
		versionNum.getStyle().set("font-weight", "500");
		versionNum.getStyle().set("color", "#0D6938");

		aboutView.add(aboutText, html, versionNum);

		add(aboutView);

	}

}
