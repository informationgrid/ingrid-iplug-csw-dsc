/**
 * CSW Record to Ingrid Record mapping
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 *
 * The following global variable are passed from the application:
 *
 * @param cswRecord A CSWRecord instance, that defines the input
 * @param document A ingrid Record instance, that defines the output
 * @param log A Log instance
 *
 */
importPackage(Packages.de.ingrid.iplug.csw.dsc.tools);
importPackage(Packages.de.ingrid.utils.dsc);
importPackage(Packages.de.ingrid.utils.udk);

log.debug("Mapping csw record "+cswRecord.getId()+" to ingrid document");

// get the xml content of the record
var recordNode = cswRecord.getOriginalResponse();

var mappingDescription = 
  		{	"table":"t01_object",
  			"fieldMappings":[
	  			{
					"field":"obj_uuid",
					"indexName":"t01_object.obj_id",
					"xpath":"//fileIdentifier/CharacterString"
				},
				{
					"field":"obj_name",
					"indexName":"title",
					"xpath":"//identificationInfo//citation/CI_Citation/title/CharacterString"
				},
				{
					"field":"org_obj_id",
					"xpath":"//fileIdentifier/CharacterString"
				},
				{
					"field":"obj_descr",
					"indexName":"summary",
					"xpath":"//identificationInfo//abstract/CharacterString"
				},
				{
					"field":"info_note",
					"xpath":"//identificationInfo//purpose/CharacterString"
				},
				{
					"field":"loc_descr",
					"xpath":"//identificationInfo//EX_Extent/description/CharacterString"
				},
				{
					"field":"dataset_alternate_name",
					"xpath":"//identificationInfo//citation/CI_Citation/alternateTitle/CharacterString"
				},
				{
					"field":"time_status",
					"xpath":"//identificationInfo//status/MD_ProgressCode/@codeListValue",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[523]
					}
				},
				{
					"field":"obj_class",
					"xpath":"//hierarchyLevel/MD_ScopeCode/@codeListValue",
					"transform":{
						"funct":getObjectClassFromHierarchyLevel
					}
				},
				{
					"field":"dataset_character_set",
					"xpath":"//identificationInfo//characterSet/MD_CharacterSetCode/@codeListValue",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[510]
					}
				},
				{
					"field":"dataset_usage",
					"xpath":"//identificationInfo//resourceSpecificUsage/MD_Usage/specificUsage/CharacterString"
				},
				{
					"field":"data_language_code",
					"xpath":"//identificationInfo//language/CharacterString",
					"transform":{
						"funct":transformISO639_2ToISO639_1
					}
				},
				{
					"field":"metadata_character_set",
					"xpath":"//characterSet/MD_CharacterSetCode/@codeListValue",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[510]
					}
				},
				{
					"field":"metadata_standard_name",
					"xpath":"//metadataStandardName/CharacterString"
				},
				{
					"field":"metadata_standard_version",
					"xpath":"//metadataStandardVersion/CharacterString"
				},
				{
					"field":"metadata_language_code",
					"xpath":"MD_Metadata/language/CharacterString",
					"transform":{
						"funct":transformISO639_2ToISO639_1
					}
				},
				{
					"field":"vertical_extent_minimum",
					"xpath":"//identificationInfo//extent/EX_Extent/verticalElement/EX_VerticalExtent/minimumValue/Real"
				},
				{
					"field":"vertical_extent_maximum",
					"xpath":"//identificationInfo//extent/EX_Extent/verticalElement/EX_VerticalExtent/maximumValue/Real"
				},
				{
					"field":"vertical_extent_unit",
					"xpath":"//identificationInfo//EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/VerticalCRS/verticalCS/VerticalCS/axis/CoordinateSystemAxis/@uom",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[102]
					}
				},
				{
					"field":"vertical_extent_vdatum",
					"xpath":"//identificationInfo//EX_Extent/verticalElement/EX_VerticalExtent/verticalCRS/VerticalCRS/verticalDatum/VerticalDatum/identifier",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[101]
					}
				},
				{
					"field":"ordering_instructions",
					"xpath":"//distributionInfo/MD_Distribution/distributor/MD_Distributor/distributionOrderProcess/MD_StandardOrderProcess/orderingInstructions/CharacterString"
				},
				{
					"field":"mod_time",
					"xpath":"//dateStamp/DateTime",
					"transform":{
						"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
					}
				},
				{	
					"execute":{
						"funct":mapKeywords
			    	}
				},
				{
					"execute":{
						"funct":mapGeographicElements // map also to spatial_ref_value
					}
		    	}
				
			],
			"subrecords":[
				{
			    "table":"object_access",
			    "xpath":"//identificationInfo//resourceConstraints",
				"line":true,
			    "fieldMappings":[
	    	  		{
    					"field":"restriction_key",
    					"xpath":"//otherConstraints/CharacterString",
    					"transform":{
    						"funct":transformToIgcDomainId,
    						"params":[6010]
    					}
	    			},
	    	  		{
    					"field":"restriction_value",
    					"xpath":"//otherConstraints/CharacterString"
	    			},
	    	  		{
    					"field":"terms_of_use",
    					"xpath":"//useLimitation/CharacterString"
	    			}
	    	  	]
				}, // END object_access
			    {
			    "table":"t0110_avail_format",
			    "xpath":"//distributionInfo/MD_Distribution/distributionFormat/MD_Format",
				"line":true,
			    "fieldMappings":[
	    	  		{
    					"field":"format_value",
    					"indexName":"t0110_avail_format.name",
    					"xpath":"name/CharacterString"
	    			},
	    	  		{
    					"field":"ver",
    					"indexName":"t0110_avail_format.version",
    					"xpath":"version/CharacterString"
	    			},
	    	  		{
    					"field":"file_decompression_technique",
    					"xpath":"fileDecompressionTechnique/CharacterString"
	    			},
	    	  		{
    					"field":"specification",
    					"xpath":"specification/CharacterString"
	    			}
	    		]
				}, // END t0110_avail_format
				{
				    "table":"t0113_dataset_reference",
				    "xpath":"//identificationInfo//citation/CI_Citation/date/CI_Date",
					"line":true,
				    "fieldMappings":[
		    	  		{
	    					"field":"reference_date",
	    					"xpath":"date/Date",
	    					"transform":{
		    	  				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
		    	  			}
		    			},
		    	  		{
	    					"field":"type",
	    					"xpath":"dateType/CI_DateTypeCode/@codeListValue",
	    					"transform":{
	    						"funct":transformToIgcDomainId,
	    						"params":[502]
	    					}
		    			}
		    		]
				}, // END t0113_dataset_reference
				{
				    "table":"t011_obj_serv",
				    "fieldMappings":[
		    	  		{
	    					"field":"type_value",
	    					"indexName":"t011_obj_serv.type",
	    					"xpath":"//identificationInfo//serviceType/LocalName"
		    			},
		    	  		{
	    					"field":"history",
	    					"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/description/CharacterString"
		    			},
		    	  		{
	    					"field":"base",
	    					"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/description/CharacterString"
		    			}
		    		],
		    		"subrecords":[
						{
							"table":"t011_obj_serv_operation",
							"xpath":"//identificationInfo//containsOperations/SV_OperationMetadata",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"name_value",
			    					"indexName":"t011_obj_serv_operation.name",
			    					"xpath":"operationName/CharacterString"
				    			},
				    	  		{
			    					"field":"descr",
			    					"xpath":"operationDescription/CharacterString"
				    			},
				    	  		{
			    					"field":"invocation_name",
			    					"xpath":"invocationName/CharacterString"
				    			}
							],
							"subrecords":[
								{
									"table":"t011_obj_serv_op_para",
									"xpath":"parameter/SV_Parameter",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"name",
					    					"xpath":"name"
						    			},
						    	  		{
					    					"field":"direction",
					    					"xpath":"direction/SV_ParameterDirection"
						    			},
						    	  		{
					    					"field":"descr",
					    					"xpath":"description/CharacterString"
						    			},
						    	  		{
					    					"field":"optional",
					    					"xpath":"optionality/CharacterString",
					    					"transform":{
							    				"funct":transformGeneric,
							    				"params":[{"optional":"1", "mandatory":"0"}, false]
							    			}						    					
						    			},
						    	  		{
					    					"field":"repeatability",
					    					"xpath":"repeatability/Boolean",
					    					"transform":{
							    				"funct":transformGeneric,
							    				"params":[{"true":"1", "false":"0"}, false]
							    			}						    					
						    			}
									]
								}, // END t011_obj_serv_op_para
								{
									"table":"t011_obj_serv_op_connpoint",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"connect_point",
					    					"xpath":"connectPoint/CI_OnlineResource/linkage/URL"
						    			}
									]
								}, // END t011_obj_serv_op_connpoint
								{
									"table":"t011_obj_serv_op_depends",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"depends_on",
					    					"xpath":"dependsOn/SV_OperationMetadata/operationName/CharacterString"
						    			}
									]
								}, // END t011_obj_serv_op_depends
								{
									"table":"t011_obj_serv_op_platform",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"platform",
					    					"xpath":"DCP/DCPList/@codeListValue"
						    			}
									]
								} // END t011_obj_serv_op_platform
				    		]
						}, // END t011_obj_serv_operation
						{
							"table":"t011_obj_serv_version",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"serv_version",
			    					"xpath":"//identificationInfo//serviceTypeVersion/CharacterString"
				    			}
							]
						} // END t011_obj_serv_version
		    		]
				}, // END t011_obj_serv
				{
					"table":"t011_obj_topic_cat",
					"xpath":"//identificationInfo//topicCategory",
					"line":true,
					"fieldMappings":[
		    	  		{
	    					"field":"topic_category",
	    					"xpath":"MD_TopicCategoryCode",
	    					"transform":{
	    						"funct":transformToIgcDomainId,
	    						"params":[527]
	    					}
		    			}
		    	  	]
				}, // END t011_obj_topic_cat
				{
					"table":"t011_obj_geo",
					"fieldMappings":[
		    	  		{
	    					"field":"special_base",
	    					"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/statement/CharacterString"
		    			},
		    	  		{
	    					"field":"data_base",
	    					"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/source/LI_Source/description/CharacterString"
		    			},
		    	  		{
	    					"field":"method",
	    					"xpath":"//dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/processStep/LI_ProcessStep/description/CharacterString"
		    	  		},
		    	  		{
		    	  			"execute":{
		    	  				"funct":mapReferenceSystemInfo
		    	  			}
		    			},
		    			{	"field":"rec_exact",
		    				"xpath":"//dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy/DQ_QuantitativeResult/value/Record"
		    			},
		    			{	"field":"rec_grade",
		    				"xpath":"//dataQualityInfo/DQ_DataQuality/report/DQ_CompletenessCommission/DQ_QuantitativeResult/value/Record"
		    			},
		    			{	"field":"hierarchy_level",
		    				"xpath":"//hierarchyLevel/MD_ScopeCode/@codeListValue",
							"transform":{
								"funct":transformGeneric,
								"params":[{"dataset":"5", "series":"6"}, false]
							}
		    			},
		    			{	"field":"vector_topology_level",
		    				"xpath":"//spatialRepresentationInfo/MD_VectorSpatialRepresentation/topologyLevel/MD_TopologyLevelCode/@codeListValue",
							"transform":{
								"funct":transformToIgcDomainId,
								"params":[528]
							}
		    			},
		    			{	"field":"pos_accuracy_vertical",
		    				"xpath":"//dataQualityInfo/DQ_DataQuality/report/DQ_RelativeInternalPositionalAccuracy[measureDescription/CharacterString='vertical']/DQ_QuantitativeResult/value/Record"
		    			},
		    			{	"field":"keyc_incl_w_dataset",
		    				"xpath":"//contentInfo/MD_FeatureCatalogueDescription/includedWithDataset/Boolean",
							"transform":{
								"funct":transformGeneric,
								"params":[{"true":"1", "false":"0"}, false]
							}
		    			},
		    			{	"field":"datasource_uuid",
		    				// accept RS_Indentifier and MD_Identifier with xpath: "...identifier//code..."
		    				"xpath":"//identificationInfo/MD_DataIdentification/citation/CI_Citation/identifier//code/CharacterString"
				    	}
		    	  	],
		    	  	"subrecords":[
						{
							"table":"t011_obj_geo_keyc",
							"xpath":"//contentInfo/MD_FeatureCatalogueDescription/featureCatalogueCitation/CI_Citation",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"keyc_value",
			    					"indexName":"t011_obj_geo_keyc.subject_cat",
			    					"xpath":"title/CharacterString"
				    			},
				    			{
			    					"field":"key_date",
			    					"xpath":"date/CI_Date/date/Date",
			    					"transform":{
			    						"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			    					}
				    			},
				    			{
			    					"field":"edition",
			    					"xpath":"edition/CharacterString"
				    			}
				    	  	]
						}, // END t011_obj_geo_keyc
						{
							"table":"t011_obj_geo_scale",
							"xpath":"//identificationInfo/MD_DataIdentification",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"scale",
			    					"xpath":"spatialResolution/MD_Resolution/equivalentScale/MD_RepresentativeFraction/denominator/Integer"
				    			},
				    			{
			    					"field":"resolution_ground",
			    					"xpath":"spatialResolution/MD_Resolution/distance/Distance[@uom='meter']"
				    			},
				    			{
			    					"field":"resolution_scan",
			    					"xpath":"spatialResolution/MD_Resolution/distance/Distance[@uom='dpi']"
				    			}
				    	  	]
						}, // END t011_obj_geo_scale
						{
							"table":"t011_obj_geo_spatial_rep",
							"xpath":"//identificationInfo/MD_DataIdentification/spatialRepresentationType",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"type",
			    					"xpath":"MD_SpatialRepresentationTypeCode/@codeListValue",
			    					"transform":{
			    						"funct":transformToIgcDomainId,
			    						"params":[526]
			    					}
				    			}
				    	  	]
						}, // END t011_obj_geo_spatial_rep
						{
							"table":"t011_obj_geo_supplinfo",
							"xpath":"//contentInfo/MD_FeatureCatalogueDescription/featureTypes",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"feature_type",
			    					"xpath":"LocalName"
				    			}
				    	  	]
						}, // END t011_obj_geo_supplinfo
						{
							"table":"t011_obj_geo_symc",
							"xpath":"//portrayalCatalogueInfo/MD_PortrayalCatalogueReference/portrayalCatalogueCitation",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"symbol_cat_value",
			    					"indexName":"t011_obj_geo_symc.symbol_cat",
			    					"xpath":"CI_Citation/title/CharacterString"
				    			},
				    	  		{
			    					"field":"symbol_date",
			    					"xpath":"CI_Citation/date/CI_Date/date/Date",
									"transform":{
										"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
									}
				    			},
				    	  		{
			    					"field":"edition",
			    					"xpath":"CI_Citation/edition/CharacterString"
				    			}
				    	  	]
						}, // END t011_obj_geo_symc
						{
							"table":"t011_obj_geo_vector",
							"xpath":"//spatialRepresentationInfo/MD_VectorSpatialRepresentation/geometricObjects",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"geometric_object_type",
			    					"xpath":"MD_GeometricObjects/geometricObjectType/MD_GeometricObjectTypeCode/@codeListValue",
									"transform":{
										"funct":transformToIgcDomainId,
										"params":[515]
									}
				    			},
				    	  		{
			    					"field":"geometric_object_count",
			    					"xpath":"MD_GeometricObjects/geometricObjectCount/Integer"
				    			}
				    	  	]
						} // END t011_obj_geo_vector
					]
				}, // END t011_obj_geo
				{
					"table":"t017_url_ref",
					"xpath":"//distributionInfo/MD_Distribution/transferOptions/MD_DigitalTransferOptions/onLine/CI_OnlineResource",
					"line":true,
					"fieldMappings":[
		    	  		{
	    					"field":"url_link",
	    					"xpath":"linkage/URL"
		    			},
		    	  		{
	    					"field":"content",
	    					"xpath":"name/CharacterString"
		    			},
		    	  		{
	    					"field":"descr",
	    					"xpath":"description/CharacterString"
		    			}
		    	  	]
				}, // END t017_url_ref
				{	
					"table":"object_references",
					"fieldMappings":[
					    {
							"execute":{
								"funct":mapReferences
					    	}
					    }
					]
				}, // END object_references
				{
					"table":"spatial_reference",
					"fieldMappings":[
					    {
							"execute":{
								"funct":mapGeographicElements // map also to spatial_ref_value
					    	}
					    }
					]
				} // END spatial_reference
			] 
  		} // END t01_object


