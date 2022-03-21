<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String requestedUri = context.getStringInfo("requestedUrl");
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<title>WebApp</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/login.js"></script>
<style>
body {
  font-size: 16px;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

input {
  width: 250px;
  border: none;
  border-bottom: 1px solid #333;
  outline: none;
  font-size: 16px;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

#wrapper {
  position: relative;
  height: calc(100vh - 17px);
}

#login {
  position: absolute;
  display: inline-block;
  width: 225px;
  height: 125px;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  margin: auto;
  text-align: center;
}
</style>
<script>
var REQUESTED_URL = '<%= requestedUri %>';
</script>
</head>
<body>
<div id="wrapper">
  <div id="login">
    <div>
      <span id="led"></span> webapp0
    </div>
    <div style="margin-top:20px;">
      <input type="text" id="id" placeholder="Username">
    </div>
    <div style="margin-top:10px;">
      <input type="password" id="pw" placeholder="Password"><br>
    </div>
    <div style="margin-top:10px;text-align:left;">
      <span id="message"></span><br>
    </div>
    <div style="margin-top:10px;">
      <button onclick="webapp0.login.login();">LOGIN</button>
    </div>
  </div>
</div>
</body>
</html>
