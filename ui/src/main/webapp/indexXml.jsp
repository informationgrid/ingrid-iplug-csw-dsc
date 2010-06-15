<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>

<%@ page import="java.sql.*"%>
<%@ page import="de.ingrid.iplug.csw.dsc.index.*"%>
<%@ page import="de.ingrid.iplug.*"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>

<%@ page import="de.ingrid.utils.xml.*" %>    
<%@ page import="java.io.*"%>
<%@ page import="de.ingrid.iplug.util.*"%>
<%@ page import="de.ingrid.utils.BeanFactory"%>

<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<%@ include file="timeoutcheck.jsp"%>

<%!
private static IndexRunner fIndexRunner;
class IndexRunner extends Thread{
	private boolean fIndexing = true;
	private boolean fError = false;
    private String fErrorMsg = "";
 
	private PlugDescription fDescription;
	public IndexRunner(PlugDescription description){
		fDescription = description;
	}
	public void run(){
		try {
			IndexingJob job = new IndexingJob();
			job.execute(null);
		} catch (Exception e ){
			e.printStackTrace();
			fError = true;
//			fErrorMsg = e.toString();
			StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fErrorMsg = sw.toString();
		}
		fIndexing= false;
	}
	
	public boolean isIndexing(){
		return fIndexing;
	}
    public boolean hasError(){
        return fError;
    }
    public String getErrorMsg(){
        return fErrorMsg;
    }
}
%>
<%
PlugDescription  description = (PlugDescription)  request.getSession().getAttribute("description");
if(fIndexRunner == null){
	// save pd first. the indexer job will pick it up from the conf directory
	BeanFactory beanFactory = (BeanFactory) application.getAttribute("beanFactory");
	File pd_file = (File) beanFactory.getBean("pd_file");
	XMLSerializer serializer = new XMLSerializer();
	if (null == description) {
		System.out.println("step1/indexXml.jsp: ERROR in step1/indexXml.jsp: current values lost during session timeout, plugdescription from web container was <null> and was not written");
	} else {
	    serializer.serialize(description,pd_file);
		fIndexRunner = new IndexRunner(description);
		fIndexRunner.start();
	}
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Datenbank indizieren</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet"
	type="text/css" />
<%
if(fIndexRunner!=null && fIndexRunner.isIndexing()){
%>
<meta http-equiv="refresh" content="5;">
<%} %>
</head>
<body>
<center>
<div class="headline"><br />
Indizierung der Daten<br />
<br />
<span class="byline">Die Datenbank wird indiziert.
Abh&#x00E4;ngig von der Menge der Daten kann dieser Prozess einige Zeit
dauern.</span></div>
<br />

<%
    boolean hasError = false;
	if(fIndexRunner!=null && !fIndexRunner.isIndexing()){
       String displayTitle = "<br />";
	   String displayMessage = "Indizierung abgeschlossen.";
	   hasError = fIndexRunner.hasError();

	   if (hasError) {
           displayTitle = "FEHLER beim Zugriff auf " + description.get("serviceUrl") + "<br /><br />";
           displayMessage = fIndexRunner.getErrorMsg();
	   }
	  
		fIndexRunner = null;
	%>
<table class="table" align="center">
	<tr align="center">
		<td align="center">
		<%=displayTitle%>
		<c:out value="<%=displayMessage%>" escapeXml="true" /><br />
		</td>
	</tr>
</table>
<%}else{ %>
<table>
	<tr>
		<td align="center">Bitte haben Sie Geduld ...<br />
		<img src="<%=response.encodeURL("gfx/progressbar_anim.gif")%>" /></td>
	</tr>
</table>
<%} %> <br />
<%
    String nextUrl = response.encodeURL("search.jsp");
    if (hasError) {
        nextUrl = "javascript:history.back()";
    }
%>
<form method="post" action="<%=nextUrl%>">
<table class="table" align="center">
	<tr align="center">
		<td><input type="button" name="back" value="Zur&#x00FC;ck"
			onclick="history.back()" /></td>
		<td><input type="button" name="cancel" value="Abbrechen"
			onclick="window.location.href='<%=response.encodeURL("index.jsp")%>'" />
		</td>
		<td><input type="submit" value="Weiter" /></td>
	</tr>
</table>
</form>
</center>
</body>
</html>