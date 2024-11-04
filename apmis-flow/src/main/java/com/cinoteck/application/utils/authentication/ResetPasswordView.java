package com.cinoteck.application.utils.authentication;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Route("reset-password")
public class ResetPasswordView extends FormLayout {

    private TextField userUuidField;
    private PasswordField customPasswordField;
    private PasswordField confirmPasswordField;
    private Button resetPasswordButton;

    @Inject
    private FacadeProvider userService; // Inject the service provider

    public ResetPasswordView() {
        userUuidField = new TextField("User UUID");
        customPasswordField = new PasswordField("New Password");
        confirmPasswordField = new PasswordField("Confirm Password");
        resetPasswordButton = new Button("Reset Password");

        resetPasswordButton.addClickListener(event -> handleResetPassword());

        add(userUuidField, customPasswordField, confirmPasswordField, resetPasswordButton);
    }

    private void handleResetPassword() {
        String userUuid = userUuidField.getValue();
        String customPassword = customPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validate inputs
        if (isInputValid(userUuid, customPassword, confirmPassword)) {
            try {
                // Send POST request to ResetPasswordServlet
                String response = sendPostRequest(userUuid, customPassword);
                Notification.show(response);
            } catch (IOException e) {
                Notification.show("An error occurred while resetting the password: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        }
    }

    private boolean isInputValid(String userUuid, String customPassword, String confirmPassword) {
        if (userUuid == null || userUuid.isEmpty()) {
            Notification.show("User UUID cannot be empty", 3000, Notification.Position.MIDDLE);
            return false;
        }
        if (customPassword == null || customPassword.isEmpty()) {
            Notification.show("Password cannot be empty", 3000, Notification.Position.MIDDLE);
            return false;
        }
        if (!customPassword.equals(confirmPassword)) {
            Notification.show("Passwords do not match", 3000, Notification.Position.MIDDLE);
            return false;
        }
        if (customPassword.length() < 8) {
            Notification.show("Password must be at least 8 characters long", 3000, Notification.Position.MIDDLE);
            return false;
        }
        return true;
    }

    private String sendPostRequest(String userUuid, String customPassword) throws IOException {
        String urlString = "http://localhost:6080/apmis-flow/resetPasswordServlet"; // Adjust to your servlet URL
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String postData = "userUuid=" + userUuid + "&customPassword=" + customPassword;
        conn.getOutputStream().write(postData.getBytes(StandardCharsets.UTF_8));

        // Handle response
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                return scanner.useDelimiter("\\A").next();
            }
        } else {
            return "Failed to reset password. HTTP error code: " + responseCode;
        }
    }
}