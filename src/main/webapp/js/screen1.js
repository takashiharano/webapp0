app.screen1 = {};

$onReady = function() {
  util.addEnterKeyHandler(app.screen1.onEnterKey);
};

app.screen1.test = function() {
  var text = $el('#text').value;
  var params = {
    text: text
  };
  app.callServerApi('hello', params, app.screen1.helloCb);
};

app.screen1.helloCb = function(xhr, res) {
  if (res.status == 'OK') {
    $el('#msg').innerHTML = res.body;
  }
};

app.screen1.clear = function() {
  $el('#msg').innerHTML = '';
};

app.screen1.onEnterKey = function() {
};
