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

import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import '@vaadin/vaadin-dialog/src/vaadin-dialog.js';
import '@vaadin/vaadin-button/src/vaadin-button.js';

class IdleNotification extends ThemableMixin(PolymerElement) {
  static get properties() {
    return {
      /**
       * Set to true when the notification is open
       * @type {boolean}
       */
      opened: {
        type: Boolean,
        value: false,
        notify: true,
        reflectToAttribute: true,
        observer: '_openedChanged',
      },

      /**
       * Determines whether a close button is displayed to the user
       * @type {boolean}
       */
      closeButtonEnabled: {
        type: Boolean,
        value: false,
        notify: true,
        observer: '_closeButtonEnabledChanged',
      },

      /**
       * Determines whether an extend-session button is displayed to the user
       * @type {boolean}
       */
      extendSessionButtonEnabled: {
        type: Boolean,
        value: false,
        notify: true,
        observer: '_extendSessionButtonEnabledChanged',
      },

      /**
       * Determines whether a re-direct button is displayed to the user
       * @type {boolean}
       */
      redirectButtonEnabled: {
        type: Boolean,
        value: false,
        notify: true,
        observer: '_redirectButtonEnabledChanged',
      },

      /**
       * The message displayed to the user before the session expires
       * @type {string}
       */
      beforeExpiredMessage: {
        type: String,
        value: '',
        notify: true,
        observer: '_beforeExpiredMessageChanged',
      },

      /**
       * The caption of the extend-session button
       * @type {string}
       */
      extendSessionButtonCaption: {
        type: String,
        value: 'Stay logged In',
      },

      /**
       * The caption of the re-direct button
       * @type {string}
       */
      redirectButtonCaption: {
        type: String,
        observer: '_redirectButtonCaptionChanged',
      },

      /**
       * The url to redirect the page to upon re-direct-button click
       * @type {string}
       */
      redirectButtonUrl: {
        type: String,
        observer: '_redirectButtonUrlChanged',
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
        value: true,
      },

      /**
       * The message displayed to the user after the session expires
       * @type {string}
       */
      afterExpiredMessage: {
        type: String,
        value: '',
      },

      /**
       * The number of seconds until the session timesout
       * @type {number}
       */
      maxInactiveInterval: {
        type: Number,
        observer: '_maxInactiveIntervalChanged',
      },

      /**
       * The number of seconds before session timesout at which point
       * the notification will be displayed to the user
       * @type {number}
       */
      secondsBeforeNotification: {
        type: Number,
        observer: '_secondsBeforeNotificationChanged',
      },

      /**
       * Set to true if the time-settings are valid
       * @type {boolean}
       */
      activated: {
        type: Boolean,
        value: false,
        notify: true,
        reflectToAttribute: true,
        observer: '_activatedChanged',
      },

      /**
       * Determines whether an outside click would poke the server (thereby extending the session)
       * @type {boolean}
       */
      extendSessionOnOutsideClick: {
        type: Boolean,
        value: true,
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
        value: false,
      },

      /** @private */
      _timeoutObj: Object,

      /** @private */
      _messageContent: String,

      /** @private */
      _actionsVisibility: {
        type: String,
        value: 'hidden',
      },

      /** @private */
      _redirectButtonVisibility: {
        type: String,
        value: 'hidden',
      },

      /** @private */
      _extendSessionButtonVisibility: {
        type: String,
        value: 'hidden',
      },

      /** @private */
      _headerVisibility: {
        type: String,
        value: 'hidden',
      },

      /** @private */
      _displayProcessStarted: Boolean,

      /** @private */
      _dialogElement: Object,

      /** @private */
      _handleLoadListener: {
         type: Object,
         value: null,
      }
    };
  }

