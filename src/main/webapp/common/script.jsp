<%@ page import="com.takashiharano.webapp0.ProcessContext"%>
<%@ page import="com.takashiharano.webapp0.AppManager"%>
<%
ProcessContext context = (ProcessContext) request.getAttribute("context");
String screenId = (String) context.getInfo("screenId");
String username = context.getUsername();
AppManager appManager = AppManager.getInstance();
int n = appManager.getConfigIntValue("bab64_n_param", 1);
%>
<script src="https://debugjs.net/debug.js"></script>
<script src="https://libutil.com/libs/util.js"></script>
<script src="common/app.js"></script>
<script src="common/common.js"></script>
<script>
var BSB64N = <%= n %>;
app.screenId = '<%= screenId %>';
app.username = '<%= username %>';
</script>
