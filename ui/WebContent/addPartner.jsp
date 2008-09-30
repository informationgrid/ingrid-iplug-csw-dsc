<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ page import="de.ingrid.iplug.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Iterator"%>
<%@ include file="timeoutcheck.jsp"%>
<%!

public boolean containsPartner(String partnerKey, String[] partners){
	for(int i = 0; i < partners.length; i++){
		if(partnerKey.equals(partners[i])){
			return true;
		}
	}
	return false;
}

%>

<%
// build partner hashmap
		HashMap partnerMap = new HashMap();
		partnerMap.put("bund", "Bund");
        partnerMap.put("bb", "Brandenburg");
        partnerMap.put("be", "Berlin");
        partnerMap.put("bw", "Baden-W&#x00FC;rttemberg");
        partnerMap.put("by", "Bayern");
        partnerMap.put("hb", "Bremen");
        partnerMap.put("he", "Hessen");
        partnerMap.put("hh", "Hamburg");
        partnerMap.put("mv", "Mecklenburg-Vorpommern");
        partnerMap.put("ni", "Niedersachsen");
        partnerMap.put("nw", "Nordrhein-Westfalen");
        partnerMap.put("rp", "Rheinland-Pfalz");
        partnerMap.put("sh", "Schleswig-Holstein");
        partnerMap.put("sl", "Saarland");
        partnerMap.put("sn", "Sachsen");
        partnerMap.put("st", "Sachsen-Anhalt");
        partnerMap.put("th", "Th&#x00FC;ringen");
        
String error = "";

            String action = WebUtil.getParameter(request, "action", "");
            String partner = WebUtil.getParameter(request, "partner", "");
            PlugDescription description = (PlugDescription) request.getSession().getAttribute("description");

            if (action.equals("forward")) {
                if (description.getPartners().length < 1) {
                    error = "Bitte f&#x00FC;gen Sie mindestens einen Partner hinzu.";
                } else {
                    response.sendRedirect(response.encodeRedirectURL("addIbus.jsp"));
                }
            } else if (action.equals("add")) {
                if (partner.length() == 0) {
                    error = "Sie m&#x00FC;ssen eine der angegebenen Partner ausw&#x00E4;hlen.";
                } else {
                    description.addPartner(partner);
                }
            } else if (action.equals("delete")) {
	            int partnerIndex=Integer.parseInt(WebUtil.getParameter(request,"partnerIndex",""));
	            String[] allPartner=description.getPartners();
                description.removeFromList(PlugDescription.PARTNER, allPartner[partnerIndex]);
            }

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
	<head>
		<title>Partner hinzuf&#x00FC;gen</title>
		<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
	</head>

	<body>
		<center>
			<div class="headline">
				<br />Hinzuf&#x00FC;gen der Partner
				<br /><br />
				<span class="byline">Bitte geben Sie mindestens einen Partner an, f&#x00FC;r den diese Datenquelle konfiguriert wird.</span>
			</div>
			<br />			
			<!-- 
			<table class="table" align="center">
				<tr align="center">
					<td colspan="2" align="center">
						<form method="post" action="<%=response.encodeURL("providerMetadata.jsp")%>">
							<input type="button" value="Zur&#x00FC;ck" name="back" onclick="history.back()"/>
						</form>
						<form method="post" action="<%=response.encodeURL("index.jsp")%>">
							<input type="button" value="Abbrechen" name="cancel" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>						
						</form>
						<form method="post" action="<%=response.encodeURL("addPartner.jsp")%>">
							<input type="hidden" name="action" value="add"/>
							<input type="submit" value="Weiter" name="cont"/>
						</form>
					</td>
				</tr>
			</table>			
			-->
			<%if (error.length() > 0) {%>
			<div class="error">
				<%=error%>
			</div>
			<br />
			<%}%>
			<table class="table" width="400" align="center">
				<tr>
					<td class="tableHead" colspan="2">
						Partner hinzuf&#x00FC;gen
					</td>
				</tr>
				<tr>
					<td class="tableCell" width="100">
						Partner:
					</td>
					<td class="tableCell">
					<br />
						<%
						String[] partners = description.getPartners();
			            for (int i = 0; i < partners.length; i++) {%>
						<form method="post" action="">
							<b><%=i + 1%>. Partner:</b>
							<%=partnerMap.get(partners[i])%>
							<input type="hidden" name="partnerIndex" value="<%=i%>" />
							<input type="hidden" name="action" value="delete" />
							<input type="image"  src="<%=response.encodeURL("gfx/delete.gif")%>" />
						</form>
						<%}%>
						<br />
						<form method="post" action="">
							<select name="partner" style="width:100%">
							<option value="">bitte w&#x00E4;hlen</option>
							<% 
							java.util.Iterator iterator = partnerMap.keySet().iterator();
							while (iterator.hasNext()) {
								String key = (String) iterator.next();
								String value = (String)partnerMap.get(key);
								if(!containsPartner(key, partners)){
								%>
								<option value="<%=key%>" ><%=value %></option>
								<% 
								}
							}
							%>
							</select>					
							<br />
							<br />
							<input type="hidden" name="action" value="add" />							
							<input type="submit" value="Hinzuf&#x00FC;gen" />
						</form>
					</td>
				</tr>
			</table>
			<br />
			<table class="table" align="center">
				<tr align="center">
						<form method="post" action="<%=response.encodeURL("addPartner.jsp")%>">
							<td>
								<input type="button" value="Zur&#x00FC;ck" name="back" onclick="history.back()"/>
							</td>
							<td>
								<input type="button" value="Abbrechen" name="cancel" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>						
							</td>
							<td>
								<input type="hidden" name="action" value="forward" />
							</td>
							<td>
								<input type="submit" value="Weiter" name="cont"/>
							</td>
						</form>
				</tr>
			</table>
		</center>
	</body>
	<!-- ingrid-iplug-admin-step1 -->
</html>
