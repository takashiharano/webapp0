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
<title>Dashboard</title>
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
</head>
<body>
<jsp:include page="common/header.jsp" />
<div id="contents">
<jsp:include page="common/menu.jsp" />

<pre>
Hello, world!
<%= datetime %> (<%= timestamp %>)
</pre>

</div>
<jsp:include page="common/footer.jsp" />
</body>
</html>
