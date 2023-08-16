package com.cinoteck.application.utils.authentication;

import java.io.Serializable;

/**
 * Simple interface for authentication and authorization checks.
 */
public interface AccessControl extends Serializable {

    String ADMIN_ROLE_NAME = "admin";
    String ADMIN_USERNAME = "admin";

    boolean signIn(String username, String password);
    
    boolean upDatePassWordCheck(String username, String password);


    boolean isUserSignedIn();

 //   boolean isUserInRole(String role);

 //   String getPrincipalName();

    void signOut(String oldUri);
}
