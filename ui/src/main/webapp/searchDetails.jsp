<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ page import="de.ingrid.utils.*"%>
<%@ page import="de.ingrid.utils.messages.*"%>
<%@ page import="de.ingrid.iplug.csw.dsc.index.*"%>
<%@ page import="java.util.HashSet"%>
<%@ page import="de.ingrid.utils.dsc.*"%>
<%@ page import="de.ingrid.utils.PlugDescription"%>
<%@ include file="timeoutcheck.jsp"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Details</title>
</head>
<body>



<%!
public String renderDetails (Record record, StringBuffer buffer)throws Exception{
	
	CategorizedKeys catKeys = CategorizedKeys.get("/indexFieldNames.properties");
	int num = record.numberOfColumns();
	for(int i=0; i<num; i++){
		Column column = record.getColumn(i);
		String fieldName = catKeys.getString(column.getTargetName());
		// custom field names
		if(fieldName == null){
			fieldName = column.getTargetName();
		}

		if(column.toIndex()){
			buffer.append("<tr>");
				buffer.append("<td bgcolor=\"#F4F4F4\" style=\"font-face:Arial;font-size:12pt\">");
					buffer.append(fieldName+":");
				buffer.append("</td>");
				buffer.append("<td bgcolor=\"#F4F4F4\" style=\"font-face:Arial;font-size:12pt\">");
					buffer.append(record.getValueAsString(column)+"&nbsp;");
				buffer.append("</td>");
			buffer.append("</tr>");
		}		
	}	
	
	Record[] subRecords = record.getSubRecords();
	
	if(subRecords != null && subRecords.length > 0){		
		for(int j=0; j < subRecords.length; j++){
			buffer.append("<tr><td colspan=\"2\">&nbsp;</td></tr>");
			renderDetails(subRecords[j], buffer);
		}		
	}
	
	return	buffer.toString();
}
%>



<%
//parameter
String plugId = "";
	if (request.getParameter("plugId")!= null){
		plugId = request.getParameter("plugId");
	}
int docId = 0;
	if (request.getParameter("docId")!= null){
		docId = Integer.parseInt(request.getParameter("docId"));
	}

// IngridHit	
IngridHit hit = new IngridHit();
hit.setDocumentId(docId);
hit.setPlugId(plugId);

PlugDescription   description = (PlugDescription)  request.getSession().getAttribute("description");
de.ingrid.iplug.csw.dsc.index.DSCSearcher searcher = new de.ingrid.iplug.csw.dsc.index.DSCSearcher();
searcher.configure(description);
Record record = searcher.getRecord(hit);


StringBuffer buffer = new StringBuffer();
%>
	
	<br />
	<table cellpadding="2" cellspacing="2" style="border:1px solid #959595" align="center">
		<%=renderDetails(record, buffer) %>
	</table>	

</body>
</html>