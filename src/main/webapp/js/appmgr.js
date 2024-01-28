/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.appmgr = {};
app.appmgr.INSEC = false;
app.appmgr.dialogFgColor = '#000';
app.appmgr.dialogBgColor = '#fff';
app.appmgr.dialogTitleFgColor = '#fff';
app.appmgr.dialogTitleBgColor = 'linear-gradient(150deg, rgba(0,32,255,0.8),rgba(0,82,255,0.8))';

app.appmgr.LED_COLORS = [
  {t: 10 * util.MINUTE, color: '#4dd965'},
  {t: 8 * util.HOUR, color: '#ffba00'},
  {t: 0, color: '#f44d41'},
];

app.appmgr.INTERVAL = 60000;
app.appmgr.USER_LIST_COLUMNS = [
  {key: 'username', label: 'Username', style: 'min-width:min-width:10em;'},
  {key: 'fullname', label: 'Full Name', style: 'min-width:10em;'},
  {key: 'localfullname', label: 'Local Full Name', style: 'min-width:10em;'},
  {key: 'is_admin', label: 'Admin'},
  {key: 'groups', label: 'Groups', style: 'min-width:15em;'},
  {key: 'privileges', label: 'Privileges', style: 'min-width:15em;'},
  {key: 'description', label: 'Description', style: 'min-width:15em;'},
  {key: 'flags', label: 'Flags'},
  {key: 'status_info.login_failed.count', label: 'Fail', sort: false},
  {key: 'status_info.sessions', label: 'S'},
  {key: 'status_info.last_accessed', label: 'Last Accessed'},
  {key: 'status_info.last_login', label: 'Last Login'},
  {key: 'status_info.last_logout', label: 'Last Logout'},
  {key: 'created_date', label: 'Created'},
  {key: 'updated_date', label: 'Updated'},
  {key: 'status_info.pw_changed_at', label: 'PwChanged'}
];

app.appmgr.listStatus = {
  sortIdx: 0,
  sortOrder: 1
};

app.appmgr.itemList = [];
app.appmgr.sessions = null;
app.appmgr.currentSid = null;
app.appmgr.userEditWindow = null;
app.appmgr.userEditMode = null;
app.appmgr.groupEditWindow = null;
app.appmgr.groupEditMode = null;
app.appmgr.tmrId = 0;
app.appmgr.interval = 0;

$onReady = function() {
  app.appmgr.reload();
  app.appmgr.queueNextUpdateSessionInfo();
};

app.appmgr.reload = function() {
  app.appmgr.reloadUserInfo();
  app.appmgr.getGroupList();
};

app.appmgr.reloadUserInfo = function() {
  app.appmgr.getUserList();
  app.appmgr.getSessionList();
};

app.appmgr.queueNextUpdateSessionInfo = function() {
  app.appmgr.tmrId = setTimeout(app.appmgr.updateSessionInfo, app.appmgr.INTERVAL);
};

app.appmgr.updateSessionInfo = function() {
  app.appmgr.interval = 1;
  app.appmgr.reloadUserInfo();
};

app.appmgr.getUserList = function() {
  app.callServerApi('GetUserInfoList', null, app.appmgr.getUserInfoListCb);
};

app.appmgr.getUserInfoListCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var infoList = res.body.userlist;
  app.appmgr.itemList = infoList;
  app.appmgr.drawList(infoList, 0, 1);
};

app.appmgr.buildListHeader = function(columns, sortIdx, sortOrder) {
  var html = '<table>';
  html += '<tr class="item-list-header">';
  html += '<th class="item-list">&nbsp;</th>';

  for (var i = 0; i < columns.length; i++) {
    var column = columns[i];
    var label = column['label'];
    var sortable = (column['sort'] === false ? false : true);

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
    sortButton += ' onclick="app.appmgr.sortItemList(' + i + ', ' + nextSortType + ');"';
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
    html += '><span>' + label + '</span>';
    if (sortable) {
      html += ' ' + sortButton;
    }
    html += '</th>';
  }

  html += '</tr>';
  return html;
};

