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


//app.processingDots.show('Loading...');
//app.processingDots.hide();

//-----------------------------------------------------------------------------
// 処理中表示
//-----------------------------------------------------------------------------
/**
 * 処理中画像表示を開始します。
 */
app.processingDots = {};
app.processingDots.modal = null;
app.processingDots.baseEl = null;
app.processingDots.msg = null;
app.processingDots.show = function(message) {
  if (app.processingDots.modal) {
    // 既に表示中
    var msg = app.processingDots.msg;
    if (message) {
      if (!msg) {
        msg = app.processingDots.appendMessageArea(app.processingDots.baseEl);
        app.processingDots.msg = msg;
      }
      msg.innerHTML = message;
      util.setStyle(msg, 'opacity', '1');
    } else {
      if (msg) {
        msg.innerText = '';
        util.setStyle(msg, 'opacity', '0');
      }
    }
    return false;
  }

  var outerWrapper = document.createElement('div');
  var styles = {
    'display': 'table',
    'position': 'absolute',
    'width': '100%',
    'height': '100%',
    'top': '0',
    'right': '0',
    'bottom': '0',
    'left': '0',
    'margin': 'auto'
  };
  util.setStyle(outerWrapper, styles);

  var wrapper = document.createElement('div');
  styles = {
    'display': 'table-cell',
    'position': 'relative',
    'width': '100%',
    'height': '100%',
    'text-align': 'center',
    'vertical-align': 'middle'
  };
  util.setStyle(wrapper, styles);
  app.processingDots.baseEl = wrapper;
  outerWrapper.appendChild(wrapper);

  if (message && !app.processingDots.msg) {
    msg = app.processingDots.appendMessageArea(wrapper);
    msg.innerHTML = message;
    app.processingDots.msg = msg;
  }

  styles = {
    background: 'rgba(0,0,0,0.3)'
  };
  var closeAnywhere = false;
  var modal = util.modal.show(outerWrapper, closeAnywhere, styles);
  app.processingDots.modal = modal;
  util.setStyle(document.body, 'cursor', 'progress');

  return true;
};

/**
 * 処理中画像表示を終了します。
 */
app.processingDots.hide = function() {
  if (app.processingDots.modal) {
    app.processingDots.modal.hide();
    app.processingDots.msg = null;
    app.processingDots.baseEl = null;
    app.processingDots.modal = null;
    util.setStyle(document.body, 'cursor', '');
    return true;
  } else {
    // 表示されていない状態で呼ばれた
    return false;
  }
};

app.processingDots.appendMessageArea = function(baseEl) {
  var msg = app.processingDots.createMessageArea();
  var br = document.createElement('br');
  baseEl.appendChild(br);
  baseEl.appendChild(msg);
  return msg;
};

app.processingDots.createMessageArea= function() {
  var el = document.createElement('pre');
  var styles = {
    'display': 'inline-block',
    'position': 'relative',
    'width': 'auto',
    'height': 'auto',
    'top': '0',
    'border-radius': '4px',
    'background': 'rgba(0,0,0,0.5)',
    'padding': '0.5em 1em',
    'color': '#fff',
    'font-size': '14px'
  };
  util.setStyle(el, styles);
  return el;
};



window.addEventListener('DOMContentLoaded', app.onReady, true);
