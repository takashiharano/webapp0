<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>System</title>
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/system.js"></script>
</head>
<body>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/menu.jsp" />

<div>
<button onclick="webapp0.system.resetApp();">RESET</button>
<div id="message"></div>
</div>
</body>
</html>
