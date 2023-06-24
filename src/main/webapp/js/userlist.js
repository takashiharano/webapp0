/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 */
webapp0.userlist = {};

webapp0.userlist.editWindow = null;
webapp0.userlist.mode = null;

$onReady = function() {
  webapp0.userlist.getUserList();
};

webapp0.userlist.getUserList = function() {
  app.callServerApi('GetUserInfoList', null, webapp0.userlist.getUserInfoListCb);
};

webapp0.userlist.getUserInfoListCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var currentUsername = app.getUsername();
  var infoList = res.body.userlist;

  var html = '<table>';
  html += '<tr class="user-list-header">';
  html += '<th class="user-list" style="min-width:10em;">Username</th>';
  html += '<th class="user-list" style="min-width:15em;">Full Name</th>';
  html += '<th class="user-list">Admin</th>';
  html += '<th class="user-list" style="min-width:20em;">Privileges</th>';
  html += '<th class="user-list">Status</th>';
  html += '<th class="user-list">&nbsp;</th>';
  html += '<th class="user-list">&nbsp;</th>';
  html += '</tr>';
  for (var i = 0; i < infoList.length; i++) {
    var info = infoList[i];
    var username = info.username;
    html += '<tr>';
    html += '<td class="user-list">' + username + '</td>';
    html += '<td class="user-list">' + info.fullname + '</td>';
    html += '<td class="user-list" style="text-align:center;">' + (info.isAdmin ? 'Y' : '') + '</td>';
    html += '<td class="user-list">' + info.privileges + '</td>';
    html += '<td class="user-list" style="text-align:center;">' + info.status + '</td>';
    html += '<td class="user-list"><span class="pseudo-link" style="color:#00a;text-align:center;" onclick="webapp0.userlist.editUser(\'' + username + '\');">EDIT</span></td>';
    html += '<td class="user-list" style="text-align:center;width:1.5em;">';
    if (username == currentUsername) {
      html += '&nbsp;';
    } else {
      html += '<span class="pseudo-link" style="color:#f88;" onclick="webapp0.userlist.deleteUser(\'' + username + '\');">X</span>';
    }
    html += '</td>';
    html += '</tr>';
  }
  html += '</table>';
  $el('#user-list').innerHTML = html;
};

webapp0.userlist.newUser = function() {
  webapp0.userlist.editUser(null);
};

webapp0.userlist.editUser = function(username) {
  webapp0.userlist.mode = (username ? 'edit' : 'add');
  if (!webapp0.userlist.editWindow) {
    webapp0.userlist.editWindow = webapp0.userlist.openUserInfoEditorWindow();
  }
  webapp0.userlist.clearUserInfoEditor();
  if (username) {
    var params = {
      username: username
    };
    app.callServerApi('GetUserInfo', params, webapp0.userlist.GetUserInfoCb);
  } else {
    $el('#username').focus();
  }
};

webapp0.userlist.openUserInfoEditorWindow = function() {
  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  html += '<div style="padding:4px;position:absolute;top:0;right:0;bottom:0;left:0;margin:auto;width:360px;height:230px;text-align:left;">';

  html += '<table>';
  html += '  <tr>';
  html += '    <td>Username</td>';
  html += '    <td style="width:256px;">';
  html += '      <input type="text" id="username" style="width:100%;">';
  html += '    </td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Full name</td>';
  html += '    <td><input type="text" id="fullname" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>isAdmin</td>';
  html += '    <td><input type="checkbox" id="isadmin">';
  html += '    </td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Privileges</td>';
  html += '    <td><input type="text" id="privileges" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Status</td>';
  html += '    <td><input type="text" id="status" style="width:1.5em;"></td>';
  html += '  </tr>';

  html += '  <tr>';
  html += '    <td>&nbsp;</td>';
  html += '    <td>&nbsp;</td>';
  html += '  </tr>';

  html += '  <tr>';
  html += '    <td>Password</td>';
  html += '    <td><input type="password" id="pw1" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Re-type</td>';
  html += '    <td><input type="password" id="pw2" style="width:100%;"></td>';
  html += '  </tr>';
  html += '<table>';

  html += '<div style="margin-top:24px;text-align:center;">';
  html += '<button onclick="webapp0.userlist.saveUserInfo();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="webapp0.userlist.editWindow.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';
  html += '</div>';

  var opt = {
    draggable: true,
    resizable: true,
    pos: 'c',
    closeButton: true,
    width: 400,
    height: 300,
    minWidth: 400,
    minHeight: 300,
    scale: 1,
    hidden: false,
    modal: false,
    title: {
      text: 'Edit User'
    },
    body: {
      style: {
        background: '#fff'
      }
    },
    onclose: webapp0.userlist.onEditWindowClose,
    content: html
  };

  var win = util.newWindow(opt);
  return win;
};

