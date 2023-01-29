webapp0.asyncsample = {};

webapp0.asyncsample.startTask = function() {
  var n = $el('#param-n').value;
  var params = {
    n: n
  };
  app.callServerApi('StartAsyncTask', params, webapp0.asyncsample.startAsyncTaskCb);
};

webapp0.asyncsample.startAsyncTaskCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.asyncsample.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var s = 'taskId = ' + taskId;
  webapp0.asyncsample.showInfo(s);

  $el('#task-id').value = taskId;
};

//-----------------------------------------------------------------------------
webapp0.asyncsample.getTaskStatus = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('GetAsyncTaskInfo', params, webapp0.asyncsample.getTaskStatusCb);

  webapp0.asyncsample.showInfo('getTaskStatus');
};
webapp0.asyncsample.getTaskStatusCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.asyncsample.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var isDone = data.isDone;
  var info = data.info;

  var s = taskId + ': isDone=' + isDone + ' : ' + info;
  webapp0.asyncsample.showInfo(s);
};

//-----------------------------------------------------------------------------
webapp0.asyncsample.getTaskResult = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('GetAsyncTaskResult', params, webapp0.asyncsample.getTaskResultCb);

  webapp0.asyncsample.showInfo('getTaskResult');
};
webapp0.asyncsample.getTaskResultCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.asyncsample.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var result = data.result;

  var s = taskId + ': ' + result;
  webapp0.asyncsample.showInfo(s);
};

//-----------------------------------------------------------------------------
webapp0.asyncsample.cancelTask = function() {
  var taskId = $el('#task-id').value;
  var params = {
    taskId: taskId
  };
  app.callServerApi('CancelAsyncTask', params, webapp0.asyncsample.cancelTaskCb);

  webapp0.asyncsample.showInfo('CancelAsyncTask');
};
webapp0.asyncsample.cancelTaskCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.asyncsample.showInfo('ERROR: ' + res.status);
    return;
  }

  var data = res.body;
  var taskId = data.taskId;
  var canceled = data.canceled;
  var s = taskId + ': canceled=' + canceled;
  webapp0.asyncsample.showInfo(s);
};

//-----------------------------------------------------------------------------
webapp0.asyncsample.showInfo = function(s) {
  $el('#info').innerHTML = s;
};
