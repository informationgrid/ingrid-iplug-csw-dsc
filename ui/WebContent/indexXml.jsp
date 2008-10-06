<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>

<%@ page import="java.sql.*"%>
<%@ page import="de.ingrid.iplug.csw.dsc.index.*"%>
<%@ page import="de.ingrid.iplug.*"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ include file="timeoutcheck.jsp"%>
<%!
private static IndexRunner fIndexRunner;
class IndexRunner extends Thread{
	private boolean fIndexing = true;
 
	private PlugDescription fDescription;
	public IndexRunner(PlugDescription description){
		fDescription = description;
	}
	public void run(){
		try{

			File indexFolder = new File((String)fDescription.get("workingDirectory"));
			indexFolder.mkdirs();
			
			IIndexer indexer = new Indexer();
			indexer.open(indexFolder);
			
			List collection = DocumentReaderFactory.getDocumentReaderCollection();
			for(int i=0; i<collection.size(); i++){
				Object object = collection.get(i);
				indexer.index((IDocumentReader) object);
			}
			
			indexer.close();
		
		} catch (Exception e ){
			e.printStackTrace();
		}
		fIndexing= false;
	}
	
	public boolean isIndexing(){
		return fIndexing;
	}
}

%>
<%
if(fIndexRunner == null){
	PlugDescription   description = (PlugDescription)  request.getSession().getAttribute("description");
	fIndexRunner = new IndexRunner(description);
	fIndexRunner.start();
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
	if(fIndexRunner!=null && !fIndexRunner.isIndexing()){
		fIndexRunner = null;
	%>
<table class="table" align="center">
	<tr align="center">
		<td align="center"><br />
		Indizierung abgeschlossen.<br />
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
<form method="post" action="<%=response.encodeURL("search.jsp")%>">
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