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
if (context.isScreenEnabled("userlist") && context.hasPrivilege("usermng")) {
%>
<button id="screen-button-useredit" class="screen-button" onclick="app.screen('useredit');">User Edit</button>
<%
}
%>
</div>
