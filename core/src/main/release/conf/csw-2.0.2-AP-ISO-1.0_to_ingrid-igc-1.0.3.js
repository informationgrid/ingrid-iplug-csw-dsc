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
importPackage(Packages.de.ingrid.utils.xml);

log.debug("Mapping csw record "+cswRecord.getId()+" to ingrid document");

// get the xml content of the record
var recordNode = cswRecord.getOriginalResponse();

var mappingDescription = 
  		{	"table":"t01_object",
  			"fieldMappings":[
	  			{
					"field":"obj_uuid",
					"indexName":"t01_object.obj_id",
					"xpath":"//gmd:fileIdentifier/gco:CharacterString"
				},
				{
					"field":"obj_name",
					"indexName":"title",
					"xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"
				},
				{
					"field":"org_obj_id",
					"xpath":"//gmd:fileIdentifier/gco:CharacterString"
				},
				{
					"field":"obj_descr",
					"indexName":"summary",
					"xpath":"//gmd:identificationInfo//gmd:abstract/gco:CharacterString"
				},
				{
					"field":"info_note",
					"xpath":"//gmd:identificationInfo//gmd:purpose/gco:CharacterString"
				},
				{
					"field":"loc_descr",
					"xpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:description/gco:CharacterString"
				},
				{
					"field":"dataset_alternate_name",
					"xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString"
				},
				{
					"field":"time_descr",
					"xpath":"//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceNote/gco:CharacterString"
				},
				{
					"field":"time_status",
					"xpath":"//gmd:identificationInfo//gmd:status/gmd:MD_ProgressCode/@codeListValue",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[523]
					}
				},
				{
					"field":"obj_class",
					"xpath":"//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
					"defaultValue":"dataset",
					"transform":{
						"funct":getObjectClassFromHierarchyLevel
					}
				},
				{
					"field":"dataset_character_set",
					"xpath":"//gmd:identificationInfo//gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[510]
					}
				},
				{
					"field":"dataset_usage",
					"xpath":"//gmd:identificationInfo//gmd:resourceSpecificUsage/gmd:MD_Usage/gmd:specificUsage/gco:CharacterString"
				},
				{
					"field":"data_language_code",
					"xpath":"//gmd:identificationInfo//gmd:language/gmd:LanguageCode/@codeListValue",
					"transform":{
						"funct":transformISO639_2ToISO639_1
					}
				},
				{
					"field":"metadata_character_set",
					"xpath":"//gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[510]
					}
				},
				{
					"field":"metadata_standard_name",
					"xpath":"//gmd:metadataStandardName/gco:CharacterString"
				},
				{
					"field":"metadata_standard_version",
					"xpath":"//gmd:metadataStandardVersion/gco:CharacterString"
				},
				{
					"field":"metadata_language_code",
					"xpath":"gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue",
					"transform":{
						"funct":transformISO639_2ToISO639_1
					}
				},
				{
					"field":"vertical_extent_minimum",
					"xpath":"//gmd:identificationInfo//gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real"
				},
				{
					"field":"vertical_extent_maximum",
					"xpath":"//gmd:identificationInfo//gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real"
				},
				{
					"field":"vertical_extent_unit",
					"xpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gmd:VerticalCRS/gmd:verticalCS/gml:VerticalCS/gml:axis/gml:CoordinateSystemAxis/@uom",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[102]
					}
				},
				{
					"field":"vertical_extent_vdatum",
					"xpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gml:VerticalCRS/gml:verticalDatum/gml:VerticalDatum/gml:identifier",
					"transform":{
						"funct":transformToIgcDomainId,
						"params":[101, true]
					}
				},
				{
					"field":"ordering_instructions",
					"xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:orderingInstructions/gco:CharacterString"
				},
				{
					"field":"mod_time",
					"xpath":"//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]",
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
		    	},
				{
					"execute":{
						"funct":mapAddressInformation
					}
		    	},
				{
					"execute":{
						"funct":mapTimeConstraints
					}
		    	},
		    	{
					"execute":{
						"funct":addResourceMaintenance
		    		}
		    	}
			],
			"subrecords":[
				{
			    "table":"object_access",
			    "xpath":"//gmd:identificationInfo//gmd:resourceConstraints",
				"line":true,
			    "fieldMappings":[
	    	  		{
    					"field":"restriction_key",
    					"xpath":"//gmd:otherConstraints/gco:CharacterString",
    					"transform":{
    						"funct":transformToIgcDomainId,
    						"params":[6010]
    					}
	    			},
	    	  		{
    					"field":"restriction_value",
    					"xpath":"//gmd:otherConstraints/gco:CharacterString"
	    			},
	    	  		{
    					"field":"terms_of_use",
    					"xpath":"//gmd:useLimitation/gco:CharacterString"
	    			}
	    	  	]
				}, // END object_access
			    {
			    "table":"t0110_avail_format",
			    "xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format",
				"line":true,
			    "fieldMappings":[
	    	  		{
    					"field":"format_value",
    					"indexName":"t0110_avail_format.name",
    					"xpath":"gmd:name/gco:CharacterString"
	    			},
	    	  		{
    					"field":"ver",
    					"indexName":"t0110_avail_format.version",
    					"xpath":"gmd:version/gco:CharacterString"
	    			},
	    	  		{
    					"field":"file_decompression_technique",
    					"xpath":"gmd:fileDecompressionTechnique/gco:CharacterString"
	    			},
	    	  		{
    					"field":"specification",
    					"xpath":"gmd:specification/gco:CharacterString"
	    			}
	    		]
				}, // END t0110_avail_format
				{
				    "table":"t0113_dataset_reference",
				    "xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date",
					"line":true,
				    "fieldMappings":[
		    	  		{
	    					"field":"reference_date",
	    					"xpath":"//gmd:date/gco:DateTime | //gmd:date/gco:Date[not(../gco:DateTime)]",
	    					"transform":{
		    	  				"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
		    	  			}
		    			},
		    	  		{
	    					"field":"type",
	    					"xpath":"gmd:dateType/gmd:CI_DateTypeCode/@codeListValue",
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
	    					"xpath":"//gmd:identificationInfo//srv:serviceType/gco:LocalName"
		    			},
		    	  		{
	    					"field":"history",
	    					"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString"
		    			},
		    	  		{
	    					"field":"base",
	    					"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString"
		    			}
		    		],
		    		"subrecords":[
						{
							"table":"t011_obj_serv_operation",
							"xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"name_value",
			    					"indexName":"t011_obj_serv_operation.name",
			    					"xpath":"srv:operationName/gco:CharacterString"
				    			},
				    	  		{
			    					"field":"descr",
			    					"xpath":"srv:operationDescription/gco:CharacterString"
				    			},
				    	  		{
			    					"field":"invocation_name",
			    					"xpath":"srv:invocationName/gco:CharacterString"
				    			}
							],
							"subrecords":[
								{
									"table":"t011_obj_serv_op_para",
									"xpath":"srv:parameter/srv:SV_Parameter",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"name",
					    					"xpath":"srv:name"
						    			},
						    	  		{
					    					"field":"direction",
					    					"xpath":"srv:direction/srv:SV_ParameterDirection"
						    			},
						    	  		{
					    					"field":"descr",
					    					"xpath":"srv:description/gco:CharacterString"
						    			},
						    	  		{
					    					"field":"optional",
					    					"xpath":"srv:optionality/gco:CharacterString",
					    					"transform":{
							    				"funct":transformGeneric,
							    				"params":[{"optional":"1", "mandatory":"0"}, false]
							    			}						    					
						    			},
						    	  		{
					    					"field":"repeatability",
					    					"xpath":"srv:repeatability/gco:Boolean",
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
					    					"xpath":"srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"
						    			}
									]
								}, // END t011_obj_serv_op_connpoint
								{
									"table":"t011_obj_serv_op_depends",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"depends_on",
					    					"xpath":"srv:dependsOn/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString"
						    			}
									]
								}, // END t011_obj_serv_op_depends
								{
									"table":"t011_obj_serv_op_platform",
									"line":true,
									"fieldMappings":[
						    	  		{
					    					"field":"platform",
					    					"xpath":"srv:DCP/srv:DCPList/@codeListValue"
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
			    					"xpath":"//gmd:identificationInfo//srv:serviceTypeVersion/gco:CharacterString"
				    			}
							]
						} // END t011_obj_serv_version
		    		]
				}, // END t011_obj_serv
				{
					"table":"t011_obj_topic_cat",
					"xpath":"//gmd:identificationInfo//gmd:topicCategory",
					"line":true,
					"fieldMappings":[
		    	  		{
	    					"field":"topic_category",
	    					"xpath":"gmd:MD_TopicCategoryCode",
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
	    					"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"
		    			},
		    	  		{
	    					"field":"data_base",
	    					"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString"
		    			},
		    	  		{
	    					"field":"method",
	    					"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString"
		    	  		},
		    	  		{
		    	  			"execute":{
		    	  				"funct":mapReferenceSystemInfo
		    	  			}
		    			},
		    			{	"field":"rec_exact",
		    				"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_RelativeInternalPositionalAccuracy/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"
		    			},
		    			{	"field":"rec_grade",
		    				"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_CompletenessCommission/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"
		    			},
		    			{	"field":"hierarchy_level",
		    				"xpath":"//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
							"transform":{
								"funct":transformGeneric,
								"params":[{"dataset":"5", "series":"6"}, false]
							}
		    			},
		    			{	"field":"vector_topology_level",
		    				"xpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:topologyLevel/gmd:MD_TopologyLevelCode/@codeListValue",
							"transform":{
								"funct":transformToIgcDomainId,
								"params":[528]
							}
		    			},
		    			{	"field":"pos_accuracy_vertical",
		    				"xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_RelativeInternalPositionalAccuracy[gmd:measureDescription/gco:CharacterString='vertical']/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"
		    			},
		    			{	"field":"keyc_incl_w_dataset",
		    				"xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:includedWithDataset/gco:Boolean",
							"transform":{
								"funct":transformGeneric,
								"params":[{"true":"1", "false":"0"}, false]
							}
		    			},
		    			{	"field":"datasource_uuid",
		    				// accept RS_Indentifier and MD_Identifier with xpath: "...identifier//code..."
		    				"xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier//gmd:code/gco:CharacterString"
				    	}
		    	  	],
		    	  	"subrecords":[
						{
							"table":"t011_obj_geo_keyc",
							"xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"keyc_value",
			    					"indexName":"t011_obj_geo_keyc.subject_cat",
			    					"xpath":"gmd:title/gco:CharacterString"
				    			},
				    			{
			    					"field":"key_date",
			    					"xpath":"gmd:date/gmd:CI_Date/gmd:date/gco:Date",
			    					"transform":{
			    						"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
			    					}
				    			},
				    			{
			    					"field":"edition",
			    					"xpath":"gmd:edition/gco:CharacterString"
				    			}
				    	  	]
						}, // END t011_obj_geo_keyc
						{
							"table":"t011_obj_geo_scale",
							"xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"scale",
			    					"xpath":"gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer"
				    			},
				    			{
			    					"field":"resolution_ground",
			    					"xpath":"gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gmd:Distance[@uom='meter']"
				    			},
				    			{
			    					"field":"resolution_scan",
			    					"xpath":"gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gmd:Distance[@uom='dpi']"
				    			}
				    	  	]
						}, // END t011_obj_geo_scale
						{
							"table":"t011_obj_geo_spatial_rep",
							"xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"type",
			    					"xpath":"gmd:MD_SpatialRepresentationTypeCode/@codeListValue",
			    					"transform":{
			    						"funct":transformToIgcDomainId,
			    						"params":[526]
			    					}
				    			}
				    	  	]
						}, // END t011_obj_geo_spatial_rep
						{
							"table":"t011_obj_geo_supplinfo",
							"xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"feature_type",
			    					"xpath":"gco:LocalName"
				    			}
				    	  	]
						}, // END t011_obj_geo_supplinfo
						{
							"table":"t011_obj_geo_symc",
							"xpath":"//gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"symbol_cat_value",
			    					"indexName":"t011_obj_geo_symc.symbol_cat",
			    					"xpath":"gmd:CI_Citation/gmd:title/gco:CharacterString"
				    			},
				    	  		{
			    					"field":"symbol_date",
			    					"xpath":"gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date",
									"transform":{
										"funct":UtilsCSWDate.mapDateFromIso8601ToIndex
									}
				    			},
				    	  		{
			    					"field":"edition",
			    					"xpath":"gmd:CI_Citation/gmd:edition/gco:CharacterString"
				    			}
				    	  	]
						}, // END t011_obj_geo_symc
						{
							"table":"t011_obj_geo_vector",
							"xpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:geometricObjects",
							"line":true,
							"fieldMappings":[
				    	  		{
			    					"field":"geometric_object_type",
			    					"xpath":"gmd:MD_GeometricObjects/gmd:geometricObjectType/gmd:MD_GeometricObjectTypeCode/@codeListValue",
									"transform":{
										"funct":transformToIgcDomainId,
										"params":[515]
									}
				    			},
				    	  		{
			    					"field":"geometric_object_count",
			    					"xpath":"gmd:MD_GeometricObjects/gmd:geometricObjectCount/gco:Integer"
				    			}
				    	  	]
						} // END t011_obj_geo_vector
					]
				}, // END t011_obj_geo
				{
					"table":"t017_url_ref",
					"xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource",
					"line":true,
					"fieldMappings":[
		    	  		{
	    					"field":"url_link",
	    					"xpath":"gmd:linkage/gmd:URL"
		    			},
		    	  		{
	    					"field":"content",
	    					"xpath":"gmd:name/gco:CharacterString"
		    			},
		    	  		{
	    					"field":"descr",
	    					"xpath":"gmd:description/gco:CharacterString"
		    			}
		    	  	]
				}, // END t017_url_ref
				{
					"table":"t017_url_ref",
					"xpath":"//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic",
					"line":false,
					"fieldMappings":[
		    	  		{
	    					"field":"url_link",
	    					"xpath":"gmd:fileName/gco:CharacterString"
		    			},
		    	  		{
	    					"field":"content",
	    					"xpath":"gmd:fileDescription/gco:CharacterString"
		    			}
		    	  	]
				}, // END t017_url_ref, MD_BrowseGraphic
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
					"table":"t0112_media_option",
					"xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:offLine/gmd:MD_Medium",
					"line":true,
					"fieldMappings":[
		    	  		{
	    					"field":"medium_note",
	    					"xpath":"gmd:mediumNote/gco:CharacterString"
		    			},
		    	  		{
	    					"field":"medium_name",
	    					"xpath":"gmd:name/gmd:MD_MediumNameCode/@codeListValue",
							"transform":{
								"funct":transformToIgcDomainId,
								"params":[520]
							}
		    			},
		    	  		{
	    					"field":"transfer_size",
	    					"xpath":"../../gmd:transferSize/gco:CharacterString"
		    			}
		    	  	]
				}, // END t0112_media_option
				{
					"table":"object_node",
					"fieldMappings":[
		    	  		{
	    					"field":"fk_obj_uuid",
	    					"indexName":"parent.object_node.obj_uuid",
	    					"xpath":"//gmd:parentIdentifier/gco:CharacterString"
		    			}
		    	  	]
				} // END object_node.parent.object_node.obj_uuid
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
				if (nodeList && nodeList.getLength() > 0) {
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
				} else {
					// no node found for this xpath
					if (fm.defaultValue) {
						value = fm.defaultValue;
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

function mapTimeConstraints(document, refNode) {
	var timePeriods = XPathUtils.getNodeList(refNode, "//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gmd:TimePeriod");
	log.debug("Found " + timePeriods.getLength() + " TimePeriod records.");
	if (hasValue(timePeriods)) {
		var beginPosition = getXPathValue(timePeriods.item(0), "gmd:beginPosition");
		var endPosition = getXPathValue(timePeriods.item(0), "gmd:endPosition");
		if (hasValue(beginPosition) && hasValue(endPosition)) {
			if (beginPosition.equals(endPosition)) {
				var t01ObjectRecord = new Record();
				t01ObjectRecord.addColumn(createColumn("t01_object", "id", "t01_object.id"), "undefined"); // needed for finding the subrecords in portal
				t01ObjectRecord.addColumn(createColumn("t01_object", "time_type", "t01_object.time_type"), "am");
				t01ObjectRecord.addColumn(createColumn("t01_object", "time_from", "t0"), UtilsCSWDate.mapDateFromIso8601ToIndex(beginPosition));
				document.addSubRecord(t01ObjectRecord);
			} else {
				var t01ObjectRecord = new Record();
				t01ObjectRecord.addColumn(createColumn("t01_object", "id", "t01_object.id"), "undefined"); // needed for finding the subrecords in portal
				t01ObjectRecord.addColumn(createColumn("t01_object", "time_type", "t01_object.time_type"), "von");
				t01ObjectRecord.addColumn(createColumn("t01_object", "time_from", "t1"),  UtilsCSWDate.mapDateFromIso8601ToIndex(beginPosition));
				t01ObjectRecord.addColumn(createColumn("t01_object", "time_to", "t2"),  UtilsCSWDate.mapDateFromIso8601ToIndex(endPosition));
				document.addSubRecord(t01ObjectRecord);
			}
		} else if (hasValue(beginPosition)) {
			var t01ObjectRecord = new Record();
			t01ObjectRecord.addColumn(createColumn("t01_object", "id", "t01_object.id"), "undefined"); // needed for finding the subrecords in portal
			t01ObjectRecord.addColumn(createColumn("t01_object", "time_type", "t01_object.time_type"), "seit");
			t01ObjectRecord.addColumn(createColumn("t01_object", "time_from", "t1"),  UtilsCSWDate.mapDateFromIso8601ToIndex(beginPosition));
			document.addSubRecord(t01ObjectRecord);
		} else if (hasValue(endPosition)) {
			var t01ObjectRecord = new Record();
			t01ObjectRecord.addColumn(createColumn("t01_object", "id", "t01_object.id"), "undefined"); // needed for finding the subrecords in portal
			t01ObjectRecord.addColumn(createColumn("t01_object", "time_type", "t01_object.time_type"), "bis");
			t01ObjectRecord.addColumn(createColumn("t01_object", "time_from", "t2"), UtilsCSWDate.mapDateFromIso8601ToIndex(endPosition));
			document.addSubRecord(t01ObjectRecord);
		}
	}
}

function addResourceMaintenance(document, refNode) {
	var maintenanceFrequencyCode = getXPathValue(refNode, "//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue");
	if (hasValue(maintenanceFrequencyCode)) {
		// transform to IGC domain id
		var idcCode = UtilsUDKCodeLists.getIgcIdFromIsoCodeListEntry(518, maintenanceFrequencyCode);
		if (hasValue(idcCode)) {
			document.addColumn(createColumn("t01_object", "time_period", "t01_object.time_period"), idcCode);
			var periodDuration = getXPathValue(recordNode, "//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:userDefinedMaintenanceFrequency/gmd:TM_PeriodDuration");
			document.addColumn(createColumn("t01_object", "time_interval", "t01_object.time_interval"), new TM_PeriodDurationToTimeInterval().parse(periodDuration));
			document.addColumn(createColumn("t01_object", "time_alle", "t01_object.time_alle"), new TM_PeriodDurationToTimeAlle().parse(periodDuration));
		} else {
			if (log.isDebugEnabled()) {
				log.debug("MD_MaintenanceFrequencyCode '" + maintenanceFrequencyCode + "' unknown.")
			}
		}
	}
}



function mapAddressInformation(document, refNode) {
	var ciResponsibleParties = XPathUtils.getNodeList(refNode, "//*/gmd:CI_ResponsibleParty");
	log.debug("Found " + ciResponsibleParties.getLength() + " CI_ResponsibleParty records.");
	if (hasValue(ciResponsibleParties)) {
		var lineCounter = 1;
		for (var i=0; i<ciResponsibleParties.getLength(); i++ ) {
			var role = getXPathValue(ciResponsibleParties.item(i), "gmd:role/gmd:CI_RoleCode/@codeListValue");
			log.debug("rp role " + role);
			if (hasValue(role)) {
				var t012ObjAddrRecord = new Record();
				t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "line", "t012_obj_adr.line"), (lineCounter))
				t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "adr_uuid", "t012_obj_adr.adr_id"), "undefined")
				var roleId = transformToIgcDomainId(role, 505);
				if (roleId != -1) {
					t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "type", "t012_obj_adr.typ"), (roleId))
					t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "special_ref", "t012_obj_adr.special_ref"), 0)
					t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "special_name", "t012_obj_adr.special_name"), role)
				} else {
					t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "type", "t012_obj_adr.typ"), -1)
					t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "special_ref", "t012_obj_adr.special_ref"), 0)
					t012ObjAddrRecord.addColumn(createColumn("t012_obj_adr", "special_name", "t012_obj_adr.special_name"), role)
				}
				var addressNodeRecord = new Record();
				addressNodeRecord.addColumn(createColumn("address_node", "fk_addr_uuid", "parent.address_node.addr_uuid"), "undefined")
				var t02AddressRecord = new Record();
				t02AddressRecord.addColumn(createColumn("t02_address", "adr_uuid", "t02_address.adr_id"), "undefined")
				t02AddressRecord.addColumn(createColumn("t02_address", "adr_type", "t02_address.typ"), 3); // freie Adresse
				t02AddressRecord.addColumn(createColumn("t02_address", "institution", "t02_address.institution"), getXPathValue(ciResponsibleParties.item(i), "gmd:organisationName/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "lastname", "t02_address.lastname"), getXPathValue(ciResponsibleParties.item(i), "gmd:individualName/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "street", "t02_address.street"), getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "postcode", "t02_address.postcode"), getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "city", "t02_address.city"), getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "country_code", "t02_address.country_code"), getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "job", "t02_address.job"), getXPathValue(ciResponsibleParties.item(i), "gmd:positionName/gco:CharacterString"));
				t02AddressRecord.addColumn(createColumn("t02_address", "descr", "t02_address.descr"), getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:contactInstructions/gco:CharacterString"));
				
				var communicationLine = 1;
				// phone
				var commValue = getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString")
				if (hasValue(commValue)) {
					var t012CommunicationRecord = new Record();
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "line", "t021_communication.line"), (communicationLine))
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_key", "t021_communication.commtype_key"), 1)
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_value", "t021_communication.comm_type"), "Telefon")
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "comm_value", "t021_communication.comm_value"), commValue)
					t02AddressRecord.addSubRecord(t012CommunicationRecord)
					communicationLine++;
				}
				// fax
				commValue = getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString")
				if (hasValue(commValue)) {
					var t012CommunicationRecord = new Record();
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "line", "t021_communication.line"), (communicationLine))
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_key", "t021_communication.commtype_key"), 2)
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_value", "t021_communication.comm_type"), "Fax")
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "comm_value", "t021_communication.comm_value"), commValue)
					t02AddressRecord.addSubRecord(t012CommunicationRecord)
					communicationLine++;
				}
				// email
				commValue = getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString")
				if (hasValue(commValue)) {
					var t012CommunicationRecord = new Record();
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "line", "t021_communication.line"), (communicationLine))
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_key", "t021_communication.commtype_key"), 3)
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_value", "t021_communication.comm_type"), "Email")
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "comm_value", "t021_communication.comm_value"), commValue)
					t02AddressRecord.addSubRecord(t012CommunicationRecord)
					communicationLine++;
				}
				// URL
				commValue = getXPathValue(ciResponsibleParties.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL")
				if (hasValue(commValue)) {
					var t012CommunicationRecord = new Record();
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "line", "t021_communication.line"), (communicationLine))
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_key", "t021_communication.commtype_key"), 4)
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "commtype_value", "t021_communication.comm_type"), "URL")
					t012CommunicationRecord.addColumn(createColumn("t021_communication", "comm_value", "t021_communication.comm_value"), commValue)
					t02AddressRecord.addSubRecord(t012CommunicationRecord)
					communicationLine++;
				}
				
				addressNodeRecord.addSubRecord(t02AddressRecord)
				t012ObjAddrRecord.addSubRecord(addressNodeRecord);
				document.addSubRecord(t012ObjAddrRecord);
			}
		}
	}
}


