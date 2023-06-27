app.screen2 = {};

app.screen2.timerId = 0;
app.screen2.led1 = null;

$onReady = function() {
  app.screen2.led1 = new util.Led('#led1');
};

app.screen2.startTask = function() {
  var n = $el('#param-n').value;
  var params = {
    n: n
  };
  app.callServerApi('StartAsyncTask', params, app.screen2.startAsyncTaskCb);
};

app.screen2.startAsyncTaskCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.screen2.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var s = 'taskId = ' + taskId;
  app.screen2.showInfo(s);

  $el('#task-id').value = taskId;

  app.screen2.startWatchStatus();
};

app.screen2.startWatchStatus = function() {
  app.screen2.led1.on();
  app.screen2.watchStatus();
};

app.screen2.stopWatchStatus = function() {
  if (app.screen2.timerId > 0) {
    clearTimeout(app.screen2.timerId);
    app.screen2.timerId = 0;
  }
  app.screen2.led1.off();
};

app.screen2.watchStatus = function() {
  app.screen2.getTaskStatus(app.screen2.watchStatusPostProc);
};

app.screen2.watchStatusPostProc = function(isDone) {
  if (isDone) {
    app.screen2.stopWatchStatus();
  } else {
    app.screen2.timerId = setTimeout(app.screen2.watchStatus, 1000);
  }
};

//-----------------------------------------------------------------------------
app.screen2.getTaskStatus = function(postProc) {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  var req = app.callServerApi('GetAsyncTaskInfo', params, app.screen2.getTaskStatusCb);
  req.postProc = postProc;

  app.screen2.showInfo('getTaskStatus');
};
app.screen2.getTaskStatusCb = function(xhr, res, req) {
  if (res.status != 'OK') {
    app.screen2.showInfo('ERROR: ' + res.status);
    if (req.postProc) {
      req.postProc(true);
    }
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var isDone = data.isDone;
  var info = data.info;

  var s = taskId + ': isDone=' + isDone + ' : ' + info;
  app.screen2.showInfo(s);

  if (req.postProc) {
    req.postProc(isDone);
  }
};

//-----------------------------------------------------------------------------
app.screen2.getTaskResult = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('GetAsyncTaskResult', params, app.screen2.getTaskResultCb);

  app.screen2.showInfo('getTaskResult');
};
app.screen2.getTaskResultCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.screen2.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var result = data.result;

  var s = taskId + ': ' + result;
  app.screen2.showInfo(s);
};

//-----------------------------------------------------------------------------
app.screen2.cancelTask = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('CancelAsyncTask', params, app.screen2.cancelTaskCb);

  app.screen2.showInfo('CancelAsyncTask');
};
app.screen2.cancelTaskCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.screen2.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var canceled = data.canceled;
  var s = taskId + ': canceled=' + canceled;
  app.screen2.showInfo(s);
};

//-----------------------------------------------------------------------------
app.screen2.showInfo = function(s) {
  $el('#info').innerHTML = s;
};
