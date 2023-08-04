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

import "@vaadin/button";
import "@vaadin/dialog";
import { dialogRenderer } from "@vaadin/dialog/lit.js";
import { ThemableMixin } from "@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js";
import { html, LitElement } from "lit";

class IdleNotification extends ThemableMixin(LitElement) {
  static get properties() {
    return {
      /**
       * Set to true when the notification is open
       * @type {boolean}
       */
      opened: {
        type: Boolean,

        notify: true,
        reflect: true,
      },

      /**
       * Determines whether a close button is displayed to the user
       * @type {boolean}
       */
      closeButtonEnabled: {
        type: Boolean,

        notify: true,
      },

      /**
       * Determines whether an extend-session button is displayed to the user
       * @type {boolean}
       */
      extendSessionButtonEnabled: {
        type: Boolean,

        notify: true,
      },

      /**
       * Determines whether a re-direct button is displayed to the user
       * @type {boolean}
       */
      redirectButtonEnabled: {
        type: Boolean,

        notify: true,
      },

      /**
       * The message displayed to the user before the session expires
       * @type {string}
       */
      beforeExpiredMessage: {
        type: String,

        notify: true,
      },

      /**
       * The caption of the extend-session button
       * @type {string}
       */
      extendSessionButtonCaption: {
        type: String,
      },

      /**
       * The caption of the re-direct button
       * @type {string}
       */
      redirectButtonCaption: {
        type: String,
      },

      /**
       * The url to redirect the page to upon re-direct-button click
       * @type {string}
       */
      redirectButtonUrl: {
        type: String,
      },

      /**
       * The url to redirect the page to upon session timeout
       * @type {string}
       */
      redirectAtTimeoutUrl: {
        type: String,
      },

      /**
       * Determines whether the user is redirected to the redirectUrl upon session timeout
       *
       * NOTE: this would only have an effect if redirectAtTimeoutUrl is set
       * @type {boolean}
       */
      redirectAtTimeoutEnabled: {
        type: Boolean,
      },

      /**
       * The message displayed to the user after the session expires
       * @type {string}
       */
      afterExpiredMessage: {
        type: String,
      },

      /**
       * The number of seconds until the session timesout
       * @type {number}
       */
      maxInactiveInterval: {
        type: Number,
      },

      /**
       * The number of seconds before session timesout at which point
       * the notification will be displayed to the user
       * @type {number}
       */
      secondsBeforeNotification: {
        type: Number,
      },

      /**
       * Set to true if the time-settings are valid
       * @type {boolean}
       */
      activated: {
        type: Boolean,

        notify: true,
        reflect: true,
      },

      /**
       * Determines whether an outside click would poke the server (thereby extending the session)
       * @type {boolean}
       */
      extendSessionOnOutsideClick: {
        type: Boolean,
      },

      /**
       * Determines whether an outside click would close the notification.
       *
       * NOTE: this would only have an effect if (extendSessionOnOutsideClick == false)
       *
       * @type {boolean}
       */
      closeNotificationOnOutsideClick: {
        type: Boolean,
      },

      /** @private */
      _timeoutObj: Object,

      /** @private */
      _messageContent: String,

      /** @private */
      _actionsVisibility: {
        type: String,
      },

      /** @private */
      _redirectButtonVisibility: {
        type: String,
      },

      /** @private */
      _extendSessionButtonVisibility: {
        type: String,
      },

      /** @private */
      _headerVisibility: {
        type: String,
      },

      /** @private */
      _displayProcessStarted: Boolean,

      /** @private */
      _dialogElement: Object,

      /** @private */
      _handleLoadListener: {
        type: Object,
      },
    };
  }

  // render() {
  //   return html`
  //     <vaadin-dialog
  //       id="notification-dialog"
  //       part="notification-dialog"
  //       theme="notification-dialog-theme"
  //       ${dialogRenderer(this.renderDialog, [this._messageContent])}
  //     >
  //     </vaadin-dialog>
  //   `;
  // }


