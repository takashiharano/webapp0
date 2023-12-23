/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.userlist = {};

app.userlist.INTERVAL = 2 * 60 * 1000;
app.userlist.LIST_COLUMNS = [
  {key: 'username', label: 'Username', style: 'min-width:min-width:10em;'},
  {key: 'fullname', label: 'Full Name', style: 'min-width:13em;'},
  {key: 'localfullname', label: 'Local Full Name', style: 'min-width:10em;'},
  {key: 'is_admin', label: 'Admin'},
  {key: 'groups', label: 'Groups', style: 'min-width:15em;'},
  {key: 'privileges', label: 'Privileges', style: 'min-width:15em;'},
  {key: 'description', label: 'Description', style: 'min-width:15em;'},
  {key: 'status', label: 'Status'},
  {key: 'created_date', label: 'Created'},
  {key: 'updated_date', label: 'Updated'},
  {key: 'pw_changed_date', label: 'PwChanged'}
];

app.userlist.listStatus = {
  sortIdx: 0,
  sortOrder: 1
};

app.userlist.itemList = [];
app.userlist.sessions = null;
app.userlist.currentSid = null;
app.userlist.editWindow = null;
app.userlist.mode = null;
app.userlist.tmrId = 0;
app.userlist.interval = 0;

$onReady = function() {
  app.userlist.reload();
  app.userlist.queueNextUpdateSessionInfo();
};

app.userlist.reload = function() {
  app.userlist.getUserList();
  app.userlist.getSessionList();
  app.userlist.getGroups();
};

app.userlist.queueNextUpdateSessionInfo = function() {
  app.userlist.tmrId = setTimeout(app.userlist.updateSessionInfo, app.userlist.INTERVAL);
};

