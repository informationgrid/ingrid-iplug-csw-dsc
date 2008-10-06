<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ include file="timeoutcheck.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Datenbank indizieren</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
<center>
		<div class="headline">
			<br />Indizierung der Daten<br /><br /><br />
		</div>
			<br />
			<table class="table" cellpadding="2" cellspacing="2">
				<tr>
					<td class="tablehead" colspan="2">Jetzt indizieren?</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" align="center">
					M&#x00F6;chten Sie die Daten jetzt gleich indizieren? 
					<br /><br />
					<span style="color:#959595">
					Dies kann abh&#x00E4;ngig von der Menge der Daten einige Zeit dauern. Jedoch haben Sie die M&#x00F6;glichkeit, das Ergebnis unmittelbar zu pr&#x00FC;fen.
					<br /><br />
					Wenn Sie jetzt nicht indizieren wird der Index zu einem sp&#x00E4;teren Zeitpunkt &#x00FC;ber die <i>zeitgesteuerte Neuindizierung</i> erstellt.
					</span>
					</td>
				</tr>
				<tr>
					<td colspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td align="center">
						<form method="post" action="<%=response.encodeURL("indexXml.jsp")%>" name="indexNow">
							<input type="hidden" name="initial" value="true"/>
							<input type="submit" value="Jetzt indizieren"/>
						</form>
					</td>
				</tr>
			</table>
			<br />
		<form method="post" action="<%=response.encodeURL("save.jsp")%>" name="noIndex">
		<table class="table" align="center">					
			<tr align="center">
				<td>
					<input type="button" name="back" value="Zur&#x00FC;ck" onclick="history.back()"/>
				</td>
				<td>
					<input type="button" name="cancel" value="Abbrechen" onclick="window.location.href='<%=response.encodeURL("index.jsp")%>'"/>						
				</td>
				<td>
					<input type="submit" value="Ohne Indizierung weiter" />
				</td>
			</tr>
		</table>
	</form>
	</center>
</body>
<!-- ingrid-iplug-admin-step2-dsc -->
</html>