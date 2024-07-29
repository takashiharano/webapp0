/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.appmgr = {};
var scnjs = app.appmgr;
scnjs.INSEC = false;
scnjs.dialogFgColor = '#000';
scnjs.dialogBgColor = '#fff';
scnjs.dialogTitleFgColor = '#fff';
scnjs.dialogTitleBgColor = 'linear-gradient(150deg, rgba(0,32,255,0.8),rgba(0,82,255,0.8))';

scnjs.LED_COLORS = [
  {t: 10 * util.MINUTE, color: 'led-color-green'},
  {t: 3 * util.HOUR, color: 'led-color-yellow'},
  {t: 0, color: 'led-color-red'},
];

scnjs.INTERVAL = 60000;
scnjs.USER_LIST_COLUMNS = [
  {key: 'elapsed', label: ''},
  {key: 'username', label: 'Username', style: 'min-width:min-width:10em;'},
  {key: 'fullname', label: 'Full Name', style: 'min-width:10em;'},
  {key: 'localfullname', label: 'Local Full Name', style: 'min-width:10em;'},
  {key: 'email', label: 'Email', style: 'min-width:10em;'},
  {key: 'is_admin', label: 'Admin'},
  {key: 'groups', label: 'Groups', style: 'min-width:15em;'},
  {key: 'privileges', label: 'Privileges', style: 'min-width:15em;'},
  {key: 'info1', label: 'Info1', style: 'min-width:5em;'},
  {key: 'info2', label: 'Info2', style: 'min-width:5em;'},
  {key: 'description', label: 'Description', style: 'min-width:15em;'},
  {key: 'flags', label: 'Flags'},
  {key: 'status_info.sessions', label: 'S'},
  {key: 'status_info.login_failed_count', label: 'E'},
  {key: 'status_info.last_access', label: 'Last Access'},
  {key: 'status_info.last_login', label: 'Last Login'},
  {key: 'status_info.last_logout', label: 'Last Logout'},
  {key: 'created_date', label: 'Created'},
  {key: 'updated_date', label: 'Updated'},
  {key: 'status_info.pw_changed_at', label: 'PwChanged'}
];

scnjs.listStatus = {
  sortIdx: 0,
  sortOrder: 1
};

scnjs.currentSid = null;
scnjs.userList = [];
scnjs.sessions = null;
scnjs.userEditWindow = null;
scnjs.userEditMode = null;
scnjs.groupEditWindow = null;
scnjs.groupEditMode = null;
scnjs.tmrId = 0;
scnjs.interval = 0;

$onReady = function() {
  scnjs.reload();
  scnjs.queueNextUpdateSessionInfo();
  $el('#search-text').focus();
};

scnjs.reload = function() {
  scnjs.reloadUserInfo();
  scnjs.getGroupList();
};

scnjs.reloadUserInfo = function() {
  scnjs.getUserList();
  scnjs.getSessionList();
};

scnjs.queueNextUpdateSessionInfo = function() {
  scnjs.tmrId = setTimeout(scnjs.updateSessionInfo, scnjs.INTERVAL);
};

scnjs.updateSessionInfo = function() {
  scnjs.interval = 1;
  scnjs.reloadUserInfo();
};

scnjs.getUserList = function() {
  app.callServerApi('GetUserInfoList', null, scnjs.getUserInfoListCb);
};

scnjs.getUserInfoListCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var now = util.now();
  var users = res.body.userlist;
  var userList = [];
  for (var i = 0; i < users.length; i++) {
    var user = users[i];
    var statusInfo = user.status_info;
    var lastAccessDate = statusInfo.last_access;
    var dt = scnjs.elapsedSinceLastAccess(now, lastAccessDate);
    user.elapsed = dt;
    userList.push(user);
  }
  scnjs.userList = userList;
  var listStatus = scnjs.listStatus;
  scnjs.drawUserList(userList, listStatus.sortIdx, listStatus.sortOrder);
};

scnjs.elapsedSinceLastAccess = function(now, t) {
  if (scnjs.INSEC) t = Math.floor(t * 1000);
  var dt = now - t;
  return dt;
};

