webapp0._template = {};

$onReady = function() {
  util.addEnterKeyHandler(webapp0._template.onEnterKey);
};

webapp0._template.test = function() {
  var text = $el('#text').value;
  var params = {
    text: text
  };
  app.callServerApi('hello', params, webapp0._template.helloCb);
};

webapp0._template.helloCb = function(xhr, res) {
  if (res.status == 'OK') {
    $el('#msg').innerHTML = res.body;
  }
};

webapp0._template.clear = function() {
  $el('#msg').innerHTML = '';
};

webapp0._template.onEnterKey = function() {
};