  render() {
    const dialogContent = this.renderDialog(); // Call renderDialog function
    return html`
      <vaadin-dialog
        id="notification-dialog"
        part="notification-dialog"
        theme="notification-dialog-theme"
      >
        ${dialogContent} <!-- Use the rendered dialog content here -->
      </vaadin-dialog>
    `;
  }



  renderDialog = () => html`
    <div part="container" class="idle-notification-container">
      <div part="header" class="${this._headerVisibility}">
        <vaadin-icon
          icon="vaadin:close-small"
          id="close-icon"
          @click="${this._handleCloseIconClick}"
        ></vaadin-icon>
      </div>
      <div part="message-container">
        <vaadin-icon
          icon="vaadin:exclamation-circle-o"
          id="warning-icon"
        ></vaadin-icon>
        <div part="message">${this._messageContent}</div>
      </div>
      <div part="actions" class="${this._actionsVisibility}">
        <vaadin-button
          id="redirect"
          @click="${this._handleRedirectButtonClick}"
          class="${this._redirectButtonVisibility}"
        >
          ${this.redirectButtonCaption}
        </vaadin-button>
        <vaadin-button
          id="extend-session"
          @click="${this._handleExtendSessionButtonClick}"
          class="${this._extendSessionButtonVisibility}"
        >
          ${this.extendSessionButtonCaption}
        </vaadin-button>
      </div>
    </div>
  `;

  /** @protected */
  firstUpdated(_changedProperties) {
    super.firstUpdated(_changedProperties);

    this._dialogElement = this.shadowRoot.querySelector("vaadin-dialog");
    this._dialogElement.$.overlay.addEventListener(
      "vaadin-overlay-outside-click",
      this._handleOutsideClick.bind(this)
    );
    this._dialogElement.noCloseOnOutsideClick =
      !this.closeNotificationOnOutsideClick;
    this._dialogElement.noCloseOnEsc = true;
    this._dialogElement.modeless = false;

    this._registerXhrListener();
  }

  /** @protected */
  disconnectedCallback() {
    super.disconnectedCallback();
    this.removeEventListener("load", this._handleLoadListener);
    this._clearTimeoutObject();
    // to not trigger _handleLoad after disconnecting the component
    this._displayProcessStarted = true;
  }

  /** @private */
  _registerXhrListener() {
    let currRequest;
    let origOpen = XMLHttpRequest.prototype.open;
    let thisComponent = this;
    XMLHttpRequest.prototype.open = function () {
      currRequest = this;
      thisComponent._handleLoadListener = (e) =>
        thisComponent._handleLoad(currRequest);
      this.addEventListener("load", thisComponent._handleLoadListener);
      origOpen.apply(this, arguments);
    };
  }

  /** @private */
  _handleLoad(currRequest) {
    if (
      currRequest.status === 200 &&
      !this._displayProcessStarted &&
      this._isVaadinRequest(currRequest)
    ) {
      this._resetTimer();
      if (this.opened) {
        this.opened = false;
      }
    }
  }

  /** @private */
  _closeButtonEnabledChanged(isCloseButtonEnabled, wasCloseButtonEnabled) {
    if (isCloseButtonEnabled) {
      this._headerVisibility = "visible";
    } else {
      this._headerVisibility = "hidden";
    }
  }

  /** @private */
  _extendSessionButtonEnabledChanged(
    isExtendSessionButtonEnabled,
    wasExtendSessionButtonEnabled
  ) {
    if (isExtendSessionButtonEnabled) {
      this._extendSessionButtonVisibility = "visible";
      this._actionsVisibility = "visible";
    } else if (
      !(
        this.redirectButtonEnabled &&
        this.redirectButtonCaption &&
        this.redirectUrl
      )
    ) {
      this._actionsVisibility = "hidden";
    }
  }

  /** @private */
  _redirectButtonEnabledChanged(
    isRedirectButtonEnabled,
    wasRedirectButtonEnabled
  ) {
    this._validateRedirectButton(
      isRedirectButtonEnabled,
      this.redirectButtonCaption,
      this.redirectButtonUrl
    );
  }