app.appmgr.drawList = function(items, sortIdx, sortOrder) {
  var now = util.now();
  var currentUsername = app.getUsername();

  if (sortIdx >= 0) {
    if (sortOrder > 0) {
      var srtDef = app.appmgr.USER_LIST_COLUMNS[sortIdx];
      var desc = (sortOrder == 2);
      items = app.appmgr.sortList(items, srtDef.key, desc, srtDef.meta);
    }
  }

  var htmlList = '';
  for (var i = 0; i < items.length; i++) {
    var item = items[i];
    var username = item.username;
    var fullname = item.fullname.replace(/ /g, '&nbsp');
    var localfullname = item.localfullname.replace(/ /g, '&nbsp');
    var statusInfo = item.status_info;
    var loginFailedCount = statusInfo.login_failed_count;
    var loginFailedTime = util.getDateTimeString(statusInfo.login_failed_time);
    var sessions = statusInfo.sessions;
    var lastAccessedDate = app.appmgr.getDateTimeString(statusInfo.last_accessed, app.appmgr.INSEC);
    var lastLoginDate = app.appmgr.getDateTimeString(statusInfo.last_login, app.appmgr.INSEC);
    var lastLogoutDate = app.appmgr.getDateTimeString(statusInfo.last_logout, app.appmgr.INSEC);
    var createdDate = app.appmgr.getDateTimeString(item.created_date, app.appmgr.INSEC);
    var updatedDate = app.appmgr.getDateTimeString(item.updated_date, app.appmgr.INSEC);
    var pwChangedDate = app.appmgr.getDateTimeString(statusInfo.pw_changed_at, app.appmgr.INSEC);
    var desc = (item.description ? item.description : '');
    var escDesc = util.escHtml(desc);
    var dispDesc = '<span style="display:inline-block;width:100%;overflow:hidden;text-overflow:ellipsis;"';
    if (util.lenW(desc) > 35) {
      dispDesc += ' data-tooltip="' + escDesc + '"';
    }
    dispDesc += '>' + escDesc + '</span>';
    var active = (sessions > 0);
    var led = app.appmgr.buildLedHtml(now, statusInfo.last_accessed, app.appmgr.INSEC, active);

    var cInd = ((username == currentUsername) ? '<span class="text-skyblue" style="cursor:default;margin-right:2px;" data-tooltip="You">*</span>' : '<span style="margin-right:2px;">&nbsp;</span>');
    var dispUid = cInd + '<span class="pseudo-link link-button" style="text-align:center;" onclick="app.appmgr.editUser(\'' + username + '\');" data-tooltip="Edit">' + username + '</span>';

    htmlList += '<tr class="item-list">';
    htmlList += '<td class="item-list" style="text-align:center;">' + led + '</td>';
    htmlList += '<td class="item-list" style="padding-right:10px;">' + dispUid + '</td>';
    htmlList += '<td class="item-list">' + fullname + '</td>';
    htmlList += '<td class="item-list">' + localfullname + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + (item.is_admin ? 'Y' : '') + '</td>';
    htmlList += '<td class="item-list">' + item.groups + '</td>';
    htmlList += '<td class="item-list">' + item.privileges + '</td>';
    htmlList += '<td class="item-list" style="max-width:20em">' + dispDesc + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + item.flags + '</td>';

    htmlList += '<td class="item-list" style="text-align:center;width:1.5em;">';
    if (loginFailedCount > 0) {
      var clz = 'pseudo-link';
      if ((appconfig.login_failure_max > 0) && (loginFailedCount >= appconfig.login_failure_max)) {
        clz += ' text-red';
      }
      htmlList += '<span class="' + clz + '" data-tooltip="' + loginFailedTime + '" onclick="app.appmgr.confirmClearLoginFailedCount(\'' + username + '\');">' + loginFailedCount + '</span>';
    } else {
      htmlList += '';
    }
    htmlList += '</td>';

    htmlList += '<td class="item-list" style="text-align:right;">' + sessions + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + lastAccessedDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + lastLoginDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + lastLogoutDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + createdDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + updatedDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + pwChangedDate + '</td>';
    htmlList += '</tr>';
  }
  htmlList += '</table>';

  var htmlHead = app.appmgr.buildListHeader(app.appmgr.USER_LIST_COLUMNS, sortIdx, sortOrder);
  var html = htmlHead + htmlList; 

  app.appmgr.drawListContent(html);
};

