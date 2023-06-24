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
  html += '<th class="user-list">username</th>';
  html += '<th class="user-list">Full Name</th>';
  html += '<th class="user-list">isAdmin</th>';
  html += '<th class="user-list">Privileges</th>';
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
    html += '<td class="user-list">' + (info.isAdmin ? 'Y' : '') + '</td>';
    html += '<td class="user-list">' + info.privileges + '</td>';
    html += '<td class="user-list">' + info.status + '</td>';
    html += '<td class="user-list"><span class="pseudo-link" style="color:#00a;" onclick="webapp0.userlist.editUser(\'' + username + '\');">EDIT</span></td>';
    html += '<td class="user-list">';
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
  if (webapp0.userlist.editWindow) {
    return;
  }

  webapp0.userlist.mode = (username ? 'edit' : 'add');

  var html = '';
  html += '<div style="width:100%;height:100%;">';
  html += '<div style="padding:4px;">';

  html += '<table>';
  html += '  <tr>';
  html += '    <td>Username</td>';
  html += '    <td>';
  html += '      <input type="text" id="username">';
  html += '    </td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Full name</td>';
  html += '    <td><input type="text" id="fullname"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>isAdmin</td>';
  html += '    <td><input type="checkbox" id="isadmin">';
  html += '    </td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Privileges</td>';
  html += '    <td><input type="text" id="privileges"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Status</td>';
  html += '    <td><input type="text" id="status"></td>';
  html += '  </tr>';

  html += '  <tr>';
  html += '    <td>&nbsp;</td>';
  html += '    <td>&nbsp;</td>';
  html += '  </tr>';

  html += '  <tr>';
  html += '    <td>Password</td>';
  html += '    <td><input type="password" id="pw1"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Re-type</td>';
  html += '    <td><input type="password" id="pw2"></td>';
  html += '  </tr>';
  html += '<table>';

  html += '<div style="margin-top:16px;">';
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

  webapp0.userlist.editWindow = util.newWindow(opt);

  if (username) {
    var params = {
      username: username
    };
    app.callServerApi('GetUserInfo', params, webapp0.userlist.GetUserInfoCb);
  }
};
webapp0.userlist.GetUserInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  $el('#username').value = info.username;
  $el('#username').disabled = true;
  $el('#fullname').value = info.fullname;
  $el('#isadmin').checked = info.isAdmin;
  $el('#privileges').value = info.privileges;
  $el('#status').value = info.status;
};

webapp0.userlist.saveUserInfo = function() {
  if (webapp0.userlist.mode == 'add') {
    webapp0.userlist.addUser();
  } else {
    webapp0.userlist.updateUser();
  }
  webapp0.userlist.editWindow.close();
};

webapp0.userlist.addUser = function() {
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

  app.callServerApi('AddUser', params, webapp0.userlist.addUserCb);
};

webapp0.userlist.addUserCb = function(xhr, res) {
  app.showInfotip(res.status);
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

  app.callServerApi('EditUser', params, webapp0.userlist.updateUserCb);
};

webapp0.userlist.updateUserCb = function(xhr, res) {
  app.showInfotip(res.status);
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


webapp0.userlist.onEditWindowClose = function() {
  webapp0.userlist.editWindow = null;
  webapp0.userlist.mode = null;
};


