<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%@ page import="com.takashiharano.webapp0.AppManager"%>
<%@ page import="com.takashiharano.webapp0.user.User"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String screenId = (String) context.getInfo("screenId");
User user = context.getUserInfo();
String username = context.getUsername();
AppManager appManager = AppManager.getInstance();
int n = appManager.getConfigValueAsInteger("bab64_n_param", 1);
%>
<script>
var BSB64N = <%= n %>;
app.screenId = '<%= screenId %>';
app.username = '<%= username %>';

app.onScreenReady = function() {
<%
if (!"login".equals(screenId) && (user != null) && (user.hasFlag(User.FLAG_NEED_PW_CHANGE))) {
%>
  app.common.usermenu.openChangePw();
<%
}
%>
};
</script>
