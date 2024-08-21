/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.cinoteck.application.views.utils;

import java.util.Objects;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;

@Tag("idle-notification")
@JsModule("/idle-notification.js")
@CssImport("/idle-notification.css")
@CssImport(value = "/vaadin-dialog-overlay.css", themeFor = "vaadin-dialog-overlay")
@Uses(Dialog.class)
public class IdleNotification extends Component {

    public static final int DEFAULT_SECONDS_BEFORE_NOTIFICATION = 120;
    public static final String DEFAULT_BEFORE_EXPIRE_MESSAGE = "Your session will expire in less than "
            + MessageFormatting.SECS_TO_TIMEOUT + " seconds. ";
    public static final String DEFAULT_AFTER_EXPIRE_MESSAGE = "Your session has expired due to inactivity.";

    public enum MessageFormatting {
        SECS_TO_TIMEOUT("${SECS_TO_TIMEOUT}"), SECS_MAX_IDLE_TIMEOUT("${SECS_MAX_IDLE_TIMEOUT}");

        public final String label;

        MessageFormatting(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private boolean closeButtonEnabled = false;
    private boolean extendSessionButtonEnabled = false;
    private boolean redirectButtonEnabled = false;
    private String message;
    private String extendSessionButtonCaption;
    private String redirectButtonCaption;
    private String redirectButtonUrl;
    private String redirectAtTimeoutUrl;
    private boolean redirectAtTimeoutEnabled = true;
    private String afterExpiredMessage;
    private Integer maxInactiveInterval;
    private Integer secondsBeforeNotification;
    private boolean extendSessionOnOutsideClick = true;
    private boolean closeNotificationOnOutsideClick = false;

    /**
     * Default constructor. Creates an idle notification with defaults.
     * <p>
     * {@link maxInactiveInterval} is obtained via
     * {@code VaadinSession.getCurrent().getSession().getMaxInactiveInterval()}
     *
     * @throws IllegalArgumentException
     *             If either {@code secondsBeforeNotification} or
     *             {@code maxInactiveInterval} value is smaller than 1 seconds,
     *             or if {@code secondsBeforeNotification} value is larger than,
     *             or equal to, {@code maxInactiveInterval}.
     *
     */
    public IdleNotification() throws IllegalArgumentException {
        this(DEFAULT_BEFORE_EXPIRE_MESSAGE, DEFAULT_SECONDS_BEFORE_NOTIFICATION);
    }

    /**
     * Creates an idle notification with the default message that will be
     * displayed to the user at the specified number of seconds before session
     * timeout.
     * <p>
     * {@link maxInactiveInterval} is obtained via
     * {@code VaadinSession.getCurrent().getSession().getMaxInactiveInterval()}
     *
     * @param secondsBeforeNotification
     *            the number of seconds before session times-out at which point
     *            the notification will be displayed to the user
     * @throws IllegalArgumentException
     *             If either {@code secondsBeforeNotification} or
     *             {@code maxInactiveInterval} value is smaller than 1 seconds,
     *             or if {@code secondsBeforeNotification} value is larger than,
     *             or equal to, {@code maxInactiveInterval}.
     */
    public IdleNotification(int secondsBeforeNotification) throws IllegalArgumentException {
        this(DEFAULT_BEFORE_EXPIRE_MESSAGE, secondsBeforeNotification,
                VaadinSession.getCurrent().getSession().getMaxInactiveInterval());
    }

    /**
     * Creates an idle notification with the specified message that will be
     * displayed to the user at the specified number of seconds before session
     * timeout.
     * <p>
     * {@link maxInactiveInterval} is obtained via
     * {@code VaadinSession.getCurrent().getSession().getMaxInactiveInterval()}
     *
     * @param message
     *            the text of the notification
     * @param secondsBeforeNotification
     *            the number of seconds before session times-out at which point
     *            the notification will be displayed to the user
     * @throws IllegalArgumentException
     *             If either {@code secondsBeforeNotification} or
     *             {@code maxInactiveInterval} value is smaller than 1 seconds,
     *             or if {@code secondsBeforeNotification} value is larger than,
     *             or equal to, {@code maxInactiveInterval}.
     */
    public IdleNotification(String message, int secondsBeforeNotification) throws IllegalArgumentException {
        this(message, secondsBeforeNotification, VaadinSession.getCurrent().getSession().getMaxInactiveInterval());
        System.out.println("______________getMaxInactiveInterval______________:" +VaadinSession.getCurrent().getSession().getMaxInactiveInterval());
    }

    /**
     * Creates an idle notification with the specified message that will be
     * displayed to the user at the specified number of seconds before session
     * timeout.
     * <p>
     * {@link maxInactiveInterval} is obtained via
     * {@code VaadinSession.getCurrent().getSession().getMaxInactiveInterval()}
     *
     * @param message
     *            the text of the notification
     * @param secondsBeforeNotification
     *            the number of seconds before session times-out at which point
     *            the notification will be displayed to the user
     * @param redirectAtTimeoutUrl
     *            The URL to redirect the page to upon session timeout
     * @throws IllegalArgumentException
     *             If either {@code secondsBeforeNotification} or
     *             {@code maxInactiveInterval} value is smaller than 1 seconds,
     *             or if {@code secondsBeforeNotification} value is larger than,
     *             or equal to, {@code maxInactiveInterval}.
     */
    public IdleNotification(String message, int secondsBeforeNotification, String redirectAtTimeoutUrl)
            throws IllegalArgumentException {
        this(message, secondsBeforeNotification, VaadinSession.getCurrent().getSession().getMaxInactiveInterval(),
                redirectAtTimeoutUrl);
    }

    /**
     * Creates an idle notification with the specified message that will be
     * displayed to the user at the specified number of seconds before session
     * timeout. This constructor allows the specification of an arbitrary
     * {@link maxInactiveInterval}, which doesn't have to coincide with the one
     * defined for the underlying HttpSession.
     *
     * @param message
     *            the text of the notification
     * @param secondsBeforeNotification
     *            the number of seconds before session times-out at which point
     *            the notification will be displayed to the user
     * @param maxInactiveInterval
     *            the number of seconds until the session times-out
     * @param redirectAtTimeoutUrl
     *            The URL to redirect the page to upon session timeout
     * @throws IllegalArgumentException
     *             If either {@code secondsBeforeNotification} or
     *             {@code maxInactiveInterval} value is smaller than 1 seconds,
     *             or if {@code secondsBeforeNotification} value is larger than,
     *             or equal to, {@code maxInactiveInterval}.
     */
    public IdleNotification(String message, int secondsBeforeNotification, int maxInactiveInterval,
            String redirectAtTimeoutUrl) throws IllegalArgumentException {
        this(message, secondsBeforeNotification, maxInactiveInterval);
        setRedirectAtTimeoutUrl(redirectAtTimeoutUrl);
    }

    /**
     * Creates an idle notification with the specified message that will be
     * displayed to the user at the specified number of seconds before session
     * timeout. This constructor allows the specification of an arbitrary
     * {@link maxInactiveInterval}, which doesn't have to coincide with the one
     * defined for the underlying HttpSession.
     *
     * @param message
     *            the text of the notification
     * @param secondsBeforeNotification
     *            the number of seconds before session times-out at which point
     *            the notification will be displayed to the user
     * @param maxInactiveInterval
     *            the number of seconds until the session times-out
     * @throws IllegalArgumentException
     *             If either {@code secondsBeforeNotification} or
     *             {@code maxInactiveInterval} value is smaller than 1 seconds,
     *             or if {@code secondsBeforeNotification} value is larger than,
     *             or equal to, {@code maxInactiveInterval}.
     */
    public IdleNotification(String message, int secondsBeforeNotification, int maxInactiveInterval)
            throws IllegalArgumentException {
        if ((secondsBeforeNotification < 1) || (maxInactiveInterval < 1)) {
            throw new IllegalArgumentException("secondsBeforeNotification & maxInactiveInterval must be both > 1");
        }
        if (secondsBeforeNotification >= maxInactiveInterval) {
            throw new IllegalArgumentException("secondsBeforeNotification must be less than maxInactiveInterval");
        }
        setMessage(message);
        setSecondsBeforeNotification(secondsBeforeNotification);
        setMaxInactiveInterval(maxInactiveInterval);
        if (afterExpiredMessage == null) {
            setAfterExpiredMessage(DEFAULT_AFTER_EXPIRE_MESSAGE);
        }
    }

    /**
     * Adds a button to close the notification without poking the server
     */
    public void addCloseButton() {
        setCloseButtonEnabled(true);
    }

    /**
     * Remove the close Button
     */
    public void removeCloseButton() {
        setCloseButtonEnabled(false);
    }

    /**
     * Get the text of the notification
     *
     * @return the text of the notification
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the text of the notification with given String
     * <p>
     * NOTE: {@link MessageFormatting} can be used to format the message
     *
     * @param message
     *            the text of the Notification
     */
    public void setMessage(String message) {
        Objects.requireNonNull(message, "message should not be null");
        this.message = message;
        getElement().setProperty("beforeExpiredMessage", message);
    }

    /**
     * Add a button to the notification that allows the user to poke the server,
     * thereby extending the session
     */
    public void addExtendSessionButton(String caption) {
        Objects.requireNonNull(caption, "caption should not be null");
        setExtendSessionButtonCaption(caption);
        setExtendSessionButtonEnabled(true);
    }

    /**
     * Remove the extend-session button
     */
    public void removeExtendSessionButton() {
        setExtendSessionButtonEnabled(false);
    }

    /**
     * Add a button to the notification that allows the user to be immediately
     * redirected to a different url (e.g. a logout url)
     */
    public void addRedirectButton(String caption, String url) {
        Objects.requireNonNull(caption, "caption should not be null");
        Objects.requireNonNull(url, "url should not be null");
        setRedirectButtonCaption(caption);
        setRedirectButtonUrl(url);
        setRedirectButtonEnabled(true);
    }

    /**
     * Remove the redirect button
     */
    public void removeRedirectButton() {
        setRedirectButtonEnabled(false);
    }

    /**
     * Get the URL to redirect the page to upon session timeout
     */
    public String getRedirectAtTimeoutUrl() {
        return redirectAtTimeoutUrl;
    }

    /**
     * The URL to redirect the page to upon session timeout. Setting this value
     * to null will disable URL redirection upon timeout
     */
    public void setRedirectAtTimeoutUrl(String redirectAtTimeoutUrl) {
        this.redirectAtTimeoutUrl = redirectAtTimeoutUrl;
        getElement().setProperty("redirectAtTimeoutUrl", redirectAtTimeoutUrl);
        setRedirectAtTimeoutEnabled(redirectAtTimeoutUrl != null);
    }

    /**
     * Get the message displayed to the user after the session expires
     */
    public String getAfterExpiredMessage() {
        return afterExpiredMessage;
    }

    /**
     * Set the message displayed to the user after the session expires
     * <p>
     *
     * NOTE: this message would only be displayed if
     * {@link #redirectAtTimeoutUrl} is not set
     */
    public void setAfterExpiredMessage(String afterExpiredMessage) {
        Objects.requireNonNull(afterExpiredMessage, "afterExpiredMessage should not be null");
        this.afterExpiredMessage = afterExpiredMessage;
        getElement().setProperty("afterExpiredMessage", afterExpiredMessage);
    }

    /**
     * Get the number of seconds until the session times-out
     * <p>
     * NOTE: This getter's return value doesn't have to coincide with the one
     * defined for the underlying HttpSession.
     */
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    /**
     * Set the number of seconds until the session times-out
     * <p>
     * NOTE: This setter allows the specification of an arbitrary
     * {@link maxInactiveInterval}, which doesn't have to coincide with the one
     * defined for the underlying HttpSession.
     *
     * @throws IllegalArgumentException
     *             If {@code maxInactiveInterval} value is smaller than 1
     *             seconds, or if {@code secondsBeforeNotification} value is
     *             larger than, or equal to, {@code maxInactiveInterval}.
     */
    public void setMaxInactiveInterval(int maxInactiveInterval) throws IllegalArgumentException {
        if ((maxInactiveInterval < 1)) {
            throw new IllegalArgumentException("maxInactiveInterval must be > 1");
        }
        if ((secondsBeforeNotification != null) && (secondsBeforeNotification >= maxInactiveInterval)) {
            throw new IllegalArgumentException("secondsBeforeNotification must be less than maxInactiveInterval");
        }
        this.maxInactiveInterval = maxInactiveInterval;
        getElement().setProperty("maxInactiveInterval", maxInactiveInterval);
    }

    /**
     * Gets the number of seconds before session times-out at which point the
     * notification will be displayed to the user
     */
    public int getSecondsBeforeNotification() {
        return secondsBeforeNotification;
    }

    /**
     * Sets the number of seconds before session times-out at which point the
     * notification will be displayed to the user
     *
     * @throws IllegalArgumentException
     *             If {@code secondsBeforeNotification} value is smaller than 1
     *             seconds, or if {@code secondsBeforeNotification} value is
     *             larger than, or equal to, {@code maxInactiveInterval}.
     */
    public void setSecondsBeforeNotification(int secondsBeforeNotification) throws IllegalArgumentException {
        if ((secondsBeforeNotification < 1)) {
            throw new IllegalArgumentException("secondsBeforeNotification must be > 1");
        }
        if ((maxInactiveInterval != null) && (secondsBeforeNotification >= maxInactiveInterval)) {
            throw new IllegalArgumentException("secondsBeforeNotification must be less than maxInactiveInterval");
        }
        this.secondsBeforeNotification = secondsBeforeNotification;
        getElement().setProperty("secondsBeforeNotification", secondsBeforeNotification);
    }

    public boolean isExtendSessionOnOutsideClick() {
        return extendSessionOnOutsideClick;
    }

    /**
     * Determines whether an outside click would poke the server (thereby
     * extending the session)
     */
    public void setExtendSessionOnOutsideClick(boolean extendSessionOnOutsideClick) {
        this.extendSessionOnOutsideClick = extendSessionOnOutsideClick;
        getElement().setProperty("extendSessionOnOutsideClick", extendSessionOnOutsideClick);
    }

    public boolean isCloseNotificationOnOutsideClick() {
        return closeNotificationOnOutsideClick;
    }

    /**
     * Determines whether an outside click would close the notification without
     * poking the server
     * <p>
     * NOTE: this would only have an effect if
     * ({@link #extendSessionOnOutsideClick} == false)
     *
     */
    public void setCloseNotificationOnOutsideClick(boolean closeNotificationOnOutsideClick) {
        this.closeNotificationOnOutsideClick = closeNotificationOnOutsideClick;
        getElement().setProperty("closeNotificationOnOutsideClick", closeNotificationOnOutsideClick);
    }

    /**
     * Adds a open handler
     */
    public Registration addOpenListener(ComponentEventListener<OpenEvent> listener) {
        return addListener(OpenEvent.class, listener);
    }

    /**
     * Adds a close handler
     */
    public Registration addCloseListener(ComponentEventListener<CloseEvent> listener) {
        return addListener(CloseEvent.class, listener);
    }

    /**
     * Adds a session extend handler
     */
    public Registration addExtendSessionListener(ComponentEventListener<ExtendSessionEvent> listener) {
        return addListener(ExtendSessionEvent.class, listener);
    }

    /**
     * Adds a redirect handler
     */
    public Registration addRedirectListener(ComponentEventListener<RedirectEvent> listener) {
        return addListener(RedirectEvent.class, listener);
    }

    /**
     * Adds a timeout handler
     */
    public Registration addTimeoutListener(ComponentEventListener<TimeoutEvent> listener) {
        return addListener(TimeoutEvent.class, listener);
    }

    /**
     * Used to extend the session from the client side
     */
    @ClientCallable
    private boolean pokeServer() {
    	System.out.println("Sever Pokedd-----------------------------");
        return true;
    }
    
//    @ClientCallable
//    public void _pokeServer(e) {
//    	 this.$server.pokeServer().then((result) => {
//    	        if (result === true) {
//    	            console.log('Idle-Notification: Server poked successfully. Session extended.');
//    	            this.opened = false;
//    	            this._resetTimer();
//    	        } else {
//    	            console.error('Could not poke the server');
//    	        }
//    	    });
////        return true;
//    }

    private boolean isCloseButtonEnabled() {
        return closeButtonEnabled;
    }

    private void setCloseButtonEnabled(boolean closeButtonEnabled) {
        this.closeButtonEnabled = closeButtonEnabled;
        getElement().setProperty("closeButtonEnabled", closeButtonEnabled);
    }

    private boolean isRedirectButtonEnabled() {
        return redirectButtonEnabled;
    }

    private void setRedirectButtonEnabled(boolean redirectButtonEnabled) {
        this.redirectButtonEnabled = redirectButtonEnabled;
        getElement().setProperty("redirectButtonEnabled", redirectButtonEnabled);
    }

    private boolean isExtendSessionButtonEnabled() {
        return extendSessionButtonEnabled;
    }

    /**
     * Determines whether an extend-session button is displayed to the user
     */
    private void setExtendSessionButtonEnabled(boolean extendSessionButtonEnabled) {
        this.extendSessionButtonEnabled = extendSessionButtonEnabled;
        getElement().setProperty("extendSessionButtonEnabled", extendSessionButtonEnabled);
    }

    private String getExtendSessionButtonCaption() {
        return extendSessionButtonCaption;
    }

    /**
     * Sets the caption of the extend-session button
     */
    private void setExtendSessionButtonCaption(String extendSessionButtonCaption) {
        Objects.requireNonNull(extendSessionButtonCaption, "extendSessionButtonCaption should not be null");
        this.extendSessionButtonCaption = extendSessionButtonCaption;
        getElement().setProperty("extendSessionButtonCaption", extendSessionButtonCaption);
    }

    private String getRedirectButtonCaption() {
        return redirectButtonCaption;
    }

    private void setRedirectButtonCaption(String redirectButtonCaption) {
        Objects.requireNonNull(redirectButtonCaption, "redirectButtonCaption should not be null");
        this.redirectButtonCaption = redirectButtonCaption;
        getElement().setProperty("redirectButtonCaption", redirectButtonCaption);
    }

    private String getRedirectButtonUrl() {
        return redirectButtonUrl;
    }

    private void setRedirectButtonUrl(String redirectButtonUrl) {
        Objects.requireNonNull(redirectButtonUrl, "redirectButtonUrl should not be null");
        this.redirectButtonUrl = redirectButtonUrl;
        getElement().setProperty("redirectButtonUrl", redirectButtonUrl);
    }

    private boolean isRedirectAtTimeoutEnabled() {
        return redirectAtTimeoutEnabled;
    }

    /**
     * Determines whether the user is redirected to the
     * {@link #redirectAtTimeoutUrl} upon session timeout.
     * <p>
     *
     * NOTE: this would only have an effect if {@link #redirectAtTimeoutUrl} is
     * set
     */
    private void setRedirectAtTimeoutEnabled(boolean redirectAtTimeoutEnabled) {
        this.redirectAtTimeoutEnabled = redirectAtTimeoutEnabled;
        getElement().setProperty("redirectAtTimeoutEnabled", redirectAtTimeoutEnabled);
    }

    @DomEvent("vaadin-idle-notification-open")
    public static class OpenEvent extends ComponentEvent<IdleNotification> {
        public OpenEvent(IdleNotification source, boolean fromClient) { super(source, fromClient); }
    }

    @DomEvent("vaadin-idle-notification-close")
    public static class CloseEvent extends ComponentEvent<IdleNotification> {
        public CloseEvent(IdleNotification source, boolean fromClient) { super(source, fromClient); }
    }

    @DomEvent("vaadin-idle-notification-extend-session")
    public static class ExtendSessionEvent extends ComponentEvent<IdleNotification> {
        public ExtendSessionEvent(IdleNotification source, boolean fromClient) { super(source, fromClient); }
    }

    @DomEvent("vaadin-idle-notification-redirect")
    public static class RedirectEvent extends ComponentEvent<IdleNotification> {
        public RedirectEvent(IdleNotification source, boolean fromClient) { super(source, fromClient); }
    }

    @DomEvent("vaadin-idle-notification-timeout")
    public static class TimeoutEvent extends ComponentEvent<IdleNotification> {
        public TimeoutEvent(IdleNotification source, boolean fromClient) { super(source, fromClient); }
    }
}