app.appmgr.buildLedHtml = function(now, ts, inSec, active) {
  var COLORS = app.appmgr.LED_COLORS;
  var tMs = ts;
  if (inSec) tMs = Math.floor(tMs * 1000);
  var elapsed = now - tMs;
  var ledColor = '#888';
  if (active) {
    for (var i = 0; i < COLORS.length; i++) {
      var c = COLORS[i];
      if ((elapsed <= c.t) || (c.t == 0)) {
        ledColor = c.color;
        break;
      }
    }
  }
  var dt = app.appmgr.getDateTimeString(tMs);
  var html = '<span class="led" style="color:' + ledColor + ';" data-tooltip="' + dt + '"></span>';
  return html;
};

app.appmgr.getDateTimeString = function(ts, inSec) {
  var tMs = ts;
  if (inSec) tMs = Math.floor(tMs * 1000);
  var s = '---------- --:--:--.---';
  if (tMs > 0) {
    s = util.getDateTimeString(tMs, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss');
  }
  return s;
};

app.appmgr.getSessionList = function() {
  if (app.appmgr.tmrId > 0) {
    clearTimeout(app.appmgr.tmrId);
    app.appmgr.tmrId = 0;
    app.appmgr.interval = 1;
  }
  app.callServerApi('GetSessionInfoList', null, app.appmgr.getSessionListCb);
};
app.appmgr.getSessionListCb = function(xhr, res, req) {
  if (res.status == 'FORBIDDEN') {
    location.href = location.href;
    return;
  } else if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var data = res.body;
  var sessions = data.sessions;
  app.appmgr.sessions = sessions;
  app.currentSid = data.currentSid;
  app.appmgr.drawSessionList(sessions);

  if (app.appmgr.interval) {
    app.appmgr.interval = 0;
    app.appmgr.queueNextUpdateSessionInfo();
  }
};

app.appmgr.drawSessionList = function(sessions) {
  var now = util.now();
  var html = '<table>';
  html += '<tr style="font-weight:bold;">';
  html += '<td></td>';
  html += '<td>UID</td>';
  html += '<td>Name</td>';
  html += '<td><span style="margin-left:8px;">Session</span></td>';
  html += '<td>Last Accessed</td>';
  html += '<td style="min-width:98px;">Elapsed</td>';
  html += '<td style="font-weight:normal;">' + app.appmgr.buildTimeLineHeader(now) + '</td>';
  html += '<td>Addr</td>';
  html += '<td>User-Agent</td>';
  html += '<td>Logged in</td>';
  html += '</tr>';

  sessions = util.sortObjectList(sessions, 'lastAccessedTime', true, true);
  html += app.appmgr.buildSessionInfoHtml(sessions, now);
  html += '</table>';
  $el('#session-list').innerHTML = html;
};

app.appmgr.buildTimeLineHeader = function(now) {
  var currentInd = '<span class="blink1 text-skyblue">v</span>';

  var nowYYYYMMDD = util.getDateTimeString(now, '%YYYY%MM%DD');
  var nowHHMM = util.getDateTimeString(now, '%HH:%mm');
  var tmp = nowHHMM.split(':');
  var nowHH = tmp[0];
  var nowMM = tmp[1];

  var html = '';
  for (var i = 0; i <= 23; i++) {
    var ts = app.appmgr.getTimeSlot(i, nowHH, nowMM);
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

app.appmgr.buildSessionInfoHtml = function(sessions, now) {
  var html = '';
  if (!sessions) return html;
  var mn = util.getMidnightTimestamp(now);
  for (var i = 0; i < sessions.length; i++) {
    var session = sessions[i];
    html += app.appmgr.buildSessionInfoOne(session, now, mn);
  }
  return html;
};
app.appmgr.buildSessionInfoOne = function(session, now, mn) {
  var cSid = app.currentSid;
  var username = session.username;
  var name = session.fullName;
  var ua = session.ua;
  var loginT = session.createdTime;
  var laTime = session.lastAccessedTime;
  if (app.appmgr.INSEC) laTime = Math.floor(laTime * 1000);
  var loginTime = util.getDateTimeString(loginT, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss')
  var laTimeStr = util.getDateTimeString(laTime, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss')
  var sid = session['sid'];
  var ssid = util.snip(sid, 7, 3, '..');
  var sid7 = util.snip(sid, 7, 0, '');
  var addr = session.addr;
  var brws = util.getBrowserInfo(ua);
  var ua = brws.name + ' ' + brws.version;
  var led = app.appmgr.buildLedHtml(now, laTime, false, true);
  var ssidLink = '<span class="pseudo-link link-button" onclick="app.appmgr.confirmLogoutSession(\'' + username + '\', \'' + sid + '\');" data-tooltip="' + sid + '">' + ssid + '</span>';
  var dispSid = ((sid == cSid) ? '<span class="text-skyblue" style="cursor:default;margin-right:2px;" data-tooltip="Current Session">*</span>' : '<span style="cursor:default;margin-right:2px;">&nbsp;</span>') + ssidLink;
  var timeId = 'tm-' + sid7;
  var tmspan = '<span id="' + timeId + '"></span>'
  var timeline = app.appmgr.buildTimeLine(now, laTime);

  var html = '';
  html += '<tr class="item-list">';
  html += '<td style="padding-right:4px;">' + led + '</td>';
  html += '<td style="padding-right:10px;">' + username + '</td>';
  html += '<td style="padding-right:6px;">' + name + '</td>';
  html += '<td style="padding-right:10px;">' + dispSid + '</td>';
  html += '<td style="padding-right:10px;">' + laTimeStr + '</td>';
  html += '<td style="padding-right:10px;text-align:right;">' + tmspan + '</td>';
  html += '<td>' + timeline + '</td>';
  html += '<td style="padding-right:10px;">' + addr + '</td>';
  html += '<td style="padding-right:10px;">' + ua + '</td>';
  html += '<td style="padding-right:10px;">' + loginTime + '</td>';
  html += '</tr>';

  setTimeout(app.appmgr.startElapsedCounter, 0, {timeId: '#' + timeId, laTime: laTime});
  return html;
};
app.appmgr.startElapsedCounter = function(param) {
  util.timecounter.start(param.timeId, param.laTime);
};
app.appmgr.buildTimeLine = function(now, lastAccessedTime) {
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
      if ((accYYYYMMDD == nowYYYYMMDD) && (app.appmgr.inTheTimeSlot(i, j, accHH, accMM))) {
        s = '</span><span style="color:#0c0;">*</span>' + span;
      }
      html += s;
      if (app.appmgr.inTheTimeSlot(i, j, nowHH, nowMM)) {
        html += '<span style="opacity:0.5;">';
        f = true;
      }
    }
  }
  if (f) html += '</span>';
  html += '</span>';
  return html;
};

app.appmgr.inTheTimeSlot = function(h, qM, hh, mm) {
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
app.appmgr.getTimeSlot = function(h, hh, mm) {
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

app.appmgr.drawListContent = function(html) {
  $el('#user-list').innerHTML = html;
};

app.appmgr.sortItemList = function(sortIdx, sortOrder) {
  if (sortOrder > 2) {
    sortOrder = 0;
  }
  app.appmgr.listStatus.sortIdx = sortIdx;
  app.appmgr.listStatus.sortOrder = sortOrder;
  app.appmgr.drawList(app.appmgr.itemList, sortIdx, sortOrder);
};

app.appmgr.confirmLogoutSession = function(username, sid) {
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
  util.confirm(m, app.appmgr.logoutSession, {data: sid});
};
app.appmgr.logoutSession = function(sid) {
  var params = {
    sid: sid
  };
  app.callServerApi('logout', params, app.appmgr.logoutSessionCb);
};
app.appmgr.logoutSessionCb = function(xhr, res) {
  app.showInfotip(res.status);
  app.appmgr.reloadUserInfo();
};

//-----------------------------------------------------------------------------
app.appmgr.newUser = function() {
  app.appmgr.editUser(null);
};

app.appmgr.editUser = function(username) {
  app.appmgr.userEditMode = (username ? 'edit' : 'new');
  if (!app.appmgr.userEditWindow) {
    app.appmgr.userEditWindow = app.appmgr.openUserInfoEditorWindow(app.appmgr.userEditMode, username);
  }
  app.appmgr.clearUserInfoEditor();
  if (username) {
    var params = {
      username: username
    };
    app.callServerApi('GetUserInfo', params, app.appmgr.GetUserInfoCb);
  } else {
    $el('#username').focus();
  }
};

app.appmgr.openUserInfoEditorWindow = function(mode, username) {
  var currentUsername = app.getUsername();

  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  if (username && (username != currentUsername)) {
    html += '<div style="position:absolute;top:8px;right:8px;"><button class="button-red" onclick="app.appmgr.deleteUser(\'' + username + '\');">DEL</button></div>';
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
  html += '    <td>Flags</td>';
  html += '    <td><input type="text" id="flags" style="width:1.5em;"></td>';
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
  html += '<button onclick="app.appmgr.saveUserInfo();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="app.appmgr.userEditWindow.close();">Cancel</button>'
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
      text: ((mode == 'new') ? 'New' : 'Edit') +' User',
      style: {
        color: app.appmgr.dialogTitleFgColor,
        background: app.appmgr.dialogTitleBgColor
      }
    },
    body: {
      style: {
        color: app.appmgr.dialogFgColor,
        background: app.appmgr.dialogBgColor
      }
    },
    onclose: app.appmgr.onUserEditWindowClose,
    content: html
  };

  var win = util.newWindow(opt);
  return win;
};

app.appmgr.GetUserInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  app.appmgr.setUserInfoToEditor(info);
};

app.appmgr.setUserInfoToEditor = function(info) {
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
  $el('#flags').value = info.flags;
};

app.appmgr.clearUserInfoEditor = function() {
  var info = {
    username: '',
    fullname: '',
    localfullname: '',
    is_admin: false,
    groups: '',
    privileges: '',
    description: '',
    flags: ''
  };
  app.appmgr.setUserInfoToEditor(info);
};

app.appmgr.saveUserInfo = function() {
  if (app.appmgr.userEditMode == 'new') {
    app.appmgr.addUser();
  } else {
    app.appmgr.updateUser();
  }
};

//-----------------------------------------------------------------------------
app.appmgr.addUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var localfullname = $el('#localfullname').value;
  var isAdmin = ($el('#isadmin').checked ? '1' : '0');
  var groups = $el('#groups').value;
  var privileges = $el('#privileges').value;
  var description = $el('#description').value;
  var flags = $el('#flags').value.trim();
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = app.appmgr.cleanseUsername(username);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  username = clnsRes.val;

  clnsRes = app.appmgr.cleanseFullName(fullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  fullname = clnsRes.val;

  clnsRes = app.appmgr.cleanseFullName(localfullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  localfullname = clnsRes.val;

  clnsRes = app.appmgr.cleanseGroups(groups);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  groups = clnsRes.val;

  clnsRes = app.appmgr.cleansePrivileges(privileges);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  privileges = clnsRes.val;

  clnsRes = app.appmgr.cleansePW(pw1, pw2, 'new');
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
    flags: flags,
  };
  if (pw) {
    var salt = username;
    params.pw = app.common.getHash('SHA-256', pw, salt);
  }

  app.callServerApi('AddUser', params, app.appmgr.addUserCb);
};

app.appmgr.addUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.appmgr.userEditWindow.close();
  app.appmgr.getUserList();
};

//-----------------------------------------------------------------------------
app.appmgr.updateUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var localfullname = $el('#localfullname').value;
  var isAdmin = ($el('#isadmin').checked ? '1' : '0');
  var groups = $el('#groups').value;
  var privileges = $el('#privileges').value;
  var description = $el('#description').value;
  var flags = $el('#flags').value;
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = app.appmgr.cleansePW(pw1, pw2, 'edit');
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
    flags: flags,
  };

  if (pw) {
    var salt = username;
    params.pw = app.common.getHash('SHA-256', pw, salt);
  }

  app.callServerApi('EditUser', params, app.appmgr.updateUserCb);
};

