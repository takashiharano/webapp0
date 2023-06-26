<%@page language="java" contentType="text/html; charset=utf-8"%>
<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<div id="footer">
<div id="footer-content">
  Footer
  <span style="position:absolute;right:10px;">
    <span id="version">v.<%= context.getAppVersion() %></span>
  </span>
</div>
</div>
