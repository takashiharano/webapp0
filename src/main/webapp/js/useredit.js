webapp0.useredit = {};
webapp0.useredit.mode = 'update';


$onReady = function() {
  util.addEnterKeyHandler(webapp0.useredit.onEnterKey);

  if (webapp0.useredit.mode != 'add') {
    $el('#username').disabled = true;
  }
};

webapp0.useredit.onOkClick = function() {
  var mode = webapp0.useredit.mode;
  if (mode == 'add') {
    webapp0.useredit.addUser();
  } else if (mode == 'update') {
    webapp0.useredit.updateUser();
  }
};

webapp0.useredit.clearUserInfo = function() {
  $el('#username').value = '';
  $el('#fullname').value = '';
  $el('#isadmin').checked = false;
  $el('#privileges').value = '';
  $el('#status').value = '0';
  $el('#pw1').value = '';
  $el('#pw2').value = '';
};

webapp0.useredit.newUser = function() {
  webapp0.useredit.mode = 'add';
  $el('#username').disabled = false;
  webapp0.useredit.clearUserInfo();
};

webapp0.useredit.editUser = function() {
  webapp0.useredit.mode = 'update';
  $el('#username').disabled = false;
  webapp0.useredit.clearUserInfo();
};

webapp0.useredit.loadUserInfo = function() {
  var username = $el('#username').value.trim();
  if (!username) {
    return;
  }
  var params = {
    username: username,
  };
  app.callServerApi('GetUserInfo', params, webapp0.useredit.loadUserInfoCb);
};

webapp0.useredit.loadUserInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.common.showInfotip(res.status);
    return;
  }
  var userInfo = res.body;
  $el('#username').value = userInfo.username;
  $el('#username').disabled = true;
  $el('#fullname').value = userInfo.fullname;
  $el('#isadmin').checked = userInfo.isAdmin;
  $el('#privileges').value = userInfo.privileges;
  webapp0.common.showInfotip('OK');
};

webapp0.useredit.addUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var isadmin = ($el('#isadmin').checked ? '1' : '0');
  var privileges = $el('#privileges').value;
  var status = $el('#status').value;
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;
  if ((pw1 != '') && (pw2 != '')) {
    if (pw1 != pw2) {
      util.alert('Password mismatched');
      return;
    }
  }

  var salt = username;
  pwHash = webapp0.common.getHash('SHA-256', pw1, salt);

  var params = {
    username: username,
    fullname: fullname,
    isadmin: isadmin,
    privileges: privileges,
    status: status,
    pw: pwHash
  };

  app.callServerApi('AddUser', params, webapp0.useredit.addUserCb);
};

webapp0.useredit.addUserCb = function(xhr, res) {
  webapp0.common.showInfotip(res.status);
};

webapp0.useredit.updateUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var isadmin = ($el('#isadmin').checked ? '1' : '0');
  var privileges = $el('#privileges').value;
  var status = $el('#status').value;
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;
  if ((pw1 != '') && (pw2 != '')) {
    if (pw1 != pw2) {
      util.alert('Password mismatched');
      return;
    }
  }

  var salt = username;
  pwHash = webapp0.common.getHash('SHA-256', pw1, salt);

  var params = {
    username: username,
    fullname: fullname,
    isadmin: isadmin,
    privileges: privileges,
    status: status,
    pw: pwHash
  };

  app.callServerApi('EditUser', params, webapp0.useredit.updateUserCb);
};

webapp0.useredit.updateUserCb = function(xhr, res) {
  webapp0.common.showInfotip(res.status);
};

webapp0.useredit.deleteUser = function() {
  util.confirm('Delete?', webapp0.useredit._deleteUser);
};
webapp0.useredit._deleteUser = function() {
  var username = $el('#username').value.trim();
  if (!username) {
    return;
  }
  var params = {
    username: username,
  };
  app.callServerApi('DeleteUser', params, webapp0.useredit.deleteUserCb);
};

webapp0.useredit.deleteUserCb = function(xhr, res) {
  if (res.status != 'OK') {
    webapp0.common.showInfotip(res.status);
    return;
  }
  webapp0.useredit.clearUserInfo();
  webapp0.common.showInfotip('OK');
};

webapp0.useredit.onEnterKey = function() {
};
