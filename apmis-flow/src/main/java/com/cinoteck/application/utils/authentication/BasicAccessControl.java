package com.cinoteck.application.utils.authentication;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.ServletException;

import com.cinoteck.application.views.admin.LoginHelper.LogoutEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserRole;

/**
 * Default mock implementation of {@link AccessControl}. This implementation
 * accepts any string as a user if the password is the same string, and
 * considers the user "admin" as the only administrator.
 */
public class BasicAccessControl implements AccessControl {

    /**
	 * 
	 */
	private static final long serialVersionUID = -884441673838115907L;

	@Override
    public boolean signIn(String username, String password) {
//        if (username == null || username.isEmpty()) {
//            return false;
//        }
//
//        if (!username.equals(password)) {
//            return false;
//        }
		
		
		
		System.out.println(username+"___________________________________step 2"+password);
		if (username == null || username.isEmpty()) {
			return false;
		}
		System.out.println("___________________________________step 3");
		BeanManager bm = CDI.current().getBeanManager();
		@SuppressWarnings("unchecked")
		Bean<SecurityContext> securityContextBean = (Bean<SecurityContext>) bm.getBeans(SecurityContext.class).iterator().next();
		CreationalContext<SecurityContext> ctx = bm.createCreationalContext(securityContextBean);
		SecurityContext securityContext = (SecurityContext) bm.getReference(securityContextBean, SecurityContext.class, ctx);
		System.out.println("___________________________________step 4");
		AuthenticationParameters authentication = new AuthenticationParameters();
		authentication.credential(new UsernamePasswordCredential(username, password));
		authentication.newAuthentication(true);
		authentication.setRememberMe(true);
		System.out.println(authentication+"___________________________________stattus up");
		AuthenticationStatus status = securityContext.authenticate(
			VaadinServletService.getCurrentServletRequest(),
			VaadinServletService.getCurrentResponse().getHttpServletResponse(),
			authentication);
		
		System.out.println(status);
		
		if (status == AuthenticationStatus.SUCCESS) {
			if (!VaadinServletService.getCurrentServletRequest().isUserInRole(UserRole._USER)) {
				try {
					VaadinServletService.getCurrentServletRequest().logout();
				} catch (ServletException e) {
					// just do not crash
				}
				return false;
			}
			
			
			Language userLanguage = FacadeProvider.getUserFacade().getByUserName(username).getLanguage();
			I18nProperties.setUserLanguage(userLanguage);
			FacadeProvider.getI18nFacade().setUserLanguage(userLanguage);
			
			
			System.out.println("+++++++++++++++" +FacadeProvider.getUserFacade().getCurrentUser().getUserName());
			 CurrentUser.set(username);
			return true;
		}

		return false;

       
       // return true;
    }

    @Override
    public boolean isUserSignedIn() {
    	System.out.println(!CurrentUser.get().isEmpty()+"_____:check if has log in");
        return !CurrentUser.get().isEmpty();
    }

//    @Override
//    public boolean isUserInRole(String role) {
//        if ("admin".equals(role)) {
//            // Only the "admin" user is in the "admin" role
//            return getPrincipalName().equals("admin");
//        }
//
//        // All users are in all non-admin roles
//        return true;
//    }

//    @Override
//    public String getPrincipalName() {
//        return CurrentUser.get();
//    }

    @Override
    public void signOut() {
//        VaadinSession.getCurrent().getSession().invalidate();
//        UI.getCurrent().navigate("");
    	
    	
    	
    	BeanManager bm = CDI.current().getBeanManager();
		Bean<Event> eventBean = (Bean<Event>) bm.getBeans(Event.class).iterator().next();
		CreationalContext<Event> ctx = bm.createCreationalContext(eventBean);
		Event<LogoutEvent> event = (Event<LogoutEvent>) bm.getReference(eventBean, Event.class, ctx);
		event.fire(new LogoutEvent());

		try {
			VaadinServletService.getCurrentServletRequest().logout();
		} catch (javax.servlet.ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		VaadinSession.getCurrent().getSession().invalidate();
		CurrentUser.set("");
		System.out.println(CurrentUser.get().isEmpty()+ " :______________XXXSingOUT_____________________NOT SUCCESSFUL: remember to reload");
		UI.getCurrent().getPage().executeJs("window.location.reload();");

		
		//return true;
		
		
    }
}
