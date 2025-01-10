/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

log.debug("WSV Geodatenkatalog post processing of lucene document !");

// Replace wrong path "urls" with domain
// see https://dev2.wemove.com/jira/browse/GEOPORTALWSV-39

var urlFieldName = "t017_url_ref.url_link";
var replaceField = false;
var myUrls = document.getValues(urlFieldName);

for (var i=0; i<myUrls.length; i++) {
	myUrls[i] = transformToValidUrl(myUrls[i], "http://geokat.wsv.bvbs.bund.de");
}

if (replaceField) {
	document.removeFields(urlFieldName);

    for (var i=0; i<myUrls.length; i++) {
    	addToDoc(urlFieldName, myUrls[i], true);
    }
}


function transformToValidUrl(oldUrl, urlDomain) {
    var newUrl = oldUrl;
    if (hasValue(oldUrl)) {
        // NOTICE: charAt delivers code, 47 = "/"
        if (oldUrl.charAt(0) == "47") {
            newUrl = urlDomain;
            replaceField = true;

		   log.debug("Replaced URL  '" + oldUrl + "' with URL '" + newUrl + "'");
        }
    }
    
    return newUrl;
}

function addToDoc(field, content, tokenized) {
	if (typeof content != "undefined" && content != null) {
		log.debug("Add '" + field + "'='" + content + "' to lucene index");
		Utils.addToDoc( document, field, content );
		Utils.addToDoc( document, "content", content );
	}
}

function hasValue(val) {
	if (typeof val == "undefined") {
		return false; 
	} else if (val == null) {
		return false; 
	} else if (typeof val == "string" && val == "") {
		return false;
	} else if (typeof val == "object" && Object.keys(val).length === 0) {
        return false;
	} else {
	  return true;
	}
}
