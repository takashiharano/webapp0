webapp0.screen2 = {};

webapp0.screen2.timerId = 0;
webapp0.screen2.led1 = null;

$onReady = function() {
  webapp0.screen2.led1 = new util.Led('#led1');
};

webapp0.screen2.startTask = function() {
  var n = $el('#param-n').value;
  var params = {
    n: n
  };
  app.callServerApi('StartAsyncTask', params, webapp0.screen2.startAsyncTaskCb);
};

webapp0.screen2.startAsyncTaskCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.screen2.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var s = 'taskId = ' + taskId;
  webapp0.screen2.showInfo(s);

  $el('#task-id').value = taskId;

  webapp0.screen2.startWatchStatus();
};

webapp0.screen2.startWatchStatus = function() {
  webapp0.screen2.led1.on();
  webapp0.screen2.watchStatus();
};

webapp0.screen2.stopWatchStatus = function() {
  if (webapp0.screen2.timerId > 0) {
    clearTimeout(webapp0.screen2.timerId);
    webapp0.screen2.timerId = 0;
  }
  webapp0.screen2.led1.off();
};

webapp0.screen2.watchStatus = function() {
  webapp0.screen2.getTaskStatus(webapp0.screen2.watchStatusPostProc);
};

webapp0.screen2.watchStatusPostProc = function(isDone) {
  if (isDone) {
    webapp0.screen2.stopWatchStatus();
  } else {
    webapp0.screen2.timerId = setTimeout(webapp0.screen2.watchStatus, 1000);
  }
};

//-----------------------------------------------------------------------------
webapp0.screen2.getTaskStatus = function(postProc) {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  var req = app.callServerApi('GetAsyncTaskInfo', params, webapp0.screen2.getTaskStatusCb);
  req.postProc = postProc;

  webapp0.screen2.showInfo('getTaskStatus');
};
webapp0.screen2.getTaskStatusCb = function(xhr, res, req) {
  if (res.status != 'OK') {
    webapp0.screen2.showInfo('ERROR: ' + res.status);
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
  webapp0.screen2.showInfo(s);

  if (req.postProc) {
    req.postProc(isDone);
  }
};

//-----------------------------------------------------------------------------
webapp0.screen2.getTaskResult = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('GetAsyncTaskResult', params, webapp0.screen2.getTaskResultCb);

  webapp0.screen2.showInfo('getTaskResult');
};
webapp0.screen2.getTaskResultCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.screen2.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var result = data.result;

  var s = taskId + ': ' + result;
  webapp0.screen2.showInfo(s);
};

//-----------------------------------------------------------------------------
webapp0.screen2.cancelTask = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('CancelAsyncTask', params, webapp0.screen2.cancelTaskCb);

  webapp0.screen2.showInfo('CancelAsyncTask');
};
webapp0.screen2.cancelTaskCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.screen2.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var canceled = data.canceled;
  var s = taskId + ': canceled=' + canceled;
  webapp0.screen2.showInfo(s);
};

//-----------------------------------------------------------------------------
webapp0.screen2.showInfo = function(s) {
  $el('#info').innerHTML = s;
};
