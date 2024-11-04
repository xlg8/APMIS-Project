package com.cinoteck.application.utils.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.symeda.sormas.api.FacadeProvider;

@WebServlet("/resetPasswordServlet")
public class ResetPasswordServlet extends HttpServlet {

//    @Inject
    private FacadeProvider userService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userUuid = request.getParameter("userUuid");
        String customPassword = request.getParameter("customPassword");

        if (userUuid != null && !userUuid.isEmpty() && customPassword != null && !customPassword.isEmpty()) {
            userService.getUserFacade().setCustomPassword(userUuid, customPassword);
            response.getWriter().println("Password reset successfully.");
        } else {
            response.getWriter().println("Invalid input. Please provide both User UUID and password.");
        }
    }
}
