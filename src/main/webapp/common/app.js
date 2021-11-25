app = {};

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
