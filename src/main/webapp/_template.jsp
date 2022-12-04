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
<meta charset="utf-8">
<title></title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/_template.js"></script>
</head>
<body>
<input type="text" id="text" value="Test"><button onclick="webapp0.xxx.test();">TEST</button><button onclick="webapp0.xxx.clear();">CLEAR</button>
<span id="msg"></span>
<pre>
<%= datetime %> (<%= timestamp %>)
App-Version: <%= appVersion %>
</pre>
</body>
</html>
