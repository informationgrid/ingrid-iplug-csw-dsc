/**
 * CSW 2.0.2 AP ISO 1.0 Record (full) to Lucene Document mapping according to mapping IGC 1.0.3
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 *
 * The following global variable are passed from the application:
 *
 * @param cswRecord A CSWRecord instance, that defines the input
 * @param document A lucene Document instance, that defines the output
 * @param log A Log instance
 *
 */
importPackage(Packages.org.apache.lucene.document);
importPackage(Packages.de.ingrid.iplug.csw.dsc.tools);
importPackage(Packages.de.ingrid.iplug.csw.dsc.index);
importPackage(Packages.de.ingrid.utils.udk);
importPackage(Packages.org.w3c.dom);



var _store = true;
var _index = true;
var _token = true;

log.debug("Mapping csw record "+cswRecord.getId()+" to lucene document");

// get the xml content of the record
var recordNode = cswRecord.getOriginalResponse();

// define one-to-one mappings
/**  each entry consists off the following possible values:
	
	indexField: The name of the field in the index the data will be put into.
	     xpath: The xpath expression for the data in the XML input file. Multiple xpath 
	     		results will be put in the same index field.
	 transform: The transformation to be executed on the value
	  		     funct: The transformation function to use.
	   		    params: The parameters for the transformation function additional to the value 
	  		            from the xpath expression that is always the first parameter. 
	   execute: The function to be executed. No xpath value is obtained. Instead the recordNode of the 
				source XML is put as default parameter to the function. All other parameters are ignored.
	  	         funct: The function to execute.
	  	        params: The parameters for the function additional to the recordNode 
	  		            that is always the first parameter.
	 tokenized: If set to false no tokenizing will take place before the value is put into the index.
*/
var transformationDescriptions = [
		{	"indexField":"t01_object.obj_id",
			"xpath":"//fileIdentifier/CharacterString"
		}, 
		{	"indexField":"title",
			"tokenized":false,
			"xpath":"//identificationInfo//citation/CI_Citation/title/CharacterString"
		},
		{	"indexField":"t01_object.org_obj_id",
			"xpath":"//fileIdentifier/CharacterString"
		},
		{	"indexField":"summary",
			"xpath":"//identificationInfo//abstract/CharacterString"
		},
		{	"indexField":"t01_object.info_note",
			"xpath":"//identificationInfo//purpose/CharacterString"
		},
		{	"indexField":"t01_object.loc_descr",
			"xpath":"//identificationInfo//EX_Extent/description/CharacterString"
		},
		{	"indexField":"t01_object.dataset_alternate_name",
			"xpath":"//identificationInfo//citation/CI_Citation/alternateTitle/CharacterString"
		},
		{	"indexField":"t01_object.time_status",
			"xpath":"//identificationInfo//status/MD_ProgressCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[523]
			}
		},
		{	"indexField":"t01_object.obj_class",
			"xpath":"//hierarchyLevel/MD_ScopeCode/@codeListValue",
			"transform":{
				"funct":getObjectClassFromHierarchyLevel
			}
		},
		{	"indexField":"t01_object.dataset_character_set",
			"xpath":"//identificationInfo//characterSet/MD_CharacterSetCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[510]
			}
		},
		{	"indexField":"t01_object.dataset_usage",
			"xpath":"//identificationInfo//resourceSpecificUsage/MD_Usage/specificUsage/CharacterString"
		},
		{	"indexField":"t01_object.data_language_code",
			"xpath":"//identificationInfo//language/CharacterString",
			"transform":{
				"funct":transformISO639_2ToISO639_1
			}
		},
		{	"indexField":"t01_object.metadata_character_set",
			"xpath":"//characterSet/MD_CharacterSetCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[510]
			}
		},
		{	"indexField":"t01_object.metadata_standard_name",
			"xpath":"//metadataStandardName/CharacterString"
		},
		{	"indexField":"t01_object.metadata_standard_version",
			"xpath":"//metadataStandardVersion/CharacterString"
		},
		{	"indexField":"t01_object.metadata_language_code",
			"xpath":"//language/CharacterString",
			"transform":{
				"funct":transformISO639_2ToISO639_1
			}
		},
		{	"indexField":"t01_object.vertical_extent_minimum",
			"xpath":"//identificationInfo//extent/EX_Extent/verticalElement/EX_VerticalExtent/minimumValue/Real"
		},
		{	"indexField":"t01_object.vertical_extent_maximum",
			"xpath":"//identificationInfo//extent/EX_Extent/verticalElement/EX_VerticalExtent/maximumValue/Real"
		},
		{	"indexField":"t01_object.vertical_extent_unit",
			"xpath":"//identificationInfo//EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/VerticalCRS/verticalCS/VerticalCS/axis/CoordinateSystemAxis/@uom",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[102]
			}
		},
		{	"indexField":"t01_object.vertical_extent_vdatum",
			"xpath":"//identificationInfo//EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/VerticalCRS/verticalDatum/VerticalDatum/identifier",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[101]
			}
		},
		{	"indexField":"t01_object.ordering_instructions",
			"xpath":"//distributionInfo/MD_Distribution/distributor/MD_Distributor/distributionOrderProcess/MD_StandardOrderProcess/orderingInstructions/CharacterString"
		},
		{	"indexField":"t01_object.mod_time",
			"xpath":"//dateStamp/Date",
			"transform":{
				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			}
		},
		{	"indexField":"t01_object.mod_time",
			"xpath":"//dateStamp/DateTime",
			"transform":{
				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			}
		},
		// object_access
		{	"indexField":"object_access.restriction_key",
			"xpath":"//identificationInfo//resourceConstraints//otherConstraints/CharacterString",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[6010]
			}
		},
		{	"indexField":"object_access.restriction_value",
			"xpath":"//identificationInfo//resourceConstraints//otherConstraints/CharacterString"
		},
		{	"indexField":"object_access.terms_of_use",
			"xpath":"//identificationInfo//resourceConstraints//useLimitation/CharacterString"
		},
		// t0110_avail_format
		{	"indexField":"t0110_avail_format.name",
			"xpath":"//distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/CharacterString"
		},
		{	"indexField":"t0110_avail_format.version",
			"xpath":"//distributionInfo/MD_Distribution/distributionFormat/MD_Format/version/CharacterString"
		},
		{	"indexField":"t0110_avail_format.file_decompression_technique",
			"xpath":"//distributionInfo/MD_Distribution/distributionFormat/MD_Format/fileDecompressionTechnique/CharacterString"
		},
		{	"indexField":"t0110_avail_format.specification",
			"xpath":"//distributionInfo/MD_Distribution/distributionFormat/MD_Format/specification/CharacterString"
		},
		// t0113_dataset_reference
		{	"indexField":"t0113_dataset_reference.reference_date",
			"xpath":"//identificationInfo//citation/CI_Citation/date/CI_Date/date/Date",
			"transform":{
				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			}
		},
		{	"indexField":"t0113_dataset_reference.type",
			"xpath":"//identificationInfo//citation/CI_Citation/date/CI_Date/dateType/CI_DateTypeCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[502]
			}
		},
		//  t011_obj_serv
		{	"indexField":"t011_obj_serv.type",
			"xpath":"//identificationInfo//serviceType/LocalName"
		},
		{	"indexField":"t011_obj_serv.history",
			"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/description/CharacterString"
		},
		{	"indexField":"t011_obj_serv.base",
			"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/source/LI_Source/description/CharacterString"
		},
		// t011_obj_serv_op_connpoint
		{	"indexField":"t011_obj_serv_op_connpoint.connect_point",
			"xpath":"//identificationInfo//srv:containsOperations/SV_OperationMetadata/connectPoint/CI_OnlineResource/linkage/URL"
		},
		// t011_obj_serv_op_depends
		{	"indexField":"t011_obj_serv_op_depends.depends_on",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/dependsOn/SV_OperationMetadata/operationName/CharacterString"
		},
		// t011_obj_serv_op_para
		{	"indexField":"t011_obj_serv_op_para.name",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/parameters/SV_Parameter/name"
		},
		{	"indexField":"t011_obj_serv_op_para.direction",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/parameters/SV_Parameter/direction/SV_ParameterDirection"
		},
		{	"indexField":"t011_obj_serv_op_para.descr",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/parameters/SV_Parameter/description/CharacterString"
		},
		{	"indexField":"t011_obj_serv_op_para.optional",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/parameters/SV_Parameter/optionality/CharacterString",
			"transform":{
				"funct":transformGeneric,
				"params":[{"optional":"1", "mandatory":"0"}, false]
			}			
		},
		{	"indexField":"t011_obj_serv_op_para.repeatability",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/parameters/SV_Parameter/repeatability/Boolean",
			"transform":{
				"funct":transformGeneric,
				"params":[{"true":"1", "false":"0"}, false]
			}			
		},
		// t011_obj_serv_op_platform
		{	"indexField":"t011_obj_serv_op_platform.platform",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/DCP/DCPList/@codeListValue"
		},
		// t011_obj_serv_operation
		{	"indexField":"t011_obj_serv_operation.name",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/operationName/CharacterString"
		},
		{	"indexField":"t011_obj_serv_operation.descr",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/operationDescription/CharacterString"
		},
		{	"indexField":"t011_obj_serv_operation.invocation_name",
			"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata/invocationName/CharacterString"
		},
		// t011_obj_serv_version
		{	"indexField":"t011_obj_serv_version.serv_version",
			"xpath":"//identificationInfo//serviceTypeVersion/CharacterString"
		},
		// t011_obj_topic_cat
		{	"indexField":"t011_obj_topic_cat.topic_category",
			"xpath":"//identificationInfo//topicCategory/MD_TopicCategoryCode",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[527]
			}
		},
		// t011_obj_geo
		{	"indexField":"t011_obj_geo.special_base",
			"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/statement/CharacterString"
		},
		{	"indexField":"t011_obj_geo.data_base",
			"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/source/LI_Source/description/CharacterString"
		},
		{	"indexField":"t011_obj_geo.method",
			"xpath":"/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/description/CharacterString"
		},
		{	"execute":{
				"funct":mapReferenceSystemInfo
			}
		}
		
	];


