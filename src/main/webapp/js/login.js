/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.login = {};

app.login.MESSAGES = {
  login_ok: 'Welcome!',
  login_ng: 'Failed. Please try again.',
};

app.login.led = null;

$onReady = function() {
  app.setMessages(app.login.MESSAGES);

  var opt = {
   speed: 125
  };
  app.login.led = new util.Led('#led', opt);

  util.addEnterKeyHandler(app.login.onEnterKey);

  $el('#id').focus();
};

app.login.login = function() {
  var username = $el('#id').value;
  var pw = $el('#pw').value;
  var salt = username;
  var pwHash = app.common.getHash('SHA-256', pw, salt);
  pwHash = util.encodeBSB64(pwHash, BSB64N);

  var params = {
    id: username,
    pw: pwHash
  };
  app.callServerApi('login', params, app.login.loginCb);
};

app.login.loginCb = function(xhr, res) {
  if (res.status == 'OK') {
    app.login.led.on('#0f0');
    var textseqOpt = {
      cursor: 2,
      oncomplete: app.login.onLoginOk
    };
    var m = app.getMessage('login_ok');
    $el('#message').textseq(m, textseqOpt);
  } else {
    m = app.getMessage('login_ng');
    if (res.status == 'ERROR') {
      m = 'Server Error';
      log.e(m + ' ' + res.body);
    }
    app.login.led.on('#f88');
    textseqOpt = {
      cursor: 2,
      oncomplete: app.login.onLoginErr
    };
    $el('#message').textseq(m, textseqOpt);
  }
};

app.login.onLoginOk = function() {
  setTimeout(app.login.forwardScreen, 1000);
};
app.login.forwardScreen = function() {
  location.href = '/' + REQUESTED_URL;
};

app.login.onLoginErr = function() {
  setTimeout(app.login._onLoginErr, 1500);
};
app.login._onLoginErr = function() {
  app.login.led.off();
  $el('#message').html('', 250);
};

app.login.onEnterKey = function() {
  if ($el('#id').hasFocus() || $el('#pw').hasFocus()) {
    app.login.login();
  }
};
