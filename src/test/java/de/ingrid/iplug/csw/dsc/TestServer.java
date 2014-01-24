/*
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc;

import de.ingrid.iplug.csw.dsc.cswclient.CSWQuery;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.cswclient.constants.Namespace;
import de.ingrid.iplug.csw.dsc.cswclient.constants.OutputFormat;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ResultType;
import de.ingrid.iplug.csw.dsc.cswclient.constants.TypeName;

public enum TestServer {
	
	PORTALU ("http://www.portalu.de/csw202/", "http://www.portalu.de/csw202", 
			Namespace.CSW_2_0_2, Namespace.CSW_PROFILE,
			OutputFormat.TEXT_XML, "2.0.2", new TypeName[] { TypeName.MD_METADATA }, ResultType.RESULTS,
			ElementSetName.FULL, "1.1.0"),

	DISY ("http://demo.disy.net/preludio2.lubw/ws/csw", "http://demo.disy.net/preludio2.lubw/ws/csw", 
			Namespace.CSW, Namespace.CSW_PROFILE,
			OutputFormat.APPLICATION_XML, "2.0.1", new TypeName[] { TypeName.RECORD }, ResultType.RESULTS,
			ElementSetName.FULL, "1.0.0"),

	SDISUITE ("http://gdi-de.sdisuite.de/soapServices/CSWStartup", "http://gdi-de.sdisuite.de/soapServices/services/CSWDiscovery", 
			Namespace.CSW_2_0_2, Namespace.CSW_PROFILE,
			OutputFormat.TEXT_XML, "2.0.2", new TypeName[] { TypeName.RECORD }, ResultType.RESULTS,
			ElementSetName.FULL, "1.0.0");

	private String capUrlGet;
	private String capUrlSoap;
	private Namespace schema;
	private Namespace outputSchema;
	private OutputFormat outputFormat;
	private String version;
	private TypeName[] typeNames;
	private ResultType resultType;
	private ElementSetName elementSetName;
	private String constraintLanguageVersion;

	TestServer(String capUrlGet, String capUrlSoap, Namespace schema, Namespace outputSchema,
			OutputFormat outputFormat, String version, TypeName[] typeNames, ResultType resultType,
			ElementSetName elementSetName, String constraintLanguageVersion) {
		
		this.capUrlGet = capUrlGet;
		this.capUrlSoap = capUrlSoap;
		this.schema = schema;
		this.outputSchema = outputSchema;
		this.outputFormat = outputFormat;
		this.version = version;
		this.typeNames = typeNames;
		this.resultType = resultType;
		this.elementSetName = elementSetName;
		this.constraintLanguageVersion = constraintLanguageVersion;
	}
	
	public CSWQuery getQuery(CSWQuery query) {
		query.setSchema(this.schema);
		query.setOutputSchema(this.outputSchema);
		query.setOutputFormat(this.outputFormat);
		query.setVersion(this.version);
		query.setTypeNames(this.typeNames);
		query.setResultType(this.resultType);
		query.setElementSetName(this.elementSetName);
		query.setConstraintLanguageVersion(this.constraintLanguageVersion);
		return query;
	}
	
	public String getCapUrlGet()
	{
		return this.capUrlGet;
	}

	public String getCapUrlSoap()
	{
		return this.capUrlSoap;
	}
}