// map more complex values
// map time constraints 
addTimeConstraints(recordNode);
// map resourceMaintenance
addResourceMaintenance(recordNode);

// code below is copied from DummyDocumentReader as demonstration 
// (satisfies IndexesTest.testIndexer())
var _counter = 1;

document.add(new Field("datatype", "default", !_store, _index, !_token));
document.add(new Field("url", "url " + _counter, _store, _index, _token));


	
// iterate over all transformation descriptions
var value;
for (var i in transformationDescriptions) {
	var t = transformationDescriptions[i];
	
	// check for execution (special function)
	if (hasValue(t.execute)) {
		log.debug("Execute function: " + t.execute.funct.name)
		call_f(t.execute.funct, t.execute.params)
	} else {
		log.debug("Working on " + t.indexField)
		var tokenized = true;
		// iterate over all xpath results
		var nodeList = XPathUtils.getNodeList(recordNode, t.xpath);
		for (j=0; j<nodeList.getLength(); j++ ) {
			value = nodeList.item(j).getTextContent()
			// check for transformation
			if (hasValue(t.transform)) {
				var args = new Array(value);
				if (hasValue(t.transform.params)) {
					args = args.concat(t.transform.params);
				}
				value = call_f(t.transform.funct,args);
			}
			// check for NOT tokenized
			if (hasValue(t.tokenized)) {
				if (!t.tokenized) {
					tokenized = false;
				}
			}
			addToDoc(t.indexField, value, tokenized);
		}
	}
}


