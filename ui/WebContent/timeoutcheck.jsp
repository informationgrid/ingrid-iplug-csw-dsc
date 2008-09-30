<%
if (request.getSession().isNew()) {
	System.out.println("REDIRECT to step1: current values lost during session timeout, plugdescription from web container was <null>");
    response.sendRedirect(response.encodeRedirectURL("timeout.jsp"));
	return;
}
%>