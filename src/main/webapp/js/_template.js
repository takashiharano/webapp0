webapp0.xxx = {};

$onReady = function() {
  util.addEnterKeyHandler(webapp0.xxx.onEnterKey);
};

webapp0.xxx.test = function() {
  var text = $el('#text').value;
  var params = {
    text: text
  };
  app.callServerApi('hello', params, webapp0.xxx.helloCb);
};

webapp0.xxx.helloCb = function(xhr, res) {
  if (res.status == 'OK') {
    $el('#msg').innerHTML = res.body;
  }
};

webapp0.xxx.clear = function() {
  $el('#msg').innerHTML = '';
};

webapp0.xxx.onEnterKey = function() {
};
