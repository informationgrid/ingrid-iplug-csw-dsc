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
			"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/description/CharacterString"
		},
		{	"execute":{
				"funct":mapReferenceSystemInfo
			}
		},
		{	"indexField":"t011_obj_geo.rec_exact",
			"xpath":"//dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy/DQ_QuantitativeResult/value/Record"
		},
		{	"indexField":"t011_obj_geo.rec_grade",
			"xpath":"//dataQualityInfo/DQ_DataQuality/report/DQ_CompletenessCommission/DQ_QuantitativeResult/value/Record"
		},
		{	"indexField":"t011_obj_geo.hierarchy_level",
			"xpath":"//hierarchyLevel/MD_ScopeCode/@codeListValue",
			"transform":{
				"funct":transformGeneric,
				"params":[{"dataset":"5", "series":"6"}, false]
			}
		},
		{	"indexField":"t011_obj_geo.vector_topology_level",
			"xpath":"//spatialRepresentationInfo/MD_VectorSpatialRepresentation/topologyLevel/MD_TopologyLevelCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[528]
			}
		},
		{	"indexField":"t011_obj_geo.pos_accuracy_vertical",
			"xpath":"//dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy[measureDescription/CharacterString='vertical']/DQ_QuantitativeResult/value/Record"
		},
		{	"indexField":"t011_obj_geo.keyc_incl_w_dataset",
			"xpath":"//contentInfo/MD_FeatureCatalogueDescription/includedWithDataset/Boolean",
			"transform":{
				"funct":transformGeneric,
				"params":[{"true":"1", "false":"0"}, false]
			}			
		},
		// accept RS_Indentifier and MD_Identifier with xpath: "...identifier//code..."
		{	"indexField":"t011_obj_geo.datasource_uuid",
			"xpath":"//identificationInfo/MD_DataIdentification/citation/CI_Citation/identifier//code/CharacterString"
		},
		// t011_obj_geo_keyc
		{	"indexField":"t011_obj_geo_keyc.subject_cat",
			"xpath":"//contentInfo/MD_FeatureCatalogueDescription/featureCatalogueCitation/CI_Citation/title/CharacterString"
		},
		{	"indexField":"t011_obj_geo_keyc.key_date",
			"xpath":"//contentInfo/MD_FeatureCatalogueDescription/featureCatalogueCitation/CI_Citation/date/CI_Date/date/Date",
			"transform":{
				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			}
		},
		{	"indexField":"t011_obj_geo_keyc.edition",
			"xpath":"//contentInfo/MD_FeatureCatalogueDescription/featureCatalogueCitation/CI_Citation/edition/CharacterString"
		},
		// t011_obj_geo_scale
		{	"indexField":"t011_obj_geo_scale.scale",
			"xpath":"//identificationInfo/MD_DataIdentification/spatialResolution/MD_Resolution/equivalentScale/MD_RepresentativeFraction/denominator/Integer"
		},
		{	"indexField":"t011_obj_geo_scale.resolution_ground",
			"xpath":"//identificationInfo/MD_DataIdentification/spatialResolution/MD_Resolution/distance/Distance[@uom='meter']"
		},
		{	"indexField":"t011_obj_geo_scale.resolution_scan",
			"xpath":"//identificationInfo/MD_DataIdentification/spatialResolution/MD_Resolution/distance/Distance[@uom='dpi']"
		},
		// t011_obj_geo_spatial_rep
		{	"indexField":"t011_obj_geo_spatial_rep.type",
			"xpath":"//identificationInfo/MD_DataIdentification/spatialRepresentationType/MD_SpatialRepresentationTypeCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[526]
			}
		},
		// t011_obj_geo_supplinfo
		{	"indexField":"t011_obj_geo_supplinfo.feature_type",
			"xpath":"//contentInfo/MD_FeatureCatalogueDescription/featureTypes/LocalName"
		},
		// t011_obj_geo_symc
		{	"indexField":"t011_obj_geo_symc.symbol_cat",
			"xpath":"//portrayalCatalogueInfo/MD_PortrayalCatalogueReference/portrayalCatalogueCitation/CI_Citation/title/CharacterString"
		},
		{	"indexField":"t011_obj_geo_symc.symbol_date",
			"xpath":"//portrayalCatalogueInfo/MD_PortrayalCatalogueReference/portrayalCatalogueCitation/CI_Citation/date/CI_Date/date/Date",
			"transform":{
				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			}
		},
		{	"indexField":"t011_obj_geo_symc.edition",
			"xpath":"//portrayalCatalogueInfo/MD_PortrayalCatalogueReference/portrayalCatalogueCitation/CI_Citation/edition/CharacterString"
		},
		// t011_obj_geo_vector
		{	"indexField":"t011_obj_geo_vector.geometric_object_type",
			"xpath":"//spatialRepresentationInfo/MD_VectorSpatialRepresentation/geometricObjects/MD_GeometricObjects/geometricObjectType/MD_GeometricObjectTypeCode/@codeListValue",
			"transform":{
				"funct":transformToIgcDomainId,
				"params":[515]
			}
		},
		{	"indexField":"t011_obj_geo_vector.geometric_object_count",
			"xpath":"//spatialRepresentationInfo/MD_VectorSpatialRepresentation/geometricObjects/MD_GeometricObjects/geometricObjectCount/Integer"
		},
		// t017_url_ref
		{	"indexField":"t017_url_ref.url_link",
			"xpath":"//distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/online/CI_OnlineResource/linkage/URL"
		},
		{	"indexField":"t017_url_ref.content",
			"xpath":"//distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/online/CI_OnlineResource/name/CharacterString"
		},
		{	"indexField":"t017_url_ref.descr",
			"xpath":"//distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/online/CI_OnlineResource/description/CharacterString"
		},
		{	"indexField":"t017_url_ref.descr",
			"xpath":"//distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/online/CI_OnlineResource/description/CharacterString"
		},
		// object_references
		{	"execute":{
				"funct":mapReferences,
				"params":[recordNode]
			}
		},
		// keywords
		{	"execute":{
				"funct":mapKeywords,
				"params":[recordNode]
			}
		},
		// geographic elements
		{	"execute":{
				"funct":mapGeographicElements,
				"params":[recordNode]
			}
		},
		// time constraints
		{	"execute":{
				"funct":addTimeConstraints,
				"params":[recordNode]
			}
		},
		// resource maintenance
		{	"execute":{
				"funct":addResourceMaintenance,
				"params":[recordNode]
			}
		},
		// addresses 
		{	"execute":{
				"funct":mapAddresses,
				"params":[recordNode]
			}
		}
	];

