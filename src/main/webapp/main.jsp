<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<title>main</title>
</head>
<body>
Hello, world!<br>
<pre>
<%
String info = (String) context.getInfo("info");
if (info != null) {
%>
<%= info %>
<%
}
%>
</pre>
</body>
</html>
