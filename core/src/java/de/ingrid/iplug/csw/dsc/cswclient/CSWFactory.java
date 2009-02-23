/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.io.Serializable;
import java.util.Map;

import de.ingrid.iplug.csw.dsc.cswclient.constants.Operation;
import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericClient;

/**
 * This class is used to create all concrete CSW related classes
 * which may vary between the different CSW servers. 
 * The specific implementation is configured in bean.xml.
 * @author ingo herwig <ingo@wemove.com>
 */
public class CSWFactory implements Serializable {

	private static final long serialVersionUID = CSWFactory.class.getName().hashCode();
	
	private String serviceUrl;
	
	private String clientImpl;
	private Map<String, String> requestImpl;
	private String capabilitiesImpl;
	private String recordDescriptionImpl;
	private String queryImpl;
	private String searchResultImpl;
	private String recordImpl;

	private CSWQuery queryTemplate;
	
	/**
	 * get the service url.
	 * @return The service url
	 * @throws RuntimeException 
	 */
	public String getServiceUrl() throws Exception {
		if (this.serviceUrl != null) {
			return this.serviceUrl;
		}
		else
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'serviceUrl' is missing.");
	}
	
	/**
	 * Set the service url
	 * @param serviceUrl
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Set the CSWClient implementation
	 * @param clientImpl
	 */
	public void setClientImpl(String clientImpl) {
		this.clientImpl = clientImpl;
	}

	/**
	 * Set the CSWRequest implementation
	 * @param requestImpl
	 */
	public void setRequestImpl(Map<String, String> requestImpl) {
		this.requestImpl = requestImpl;
	}

	/**
	 * Set the CSWCapabilities implementation
	 * @param capabilitiesImpl
	 */
	public void setCapabilitiesImpl(String capabilitiesImpl) {
		this.capabilitiesImpl = capabilitiesImpl;
	}

	/**
	 * Set the CSWRecordDescription implementation
	 * @param recordDescriptionImpl
	 */
	public void setRecordDescriptionImpl(String recordDescriptionImpl) {
		this.recordDescriptionImpl = recordDescriptionImpl;
	}

	/**
	 * Set the CSWQuery implementation
	 * @param queryImpl the fQueryImpl to set
	 */
	public void setQueryImpl(String queryImpl) {
		this.queryImpl = queryImpl;
	}

	/**
	 * Set the CSWSearchResult implementation
	 * @param searchResultImpl
	 */
	public void setSearchResultImpl(String searchResultImpl) {
		this.searchResultImpl = searchResultImpl;
	}

	/**
	 * Set the CSWRecord implementation
	 * @param recordImpl
	 */
	public void setRecordImpl(String recordImpl) {
		this.recordImpl = recordImpl;
	}

	/**
	 * Set the query template, which will be used when creating queries 
	 * @param queryTemplate
	 */
	public void setQueryTemplate(CSWQuery queryTemplate) {
		this.queryTemplate = queryTemplate;
	}

	/**
	 * Get the query template, which will be used when creating queries 
	 * @return CSWQuery
	 */
	public CSWQuery getQueryTemplate() {
		return this.queryTemplate;
	}

	/**
	 * Factory methods
	 */

	/**
	 * Create a CSWClient.
	 * @return A concrete CSWClient instance
	 */
	public CSWClient createClient() throws RuntimeException {
		GenericClient factory;
		try {
			factory = (GenericClient)Class.forName(this.clientImpl).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'clientImpl' is missing or wrong.");
		}
		return factory;
	}

	/**
	 * Create a CSWRequest.
	 * @return A concrete CSWRequest instance
	 */
	public CSWRequest createRequest(Operation op) throws RuntimeException {
		CSWRequest request;
		try {
			request = (CSWRequest)Class.forName(this.requestImpl.get(op.toString()).toString()).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'requestImpl' is missing or wrong. No value found for operation "+op+".");
		}
		return request;
	}

	/**
	 * Create a CSWCapabilities.
	 * @return A concrete CSWCapabilities instance
	 */
	public CSWCapabilities createCapabilities() throws RuntimeException {
		CSWCapabilities capabilities;
		try {
			capabilities = (CSWCapabilities)Class.forName(this.capabilitiesImpl).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'capabilitiesImpl' is missing or wrong.");
		}
		return capabilities;
	}

	/**
	 * Create a CSWRecordDescription.
	 * @return A concrete CSWRecordDescription instance
	 */
	public CSWRecordDescription createRecordDescription() throws RuntimeException {
		CSWRecordDescription description;
		try {
			description = (CSWRecordDescription)Class.forName(this.recordDescriptionImpl).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'recordDescriptionImpl' is missing or wrong.");
		}
		return description;
	}

	/**
	 * Create a CSWQuery.
	 * @return A concrete CSWQuery instance
	 */
	public CSWQuery createQuery() throws RuntimeException {
		CSWQuery query;
		try {
			query = (CSWQuery)Class.forName(this.queryImpl).newInstance();

			// set default config values from the template query
			if (queryTemplate != null) {
				query.setSchema(queryTemplate.getSchema());
				query.setOutputSchema(queryTemplate.getOutputSchema());
				query.setOutputFormat(queryTemplate.getOutputFormat());
				query.setVersion(queryTemplate.getVersion());
				query.setElementSetName(queryTemplate.getElementSetName());
				query.setTypeNames(queryTemplate.getTypeNames());
				query.setResultType(queryTemplate.getResultType());
				query.setConstraintLanguage(queryTemplate.getConstraintLanguage());
				query.setConstraintLanguageVersion(queryTemplate.getConstraintLanguageVersion());
			}
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'queryImpl' is missing or wrong.");
		}
		return query;
	}

	/**
	 * Create a CSWSearchResult.
	 * @return A concrete CSWSearchResult instance
	 */
	public CSWSearchResult createSearchResult() throws RuntimeException {
		CSWSearchResult result;
		try {
			result = (CSWSearchResult)Class.forName(this.searchResultImpl).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'searchResultImpl' is missing or wrong.");
		}
		return result;
	}

	/**
	 * Create a CSWRecord.
	 * @return A concrete CSWRecord instance
	 */
	public CSWRecord createRecord() throws RuntimeException {
		CSWRecord record;
		try {
			record = (CSWRecord)Class.forName(this.recordImpl).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("CSWFactory is not configured properly. Parameter 'recordImpl' is missing or wrong.");
		}
		return record;
	}
}