mapToRecord(mappingDescription, document, recordNode);


  	
function mapToRecord(mapping, document, refNode) {
	log.debug("Working on mapping for table " + mapping.table);
	
		var baseNode = refNode
		// iterate over all field descriptions, create column from xpath value
		for (var j in mapping.fieldMappings) {
			var fm = mapping.fieldMappings[j];
			// check for execution (special function)
			if (hasValue(fm.execute)) {
				log.debug("Execute function: " + fm.execute.funct.name + "on node '" + baseNode.nodeName + "'")
				var args = new Array(document, baseNode);
				if (hasValue(fm.execute.params)) {
					args = args.concat(fm.execute.params);
				}
				call_f(fm.execute.funct, args)
			} else {
				log.debug("Working on " + mapping.table + "." + fm.field + " with xpath:'" + fm.xpath + "' from parent node '" + refNode.nodeName + "'")
				// iterate over all xpath results
				var nodeList = XPathUtils.getNodeList(baseNode, fm.xpath);
				for (k=0; k<nodeList.getLength(); k++ ) {
					var value = nodeList.item(k).getTextContent()
					// check for transformation
					if (hasValue(fm.transform)) {
						var args = new Array(value);
						if (hasValue(fm.transform.params)) {
							args = args.concat(fm.transform.params);
						}
						value = call_f(fm.transform.funct,args);
					}
					var indexFieldName;
					if (hasValue(fm.indexName)) {
						indexFieldName = fm.indexName;
					} else {
						indexFieldName = mapping.table + "." + fm.field;
					}
					if (hasValue(value)) {
						log.debug("adding '" + indexFieldName + "' = '" + value + "' to ingrid document.");
						document.addColumn(createColumn(mapping.table, fm.field, indexFieldName), value)
					}
				}
			}
		}
		// work on sub records mappings
		if (hasValue(mapping.subrecords)) {
			for (var j in mapping.subrecords) {
				var subRecordMapping = mapping.subrecords[j];
				var baseNodeList;
				if (hasValue(subRecordMapping.xpath)) {
					baseNodeList = XPathUtils.getNodeList(refNode, subRecordMapping.xpath);
					log.debug("Select " + baseNodeList.getLength() + " nodes with xpath:'" + subRecordMapping.xpath + "' from parent path '" + mapping.xpath + "'");
				} else {
					baseNodeList = XPathUtils.getNodeList(refNode, ".");
					log.debug("Select " + baseNodeList.getLength() + " nodes with xpath:'.' from parent path '" + mapping.xpath + "'");
				}
				for (var k=0; k<baseNodeList.getLength(); k++ ) {
					var baseNode = baseNodeList.item(k)
					var subRecord = mapToRecord(subRecordMapping, new Record(), baseNode);
					if (subRecord.getColumns().length > 0) {
						if (subRecordMapping.line) {
							// automatically add line column to table mapping
							log.debug("adding '" + subRecordMapping.table + ".line" + "' = '" + (k+1) + "' to ingrid document.");
							subRecord.addColumn(createColumn(subRecordMapping.table, "line", subRecordMapping.table + ".line"), (k+1))
						}
						document.addSubRecord(subRecord);
					}
				}

			}
		}
		
	return document;
}

