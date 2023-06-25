webapp0.screen1 = {};

$onReady = function() {
  util.addEnterKeyHandler(webapp0.screen1.onEnterKey);
};

webapp0.screen1.test = function() {
  var text = $el('#text').value;
  var params = {
    text: text
  };
  app.callServerApi('hello', params, webapp0.screen1.helloCb);
};

webapp0.screen1.helloCb = function(xhr, res) {
  if (res.status == 'OK') {
    $el('#msg').innerHTML = res.body;
  }
};

webapp0.screen1.clear = function() {
  $el('#msg').innerHTML = '';
};

webapp0.screen1.onEnterKey = function() {
};
