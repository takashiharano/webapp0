/*!
 * THIS CODE IS IMPLEMENTED BASED ON THE WEBAPP0 TEMPLATE.
 * The template is released under the MIT license.
 * Copyright 2023 Takashi Harano
 */
app.common = {};

app.common.MESSAGES = {
  confirm_logout: 'Logout?'
};

app.common.init = function() {
  app.addMessages(app.common.MESSAGES);
};

app.common.confirmLogout = function() {
  util.confirm(app.getMessage('confirm_logout'), app.common.logout);
};

app.common.logout = function() {
  app.callServerApi('logout', null, app.common.logoutCb);
};

app.common.logoutCb = function() {
  location.href = 'main';
};

app.common.getHash = function(algorithm, src, salt) {
  var shaObj = new jsSHA(algorithm, 'TEXT');
  shaObj.update(src);
  if (salt != undefined) {
    shaObj.update(salt);
  }
  var hash = shaObj.getHash('HEX');
  return hash;
};

app.registerInitFunction(app.common.init);
