package com.cinoteck.application.views.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.security.GeneralSecurityException;


import com.cinoteck.application.views.MainLayout;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
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


	public SupportView() {
	    try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
		
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
	        Paragraph text = new Paragraph("The Afghanistan Polio Management Information System (APMIS) is an online data system that simplifies and improves the use and management of polio immunization-related data. APMIS facilitates field data entry, immunization data storage, data visualization, and real-time monitoring of polio immunization activities in Afghanistan.  Using this system will assist in evaluating immunization campaign activities and identifying program challenges.");
	        text.getStyle().set("color", "green");
	        text.getStyle().set("font-size", "20px");
	        text.getStyle().set("text-align", "justify");
	        text.getStyle().set("padding-top", "30px");

	        aboutText.add(text);


			Div feedbackFormFields = new Div();
	      //  Binder<Feedback> binder = new Binder<>(Feedback.class);

	    
	        
	        firstName.getStyle().set("color", "green");
	       
	     //   binder.forField(firstName).bind(Feedback::getFirstName, Feedback::setFirstName);

	        
	        lastName.getStyle().set("color", "green");
	       
	     //   binder.forField(lastName).bind(Feedback::getLastName, Feedback::setLastName);

	        
	        email.getStyle().set("color", "green");
	     //   binder.forField(email).bind(Feedback::getEmailAddress, Feedback::setEmailAddress);

	       
	        message.getStyle().set("color", "green");
	        message.getStyle().set("height", "370px");
	     //   binder.forField(message).bind(Feedback::getFeedback, Feedback::setFeedback);

	        Button sendFeedback = new Button("Send", new Icon("vaadin", "location-arrow-circle-o"));
	        sendFeedback.getStyle().set("color", "white");
	        sendFeedback.getStyle().set("background", "#0D6938");
	        sendFeedback.getStyle().set("width", "10%");
	        sendFeedback.getStyle().set("border-radius", "8px");

	        sendFeedback.addClickListener(click -> submitForm(firstName.getValue(), lastName.getValue(), email.getValue(), message.getValue()));
	     //   sendFeedback.setSuffixComponent(new Icon("vaadin", "building"));


	        message.setWidthFull();
	        message.setLabel("Feedback");

	        
	        feedbackForm.add(firstName, lastName, email, message);
	        feedbackForm.setResponsiveSteps(
	                // Use one column by default
	                new ResponsiveStep("0", 1),
	                // Use two columns, if layout's width exceeds 500px
	                new ResponsiveStep("500px", 2));
	        // Stretch the username field over 2 columns
	        feedbackForm.setColspan(email, 2);
	        feedbackForm.setColspan(message, 2);
	        feedbackForm.setColspan(sendFeedback, 0);

			feedbackFormFields.add(feedbackForm);

	        Paragraph versionNum = new Paragraph("Version Number : APMIS 5.0.0");
	        versionNum.getStyle().set("font-size", "12px");

			aboutView.add(aboutText, feedbackForm, sendFeedback, versionNum);
	        add(aboutView);
	}
	
	 private void submitForm(String firstName, String lastNamw, String email, String message) {
	        try {
	            ValueRange body = new ValueRange()
	                    .setValues(Arrays.asList(Arrays.asList(firstName, lastName, email, message)));
	            sheetsService.spreadsheets().values()
	                    .append(SPREADSHEET_ID, "Sheet1", body)
	                    .setValueInputOption("USER_ENTERED")
	                    .execute();
	            Notification.show("Form submitted successfully");
	            // Clear form fields or perform any other desired actions
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            // Handle the exception appropriately
	        }
	    }

}
