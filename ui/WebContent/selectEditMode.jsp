<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Bearbeiten Modus w&#x00E4;hlen</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
<center>
	<div class="headline">
		<br />Konfiguration geladen<br /><br />
		<span class="byline">Eine vorhandene Konfiguration konnte geladen werden. Bitte w&#x00E4;hlen Sie, ob Sie alle Einstellungen bearbeiten wollen oder nur die zeitgesteuerte Neuindizierung / sofortige Neuindizierung.</span>
	</div>
	<br />
	<table>
		<tr>
			<td>
				<form method="post" action="<%=response.encodeURL("selectWorkingFolder.jsp")%>">
					<input type="submit" value="Alle Einstellungen bearbeiten"/>
				</form>
			</td>
			<td>
				<form method="post" action="<%=response.encodeURL("selectWorkingFolder.jsp")%>">
					<input type="button" name="back" value="Zur&#x00FC;ck" onclick="history.back()"/>
				</form>
			</td>
			<td>
				<form method="post" action="<%=response.encodeURL("selectWorkingFolder.jsp")%>">
					<input type="button" name="cancel" value="Abbrechen" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>
				</form>							
			</td>
			<td>
				<form method="post" action="<%=response.encodeURL("save.jsp?mode=reindex")%>">
					<input type="submit" value="Nur Zeitsteuerung bearbeiten / sofortige Neuindizierung"/>
				</form>
			</td>
		</tr>
	</table>
</center>
</body>
</html>