<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title></title>
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/_template.js"></script>
</head>
<body>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/menu.jsp" />

<input type="text" id="text" value="Test"><button onclick="webapp0._template.test();">TEST</button><button onclick="webapp0._template.clear();">CLEAR</button>
<span id="msg"></span>
<pre>
</pre>
</body>
</html>
