/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.userlist = {};

app.userlist.LIST_COLUMNS = [
  {key: 'username', label: 'Username', style: 'min-width:min-width:10em;'},
  {key: 'fullname', label: 'Full Name', style: 'min-width:13em;'},
  {key: 'localfullname', label: 'Local Full Name', style: 'min-width:10em;'},
  {key: 'is_admin', label: 'Admin'},
  {key: 'privileges', label: 'Privileges', style: 'min-width:20em;'},
  {key: 'status', label: 'Status'},
  {key: 'created_date', label: 'Created'},
  {key: 'updated_date', label: 'Updated'}
];

app.userlist.listStatus = {
  sortIdx: 0,
  sortOrder: 1
};

app.userlist.itemList = [];

app.userlist.editWindow = null;
app.userlist.mode = null;

$onReady = function() {
  app.userlist.getUserList();
};

app.userlist.getUserList = function() {
  app.callServerApi('GetUserInfoList', null, app.userlist.getUserInfoListCb);
};

app.userlist.getUserInfoListCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var infoList = res.body.userlist;
  app.userlist.itemList = infoList;
  app.userlist.drawList(infoList, 0, 1);
};

app.userlist.drawList = function(items, sortIdx, sortOrder) {
  if (sortIdx >= 0) {
    if (sortOrder > 0) {
      var srtDef = app.userlist.LIST_COLUMNS[sortIdx];
      var desc = (sortOrder == 2);
      items = app.userlist.sortList(items, srtDef.key, desc, srtDef.meta);
    }
  }

  var currentUsername = app.getUsername();

  var htmlList = '';
  for (var i = 0; i < items.length; i++) {
    var item = items[i];
    var username = item.username;
    var fullname = item.fullname.replace(/ /g, '&nbsp');
    var localfullname = item.localfullname.replace(/ /g, '&nbsp');

    var createdDate = '---------- --:--:--';
    if (item.created_date > 0) {
      createdDate = util.getDateTimeString(item.created_date, '%YYYY-%MM-%DD %HH:%mm:%SS');
    }

    var updatedDate = '---------- --:--:--';
    if (item.updated_date > 0) {
      updatedDate = util.getDateTimeString(item.updated_date, '%YYYY-%MM-%DD %HH:%mm:%SS');
    }

    htmlList += '<tr class="item-list">';
    htmlList += '<td class="item-list">' + username + '</td>';
    htmlList += '<td class="item-list">' + fullname + '</td>';
    htmlList += '<td class="item-list">' + localfullname + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + (item.is_admin ? 'Y' : '') + '</td>';
    htmlList += '<td class="item-list">' + item.privileges + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + item.status + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + createdDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + updatedDate + '</td>';
    htmlList += '<td class="item-list"><span class="pseudo-link" style="color:#00a;text-align:center;" onclick="app.userlist.editUser(\'' + username + '\');">EDIT</span></td>';
    htmlList += '<td class="item-list" style="text-align:center;width:1.5em;">';
    if (username == currentUsername) {
      htmlList += '&nbsp;';
    } else {
      htmlList += '<span class="pseudo-link" style="color:#f88;" onclick="app.userlist.deleteUser(\'' + username + '\');">X</span>';
    }
    htmlList += '</td>';
    htmlList += '</tr>';
  }
  htmlList += '</table>';

  var htmlHead = app.userlist.buildListHeader(app.userlist.LIST_COLUMNS, sortIdx, sortOrder);
  var html = htmlHead + htmlList; 

  app.userlist.drawListContent(html);
};

app.userlist.drawListContent = function(html) {
  $el('#user-list').innerHTML = html;
};