function mapGeographicElements(document, refNode) {
	var geographicElements = XPathUtils.getNodeList(refNode, "//identificationInfo//extent/EX_Extent/geographicElement");
	if (hasValue(geographicElements)) {
		var lineCounter = 1;
		for (var i=0; i<geographicElements.getLength(); i++ ) {
			var value = XPathUtils.getString(geographicElements.item(i), "EX_GeographicDescription/geographicIdentifier/MD_Identifier/code/CharacterString");
			if (hasValue(value)) {
				var spatialReferenceRecord = new Record();
				log.debug("adding 'spatial_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				spatialReferenceRecord.addColumn(createColumn("spatial_reference", "line", "spatial_reference.line"), (lineCounter))
				lineCounter++;
				document.addSubRecord(spatialReferenceRecord);
				var spatialRefValueRecord = new Record();
				log.debug("adding 'spatial_ref_value.name_value" + "' = '" + value + "' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "name_value", "spatial_ref_value.name_value"), value)
				log.debug("adding 'spatial_ref_value.x1" + "' = '' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "x1", "x1"), "")
				log.debug("adding 'spatial_ref_value.x2" + "' = '' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "x2", "x2"), "")
				log.debug("adding 'spatial_ref_value.y1" + "' = '' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "y1", "y1"), "")
				log.debug("adding 'spatial_ref_value.y2" + "' = '' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "y2", "y2"), "")
				spatialReferenceRecord.addSubRecord(spatialRefValueRecord);
			}
			var boundingBoxes = XPathUtils.getNodeList(geographicElements.item(i), "EX_GeographicBoundingBox");
			for (j=0; j<boundingBoxes.getLength(); j++ ) {
				if (hasValue(boundingBoxes.item(j)) && hasValue(XPathUtils.getString(boundingBoxes.item(j), "westBoundLongitude/Decimal"))) {
					var spatialReferenceRecord = new Record();
					log.debug("adding 'spatial_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
					spatialReferenceRecord.addColumn(createColumn("spatial_reference", "line", "spatial_reference.line"), (lineCounter))
					lineCounter++;
					document.addSubRecord(spatialReferenceRecord);
					var spatialRefValueRecord = new Record();
					log.debug("adding 'spatial_ref_value.name_value" + "' = '' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "name_value", "spatial_ref_value.name_value"), "")
					log.debug("adding 'spatial_ref_value.x1" + "' = '" + XPathUtils.getString(boundingBoxes.item(j), "westBoundLongitude/Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "x1", "x1"), XPathUtils.getString(boundingBoxes.item(j), "westBoundLongitude/Decimal"))
					log.debug("adding 'spatial_ref_value.x2" + "' = '" + XPathUtils.getString(boundingBoxes.item(j), "eastBoundLongitude/Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "x2", "x2"), XPathUtils.getString(boundingBoxes.item(j), "eastBoundLongitude/Decimal"))
					log.debug("adding 'spatial_ref_value.y1" + "' = '" + XPathUtils.getString(boundingBoxes.item(j), "southBoundLatitude/Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "y1", "y1"), XPathUtils.getString(boundingBoxes.item(j), "southBoundLatitude/Decimal"))
					log.debug("adding 'spatial_ref_value.y2" + "' = '" + XPathUtils.getString(boundingBoxes.item(j), "northBoundLatitude/Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "y2", "y2"), XPathUtils.getString(boundingBoxes.item(j), "northBoundLatitude/Decimal"))
					spatialReferenceRecord.addSubRecord(spatialRefValueRecord);
				}
			}
		}
	}
}


function mapKeywords(document, refNode) {
	var usedKeywords = "";
	var lineCounter = 1;
	// check for INSPIRE themes
	var keywords = XPathUtils.getNodeList(refNode, "//identificationInfo//descriptiveKeywords/MD_Keywords[thesaurusName/CI_Citation/title/CharacterString='GEMET - INSPIRE themes, version 1.0']/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (var i=0; i<keywords.getLength(); i++ ) {
			log.debug("keywords:" + keywords.nodeName)
			var value = keywords.item(i).getTextContent().trim()
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				log.debug("adding 't04_search.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				var searchtermObjRecord = new Record();
				searchtermObjRecord.addColumn(createColumn("searchterm_obj", "line", "t04_search.line"), (lineCounter))
				lineCounter++;
				document.addSubRecord(searchtermObjRecord);
				var searchtermValueRecord = new Record();
				log.debug("adding 't04_search.searchterm' = '" + value + "' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "term", "t04_search.searchterm"), value)
				log.debug("adding 't04_search.type' = 'I' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "type", "t04_search.type"), "I")
				usedKeywords+=value+";"
				searchtermObjRecord.addSubRecord(searchtermValueRecord);
			}
		}
	}
	// check for GEMET keywords
	var keywords = XPathUtils.getNodeList(refNode, "//identificationInfo//descriptiveKeywords/MD_Keywords[thesaurusName/CI_Citation/title/CharacterString='GEMET - Concepts, version 2.1']/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim()
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				log.debug("adding 't04_search.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				var searchtermObjRecord = new Record();
				searchtermObjRecord.addColumn(createColumn("searchterm_obj", "line", "t04_search.line"), (lineCounter))
				lineCounter++;
				document.addSubRecord(searchtermObjRecord);
				var searchtermValueRecord = new Record();
				log.debug("adding 't04_search.searchterm' = '" + value + "' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "term", "t04_search.searchterm"), value)
				log.debug("adding 'searchterm_value.type' = 'G' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "type", "t04_search.type"), "G")
				usedKeywords+=value+";"
				searchtermObjRecord.addSubRecord(searchtermValueRecord);
			}
		}
	}
	// check for UMTHES keywords
	var keywords = XPathUtils.getNodeList(refNode, "//identificationInfo//descriptiveKeywords/MD_Keywords[thesaurusName/CI_Citation/title/CharacterString='UMTHES Thesaurus']/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim()
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				log.debug("adding 't04_search.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				var searchtermObjRecord = new Record();
				searchtermObjRecord.addColumn(createColumn("searchterm_obj", "line", "t04_search.line"), (lineCounter))
				lineCounter++;
				document.addSubRecord(searchtermObjRecord);
				var searchtermValueRecord = new Record();
				log.debug("adding 't04_search.searchterm' = '" + value + "' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "term", "t04_search.searchterm"), value)
				log.debug("adding 'searchterm_value.type' = 'T' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "type", "t04_search.type"), "T")
				usedKeywords+=value+";"
				searchtermObjRecord.addSubRecord(searchtermValueRecord);
			}
		}
	}
	// check for other keywords
	var keywords = XPathUtils.getNodeList(refNode, "//identificationInfo//descriptiveKeywords/MD_Keywords/keyword/CharacterString");
	if (hasValue(keywords)) {
		for (i=0; i<keywords.getLength(); i++ ) {
			var value = keywords.item(i).getTextContent().trim();
			if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
				log.debug("adding 't04_search.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				var searchtermObjRecord = new Record();
				searchtermObjRecord.addColumn(createColumn("searchterm_obj", "line", "t04_search.line"), (lineCounter))
				lineCounter++;
				document.addSubRecord(searchtermObjRecord);
				var searchtermValueRecord = new Record();
				log.debug("adding 't04_search.searchterm' = '" + value + "' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "term", "t04_search.searchterm"), value)
				log.debug("adding 't04_search.type' = 'F' to ingrid document.");
				searchtermValueRecord.addColumn(createColumn("searchterm_value", "type", "t04_search.type"), "F")
				usedKeywords+=value+";"
				searchtermObjRecord.addSubRecord(searchtermValueRecord);
			}
		}
	}
}


function mapReferences(document, refNode) {
	// check for coupled resources, bound to a specific operation in services
	var usedUuids="";
	var lineCounter = 1;
	var coupledResources = XPathUtils.getNodeList(refNode, "//identificationInfo/SV_ServiceIdentification/coupledResource/SV_CoupledResource/identifier/CharacterString");
	if (hasValue(coupledResources)) {
		for (i=0; i<coupledResources.getLength(); i++ ) {
			var value = coupledResources.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3345") == -1) {
				log.debug("adding 'object_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "line", "object_reference" + ".line"), (lineCounter))
				log.debug("adding 'object_reference.obj_to_uuid' = '" + value + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "obj_to_uuid", "object_reference.obj_to_uuid"), value)
				log.debug("adding 'object_reference.special_ref' = '3345' to ingrid document.");
				document.addColumn(createColumn("object_reference", "special_ref", "object_reference.special_ref"), "3345")
				lineCounter++;
				usedUuids+=value+"3345;"
			}
		}
	}
	// check for coupled resources (operatedOn)
	var operatesOn = XPathUtils.getNodeList(refNode, "//identificationInfo/SV_ServiceIdentification/operatesOn/@uuidref");
	if (hasValue(operatesOn)) {
		for (i=0; i<operatesOn.getLength(); i++ ) {
			var value = operatesOn.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3345") == -1) {
				log.debug("adding 'object_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "line", "object_reference" + ".line"), (lineCounter))
				log.debug("adding 'object_reference.obj_to_uuid' = '" + value + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "obj_to_uuid", "object_reference.obj_to_uuid"), value)
				log.debug("adding 'object_reference.special_ref' = '3345' to ingrid document.");
				document.addColumn(createColumn("object_reference", "special_ref", "object_reference.special_ref"), "3345")
				lineCounter++;
				usedUuids+=value+"3345;"
			}
		}
	}
	// check for content info references (Schlüsselkatalog)
	var operatesOn = XPathUtils.getNodeList(refNode, "//contentInfo/@uuidref");
	if (hasValue(operatesOn)) {
		for (i=0; i<operatesOn.getLength(); i++ ) {
			var value = operatesOn.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3535") == -1) {
				log.debug("adding 'object_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "line", "object_reference" + ".line"), (lineCounter))
				log.debug("adding 'object_reference.obj_to_uuid' = '" + value + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "obj_to_uuid", "object_reference.obj_to_uuid"), value)
				log.debug("adding 'object_reference.special_ref' = '3535' to ingrid document.");
				document.addColumn(createColumn("object_reference", "special_ref", "object_reference.special_ref"), "3535")
				lineCounter++;
				usedUuids+=value+"3535;"
			}
		}
	}
	// check for portrayalCatalogue info references (Symbolkatalog)
	var operatesOn = XPathUtils.getNodeList(refNode, "//contentInfo/@uuidref");
	if (hasValue(operatesOn)) {
		for (i=0; i<operatesOn.getLength(); i++ ) {
			var value = operatesOn.item(i).getTextContent()
			if (hasValue(value) && usedUuids.indexOf(value+"3555") == -1) {
				log.debug("adding 'object_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "line", "object_reference" + ".line"), (lineCounter))
				log.debug("adding 'object_reference.obj_to_uuid' = '" + value + "' to ingrid document.");
				document.addColumn(createColumn("object_reference", "obj_to_uuid", "object_reference.obj_to_uuid"), value)
				log.debug("adding 'object_reference.special_ref' = '3555' to ingrid document.");
				document.addColumn(createColumn("object_reference", "special_ref", "object_reference.special_ref"), "3555")
				lineCounter++;
				usedUuids+=value+"3555;"
			}
		}
	}
}


function mapReferenceSystemInfo(document, refNode) {
	var rsIdentifiers = XPathUtils.getNodeList(refNode, "//referenceSystemInfo/MD_ReferenceSystem/referenceSystemIdentifier/RS_Identifier");
	if (hasValue(rsIdentifiers)) {
		for (i=0; i<rsIdentifiers.getLength(); i++ ) {
			var code = XPathUtils.getString(rsIdentifiers.item(i), "code/CharacterString");
			var codeSpace = XPathUtils.getString(rsIdentifiers.item(i), "codeSpace/CharacterString");
			if (hasValue(codeSpace) && hasValue(code)) {
				log.debug("adding '" + "t011_obj_geo.referencesystem_id" + "' = '" + codeSpace+":"+code + "' to ingrid document.");
				document.addColumn(createColumn("t011_obj_geo", "referencesystem_value", "t011_obj_geo.referencesystem_id"), codeSpace+":"+code)
			} else if (hasValue(code)) {
				log.debug("adding '" + "t011_obj_geo.referencesystem_id" + "' = '" + code + "' to ingrid document.");
				document.addColumn(createColumn("t011_obj_geo", "referencesystem_value", "t011_obj_geo.referencesystem_id"), code)
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

function getObjectClassFromHierarchyLevel(val) {
	// default to "Geo-Information / Karte"
	var result = "1"; 
	if (hasValue(val) && val.toLowerCase() == "service") {
		// "Dienst / Anwendung / Informationssystem"
		result = "3";
	}
	return result;
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



function createColumn(table, field, idxName) {
	return MapperUtils.createColumn(table, field, idxName, Column.TEXT, true)
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