webapp0.userlist.GetUserInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  webapp0.userlist.setUserInfoToEditor(info);
};

webapp0.userlist.setUserInfoToEditor = function(info) {
  var username = info.username;
  $el('#username').value = username;
  if (username) {
    $el('#username').disabled = true;
    $el('#username').addClass('edit-disabled');
  } else {
    $el('#username').disabled = false;
    $el('#username').removeClass('edit-disabled');
  }
  $el('#fullname').value = info.fullname;
  $el('#isadmin').checked = info.isAdmin;
  $el('#privileges').value = info.privileges;
  $el('#status').value = info.status;
};

webapp0.userlist.clearUserInfoEditor = function() {
  var info = {
    username: '',
    fullname: '',
    isAdmin: false,
    privileges: '',
    status: ''
  };
  webapp0.userlist.setUserInfoToEditor(info);
};

webapp0.userlist.saveUserInfo = function() {
  if (webapp0.userlist.mode == 'add') {
    webapp0.userlist.addUser();
  } else {
    webapp0.userlist.updateUser();
  }
};

webapp0.userlist.addUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var isadmin = ($el('#isadmin').checked ? '1' : '0');
  var privileges = $el('#privileges').value;
  var status = $el('#status').value.trim();
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = webapp0.userlist.cleanseUsername(username);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  username = clnsRes.val;

  var clnsRes = webapp0.userlist.cleanseFullName(fullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  fullname = clnsRes.val;

  var clnsRes = webapp0.userlist.cleansePrivilege(privileges);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  privileges = clnsRes.val;

  if ((pw1 != '') || (pw2 != '')) {
    if (pw1 != pw2) {
      app.showInfotip('Password mismatched', 2000);
      return false;
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

  app.callServerApi('AddUser', params, webapp0.userlist.addUserCb);
};

webapp0.userlist.addUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  webapp0.userlist.editWindow.close();
  webapp0.userlist.getUserList();
};

webapp0.userlist.updateUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var isadmin = ($el('#isadmin').checked ? '1' : '0');
  var privileges = $el('#privileges').value;
  var status = $el('#status').value;
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;
  if ((pw1 != '') || (pw2 != '')) {
    if (pw1 != pw2) {
      app.showInfotip('Password mismatched', 2000);
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

  app.callServerApi('EditUser', params, webapp0.userlist.updateUserCb);
};

webapp0.userlist.updateUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  webapp0.userlist.editWindow.close();
  webapp0.userlist.getUserList();
};

webapp0.userlist.deleteUser = function(username) {
  var opt = {
    data: username
  };
  util.confirm('Delete ' + username + ' ?', webapp0.userlist._deleteUser, opt);
};
webapp0.userlist._deleteUser = function(username) {
  if (!username) {
    return;
  }
  var params = {
    username: username,
  };
  app.callServerApi('DeleteUser', params, webapp0.userlist.deleteUserCb);
};

webapp0.userlist.deleteUserCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  webapp0.userlist.getUserList();
};

//-----------------------------------------------------------------------------
webapp0.userlist.cleanseCommon = function(s) {
  s = s.trim();
  s = s.replace(/\t/g, ' ');
  var res = {
    val: s,
    msg: null
  };
  return res;
};

webapp0.userlist.cleanseUsername = function(s) {
  var res = webapp0.userlist.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  if (!s) {
    msg = 'Username is required';
  }
  res.val = s;
  res.msg = msg;
  return res;
};

webapp0.userlist.cleanseFullName = function(s) {
  var res = webapp0.userlist.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

webapp0.userlist.cleansePrivilege = function(s) {
  var res = webapp0.userlist.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  s = s.replace(/\s{2,}/g, ' ');
  res.val = s;
  res.msg = msg;
  return res;
};


//-----------------------------------------------------------------------------
webapp0.userlist.onEditWindowClose = function() {
  webapp0.userlist.editWindow = null;
  webapp0.userlist.mode = null;
};

$onBeforeUnload = function(e) {
  if (webapp0.userlist.editWindow) e.returnValue = '';
};
