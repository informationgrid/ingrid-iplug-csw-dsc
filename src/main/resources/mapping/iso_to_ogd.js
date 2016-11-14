/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
 * CSW 2.0.2 AP ISO 1.0 Record (full) to OGD to Lucene Document mapping
 *
 * The following global variable are passed from the application:
 *
 * @param cswRecord A CSWRecord instance, that defines the input
 * @param document A lucene Document instance, that defines the output
 * @param log A Log instance
 * @param XPathUtils utility extracting data from CSW
 * @param IDX utility for adding data to index
 *
 */
if (javaVersion.indexOf( "1.8" ) === 0) {
	load("nashorn:mozilla_compat.js");
}

if (log.isDebugEnabled()) {
	log.debug("Mapping csw record "+cswRecord.getId()+" to ogd to lucene document");
}

// get the xml content of the record
var recordNode = cswRecord.getOriginalResponse();


var jsonTransformationDescriptions = [{
        "jsonPath": "author",
        "fixed": "FIXED"
    }, {
        "jsonPath": "author_email",
        "fixed": "FIXED"
    }, {
        "jsonPath": "extras/contacts/role",
        "fixed": "ANALOG? \"vertrieb\" oder \"autor\""
    }, //"xpath": "//gmd:MD_DataIdentification/gmd:pointOfContact"},

    {
        "jsonPath": "title",
        "xpath": "//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"
    }, {
        "jsonPath": "name",
        "xpath": "//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"
    }, {
        "jsonPath": "notes",
        "xpath": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"
    }, {
        "jsonPath": "url",
        "xpath": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"
//    }, {
//        "jsonPath": "type",
//        "xpath": "//gmd:identificationInfo/gmd:MD_DataIdentification"
    }, {
        "jsonPath": "resources/url",
        "xpath": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"
    }, {
        "jsonPath": "resources/format",
        "xpath": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString"
    }, {
        "jsonPath": "resources/description",
        "xpath": "//gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"
    }, {
        "jsonPath": "resources/language",
        "xpath": "//gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue"
    }, {
        "jsonPath": "extras/contacts/name",
        "xpath": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/idf:idfResponsibleParty/gmd:organisationName/gco:CharacterString"
    }, {
        "jsonPath": "extras/contacts/url",
        "xpath": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/idf:idfResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"
    }, {
        "jsonPath": "extras/contacts/email",
        "xpath": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/idf:idfResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"
    }, {
        "jsonPath": "extras/spatial/coordinates",
        "xpath": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"
    }, {
        "jsonPath": "extras/metadata_original_id",
        "xpath": "//gmd:fileIdentifier/gco:CharacterString"
    },

    {
        "jsonPath": "maintaner",
        "withParent": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/idf:idfResponsibleParty/gmd:role/gmd:CI_RoleCode[@codeListValue='custodian']/../..",
        "xpath": "./gmd:organisationName/gco:CharacterString"
    }, {
        "jsonPath": "extras/contacts/address",
        "withParent": "//gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/idf:idfResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address",
        "xpath": ["./gmd:postalCode/gco:CharacterString", "./gmd:city/gco:CharacterString", "./gmd:deliveryPoint/gco:CharacterString"]
    }, {
        "jsonPath": "maintaner_email",
        "withParent": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/idf:idfResponsibleParty/gmd:role/gmd:CI_RoleCode[@codeListValue='custodian']/../..",
        "xpath": "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"
    },
/*
    {
        "jsonPath": "groups",
        "multiples": true,
        "withParent": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString[text()='OGDD-Kategorien']/../../../..",
        "xpath": ".//gmd:keyword/gco:CharacterString"
    },
    {
        "jsonPath": "tags",
        "multiples": true,
        "withParent": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString[text()='OGDD-Kategorien']/../../../..",
        "xpath": "./gmd:keyword/gco:CharacterString"
    },
    {
        "jsonPath": "extras/subgroups",
        "multiples": true,
        "xpath": ["//gmd:hierarchyLevelName/gco:CharacterString", "//gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString"]
    },
*/
    {
        "jsonPath": "license_id",
        "inJsonField": "id",
        "xpath": "//gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString[contains(text(),'\"id\": ')]"
    },

    {
        "jsonPath": "extras/dates",
        "isDateField": true,
        "multiples": true,
        "xpath": "//gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"
    },
];

var jsonObject = {};
for (var i in jsonTransformationDescriptions) {
    var t = jsonTransformationDescriptions[i];
    var value;
    if (t.fixed) {
        value = t.fixed;
    } else {
        if (typeof t.xpath === 'string') {
            t.xpath = [t.xpath];
        }
        var tempNode;
        if (t.withParent) {
            var nodeList = XPathUtils.getNodeList(tempNode, t.withParent);
            if (nodeList && nodeList.getLength() > 0) {
                if (nodeList.getLength() === 1) {
                    tempNode = nodeList.item(0);
                } else {
                    log.debug("Error! More than one Match for Parent xpath: " + t.jsonPath + " " + t.withParent);
                    continue;
                }
            } else {
                log.debug("Error! Parent Xpath not found: " + t.jsonPath + " " + t.withParent);
                continue;
            }
        } else {
            tempNode = recordNode;
        }

        var err = false;
        value = "";
        if (t.isDateField) {
            dateArray = [];
        }
        for (var i in t.xpath) {
            var nodeList = XPathUtils.getNodeList(tempNode, t.xpath[i]);
            if (nodeList && nodeList.getLength() > 0) {

                for (var x = 0; x < nodeList.getLength(); x++) {
                    if (t.inJsonField) {
                        value += JSON.parse(nodeList.item(x).getTextContent())[t.inJsonField] + " ";
                    } else if (t.isDateField) {
                        name = nodeList.item(x).getTextContent();
                        date = XPathUtils.getNode(nodeList.item(x), "./../../../gmd:date/gco:Date").getTextContent();
                        date = date + "T00:00:00";
                        dateArray.push({
                            date: date,
                            role: name
                        });

                    } else {
                        value += nodeList.item(x).getTextContent() + " ";
                    }
                }
                if (nodeList.getLength() !== 1 && !t.multiples) {
                    log.debug("Error! More than one Match for xpath: " + nodeList.getLength() + " " + t.xpath[i]);
                    err = true;
                    break;
                }
            } else {
                log.debug("Error! Xpath not found: " + t.jsonPath + " " + t.xpath[i]);
                err = true;
                break;
            }
        }
        value = value.trim();
        if (t.isDateField) {
            value = dateArray;
        }
        if (err) continue;

    }

    var jPath = t.jsonPath.split("/");
    var current = jsonObject;
    for (var j in jPath) {
        var curVar = jPath[j];
        if (j == jPath.length - 1) {
            current[curVar] = value;
        } else {
            if (!current[curVar]) {
                current[curVar] = {};
            }
            current = current[curVar];
        }
    }
}
//log.debug("\n\nJSON String:");
//log.debug(JSON.stringify(jsonObject, null, 2));

IDX.addAllFromJSON(JSON.stringify(jsonObject));
