<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp1.Hello"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<title>main</title>
</head>
<body>
<pre>
<%= Hello.hello() %>
<%
  String info = (String) request.getAttribute("info");
  if (info != null) {
%>
<%= info %>
<%
  }
%>
</pre>
</body>
</html>
