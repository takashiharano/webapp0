<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String username = context.getUsername();
String fullname = context.getUserFullName();
if ((fullname == null) || "".equals(fullname)) {
  fullname = username;
}
%>
<div id="header">
<div id="header-content">
webapp0
<span id="header-right">
<span id="user" class="pseudo-link" onclick="webapp0.common.usermenu.openUserMenu();"><%= fullname %></span>
<span id="clock"></span>
<span id="version">v.<%= context.getAppVersion() %></span>
</span>
</div>
</div>