function mapReferenceSystemInfo() {
	var rsIdentifiers = XPathUtils.getNodeList(recordNode, "//referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier");
	if (hasValue(rsIdentifiers)) {
		for (i=0; i<rsIdentifiers.getLength(); i++ ) {
			var code = XPathUtils.getString(rsIdentifiers.item(i), "code/CharacterString");
			var codeSpace = XPathUtils.getString(rsIdentifiers.item(i), "codeSpace/CharacterString");
			if (hasValue(codeSpace) && hasValue(code)) {
				addToDoc("t011_obj_geo.referencesystem_id", codeSpace+":"+code, true);
			} else if (hasValue(code)) {
				addToDoc("t011_obj_geo.referencesystem_id", code, true);
			}
		}
	}
}


function transformGeneric(val, mappings, caseSensitive) {
	for (var t in mappings) {
		for (var key in t) {
			if (caseSensitive) {
				if (key == val) {
					return t[key];
				}
			} else {
				if (key.toLowerCase() == val.toLowerCase()) {
					return t[key];
				}
			}
		}
	}
	return val;
}


function transformToIgcDomainId(val, codeListId) {
	if (hasValue(val)) {
		// transform to IGC domain id, use english code
		var idcCode = UtilsUDKCodeLists.getCodeListDomainId(codeListId, val, 94);
		if (hasValue(idcCode)) {
			return idcCode;
		} else {
			log.debug("Domain code '" + val + "' unknown in code list " + codeListId + ".");
			return -1;
		}
	}
}

