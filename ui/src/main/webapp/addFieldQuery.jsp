<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ page import="de.ingrid.iplug.util.*"%>
<%@ page import="java.util.regex.Pattern"%>

<%@ include file="timeoutcheck.jsp"%>
<%
String error = "";
String iBusUrl = WebUtil.getParameter(request, "busUrl", "");
String action = WebUtil.getParameter(request, "action", "");
PlugDescription  description = (PlugDescription) request.getSession().getAttribute("description");

if("addfieldquery".equals(action)) {
	String key = WebUtil.getParameter(request, "key", "");
	String value = WebUtil.getParameter(request, "value", "");
	String regex = WebUtil.getParameter(request, "regex", ".*");
	boolean prohibited  = WebUtil.getParameter(request, "prohibited", null) != null;
	boolean required  = WebUtil.getParameter(request, "required", null) != null;
	FieldQuery fieldQuery = new FieldQuery(required, prohibited, key, value);
	QueryExtensionContainer container = (QueryExtensionContainer) description.get("QUERY_EXTENSION");
	if(container ==  null) {
		container = new QueryExtensionContainer();
		description.put("QUERY_EXTENSION", container);
	}
	QueryExtension queryExtension = container.getQueryExtension(iBusUrl);
	if(queryExtension == null) {
		queryExtension = new QueryExtension();
		queryExtension.setBusUrl(iBusUrl);
		Pattern pattern = Pattern.compile(regex);
		queryExtension.setPattern(pattern);
		container.addQueryExtension(queryExtension);
	}
	queryExtension.addFieldQuery(fieldQuery);
}else if(action.equals("reset")){
	QueryExtensionContainer container = (QueryExtensionContainer) description.get("QUERY_EXTENSION");
	if(container !=  null) {
		container.removeQueryExtension(iBusUrl);
	}
} else if(action.equals("save")){
	response.sendRedirect(response.encodeRedirectURL("cswInfos.jsp"));
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@page import="de.ingrid.utils.query.FieldQuery"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="de.ingrid.utils.QueryExtensionContainer"%>
<%@page import="de.ingrid.utils.QueryExtension"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Fieldquery hinzuf&#x00FC;gen</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
	<center>
	<div class="headline">
		<br />Verf&#x00FC;gbarkeit der Ergebnisse
		<br /><br />
		<span class="byline">Geben Sie Field Queries an.</span>		
	</div>
	<br />

		<table class="table" width="400" align="center">
			<tr>
				<td class="tableHead">Bus-Url</td>
				<td class="tableHead">Regex</td>
				<td class="tableHead">Index Feld Name</td>
				<td class="tableHead">Index Feld Wert</td>
				<td class="tableHead">Verboten</td>
				<td class="tableHead">Erforderlich</td>
				<td class="tableHead">Hinzuf&#x00FC;gen</td>
				<td class="tableHead">Reset</td>
				<td class="tableHead">Hinzugefuegter Regex</td>
				<td class="tableHead">Hinzugefuegte Query Erweiterung</td>
			</tr>
			<%
			String[] busUrls = description.getBusUrls();
			for(int i=0; i < busUrls.length; i++){
			%>
			
			<tr>
				<form method="post" action="<%=response.encodeURL("addFieldQuery.jsp")%>">
				<input type="hidden" name="action" value="addfieldquery"/>
				<input type="hidden" name="busUrl" value="<%=busUrls[i]%>"/>
				<td class="tableCell"><%=busUrls[i]%></td>
				<td class="tableCell"><input type="text" name="regex" value="" /></td>
				<td class="tableCell"><input type="text" name="key" value="" /></td>
				<td class="tableCell"><input type="text" name="value" value="" /></td>
				<td class="tableCell"><input type="checkbox" name="prohibited"></td>
				<td class="tableCell"><input type="checkbox" name="required"></td>
				<td class="tableCell"><input type="submit" value="Hinzuf&#x00FC;gen"/></td>
				</form>
				<form method="post" action="<%=response.encodeURL("addFieldQuery.jsp")%>">
					<input type="hidden" name="action" value="reset"/>
					<input type="hidden" name="busUrl" value="<%=busUrls[i]%>"/>
				<td class="tableCell"><input type="submit" value="Reset"/></td>
				</form>

				<td class="tableCell">
				<%
				QueryExtensionContainer container = (QueryExtensionContainer) description.get("QUERY_EXTENSION");
				if(container != null) {
					QueryExtension queryExtension = container.getQueryExtension(busUrls[i]);
					if(queryExtension != null) {
							%>
							<%=queryExtension.getPattern()!=null?queryExtension.getPattern().pattern():""%><br>
							<%
					}
				}
				%>
				</td>

				<td class="tableCell">
				<%
				container = (QueryExtensionContainer) description.get("QUERY_EXTENSION");
				if(container != null) {
					QueryExtension queryExtension = container.getQueryExtension(busUrls[i]);
					if(queryExtension != null) {
						Set set = queryExtension.getFieldQueries();
						Iterator iterator = set.iterator();
						while(iterator.hasNext()) {
							FieldQuery fieldQuery = (FieldQuery) iterator.next();
							%>
							<%=fieldQuery %><br>
							<%
						}
					}
				}
				%>
				</td>
				
			</tr>
			
		<%} %>
		</table>	
		
	<form method="post" action="<%=response.encodeURL("addFieldQuery.jsp")%>" >
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