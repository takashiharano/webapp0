<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String uid = context.getUserId();
String fullname = context.getUserFullName();
if ((fullname == null) || "".equals(fullname)) {
  fullname = uid;
}
%>
<div id="header">
<div id="header-content">
webapp0
<span style="position:absolute;right:10px;">
<span id="user" class="pseudo-link" onclick="app.common.usermenu.openUserMenu();"><%= fullname %></span>
<span id="clock"></span>
</span>
</div>
</div>
