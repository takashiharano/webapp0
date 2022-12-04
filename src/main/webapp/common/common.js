var webapp0 = {};
webapp0.common = {};

webapp0.common.showInfotip = function(s, d) {
  if (!d) d = 3000;
  var opt = {
    style: {
      fontSize: '16px'
    }
  };
  util.infotip.show(s, d, opt);
};

webapp0.common.logout = function() {
  util.confirm('Logout?', webapp0.common._logout);
};

webapp0.common._logout = function() {
  app.callServerApi('logout', null, webapp0.common.logoutCb);
};

webapp0.common.logoutCb = function() {
  location.href = 'main';
};

webapp0.common.getHash = function(algorithm, src, salt) {
  var shaObj = new jsSHA(algorithm, 'TEXT');
  shaObj.update(src);
  if (salt != undefined) {
    shaObj.update(salt);
  }
  var hash = shaObj.getHash('HEX');
  return hash;
};