scnjs.buildListHeader = function(columns, sortIdx, sortOrder) {
  var html = '<tr class="item-list-header">';
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
    sortButton += ' onclick="scnjs.sortItemList(' + i + ', ' + nextSortType + ');"';
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

scnjs.onSearchInput = function(el) {
  scnjs.filterUserList(el.value);
};

scnjs.clearSeachKey = function() {
  var elId = '#search-text';
  if ($el(elId).value) {
    $el(elId).value = '';
    scnjs.onSearchInput($el(elId));
  }
};

scnjs.filterUserList = function(filter) {
  var userList = scnjs.userList;
  var listStatus = scnjs.listStatus;
  scnjs._drawUserList(userList, listStatus.sortIdx, listStatus.sortOrder, filter);
};

scnjs.drawUserList = function(userList, sortIdx, sortOrder) {
  var filter = $el('#search-text').value;
  scnjs._drawUserList(userList, sortIdx, sortOrder, filter);
};

scnjs._drawUserList = function(items, sortIdx, sortOrder, filter) {
  var now = util.now();
  var currentUsername = app.getUsername();

  if (sortIdx >= 0) {
    if (sortOrder > 0) {
      var srtDef = scnjs.USER_LIST_COLUMNS[sortIdx];
      var isDesc = (sortOrder == 2);
      items = scnjs.sortList(items, srtDef.key, isDesc);
    }
  }

  var filterCaseSensitive = false;

  var count = 0;
  var htmlList = '';
  for (var i = 0; i < items.length; i++) {
    var item = items[i];
    if (!scnjs.filterUserByKeyword(item, filter, filterCaseSensitive)) continue;
    count++;
    var uid = item.username;
    var fullname = item.fullname;
    var localfullname = item.localfullname;
    var email = item.email;
    var groups = item.groups;
    var privs = item.privileges;
    var statusInfo = item.status_info;
    var loginFailedCount = statusInfo.login_failed_count;
    var loginFailedTime = util.getDateTimeString(statusInfo.login_failed_time);
    var sessions = statusInfo.sessions;
    var lastAccessDate = scnjs.getDateTimeString(statusInfo.last_access, scnjs.INSEC);
    var lastLoginDate = scnjs.getDateTimeString(statusInfo.last_login, scnjs.INSEC);
    var lastLogoutDate = scnjs.getDateTimeString(statusInfo.last_logout, scnjs.INSEC);
    var createdDate = scnjs.getDateTimeString(item.created_date, scnjs.INSEC);
    var updatedDate = scnjs.getDateTimeString(item.updated_date, scnjs.INSEC);
    var pwChangedDate = scnjs.getDateTimeString(statusInfo.pw_changed_at, scnjs.INSEC);
    var info1 = item.info1;
    var info2 = item.info2;
    var desc = (item.description ? item.description : '');
    var escDesc = util.escHtml(desc);
    var dispDesc = '<span style="display:inline-block;width:100%;overflow:hidden;text-overflow:ellipsis;"';
    if (util.lenW(desc) > 15) {
      dispDesc += ' data-tooltip="' + escDesc + '"';
    }
    dispDesc += '>' + escDesc + '</span>';

    var active = (sessions > 0);
    var led = scnjs.buildLedHtml(now, statusInfo.last_access, scnjs.INSEC, active);
    var cInd = ((uid == currentUsername) ? '<span class="text-skyblue" style="cursor:default;margin-right:2px;" data-tooltip2="You">*</span>' : '<span style="margin-right:2px;">&nbsp;</span>');

    var dispUid = uid;
    var dispFullname = fullname;
    var dispLocalFullname = localfullname;
    var dispEmail = email;
    var dispGroups = groups;
    var dispPrivs = privs;
    var dispInfo1 = info1;
    var dispInfo2 = info2;

    if (filter) {
      dispUid = scnjs.highlightKeyword(uid, filter, filterCaseSensitive);
      dispFullname = scnjs.highlightKeyword(fullname, filter, filterCaseSensitive);
      dispLocalFullname = scnjs.highlightKeyword(localfullname, filter, filterCaseSensitive);
      dispEmail = scnjs.highlightKeyword(email, filter, filterCaseSensitive);
      dispGroups = scnjs.highlightKeyword(groups, filter, filterCaseSensitive);
      dispPrivs = scnjs.highlightKeyword(privs, filter, filterCaseSensitive);
      dispInfo1 = scnjs.highlightKeyword(info1, filter, filterCaseSensitive);
      dispInfo2 = scnjs.highlightKeyword(info2, filter, filterCaseSensitive);
    }

    dispUid = cInd + '<span class="pseudo-link link-button" onclick="scnjs.editUser(\'' + uid + '\');" data-tooltip2="Edit">' + dispUid + '</span>';
    dispFullname = scnjs.buildCopyableLabel(fullname, dispFullname);
    dispLocalFullname = scnjs.buildCopyableLabel(localfullname, dispLocalFullname);
    dispEmail = scnjs.buildCopyableLabel(email, dispEmail);
    dispInfo1 = scnjs.buildCopyableLabel(info1, dispInfo1);
    dispInfo2 = scnjs.buildCopyableLabel(info2, dispInfo2);

    var failedCount = '<td class="item-list" style="text-align:right;width:1.5em;">';
    if (loginFailedCount > 0) {
      var clz = 'pseudo-link';
      if ((appconfig.login_failure_max > 0) && (loginFailedCount >= appconfig.login_failure_max)) {
        clz += ' login-locked';
      } else {
        clz += ' text-red';
      }
      failedCount += '<span class="' + clz + '" data-tooltip="Last failed: ' + loginFailedTime + '" onclick="scnjs.confirmClearLoginFailedCount(\'' + uid + '\');">' + loginFailedCount + '</span>';
    } else {
      failedCount += '';
    }
    failedCount += '</td>';

    var clz = ((i % 2 == 0) ? 'row-odd' : 'row-even');

    htmlList += '<tr class="item-list ' + clz + '">';
    htmlList += '<td class="item-list" style="text-align:center;">' + led + '</td>';
    htmlList += '<td class="item-list" style="padding-right:10px;">' + dispUid + '</td>';
    htmlList += '<td class="item-list">' + dispFullname + '</td>';
    htmlList += '<td class="item-list">' + dispLocalFullname + '</td>';
    htmlList += '<td class="item-list">' + dispEmail + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + (item.is_admin ? 'Y' : '') + '</td>';
    htmlList += '<td class="item-list">' + dispGroups + '</td>';
    htmlList += '<td class="item-list">' + dispPrivs + '</td>';
    htmlList += '<td class="item-list">' + dispInfo1 + '</td>';
    htmlList += '<td class="item-list">' + dispInfo2 + '</td>';
    htmlList += '<td class="item-list" style="max-width:15em;">' + dispDesc + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + item.flags + '</td>';
    htmlList += '<td class="item-list" style="text-align:right;">' + sessions + '</td>';
    htmlList += failedCount;
    htmlList += '<td class="item-list" style="text-align:center;">' + lastAccessDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + lastLoginDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + lastLogoutDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + createdDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + updatedDate + '</td>';
    htmlList += '<td class="item-list" style="text-align:center;">' + pwChangedDate + '</td>';
    htmlList += '</tr>';
  }

  var listInfo = 'count = ' + count;
  if (count == 0) {
    listInfo = '<div style="margin-top:8px;margin-bottom:40px;">- DATA NOT FOUND -</div>';
  }

  var htmlHead = scnjs.buildListHeader(scnjs.USER_LIST_COLUMNS, sortIdx, sortOrder);
  var html = '<table>' + htmlHead + htmlList + '</table>';
  html += '<div style="margin-bottom:4px;" class="list-info">' + listInfo + '</div>';

  $el('#user-list').innerHTML = html;
};

scnjs.highlightKeyword = function(v, filter, fltCase) {
  if (!fltCase) filter = filter.toLowerCase();
  try {
    var pos = (fltCase ? v.indexOf(filter) : v.toLowerCase().indexOf(filter));
    if (pos != -1) {
      var key = v.slice(pos, pos + filter.length);
      var hl = '<span class="search-highlight">' + key + '</span>';
      v = v.replace(key, hl, 'ig');
    }
  } catch (e) {}
  return v;
}

scnjs.filterUserByKeyword = function(item, key, fltCase) {
  if (!key) return true;
  var targets = [];
  targets.push(item.uid);
  targets.push(item.name);
  targets.push(item.local_name);
  targets.push(item.email);
  targets.push(item.groups);
  targets.push(item.privs);
  targets.push(item.info1);
  targets.push(item.info2);
  return scnjs.filterByKeyword(targets, key, fltCase);
};
scnjs.filterByKeyword = function(targets, key, fltCase) {
  var flg = (fltCase ? 'g' : 'gi');
  for (var i = 0; i < targets.length; i++) {
    var v = targets[i];
    try {
      var re = new RegExp(key, flg);
      var r = re.exec(v);
      if (r != null) return true;
    } catch (e) {}
  }
  return false;
};

scnjs.buildCopyableLabel = function(v, s) {
  if (!s) s = v;
  var v = v.replace(/\\/g, '\\\\').replace(/'/g, '\\\'').replace(/"/g, '&quot;');
  var label = s;
  var r = '<pre class="pseudo-link" onclick="scnjs.copy(\'' + v + '\');" data-tooltip2="Click to copy">' + label + '</pre>';
  return r;
};

scnjs.buildLedHtml = function(now, ts, inSec, active) {
  var COLORS = scnjs.LED_COLORS;
  var tMs = ts;
  if (inSec) tMs = Math.floor(tMs * 1000);
  var elapsed = now - tMs;
  var ledColor = 'led-color-gray';
  if (active) {
    for (var i = 0; i < COLORS.length; i++) {
      var c = COLORS[i];
      if ((elapsed <= c.t) || (c.t == 0)) {
        ledColor = c.color;
        break;
      }
    }
  }
  var dt = scnjs.getDateTimeString(tMs);
  var html = '<span class="led ' + ledColor + '" data-tooltip="Last access: ' + dt + '"></span>';
  return html;
};

scnjs.getDateTimeString = function(ts, inSec) {
  var tMs = ts;
  if (inSec) tMs = Math.floor(tMs * 1000);
  var s = '---------- --:--:--.---';
  if (tMs > 0) {
    s = util.getDateTimeString(tMs, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss');
  }
  return s;
};

scnjs.getSessionList = function() {
  if (scnjs.tmrId > 0) {
    clearTimeout(scnjs.tmrId);
    scnjs.tmrId = 0;
    scnjs.interval = 1;
  }
  var param = {logs: '1'};
  app.callServerApi('GetSessionInfoList', param, scnjs.getSessionListCb);
};
scnjs.getSessionListCb = function(xhr, res, req) {
  if (res.status == 'FORBIDDEN') {
    location.href = location.href;
    return;
  } else if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var data = res.body;
  var sessions = data.sessions;
  scnjs.sessions = sessions;
  app.currentSid = data.currentSid;
  scnjs.drawSessionList(sessions);

  if (scnjs.interval) {
    scnjs.interval = 0;
    scnjs.queueNextUpdateSessionInfo();
  }
};

scnjs.drawSessionList = function(sessions) {
  var now = util.now();
  var html = '<table>';
  html += '<tr style="font-weight:bold;">';
  html += '<td></td>';
  html += '<td>UID</td>';
  html += '<td>Name</td>';
  html += '<td><span style="margin-left:8px;">Session</span></td>';
  html += '<td>Last Access</td>';
  html += '<td style="min-width:98px;">Elapsed</td>';
  html += '<td style="font-weight:normal;">' + scnjs.buildTimeLineHeader(now) + '</td>';
  html += '<td>Addr</td>';
  html += '<td>User-Agent</td>';
  html += '<td>Logged in</td>';
  html += '</tr>';

  sessions = util.sortObjectList(sessions, 'lastAccessTime', true, true);
  html += scnjs.buildSessionInfoHtml(sessions, now);
  html += '</table>';
  $el('#session-list').innerHTML = html;
};

scnjs.buildTimeLineHeader = function(now) {
  var currentInd = '<span class="blink1 text-skyblue">v</span>';

  var nowYYYYMMDD = util.getDateTimeString(now, '%YYYY%MM%DD');
  var nowHHMM = util.getDateTimeString(now, '%HH:%mm');
  var tmp = nowHHMM.split(':');
  var nowHH = tmp[0];
  var nowMM = tmp[1];

  var html = '';
  for (var i = 0; i <= 23; i++) {
    var ts = scnjs.getTimeSlot(i, nowHH, nowMM);
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

scnjs.buildSessionInfoHtml = function(sessions, now) {
  var html = '';
  if (!sessions) return html;
  var mn = util.getMidnightTimestamp(now);
  for (var i = 0; i < sessions.length; i++) {
    var session = sessions[i];
    html += scnjs.buildSessionInfoOne(session, now, mn);
  }
  return html;
};
scnjs.buildSessionInfoOne = function(session, now, mn) {
  var cSid = app.currentSid;
  var username = session.username;
  var name = session.fullName;
  var ua = session.ua;
  var loginT = session.createdTime;
  var laTime = session.lastAccessTime;
  if (scnjs.INSEC) laTime = Math.floor(laTime * 1000);
  var loginTime = util.getDateTimeString(loginT, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss');
  var laTimeStr = util.getDateTimeString(laTime, '%YYYY-%MM-%DD %HH:%mm:%SS.%sss');
  var sid = session['sid'];
  var ssid = util.snip(sid, 7, 3, '..');
  var sid7 = util.snip(sid, 7, 0, '');
  var addr = session.addr;
  var brws = util.getBrowserInfo(ua);
  var ua = brws.name + ' ' + brws.version;
  var led = scnjs.buildLedHtml(now, laTime, false, true);
  var ssidLink = '<span class="pseudo-link link-button" onclick="scnjs.confirmLogoutSession(\'' + username + '\', \'' + sid + '\');" data-tooltip="' + sid + '">' + ssid + '</span>';
  var dispSid = ((sid == cSid) ? '<span class="text-skyblue" style="cursor:default;margin-right:2px;" data-tooltip2="Current Session">*</span>' : '<span style="cursor:default;margin-right:2px;">&nbsp;</span>') + ssidLink;
  var timeId = 'tm-' + sid7;
  var tmspan = '<span id="' + timeId + '"></span>';

  var slotTimestampHistories = session['timeline_log'];
  var timeline = scnjs.buildTimeLine(now, laTime, slotTimestampHistories);

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

  setTimeout(scnjs.startElapsedCounter, 0, {timeId: '#' + timeId, laTime: laTime});
  return html;
};
scnjs.startElapsedCounter = function(param) {
  var o = {zero: true};
  util.timecounter.start(param.timeId, param.laTime, o);
};

scnjs.buildTimeLine = function(now, lastAccessTime, slotTimestampHistories) {
  var accYearDateTime = util.getDateTimeString(lastAccessTime, '%YYYY-%MM-%DD %HH:%mm');
  var accDateTime = util.getDateTimeString(lastAccessTime, '%W %DD %MMM %HH:%mm');
  var accTime = util.getDateTimeString(lastAccessTime, '%HH:%mm');
  var accTp = scnjs.getTimePosition(now, lastAccessTime);
  var nowTp = scnjs.getTimePosition(now, now);
  var hrBlk = 5;
  var ttlPs = hrBlk * 24;
  var dispAccDateTime = ' ' + accDateTime + ' ';
  var dispAccTime = ' ' + accTime + ' ';
  var remains = ttlPs - (accTp + dispAccTime.length);

  var tsPosList = scnjs.getPosList4History(now, slotTimestampHistories);

  var html = '<span class="timeline-span">';
  var s;
  var f = false;
  for (var i = 0; i <= ttlPs; i++) {
    if (!f && (i > nowTp)) {
      html += '<span class="timeline-forward">';
      f = true;
    }

    if ((i == 0) && (accTp == -1)) {
      s = '<span class="timeline-acc-ind-out" data-tooltip="' + accYearDateTime + '">&lt;</span>';
      s += '<span class="timeline-acc-ind-time">' + dispAccDateTime + '</san>';
      html += s;
      i += dispAccDateTime.length;
      continue;
    } else if (i % hrBlk == 0) {
      html += '|';
      continue;
    }

    s = '';
    if (i == accTp) {
      s += '<span class="timeline-acc-ind" data-tooltip="' + accTime + '">*</span>';
      s += '<span class="timeline-acc-ind-time">' + dispAccTime + '</span>';
      i += dispAccTime.length;
    } else {
      s += scnjs.getTimeslotInd(tsPosList, i);
    }
    html += s;
  }

  if (f) html += '</span>';
  html += '</span>';
  return html;
};

scnjs.getTimeslotInd = function(tsPosList, pos) {
  var s = '-';
  for (var i = 0; i < tsPosList.length; i++) {
    var tsPos = tsPosList[i];
    var p = tsPos.p;
    if (p == pos) {
      var t = tsPos.t;
      var tt = util.getDateTimeString(t, '%HH:%mm');
      s = '<span class="timeline-acc-ind timeline-acc-ind-past" data-tooltip="' + tt + '">*</span>';
      break;
    }
  }
  return s;
};

scnjs.getPosList4History = function(now, slotTimestampHistories) {
  var posList = [];
  for (var i = 0; i < slotTimestampHistories.length; i++) {
    var t = slotTimestampHistories[i];
    if (scnjs.INSEC) t *= 1000;
    var p = scnjs.getTimePosition(now, t);
    if (p >= 0) {
      posList.push({p: p, t: t});
    }
  }
  return posList;
};

scnjs.getTimePosition = function(now, timestamp) {
  var nowYYYYMMDD = util.getDateTimeString(now, '%YYYY%MM%DD');
  var accYYYYMMDD = util.getDateTimeString(timestamp, '%YYYY%MM%DD');
  var accHHMM = util.getDateTimeString(timestamp, '%HH:%mm');
  var wk = accHHMM.split(':');
  var accHH = wk[0];
  var accMM = wk[1];
  var p = 0;
  for (var i = 0; i <= 23; i++) {
    p++;
    for (var j = 0; j < 4; j++) {
      if ((accYYYYMMDD == nowYYYYMMDD) && (scnjs.inTheTimeSlot(i, j, accHH, accMM))) {
        return p;
      }
      p++;
    }
  }
  return -1;
};

scnjs.inTheTimeSlot = function(h, qM, hh, mm) {
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
scnjs.getTimeSlot = function(h, hh, mm) {
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

scnjs.sortItemList = function(sortIdx, sortOrder) {
  if (sortOrder > 2) {
    sortOrder = 0;
  }
  scnjs.listStatus.sortIdx = sortIdx;
  scnjs.listStatus.sortOrder = sortOrder;
  scnjs.drawUserList(scnjs.userList, sortIdx, sortOrder);
};

scnjs.confirmLogoutSession = function(username, sid) {
  var cSid = app.currentSid;
  var ssid = util.snip(sid, 7, 7, '..');
  var currentUsername = app.getUsername();
  var m = 'Logout?\n\n';
  if (sid == cSid) {
    m += '<span class="warn-red" style="font-weight:bold;">[CURRENT SESSION]</span>\n';
  }
  m += '<div style="text-align:left;">';
  m += username;
  if (username == currentUsername) m += ' <span class="you">(You)</span>';
  m += '\n';
  m += 'sid: ' + sid;
  m += '</div>';
  util.confirm(m, scnjs.logoutSession, {data: sid});
};
scnjs.logoutSession = function(sid) {
  var params = {
    sid: sid
  };
  app.callServerApi('logout', params, scnjs.logoutSessionCb);
};
scnjs.logoutSessionCb = function(xhr, res) {
  app.showInfotip(res.status);
  scnjs.reloadUserInfo();
};

//-----------------------------------------------------------------------------
scnjs.newUser = function() {
  scnjs.editUser(null);
};

scnjs.editUser = function(username) {
  var mode = (username ? 'edit' : 'new');
  scnjs.userEditMode = mode;
  if (!scnjs.userEditWindow) {
    scnjs.userEditWindow = scnjs.openUserInfoEditorWindow(mode, username);
  }
  scnjs.clearUserInfoEditor();
  if (mode == 'edit') {
    var params = {
      username: username
    };
    app.callServerApi('GetUserInfo', params, scnjs.GetUserInfoCb);
  } else {
    $el('#flags').value = '1';
    $el('#username').focus();
  }
};

scnjs.openUserInfoEditorWindow = function(mode, username) {
  var currentUsername = app.getUsername();

  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  if (username && (username != currentUsername)) {
    html += '<div style="position:absolute;top:8px;right:8px;"><button class="button-red" onclick="scnjs.deleteUser(\'' + username + '\');">DEL</button></div>';
  }
  html += '<div style="padding:4px;position:absolute;top:0;right:0;bottom:0;left:0;margin:auto;width:400px;height:400px;text-align:left;">';

  html += '<table class="edit-table">';
  html += '  <tr>';
  html += '    <td>Username</td>';
  html += '    <td style="width:256px;"><input type="text" id="username" style="width:100%;" onblur="scnjs.onUsernameBlur();"></td>';
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
  html += '    <td>Email</td>';
  html += '    <td><input type="text" id="email" style="width:100%;"></td>';
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
  html += '    <td>Info1</td>';
  html += '    <td><input type="text" id="info1" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Info2</td>';
  html += '    <td><input type="text" id="info2" style="width:100%;"></td>';
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
  html += '</table>';

  html += '<div style="margin-top:40px;text-align:center;">';
  html += '<button onclick="scnjs.saveUserInfo();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="scnjs.userEditWindow.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';
  html += '</div>';

  var opt = {
    draggable: true,
    resizable: true,
    pos: 'c',
    closeButton: true,
    width: 500,
    height: 540,
    minWidth: 500,
    minHeight: 540,
    scale: 1,
    hidden: false,
    modal: false,
    title: {
      text: ((mode == 'new') ? 'New' : 'Edit') +' User',
      style: {
        color: scnjs.dialogTitleFgColor,
        background: scnjs.dialogTitleBgColor
      }
    },
    body: {
      style: {
        color: scnjs.dialogFgColor,
        background: scnjs.dialogBgColor
      }
    },
    onclose: scnjs.onUserEditWindowClose,
    content: html
  };

  var win = util.newWindow(opt);
  return win;
};

scnjs.onUsernameBlur = function() {
  var fullname = $el('#fullname').value;
  if (fullname) return;
  var username = $el('#username').value;
  if (username.match()) {
    fullname = scnjs.mail2name(username);
  }
  $el('#fullname').value = fullname;
};

scnjs.mail2name = function(m) {
  var a = m.split('@');
  a = a[0].split('.');
  if (a.length == 1) return a[0];
  var s = '';
  for (var i = 0; i < a.length; i++) {
    if (i > 0) {
      s += ' ';
    }
    s += util.capitalize(a[i]);
  }
  return s;
};

scnjs.GetUserInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  scnjs.setUserInfoToEditor(info);
};

scnjs.setUserInfoToEditor = function(info) {
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
  $el('#email').value = info.email;
  $el('#isadmin').checked = info.is_admin;
  $el('#groups').value = info.groups;
  $el('#privileges').value = info.privileges;
  $el('#info1').value = info.info1;
  $el('#info2').value = info.info2;
  $el('#description').value = info.description;
  $el('#flags').value = info.flags;
};

scnjs.clearUserInfoEditor = function() {
  var info = {
    username: '',
    fullname: '',
    localfullname: '',
    email: '',
    is_admin: false,
    groups: '',
    privileges: '',
    info1: '',
    info2: '',
    description: '',
    flags: ''
  };
  scnjs.setUserInfoToEditor(info);
};

scnjs.saveUserInfo = function() {
  if (scnjs.userEditMode == 'new') {
    scnjs.addUser();
  } else {
    scnjs.updateUser();
  }
};

//-----------------------------------------------------------------------------
scnjs.addUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var localfullname = $el('#localfullname').value;
  var email = $el('#email').value;
  var isAdmin = ($el('#isadmin').checked ? '1' : '0');
  var groups = $el('#groups').value;
  var privileges = $el('#privileges').value;
  var info1 = $el('#info1').value;
  var info2 = $el('#info2').value;
  var description = $el('#description').value;
  var flags = $el('#flags').value.trim();
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = scnjs.cleanseUsername(username);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  username = clnsRes.val;

  clnsRes = scnjs.cleanseFullName(fullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  fullname = clnsRes.val;

  clnsRes = scnjs.cleanseFullName(localfullname);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  localfullname = clnsRes.val;

  clnsRes = scnjs.cleanseGroups(groups);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  groups = clnsRes.val;

  clnsRes = scnjs.cleansePrivileges(privileges);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  privileges = clnsRes.val;

  clnsRes = scnjs.cleansePW(pw1, pw2, 'new');
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  var pw = clnsRes.val;
  pw = scnjs.getUserPwHash(username, pw);

  var params = {
    username: username,
    fullname: fullname,
    localfullname: localfullname,
    email: email,
    is_admin: isAdmin,
    groups: groups,
    privileges: privileges,
    info1: info1,
    info2: info2,
    description: description,
    flags: flags,
    pw: pw
  };

  app.callServerApi('AddUser', params, scnjs.addUserCb);
};

scnjs.addUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  scnjs.userEditWindow.close();
  scnjs.getUserList();
};

//-----------------------------------------------------------------------------
scnjs.updateUser = function() {
  var username = $el('#username').value;
  var fullname = $el('#fullname').value;
  var localfullname = $el('#localfullname').value;
  var email = $el('#email').value;
  var isAdmin = ($el('#isadmin').checked ? '1' : '0');
  var groups = $el('#groups').value;
  var privileges = $el('#privileges').value;
  var info1 = $el('#info1').value;
  var info2 = $el('#info2').value;
  var description = $el('#description').value;
  var flags = $el('#flags').value;
  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;

  var clnsRes = scnjs.cleansePW(pw1, pw2, 'edit');
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  var pw = clnsRes.val;

  var params = {
    username: username,
    fullname: fullname,
    localfullname: localfullname,
    email: email,
    is_admin: isAdmin,
    groups: groups,
    privileges: privileges,
    info1: info1,
    info2: info2,
    description : description,
    flags: flags,
  };

  if (pw) {
    params.pw = scnjs.getUserPwHash(username, pw);
  }

  app.callServerApi('EditUser', params, scnjs.updateUserCb);
};

scnjs.updateUserCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  scnjs.userEditWindow.close();
  scnjs.getUserList();
};

//-----------------------------------------------------------------------------
scnjs.deleteUser = function(username) {
  var opt = {
    data: username
  };
  util.confirm('Delete ' + username + ' ?', scnjs._deleteUser, opt);
};
scnjs._deleteUser = function(username) {
  if (!username) {
    return;
  }
  if (scnjs.userEditWindow) {
    scnjs.userEditWindow.close();
  }
  var params = {
    username: username,
  };
  app.callServerApi('DeleteUser', params, scnjs.deleteUserCb);
};

scnjs.deleteUserCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  scnjs.getUserList();
};

//-----------------------------------------------------------------------------
scnjs.confirmClearLoginFailedCount = function(username) {
  var opt = {
    data: username
  };
  util.confirm('Clear failure count for ' + username + ' ?', scnjs.clearLoginFailedCount, opt);
};
scnjs.clearLoginFailedCount = function(username) {
  if (!username) {
    return;
  }
  var params = {
    username: username
  };
  app.callServerApi('UnlockUser', params, scnjs.clearLoginFailedCountCb);
};

scnjs.clearLoginFailedCountCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  scnjs.getUserList();
};

//-----------------------------------------------------------------------------
scnjs.sortList = function(itemList, sortKey, isDesc) {
  var items = util.copyObject(itemList);
  var srcList = items;
  var asNum = true;
  var sortedList = util.sortObjectList(srcList, sortKey, isDesc, asNum);
  return sortedList;
};

//-----------------------------------------------------------------------------
scnjs.cleanseCommon = function(s) {
  s = s.trim();
  s = s.replace(/\t/g, ' ');
  var res = {
    val: s,
    msg: null
  };
  return res;
};

scnjs.cleanseUsername = function(s) {
  var res = scnjs.cleanseCommon(s);
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

scnjs.cleanseFullName = function(s) {
  var res = scnjs.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

scnjs.cleanseLocalFullName = function(s) {
  var res = scnjs.cleanseCommon(s);
  if (res.msg) {
    return res;
  }
  var msg = null;
  s = res.val;
  res.val = s;
  res.msg = msg;
  return res;
};

scnjs.cleansePW = function(pw1, pw2, mode) {
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

scnjs.cleanseGroups = function(s) {
  var res = scnjs.cleanseCommon(s);
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

scnjs.cleansePrivileges = function(s) {
  var res = scnjs.cleanseCommon(s);
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
scnjs.drawGroupStatus = function(s) {
  $el('#groups-status').innerHTML = s;
};

scnjs.getGroupList = function() {
  app.callServerApi('GetGroupInfoList', null, scnjs.getGroupListCb);
};
scnjs.getGroupListCb = function(xhr, res) {
  if (res.status == 'OK') {
    scnjs.drawGroupStatus('');
    var list = res.body.grouplist;
    scnjs.drawGroupList(list);
  }
};

scnjs.drawGroupList = function(list) {
  var html = '<table>';
  html += '<tr class="item-list-header">';
  html += '<th class="item-list" style="min-width:10em;">GID</th>';
  html += '<th class="item-list" style="min-width:15em;">Name</th>';
  html += '<th class="item-list" style="min-width:20em;">Prvileges</th>';
  html += '<th class="item-list" style="min-width:20em;">Description</th>';
  html += '<th class="item-list">Created</th>';
  html += '<th class="item-list">Updated</th>';
  html += '</tr>';

  for (var i = 0; i < list.length; i++) {
    var group = list[i];
    var gid = group.gid;
    var name = group.name;
    var privs = (group.privileges ? group.privileges : '');
    var desc = (group.description ? group.description : '');
    var createdDate = scnjs.getDateTimeString(group.created_date, scnjs.INSEC);
    var updatedDate = scnjs.getDateTimeString(group.updated_date, scnjs.INSEC);

    var clz = ((i % 2 == 0) ? 'row-odd' : 'row-even');

    html += '<tr class="item-list ' + clz + '">';
    html += '<td class="item-list"><span class="pseudo-link link-button" onclick="scnjs.editGroup(\'' + gid + '\');" data-tooltip2="Edit">' + gid + '</span></td>';
    html += '<td class="item-list">' + name + '</td>';
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
scnjs.newGroup = function() {
  scnjs.editGroup(null);
};

scnjs.editGroup = function(gid) {
  scnjs.groupEditMode = (gid ? 'edit' : 'new');
  if (!scnjs.groupEditWindow) {
    scnjs.groupEditWindow = scnjs.openGroupInfoEditorWindow(scnjs.groupEditMode, gid);
  }
  scnjs.clearGroupInfoEditor();
  if (gid) {
    var params = {
      gid: gid
    };
    app.callServerApi('GetGroupInfo', params, scnjs.getGroupInfoCb);
  } else {
    $el('#gid').focus();
  }
};

scnjs.openGroupInfoEditorWindow = function(mode, gid) {
  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';
  html += '<div style="position:absolute;top:8px;right:8px;"><button class="button-red" onclick="scnjs.deleteGroup(\'' + gid + '\');">DEL</button></div>';
  html += '<div style="padding:4px;position:absolute;top:0;right:0;bottom:0;left:0;margin:auto;width:360px;height:120px;text-align:left;">';

  html += '<table>';
  html += '  <tr>';
  html += '    <td>GID</td>';
  html += '    <td style="width:256px;">';
  html += '      <input type="text" id="gid" style="width:100%;">';
  html += '    </td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Name</td>';
  html += '    <td><input type="text" id="group-name" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Privileges</td>';
  html += '    <td><input type="text" id="group-privs" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Description</td>';
  html += '    <td><input type="text" id="group-desc" style="width:100%;"></td>';
  html += '  </tr>';
  html += '</table>';

  html += '<div style="margin-top:24px;text-align:center;">';
  html += '<button onclick="scnjs.saveGroupInfo();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="scnjs.groupEditWindow.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';
  html += '</div>';

  var opt = {
    draggable: true,
    resizable: true,
    pos: 'c',
    closeButton: true,
    width: 480,
    height: 240,
    minWidth: 480,
    minHeight: 240,
    scale: 1,
    hidden: false,
    modal: false,
    title: {
      text: ((mode == 'new') ? 'New' : 'Edit') +' Group',
      style: {
        color: scnjs.dialogTitleFgColor,
        background: scnjs.dialogTitleBgColor
      }
    },
    body: {
      style: {
        color: scnjs.dialogFgColor,
        background: scnjs.dialogBgColor
      }
    },
    onclose: scnjs.onGroupEditWindowClose,
    content: html
  };

  var win = util.newWindow(opt);
  return win;
};

//-----------------------------------------------------------------------------
scnjs.addGroup = function() {
  var gid = $el('#gid').value.trim();
  var name = $el('#group-name').value;
  var privs = $el('#group-privs').value;
  var desc = $el('#group-desc').value;

  if (!gid) {
    app.showInfotip('Group ID is required.', 2000);
    return;
  }

  clnsRes = scnjs.cleansePrivileges(privs);
  if (clnsRes.msg) {
    app.showInfotip(clnsRes.msg, 2000);
    return;
  }
  privs = clnsRes.val;

  var params = {
    gid: gid,
    name: name,
    privileges: privs,
    description: desc
  };

  app.callServerApi('AddGroup', params, scnjs.addGroupCb);
};

scnjs.addGroupCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  scnjs.groupEditWindow.close();
  scnjs.getGroupList();
};

//-----------------------------------------------------------------------------
scnjs.updateGroup = function() {
  var gid = $el('#gid').value;
  var name = $el('#group-name').value;
  var privs = $el('#group-privs').value;
  var desc = $el('#group-desc').value;

  var params = {
    gid: gid,
    name: name,
    privileges: privs,
    description: desc
  };

  app.callServerApi('EditGroup', params, scnjs.updateGroupCb);
};

scnjs.updateGroupCb = function(xhr, res) {
  app.showInfotip(res.status);
  if (res.status != 'OK') {
    return;
  }
  scnjs.groupEditWindow.close();
  scnjs.getGroupList();
};

//-----------------------------------------------------------------------------
scnjs.deleteGroup = function(gid) {
  var opt = {
    data: gid
  };
  util.confirm('Delete ' + gid + ' ?', scnjs._deleteGroup, opt);
};
scnjs._deleteGroup = function(gid) {
  if (!gid) {
    return;
  }
  if (scnjs.groupEditWindow) {
    scnjs.groupEditWindow.close();
  }
  var params = {
    gid: gid
  };
  app.callServerApi('DeleteGroup', params, scnjs.deleteGroupCb);
};

scnjs.deleteGroupCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  app.showInfotip('OK');
  scnjs.getGroupList();
};

//-----------------------------------------------------------------------------
scnjs.getGroupInfoCb = function(xhr, res) {
  if (res.status != 'OK') {
    app.showInfotip(res.status);
    return;
  }
  var info = res.body;
  scnjs.setGroupInfoToEditor(info);
};

scnjs.setGroupInfoToEditor = function(info) {
  var gid = info.gid;
  $el('#gid').value = gid;
  if (gid) {
    $el('#gid').disabled = true;
    $el('#gid').addClass('edit-disabled');
  } else {
    $el('#gid').disabled = false;
    $el('#gid').removeClass('edit-disabled');
  }
  $el('#group-name').value = info.name;
  $el('#group-privs').value = info.privileges;
  $el('#group-desc').value = (info.description ? info.description : '');
};

scnjs.clearGroupInfoEditor = function() {
  var info = {
    gid: '',
    name: '',
    privileges: '',
    description: ''
  };
  scnjs.setGroupInfoToEditor(info);
};

scnjs.saveGroupInfo = function() {
  if (scnjs.groupEditMode == 'new') {
    scnjs.addGroup();
  } else {
    scnjs.updateGroup();
  }
};

//-----------------------------------------------------------------------------
scnjs.onUserEditWindowClose = function() {
  scnjs.userEditWindow = null;
  scnjs.userEditMode = null;
};

scnjs.onGroupEditWindowClose = function() {
  scnjs.groupEditWindow = null;
  scnjs.groupEditMode = null;
};

//-----------------------------------------------------------------------------
scnjs.resetApp = function() {
  util.confirm('Reset WebApp?', scnjs.reset);
};

scnjs.reset = function() {
  app.callServerApi('reset', null, scnjs.resetCb);
};

scnjs.resetCb = function(xhr, res) {
  var msg;
  if (res.status == 'OK') {
    msg = 'OK: The system has been restarted successfully.';
  } else {
    msg = 'ERROR: ' + res.body;
  }
  $el('#message').textseq(msg, {cursor: 3});
  setTimeout(scnjs.crearMessage, 5000)
};

scnjs.crearMessage = function() {
  $el('#message').fadeOut();
};

scnjs.copy = function(s) {
  util.copy(s);
  var o = {pos: 'pointer'};
  scnjs.showInfotip('Copied', 1000, o);
};

scnjs.showInfotip = function(m, d, o) {
  if (!o) o = {};
  o.style = {
    'font-size': '14px'
  };
  util.infotip.show(m, d, o);
};

scnjs.getUserPwHash = function(uid, pw) {
  return app.common.getHash('SHA-256', pw, uid);
};

$onEscKey = function(e) {
  if ($el('#search-text').hasFocus()) {
    scnjs.clearSeachKey();
  }
};

$onCtrlS = function(e) {
};

$onBeforeUnload = function(e) {
  if ((scnjs.userEditWindow) || (scnjs.groupEditWindow)) e.returnValue = '';
};
