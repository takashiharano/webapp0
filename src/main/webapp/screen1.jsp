<%@ page language="java" contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>main</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/screen1.js"></script>
</head>
<body>
<jsp:include page="common/header.jsp" />
<jsp:include page="common/menu.jsp" />
<div>
AsyncTask Sample <span id="led1"></span>
</div>
<input type="text" id="param-n" value="100">
<button onclick="webapp0.screen1.startTask();">Start</button>
<br>
<input type="text" id="task-id" value="">
<button onclick="webapp0.screen1.getTaskStatus();">Status</button>
<button onclick="webapp0.screen1.getTaskResult();">Result</button>
<button onclick="webapp0.screen1.cancelTask();">Cancel</button>
<br>
<div id="info"></div>
</body>
</html>