  /** @private */
  _redirectButtonCaptionChanged(
    newRedirectButtonCaption,
    oldRedirectButtonCaption
  ) {
    this._validateRedirectButton(
      this.redirectButtonEnabled,
      newRedirectButtonCaption,
      this.redirectButtonUrl
    );
  }

  /** @private */
  _redirectButtonUrlChanged(newRedirectButtonUrl, oldRedirectButtonUrl) {
    this._validateRedirectButton(
      this.redirectButtonEnabled,
      this.redirectButtonCaption,
      newRedirectButtonUrl
    );
  }

  /** @private */
  _validateRedirectButton(
    isRedirectButtonEnabled,
    redirectButtonCaption,
    redirectButtonUrl
  ) {
    if (isRedirectButtonEnabled && redirectButtonCaption && redirectButtonUrl) {
      this._redirectButtonVisibility = "visible";
      this._actionsVisibility = "visible";
    } else if (!this.extendSessionButtonEnabled) {
      this._actionsVisibility = "hidden";
    }
  }

  /** @private */
  _activatedChanged(isActivated, wasActivated) {
    if (isActivated && !wasActivated) {
      this._resetTimer();
    } else {
      this._clearTimeoutObject();
    }
  }

  /** @private */
  _handleCloseIconClick(e) {
    this.opened = false;
  }

  /** @private */
  _handleExtendSessionButtonClick(e) {
    this._pokeServer();
    this.dispatchEvent(
      new CustomEvent("vaadin-idle-notification-extend-session", {
        bubbles: true,
        composed: true,
      })
    );
  }

  /** @private */
  _handleRedirectButtonClick(e) {
    if (this.redirectButtonUrl) {
      this.dispatchEvent(
        new CustomEvent("vaadin-idle-notification-redirect", {
          bubbles: true,
          composed: true,
        })
      );
      this._doRedirect(this.redirectButtonUrl);
    }
  }

  /** @private */
  _resetTimer() {
    clearTimeout(this._timeoutObj);
    this._timeoutObj = setTimeout(
      (e) => this._displayNotification(e),
      (this.maxInactiveInterval - this.secondsBeforeNotification) * 1000
    );
  }

  /** @private */
  _isVaadinRequest(req) {
    const reqUrl = new URL(req.responseURL);
    // ignore heartbeat requests, so timeout value can be larger than heartbeat interval
    return (
      reqUrl.searchParams.has("v-r") &&
      reqUrl.searchParams.get("v-r") !== "heartbeat"
    );
  }

  /** @private */
  _displayNotification(e) {
    // The UI may have one of its components (for example, a Textfield) in focus.
    // Opening the dialog may cause such component to lose focus, hence triggering a
    // change event that send a request to the server (and thereby extending the session).
    // The following flag is used to prevent this sequence from happening.
    this._displayProcessStarted = true;

    this.opened = true;
    let timeleft = this.secondsBeforeNotification - 1;
    this._timeoutObj = setInterval((e) => {
      if (timeleft <= 0) {
        clearInterval(this._timeoutObj);
        this._handleSessionTimeout(e);
      } else {
        this._updateFormattedMessage(this.beforeExpiredMessage, timeleft);
        timeleft -= 1;
      }
    }, 1000);
  }

  /** @private */
  _handleSessionTimeout(e) {
    this.dispatchEvent(
      new CustomEvent("vaadin-idle-notification-timeout", {
        bubbles: true,
        composed: true,
      })
    );

    if (this.redirectAtTimeoutEnabled && this.redirectAtTimeoutUrl) {
      this._doRedirect(this.redirectAtTimeoutUrl);
    } else {
      this._updateFormattedMessage(this.afterExpiredMessage, 0);
      this.extendSessionOnOutsideClick = false;
      this.closeNotificationOnOutsideClick = true;
      this._actionsVisibility = "hidden";
    }
  }

