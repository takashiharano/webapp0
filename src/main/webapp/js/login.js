/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
webapp0.login = {};

webapp0.login.MESSAGES = {
  login_ok: 'Welcome!',
  login_ng: 'Failed. Please try again.',
};

webapp0.login.led = null;

$onReady = function() {
  app.setMessages(webapp0.login.MESSAGES);

  var opt = {
   speed: 125
  };
  webapp0.login.led = new util.Led('#led', opt);

  util.addEnterKeyHandler(webapp0.login.onEnterKey);

  $el('#id').focus();
};

webapp0.login.login = function() {
  var username = $el('#id').value;
  var pw = $el('#pw').value;
  var salt = username;
  var pwHash = webapp0.common.getHash('SHA-256', pw, salt);
  pwHash = util.encodeBSB64(pwHash, BSB64N);

  var params = {
    id: username,
    pw: pwHash
  };
  app.callServerApi('login', params, webapp0.login.loginCb);
};

webapp0.login.loginCb = function(xhr, res) {
  if (res.status == 'OK') {
    webapp0.login.led.on('#0f0');
    var textseqOpt = {
      cursor: 2,
      oncomplete: webapp0.login.onLoginOk
    };
    var m = app.getMessage('login_ok');
    $el('#message').textseq(m, textseqOpt);
  } else {
    m = app.getMessage('login_ng');
    if (res.status == 'ERROR') {
      m = 'Server Error';
      log.e(m + ' ' + res.body);
    }
    webapp0.login.led.on('#f88');
    textseqOpt = {
      cursor: 2,
      oncomplete: webapp0.login.onLoginErr
    };
    $el('#message').textseq(m, textseqOpt);
  }
};

webapp0.login.onLoginOk = function() {
  setTimeout(webapp0.login.forwardScreen, 1000);
};
webapp0.login.forwardScreen = function() {
  location.href = '/' + REQUESTED_URL;
};

webapp0.login.onLoginErr = function() {
  setTimeout(webapp0.login._onLoginErr, 1500);
};
webapp0.login._onLoginErr = function() {
  webapp0.login.led.off();
  $el('#message').html('', 250);
};

webapp0.login.onEnterKey = function() {
  if ($el('#id').hasFocus() || $el('#pw').hasFocus()) {
    webapp0.login.login();
  }
};
