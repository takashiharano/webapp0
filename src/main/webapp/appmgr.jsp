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

.timeline-span {
  opacity: 0.6;
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
<button onclick="app.appmgr.reloadUserInfo();">RELOAD</button>
</div>
<div id="user-list" style="width:100%;max-height:400px;overflow:auto;"></div>

<pre style="margin-top:20px;">Sessions
<div id="session-list"></div></pre>

<div style="display:inline-block;margin-top:20px;margin-bottom:40px;">
<div style="margin-bottom:4px;">
<b>Groups</b><br>
<button onclick="app.appmgr.newGroup();">+</button>
<button onclick="app.appmgr.getGroupList();">RELOAD</button>
<span id="groups-status" style="margin-left:8px;"></span><br>
</div>
<div id="group-list"></div>
</div>

<div style="margin-bottom:4px;">
<b>App Reset</b><br>
<button onclick="app.appmgr.resetApp();">RESET</button><div id="message"></div>
</div>

</div>
<jsp:include page="common/footer.jsp" />
</body>
</html>