app.userlist.updateSessionInfo = function() {
  app.userlist.interval = 1;
  app.userlist.getSessionList();
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

    var pwChangedDate = '---------- --:--:--';
    if (item.pw_changed_date > 0) {
      pwChangedDate = util.getDateTimeString(item.pw_changed_date, '%YYYY-%MM-%DD %HH:%mm:%SS');
    }

    var desc = (item.description ? item.description : '');
    var escDesc = util.escHtml(desc);
    var dispDesc = '<span style="display:inline-block;width:100%;overflow:hidden;text-overflow:ellipsis;"';
    if (util.lenW(desc) > 35) {
      dispDesc += ' data-tooltip="' + escDesc + '"';
    }
    dispDesc += '>' + escDesc + '</span>';

    htmlList += '<tr class="item-list">';
    htmlList += '<td class="item-list"><span class="pseudo-link link-button" style="text-align:center;" onclick="app.userlist.editUser(\'' + username + '\');" data-tooltip="Edit">' + username + '</span></td>';
    htmlList += '<td class="item-list">' + fullname + '</td>';
    htmlList += '<td class="item-list">' + localfullname + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + (item.is_admin ? 'Y' : '') + '</td>';
    htmlList += '<td class="item-list">' + item.groups + '</td>';
    htmlList += '<td class="item-list">' + item.privileges + '</td>';
    htmlList += '<td class="item-list" style="max-width:20em">' + dispDesc + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + item.status + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + createdDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + updatedDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + pwChangedDate + '</td>';
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

app.userlist.getSessionList = function() {
  if (app.userlist.tmrId > 0) {
    clearTimeout(app.userlist.tmrId);
    app.userlist.tmrId = 0;
    app.userlist.interval = 1;
  }
  app.callServerApi('GetSessionInfoList', null, app.userlist.getSessionListCb);
};
app.userlist.getSessionListCb = function(xhr, res, req) {
  if (res.status == 'FORBIDDEN') {
    location.href = location.href;
    return;
  } else if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var data = res.body;
  var sessions = data.sessions;
  app.userlist.sessions = sessions;
  app.currentSid = data.currentSid;
  app.userlist.drawSessionList(sessions);

  if (app.userlist.interval) {
    app.userlist.interval = 0;
    app.userlist.queueNextUpdateSessionInfo();
  }
};

app.userlist.drawSessionList = function(sessions) {
  var now = util.now();

  var html = '<table>';
  html += '<tr style="font-weight:bold;">';
  html += '<td></td>';
  html += '<td>UID</td>';
  html += '<td>Name</td>';
  html += '<td>Session</td>';
  html += '<td>Last Accessed</td>';
  html += '<td>Elapsed</td>';
  html += '<td style="font-weight:normal;">' + app.userlist.buildTimeLineHeader(now) + '</td>';
  html += '<td>Addr</td>';
  html += '<td>User-Agent</td>';
  html += '<td>Logged in</td>';
  html += '</tr>';

  sessions = util.sortObject(sessions, 'lastAccessedTime', true, true);
  html += app.userlist.buildSessionInfoHtml(sessions);
  html += '</table>';
  $el('#session-list').innerHTML = html;
};

app.userlist.buildTimeLineHeader = function(now) {
  var currentInd = '<span class="blink1" style="color:#08c;">v</span>';

  var nowYYYYMMDD = util.getDateTimeString(now, '%YYYY%MM%DD');
  var nowHHMM = util.getDateTimeString(now, '%HH:%mm');
  var tmp = nowHHMM.split(':');
  var nowHH = tmp[0];
  var nowMM = tmp[1];

  var html = '';
  for (var i = 0; i <= 23; i++) {
    var ts = app.userlist.getTimeSlot(i, nowHH, nowMM);
    var v = false;
    if (i < 10) {
      if (ts == 0) {
        html += currentInd;
      }
    } else {
      if (ts == 0) {
        html += currentInd + ' ';
      } else if (ts == 1) {
        html += ' ' + currentInd;
      }
    }

    if (!((ts == 0) || ((i >= 10) && (ts == 1)))) {
      html += i;
    }

    var st = ((i < 10) ? 1 : 2);
    for (var j = st; j <= 4; j++) {
      if (ts == j) {
        html += currentInd;
      } else {
        html += ' ';
      }
    }
  }
  return html;
};

app.userlist.buildSessionInfoHtml = function(sessions) {
  var html = '';
  if (!sessions) return html;
  var now = util.now();
  var mn = util.getMidnightTimestamp(now);
  for (var i = 0; i < sessions.length; i++) {
    var session = sessions[i];
    html += app.userlist.buildSessionInfoOne(session, now, mn);
  }
  return html;
};
app.userlist.buildSessionInfoOne = function(session, now, mn) {
  var username = session.username;
  var name = session.fullName;
  var ua = session.ua;
  var loginT = session.createdTime;
  var laTime = session.lastAccessedTime;
  var loginTime = util.getDateTimeString(loginT, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss')
  var laTimeStr = util.getDateTimeString(laTime, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss')
  var sid = session['sid'];
  var ssid = util.snip(sid, 7, 2, '..');
  var sid7 = util.snip(sid, 7, 0, '');
  var addr = session.addr;
  var brws = util.getBrowserInfo(ua);
  var ua = brws.name + ' ' + brws.version;

  var elapsed = now - laTime;
  var ledColor = '#888';
  if (elapsed <= 10 * util.MINUTE) {
    ledColor = '#0f0';
  } else if (elapsed <= 30 * util.MINUTE) {
    ledColor = '#0a0';
  } else if (elapsed <= 6 * util.HOUR) {
    ledColor = '#080';
  } else if (laTime >= mn) {
    ledColor = '#262';
  }

  var led = '<span class="led" style="color:' + ledColor + '"></span>'
  var ssidLink = '<span class="pseudo-link link-button" onclick="app.userlist.confirmLogoutSession(\'' + username + '\', \'' + sid + '\');" data-tooltip="' + sid + '">' + ssid + '</span>';
  var timeId = 'tm-' + sid7;
  var tmspan = '<span id="' + timeId + '"></span>'
  var timeline = app.userlist.buildTimeLine(now, laTime);

  var html = '';
  html += '<tr class="item-list">';
  html += '<td style="padding-right:4px;">' + led + '</td>';
  html += '<td style="padding-right:10px;">' + username + '</td>';
  html += '<td style="padding-right:10px;">' + name + '</td>';
  html += '<td style="padding-right:10px;">' + ssidLink + '</td>';
  html += '<td style="padding-right:10px;">' + laTimeStr + '</td>';
  html += '<td style="padding-right:10px;text-align:right;">' + tmspan + '</td>';
  html += '<td>' + timeline + '</td>';
  html += '<td style="padding-right:10px;">' + addr + '</td>';
  html += '<td style="padding-right:10px;">' + ua + '</td>';
  html += '<td style="padding-right:10px;">' + loginTime + '</td>';
  html += '</tr>';

  util.timecounter.start('#' + timeId, laTime);
  return html;
};
app.userlist.buildTimeLine = function(now, lastAccessedTime) {
  var mn = util.getMidnightTimestamp(now);
  var nowYYYYMMDD = util.getDateTimeString(now, '%YYYY%MM%DD');
  var nowHHMM = util.getDateTimeString(now, '%HH:%mm');
  var tmp = nowHHMM.split(':');
  var nowHH = tmp[0];
  var nowMM = tmp[1];
  var accYYYYMMDD = util.getDateTimeString(lastAccessedTime, '%YYYY%MM%DD');
  var accHHMM = util.getDateTimeString(lastAccessedTime, '%HH:%mm');
  tmp = accHHMM.split(':');
  var accHH = tmp[0];
  var accMM = tmp[1];

  var span = '<span style="opacity:0.6;">';
  var html = span;
  var f = false;
  for (var i = 0; i <= 23; i++) {
    if ((i == 0) && (lastAccessedTime < mn)) {
      html += '</span><span style="color:#d66;">&lt;</span>' + span;
    } else {
      html += '|';
    }
    for (var j = 0; j < 4; j++) {
      var s = '-';
      if ((accYYYYMMDD == nowYYYYMMDD) && (app.userlist.inTheTimeSlot(i, j, accHH, accMM))) {
        s = '</span><span style="color:#0c0;">*</span>' + span;
      }
      html += s;
      if (app.userlist.inTheTimeSlot(i, j, nowHH, nowMM)) {
        html += '<span style="opacity:0.5;">';
        f = true;
      }
    }
  }
  if (f) html += '</span>';
  html += '</span>';
  return html;
};

app.userlist.inTheTimeSlot = function(h, qM, hh, mm) {
  if (hh == h) {
    if ((qM == 0) && (mm < 15)) {
      return true;
    } else if ((qM == 1) && (mm >= 15) && (mm < 30)) {
      return true;
    } else if ((qM == 2) && (mm >= 30) && (mm < 45)) {
      return true;
    } else if ((qM == 3) && (mm >= 45)) {
      return true;
    }
  }
  return false;
};

app.userlist.getTimeSlot = function(h, hh, mm) {
  if (h == hh) {
    if (mm == 0) {
      return 0;
    } else if (mm < 15) {
      return 1;
    } else if ((mm >= 15) && (mm < 30)) {
      return 2;
    } else if ((mm >= 30) && (mm < 45)) {
      return 3;
    } else if (mm >= 45) {
      return 4;
    }
  }
  return -1;
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
app.userlist.confirmLogoutSession = function(username, sid) {
  var cSid = app.currentSid;
  var ssid = util.snip(sid, 7, 7, '..');
  var m = 'Logout?\n\n';
  if (sid == cSid) {
    m += '<span style="color:#f44;font-weight:bold;">[CURRENT SESSION]</span>\n';
  }
  m += '<div style="text-align:left;">';
  m += username + '\n';
  m += 'sid: ' + sid;
  m += '</div>';
  util.confirm(m, app.userlist.logoutSession, {data: sid});
};
app.userlist.logoutSession = function(sid) {
  var params = {
    sid: sid
  };
  app.callServerApi('logout', params, app.userlist.logoutSessionCb);
};
app.userlist.logoutSessionCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.userlist.getSessionList();
};

//-----------------------------------------------------------------------------
app.userlist.newUser = function() {
  app.userlist.editUser(null);
};

app.userlist.editUser = function(username) {
  app.userlist.mode = (username ? 'edit' : 'new');
  if (!app.userlist.editWindow) {
    app.userlist.editWindow = app.userlist.openUserInfoEditorWindow(app.userlist.mode, username);
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

app.userlist.openUserInfoEditorWindow = function(mode, username) {
  var currentUsername = app.getUsername();

  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  if (username && (username != currentUsername)) {
    html += '<div style="position:absolute;top:8px;right:8px;"><button class="button-red" onclick="app.userlist.deleteUser(\'' + username + '\');">DEL</button></div>';
  }
  html += '<div style="padding:4px;position:absolute;top:0;right:0;bottom:0;left:0;margin:auto;width:360px;height:350px;text-align:left;">';

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
  html += '    <td>Groups</td>';
  html += '    <td><input type="text" id="groups" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Privileges</td>';
  html += '    <td><input type="text" id="privileges" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Description</td>';
  html += '    <td><input type="text" id="description" style="width:100%;"></td>';
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
    height: 450,
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
  $el('#groups').value = info.groups;
  $el('#privileges').value = info.privileges;
  $el('#description').value = info.description;
  $el('#status').value = info.status;
};

app.userlist.clearUserInfoEditor = function() {
  var info = {
    username: '',
    fullname: '',
    localfullname: '',
    is_admin: false,
    groups: '',
    privileges: '',
    description: '',
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
  var groups = $el('#groups').value;
  var privileges = $el('#privileges').value;
  var description = $el('#description').value;
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

  clnsRes = app.userlist.cleanseGroups(groups);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  groups = clnsRes.val;

  clnsRes = app.userlist.cleansePrivileges(privileges);
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
    groups: groups,
    privileges: privileges,
    description: description,
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
  var groups = $el('#groups').value;
  var privileges = $el('#privileges').value;
  var description = $el('#description').value;
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
    groups: groups,
    privileges: privileges,
    description : description,
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
  if (app.userlist.editWindow) {
    app.userlist.editWindow.close();
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

app.userlist.cleanseGroups = function(s) {
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

app.userlist.cleansePrivileges = function(s) {
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
app.userlist.drawGroupStatus = function(s) {
  $el('#groups-status').innerHTML = s;
};

app.userlist.getGroups = function() {
  app.callServerApi('GetGroupsDefinition', null, app.userlist.getGroupsCb);
};
app.userlist.getGroupsCb = function(xhr, res) {
  app.userlist.drawGroupStatus('');
  var s = util.decodeBase64(res.body);
  $el('#groups-text').value = s;
};

app.userlist.confirmSaveGroups = function() {
  util.confirm('Save?', app.userlist.saveGroups);
};
app.userlist.saveGroups = function() {
  var s = $el('#groups-text').value;
  var b64 = util.encodeBase64(s);
  var params = {
    text: b64
  }
  app.callServerApi('SaveGroupsDefinition', params, app.userlist.saveGroupsCb);
};
app.userlist.saveGroupsCb = function(xhr, res) {
  app.showInfotip('OK');
};


//-----------------------------------------------------------------------------
app.userlist.onEditWindowClose = function() {
  app.userlist.editWindow = null;
  app.userlist.mode = null;
};

$onCtrlS = function(e) {
  if ($el('#groups-text').hasFocus()) {
    app.userlist.confirmSaveGroups();
  }
};

$onBeforeUnload = function(e) {
  if (app.userlist.editWindow) e.returnValue = '';
};
