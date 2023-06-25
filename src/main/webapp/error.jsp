<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String errorInfo = (String) context.getInfo("errorInfo");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>main</title>
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<style>
body {
  background: #0078d7;
  color: #fff;
  font-size: 16px;
}
</style>
</head>
<body>
<pre>
<%= errorInfo %>
</pre>
</body>
</html>
