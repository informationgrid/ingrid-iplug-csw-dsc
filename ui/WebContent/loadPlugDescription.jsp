<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="java.io.*"%>
<%@ page import="de.ingrid.utils.xml.*"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ page import="de.ingrid.utils.BeanFactory"%>
<%@ include file="timeoutcheck.jsp"%>
<%
// get plug description from class path
PlugDescription description;
BeanFactory beanFactory = (BeanFactory) application.getAttribute("beanFactory");
File pd_file = (File) beanFactory.getBean("pd_file");

if(pd_file.exists()){
	InputStream in = new FileInputStream(pd_file);
	
	XMLSerializer serializer = new XMLSerializer();
	serializer.aliasClass(PlugDescription.class.getName(), PlugDescription.class );
	description = (PlugDescription)serializer.deSerialize(in);		
} else {
	description = new PlugDescription();
}
request.getSession().setAttribute("description", description);

if(pd_file.exists()){
	response.sendRedirect(response.encodeRedirectURL("selectEditMode.jsp"));       	
} else {
	response.sendRedirect(response.encodeRedirectURL("selectWorkingFolder.jsp"));
}
%>
