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
<div id="contents">
<jsp:include page="common/menu.jsp" />

<button onclick="app.system.resetApp();">RESET</button>
<div id="message"></div>

</div>
<jsp:include page="common/footer.jsp" />
</body>
</html>