  static get template() {
    return html`
      <style>
        [part='container'] {
          --notification-background-color: var(--idle-notification-background-color, #ffc13f);
          --notification-color: var(--idle-notification-color, var(--lumo-contrast));
          color: var(--notification-color);
          display: flex;
          flex-direction: column;
          background-color: var(--notification-background-color);
          padding: 1em;
          border-radius: var(--lumo-border-radius-m);
        }

        [part='message-container'] {
          display: flex;
        }

        #warning-icon {
          display: flex;
          flex-shrink: 0;
          align-self: center;
          margin-right: 1em;
        }

        [part='header'].hidden {
          display: none;
        }

        [part='header'].visible {
          display: flex;
          justify-content: flex-end;
          margin-bottom: 1em;
        }

        #close-icon {
          display: flex;
          font-size: var(--lumo-font-size-xs);
          height: 1em;
          width: 1em;
        }

        [part='message'] {
          display: flex;
        }

        [part='actions'].visible {
          display: flex;
          justify-content: flex-end;
          margin-top: 1em;
        }

        [part='actions'].hidden {
          display: none;
        }

        vaadin-button {
          --fallback-border-radius: calc(var(--lumo-size-m) / 2);
          color: var(--idle-notification-button-color, --notification-color);
          background-color: var(--idle-notification-button-background-color, white);
          border-radius: var(--lumo-border-radius-m, var(--fallback-border-radius));
        }

        #extend-session.visible {
          margin-left: 1em;
        }

        #extend-session.hidden {
          display: none;
        }

        #redirect.hidden {
          display: none;
        }
      </style>

      <vaadin-dialog id="notification-dialog" part="notification-dialog" theme="notification-dialog-theme">
        <template>
          <div part="container" class="idle-notification-container">
            <div part="header" class$="[[_headerVisibility]]">
              <iron-icon icon="vaadin:close-small" id="close-icon" on-click="_handleCloseIconClick"></iron-icon>
            </div>
            <div part="message-container">
              <iron-icon icon="vaadin:exclamation-circle-o" id="warning-icon"></iron-icon>
              <div part="message">[[_messageContent]]</div>
            </div>
            <div part="actions" class$="[[_actionsVisibility]]">
              <vaadin-button id="redirect" on-click="_handleRedirectButtonClick" class$="[[_redirectButtonVisibility]]">
                [[redirectButtonCaption]]
              </vaadin-button>
              <vaadin-button
                id="extend-session"
                on-click="_handleExtendSessionButtonClick"
                class$="[[_extendSessionButtonVisibility]]"
              >
                [[extendSessionButtonCaption]]
              </vaadin-button>
            </div>
          </div>
        </template>
      </vaadin-dialog>
    `;
  }

  /** @protected */
  ready() {
    super.ready();

    this._dialogElement = this.shadowRoot.querySelector('vaadin-dialog');
    this._dialogElement.$.overlay.addEventListener('vaadin-overlay-outside-click', this._handleOutsideClick.bind(this));
    this._dialogElement.noCloseOnOutsideClick = !this.closeNotificationOnOutsideClick;
    this._dialogElement.noCloseOnEsc = true;
    this._dialogElement.modeless = false;

    this._registerXhrListener();
  }

  /** @protected */
  disconnectedCallback() {
    super.disconnectedCallback();
    this.removeEventListener('load', this._handleLoadListener);
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
      thisComponent._handleLoadListener = (e) => thisComponent._handleLoad(currRequest);
      this.addEventListener('load', thisComponent._handleLoadListener);
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
  };

  /** @private */
  _closeButtonEnabledChanged(isCloseButtonEnabled, wasCloseButtonEnabled) {
    if (isCloseButtonEnabled) {
      this._headerVisibility = 'visible';
    } else {
      this._headerVisibility = 'hidden';
    }
  }

  /** @private */
  _extendSessionButtonEnabledChanged(isExtendSessionButtonEnabled, wasExtendSessionButtonEnabled) {
    if (isExtendSessionButtonEnabled) {
      this._extendSessionButtonVisibility = 'visible';
      this._actionsVisibility = 'visible';
    } else if (!(this.redirectButtonEnabled && this.redirectButtonCaption && this.redirectUrl)) {
      this._actionsVisibility = 'hidden';
    }
  }

