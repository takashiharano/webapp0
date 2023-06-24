<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>User List</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="libs/sha.js"></script>
<script src="js/userlist.js"></script>
<style>
.user-list-header {
  background: #ddebf7;
}

td.user-list,th.user-list {
  border: 1px solid #888;
  white-space: nowrap;
}

.edit-disabled {
  border: none !important;
  background: transparent;
}
</style>
</head>
<body>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/menu.jsp" />
<div style="margin-top:20px;">
<div style="margin-bottom:4px;">
<button onclick="webapp0.userlist.newUser();">ADD</button>
</div>
<div id="user-list"></div>
</div>
</body>
</html>
