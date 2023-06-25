/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
webapp0.common.usermenu = {};

webapp0.common.usermenu.openUserMenu = function() {
  var style = {
    background: 'rgba(0,0,0,0.3)'
  };
  util.modal.setStyle(style);
  
  var html = '';
  html += '<b>User Menu</b>\n\n';
  html += '<div>';
  html += '<div style="display:inline-block;width:160px;height:60px;text-align:left;">';
  html += '<li><span class="pseudo-link" onclick="webapp0.common.usermenu.openChangePw();">Change Password</span></li>';
  html += '<li><span class="pseudo-link" onclick="webapp0.common.confirmLogout();">Logout</span></li>\n\n';
  html += '</div>';
  html += '</div>';
  html += '<button onclick="util.dialog.close();">Close</button>';

  var opt = {
    style: {
      body: {
        'width': '300px',
        'height': '160px'
      }
    },
    closeAnywhere: true,
  };

  util.dialog.open(html, opt);
};

webapp0.common.usermenu.openChangePw = function() {
  var html = '';
  html += '<div style="position:relative;width:100%;height:100%;text-align:center;vertical-align:middle">';

  html += '<table>';
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
  html += '<button onclick="webapp0.common.usermenu.chengaPw();">OK</button>'
  html += '<button style="margin-left:8px;" onclick="util.dialog.close();">Cancel</button>'
  html += '</div>';

  html += '</div>';

  var opt = {
    style: {
      body: {
        'width': '300px',
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

webapp0.common.usermenu.chengaPw = function() {
  var username = app.getUsername();

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
    pw: pwHash
  };

  app.callServerApi('EditUser', params, webapp0.common.usermenu.chengaPwCb);
};

webapp0.common.usermenu.chengaPwCb = function(xhr, res) {
  if (res.status == 'OK') {
    var m = 'Your password has been updated.\n\nLogout?\n\n';
    util.dialog.closeAll();
    util.confirm('Success', m, webapp0.common.logout);
  } else {
    m = 'ERROR: ' + res.status;
    util.alert(m);
  }
};