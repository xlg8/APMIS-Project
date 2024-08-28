package com.cinoteck.application.views.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;

@Tag("onUserInteraction")
public class SessionTimeout extends Component {
    private static final int SESSION_TIMEOUT = 30; // 10 minutes in seconds
    private long lastInteractionTime;
    private Dialog timeoutDialog;

    public SessionTimeout() {
    	
        // Initialize lastInteractionTime
        lastInteractionTime = System.currentTimeMillis() / 1000;

    	System.out.println("+++++++++++++++++checkSessionTimeout++++++++++++++++++++++:"+lastInteractionTime);

    	
        // Register the JavaScript listener
        UI.getCurrent().getPage().executeJs("document.addEventListener('mousemove', () => $0.$server.onUserInteraction())", getElement());

        // Create the timeout dialog
        timeoutDialog = new Dialog();
        timeoutDialog.setCloseOnEsc(false);
        timeoutDialog.setCloseOnOutsideClick(false);

        Button stayLoggedInButton = new Button("Stay Logged In", event -> {
            timeoutDialog.close();
            resetSessionTimeout();
        });

        timeoutDialog.add(
            new Notification("Your session will expire soon due to inactivity."),
               // .withPosition(Notification.Position.MIDDLE)
               // .withDuration(0)
               // .withVariants(NotificationVariant.LUMO_ERROR),
            stayLoggedInButton
        );
        
        stayLoggedInButton.addClickListener(e->{
        	 timeoutDialog.close();
             resetSessionTimeout();
        });

        resetSessionTimeout();
    }

    public void onUserInteraction() {
        lastInteractionTime = System.currentTimeMillis() / 1000;
        resetSessionTimeout();
    }

    private void resetSessionTimeout() {
//        if (timeoutDialog.isOpened()) {
//            timeoutDialog.close();
//        }

        long currentTime = System.currentTimeMillis() / 1000;
        long elapsedTime = currentTime - lastInteractionTime;

        if (elapsedTime >= SESSION_TIMEOUT - 60) {
//            timeoutDialog.open();
        	 UI.getCurrent().getPage().executeJs(
                     "document.querySelector('idle-notification').opened = true");
        }
    }
}
