package com.cinoteck.application.views.utils;

import com.cinoteck.application.utils.authentication.AccessControl;
import com.cinoteck.application.utils.authentication.AccessControlFactory;
import com.cinoteck.application.utils.authentication.LoginView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
//import org.vaadin.example.bookstore.authentication.AccessControl;
//import org.vaadin.example.bookstore.authentication.AccessControlFactory;
//import org.vaadin.example.bookstore.ui.login.LoginScreen;

/**
 * This class is used to listen to BeforeEnter event of all UIs in order to
 * check whether a user is signed in or not before allowing entering any page.
 * It is registered in a file named
 * com.vaadin.flow.server.VaadinServiceInitListener in META-INF/services.
 */
public class APMISInitListener implements VaadinServiceInitListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8385570446755288676L;

	@Override
    public void serviceInit(ServiceInitEvent initEvent) {
        final AccessControl accessControl = AccessControlFactory.getInstance()
                .createAccessControl();

        initEvent.getSource().addUIInitListener(uiInitEvent -> {
            uiInitEvent.getUI().addBeforeEnterListener(enterEvent -> {
            	System.out.println(accessControl.isUserSignedIn()+"+++++++++++++++++++++++++++________________"+enterEvent.getNavigationTarget());
                if (!accessControl.isUserSignedIn() && !LoginView.class
                        .equals(enterEvent.getNavigationTarget()))
                    enterEvent.rerouteTo(LoginView.class);
            });
        });
    }
}