function transformISO639_2ToISO639_1(val) {
	var ISO639_2ToISO639_1 = {
		"deu":"de",
		"ger":"de",
		"ger":"de",
		"eng":"en"
	}
	for(iso639_2 in ISO639_2ToISO639_1) {
		if (val == iso639_2) {
			return ISO639_2ToISO639_1[iso639_2];
		}
		return val;
	}
	
} 


function addResourceMaintenance() {
	var maintenanceFrequencyCode = XPathUtils.getString(recordNode, "//identificationInfo//resourceMaintenance/MD_MaintenanceInformation/maintenanceAndUpdateFrequency/MD_MaintenanceFrequencyCode/@codeListValue")
	if (hasValue(maintenanceFrequencyCode)) {
		// transform to IGC domain id
		var idcCode = UtilsUDKCodeLists.getCodeListDomainId(518, maintenanceFrequencyCode, 94);
		if (hasValue(idcCode)) {
			addToDoc("t01_object.time_period", idcCode, false);
			addToDoc("t01_object.time_descr", XPathUtils.getString(recordNode, "//identificationInfo//resourceMaintenance/MD_MaintenanceInformation/maintenanceNote/CharacterString"), true);
			var periodDuration = XPathUtils.getString(recordNode, "//identificationInfo//resourceMaintenance/MD_MaintenanceInformation/userDefinedMaintenanceFrequency/TM_PeriodDuration");
			addToDoc("t01_object.time_interval", new TM_PeriodDurationToTimeInterval().parse(periodDuration), false);
			addToDoc("t01_object.time_alle", new TM_PeriodDurationToTimeAlle().parse(periodDuration), false);
		} else {
			log.debug("MD_MaintenanceFrequencyCode '" + maintenanceFrequencyCode + "' unknown.")
		}
	}
}

function addTimeConstraints() {
	var t1 = UtilsCSWDate.mapDateFromIso8601ToIndex(XPathUtils.getString(recordNode, "//identificationInfo//EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/beginPosition"));
	var t2 = UtilsCSWDate.mapDateFromIso8601ToIndex(XPathUtils.getString(recordNode, "//identificationInfo//EX_Extent/temporalElement/EX_TemporalExtent/extent/TimePeriod/endPosition"));
	var timeType;
	if (hasValue(t1) && hasValue(t2)) {
		if (t1 == t2) {
			addToDoc("t01_object.time_type", "am", false);
			addToDoc("t0", t1, false);
		} else {
			addToDoc("t01_object.time_type", "von", false);
			addToDoc("t1", t1, false);
			addToDoc("t2", t2, false);
		}
	} else if (hasValue(t1) && !hasValue(t2)) {
		addToDoc("t01_object.time_type", "seit", false);
		addToDoc("t1", t1, false);
	} else if (!hasValue(t1) && hasValue(t2)) {
		addToDoc("t01_object.time_type", "bis", false);
		addToDoc("t2", t2, false);
	}
}


function getObjectClassFromHierarchyLevel(val) {
	// default to "Geo-Information / Karte"
	var result = "1"; 
	if (hasValue(val) && val.toLowerCase() == "service") {
		// "Dienst / Anwendung / Informationssystem"
		result = "3";
	}
	return result;
}

function addToDoc(field, content, tokenized) {
	if (hasValue(content)) {
		log.debug("Add '" + field + "'='" + content + "' to lucene index");
		document.add(new Field(field, content, _store, _index, tokenized));
		document.add(new Field("content", content, !_store, _index, true));
		document.add(new Field("content", AbstractSearcher.filterTerm(content), !_store, _index, true));
	}
}

function hasValue(val) {
	if (typeof val == "undefined") {
		return false; 
	} else if (val == null) {
		return false; 
	} else if (typeof val == "string" && val == "") {
		return false;
	} else {
	  return true;
	}
}

function call_f(f,args)
{
  f.call_self = function(ars)
  {
    var callstr = "";
    if (hasValue(ars)) {
	    for(var i = 0; i < ars.length; i++)
	    {
	      callstr += "ars["+i+"]";
	      if(i < ars.length - 1)
	      {
	        callstr += ',';
	      }
	    }
	}
    return eval("this("+callstr+")");
  };

  return f.call_self(args);
}


