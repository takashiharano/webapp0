<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>App Manager</title>
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/appmgr.js"></script>
<style>
.item-list-header {
  background: #ddebf7;
}

td.item-list,th.item-list {
  border: 1px solid #888;
  white-space: nowrap;
}

table.edit-table {
  width: 100%;
}

.edit-table td {
  white-space: pre;
}

.edit-disabled {
  border: none !important;
  background: transparent;
}

.led-color-green {
  color: #4dd965;
}

.led-color-yellow {
  color: #ffba00;
}

.led-color-red {
  color: #f44d41;
}

.led-color-gray {
  color: #888;
}

.warn-red {
  color: #f66;
}

.you {
  color: #26c;
}

.user-edit-field-name {
  width: 120px;
}

.session-info-head {
  vertical-align: bottom;
}

.timeline-span {
  color: #555;
  cursor: default;
}

.timeline-forward {
  color: #ccc;
}

.timeline-current {
  cursor: default;
}

.timeline-acc-ind {
  color: #32cd32;
}

.timeline-acc-ind-past {
  opacity: 0.6;
}

.timeline-acc-ind-out {
  color: #d66;
}

.timeline-acc-ind-time {
  color: #777;
}

.wday-sat {
  color: #1caed7;
}

.wday-sun {
  color: #e86;
}

.search-highlight {
  background: rgba(200,200,200,0.5) !important;
}

.list-info {
  color: #666;
}

.login-locked {
  color: #f00;
  font-weight: bold;
}

#contents {
  font-size: 12px;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

#message {
  display: inline-block;
  margin-left: 16px;
}
</style>
<script>
var appconfig = {
  login_failure_max: <%= context.getConfigValueAsInteger("login_failure_max") %>
};
</script>
</head>
<body>
<jsp:include page="common/header.jsp" />
<div id="contents">
<jsp:include page="common/menu.jsp" />

<div style="margin-bottom:4px;">
<b>Users</b><br>
<button onclick="app.appmgr.newUser();">+</button>
<button onclick="app.appmgr.reloadUserInfo();">Reload</button>
<span style="margin-left:16px;">Search: <input type="text" id="search-text" style="width:150px;" oninput="scnjs.onSearchInput(this);"></span><input type="checkbox" id="search-filter" onchange="scnjs.onFilterChange();"><label for="search-filter">Filter</label></span>
<span id="letter-case-button" class="pseudo-link link-button" style="margin-left:16px;" onclick="scnjs.toggleLetterCase();"><span id="uc">A</span><span id="lc">a</span></span>
</div>
<div id="user-list" style="width:100%;max-height:400px;overflow:auto;"></div>

<pre style="margin-top:20px;">Sessions
<div id="session-list" style="width:100%;max-height:250px;overflow:auto;"></div></pre>

<div style="display:inline-block;margin-top:20px;margin-bottom:40px;">
<div style="margin-bottom:4px;">
<b>Groups</b><br>
<button onclick="app.appmgr.newGroup();">+</button>
<button onclick="app.appmgr.getGroupList();">Reload</button>
<span id="groups-status" style="margin-left:8px;"></span><br>
</div>
<div id="group-list" style="width:100%;max-height:300px;overflow:auto;"></div>
</div>

<div style="margin-bottom:4px;">
<b>App Reset</b><br>
<button onclick="app.appmgr.resetApp();">RESET</button><div id="message"></div>
</div>

</div>
<jsp:include page="common/footer.jsp" />
</body>
</html>
