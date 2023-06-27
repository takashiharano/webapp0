app.system = {};

app.system.resetApp = function() {
  util.confirm('Reset WebApp?', app.system.reset);
};

app.system.reset = function() {
  app.callServerApi('reset', null, app.system.resetCb);
};

app.system.resetCb = function(xhr, res) {
  var msg;
  if (res.status == 'OK') {
    msg = 'OK: The system has been restarted successfully.';
  } else {
    msg = 'ERROR: ' + res.body;
  }
  $el('#message').textseq(msg, {cursor: 3});
};
