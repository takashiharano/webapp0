<%@ page language="java" contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Screen1</title>
<jsp:include page="common/include.jsp" />
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/screen2.js"></script>
</head>
<body>
<jsp:include page="common/header.jsp" />
<div id="contents">
<jsp:include page="common/menu.jsp" />

<div>
AsyncTask Sample <span id="led1"></span>
</div>
<input type="text" id="param-n" value="100">
<button onclick="app.screen2.startTask();">Start</button>
<br>
<input type="text" id="task-id" value="">
<button onclick="app.screen2.getTaskStatus();">Status</button>
<button onclick="app.screen2.getTaskResult();">Result</button>
<button onclick="app.screen2.cancelTask();">Cancel</button>
<br>
<div id="info"></div>

</div>
<jsp:include page="common/footer.jsp" />
</body>
</html>
