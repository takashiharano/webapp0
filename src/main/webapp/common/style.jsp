<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
%>
<style>
body {
  box-sizing: border-box;
  width: 100%;
  min-width: 1000px;
  height: calc(100vh - 10px);
  margin: 0;
  padding: 0;
  margin: 0;
  padding: 0;
  font-size: 14px;
  font-family: Meiryo;
  color: <%= context.getConfigValue("contents_fg_color") %>;
  background: <%= context.getConfigValue("contents_bg_color") %>;
}

button, input[type="button"], input[type="submit"] {
  width: 70px;
  height: 20px;
  border: none;
  border-radius: 2px;
  outline: none;
  color: <%= context.getConfigValue("button_fg_color") %>;
  background: <%= context.getConfigValue("button_bg_color") %>;
  transition: all 0.2s ease;
}

button:hover, input[type="button"]:hover, input[type="submit"]:hover {
  cursor:pointer;
  background: #00a8ff;
  color: #fff;
  transition: all 0.2s ease;
}

button:disabled, input[type="button"]:disabled, input[type="submit"]:disabled {
  background: #888;
}

.button-red {
  background: #a00;
}
.button-red:hover {
  background: #d55;
}

input[type="text"],input[type="password"] {
  border: none;
  border-bottom: 1px solid #000;
  outline: none;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

input:-webkit-autofill {
  -webkit-transition: all 86400s;
  transition: all 86400s;
}

input[type="checkbox"] {
  position: relative;
  top: 2px;
}

textarea {
  outline: none;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

pre {
  margin: 2px 0;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

table {
  border-collapse: collapse;
}

td,th {
  padding: 2px 4px;
}

td {
  vertical-align: top;
}

.row-odd {
  background: #fff;
}

.row-even {
  background: #f4fdff;
}

tr.item-list:hover {
  background: #ecfaff;
}

.link-button {
  color: #00a;
}

.link-button-inactive {
  color: #aaa;
}

.screen-button {
  min-width: 7em;
}

.screen-button-active {
  font-weight: bold;
}

.sort-button {
  display: inline-block;
  line-height: 1em;
  color: #ccc;
  font-size: 8px;
}

.sort-button:hover {
  cursor: pointer;
}

.sort-active {
  color: #555;
}

.dialog-content {
  font-size: 16px;
}

.text-red {
  color: #f44;
}

.text-skyblue {
  color: #0af;
}

#clock {
  margin-left: 1em;
  font-family: Consolas, Monaco, Menlo, monospace, sans-serif;
}

#header {
  display: table;
  position: relative;
  width: 100%;
  height: 32px;
  table-layout: fixed;
  white-space: nowrap;
  color: <%= context.getConfigValue("header_fg_color") %>;
  background: <%= context.getConfigValue("header_bg_color") %>;
}

#header-content {
  display: table-cell;
  position: relative;
  width: 100%;
  height: 100%;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

#contents {
  width: calc(100% - 8px);
  position: relative;
  min-height: calc(100% - 64px);
  padding: 4px;
  overflow: auto;
}

#footer {
  display: table;
  position: relative;
  width: 100%;
  height: 32px;
  table-layout: fixed;
  white-space: nowrap;
  color: <%= context.getConfigValue("footer_fg_color") %>;
  background: <%= context.getConfigValue("footer_bg_color") %>;
}

#footer-content {
  display: table-cell;
  position: relative;
  width: 100%;
  height: 100%;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}

</style>
