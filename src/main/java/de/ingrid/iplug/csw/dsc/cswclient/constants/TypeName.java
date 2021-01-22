/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient.constants;

import javax.xml.namespace.QName;

public enum TypeName {
	MD_METADATA { public QName getQName() { return Namespace.ISO_METADATA.getQName(); } },
	RECORD 		{ public QName getQName() { return Namespace.CSW_RECORD.getQName(); } },
	DATASET		{ public QName getQName() { return Namespace.CSW_DATASET.getQName(); } };
	
	/**
	 * Get the QName of a typename constant
	 * @return
	 */
	public abstract QName getQName(); 
}