  /** @private */
  _doRedirect(url) {
    window.location.replace(url);
  }

  /** @private */
  _clearTimeoutObject() {
    if (this._timeoutObj) {
      clearInterval(this._timeoutObj);
    }
  }

  /** @private */
  _openedChanged(opened, wasOpened) {
    if (this._dialogElement) {
      this._dialogElement.opened = opened;
    }
    if (opened) {
      this._updateFormattedMessage(
        this.beforeExpiredMessage,
        this.secondsBeforeNotification
      );
      if (!wasOpened)
        this.dispatchEvent(
          new CustomEvent("vaadin-idle-notification-open", {
            bubbles: true,
            composed: true,
          })
        );
    } else {
      this._displayProcessStarted = false;
      if (wasOpened)
        this.dispatchEvent(
          new CustomEvent("vaadin-idle-notification-close", {
            bubbles: true,
            composed: true,
          })
        );
    }
  }

  /** @private */
  _beforeExpiredMessageChanged(newValue, oldValue) {
    this._updateFormattedMessage(newValue, this.secondsBeforeNotification);
  }

  /** @private */
  _updateFormattedMessage(message, secondsToTimeout) {
    this._messageContent = message
      .replace("${SECS_TO_TIMEOUT}", secondsToTimeout)
      .replace("${SECS_MAX_IDLE_TIMEOUT}", this.maxInactiveInterval);
  }

  /** @private */
  _maxInactiveIntervalChanged(newValue, oldValue) {
    if (
      newValue &&
      this.secondsBeforeNotification &&
      newValue > this.secondsBeforeNotification
    ) {
      this.activated = true;
    } else {
      this.activated = false;
    }
  }

  /** @private */
  _secondsBeforeNotificationChanged(newValue, oldValue) {
    if (
      newValue &&
      this.maxInactiveInterval &&
      newValue < this.maxInactiveInterval
    ) {
      this.activated = true;
    } else {
      this.activated = false;
    }
  }

  /** @private */
  _handleOutsideClick(e) {
    if (this.extendSessionOnOutsideClick) {
      this._pokeServer();
    } else if (this.closeNotificationOnOutsideClick) {
      this.opened = false;
    }
  }

  /** @private */
  _pokeServer(e) {
    this.$server.pokeServer().then((result) => {
      if (result === true) {
        console.log(
          "Idle-Notification: Server poked successfully. Session extended."
        );
        this.opened = false;
        this._resetTimer();
      } else {
        console.error("Could not poke the server");
      }
    });
  }

  static get is() {
    return "idle-notification";
  }
  set opened(newValue) {
    const oldValue = this.opened;
    this._opened = newValue;
    if (oldValue !== newValue) {
      this._openedChanged(newValue, oldValue);
      this.requestUpdate(
        "opened",
        oldValue,
        this.constructor.properties.opened
      );
    }
  }
  get opened() {
    return this._opened;
  }

  set closeButtonEnabled(newValue) {
    const oldValue = this.closeButtonEnabled;
    this._closeButtonEnabled = newValue;
    if (oldValue !== newValue) {
      this._closeButtonEnabledChanged(newValue, oldValue);
      this.requestUpdate(
        "closeButtonEnabled",
        oldValue,
        this.constructor.properties.closeButtonEnabled
      );
    }
  }
  get closeButtonEnabled() {
    return this._closeButtonEnabled;
  }

  set extendSessionButtonEnabled(newValue) {
    const oldValue = this.extendSessionButtonEnabled;
    this._extendSessionButtonEnabled = newValue;
    if (oldValue !== newValue) {
      this._extendSessionButtonEnabledChanged(newValue, oldValue);
      this.requestUpdate(
        "extendSessionButtonEnabled",
        oldValue,
        this.constructor.properties.extendSessionButtonEnabled
      );
    }
  }
  get extendSessionButtonEnabled() {
    return this._extendSessionButtonEnabled;
  }

