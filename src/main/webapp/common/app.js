/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app = {};
app.status = 0;
app.language = 'en';
app.messages = {};
app.initFns = [];

/**
 * App initialize function.
 *
 * It will be called when the HTML document has been completely parsed, 
 * and all deferred scripts (<script defer src="…"> and 
 * <script type="module">) have downloaded and executed.
 * It doesn't wait for other things like images, subframes, 
 * and async scripts to finish loading.
 */
app.onReady = function() {
  util.clock('#clock');
  $el('.screen-button').removeClass('screen-button-active');
  $el('#screen-button-' + app.screenId).addClass('screen-button-active');
  app.callInitFunctions();
  app.onScreenReady();
};

/**
 * Call the init functions for each script files.
 */
app.callInitFunctions = function() {
  for (var i = 0; i < app.initFns.length; i++) {
    var f = app.initFns[i];
    f();
  }
};

/**
 * Registers init functions.
 * The given functions will be called from app.onReady().
 */
app.registerInitFunction = function(f) {
  app.initFns.push(f);
};

/**
 * Ajax communication.
 * Invoke an Action class on the server side.
 *
 * @param actionName
 *          Action class name
 * @param params
 *          parameters in object format (JSON)
 * @param onsuccess
 *          callback function on success
 * @param onerror
 *          callback function on error
 */
app.callServerApi = function(actionName, params, onsuccess, onerror) {
  if (!params) params = {};
  params.action = actionName;
  var req = {
    url: 'main',
    method: 'POST',
    data: params,
    onsuccess: app.onHttpReceive,
    onerror: onerror,
    orgOnsuccess: onsuccess
  };
  util.http(req);
  return req;
};

/**
 * The system callback for HTTP (Ajax) communication.
 * This is called when the status code is 2xx or 304 (Not Modified).
 *
 * @param xhr
 *          XMLHttpRequest object
 * @param res
 *          The response object. Generally stores a JSON object consisting of status and body.
 * @param req
 *          The object used for the request.
 */
app.onHttpReceive = function(xhr, res, req) {
  if ((res instanceof Object) && (res.status == 'FORBIDDEN')) {
    location.href = 'main';
    return;
  }
  req.orgOnsuccess(xhr, res, req);
};

/**
 * Moves to the specified screen.
 */
app.screen = function(screenId) {
  var url = 'main';
  var param = {
    screen: screenId
  };
  util.submit(url, 'GET', param);
};

/**
 * Shows an infotip.
 */
app.showInfotip = function(s, d) {
  if (!d) d = 1500;
  var opt = {
    style: {
      'font-size': '16px'
    }
  };
  util.infotip.show(s, d, opt);
};

/**
 * Returns the current username;
 */
app.getUsername = function() {
  return app.username;
};

/**
 * Sets the language in two-letter code specified in ISO 639-1.
 */
app.setLanguage = function(lang) {
  app.language = lang;
};

/**
 * Sets messages for the screen.
 * It must be called first when the screen is initialized.
 *
 * msgs = {
 *  msg1: 'aaa', // always return 'aaa'
 *  msg2: {en: 'Hello', ja: 'こんにちは'} // returns corresponding to app.language
 * }
 */
app.setMessages = function(msgs) {
  app.messages = msgs;
};

/**
 * Adds messages to an existing messages.
 */
app.addMessages = function(msgs) {
  for (var k in msgs) {
    app.messages[k] = msgs[k];
  }
};

/**
 * Returns a message corresponding to the id.
 * msg: 'Hello {0}!', a0='John'
 * -> 'Hello John!'
 */
app.getMessage = function(id, a0, a1, a2, a3) {
  var m = app._getMessage(id);
  if (!m) return m;
  m = m.replace(/\{0\}/g, a0);
  m = m.replace(/\{1\}/g, a1);
  m = m.replace(/\{2\}/g, a2);
  m = m.replace(/\{3\}/g, a3);
  m = m.replace(/\\\{/g, '{');
  m = m.replace(/\\\}/g, '}');
  return m;
};

/**
 * Returns a message corresponding to the id and the language.
 * If the message for the language is not defined, it will return the message for 'en'.
 *
 * See the description of app.setMessages() for the list format.
 */
app._getMessage = function(id) {
  var o = app.messages[id];
  if (!o) return null;
  if (typeof o == 'string') return o;
  var m = o[app.language];
  if (!m) m = o['en'];
  if (!m) m = null;
  return m;
};

/**
 * Returns app status.
 */
app.getStatus = function() {
  return app.status;
};

/**
 * Sets app status.
 */
app.setStatus = function(st) {
  app.status = st;
};

/**
 * Sets the state to app status.
 */
app.setState = function(st) {
  app.status |= st;
};

/**
 * Unsets the state from app status.
 */
app.unsetState = function(st) {
  app.status &= ~st;
};

/**
 * Show loading screen
 */
app.loadingScreen = {};
app.loadingScreen.show = function(m, html) {
  util.loadingScreen.show(m, html);
};

/**
 * Hide loading screen
 */
app.loadingScreen.hide = function() {
  util.loadingScreen.hide();
};

window.addEventListener('DOMContentLoaded', app.onReady, true);
