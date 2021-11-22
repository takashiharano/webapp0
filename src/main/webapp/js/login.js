webapp0.login = {};

webapp0.login.led = null;

$onReady = function() {
  var opt = {
   speed: 125
  };
  webapp0.login.led = new util.Led('#led', opt);

  util.addEnterKeyHandler(webapp0.login.onEnterKey);

  $el('#id').focus();
};

webapp0.login.login = function() {
  var id = $el('#id').value;
  var pw = util.encodeBSB64($el('#pw').value, BSB64N);
  var params = {
    id: id,
    pw: pw
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
    $el('#message').textseq('Welcome!', textseqOpt);
  } else {
    webapp0.login.led.on('#f88');
    textseqOpt = {
      cursor: 2,
      oncomplete: webapp0.login.onLoginErr
    };
    $el('#message').textseq('Failed to Login', textseqOpt);
  }
};

webapp0.login.onLoginOk = function() {
  setTimeout(webapp0.login._onLoginOk, 1500);
};
webapp0.login._onLoginOk = function() {
  location.href = 'main';
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
