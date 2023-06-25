webapp0.system = {};

webapp0.system.resetApp = function() {
  util.confirm('Reset WebApp?', webapp0.system.reset);
};

webapp0.system.reset = function() {
  app.callServerApi('reset', null, webapp0.system.resetCb);
};

webapp0.system.resetCb = function(xhr, res) {
  var msg;
  if (res.status == 'OK') {
    msg = 'OK: The system has been restarted successfully.';
  } else {
    msg = 'ERROR: ' + res.body;
  }
  $el('#message').textseq(msg, {cursor: 3});
};