app.userlist.buildListHeader = function(columns, sortIdx, sortOrder) {
  var html = '<table>';
  html += '<tr class="item-list-header">';

  for (var i = 0; i < columns.length; i++) {
    var column = columns[i];
    var label = column['label'];

    var sortAscClz = '';
    var sortDescClz = '';
    var nextSortType = 1;
    if (i == sortIdx) {
      if (sortOrder == 1) {
        sortAscClz = 'sort-active';
      } else if (sortOrder == 2) {
        sortDescClz = 'sort-active';
      }
      nextSortType = sortOrder + 1;
    }

    var sortButton = '<span class="sort-button" ';
    sortButton += ' onclick="app.userlist.sortItemList(' + i + ', ' + nextSortType + ');"';
    sortButton += '>';
    sortButton += '<span';
    if (sortAscClz) {
       sortButton += ' class="' + sortAscClz + '"';
    }
    sortButton += '>▲</span>';
    sortButton += '<br>';
    sortButton += '<span';
    if (sortDescClz) {
       sortButton += ' class="' + sortDescClz + '"';
    }
    sortButton += '>▼</span>';
    sortButton += '</span>';

    html += '<th class="item-list"';
    if (column.style) {
      html += ' style="' + column.style + '"';
    }
    html += '><span>' + label + '</span> ' + sortButton + '</th>';
  }
  html += '<th class="item-list">&nbsp;</th>';
  html += '<th class="item-list">&nbsp;</th>';
  html += '</tr>';
  return html;
};

app.userlist.sortItemList = function(sortIdx, sortOrder) {
  if (sortOrder > 2) {
    sortOrder = 0;
  }
  app.userlist.listStatus.sortIdx = sortIdx;
  app.userlist.listStatus.sortOrder = sortOrder;
  app.userlist.drawList(app.userlist.itemList, sortIdx, sortOrder);
};

//-----------------------------------------------------------------------------
app.userlist.newUser = function() {
  app.userlist.editUser(null);
};

app.userlist.editUser = function(username) {
  app.userlist.mode = (username ? 'edit' : 'new');
  if (!app.userlist.editWindow) {
    app.userlist.editWindow = app.userlist.openUserInfoEditorWindow(app.userlist.mode);
  }
  app.userlist.clearUserInfoEditor();
  if (username) {
    var params = {
      username: username
    };
    app.callServerApi('GetUserInfo', params, app.userlist.GetUserInfoCb);
  } else {
    $el('#username').focus();
  }
};

app.userlist.openUserInfoEditorWindow = function(mode) {
  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  html += '<div style="padding:4px;position:absolute;top:0;right:0;bottom:0;left:0;margin:auto;width:360px;height:260px;text-align:left;">';

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
  html += '    <td>Local Full name</td>';
  html += '    <td><input type="text" id="localfullname" style="width:100%;"></td>';
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
  html += '<button onclick="app.userlist.saveUserInfo();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="app.userlist.editWindow.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';
  html += '</div>';

  var opt = {
    draggable: true,
    resizable: true,
    pos: 'c',
    closeButton: true,
    width: 480,
    height: 360,
    minWidth: 480,
    minHeight: 360,
    scale: 1,
    hidden: false,
    modal: false,
    title: {
      text: ((mode == 'new') ? 'New' : 'Edit') +' User'
    },
    body: {
      style: {
        background: '#fff'
      }
    },
    onclose: app.userlist.onEditWindowClose,
    content: html
  };

  var win = util.newWindow(opt);
  return win;
};

app.userlist.GetUserInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  app.userlist.setUserInfoToEditor(info);
};

app.userlist.setUserInfoToEditor = function(info) {
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
  $el('#localfullname').value = info.localfullname;
  $el('#isadmin').checked = info.is_admin;
  $el('#privileges').value = info.privileges;
  $el('#status').value = info.status;
};

app.userlist.clearUserInfoEditor = function() {
  var info = {
    username: '',
    fullname: '',
    localfullname: '',
    is_admin: false,
    privileges: '',
    status: ''
  };
  app.userlist.setUserInfoToEditor(info);
};

app.userlist.saveUserInfo = function() {
  if (app.userlist.mode == 'new') {
    app.userlist.addUser();
  } else {
    app.userlist.updateUser();
  }
};

