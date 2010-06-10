<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.List"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ page import="de.ingrid.iplug.util.WebUtil"%>
<%@ page import="de.ingrid.utils.QueryExtension"%>
<%@ page import="de.ingrid.utils.QueryExtensionContainer"%>
<%@ page import="de.ingrid.utils.query.FieldQuery"%>
<%@ include file="timeoutcheck.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
PlugDescription  description = (PlugDescription) request.getSession().getAttribute("description");
String[] busUrls = description.getBusUrls();

String action = WebUtil.getParameter(request, "action", "");
String busUrl = request.getParameter("busUrl");
String regex = WebUtil.getParameter(request, "regex", ".*");
regex.trim();
String key = WebUtil.getParameter(request, "key", "");
key.trim();
String value = WebUtil.getParameter(request, "value", "");
value.trim();
String option = request.getParameter("option");

String error = "";

if (action.equals("forward")) {
  response.sendRedirect(response.encodeRedirectURL("cswInfos.jsp"));
}

if (request.getParameter("add") != null) {
    if (regex.isEmpty()) error += "Bitte geben Sie einen Regex an.<br />";
    if (key.isEmpty()) error += "Bitte vergeben Sie dem Index Feld einen Namen.<br />";
    if (value.isEmpty()) error += "Bitte vergeben Sie dem Index Feld einen Wert.<br />";
    
    if (error.isEmpty()) {
        final boolean required = "required".equals(option);
        final boolean prohibited = "prohibited".equals(option);
        
        // get container
        QueryExtensionContainer container = description.getQueryExtensionContainer();
        if (null == container) {
            // create container
            container = new QueryExtensionContainer();
            description.setQueryExtensionContainer(container);
        }
        // get extension
        QueryExtension extension = container.getQueryExtension(busUrl);
        if (null == extension) {
            // create extension
            extension = new QueryExtension();
            extension.setBusUrl(busUrl);
            container.addQueryExtension(extension);
        }
        // create field query
        final Pattern pattern = Pattern.compile(regex);
        final FieldQuery fq = new FieldQuery(required, prohibited, key, value);
        extension.addFieldQuery(pattern, fq);
        
        // reset
        regex = ".*";
        key = "";
        value = "";
    }
} else if (request.getParameter("delete") != null) {
    final String delBus = request.getParameter("delBus");
    final String delRegex = request.getParameter("delRegex");
    final String delKey = request.getParameter("delKey");
    final String delValue = request.getParameter("delValue");
    final String delOption = request.getParameter("delOption");
    final boolean delProhibited = "prohibited".equals(delOption);
    final boolean delRequired ="required".equals(delOption);
    // get extension
    final QueryExtension extension = description.getQueryExtensionContainer().getQueryExtension(delBus);
    final Set fieldQueries = extension.getFieldQueries(delRegex);
    // find correct field query
    final Iterator fqIter = fieldQueries.iterator();
    while (fqIter.hasNext()) {
        final FieldQuery fq = (FieldQuery) fqIter.next();
        if (fq.getFieldName().equals(delKey) && fq.getFieldValue().equals(delValue) && (fq.isProhibited() == delProhibited) && (fq.isRequred() == delRequired)) {
            // delete it
            fqIter.remove();
            break;
        }
    }
}

