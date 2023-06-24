<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String requestedUri = context.getStringInfo("requestedUrl");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Login</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="js/login.js"></script>
<style>
body {
  font-size: 18px;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

input {
  width: 100%;
  border: none;
  border-bottom: 1px solid #333;
  outline: none;
  font-size: 18px;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

button {
  width: 100%;
}

#wrapper {
  position: relative;
  height: calc(100vh - 17px);
}

#login {
  position: absolute;
  display: inline-block;
  width: 320px;
  height: 180px;
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
      <input type="text" id="id" placeholder="Username" spellcheck="false">
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
