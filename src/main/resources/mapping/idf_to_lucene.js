/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
if (javaVersion.indexOf( "1.8" ) === 0) {
    load("nashorn:mozilla_compat.js");
}

importPackage(Packages.de.ingrid.iplug.csw.dsc.tools);
importPackage(Packages.de.ingrid.iplug.csw.dsc.index);
importPackage(Packages.de.ingrid.utils.udk);
importPackage(Packages.org.w3c.dom);

//constant to punish the rank of a service/data object, which has no coupled resource
var BOOST_NO_COUPLED_RESOURCE  = 0.9;
//constant to boost the rank of a service/data object, which has at least one coupled resource
var BOOST_HAS_COUPLED_RESOURCE = 1.0;


if (log.isDebugEnabled()) {
    log.debug("Mapping csw record "+cswRecord.getId()+" to lucene document");
}

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
     additionalTokenize: constant specifying additional method to tokenize value and write tokenized value to index
*/
var transformationDescriptions = [
    {   "indexField":"t01_object.obj_id",
        "tokenized":true,
        "xpath":"//gmd:fileIdentifier/gco:CharacterString"
    }, 
    {   "indexField":"title",
        "tokenized":true,
        "xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"
    },
    {   "indexField":"alternatetitle",
        "xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString"
    },
    {   "indexField":"t01_object.org_obj_id",
        "tokenized":true,
        "xpath":"//gmd:fileIdentifier/gco:CharacterString"
    },
    {   "indexField":"summary",
        "xpath":"//gmd:identificationInfo//gmd:abstract/gco:CharacterString"
    },
    {   "indexField":"t01_object.info_note",
        "xpath":"//gmd:identificationInfo//gmd:purpose/gco:CharacterString"
    },
    {   "indexField":"t01_object.loc_descr",
        "xpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:description/gco:CharacterString"
    },
    {   "indexField":"t01_object.dataset_alternate_name",
        "xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString"
    },
    {   "indexField":"t01_object.time_status",
        "xpath":"//gmd:identificationInfo//gmd:status/gmd:MD_ProgressCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[523]
        }
    },
    {   "indexField":"hierarchylevel",
        "xpath":"//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
    },
    {   "execute":{
            "funct":mapObjectClass,
            "params":[recordNode]
        }
    },
    {   "indexField":"t01_object.dataset_character_set",
        "xpath":"//gmd:identificationInfo//gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[510]
        }
    },
    {   "indexField":"t01_object.dataset_usage",
        "xpath":"//gmd:identificationInfo//gmd:resourceSpecificUsage/gmd:MD_Usage/gmd:specificUsage/gco:CharacterString"
    },
    {   "indexField":"t01_object.data_language_code",
        "xpath":"//gmd:identificationInfo//gmd:language/gco:CharacterString",
        "transform":{
            "funct":transformISO639_2ToISO639_1
        }
    },
    {   "indexField":"t01_object.metadata_character_set",
        "xpath":"//gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[510]
        }
    },
    {   "indexField":"t01_object.metadata_standard_name",
        "xpath":"//gmd:metadataStandardName/gco:CharacterString"
    },
    {   "indexField":"t01_object.metadata_standard_version",
        "xpath":"//gmd:metadataStandardVersion/gco:CharacterString"
    },
    {   "indexField":"t01_object.metadata_language_code",
        "xpath":"//gmd:language/gco:CharacterString",
        "transform":{
            "funct":transformISO639_2ToISO639_1
        }
    },
    {   "indexField":"t01_object.vertical_extent_minimum",
        "xpath":"//gmd:identificationInfo//gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real"
    },
    {   "indexField":"t01_object.vertical_extent_maximum",
        "xpath":"//gmd:identificationInfo//gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real"
    },
    {   "indexField":"t01_object.vertical_extent_unit",
        "xpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gmd:verticalCRS/gml:verticalCS/gml:VerticalCS/gml:axis/gml:CoordinateSystemAxis/@uom" +
            " | //gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gmd:verticalCRS/gml311:verticalCS/gml311:VerticalCS/gml311:axis/gml311:CoordinateSystemAxis/@uom",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[102]
        }
    },
    {   "indexField":"t01_object.vertical_extent_vdatum",
        "xpath":"//gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gml:verticalCRS/gml:verticalDatum/gml:VerticalDatum/gml:identifier" +
            " | //gmd:identificationInfo//gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS/gml311:verticalCRS/gml311:verticalDatum/gml311:VerticalDatum/gml311:identifier",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[101]
        }
    },
    {   "indexField":"t01_object.ordering_instructions",
        "xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributionOrderProcess/gmd:MD_StandardOrderProcess/gmd:orderingInstructions/gco:CharacterString"
    },
    {   "indexField":"t01_object.mod_time",
        "xpath":"//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]",
        "transform":{
            "funct":UtilsCSWDate.mapDateFromIso8601ToIndex
        }
    },
    {   "indexField":"modified",
        "xpath":"//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]",
        "transform":{
          "funct":UtilsCSWDate.mapDateFromIso8601ToIndex
        }
    },
    // object_access
    {   "indexField":"object_access.restriction_key",
        "xpath":"//gmd:identificationInfo//gmd:resourceConstraints//gmd:otherConstraints[../gmd:accessConstraints]/gco:CharacterString",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[6010]
        }
    },
    {   "indexField":"object_access.restriction_value",
        "xpath":"//gmd:identificationInfo//gmd:resourceConstraints//gmd:otherConstraints[../gmd:accessConstraints]/gco:CharacterString"
    },
    {   "indexField":"object_access.terms_of_use",
        "xpath":"//gmd:identificationInfo//gmd:resourceConstraints//gmd:useLimitation/gco:CharacterString"
    },
    // object_use
    {   "indexField":"object_use.terms_of_use_value",
        "xpath":"//gmd:identificationInfo//gmd:resourceConstraints//gmd:otherConstraints[../gmd:useLimitation]/gco:CharacterString"
    },
    // object_use_constraint
    {   "indexField":"object_use_constraint.license_value",
        "xpath":"//gmd:identificationInfo//gmd:resourceConstraints//gmd:otherConstraints[../gmd:useConstraints]/gmx:Anchor | //gmd:identificationInfo//gmd:resourceConstraints//gmd:otherConstraints[../gmd:useConstraints]/gco:CharacterString"
    },
    // t0110_avail_format
    {   "indexField":"t0110_avail_format.name",
        "xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString"
    },
    {   "indexField":"t0110_avail_format.version",
        "xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:version/gco:CharacterString"
    },
    {   "indexField":"t0110_avail_format.file_decompression_technique",
        "xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:fileDecompressionTechnique/gco:CharacterString"
    },
    {   "indexField":"t0110_avail_format.specification",
        "xpath":"//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:specification/gco:CharacterString"
    },
    // t0113_dataset_reference
    {   "indexField":"t0113_dataset_reference.reference_date",
        "xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date",
        "transform":{
            "funct":UtilsCSWDate.mapDateFromIso8601ToIndex
        }
    },
    {   "indexField":"t0113_dataset_reference.type",
        "xpath":"//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[502]
        }
    },
    //  t011_obj_serv
    {   "indexField":"t011_obj_serv.type",
        "xpath":"//gmd:identificationInfo//srv:serviceType/gco:LocalName"
    },
    {   "indexField":"t011_obj_serv.history",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString"
    },
    {   "indexField":"t011_obj_serv.base",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/source/LI_Source/gmd:description/gco:CharacterString"
    },
    // t011_obj_serv_op_connpoint
    {   "indexField":"t011_obj_serv_op_connpoint.connect_point",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL",
        "additionalTokenize":"SPLIT_URL"
    },
    {   "indexField":"capabilities_url",
        "xpath":"//gmd:identificationInfo/srv:SV_ServiceIdentification[./srv:serviceType/gco:LocalName/text() = 'WMS' or ./srv:serviceType/gco:LocalName/text() = 'view']//srv:containsOperations/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString[text() = 'GetCapabilities']/../../srv:connectPoint//gmd:URL",
        "additionalTokenize":"SPLIT_URL"
    },
    // t011_obj_serv_op_depends
    {   "indexField":"t011_obj_serv_op_depends.depends_on",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:dependsOn/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString"
    },
    // t011_obj_serv_op_para
    {   "indexField":"t011_obj_serv_op_para.name",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:parameters/srv:SV_Parameter/srv:name"
    },
    {   "indexField":"t011_obj_serv_op_para.direction",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:parameters/srv:SV_Parameter/direction/SV_ParameterDirection"
    },
    {   "indexField":"t011_obj_serv_op_para.descr",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:parameters/srv:SV_Parameter/gmd:description/gco:CharacterString"
    },
    {   "indexField":"t011_obj_serv_op_para.optional",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:parameters/srv:SV_Parameter/srv:optionality/gco:CharacterString",
        "transform":{
            "funct":transformGeneric,
            "params":[{"optional":"1", "mandatory":"0"}, false]
        }            
    },
    {   "indexField":"t011_obj_serv_op_para.repeatability",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:parameters/srv:SV_Parameter/srv:repeatability/gco:Boolean",
        "transform":{
            "funct":transformGeneric,
            "params":[{"true":"1", "false":"0"}, false]
        }            
    },
    // t011_obj_serv_op_platform
    {   "indexField":"t011_obj_serv_op_platform.platform",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:DCP/srv:DCPList/@codeListValue"
    },
    // t011_obj_serv_operation
    {   "indexField":"t011_obj_serv_operation.name",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:operationName/gco:CharacterString"
    },
    {   "indexField":"t011_obj_serv_operation.descr",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:operationDescription/gco:CharacterString"
    },
    {   "indexField":"t011_obj_serv_operation.invocation_name",
        "xpath":"//gmd:identificationInfo//srv:containsOperations/srv:SV_OperationMetadata/srv:invocationName/gco:CharacterString"
    },
    // t011_obj_serv_version
    {   "indexField":"t011_obj_serv_version.version_value",
        "xpath":"//gmd:identificationInfo//srv:serviceTypeVersion/gco:CharacterString"
    },
    // t011_obj_topic_cat
    {   "indexField":"t011_obj_topic_cat.topic_category",
        "xpath":"//gmd:identificationInfo//gmd:topicCategory/gmd:MD_TopicCategoryCode",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[527]
        }
    },
    // t011_obj_geo
    {   "indexField":"t011_obj_geo.special_base",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"
    },
    {   "indexField":"t011_obj_geo.data_base",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description/gco:CharacterString"
    },
    {   "indexField":"t011_obj_geo.method",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description/gco:CharacterString"
    },
    {   "execute":{
            "funct":mapReferenceSystemInfo
        }
    },
    {   "indexField":"t011_obj_geo.rec_exact",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/report/gmd:DQ_RelativeInternalPositionalAccuracy/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"
    },
    {   "indexField":"t011_obj_geo.rec_grade",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/report/DQ_CompletenessCommission/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"
    },
    {   "indexField":"t011_obj_geo.hierarchy_level",
        "xpath":"//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue",
        "transform":{
            "funct":transformGeneric,
            "params":[{"dataset":"5", "series":"6"}, false]
        }
    },
    {   "indexField":"t011_obj_geo.vector_topology_level",
        "xpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:topologyLevel/gmd:MD_TopologyLevelCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[528]
        }
    },
    {   "indexField":"t011_obj_geo.pos_accuracy_vertical",
        "xpath":"//gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_RelativeInternalPositionalAccuracy[gmd:measureDescription/gco:CharacterString='vertical']/gmd:DQ_QuantitativeResult/gmd:value/gmd:Record"
    },
    {   "indexField":"t011_obj_geo.keyc_incl_w_dataset",
        "xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:includedWithDataset/gco:Boolean",
        "transform":{
            "funct":transformGeneric,
            "params":[{"true":"1", "false":"0"}, false]
        }            
    },
    // accept RS_Indentifier and MD_Identifier with xpath: "...identifier//code..."
    {   "indexField":"t011_obj_geo.datasource_uuid",
        "xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier//gmd:code/gco:CharacterString"
    },
    // t011_obj_geo_keyc
    {   "indexField":"t011_obj_geo_keyc.subject_cat",
        "xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString"
    },
    {   "indexField":"t011_obj_geo_keyc.key_date",
        "xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date",
        "transform":{
            "funct":UtilsCSWDate.mapDateFromIso8601ToIndex
        }
    },
    {   "indexField":"t011_obj_geo_keyc.edition",
        "xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:edition/gco:CharacterString"
    },
    // t011_obj_geo_scale
    {   "indexField":"t011_obj_geo_scale.scale",
        "xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer"
    },
    {   "indexField":"t011_obj_geo_scale.resolution_ground",
        "xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gmd:Distance[@uom='meter']"
    },
    {   "indexField":"t011_obj_geo_scale.resolution_scan",
        "xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gmd:Distance[@uom='dpi']"
    },
    // t011_obj_geo_spatial_rep
    {   "indexField":"t011_obj_geo_spatial_rep.type",
        "xpath":"//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType/MD_SpatialRepresentationTypeCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[526]
        }
    },
    // t011_obj_geo_supplinfo
    {   "indexField":"t011_obj_geo_supplinfo.feature_type",
        "xpath":"//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription/gmd:featureTypes/gco:LocalName"
    },
    // t011_obj_geo_symc
    {   "indexField":"t011_obj_geo_symc.symbol_cat",
        "xpath":"//gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString"
    },
    {   "indexField":"t011_obj_geo_symc.symbol_date",
        "xpath":"//gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date",
        "transform":{
            "funct":UtilsCSWDate.mapDateFromIso8601ToIndex
        }
    },
    {   "indexField":"t011_obj_geo_symc.edition",
        "xpath":"//gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference/gmd:portrayalCatalogueCitation/gmd:CI_Citation/gco:CharacterString"
    },
    // t011_obj_geo_vector
    {   "indexField":"t011_obj_geo_vector.geometric_object_type",
        "xpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:geometricObjects/gmd:MD_GeometricObjects/gmd:geometricObjectType/gmd:MD_GeometricObjectTypeCode/@codeListValue",
        "transform":{
            "funct":transformToIgcDomainId,
            "params":[515]
        }
    },
    {   "indexField":"t011_obj_geo_vector.geometric_object_count",
        "xpath":"//gmd:spatialRepresentationInfo/gmd:MD_VectorSpatialRepresentation/gmd:geometricObjects/gmd:MD_GeometricObjects/gmd:geometricObjectCount/gco:Integer"
    },
    // t017_url_ref
    {   "execute":{
            "funct":mapOnlineResource,
            "params":[recordNode]
        }
    },
    // add MD_BrowseGraphic as additional html
    {   "execute":{
            "funct":transformToPreviewGraphic,
            "params":[recordNode]
        }
    },
    // object_references
    {   "execute":{
            "funct":mapReferences,
            "params":[recordNode]
        }
    },
    // keywords
    {   "execute":{
            "funct":mapKeywords,
            "params":[recordNode]
        }
    },
    // geographic elements
    {   "execute":{
            "funct":mapGeographicElements,
            "params":[recordNode]
        }
    },
    // time constraints
    {   "execute":{
            "funct":addTimeConstraints,
            "params":[recordNode]
        }
    },
    // resource maintenance
    {   "execute":{
            "funct":addResourceMaintenance,
            "params":[recordNode]
        }
    },
    // addresses 
    {   "execute":{
            "funct":mapAddresses,
            "params":[recordNode]
        }
    },
    {   "execute":{
            "funct":addCoupledServices,
            "params":[recordNode]
        }
    },
    {   "execute":{
            "funct":addObjectReference,
            "params":[recordNode]
        }
    },
    {   "execute":{
            "funct":addObjectReferenceTo,
            "params":[recordNode]
        }
    }
];