document.add(new Field("datatype", "default", !_store, _index, !_token));
	
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
			if (hasValue(value)) {
				addToDoc(t.indexField, value, tokenized);
			}
		}
	}
}

function mapAddresses(recordNode) {
	var addresses = XPathUtils.getNodeList(recordNode, "//CI_ResponsibleParty");
	if (hasValue(addresses)) {
		for (i=0; i<addresses.getLength(); i++ ) {
			var addressRole = XPathUtils.getString(addresses.item(i), "role/CI_RoleCode/@codeListValue");
			if (hasValue(addressRole)) {
				// map address role
				addToDoc("t012_obj_adr.special_ref", "0", true);
				var mappedAddressRole = transformToIgcDomainId(addressRole, 505);
				if (hasValue(mappedAddressRole) && mappedAddressRole != "-1") {
					// mapping to code list 505 successful
					addToDoc("t012_obj_adr.typ", mappedAddressRole, false);
					addToDoc("t012_obj_adr.special_name", "", true);
				} else {
					addToDoc("t012_obj_adr.typ", "-1", true);
					addToDoc("t012_obj_adr.special_name", addressRole, true);
				}
				// map address data
				addToDoc("t02_address.institution", XPathUtils.getString(addresses.item(i), "organisationName/CharacterString"), true);
				addToDoc("t02_address.lastname", XPathUtils.getString(addresses.item(i), "individualName/CharacterString"), true);
				addToDoc("t02_address.street", XPathUtils.getString(addresses.item(i), "contactInfo/CI_Contact/address/CI_Address/deliveryPoint/CharacterString"), true);
				addToDoc("t02_address.postcode", XPathUtils.getString(addresses.item(i), "contactInfo/CI_Contact/address/CI_Address/postalCode/CharacterString"), true);
				addToDoc("t02_address.city", XPathUtils.getString(addresses.item(i), "contactInfo/CI_Contact/address/CI_Address/city/CharacterString"), true);
				addToDoc("t02_address.country_code", XPathUtils.getString(addresses.item(i), "contactInfo/CI_Contact/address/CI_Address/country/CharacterString"), true);
				addToDoc("t02_address.job", XPathUtils.getString(addresses.item(i), "positionName/CharacterString"), true);
				addToDoc("t02_address.descr", XPathUtils.getString(addresses.item(i), "contactInfo/CI_Contact/contactInstructions/CharacterString"), true);
				// map communication Data
				// phone
				var entries = XPathUtils.getNodeList(addresses.item(i), "contactInfo/CI_Contact/phone/CI_Telephone/voice/CharacterString");
				if (hasValue(entries)) {
					for (j=0; j<entries.getLength(); j++ ) {
						addToDoc("t021_communication.comm_type", "Telefon", true);
						addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
					}
				}
				// fax
				var entries = XPathUtils.getNodeList(addresses.item(i), "contactInfo/CI_Contact/phone/CI_Telephone/facsimile/CharacterString");
				if (hasValue(entries)) {
					for (j=0; j<entries.getLength(); j++ ) {
						addToDoc("t021_communication.comm_type", "Fax", true);
						addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
					}
				}
				// email
				var entries = XPathUtils.getNodeList(addresses.item(i), "contactInfo/CI_Contact/address/CI_Address/electronicMailAddress/CharacterString");
				if (hasValue(entries)) {
					for (j=0; j<entries.getLength(); j++ ) {
						addToDoc("t021_communication.comm_type", "Email", true);
						addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
					}
				}
				// url
				var entries = XPathUtils.getNodeList(addresses.item(i), "contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL");
				if (hasValue(entries)) {
					for (j=0; j<entries.getLength(); j++ ) {
						addToDoc("t021_communication.comm_type", "URL", true);
						addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
					}
				}
			}
			
		}
	}
}


