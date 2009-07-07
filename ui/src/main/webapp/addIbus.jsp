<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ page import="de.ingrid.iplug.util.*"%>
<%@ include file="timeoutcheck.jsp"%>
<%
String error = "";
String iBusUrl = WebUtil.getParameter(request, "iBusUrl", "");
String action = WebUtil.getParameter(request, "action", "");
PlugDescription  description = (PlugDescription) request.getSession().getAttribute("description");

if(action.equals("add")){
	if(!iBusUrl.trim().equals("")){
		description.addBusUrl(iBusUrl);
	}else{
		error = "urlEmpty";
	}
}
if(action.equals("save")){
	if(description.getBusUrls().length < 1){
		error = "noIbusAdded";
	}else{
		response.sendRedirect(response.encodeRedirectURL("addFieldQuery.jsp"));
	}
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>IBus hinzuf&#x00FC;gen</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
	<center>
	<div class="headline">
		<br />Verf&#x00FC;gbarkeit der Ergebnisse
		<br /><br />
		<span class="byline">Geben Sie hier die URL eines iBusses an, dem Sie das Durchsuchen Ihrer Daten erlauben wollen. Sie k&#x00F6;nnen Ihre Datenquelle auch an mehrere iBusse anschlie&#x00DF;en.</span>		
	</div>
	<br />
	<!--
	<form method="post" action="<%=response.encodeURL("addIbus.jsp")%>" name="addIbus">
	<table class="table" align="center">
		<tr align="center">
			<td>
				<input type="button" name="back" value="Zur&#x00FC;ck" onclick="history.back()"/>
			</td>
			<td>
				<input type="button" name="cancel" value="Abbrechen" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>						
			</td>
			<td>
				<input type="hidden" name="action" value="save"/>
				<input type="submit" value="Weiter" />
			</td>
		</tr>
	</table>
	</form>
	-->	
<%if (error.equals("noIbusAdded")) { %>
	<div class="error">Bitte f&#x00FC;gen Sie Ihre Datenquelle zu mindestens einem Ibus hinzu.</div>
	<br />
<%} %>
<%if (error.equals("urlEmpty")) { %>
	<div class="error">URL darf nicht leer sein.</div>
	<br />
<%} %>	
		<table class="table" width="400" align="center">
			<tr>
				<td class="tableHead" colspan="2">Datenquelle zu iBus hinzuf&#x00FC;gen</td>
			</tr>
			<tr>
				<td class="tableCell" width="100">iBus-URL:</td>
				<td class="tableCell">
					<br />
					<%
					String[] busUrls = description.getBusUrls();
					for(int i=0; i < busUrls.length; i++){
					%>
						<form method="get" action="<%=response.encodeURL("deleteIbus.jsp")%>">
							<br /><b><%=i+1%>. iBus:</b> <%=busUrls[i] %>
							<input type="hidden" name="busIndex" value="<%=i%>"/>
							<input type="image" src="<%=response.encodeURL("gfx/delete.gif")%>"/>
						</form>
					<%} %>
					<br />
					<form method="post" action="<%=response.encodeURL("addIbus.jsp")%>" name="addIbus">
						<input type="hidden" name="action" value="add"/>
						<input type="text" name="iBusUrl" value="" style="width:100%"/>
						<br />
						<br />
						<input type="submit" value="Hinzuf&#x00FC;gen"/>
					</form>
				</td>
			</tr>
		</table>
        <br />
		<form method="post" action="<%=response.encodeURL("addIbus.jsp")%>" name="addIbus">
		<table class="table" align="center">
				<tr align="center">
					<td>
						<input type="button" name="back" value="Zur&#x00FC;ck" onclick="history.back()"/>
					</td>
					<td>
						<input type="button" name="cancel" value="Abbrechen" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>						
					</td>
					<td>
						<input type="hidden" name="action" value="save"/>
						<input type="submit" value="Weiter" />
					</td>
				</tr>
		</table>
		</form>
		</center>
</body>
<!-- ingrid-iplug-admin-step1 -->
</html>