document.put( "datatype", "default" );
    
// iterate over all transformation descriptions
var value;
for (var i in transformationDescriptions) {
    var t = transformationDescriptions[i];
    
    // check for execution (special function)
    if (hasValue(t.execute)) {
        if (log.isDebugEnabled()) {
            log.debug("Execute function: " + t.execute.funct.name)
        }
        call_f(t.execute.funct, t.execute.params)
    } else {
        if (log.isDebugEnabled()) {
            log.debug("Working on " + t.indexField)
        }
        var tokenized = true;
        // iterate over all xpath results
        var nodeList = XPathUtils.getNodeList(recordNode, t.xpath);
        if (nodeList && nodeList.getLength() > 0) {
            for (j=0; j<nodeList.getLength(); j++ ) {
                value = nodeList.item(j).getTextContent();
                if (log.isDebugEnabled()) {
                    log.debug("Found value '" + value + "'");
                }
                // check for transformation
                if (hasValue(t.transform) && hasValue(value)) {
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
                    addToDoc(t.indexField, value, tokenized, t.additionalTokenize);
                }
            }
        } else {
            // no node found for this xpath
            if (t.defaultValue) {
                value = t.defaultValue;
                // check for transformation
                if (hasValue(t.transform) && hasValue(value)) {
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
                    addToDoc(t.indexField, value, tokenized, t.additionalTokenize);
                }
            }
        }
    }
}