function mapGeographicElements(recordNode) {
	var geographicElements = XPathUtils.getNodeList(recordNode, "//identificationInfo//extent/EX_Extent/geographicElement");
	if (hasValue(geographicElements)) {
		for (i=0; i<geographicElements.getLength(); i++ ) {
			var value = XPathUtils.getString(geographicElements.item(i), "EX_GeographicDescription/geographicIdentifier/MD_Identifier/code/CharacterString");
			if (hasValue(value)) {
				addToDoc("spatial_ref_value.name_value", value, true);
				addToDoc("x1", "", false);
				addToDoc("x2", "", false);
				addToDoc("y1", "", false);
				addToDoc("y2", "", false);
			}
			var boundingBoxes = XPathUtils.getNodeList(geographicElements.item(i), "EX_GeographicBoundingBox");
			for (j=0; j<boundingBoxes.getLength(); j++ ) {
				if (hasValue(boundingBoxes.item(j)) && hasValue(XPathUtils.getString(boundingBoxes.item(j), "westBoundLongitude/Decimal"))) {
					addToDoc("spatial_ref_value.name_value", "", true);
					addToDoc("x1", XPathUtils.getString(boundingBoxes.item(j), "westBoundLongitude/Decimal"), false);
					addToDoc("x2", XPathUtils.getString(boundingBoxes.item(j), "eastBoundLongitude/Decimal"), false);
					addToDoc("y1", XPathUtils.getString(boundingBoxes.item(j), "southBoundLatitude/Decimal"), false);
					addToDoc("y2", XPathUtils.getString(boundingBoxes.item(j), "northBoundLatitude/Decimal"), false);
				}
			}
		}
	}
}


