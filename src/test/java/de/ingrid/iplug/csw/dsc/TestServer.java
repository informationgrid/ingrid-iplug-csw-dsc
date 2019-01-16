/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
	
	PORTALU ("https://dev.informationgrid.eu/csw", "https://dev.informationgrid.eu/csw", 
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
