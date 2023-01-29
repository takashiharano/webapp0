<%@ page language="java" contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<title>main</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/asyncsample.js"></script>
</head>
<body>
AsyncTask Sample<br>
<input type="text" id="param-n" value="30">
<button onclick="webapp0.asyncsample.startTask();">Start</button>
<br>
<input type="text" id="task-id" value="">
<button onclick="webapp0.asyncsample.getTaskStatus();">Status</button>
<button onclick="webapp0.asyncsample.getTaskResult();">Result</button>
<button onclick="webapp0.asyncsample.cancelTask();">Cancel</button>
<br>
<div id="info"></div>
</body>
</html>