app.appmgr.updateUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.appmgr.userEditWindow.close();
  app.appmgr.getUserList();
};

//-----------------------------------------------------------------------------
app.appmgr.deleteUser = function(username) {
  var opt = {
    data: username
  };
  util.confirm('Delete ' + username + ' ?', app.appmgr._deleteUser, opt);
};
app.appmgr._deleteUser = function(username) {
  if (!username) {
    return;
  }
  if (app.appmgr.userEditWindow) {
    app.appmgr.userEditWindow.close();
  }
  var params = {
    username: username,
  };
  app.callServerApi('DeleteUser', params, app.appmgr.deleteUserCb);
};

app.appmgr.deleteUserCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  app.appmgr.getUserList();
};

//-----------------------------------------------------------------------------
app.appmgr.confirmClearLoginFailedCount = function(username) {
  var opt = {
    data: username
  };
  util.confirm('Clear failure count for ' + username + ' ?', app.appmgr.clearLoginFailedCount, opt);
};
app.appmgr.clearLoginFailedCount = function(username) {
  if (!username) {
    return;
  }
  var params = {
    username: username
  };
  app.callServerApi('UnlockUser', params, app.appmgr.clearLoginFailedCountCb);
};

app.appmgr.clearLoginFailedCountCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  app.appmgr.getUserList();
};