function mapKeywords(recordNode) {
	var usedKeywords = "";
	// check for INSPIRE themes
	var keywords = XPathUtils.getNodeList(recordNode, "//identificationInfo//descriptiveKeywords/MD_Keywords[thesaurusName/CI_Citation/title/CharacterString='GEMET - INSPIRE themes, version 1.0']/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim()
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				addToDoc("searchterm_value.term", value, true);
				addToDoc("searchterm_value.type", "I", false);
				usedKeywords+=value+";"
			}
		}
	}
	// check for GEMET keywords
	var keywords = XPathUtils.getNodeList(recordNode, "//identificationInfo//descriptiveKeywords/MD_Keywords[thesaurusName/CI_Citation/title/CharacterString='GEMET - Concepts, version 2.1']/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim()
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				addToDoc("searchterm_value.term", value, true);
				addToDoc("searchterm_value.type", "G", false);
				usedKeywords+=value+";"
			}
		}
	}
	// check for UMTHES keywords
	var keywords = XPathUtils.getNodeList(recordNode, "//identificationInfo//descriptiveKeywords/MD_Keywords[thesaurusName/CI_Citation/title/CharacterString='UMTHES Thesaurus']/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim()
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				addToDoc("searchterm_value.term", value, true);
				addToDoc("searchterm_value.type", "T", false);
				usedKeywords+=value+";"
			}
		}
	}
	// check for other keywords
	var keywords = XPathUtils.getNodeList(recordNode, "//identificationInfo//descriptiveKeywords/MD_Keywords/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim();
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				addToDoc("searchterm_value.term", value, true);
				addToDoc("searchterm_value.type", "F", false);
				usedKeywords+=value+";"
			}
		}
	}
}


function mapReferences(recordNode) {
	// check for coupled resources, bound to a specific operation in services
	var usedUuids="";
	var coupledResources = XPathUtils.getNodeList(recordNode, "//identificationInfo/SV_ServiceIdentification/coupledResource/SV_CoupledResource/identifier/CharacterString");
	if (hasValue(coupledResources)) {
		for (i=0; i<coupledResources.getLength(); i++ ) {
			var value = coupledResources.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3345") == -1) {
				addToDoc("object_reference.obj_to_uuid", value, true);
				addToDoc("object_reference.special_ref", "3345", true);
				usedUuids+=value+"3345;"
			}
		}
	}
	// check for coupled resources (operatedOn)
	var operatesOn = XPathUtils.getNodeList(recordNode, "//identificationInfo/SV_ServiceIdentification/operatesOn/@uuidref");
	if (hasValue(operatesOn)) {
		for (i=0; i<operatesOn.getLength(); i++ ) {
			var value = operatesOn.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3345") == -1) {
				addToDoc("object_reference.obj_to_uuid", value, true);
				addToDoc("object_reference.special_ref", "3345", true);
				usedUuids+=value+"3345;"
			}
		}
	}
	// check for content info references (Schlüsselkatalog)
	var operatesOn = XPathUtils.getNodeList(recordNode, "//contentInfo/@uuidref");
	if (hasValue(operatesOn)) {
		for (i=0; i<operatesOn.getLength(); i++ ) {
			var value = operatesOn.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3535") == -1) {
				addToDoc("object_reference.obj_to_uuid", value, true);
				addToDoc("object_reference.special_ref", "3535", true);
				usedUuids+=value+"3535;"
			}
		}
	}
	// check for portrayalCatalogue info references (Symbolkatalog)
	var operatesOn = XPathUtils.getNodeList(recordNode, "//contentInfo/@uuidref");
	if (hasValue(operatesOn)) {
		for (i=0; i<operatesOn.getLength(); i++ ) {
			var value = operatesOn.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3555") == -1) {
				addToDoc("object_reference.obj_to_uuid", value, true);
				addToDoc("object_reference.special_ref", "3555", true);
				usedUuids+=value+"3555;"
			}
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
	return null;
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
	if (typeof content != "undefined" && content != null) {
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


