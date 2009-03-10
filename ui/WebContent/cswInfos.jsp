<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ page import="de.ingrid.iplug.util.*"%>
<%@ include file="timeoutcheck.jsp"%>
<%

PlugDescription description = (PlugDescription)request.getSession().getAttribute("description");
String cswServiceUrl = "serviceUrl";
String useIndex = "useIndex";

if(!WebUtil.getParameter(request, cswServiceUrl, "").equals("") && !WebUtil.getParameter(request, timeout, "").equals("") && !WebUtil.getParameter(request, soapVersion, "").equals("")){
	description.put(cswServiceUrl, request.getParameter(cswServiceUrl));
	if(!WebUtil.getParameter(request, useIndex, "").equals("")) {
		description.put(useIndex, "true");
	}
	response.sendRedirect(response.encodeRedirectURL("scheduling.jsp"));
} 


%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Csw Infos</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet"
	type="text/css" />
</head>
<body>
<center>
<div class="headline"><br />
Csw Infos <br />
<br />
</div>
<br />
<form method="post" action="<%=response.encodeURL("cswInfos.jsp")%>">
<table>
	<tr>
		<td colspan="2" class="tablehead">Csw Infos</td>
	</tr>
	<tr>
		<td class="tablecell" width="100">Csw Service Url:</td>
		<td class="tablecell"><input type="text" name="cswServiceUrl"
			value="<%=description.get(cswServiceUrl)!=null?description.get(cswServiceUrl):""%>" style="width: 100%" /></td>
	</tr>
	<tr>
		<td class="tablecell" width="100">Index Verwenden:</td>
		<%
		String checked = description.get(useIndex)!=null && description.get(useIndex).equals("true") ? "checked=\"checked\"": "";
		%>
		<td class="tablecell"><input type="checkbox" name="useIndex"
			value="Index Verwenden" <%=checked%>
			style="width: 100%" /></td>

	</tr>

</table>


<table class="table" align="center">
	<tr align="center">
		<td><input type="button" name="back" value="Zur&#x00FC;ck"
			onclick="history.back()" /></td>
		<td><input type="button" name="cancel" value="Abbrechen"
			onclick="window.location.href='<%=response.encodeURL("index.jsp")%>'" />
		</td>
		<td><input type="hidden" name="submitted" value="true"> <input
			type="submit" value="Weiter" value="true" /></td>
	</tr>
</table>
</form>
</center>
</body>
</html>