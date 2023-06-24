<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ page import="com.takashiharano.webapp0.ProcessContext" %>
<%@ page import="com.takashiharano.webapp0.user.UserInfo" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
UserInfo user = context.getUserInfo();
String username = user.getUsername();
String fullname = user.getFullName();
String privileges = user.getPrivilegesInOneLine();
boolean isAdmin = user.isAdmin();
int status = user.getStatus();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>User Info</title>
<jsp:include page="common/style.jsp" />
<jsp:include page="common/script.jsp" />
<script src="libs/sha.js"></script>
<script src="js/useredit.js"></script>
</head>
<body>
<%
if (isAdmin) {
%>
<button onclick="webapp0.useredit.newUser();">NEW</button>
<button onclick="webapp0.useredit.editUser();">EDIT</button>
<%
}
%>
<table>
  <tr>
    <td>Username</td>
    <td>
      <input type="text" id="username" value="<%= username %>">
<%
if (isAdmin) {
%>
<button onclick="webapp0.useredit.loadUserInfo();">LOAD</button>
<button onclick="webapp0.useredit.deleteUser();">DELETE</button>
<%
}
%>
    </td>
  </tr>
  <tr>
    <td>Full name</td>
    <td><input type="text" id="fullname" value="<%= fullname %>"></td>
  </tr>
  <tr>
    <td>isAdministrator</td>
    <td><input type="checkbox" id="isadmin"
<%
if (isAdmin) {
%>
 checked
<%
}
%>
>
    </td>
  </tr>
  <tr>
    <td>Privileges</td>
    <td><input type="text" id="privileges" value="<%= privileges %>"></td>
  </tr>
  <tr>
    <td>Status</td>
    <td><input type="text" id="status" value="<%= status %>"></td>
  </tr>
  <tr>
    <td>Password</td>
    <td><input type="password" id="pw1"></td>
  </tr>
  <tr>
    <td>Re-type</td>
    <td><input type="password" id="pw2"></td>
  </tr>
<table>

<button onclick="webapp0.useredit.onOkClick();">OK</button>
<button onclick="location.href='main'">Clear</button>
</body>
</html>