  /** @private */
  _redirectButtonEnabledChanged(isRedirectButtonEnabled, wasRedirectButtonEnabled) {
    this._validateRedirectButton(isRedirectButtonEnabled, this.redirectButtonCaption, this.redirectButtonUrl);
  }

  /** @private */
  _redirectButtonCaptionChanged(newRedirectButtonCaption, oldRedirectButtonCaption) {
    this._validateRedirectButton(this.redirectButtonEnabled, newRedirectButtonCaption, this.redirectButtonUrl);
  }

  /** @private */
  _redirectButtonUrlChanged(newRedirectButtonUrl, oldRedirectButtonUrl) {
    this._validateRedirectButton(this.redirectButtonEnabled, this.redirectButtonCaption, newRedirectButtonUrl);
  }

  /** @private */
  _validateRedirectButton(isRedirectButtonEnabled, redirectButtonCaption, redirectButtonUrl) {
    if (isRedirectButtonEnabled && redirectButtonCaption && redirectButtonUrl) {
      this._redirectButtonVisibility = 'visible';
      this._actionsVisibility = 'visible';
    } else if (!this.extendSessionButtonEnabled) {
      this._actionsVisibility = 'hidden';
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
    this.dispatchEvent(new CustomEvent('vaadin-idle-notification-extend-session', { bubbles: true, composed: true }));
  }

  /** @private */
  _handleRedirectButtonClick(e) {
    if (this.redirectButtonUrl) {
      this.dispatchEvent(new CustomEvent('vaadin-idle-notification-redirect', { bubbles: true, composed: true }));
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
    return reqUrl.searchParams.has('v-r') && reqUrl.searchParams.get('v-r') !== 'heartbeat';
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
    this.dispatchEvent(new CustomEvent('vaadin-idle-notification-timeout', { bubbles: true, composed: true }));

    if (this.redirectAtTimeoutEnabled && this.redirectAtTimeoutUrl) {
      this._doRedirect(this.redirectAtTimeoutUrl);
    } else {
      this._updateFormattedMessage(this.afterExpiredMessage, 0);
      this.extendSessionOnOutsideClick = false;
      this.closeNotificationOnOutsideClick = true;
      this._actionsVisibility = 'hidden';
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
      this._updateFormattedMessage(this.beforeExpiredMessage, this.secondsBeforeNotification);
      if(!wasOpened) this.dispatchEvent(new CustomEvent('vaadin-idle-notification-open', { bubbles: true, composed: true }));
    } else {
      this._displayProcessStarted = false;
      if(wasOpened) this.dispatchEvent(new CustomEvent('vaadin-idle-notification-close', { bubbles: true, composed: true }));
    }
  }

  /** @private */
  _beforeExpiredMessageChanged(newValue, oldValue) {
    this._updateFormattedMessage(newValue, this.secondsBeforeNotification);
  }

  /** @private */
  _updateFormattedMessage(message, secondsToTimeout) {
    let SECS_TO_TIMEOUT = secondsToTimeout;
    let SECS_MAX_IDLE_TIMEOUT = this.maxInactiveInterval;
    this._messageContent = eval('`' + message + '`');
  }

  /** @private */
  _maxInactiveIntervalChanged(newValue, oldValue) {
    if (newValue && this.secondsBeforeNotification && newValue > this.secondsBeforeNotification) {
      this.activated = true;
    } else {
      this.activated = false;
    }
  }

  /** @private */
  _secondsBeforeNotificationChanged(newValue, oldValue) {
    if (newValue && this.maxInactiveInterval && newValue < this.maxInactiveInterval) {
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
        console.log('Idle-Notification: Server poked successfully. Session extended.');
        this.opened = false;
        this._resetTimer();
      } else {
        console.error('Could not poke the server');
      }
    });
  }

  static get is() {
    return 'idle-notification';
  }
}
customElements.define(IdleNotification.is, IdleNotification);