  set redirectButtonEnabled(newValue) {
    const oldValue = this.redirectButtonEnabled;
    this._redirectButtonEnabled = newValue;
    if (oldValue !== newValue) {
      this._redirectButtonEnabledChanged(newValue, oldValue);
      this.requestUpdate(
        "redirectButtonEnabled",
        oldValue,
        this.constructor.properties.redirectButtonEnabled
      );
    }
  }
  get redirectButtonEnabled() {
    return this._redirectButtonEnabled;
  }

  set beforeExpiredMessage(newValue) {
    const oldValue = this.beforeExpiredMessage;
    this._beforeExpiredMessage = newValue;
    if (oldValue !== newValue) {
      this._beforeExpiredMessageChanged(newValue, oldValue);
      this.requestUpdate(
        "beforeExpiredMessage",
        oldValue,
        this.constructor.properties.beforeExpiredMessage
      );
    }
  }
  get beforeExpiredMessage() {
    return this._beforeExpiredMessage;
  }

  set redirectButtonCaption(newValue) {
    const oldValue = this.redirectButtonCaption;
    this._redirectButtonCaption = newValue;
    if (oldValue !== newValue) {
      this._redirectButtonCaptionChanged(newValue, oldValue);
      this.requestUpdate(
        "redirectButtonCaption",
        oldValue,
        this.constructor.properties.redirectButtonCaption
      );
    }
  }
  get redirectButtonCaption() {
    return this._redirectButtonCaption;
  }

  set redirectButtonUrl(newValue) {
    const oldValue = this.redirectButtonUrl;
    this._redirectButtonUrl = newValue;
    if (oldValue !== newValue) {
      this._redirectButtonUrlChanged(newValue, oldValue);
      this.requestUpdate(
        "redirectButtonUrl",
        oldValue,
        this.constructor.properties.redirectButtonUrl
      );
    }
  }
  get redirectButtonUrl() {
    return this._redirectButtonUrl;
  }

  set maxInactiveInterval(newValue) {
    const oldValue = this.maxInactiveInterval;
    this._maxInactiveInterval = newValue;
    if (oldValue !== newValue) {
      this._maxInactiveIntervalChanged(newValue, oldValue);
      this.requestUpdate(
        "maxInactiveInterval",
        oldValue,
        this.constructor.properties.maxInactiveInterval
      );
    }
  }
  get maxInactiveInterval() {
    return this._maxInactiveInterval;
  }

  set secondsBeforeNotification(newValue) {
    const oldValue = this.secondsBeforeNotification;
    this._secondsBeforeNotification = newValue;
    if (oldValue !== newValue) {
      this._secondsBeforeNotificationChanged(newValue, oldValue);
      this.requestUpdate(
        "secondsBeforeNotification",
        oldValue,
        this.constructor.properties.secondsBeforeNotification
      );
    }
  }
  get secondsBeforeNotification() {
    return this._secondsBeforeNotification;
  }

  set activated(newValue) {
    const oldValue = this.activated;
    this._activated = newValue;
    if (oldValue !== newValue) {
      this._activatedChanged(newValue, oldValue);
      this.requestUpdate(
        "activated",
        oldValue,
        this.constructor.properties.activated
      );
    }
  }
  get activated() {
    return this._activated;
  }
  constructor() {
    super();
    this.opened = false;
    this.closeButtonEnabled = false;
    this.extendSessionButtonEnabled = false;
    this.redirectButtonEnabled = false;
    this.beforeExpiredMessage = "";
    this.extendSessionButtonCaption = "Stay logged In";
    this.redirectAtTimeoutEnabled = true;
    this.afterExpiredMessage = "";
    this.activated = false;
    this.extendSessionOnOutsideClick = true;
    this.closeNotificationOnOutsideClick = false;
    this._actionsVisibility = "hidden";
    this._redirectButtonVisibility = "hidden";
    this._extendSessionButtonVisibility = "hidden";
    this._headerVisibility = "hidden";
    this._handleLoadListener = null;
  }
}
customElements.define(IdleNotification.is, IdleNotification);
