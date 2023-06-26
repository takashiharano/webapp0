/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
var webapp0 = {};
webapp0.common = {};

webapp0.common.MESSAGES = {
  confirm_logout: 'Logout?'
};

webapp0.common.init = function() {
  app.addMessages(webapp0.common.MESSAGES);
};

webapp0.common.confirmLogout = function() {
  util.confirm(app.getMessage('confirm_logout'), webapp0.common.logout);
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

app.registerInitFunction(webapp0.common.init);
