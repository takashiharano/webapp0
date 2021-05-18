<%@page language="java" contentType="text/html; charset=utf-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<title>WebApp</title>
</head>
<body>
Result<br>
<pre>
<%
  String result = (String) request.getAttribute("result");
  if (result != null) {
%>
<%= result %>
<%
  }
%>
</pre>
</body>
</html>
