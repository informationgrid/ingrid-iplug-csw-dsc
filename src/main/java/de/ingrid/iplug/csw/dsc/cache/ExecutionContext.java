/*
 * **************************************************-
 * ingrid-iplug-csw-dsc:war
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
 * Copyright (c) 2009 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cache;

import java.util.Date;
import java.util.Set;

import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;

public class ExecutionContext {

	CSWFactory factory;
	Cache cache;
	Set<String> filterStrSet;
	Date lastExecutionDate;
	int recordsPerCall;
	int requestPause;
	
	/**
	 * @return the factory
	 */
	public CSWFactory getFactory() {
		return factory;
	}
	/**
	 * @param factory the factory to set
	 */
	public void setFactory(CSWFactory factory) {
		this.factory = factory;
	}
	/**
	 * @return the cache
	 */
	public Cache getCache() {
		return cache;
	}
	/**
	 * @param cache the cache to set
	 */
	public void setCache(Cache cache) {
		this.cache = cache;
	}
	/**
	 * @return the filterStrSet
	 */
	public Set<String> getFilterStrSet() {
		return filterStrSet;
	}
	/**
	 * @param filterStrSet the filterStrSet to set
	 */
	public void setFilterStrSet(Set<String> filterStrSet) {
		this.filterStrSet = filterStrSet;
	}
	/**
	 * @return the lastExecutionDate
	 */
	public Date getLastExecutionDate() {
		return lastExecutionDate;
	}
	/**
	 * @param lastExecutionDate the lastExecutionDate to set
	 */
	public void setLastExecutionDate(Date lastExecutionDate) {
		this.lastExecutionDate = lastExecutionDate;
	}
	/**
	 * @return the recordsPerCall
	 */
	public int getRecordsPerCall() {
		return recordsPerCall;
	}
	/**
	 * @param recordsPerCall the recordsPerCall to set
	 */
	public void setRecordsPerCall(int recordsPerCall) {
		this.recordsPerCall = recordsPerCall;
	}
	/**
	 * @return the requestPause
	 */
	public int getRequestPause() {
		return requestPause;
	}
	/**
	 * @param requestPause the requestPause to set
	 */
	public void setRequestPause(int requestPause) {
		this.requestPause = requestPause;
	}
}
