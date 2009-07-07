<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.PlugDescription" %>
<%@ page import="de.ingrid.iplug.util.*" %>
<%@ include file="timeoutcheck.jsp"%>
<%
int busIndex = Integer.parseInt(WebUtil.getParameter(request, "busIndex", "-1"));
PlugDescription  description = (PlugDescription) request.getSession().getAttribute("description");
String[] busUrls = description.getBusUrls();
for(int i=0; i < busUrls.length; i++){
	String busUrl = busUrls[i];
	if(i == busIndex){
		description.removeBusUrl(busUrl);
		response.sendRedirect(response.encodeRedirectURL("addIbus.jsp"));
	}
}
%>
