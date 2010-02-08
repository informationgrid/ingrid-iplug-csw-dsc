<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="de.ingrid.iplug.util.*" %>
<%@ page import="de.ingrid.iplug.web.WebContainer" %>
<%@ page import="de.ingrid.iplug.*" %>
<%@ page import="de.ingrid.ibus.client.*" %>
<%@ page import="java.io.*" %>
<%@ page import="de.ingrid.utils.xml.*" %>
<%@ page import="de.ingrid.utils.PlugDescription" %>
<%@ page import="de.ingrid.utils.messages.*" %>
<%@ page import="de.ingrid.utils.query.*" %>
<%@ page import="de.ingrid.utils.queryparser.*" %>
<%@ page import="de.ingrid.utils.*" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="de.ingrid.utils.IDataSourceConnection" %>
<%@ page import="de.ingrid.utils.datatype.IDataTypeProvider" %>
<%@ page import="de.ingrid.utils.datatype.DataType" %>
<%@ page import="de.ingrid.utils.BeanFactory" %>


<%@ include file="timeoutcheck.jsp"%>
<%!

public void savePortalProviders(final BusClient busClient) {
    try {
    	IBus bus = busClient.getNonCacheableIBus();

    	final String query = "datatype:management management_request_type:2";
		IngridQuery ingridQuery = QueryStringParser.parse(query);
    	IngridHits hits = bus.search(ingridQuery, 1000, 0, 0, 120000);
    	IngridHit hit = hits.getHits()[0];
    	ArrayList providers = hit.getArrayList("provider");
    
    	Properties p = new Properties();
    	for (Iterator iter = providers.iterator(); iter.hasNext();) {
	        HashMap element = (HashMap) iter.next();
        	final String providerId = (String) element.get("providerid");
        	String partnerId = "";
        	if (providerId.indexOf('_') != -1) {
        		partnerId = providerId.substring(0, providerId.indexOf('_'));
        	}
	        String partner = "Anderer";
	        if (partnerId.equals("bu")) {
	            partner = "bund";
	        } else if (partnerId.equals("ni")) {
	            partner = "Niedersachsen";
	        } else if (partnerId.equals("sl")) {
	            partner = "Saarland";
	        } else if (partnerId.equals("bw")) {
	            partner = "Baden-W&#x00FC;rttemberg";
	        } else if (partnerId.equals("be")) {
	            partner = "Berlin";
	        } else if (partnerId.equals("by")) {
	            partner = "Bayern";
	        } else if (partnerId.equals("he")) {
	            partner = "Hessen";
	        } else if (partnerId.equals("hb")) {
	            partner = "Hansestadt-Bremen";
	        } else if (partnerId.equals("hh")) {
	            partner = "Hansestadt-Hamburg";
	        } else if (partnerId.equals("mv")) {
	            partner = "Mecklenburg-Vorpommern";
	        } else if (partnerId.equals("nw")) {
	            partner = "Nordrhein-Westfalen";
	        } else if (partnerId.equals("rp")) {
	            partner = "Rheinland-Pfalz";
	        } else if (partnerId.equals("sh")) {
	            partner = "Schleswig-Holstein";
	        } else if (partnerId.equals("sn")) {
	            partner = "Sachsen";
	        } else if (partnerId.equals("st")) {
	            partner = "Sachsen-Anhalt";
	        } else if (partnerId.equals("th")) {
	            partner = "Th&#x00FC;ringen";
	    	} else if (partnerId.equals("bb")) {
    	        partner = "Brandenburg";
        	}
	        
    	    p.setProperty(partner + '.' + providerId, (String) element.get("name"));
    	}
    
    	java.net.URL url = this.getClass().getClassLoader().getResource("/provider.properties");
        FileOutputStream out = new FileOutputStream(url.getPath(), false);
        p.store(out, "");
        out.close();
    } catch (Exception e) {
        // Ignore this
    }
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Angaben zum Informations-Anbieter</title>
<link href="<%=response.encodeURL("css/admin.css")%>" rel="stylesheet" type="text/css" />
</head>
<body>
<%
boolean submitted = WebUtil.getParameter(request,"submitted", null)!=null;
java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
String myIp = localHost.getHostAddress();
PlugDescription description = (PlugDescription)request.getSession().getAttribute("description");

String organisation = "";
String organisationAbbr =  "";
String personTitle = "";
String personSureName = "";
String personName = "";
String personPhone = "";
String personMail = "";
String dataSourceName = "";
String[] datatypes = new String[]{};
String dataSourceDescription = "";
String iplugAdminGuiUrl = "";
String iplugAdminGuiPort = "8082";
String iplugAdminGuiPassword = "";
String proxyServiceUrl = "/kug-group:<eindeutiger IPlug Name>";
String plugId = "";
String error = "";
String forceRankingOff = "";

BeanFactory beanFactory = (BeanFactory) application.getAttribute("beanFactory");
BusClient busClient = (BusClient) beanFactory.getBean("busClient");
savePortalProviders(busClient);
CategorizedKeys.clear();
CategorizedKeys keys = CategorizedKeys.get("/provider.properties", this.getClass().getClassLoader().getResource("/provider.properties").openStream());

// nothing to load; first setup
if(submitted){
	organisationAbbr =  WebUtil.getParameter(request, "organisationAbbr", "");
	personTitle = WebUtil.getParameter(request, "personTitle", "");
	personSureName = WebUtil.getParameter(request, "personSureName", "");
	personName = WebUtil.getParameter(request, "personName", "");
	personPhone = WebUtil.getParameter(request, "personPhone", "");
	personMail = WebUtil.getParameter(request, "personMail", "");
	dataSourceName = WebUtil.getParameter(request, "dataSourceName", "");
	dataSourceDescription = WebUtil.getParameter(request, "dataSourceDescription", "");
	iplugAdminGuiUrl = WebUtil.getParameter(request, "iplugAdminGuiUrl", "");
	iplugAdminGuiPort = WebUtil.getParameter(request, "iplugAdminGuiPort", "8082");
	iplugAdminGuiPassword = WebUtil.getParameter(request, "iplugAdminGuiPassword", "");
	proxyServiceUrl = WebUtil.getParameter(request, "proxyServiceUrl", "/kug-group:<eindeutiger IPlug Name>");
	plugId = myIp +"-" +System.currentTimeMillis();
	error = WebUtil.getParameter(request, "error", "");
  forceRankingOff = WebUtil.getParameter(request, "forceRankingOff", "");
	datatypes = WebUtil.getParameters(request, "datatype", new String[]{});
}else if(!submitted && description.getOrganisation() != null){
	// we load an existing plugdescription
	organisation = description.getOrganisation();
	organisationAbbr =  description.getOrganisationAbbr();
	personTitle = description.getPersonTitle();
	personSureName = description.getPersonSureName();
	personName = description.getPersonName();
	personPhone = description.getPersonPhone();
	personMail = description.getPersonMail();
	dataSourceName = description.getDataSourceName();
	datatypes = description.getDataTypes();
	plugId = description.getPlugId();
	dataSourceDescription = description.getDataSourceDescription();
	iplugAdminGuiUrl = description.getIplugAdminGuiUrl();
	iplugAdminGuiPort = String.valueOf(description.getIplugAdminGuiPort());
	iplugAdminGuiPassword = description.getIplugAdminPassword();
	proxyServiceUrl = description.getProxyServiceURL();
	error = WebUtil.getParameter(request, "error", "");
  if (description.containsKey("forceAddRankingOff")) {
        if (description.getBoolean("forceAddRankingOff")) {
            forceRankingOff = "checked=\"checked\"";
        }
	}
}

if (!WebUtil.getParameter(request, "organisationAbbr", "").equals("")
		&& !WebUtil.getParameter(request, "personSureName", "").equals("") && !WebUtil.getParameter(request, "personName", "").equals("")
		&& !WebUtil.getParameter(request, "personPhone", "").equals("") && !WebUtil.getParameter(request, "personMail", "").equals("")
		&& !WebUtil.getParameter(request, "iplugAdminGuiPassword", "").equals("") && !WebUtil.getParameter(request, "iplugAdminGuiPort", "").equals("")
		&& !WebUtil.getParameter(request, "dataSourceName", "").equals("") && !WebUtil.getParameter(request, "proxyServiceUrl", "").equals("")
		&& !WebUtil.getParameter(request, "iplugAdminGuiUrl", "").equals("") 
		&& !(WebUtil.getParameters(request, "datatype", null)==null)
		&& WebUtil.getParameter(request, "iplugAdminGuiPort", "").matches("^\\p{Digit}{1,5}$")
		){
	
	organisation = keys.getString(organisationAbbr);
	
	description.setOrganisation(organisation);
	description.setOrganisationAbbr(organisationAbbr);
	description.remove("provider"); // removing old providers
	description.addProvider(organisationAbbr);
	description.setPersonTitle(personTitle);
	description.setPersonSureName(personSureName);
	description.setPersonName(personName);
	description.setPersonPhone(personPhone);
	description.setPersonMail(personMail);
	description.setDataSourceName(dataSourceName);
	description.setDataSourceDescription(dataSourceDescription);
	description.setProxyServiceURL(proxyServiceUrl);


	description.remove("ranking");
  if (forceRankingOff.isEmpty()) {
	    description.setRankinTypes(true, false, false);
	    description.putBoolean("forceAddRankingOff", false);
	} else {
	    description.setRankinTypes(true, false, true);
	    description.putBoolean("forceAddRankingOff", true);
	}
	description.remove("dataType"); // removing old datatypes.
	IDataTypeProvider datatypeProvider = (IDataTypeProvider) beanFactory.getBean("dataTypeProvider");
	for(int i = 0; i < datatypes.length; i++) {
		description.addDataType(datatypes[i]);
		DataType[] includedDatatypes = datatypeProvider.getIncludedDataTypes(datatypes[i]);
		for(int j = 0; j < includedDatatypes.length; j++) {
			description.addDataType(includedDatatypes[j].getName());
		}
	} 
	description.setIplugAdminGuiUrl(iplugAdminGuiUrl);
	description.setIplugAdminPassword(iplugAdminGuiPassword.trim());
	description.setIplugAdminGuiPort(Integer.parseInt(iplugAdminGuiPort));
	
	// frist we remove the old fields.
	description.remove("fields");
	description.addField("incl_meta");	
	// preset fields to identify this iplug as IGC DB 1.0.3 compatible
	description.addField("t01_object.obj_id");
	description.addField("parent.object_node.obj_uuid");
	description.addField("object_access.terms_of_use");
	
	// generate this dynamic
	description.setPlugId(plugId);
	description.setIPlugClass("de.ingrid.iplug.csw.dsc.index.DSCSearcher");
	response.sendRedirect(response.encodeRedirectURL("addPartner.jsp"));
}
%>

<center>
	<div class="headline"><br />
		Angaben zu Betreiber und Datenquelle
		<br />
		<br />
		<span class="byline">Mit * gekennzeichnete Felder sind optional.</span>		
	</div>
<br />
	<form method="get" action="<%=response.encodeURL("providerMetadata.jsp")%>">
		<table class="table" align="center">
				<tr align="center">
						<td>
								<input type="button" name="back" value="Zur&#x00FC;ck" onclick="history.back()"/>
						</td>
						<td>
								<input type="button" name="cancel" value="Abbrechen" onclick="window.location.href='<%=response.encodeURL("../step1/index.jsp")%>'"/>						
						</td>
						<td>
								<input type="hidden" name="submitted" value="true">
								<input type="submit" value="Weiter" />
						</td>
					</tr>
		</table>
		<br />
<%if (!error.equals("")) { %>
	<div class="error">Angaben sind nicht vollst&#x00E4;ndig!</div>
	<br />
<%} %>
		<input type="hidden" name="error" value="missingParameter"/>
		<table class="table" width="400" align="center">
			<tr>
				<td colspan="2" class="tablehead">Allgemeine Angaben zum Betreiber</td>
			</tr>
			<!-- TODO check -->
			<!-- <tr>
				<td class="tablecell" width="100">Name des Anbieters:</td>
				<td class="tablecell">
					<input type="text" name="organisation" value="<%//=organisation %>" style="width:100%"/>					
				</td>
			</tr> -->
			<tr>
				<td class="tablecell" width="100">Name des Anbieters:</td>
				<td class="tablecell">					
					<select name="organisationAbbr">
						<option value="">bitte w&#x00E4;hlen</option>
						<%
						String[] categories = keys.getCategories();
						for (int i=0; i < categories.length; i++) {
							String category = categories[i];						
							Map map = de.ingrid.utils.messages.SortedCategorizedKeys.getSortedCategorizedKeys(keys, category);
							java.util.Set keySet = map.keySet();
							java.util.Iterator iter = keySet.iterator();
							%>
							<optgroup label="<%=category%>">
							<%
							while(iter.hasNext()) {
							String key = (String) iter.next();
							String value = (String) map.get(key);				
							%>					
								<option value="<%=value%>" <%if(organisationAbbr !=null && !organisationAbbr.equals("") && value.equals(organisationAbbr)){ %>selected<%}%>><%=key %></option>
							<%} %>
							</optgroup>
						<%} %>
					</select>
					<!-- <input type="text" name="organisationAbbr" value="<%//=organisationAbbr %>" style="width:100%"/>	-->				
				</td>
			</tr>
			<tr>
				<td colspan="2" class="tablehead">Ansprechpartner</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Titel(*):</td>
				<td class="tablecell">
					<input type="text" name="personTitle" value="<%=personTitle%>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Nachname:</td>
				<td class="tablecell">
					<input type="text" name="personSureName" value="<%=personSureName%>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Vorname:</td>
				<td class="tablecell">
					<input type="text" name="personName" value="<%=personName%>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Tel.:</td>
				<td class="tablecell">
					<input type="text" name="personPhone" value="<%=personPhone%>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Email:</td>
				<td class="tablecell">
					<input type="text" name="personMail" value="<%=personMail%>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td colspan="2" class="tablehead">Datenquelle</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Name der Datenquelle:</td>
				<td class="tablecell">
					<input type="text" name="dataSourceName" value="<%=dataSourceName %>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Kurzbeschreibung(*):</td>
				<td class="tablecell">
					<textarea name="dataSourceDescription" style="width:100%; height:100px;"><%=dataSourceDescription %></textarea>					
				</td>				
			</tr>
			<tr>
				<td class="tablecell" width="100">Art der Datenquelle:</td>
				<td class="tablecell">
				
				<%
					IDataTypeProvider datatypeProvider = (IDataTypeProvider) beanFactory.getBean("dataTypeProvider");
					DataType[] datatypesObjects = datatypeProvider.getDataTypes();
					for(int i=0; i<datatypesObjects.length; i++) {
						DataType dataTypeObject = datatypesObjects[i];
						if( dataTypeObject.get("hidden").contains("true")) {
							continue;
						}
						String checked = "";
						for(int j=0; j<datatypes.length;j++) {
							if(datatypes[j].equals(dataTypeObject.getName())) {
								checked="checked=\"checked\"";
								break;
							}
						}
					%>
						<input type="checkbox" name="datatype" value="<%=dataTypeObject.getName()%>" <%=checked%> /> <%=dataTypeObject.getDisplayName()%> <br />
						<%
					}
				%>
				</td>
			</tr>
      <tr>
          <td class="tablecell" width="100">Als Nebenergebnis anzeigen:</td>
          <td class="tablecell">
              <input type="checkbox" name="forceRankingOff" value="checked" <%=forceRankingOff%> /> <br />
          </td>
      </tr>
			<tr>
				<td colspan="2" class="tablehead">iPlug</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Adresse des iPlugs (/&lt;Gruppen Name&gt;:&lt;IPlug Name&gt;)</td>
				<td class="tablecell">
					<input type="text" name="proxyServiceUrl" value="<%=proxyServiceUrl %>" style="width:100%"/>					
				</td>
			</tr>			
			<tr>
				<td colspan="2" class="tablehead">Administrationsinterface</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">URL:</td>
				<td class="tablecell">
					<input type="text" name="iplugAdminGuiUrl" value="<%=iplugAdminGuiUrl%>" style="width:100%"/>					
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Port:</td>
				<td class="tablecell">
					<input type="text" name="iplugAdminGuiPort" value="<%=iplugAdminGuiPort%>" style="width:100%"/>	
				</td>
			</tr>
			<tr>
				<td class="tablecell" width="100">Administrationskennwort:</td>
				<td class="tablecell">
					<input type="<% if (iplugAdminGuiPassword.trim().equals("")) { %>text<% } else { %>password<% } %>" name="iplugAdminGuiPassword" value="<%=iplugAdminGuiPassword%>" style="width:100%"/>
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
								<input type="hidden" name="submitted" value="true">
								<input type="submit" value="Weiter" value="true"/>
						</td>
					</tr>
		</table>
		<!-- ingrid-iplug-admin-step1 -->
	</form>
</center>
</body>
</html>