%>
<html>
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
                        <input type="hidden" name="action" value="forward" />
                        <input type="submit" name="save" value="Weiter"/>  
                    </td>
                </tr>
        </table>
    </form>

    <% if (!error.isEmpty()) { %>
    <div class="error"><%=error%></div>
    <%}%>

    <form method="post" action="addFieldQuery.jsp">
        <table class="table">
            <tr>
                <td class="tableHead">Bus-Url</td>
                <td class="tableHead">Regex</td>
                <td class="tableHead">Index Feld Name</td>
                <td class="tableHead">Index Feld Wert</td>
                <td class="tableHead">Verboten</td>
                <td class="tableHead">Erforderlich</td>
                <td class="tableHead">Hinzuf&uuml;gen</td>
            </tr>
            <tr>
                <td class="tableCell">
                    <select name="busUrl">
                        <% for (int i = 0; i < busUrls.length; i++) { %>
                        <option value="<%=busUrls[i] %>" <% if (busUrls[i].equals(busUrl)) { %><%= "selected=\"selected\"" %><%} %>><%=busUrls[i] %></option>
                        <% } %>
                    </select>
                </td>
                <td class="tableCell"><input type="text" name="regex" value="<%= regex %>"/></td>
                <td class="tableCell"><input type="text" name="key" value="<%= key %>"/></td>
                <td class="tableCell"><input type="text" name="value" value="<%= value %>"/></td>
                <td class="tableCell"><input type="radio" name="option" value="prohibited" <% if (!"required".equals(option)) {%><%= "checked=\"checked\"" %><%} %>/></td>
                <td class="tableCell"><input type="radio" name="option" value="required" <% if ("required".equals(option)) {%><%= "checked=\"checked\"" %><%} %>/></td>
                <td class="tableCell"><input type="submit" name="add" value="Hinzuf&uuml;gen" /></td>
            </tr>
        </table>
    </form>
    
    <%
    Map extensions = description.getQueryExtensions();
    if (extensions != null && extensions.size() > 0) {
    %>
    <table class="table">
        <tr>
            <td class="tableHead">Bus-Url</td>
            <td class="tableHead">Regex</td>
            <td class="tableHead">Index Feld Name</td>
            <td class="tableHead">Index Feld Wert</td>
            <td class="tableHead">Option</td>
            <td class="tableHead">L&ouml;schen</td>
        </tr>
        <%
        final Iterator busIter = extensions.keySet().iterator();
        while (busIter.hasNext()) {
            final String bus = (String) busIter.next();
            final QueryExtension extension = (QueryExtension) extensions.get(bus);
            final Set patterns = extension.getPatterns();
            if (patterns != null) {
                final Iterator patternIter = patterns.iterator();
                while (patternIter.hasNext()) {
                    final Pattern pattern = (Pattern) patternIter.next();
                    final Set fql = extension.getFieldQueries(pattern);
                    if (fql != null) {
                        final Iterator fqIter = fql.iterator();
                        while (fqIter.hasNext()) {
                            final FieldQuery fq = (FieldQuery) fqIter.next();
                            %>
        <tr>
            <td class="tableCell"><%=bus %></td>
            <td class="tableCell"><%=pattern.pattern() %></td>
            <td class="tableCell"><%=fq.getFieldName() %></td>
            <td class="tableCell"><%=fq.getFieldValue() %></td>
            <td class="tableCell"><% if (fq.isProhibited()) {%><%="verboten" %><%} else if (fq.isRequred()) {%><%="erforderlich" %><%} %></td>
            <td class="tableCell">
                <form method="post" action="addFieldQuery.jsp">
                    <input type="hidden" name="delBus" value="<%=bus %>" />
                    <input type="hidden" name="delRegex" value="<%=pattern.pattern() %>" />
                    <input type="hidden" name="delKey" value="<%=fq.getFieldName() %>" />
                    <input type="hidden" name="delValue" value="<%=fq.getFieldValue() %>" />
                    <input type="hidden" name="delOption" value="<% if (fq.isProhibited()) {%><%="prohibited" %><%} else {%><%="required" %><%}%>" />
                    <input type="submit" name="delete" value="L&ouml;schen" />
                </form>
            </td>
        </tr>
                            <%
                        }
                    }
                }
            }
        }
        %>
    </table>
    <%
    }
    %>
    
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
                        <input type="hidden" name="action" value="forward" />
                        <input type="submit" name="save" value="Weiter"/>  
                    </td>
                </tr>
        </table>
    </form>
    </center>
</body>
</html>