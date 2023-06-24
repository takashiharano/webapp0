<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String username = context.getUserFullName();
%>
<div id="header">
<div id="header-content">
webapp0
<span id="header-right">
<span id="user" class="pseudo-link" onclick="webapp0.common.logout();"><%= username %></span>
<span id="clock"></span>
<span id="version">v.<%= context.getAppVersion() %></span>
</span>
</div>
</div>
