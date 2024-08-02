/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.common.usermenu = {};

app.common.usermenu.openUserMenu = function() {
  var style = {
    background: 'rgba(0,0,0,0.3)'
  };
  util.modal.setStyle(style);
  
  var html = '';
  html += '<div class="dialog-content">';
  html += '<b>User Menu</b>';
  html += '<div style="margin-top:1em;">';
  html += '<div style="display:inline-block;width:160px;height:60px;text-align:left;">';
  html += '<li><span class="pseudo-link" onclick="app.common.usermenu.openChangePw();">Change Password</span></li>';
  html += '<li><span class="pseudo-link" onclick="app.common.confirmLogout();">Logout</span></li>\n';
  html += '</div>';
  html += '</div>';
  html += '</div>';
  html += '<button onclick="util.dialog.close();">Close</button>';

  var opt = {
    style: {
      body: {
        'width': '300px',
        'height': '120px'
      }
    },
    closeAnywhere: true,
  };

  util.dialog.open(html, opt);
};

app.common.usermenu.openChangePw = function() {
  var html = '';
  html += '<div class="dialog-content" style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';

  html += '<table style="width:95%;text-align:left;">';
  html += '  <tr>';
  html += '    <td>New password</td>';
  html += '    <td><input type="password" id="pw1" style="width:100%;"></td>';
  html += '  </tr>';
  html += '  <tr>';
  html += '    <td>Re-type</td>';
  html += '    <td><input type="password" id="pw2" style="width:100%;"></td>';
  html += '  </tr>';
  html += '<table>';

  html += '<div style="margin-top:24px;text-align:center;">';
  html += '<button onclick="app.common.usermenu.chengaPw();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="util.dialog.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';

  var opt = {
    style: {
      body: {
        'width': '340px',
        'height': '160px'
      },
      title: {
        'font-size': '24px'
      }
    }
  };

  util.dialog.open(html, opt);
  $el('#pw1').focus();
};

app.common.usermenu.chengaPw = function() {
  var uid = app.getUserId();

  var pw1 = $el('#pw1').value;
  var pw2 = $el('#pw2').value;
  if (!pw1) {
    app.showInfotip('Password is required', 2000);
    return;
  }
  if ((pw1 != '') || (pw2 != '')) {
    if (pw1 != pw2) {
      app.showInfotip('Password mismatched', 2000);
      return;
    }
  }

  var salt = uid;
  pwHash = app.common.getHash('SHA-256', pw1, salt);

  var params = {
    uid: uid,
    pw: pwHash
  };

  app.callServerApi('ChangePassword', params, app.common.usermenu.changePwCb);
};

app.common.usermenu.changePwCb = function(xhr, res) {
  if (res.status == 'OK') {
    var m = 'Your password has been updated.\n\nLogout now?\n\n';
    util.dialog.closeAll();
    util.confirm('Success', m, app.common.logout);
  } else {
    m = 'ERROR: ' + res.status;
    util.alert(m);
  }
};
