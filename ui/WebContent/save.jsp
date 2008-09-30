<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.PlugDescription" %>
<%@ page import="de.ingrid.utils.xml.*" %>    
<%@ page import="de.ingrid.iplug.*"%>
<%@ page import="java.io.*"%>
<%@ page import="de.ingrid.iplug.util.*"%>
<%@ page import="de.ingrid.utils.BeanFactory"%>

<%@ include file="timeoutcheck.jsp"%>
<%
PlugDescription  description = (PlugDescription) request.getSession().getAttribute("description");
BeanFactory beanFactory = (BeanFactory) application.getAttribute("beanFactory");
File pd_file = (File) beanFactory.getBean("pd_file");
XMLSerializer serializer = new XMLSerializer();
if (null == description) {
	System.out.println("step1/save.jsp: ERROR in step1/save.jsp: current values lost during session timeout, plugdescription from web container was <null> and was not written");
} else {
    serializer.serialize(description,pd_file);
}

response.sendRedirect(response.encodeRedirectURL("/step1/index.jsp"));
%>
