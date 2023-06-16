
package com.cinoteck.application.views.admin;



import com.cinoteck.application.utils.authentication.CurrentUser;
import com.vaadin.flow.server.VaadinServletService;
import com.vaadin.flow.server.VaadinSession;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.user.UserRole;

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

public final class LoginHelper {

	private LoginHelper() {
		// Hide Utility Class Constructor
	}

	public static boolean login(String username, String password) {
		System.out.println("___________________________________step 2");
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
		System.out.println("___________________________________stattus up");
		AuthenticationStatus status = securityContext.authenticate(
			VaadinServletService.getCurrentServletRequest(),
			VaadinServletService.getCurrentResponse().getHttpServletResponse(),
			authentication);
		
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
			
			return true;
		}

		return false;
	}

	/**
	 * Trigger logout event for Authentication Mechanism or other components to react.
	 */
	@SuppressWarnings("unchecked")
	public static boolean logout() {

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
		
System.out.println("___________________________________NOT SUCCESSFUL: remember to reload");
	//	UI.getCurrent().getPage().executeJs("window.location.reload();");
CurrentUser.set("");
		return true;
	}

	public static class LogoutEvent {
		
	}

}
