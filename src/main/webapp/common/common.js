/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
var webapp0 = {};
webapp0.common = {};

webapp0.common.confirmLogout = function() {
  util.confirm('Logout?', webapp0.common.logout);
};

webapp0.common.logout = function() {
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
