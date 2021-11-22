var webapp0 = {};
webapp0.common = {};

webapp0.common.logout = function() {
  util.confirm('Logout?', webapp0.common._logout);
};

webapp0.common._logout = function() {
  app.callServerApi('logout', null, webapp0.common.logoutCb);
};

webapp0.common.logoutCb = function() {
  location.href = 'main';
};
