<%@ include file="/WEB-INF/jsp/base/include.jsp" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page import="de.ingrid.admin.security.IngridPrincipal"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="de">
<head>
<title><fmt:message key="CswConfig.main.configuration"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="description" content="" />
<meta name="keywords" content="" />
<meta name="author" content="wemove digital solutions" />
<meta name="copyright" content="wemove digital solutions GmbH" />
<link rel="StyleSheet" href="../css/base/portal_u.css" type="text/css" media="all" />

</head>
<body>
    <div id="header">
        <img src="../images/base/logo.gif" width="168" height="60" alt="Portal U" />
        <h1><fmt:message key="CswConfig.main.configuration"/></h1>
        <%
          java.security.Principal  principal = request.getUserPrincipal();
          if(principal != null && !(principal instanceof IngridPrincipal.SuperAdmin)) {
        %>
            <div id="language"><a href="../base/auth/logout.html"><fmt:message key="CswConfig.main.logout"/></a></div>
        <%
          }
        %>
    </div>
    <div id="help"><a href="#">[?]</a></div>

    <c:set var="active" value="cswParams" scope="request"/>
    <c:import url="../base/subNavi.jsp"></c:import>

    <div id="contentBox" class="contentMiddle">
        <h1 id="head">CSW Parameter</h1>
        <div class="controls">
            <a href="../base/extras.html">Zur&uuml;ck</a>
            <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('cswConfig').submit();">Weiter</a>
        </div>
        <div class="controls cBottom">
            <a href="../base/extras.html">Zur&uuml;ck</a>
            <a href="../base/welcome.html">Abbrechen</a>
            <a href="#" onclick="document.getElementById('cswConfig').submit();">Weiter</a>
        </div>
        <div id="content">
            <form:form method="post" action="cswParams.html" modelAttribute="cswConfig">
                <input type="hidden" name="action" value="submit" />
                <input type="hidden" name="id" value="" />
                <table id="konfigForm">
                    <br />
                    <tr>
                        <td colspan="2"><h3><fmt:message key="CswConfiguration.serviceUrl.chooseParameter"/></h3></td>
                    </tr>
                    <tr>
                        <td class="leftCol">Service Url</td>
                        <td>
                            <div class="input full"><form:input path="serviceUrl" /></div>
                            <form:errors path="serviceUrl" cssClass="error" element="div" />
                            <br />
                            <fmt:message key="CswConfiguration.serviceUrl.help"/>
                            <p style="color: gray;"><fmt:message key="CswConfiguration.serviceUrl.help.example"/></p>
                        </td>
                    </tr>
                </table>
            </form:form>
        </div>
    </div>

    <div id="footer" style="height:100px; width:90%"></div>
</body>
</html>