//-----------------------------------------------------------------------------
app.appmgr.sortList = function(itemList, sortKey, desc) {
  var items = util.copyObject(itemList);
  var srcList = items;
  var asNum = true;
  var sortedList = util.sortObjectList(srcList, sortKey, desc, asNum);
  return sortedList;
};

//-----------------------------------------------------------------------------
app.appmgr.cleanseCommon = function(s) {
  s = s.trim();
  s = s.replace(/\t/g, ' ');
  var res = {
    val: s,
    msg: null
  };
  return res;
};

app.appmgr.cleanseUsername = function(s) {
  var res = app.appmgr.cleanseCommon(s);
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

app.appmgr.cleanseFullName = function(s) {
  var res = app.appmgr.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

app.appmgr.cleanseLocalFullName = function(s) {
  var res = app.appmgr.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

app.appmgr.cleansePW = function(pw1, pw2, mode) {
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

app.appmgr.cleanseGroups = function(s) {
  var res = app.appmgr.cleanseCommon(s);
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

app.appmgr.cleansePrivileges = function(s) {
  var res = app.appmgr.cleanseCommon(s);
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
app.appmgr.drawGroupStatus = function(s) {
  $el('#groups-status').innerHTML = s;
};

app.appmgr.getGroupList = function() {
  app.callServerApi('GetGroupInfoList', null, app.appmgr.getGroupListCb);
};
app.appmgr.getGroupListCb = function(xhr, res) {
  if (res.status == 'OK') {
    app.appmgr.drawGroupStatus('');
    var list = res.body.grouplist;
    app.appmgr.drawGroupList(list);
  }
};

app.appmgr.drawGroupList = function(list) {
  var html = '<table>';
  html += '<tr class="item-list-header">';
  html += '<th class="item-list" style="min-width:10em;">GID</th>';
  html += '<th class="item-list" style="min-width:20em;">Prvileges</th>';
  html += '<th class="item-list" style="min-width:20em;">Description</th>';
  html += '<th class="item-list">Created</th>';
  html += '<th class="item-list">Updated</th>';
  html += '</tr>';

  for (var i = 0; i < list.length; i++) {
    var group = list[i];
    var gid = group.gid;
    var privs = (group.privileges ? group.privileges : '');
    var desc = (group.description ? group.description : '');
    var createdDate = app.appmgr.getDateTimeString(group.created_date, app.appmgr.INSEC);
    var updatedDate = app.appmgr.getDateTimeString(group.updated_date, app.appmgr.INSEC);

    html += '<tr class="item-list">';
    html += '<td class="item-list"><span class="pseudo-link link-button" onclick="app.appmgr.editGroup(\'' + gid + '\');" data-tooltip="Edit">' + gid + '</span></td>';
    html += '<td class="item-list">' + privs + '</td>';
    html += '<td class="item-list">' + desc + '</td>';
    html += '<td class="item-list">' + createdDate + '</td>';
    html += '<td class="item-list">' + updatedDate + '</td>';
    html += '</tr>';
  }
  html += '</table>';
  $el('#group-list').innerHTML = html;
};

//-----------------------------------------------------------------------------
app.appmgr.newGroup = function() {
  app.appmgr.editGroup(null);
};

app.appmgr.editGroup = function(gid) {
  app.appmgr.groupEditMode = (gid ? 'edit' : 'new');
  if (!app.appmgr.groupEditWindow) {
    app.appmgr.groupEditWindow = app.appmgr.openGroupInfoEditorWindow(app.appmgr.groupEditMode, gid);
  }
  app.appmgr.clearGroupInfoEditor();
  if (gid) {
    var params = {
      gid: gid
    };
    app.callServerApi('GetGroupInfo', params, app.appmgr.getGroupInfoCb);
  } else {
    $el('#gid').focus();
  }
};

app.appmgr.openGroupInfoEditorWindow = function(mode, gid) {
  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  html += '<div style="position:absolute;top:8px;right:8px;"><button class="button-red" onclick="app.appmgr.deleteGroup(\'' + gid + '\');">DEL</button></div>';
  html += '<div style="padding:4px;position:absolute;top:0;right:0;bottom:0;left:0;margin:auto;width:360px;height:110px;text-align:left;">';

  html += '<table>';
  html += '  <tr>';
  html += '    <td>GID</td>';
  html += '    <td style="width:256px;">';
  html += '      <input type="text" id="gid" style="width:100%;">';
  html += '    </td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Privileges</td>';
  html += '    <td><input type="text" id="group-privs" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Description</td>';
  html += '    <td><input type="text" id="group-desc" style="width:100%;"></td>';
  html += '  </tr>';
  html += '<table>';

  html += '<div style="margin-top:24px;text-align:center;">';
  html += '<button onclick="app.appmgr.saveGroupInfo();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="app.appmgr.groupEditWindow.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';
  html += '</div>';

  var opt = {
    draggable: true,
    resizable: true,
    pos: 'c',
    closeButton: true,
    width: 480,
    height: 200,
    minWidth: 480,
    minHeight: 360,
    scale: 1,
    hidden: false,
    modal: false,
    title: {
      text: ((mode == 'new') ? 'New' : 'Edit') +' Group',
      style: {
        color: app.appmgr.dialogTitleFgColor,
        background: app.appmgr.dialogTitleBgColor
      }
    },
    body: {
      style: {
        color: app.appmgr.dialogFgColor,
        background: app.appmgr.dialogBgColor
      }
    },
    onclose: app.appmgr.onGroupEditWindowClose,
    content: html
  };

  var win = util.newWindow(opt);
  return win;
};

//-----------------------------------------------------------------------------
app.appmgr.addGroup = function() {
  var gid = $el('#gid').value;
  var privs = $el('#group-privs').value;
  var desc = $el('#group-desc').value;

  clnsRes = app.appmgr.cleansePrivileges(privs);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  privs = clnsRes.val;

  var params = {
    gid: gid,
    privileges: privs,
    description: desc
  };

  app.callServerApi('AddGroup', params, app.appmgr.addGroupCb);
};

app.appmgr.addGroupCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.appmgr.groupEditWindow.close();
  app.appmgr.getGroupList();
};

//-----------------------------------------------------------------------------
app.appmgr.updateGroup = function() {
  var gid = $el('#gid').value;
  var privs = $el('#group-privs').value;
  var desc = $el('#group-desc').value;

  var params = {
    gid: gid,
    privileges: privs,
    description: desc
  };

  app.callServerApi('EditGroup', params, app.appmgr.updateGroupCb);
};

app.appmgr.updateGroupCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  app.appmgr.groupEditWindow.close();
  app.appmgr.getGroupList();
};

//-----------------------------------------------------------------------------
app.appmgr.deleteGroup = function(gid) {
  var opt = {
    data: gid
  };
  util.confirm('Delete ' + gid + ' ?', app.appmgr._deleteGroup, opt);
};
app.appmgr._deleteGroup = function(gid) {
  if (!gid) {
    return;
  }
  if (app.appmgr.groupEditWindow) {
    app.appmgr.groupEditWindow.close();
  }
  var params = {
    gid: gid
  };
  app.callServerApi('DeleteGroup', params, app.appmgr.deleteGroupCb);
};

app.appmgr.deleteGroupCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  app.appmgr.getGroupList();
};

//-----------------------------------------------------------------------------
app.appmgr.getGroupInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  app.appmgr.setGroupInfoToEditor(info);
};

app.appmgr.setGroupInfoToEditor = function(info) {
  var gid = info.gid;
  $el('#gid').value = gid;
  if (gid) {
    $el('#gid').disabled = true;
    $el('#gid').addClass('edit-disabled');
  } else {
    $el('#gid').disabled = false;
    $el('#gid').removeClass('edit-disabled');
  }
  $el('#group-privs').value = info.privileges;
  $el('#group-desc').value = (info.description ? info.description : '');
};

app.appmgr.clearGroupInfoEditor = function() {
  var info = {
    gid: '',
    privileges: '',
    description: ''
  };
  app.appmgr.setGroupInfoToEditor(info);
};

app.appmgr.saveGroupInfo = function() {
  if (app.appmgr.groupEditMode == 'new') {
    app.appmgr.addGroup();
  } else {
    app.appmgr.updateGroup();
  }
};

//-----------------------------------------------------------------------------
app.appmgr.onUserEditWindowClose = function() {
  app.appmgr.userEditWindow = null;
  app.appmgr.userEditMode = null;
};

app.appmgr.onGroupEditWindowClose = function() {
  app.appmgr.groupEditWindow = null;
  app.appmgr.groupEditMode = null;
};

//-----------------------------------------------------------------------------
app.appmgr.resetApp = function() {
  util.confirm('Reset WebApp?', app.appmgr.reset);
};

app.appmgr.reset = function() {
  app.callServerApi('reset', null, app.appmgr.resetCb);
};

app.appmgr.resetCb = function(xhr, res) {
  var msg;
  if (res.status == 'OK') {
    msg = 'OK: The system has been restarted successfully.';
  } else {
    msg = 'ERROR: ' + res.body;
  }
  $el('#message').textseq(msg, {cursor: 3});
  setTimeout(app.appmgr.crearMessage, 5000)
};

app.appmgr.crearMessage = function() {
  $el('#message').fadeOut();
};

//-----------------------------------------------------------------------------
$onCtrlS = function(e) {
};

$onBeforeUnload = function(e) {
  if ((app.appmgr.userEditWindow) || (app.appmgr.groupEditWindow)) e.returnValue = '';
};
