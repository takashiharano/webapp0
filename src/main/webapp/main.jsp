<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
long timestamp = System.currentTimeMillis();
Date date = new Date(timestamp);
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
String datetime = sdf.format(date);
String appVersion = context.getAppVersion();
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<title>main</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
</head>
<body>
<pre>
Hello, world!
<%= datetime %> (<%= timestamp %>)
App-Version: <%= appVersion %>
<button onclick="webapp0.common.logout();">Logout</button>
</pre>
</body>
</html>
