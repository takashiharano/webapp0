<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<div style="margin-bottom:4px;">
<button id="screen-button-dashboard" class="screen-button" onclick="app.screen('dashboard');">Dashboard</button>
<%
if (context.isScreenEnabled("screen1")) {
%>
<button id="screen-button-screen1" class="screen-button" onclick="app.screen('screen1');">Screen1</button>
<%
}
%>
<%
if (context.isScreenEnabled("userlist") && context.hasPrivilege("useredit")) {
%>
<button id="screen-button-userlist" class="screen-button" onclick="app.screen('userlist');">User List</button>
<%
}
%>
</div>