app = {};

app.onReady = function() {
  util.clock('#clock');
  app.onScreenShow();
};

app.onScreenShow = function() {
  $el('.screen-button').removeClass('screen-button-active');
  $el('#screen-button-' + app.screenId).addClass('screen-button-active');
};

app.callServerApi = function(actionName, params, onsuccess, onerror) {
  if (!params) params = {};
  params.action = actionName;
  var req = {
    url: 'main',
    method: 'POST',
    data: params,
    onsuccess: onsuccess,
    onerror: onerror,
    orgOnsuccess: onsuccess
  };
  util.http(req);
  return req;
};

app.onHttpReceive = function(xhr, res, req) {
  if ((res instanceof Object) && (res.status == 'FORBIDDEN')) {
    location.href = 'main';
    return;
  }
  req.orgOnsuccess(xhr, res, req);
};

app.screen = function(screenId) {
  var url = 'main';
  var param = {
    screen: screenId
  };
  util.submit(url, 'GET', param);
};

window.addEventListener('DOMContentLoaded', app.onReady, true);
