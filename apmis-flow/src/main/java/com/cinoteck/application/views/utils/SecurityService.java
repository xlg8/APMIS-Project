//package com.cinoteck.application.views.utils;
//
//
//
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
//import org.springframework.stereotype.Component;
//
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.server.VaadinServletRequest;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//@Component
//public class SecurityService {
//
//    private static final String LOGOUT_SUCCESS_URL = "/";
//
//    public UserDetails getAuthenticatedUser() {
//        SecurityContext context = SecurityContextHolder.getContext();
//        Object principal = context.getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            return (UserDetails) context.getAuthentication().getPrincipal();
//        }
//        // Anonymous or no authentication.
//        return null;
//    }
//
//    public void logout() {
//        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
//        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
//        logoutHandler.logout((HttpServletRequest) VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
//    }
//}