function mapGeographicElements(document, refNode) {
	var geographicElements = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo//gmd:extent/gmd:EX_Extent/gmd:geographicElement");
	if (hasValue(geographicElements)) {
		var lineCounter = 1;
		for (var i=0; i<geographicElements.getLength(); i++ ) {
			var value = getXPathValue(geographicElements.item(i), "gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
			if (hasValue(value)) {
				var spatialReferenceRecord = new Record();
				log.debug("adding 'spatial_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
				spatialReferenceRecord.addColumn(createColumn("spatial_reference", "line", "spatial_reference.line"), (lineCounter))
				lineCounter++;
				document.addSubRecord(spatialReferenceRecord);
				var spatialRefValueRecord = new Record();
				log.debug("adding 'spatial_ref_value.name_value" + "' = '" + value + "' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "name_value", "location"), value)
				log.debug("adding 'spatial_ref_value.type" + "' = 'F' to ingrid document.");
				spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "type", "spatial_ref_value.type"), "F")
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
			var boundingBoxes = XPathUtils.getNodeList(geographicElements.item(i), "gmd:EX_GeographicBoundingBox");
			for (j=0; j<boundingBoxes.getLength(); j++ ) {
				if (hasValue(boundingBoxes.item(j)) && hasValue(getXPathValue(boundingBoxes.item(j), "gmd:westBoundLongitude/gco:Decimal"))) {
					var spatialReferenceRecord = new Record();
					log.debug("adding 'spatial_reference.line" + "' = '" + (lineCounter) + "' to ingrid document.");
					spatialReferenceRecord.addColumn(createColumn("spatial_reference", "line", "spatial_reference.line"), (lineCounter))
					lineCounter++;
					document.addSubRecord(spatialReferenceRecord);
					var spatialRefValueRecord = new Record();
					log.debug("adding 'spatial_ref_value.name_value" + "' = '' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "name_value", "location"), "")
					log.debug("adding 'spatial_ref_value.type" + "' = 'F' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "type", "spatial_ref_value.type"), "F")
					log.debug("adding 'spatial_ref_value.x1" + "' = '" + getXPathValue(boundingBoxes.item(j), "gmd:westBoundLongitude/gco:Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "x1", "x1"), getXPathValue(boundingBoxes.item(j), "gmd:westBoundLongitude/gco:Decimal"))
					log.debug("adding 'spatial_ref_value.x2" + "' = '" + getXPathValue(boundingBoxes.item(j), "gmd:eastBoundLongitude/gco:Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "x2", "x2"), getXPathValue(boundingBoxes.item(j), "gmd:eastBoundLongitude/gco:Decimal"))
					log.debug("adding 'spatial_ref_value.y1" + "' = '" + getXPathValue(boundingBoxes.item(j), "gmd:southBoundLatitude/gco:Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "y1", "y1"), getXPathValue(boundingBoxes.item(j), "gmd:southBoundLatitude/gco:Decimal"))
					log.debug("adding 'spatial_ref_value.y2" + "' = '" + getXPathValue(boundingBoxes.item(j), "gmd:northBoundLatitude/gco:Decimal") + "' to ingrid document.");
					spatialRefValueRecord.addColumn(createColumn("spatial_ref_value", "y2", "y2"), getXPathValue(boundingBoxes.item(j), "gmd:northBoundLatitude/gco:Decimal"))
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
	var keywords = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='GEMET - INSPIRE themes, version 1.0']/gmd:keyword/gco:CharacterString");
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
	var keywords = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='GEMET - Concepts, version 2.1']/gmd:keyword/gco:CharacterString");
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
	var keywords = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='UMTHES Thesaurus']/gmd:keyword/gco:CharacterString");
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
	var keywords = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
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
	var coupledResources = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString");
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
	var operatesOn = XPathUtils.getNodeList(refNode, "//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn/@uuidref");
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
	// check for content info references (Schlï¿½sselkatalog)
	var operatesOn = XPathUtils.getNodeList(refNode, "//gmd:contentInfo/@uuidref");
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
	var operatesOn = XPathUtils.getNodeList(refNode, "//gmd:contentInfo/@uuidref");
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
	var rsIdentifiers = XPathUtils.getNodeList(refNode, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
	if (hasValue(rsIdentifiers)) {
		for (i=0; i<rsIdentifiers.getLength(); i++ ) {
			var code = getXPathValue(rsIdentifiers.item(i), "gmd:code/gco:CharacterString");
			var codeSpace = getXPathValue(rsIdentifiers.item(i), "gmd:codeSpace/gco:CharacterString");
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


function transformToIgcDomainId(val, codeListId, doReturnValueIfNotFound) {
	if (hasValue(val)) {
		// transform to IGC domain id
		var idcCode = UtilsUDKCodeLists.getIgcIdFromIsoCodeListEntry(codeListId, val);
		if (hasValue(idcCode)) {
			return idcCode;
		} else if (doReturnValueIfNotFound) {
			return val;
		} else {
			log.info("Domain code '" + val + "' unknown in code list " + codeListId + ".");
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

function getXPathValue(node, xpath) {
	var s = XPathUtils.getString(node, xpath);
	if (s == null) {
		return "";
	} else {
		return s;
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