//-----------------------------------------------------------------------------
app.userlist.addUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var localfullname = $el('#localfullname').value;
  var isAdmin = ($el('#isadmin').checked ? '1' : '0');
  var privileges = $el('#privileges').value;
  var status = $el('#status').value.trim();
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = app.userlist.cleanseUsername(username);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  username = clnsRes.val;

  clnsRes = app.userlist.cleanseFullName(fullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  fullname = clnsRes.val;

  clnsRes = app.userlist.cleanseFullName(localfullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  localfullname = clnsRes.val;

  clnsRes = app.userlist.cleansePrivilege(privileges);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  privileges = clnsRes.val;

  clnsRes = app.userlist.cleansePW(pw1, pw2, 'new');
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  var pw = clnsRes.val;

  var params = {
    username: username,
    fullname: fullname,
    localfullname: localfullname,
    is_admin: isAdmin,
    privileges: privileges,
    status: status,
  };
  if (pw) {
    var salt = username;
    params.pw = app.common.getHash('SHA-256', pw, salt);
  }

  app.callServerApi('AddUser', params, app.userlist.addUserCb);
};

app.userlist.addUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.userlist.editWindow.close();
  app.userlist.getUserList();
};

//-----------------------------------------------------------------------------
app.userlist.updateUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var localfullname = $el('#localfullname').value;
  var isAdmin = ($el('#isadmin').checked ? '1' : '0');
  var privileges = $el('#privileges').value;
  var status = $el('#status').value;
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = app.userlist.cleansePW(pw1, pw2, 'edit');
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  var pw = clnsRes.val;

  var params = {
    username: username,
    fullname: fullname,
    localfullname: localfullname,
    is_admin: isAdmin,
    privileges: privileges,
    status: status,
  };

  if (pw) {
    var salt = username;
    params.pw = app.common.getHash('SHA-256', pw, salt);
  }

  app.callServerApi('EditUser', params, app.userlist.updateUserCb);
};

app.userlist.updateUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.userlist.editWindow.close();
  app.userlist.getUserList();
};

//-----------------------------------------------------------------------------
app.userlist.deleteUser = function(username) {
  var opt = {
    data: username
  };
  util.confirm('Delete ' + username + ' ?', app.userlist._deleteUser, opt);
};
app.userlist._deleteUser = function(username) {
  if (!username) {
    return;
  }
  var params = {
    username: username,
  };
  app.callServerApi('DeleteUser', params, app.userlist.deleteUserCb);
};

app.userlist.deleteUserCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  app.userlist.getUserList();
};

//-----------------------------------------------------------------------------
app.userlist.sortList = function(itemList, sortKey, desc) {
  var items = util.copyObject(itemList);
  var srcList = items;
  var asNum = true;
  var sortedList = util.sortObject(srcList, sortKey, desc, asNum);
  return sortedList;
};

//-----------------------------------------------------------------------------
app.userlist.cleanseCommon = function(s) {
  s = s.trim();
  s = s.replace(/\t/g, ' ');
  var res = {
    val: s,
    msg: null
  };
  return res;
};

app.userlist.cleanseUsername = function(s) {
  var res = app.userlist.cleanseCommon(s);
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

app.userlist.cleanseFullName = function(s) {
  var res = app.userlist.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

app.userlist.cleanseLocalFullName = function(s) {
  var res = app.userlist.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

app.userlist.cleansePW = function(pw1, pw2, mode) {
  var msg = null;
  if (mode == 'new') {
    if (pw1 == '') {
      msg = 'Password is required';
    }
  }
  if ((pw1 != '') || (pw2 != '')) {
    if (pw1 != pw2) {
      msg = 'Password mismatched';
    }
  }
  var res = {
    val: pw1,
    msg: msg
  };
  return res;
};

app.userlist.cleansePrivilege = function(s) {
  var res = app.userlist.cleanseCommon(s);
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
app.userlist.onEditWindowClose = function() {
  app.userlist.editWindow = null;
  app.userlist.mode = null;
};

$onBeforeUnload = function(e) {
  if (app.userlist.editWindow) e.returnValue = '';
};
