<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ include file="timeoutcheck.jsp"%>
<%
	request.getSession().invalidate();
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Konfiguation abgeschlossen</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
<center>
	<div class="headline"><br />
		Konfiguration abgeschlossen
		<br /><br />
		<span class="byline">
			Ihr Datasource Client ist jetzt fertig eingerichtet. Sie k&#x00F6;nnen Ihren Browser schlie&#x00DF;en, um sich auszuloggen. <br />
		</span>
	</div>
	<br />
	<form method="post" action="<%=response.encodeURL("index.jsp")%>">
	<table class="table" align="center">
		<tr align="center">
			<td>
				<input type="submit" value="Weiter" />
			</td>
		</tr>
	</table>
	</form>
	</center>
</body>
</html>