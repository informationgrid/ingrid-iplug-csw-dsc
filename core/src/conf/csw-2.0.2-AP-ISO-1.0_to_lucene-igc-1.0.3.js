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
importPackage(Packages.de.ingrid.utils.udk);


var _store = true;
var _index = true;
var _token = true;

log.debug("Mapping csw record "+cswRecord.getId()+" to lucene document");

// get the xml content of the record
var recordNode = cswRecord.getOriginalResponse();

// define one-to-one mappings
var transformationDescriptions = [
		{	"indexField":"t01_object.obj_id",
			"xpath":"//fileIdentifier/CharacterString"
		}, 
		{	"indexField":"title",
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
document.add(new Field("content", "content " + _counter, _store, _index, _token));
document.add(new Field("url", "url " + _counter, _store, _index, _token));


	
// iterate over all transformation descriptions
var value;
for (var i in transformationDescriptions) {
	var t = transformationDescriptions[i];
	log.debug("Working on " + t.indexField)
	
	// check for execution (special function)
	if (hasValue(t.execute)) {
		call_f(t.execute.funct, t.execute.params)
	} else {
		value = XPathUtils.getString(recordNode, t.xpath);
		// check for transformation
		if (hasValue(t.transform)) {
			var args = new Array(value);
			if (hasValue(t.transform.params)) {
				args = args.concat(t.transform.params);
			}
			value = call_f(t.transform.funct,args);
		}
		addToDoc(t.indexField, value);
	}
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
			addToDoc("t01_object.time_period", idcCode);
			addToDoc("t01_object.time_descr", XPathUtils.getString(recordNode, "//identificationInfo//resourceMaintenance/MD_MaintenanceInformation/maintenanceNote/CharacterString"));
			var periodDuration = XPathUtils.getString(recordNode, "//identificationInfo//resourceMaintenance/MD_MaintenanceInformation/userDefinedMaintenanceFrequency/TM_PeriodDuration");
			addToDoc("t01_object.time_interval", new TM_PeriodDurationToTimeInterval().parse(periodDuration));
			addToDoc("t01_object.time_alle", new TM_PeriodDurationToTimeAlle().parse(periodDuration));
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
			addToDoc("t01_object.time_type", "am");
			addToDoc("t0", t1);
		} else {
			addToDoc("t01_object.time_type", "von");
			addToDoc("t1", t1);
			addToDoc("t2", t2);
		}
	} else if (hasValue(t1) && !hasValue(t2)) {
		addToDoc("t01_object.time_type", "seit");
		addToDoc("t1", t1);
	} else if (!hasValue(t1) && hasValue(t2)) {
		addToDoc("t01_object.time_type", "bis");
		addToDoc("t2", t2);
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

function addToDoc(field, content) {
	if (hasValue(content)) {
		log.debug("Add '" + field + "'='" + content + "' to lucene index");
		document.add(new Field(field, content, _store, _index, !_token));
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
    for(var i = 0; i < ars.length; i++)
    {
      callstr += "ars["+i+"]";
      if(i < ars.length - 1)
      {
        callstr += ',';
      }
    }
    return eval("this("+callstr+")");
  };

  return f.call_self(args);
}


