<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="de.ingrid.iplug.scheduler.*" %>    
<%@ page import="de.ingrid.iplug.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.quartz.*" %>
<%@ page import="de.ingrid.iplug.csw.dsc.index.*" %>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ include file="timeoutcheck.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Scheduling</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
<%

PlugDescription description = (PlugDescription) request.getSession().getAttribute("description");	
String time = description.get("cronPattern_time")!= null ? (String)description.get("cronPattern_time") : "";
String day = description.get("cronPattern_day")!= null ? (String)description.get("cronPattern_day") : "";
String cronPattern = "";
if(!time.equals("") && !day.equals("")){
	cronPattern = time + " " + day;
}

if (request.getParameter("time") != null && request.getParameter("day") != null) {	
	time = request.getParameter("time");
	day = request.getParameter("day");
	cronPattern = time + " " + day;
	description.put("cronPattern_time", time);
	description.put("cronPattern_day", day);
	File file = new File(description.getWorkinDirectory(),"jobstore");
	
	if(!time.equals("never") && !day.equals("never")){			
		if(!file.exists()){
			file.mkdirs();
		}
		SchedulingService scheduler = new SchedulingService(file);
		scheduler.scheduleCronJob("indexing", "dsc", IndexingJob.class, new java.util.HashMap(),cronPattern  );
	}else if(file.exists()){		
		File[] myFiles = file.listFiles();
		for(int i=0; i < myFiles.length; i++){
			myFiles[i].delete();
		}		
	}
	response.sendRedirect(response.encodeRedirectURL("indexXmlQuestion.jsp"));
}
%>
<center>
	<div class="headline"><br />
		Scheduling<br /><br />
		<span class="byline">
		Bitte geben Sie ein Zeitmuster an, nach dem der Index regelm&#x00E4;&#x00DF;ig neu erstellt werden soll.<br />
		Die Bedingungen werden mit UND verkn&#x00FC;pft (z.B. jeden Sonntag 15:00).
		</span>		
	</div>
	<br />
	<span class="metadata">Cron Pattern: <%=cronPattern %></span><br />
	<br />
	<form method="post" action="<%=response.encodeURL("scheduling.jsp")%>">
		<table class="table" width="400" align="center">
			<tr>
				<td colspan="2" class="tablehead">Scheduling</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Stunde:Minute</td>
				<td class="tablecell">
					<select name="time" style="width:100%">						
						<option value="never">Nie (keine autom. Neuindizierung)</option>
						<optgroup label="Feste Uhrzeit">
							<%
							for(int i = 0; i < 24; i++){
							%>
								<option value="0 0 <%=i%>"<%if(time.equals("0 0 "+i)) {%> selected="selected"<%}%>><%if(i < 10){%>0<%}%><%=i%>:00</option>
							<%} %>
						</optgroup>
						<optgroup label="Zyklische Wiederholung">
							<option value="0 0/5 *" <%if(time.equals("0 0/5 *")) {%>selected="selected"<%}%>>alle 5 Min</option>
							<option value="0 0/30 *" <%if(time.equals("0 0/30 *")) {%>selected="selected"<%}%>>alle 30 Min</option>
							<option value="0 0 0/1" <%if(time.equals("0 0 0/1")) {%>selected="selected"<%}%>>st&#x00FC;ndlich</option>
							<option value="0 0 0/2" <%if(time.equals("0 0 0/2")) {%>selected="selected"<%}%>>alle 2 Stunden</option>
							<option value="0 0 0/4" <%if(time.equals("0 0 0/4")) {%>selected="selected"<%}%>>alle 4 Stunden</option>
							<option value="0 0 0/6" <%if(time.equals("0 0 0/6")) {%>selected="selected"<%}%>>alle 6 Stunden</option>
							<option value="0 0 0/8" <%if(time.equals("0 0 0/8")) {%>selected="selected"<%}%>>alle 8 Stunden</option>
							<option value="0 0 0/12" <%if(time.equals("0 0 0/12")) {%>selected="selected"<%}%>>alle 12 Stunden</option>
						</optgroup>													
					</select>					
				</td>
			</tr>			
			<tr>
				<td class="tablecell" width="100">Tag / Woche:</td>
				<td class="tablecell">
					<select name="day" style="width:100%">
						<option value="never">Nie (keine autom. Neuindizierung)</option>
						<option value="* * ?" <%if(day.equals("* * ?")) {%>selected="selected"<%}%>>t&#x00E4;glich</option>
						<optgroup label="Fester Wochentag">												
							<option value="? * MON"  <%if(day.equals("? * MON")) {%>selected="selected"<%}%>>nur Montag</option>
							<option value="? * TUE"  <%if(day.equals("? * TUE")) {%>selected="selected"<%}%>>nur Dienstag</option>
							<option value="? * WED"  <%if(day.equals("? * WED")) {%>selected="selected"<%}%>>nur Mittwoch</option>
							<option value="? * THU"  <%if(day.equals("? * THU")) {%>selected="selected"<%}%>>nur Donnerstag</option>
							<option value="? * FRI"  <%if(day.equals("? * FRI")) {%>selected="selected"<%}%>>nur Freitag</option>
							<option value="? * SAT"  <%if(day.equals("? * SAT")) {%>selected="selected"<%}%>>nur Samstag</option>
							<option value="? * SUN"  <%if(day.equals("? * SUN")) {%>selected="selected"<%}%>>nur Sonntag</option>
						</optgroup>
						<optgroup label="Ausgew&#x00E4;hlte Wochentage">
							<option value="? * MON-FRI"  <%if(day.equals("? * MON-FRI")) {%>selected="selected"<%}%>>Montag bis Freitag</option>
							<option value="? * MON,THU"  <%if(day.equals("? * MON,THU")) {%>selected="selected"<%}%>>Montag und Donnerstag</option>
							<option value="? * TUE,FRI"  <%if(day.equals("? * TUE,FRI")) {%>selected="selected"<%}%>>Dienstag und Freitag</option>
							<option value="? * MON,WED,FRI"  <%if(day.equals("? * MON,WED,FRI")) {%>selected="selected"<%}%>>Montag, Mittwoch und Freitag</option>
							<option value="? * TUE,THU,SAT"  <%if(day.equals("? * TUE,THU,SAT")) {%>selected="selected"<%}%>>Dienstag, Donnerstag und Samstag</option>
						</optgroup>
						<optgroup label="Zyklische Wiederholung">
							<option value="1/2 * ?"  <%if(day.equals("1/2 * ?")) {%>selected="selected"<%}%>>alle 2 Tage</option>
							<option value="1/3 * ?"  <%if(day.equals("1/3 * ?")) {%>selected="selected"<%}%>>alle 3 Tage</option>
							<option value="1/4 * ?"  <%if(day.equals("1/4 * ?")) {%>selected="selected"<%}%>>alle 4 Tage</option>
							<option value="1/5 * ?"  <%if(day.equals("1/5 * ?")) {%>selected="selected"<%}%>>alle 5 Tage</option>	
							<option value="1,14,L * ?"  <%if(day.equals("1,14,L * ?")) {%>selected="selected"<%}%>>am 01., 14. und letzten des Monats</option>
						</optgroup>					
						<optgroup label="Fester Tag im Monat">							
							<%
							for(int i = 1; i < 32; i++){
							%>
								<option value="<%=i%> * ?"<%if(day.equals(i+" * ?")) {%> selected="selected"<%}%>>nur am <%if(i < 10){%>0<%}%><%=i%>. des Monats</option>
							<%} %>
							<option value="L * ?"  <%if(day.equals("L * ?")) {%>selected="selected"<%}%>>nur am letzten Tag des Monats</option>	
						</optgroup>
					</select>
				</td>
			</tr>													
			</table>
			<br />
			<table class="table" align="center">					
				<tr align="center">
					<td>
						<input type="button" name="back" value="Zur&#x00FC;ck" onclick="history.back()"/>
					</td>
					<td>
						<input type="button" name="cancel" value="Abbrechen" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>						
					</td>
					<td>
						<input type="submit" value="Weiter" />
					</td>
				</tr>
			</table>
		</form>
</center>
</body>
</html>
