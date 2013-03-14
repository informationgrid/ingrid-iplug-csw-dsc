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
importPackage(Packages.de.ingrid.utils.xml);
importPackage(Packages.org.w3c.dom);



if (log.isDebugEnabled()) {
	log.debug("WSV Geodatenkatalog post processing of lucene document !");
}

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

            if (log.isDebugEnabled()) {
               log.debug("Replaced URL  '" + oldUrl + "' with URL '" + newUrl + "'");
            }
        }
    }
    
    return newUrl;
}

function addToDoc(field, content, tokenized) {
	if (typeof content != "undefined" && content != null) {
		if (log.isDebugEnabled()) {
			log.debug("Add '" + field + "'='" + content + "' to lucene index");
		}
		var analyzed = Field.Index.ANALYZED;
		if (!tokenized) analyzed = Field.Index.NOT_ANALYZED;
		document.add(new Field(field, content, Field.Store.YES, analyzed));
		document.add(new Field("content", content, Field.Store.NO, analyzed));
		document.add(new Field("content", LuceneTools.filterTerm(content), Field.Store.NO, Field.Index.ANALYZED));
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