function mapAddresses(recordNode) {
    var addresses = XPathUtils.getNodeList(recordNode, "//*/idf:idfResponsibleParty");
    if (hasValue(addresses)) {
        if (log.isDebugEnabled()) {
            log.debug("number of found addresses:" + addresses.getLength())
        }
        for (i=0; i<addresses.getLength(); i++ ) {
            var addressRole = XPathUtils.getString(addresses.item(i), "gmd:role/gmd:CI_RoleCode/@codeListValue");
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
                addToDoc("t02_address.institution", XPathUtils.getString(addresses.item(i), "gmd:organisationName/gco:CharacterString"), true);
                addToDoc("t02_address.lastname", XPathUtils.getString(addresses.item(i), "gmd:individualName/gco:CharacterString"), true);
                addToDoc("t02_address.street", XPathUtils.getString(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"), true);
                addToDoc("t02_address.postcode", XPathUtils.getString(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"), true);
                addToDoc("t02_address.city", XPathUtils.getString(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"), true);
                addToDoc("t02_address.administrative_area_value", XPathUtils.getString(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:administrativeArea/gco:CharacterString"), false);
                addToDoc("t02_address.country_code", XPathUtils.getString(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"), true);
                addToDoc("t02_address.job", XPathUtils.getString(addresses.item(i), "positionName/gco:CharacterString"), true);
                addToDoc("t02_address.descr", XPathUtils.getString(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/contactInstructions/gco:CharacterString"), true);
                // map communication Data
                // phone
                var entries = XPathUtils.getNodeList(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString");
                if (hasValue(entries)) {
                    for (j=0; j<entries.getLength(); j++ ) {
                        addToDoc("t021_communication.comm_type", "Telefon", true);
                        addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
                    }
                }
                // fax
                var entries = XPathUtils.getNodeList(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString");
                if (hasValue(entries)) {
                    for (j=0; j<entries.getLength(); j++ ) {
                        addToDoc("t021_communication.comm_type", "Fax", true);
                        addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
                    }
                }
                // email
                var entries = XPathUtils.getNodeList(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
                if (hasValue(entries)) {
                    for (j=0; j<entries.getLength(); j++ ) {
                        addToDoc("t021_communication.comm_type", "Email", true);
                        addToDoc("t021_communication.comm_value", entries.item(j).getTextContent(), true);
                    }
                }
                // url
                var entries = XPathUtils.getNodeList(addresses.item(i), "gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
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

function mapOnlineResource(recordNode) {
    var onlineResources = XPathUtils.getNodeList(recordNode, "//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource");
    if (hasValue(onlineResources)) {
        for (i=0; i<onlineResources.getLength(); i++ ) {

            // first check type of CI_OnlineResource and filter
            // e.g. in GeoKatalog.WSV "localZipDownload" has to be filtered
            // (see https://redmine.wemove.com/issues/1745 / "AF-00448 GP4: GeoKatalog - iPlug - Download Link anzeigen")

            // NOTICE: Wrong downloads are already filtered by XSLT to IDF ! Index mapping receives IDF and not plain ISO !
            // So check for wrong downloads not necessary, we comment to avoid side effects with other catalogs ...
/*
            var codeListValue = XPathUtils.getString(onlineResources.item(i), "gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue");
            if (hasValue(codeListValue) && (
                    (codeListValue == 'localZipDownload') ||
                    (codeListValue == 'localImageDownloadTransform') ||
                    (codeListValue == 'offlineAccess') )) {

                if (log.isInfoEnabled()) {
                    var id = XPathUtils.getString(recordNode, "//gmd:fileIdentifier/gco:CharacterString");
                    log.info("" + id + ": found CI_OnlineResource of type '" + codeListValue + "', we skip this one !");
                }

                continue;
            }
*/
            // map CI_OnlineResource
            var urlLink = "";
            var content = "";
            var descr = "";
            var codeListValue = "";
            var specialRef = "";
            
            if(XPathUtils.getString(onlineResources.item(i), "gmd:linkage/gmd:URL") != null) {
              urlLink = XPathUtils.getString(onlineResources.item(i), "gmd:linkage/gmd:URL");
            }

            if(hasValue(urlLink)) {
                if(XPathUtils.getString(onlineResources.item(i), "gmd:name/gco:CharacterString") != null) {
                    content = XPathUtils.getString(onlineResources.item(i), "gmd:name/gco:CharacterString");
                }
                if(XPathUtils.getString(onlineResources.item(i), "gmd:description/gco:CharacterString") != null) {
                    descr = XPathUtils.getString(onlineResources.item(i), "gmd:description/gco:CharacterString");
                }
                if(XPathUtils.getString(onlineResources.item(i), "gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue") != null) {
                    codeListValue = XPathUtils.getString(onlineResources.item(i), "gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue");
                    if(transformToIgcDomainId(codeListValue, "2000")) {
                        specialRef = transformToIgcDomainId(codeListValue, "2000");
                    }
                }
                addToDoc("t017_url_ref.url_link", urlLink, true);
                addToDoc("t017_url_ref.content", content, true);
                addToDoc("t017_url_ref.descr", descr, true);
                addToDoc("t017_url_ref.special_ref", specialRef, true);
            }
        }
    }
}


function mapGeographicElements(recordNode) {
    var geographicElements = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//*/gmd:EX_Extent/gmd:geographicElement");
    if (hasValue(geographicElements)) {
        for (i=0; i<geographicElements.getLength(); i++ ) {
            var value = XPathUtils.getString(geographicElements.item(i), "gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
            if (hasValue(value)) {
                addToDoc("location", value, true);
            }
            var boundingBoxes = XPathUtils.getNodeList(geographicElements.item(i), "gmd:EX_GeographicBoundingBox");
            for (j=0; j<boundingBoxes.getLength(); j++ ) {
                if (hasValue(boundingBoxes.item(j)) && hasValue(XPathUtils.getString(boundingBoxes.item(j), "gmd:westBoundLongitude/gco:Decimal"))) {
                    addToDoc("location", "", true);
                    addNumericToDoc("x1", XPathUtils.getString(boundingBoxes.item(j), "gmd:westBoundLongitude/gco:Decimal"), false);
                    addNumericToDoc("x2", XPathUtils.getString(boundingBoxes.item(j), "gmd:eastBoundLongitude/gco:Decimal"), false);
                    addNumericToDoc("y1", XPathUtils.getString(boundingBoxes.item(j), "gmd:southBoundLatitude/gco:Decimal"), false);
                    addNumericToDoc("y2", XPathUtils.getString(boundingBoxes.item(j), "gmd:northBoundLatitude/gco:Decimal"), false);
                }
            }
        }
    }
}


function mapKeywords(recordNode) {
    var usedKeywords = "";
    // check for INSPIRE themes
    var keywords = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='GEMET - INSPIRE themes, version 1.0']/keyword/gco:CharacterString");
    if (hasValue(keywords)) {
        for (i=0; i<keywords.getLength(); i++ ) {
            var value = keywords.item(i).getTextContent().trim()
            if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
                addToDoc("t04_search.searchterm", value, true);
                addToDoc("t04_search.type", "I", false);
                usedKeywords+=value+";"
            }
        }
    }
    // check for GEMET keywords
    var keywords = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='GEMET - Concepts, version 2.1']/keyword/gco:CharacterString");
    if (hasValue(keywords)) {
        for (i=0; i<keywords.getLength(); i++ ) {
            var value = keywords.item(i).getTextContent().trim()
            if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
                addToDoc("t04_search.searchterm", value, true);
                addToDoc("t04_search.type", "G", false);
                usedKeywords+=value+";"
            }
        }
    }
    // check for UMTHES keywords
    var keywords = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString='UMTHES Thesaurus']/keyword/gco:CharacterString");
    if (hasValue(keywords)) {
        for (i=0; i<keywords.getLength(); i++ ) {
            var value = keywords.item(i).getTextContent().trim()
            if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
                addToDoc("t04_search.searchterm", value, true);
                addToDoc("t04_search.type", "T", false);
                usedKeywords+=value+";"
            }
        }
    }
    // check for other keywords
    var keywords = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");
    if (hasValue(keywords)) {
        for (i=0; i<keywords.getLength(); i++ ) {
            var value = keywords.item(i).getTextContent().trim();
            if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
                addToDoc("t04_search.searchterm", value, true);
                addToDoc("t04_search.type", "F", false);
                usedKeywords+=value+";"
            }
        }
    }
}

function mapKeywordsThesaurusName(recordNode) {
    var usedKeywords = "";
    var keywords = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString");
    if (hasValue(keywords)) {
        for (i=0; i<keywords.getLength(); i++ ) {
            var value = keywords.item(i).getTextContent().trim()
            if (hasValue(value) && usedKeywords.indexOf(value) == -1) {
                addToDoc("thesaurusname", value, false);
                usedKeywords+=value+";"
            }
        }
    }
}

function mapReferences(recordNode) {
    // check for coupled resources, bound to a specific operation in services
    var usedUuids="";
    var coupledResources = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:coupledResource/srv:SV_CoupledResource/srv:identifier/gco:CharacterString");
    if (hasValue(coupledResources)) {
        for (i=0; i<coupledResources.getLength(); i++ ) {
            var value = coupledResources.item(i).getTextContent()
            if (hasValue(value) && usedUuids.indexOf(value+"3345") == -1) {
                addToDoc("object_reference.obj_to_uuid", value, true);
                addToDoc("object_reference.obj_uuid", value, false);
                addToDoc("object_reference.obj_name", "", false);
                addToDoc("object_reference.obj_class", "", false);
                addToDoc("object_reference.type", "", false);
                addToDoc("object_reference.version", "", false);
                addToDoc("object_reference.special_ref", "3345", true);
                addToDoc("object_reference.special_name", "", false);
                usedUuids+=value+"3345;"
            }
        }
    }
    // check for coupled resources (operatedOn)
    var operatesOn = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo/srv:SV_ServiceIdentification/srv:operatesOn/@uuidref");
    if (hasValue(operatesOn)) {
        for (i=0; i<operatesOn.getLength(); i++ ) {
            var value = operatesOn.item(i).getTextContent()
            if (hasValue(value) && usedUuids.indexOf(value+"3345") == -1) {
                addToDoc("object_reference.obj_to_uuid", value, true);
                addToDoc("object_reference.obj_uuid", value, false);
                addToDoc("object_reference.obj_name", "", false);
                addToDoc("object_reference.obj_class", "", false);
                addToDoc("object_reference.type", "", false);
                addToDoc("object_reference.version", "", false);
                addToDoc("object_reference.special_ref", "3345", true);
                addToDoc("object_reference.special_name", "", false);
                usedUuids+=value+"3345;"
            }
        }
    }
    // check for content info references (Schluesselkatalog)
    var operatesOn = XPathUtils.getNodeList(recordNode, "//gmd:contentInfo/@uuidref");
    if (hasValue(operatesOn)) {
        for (i=0; i<operatesOn.getLength(); i++ ) {
            var value = operatesOn.item(i).getTextContent()
            if (hasValue(value) && usedUuids.indexOf(value+"3535") == -1) {
                addToDoc("object_reference.obj_to_uuid", value, true);
                addToDoc("object_reference.obj_uuid", value, false);
                addToDoc("object_reference.obj_name", "", false);
                addToDoc("object_reference.obj_class", "", false);
                addToDoc("object_reference.type", "", false);
                addToDoc("object_reference.version", "", false);
                addToDoc("object_reference.special_ref", "3535", true);
                addToDoc("object_reference.special_name", "", false);
                usedUuids+=value+"3535;"
            }
        }
    }
    // check for portrayalCatalogue info references (Symbolkatalog)
    var operatesOn = XPathUtils.getNodeList(recordNode, "//gmd:contentInfo/@uuidref");
    if (hasValue(operatesOn)) {
        for (i=0; i<operatesOn.getLength(); i++ ) {
            var value = operatesOn.item(i).getTextContent()
            if (hasValue(value) && usedUuids.indexOf(value+"3555") == -1) {
                addToDoc("object_reference.obj_to_uuid", value, true);
                addToDoc("object_reference.obj_uuid", value, false);
                addToDoc("object_reference.obj_name", "", false);
                addToDoc("object_reference.obj_class", "", false);
                addToDoc("object_reference.type", "", false);
                addToDoc("object_reference.version", "", false);
                addToDoc("object_reference.special_ref", "3555", true);
                addToDoc("object_reference.special_name", "", false);
                usedUuids+=value+"3555;"
            }
        }
    }
}


function mapReferenceSystemInfo() {
    var rsIdentifiers = XPathUtils.getNodeList(recordNode, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
    if (hasValue(rsIdentifiers)) {
        for (i=0; i<rsIdentifiers.getLength(); i++ ) {
            var code = XPathUtils.getString(rsIdentifiers.item(i), "gmd:code/gco:CharacterString");
            var codeSpace = XPathUtils.getString(rsIdentifiers.item(i), "gmd:codeSpace/gco:CharacterString");
            var val = code;
            if (hasValue(codeSpace) && hasValue(code)) {
                val = codeSpace+":"+code;
            }
            if (hasValue(val)) {
                addToDoc("spatial_system.referencesystem_value", val, true);
                // legacy mapping (used in csw interface)
                addToDoc("t011_obj_geo.referencesystem_id", val, true);
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
        // transform to IGC domain id
        var idcCode = null;
        try {
            idcCode = codelistService.getCodeListEntryId(codeListId, val, "iso");
            if (idcCode == null)
                idcCode = codelistService.getCodeListEntryId(codeListId, val, "en");
        } catch (e) {
            if (log.isInfoEnabled()) {
                log.info("Error tranforming value '" + val + "' with code list " + codeListId + ". Does the codeList exist?");
            }
        }
        if (hasValue(idcCode)) {
            return idcCode;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Domain code '" + val + "' unknown in code list " + codeListId + ".");
            }
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

function transformToPreviewGraphic() {
    var mdBrowseGraphics = XPathUtils.getNodeList(recordNode, "//gmd:identificationInfo//gmd:graphicOverview/gmd:MD_BrowseGraphic")
    if (hasValue(mdBrowseGraphics)) {
        for (i=0; i<mdBrowseGraphics.getLength(); i++ ) {
          var mdBrowseGraphic = mdBrowseGraphics.item(i);
          var fileName = XPathUtils.getString(mdBrowseGraphic, "./gmd:fileName/gco:CharacterString");
          var fileDescription = XPathUtils.getString(mdBrowseGraphic, "./gmd:fileDescription/gco:CharacterString");
          if (hasValue(fileName)) {
              var previewImageHtmlTag = "<img src='" + fileName + "' height='100' class='preview_image' ";
              if (hasValue(fileDescription)) {
                  previewImageHtmlTag += "alt='" + fileDescription + "' title='" + fileDescription + "' >";
              } else {
                  previewImageHtmlTag += "alt='"+ fileName + "' >";
              }
              addToDoc("additional_html_1", previewImageHtmlTag, false);
          }
        }
    }
} 


function addResourceMaintenance() {
    var maintenanceFrequencyCode = XPathUtils.getString(recordNode, "//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue")
    if (hasValue(maintenanceFrequencyCode)) {
        // transform to IGC domain id
        var idcCode = codelistService.getCodeListEntryId("518", maintenanceFrequencyCode, "iso");
        if (hasValue(idcCode)) {
            addToDoc("t01_object.time_period", idcCode, false);
            addToDoc("t01_object.time_descr", XPathUtils.getString(recordNode, "//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceNote/gco:CharacterString"), true);
            var periodDuration = XPathUtils.getString(recordNode, "//gmd:identificationInfo//gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:userDefinedMaintenanceFrequency/gmd:TM_PeriodDuration");
            addToDoc("t01_object.time_interval", new TM_PeriodDurationToTimeInterval().parse(periodDuration), false);
            addToDoc("t01_object.time_alle", new TM_PeriodDurationToTimeAlle().parse(periodDuration), false);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("MD_MaintenanceFrequencyCode '" + maintenanceFrequencyCode + "' unknown.")
            }
        }
    }
}

/*
 * Set the boundaries of dates to values that can be compared with lucene. The
 * value of inifinite pas is '00000000' and the value for inifinit future is '99999999'.
 * 
 * Makes sure that the fields are only set, if we have a UDK date type of 'seit' or 'bis'. 
 * We can do this because the mapping filters and maps the dates to t0 in case of date type
 * 'am' and to t1 in case of 'seit', even if the database fields are the same. Thus we do not 
 * need to look at the DB field time_type which controls the date 
 * type ('am', 'seit', 'bis', 'von (von-bis)')   
 * 
 */
function addTimeConstraints() {
    var t0 = UtilsCSWDate.mapDateFromIso8601ToIndex(XPathUtils.getString(recordNode, "//gmd:identificationInfo//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:timePosition" +
        " | //gmd:identificationInfo//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml311:TimeInstant/gml311:timePosition"));
    var t1 = UtilsCSWDate.mapDateFromIso8601ToIndex(XPathUtils.getString(recordNode, "//gmd:identificationInfo//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition" +
        " | //gmd:identificationInfo//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml311:TimePeriod/gml311:beginPosition"));
    var t2 = UtilsCSWDate.mapDateFromIso8601ToIndex(XPathUtils.getString(recordNode, "//gmd:identificationInfo//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition" +
        " | //gmd:identificationInfo//gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml311:TimePeriod/gml311:endPosition"));
    var timeType;
    if (hasValue(t0)) {
        addToDoc("t01_object.time_type", "am", false);
        addToDoc("t0", t0, false);
    } else if (hasValue(t1) && hasValue(t2)) {
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
        addToDoc("t2", "99999999", false);
    } else if (!hasValue(t1) && hasValue(t2)) {
        addToDoc("t01_object.time_type", "bis", false);
        addToDoc("t1", "00000000", false);
        addToDoc("t2", t2, false);
    }
}

function mapObjectClass() {
    var hierarchyLevel = XPathUtils.getString(recordNode, "//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue");
    var hierarchyLevelName = XPathUtils.getString(recordNode, "//gmd:hierarchyLevelName/gco:CharacterString");
    var objectClass = "1"; 
    if (hasValue(hierarchyLevel)) {
        if (hierarchyLevel.toLowerCase() == "service") {
            // "Geodatendienst"
            objectClass = "3";
        } else if (hierarchyLevel.toLowerCase() == "application") {
            // "Dienst / Anwendung / Informationssystem"
            objectClass = "6";
        } else if (hierarchyLevel.toLowerCase() == "nongeographicdataset") {
            if (hasValue(hierarchyLevelName)) {
                if (hierarchyLevelName == "job") {
                    // "Organisation/Fachaufgabe"
                    objectClass = "0";
                } else if (hierarchyLevelName == "document") {
                    objectClass = "2";
                } else if (hierarchyLevelName == "project") {
                    objectClass = "4";
                } else if (hierarchyLevelName == "database") {
                    objectClass = "5";
                }
            }
        }
    }
    addToDoc("t01_object.obj_class", objectClass, false);    
}

function addCoupledServices() {
    var crossReferences = XPathUtils.getNodeList(recordNode, "//idf:crossReference[./idf:objectType=3]");
    if (hasValue(crossReferences)) {
        for (i=0; i<crossReferences.getLength(); i++ ) {
            var crossReference = crossReferences.item(i);
            var objUuid = XPathUtils.getString(crossReference, "./@uuid");
            var objName = XPathUtils.getString(crossReference, "idf:objectName");
            var serviceUrl = XPathUtils.getString(crossReference, "idf:serviceUrl");
            var resourceIdentifier = XPathUtils.getString(recordNode, "//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString");
            var data = objUuid 
                + "@@" + objName 
                + "@@" + serviceUrl
                + "@@" + resourceIdentifier;
            addToDoc("refering_service_uuid", data, false);
            addToDoc("capabilities_url", serviceUrl, false);
        }
    }
}

function addObjectReference() {
    var crossReferences = XPathUtils.getNodeList(recordNode, "//idf:crossReference[@direction='IN']");
    if (hasValue(crossReferences)) {
        for (i=0; i<crossReferences.getLength(); i++ ) {
            var crossReference = crossReferences.item(i);
            var objUuid = XPathUtils.getString(crossReference, "./@uuid");
            var objName = XPathUtils.getString(crossReference, "idf:objectName");
            var objClass = XPathUtils.getString(crossReference, "idf:objectType");
            var objSpecialRef = XPathUtils.getString(crossReference, "idf:attachedToField/@entry-id");
            var objSpecialName = XPathUtils.getString(crossReference, "idf:attachedToField");
            var objServiceType = XPathUtils.getString(crossReference, "idf:serviceType");
            var objServiceVersion = XPathUtils.getString(crossReference, "idf:serviceVersion");
            addToDoc("refering.object_reference.obj_uuid", objUuid || "", false);
            addToDoc("refering.object_reference.obj_name", objName || "", false);
            addToDoc("refering.object_reference.obj_class", objClass || "", false);
            addToDoc("refering.object_reference.type", objServiceType || "", false);
            addToDoc("refering.object_reference.version", objServiceVersion || "", false);
            addToDoc("refering.object_reference.special_ref", objSpecialRef || "", false);
            addToDoc("refering.object_reference.special_name", objSpecialName || "", false);
        }
    }
}

function addObjectReferenceTo() {
  var crossReferences = XPathUtils.getNodeList(recordNode, "//idf:crossReference[@direction='OUT']");
  if (hasValue(crossReferences)) {
      for (i=0; i<crossReferences.getLength(); i++ ) {
          var crossReference = crossReferences.item(i);
          var objUuid = XPathUtils.getString(crossReference, "./@uuid");
          var objName = XPathUtils.getString(crossReference, "idf:objectName");
          var objClass = XPathUtils.getString(crossReference, "idf:objectType");
          var objSpecialRef = XPathUtils.getString(crossReference, "idf:attachedToField/@entry-id");
          var objSpecialName = XPathUtils.getString(crossReference, "idf:attachedToField");
          var objServiceType = XPathUtils.getString(crossReference, "idf:serviceType");
          var objServiceVersion = XPathUtils.getString(crossReference, "idf:serviceVersion");
          addToDoc("object_reference.obj_to_uuid", objUuid || "", false);
          addToDoc("object_reference.obj_uuid", objUuid || "", false);
          addToDoc("object_reference.obj_name", objName || "", false);
          addToDoc("object_reference.obj_class", objClass || "", false);
          addToDoc("object_reference.type", objServiceType || "", false);
          addToDoc("object_reference.version", objServiceVersion || "", false);
          addToDoc("object_reference.special_ref", objSpecialRef || "", false);
          addToDoc("object_reference.special_name", objSpecialName || "", false);
      }
  }
}

function addToDoc(field, content, tokenized, additionalTokenize) {
    if (typeof content != "undefined" && content != null) {
        if (log.isDebugEnabled()) {
          log.debug("Add '" + field + "'='" + content + "' to lucene index");
        }

        // try to fix trim related errors (INGRID-2350)
        if (typeof(content.trim) === "function") {
          content = content.trim();
        } else {
          if (log.isDebugEnabled()) {
            log.debug("Could not execut trim() on '" + content + "' (Type: "+ (typeof content) +"). Trim() not found, fall back to regular expression based trimming.");
          }
          content = (""+content).replace(/^\s+|\s+$/gm,'');
        }

        document.put( field, content );
        document.put( "content", content );

        if (hasValue(additionalTokenize)) {
          var newContent = doAdditionalTokenize(content, additionalTokenize);
          if (hasValue(newContent) && newContent != content) {
            document.put( field, newContent );
            document.put( "content", newContent );
          }
        }
    }
}

function doAdditionalTokenize(content, additionalTokenize) {
  if (hasValue(content)) {
    if (additionalTokenize == "SPLIT_URL") {
        // split by "/" and "."
        var newContent = ("" + content).split(/[.\/]/).join(' ');
        if (log.isDebugEnabled()) {
          log.debug("Additional tokenizing of '" + content + "' to '" + newContent + "'");
        }
        return newContent;
    }
  }
  return content;
}

function addNumericToDoc(field, content) {
    if (typeof content != "undefined" && content != null) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Add numeric '" + field + "'='" + content + "' to lucene index.");
            }
            document.put( field, content );
        } catch (e) {
            if (log.isDebugEnabled()) {
                log.debug("Value '" + content + "' is not a number. Ignoring field '" + field + "'.");
            }
        }
    }
}


function hasValue(val) {
    if (typeof val == "undefined") {
        return false; 
    } else if (val == null) {
        return false; 
    } else if (typeof val == "string" && val == "") {
        return false;
    } else if (typeof val == "object" && val.toString() == "") {
        return false;
    } else {
      return true;
    }
}

function call_f(f,args)
{
  if (hasValue(args)) {
    if (args.length === 0)
      return f();
    else if (args.length === 1)
      return f(args[0]);
    else if (args.length === 2)
      return f(args[0], args[1]);
    else if (args.length === 3)
      return f(args[0], args[1], args[2]);
    else if (args.length === 4)
      return f(args[0], args[1], args[2], args[3]);
    else
      log.error("Function call does not support number of arguments: " + args.length);
          
  } else {
    return f();
  }
}
