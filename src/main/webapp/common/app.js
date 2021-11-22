app = {};

app.callServerApi = function(actionName, params, cb, errCb) {
  if (!params) params = {};
  params.action = actionName;
  var req = {
    url: 'main',
    method: 'POST',
    data: params,
    cb: cb
  };
  util.http(req);
};
