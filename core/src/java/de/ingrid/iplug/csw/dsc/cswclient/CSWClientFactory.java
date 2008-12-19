/*
 * Copyright (c) 2008 wemove digital solutions. All rights reserved.
 */

package de.ingrid.iplug.csw.dsc.cswclient;

import java.io.Serializable;

import de.ingrid.iplug.csw.dsc.cswclient.impl.GenericClient;

/**
 * This class is used to create all concrete CSW related classes
 * which may vary between the different CSW servers. 
 * The specific implementation is configured in plugdescription.xml.
 * @author ingo herwig <ingo@wemove.com>
 * TODO: Check if request implementation may vary with operation
 */
public class CSWClientFactory implements Serializable {

	private static final long serialVersionUID = CSWClientFactory.class.getName().hashCode();
	
	private String serviceUrl;
	
	private String clientImpl;
	private String requestImpl;
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
		if (serviceUrl != null)
		{
			return serviceUrl;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
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
	public void setRequestImpl(String requestImpl) {
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
		return queryTemplate;
	}

	/**
	 * Factory methods
	 */

	/**
	 * Create a CSWClient.
	 * @return A concrete CSWClient instance
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public CSWClient createClient() throws Exception {
		if (clientImpl != null)
		{
			GenericClient factory = (GenericClient)Class.forName(clientImpl).newInstance();
			return factory;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}

	/**
	 * Create a CSWRequest.
	 * @return A concrete CSWRequest instance
	 */
	public CSWRequest createRequest() throws Exception {
		if (requestImpl != null)
		{
			CSWRequest request = (CSWRequest)Class.forName(requestImpl).newInstance();
			return request;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}

	/**
	 * Create a CSWCapabilities.
	 * @return A concrete CSWCapabilities instance
	 */
	public CSWCapabilities createCapabilities() throws Exception {
		if (capabilitiesImpl != null)
		{
			CSWCapabilities capabilities = (CSWCapabilities)Class.forName(capabilitiesImpl).newInstance();
			return capabilities;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}

	/**
	 * Create a CSWRecordDescription.
	 * @return A concrete CSWRecordDescription instance
	 */
	public CSWRecordDescription createRecordDescription() throws Exception {
		if (recordDescriptionImpl != null)
		{
			CSWRecordDescription description = (CSWRecordDescription)Class.forName(recordDescriptionImpl).newInstance();
			return description;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}

	/**
	 * Create a CSWQuery.
	 * @return A concrete CSWQuery instance
	 */
	public CSWQuery createQuery() throws Exception {
		if (queryImpl != null)
		{
			CSWQuery query = (CSWQuery)Class.forName(queryImpl).newInstance();

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
			
			return query;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}

	/**
	 * Create a CSWSearchResult.
	 * @return A concrete CSWSearchResult instance
	 */
	public CSWSearchResult createSearchResult() throws Exception {
		if (searchResultImpl != null)
		{
			CSWSearchResult result = (CSWSearchResult)Class.forName(searchResultImpl).newInstance();
			return result;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}

	/**
	 * Create a CSWRecord.
	 * @return A concrete CSWRecord instance
	 */
	public CSWRecord createRecord() throws Exception {
		if (recordImpl != null)
		{
			CSWRecord record = (CSWRecord)Class.forName(recordImpl).newInstance();
			return record;
		}
		else
			throw new RuntimeException("CSWClientFactory is not configured properly. Check the plugdescription for problems.");
	}
}
