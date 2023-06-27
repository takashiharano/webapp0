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
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/userlist.js"></script>
<style>
.item-list-header {
  background: #ddebf7;
}

td.item-list,th.item-list {
  border: 1px solid #888;
  white-space: nowrap;
}

tr.item-list:hover {
  background: #ecfaff;
}

.edit-disabled {
  border: none !important;
  background: transparent;
}
</style>
</head>
<body>
<jsp:include page="common/header.jsp" />
<div id="contents">
<jsp:include page="common/menu.jsp" />

<div style="margin-bottom:4px;">
<button onclick="app.userlist.newUser();">+</button>
</div>
<div id="user-list"></div>

</div>
<jsp:include page="common/footer.jsp" />
</body>
</html>
