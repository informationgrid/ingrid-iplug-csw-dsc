/*
 * Copyright (c) 1997-2005 by media style GmbH
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.cswclient.CSWRecord;
import de.ingrid.iplug.csw.dsc.cswclient.constants.ElementSetName;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.iplug.scheduler.SchedulingService;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.IngridHitsEnrichmentFactory;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.processor.ProcessorPipe;
import de.ingrid.utils.processor.ProcessorPipeFactory;
import de.ingrid.utils.query.IngridQuery;

/**
 * Searcher for the local index.
 */
public class DSCSearcher extends AbstractSearcher {

	private RecordLoader fDetailer;

	private SchedulingService fScheduler;

	private PlugDescription fPlugDescription;

	private DocumentMapper mapper = null;
	private Cache cache = null;
	
	private ProcessorPipe _processorPipe = new ProcessorPipe();
	

	/**
	 * Initilaizes the DSC searcher variant.
	 */
	public DSCSearcher() {
		// nothing to do here
	}

	/**
	 * @param file
	 * @param string
	 * @throws IOException
	 */
	public DSCSearcher(File file, String string) throws IOException {
		super(file, string);
	}

	public void configure(PlugDescription plugDescription) throws Exception {
		this.fPlugDescription = plugDescription;
		this.fPlugId = plugDescription.getPlugId();
		this.fUrl = (String) plugDescription.get("detailUrl");
		this.fSearcher = new IndexSearcher(new File(plugDescription
				.getWorkinDirectory(), "index").getAbsolutePath());
		this.fDetailer = new RecordLoader();
		this.fScheduler = new SchedulingService(new File(plugDescription
				.getWorkinDirectory(), "jobstore"));
		ProcessorPipeFactory processorPipeFactory = new ProcessorPipeFactory(
				plugDescription);
		_processorPipe = processorPipeFactory.getProcessorPipe();
		IngridHitsEnrichmentFactory factory = new IngridHitsEnrichmentFactory();
		factory.register(new CswDscIdentifierEnrichment());
		
		this.mapper = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_MAPPER, DocumentMapper.class);
		if (this.mapper == null) {
			throw new RuntimeException("DSCSearcher is not configured properly. "+
					"Bean '"+ConfigurationKeys.CSW_MAPPER+"' is missing in spring configuration.");
		}


		this.cache = SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_CACHE, Cache.class);
		if (this.cache == null) {
			throw new RuntimeException("DSCSearcher is not configured properly. "+
					"Bean '"+ConfigurationKeys.CSW_CACHE+"' is missing in spring configuration.");
		} else {
			this.cache.configure(SimpleSpringBeanFactory.INSTANCE.getBean(ConfigurationKeys.CSW_FACTORY, CSWFactory.class));
		}
	}

	public IngridHits search(IngridQuery query, int start, int length)
			throws Exception {

    	_processorPipe.preProcess(query);
		IngridHits ingridHits = search(query, false, start, length);
		_processorPipe.postProcess(query, ingridHits.getHits());
		
		// add cswDirectResponse flag to hits, if requested by the query
		if (this.supportsDirectData()) {
			ElementSetName elementSetName = this.getDirectDataElementSetName(query);
			if (elementSetName != null) {
				for (IngridHit hit : ingridHits.getHits()) {
					this.setDirectDataElementSetName(hit, elementSetName);
				}
			}
		}
		
		return ingridHits;
	}

	public Record getRecord(IngridHit hit) throws Exception {
		
		Document document = this.fSearcher.doc(hit.getDocumentId());
		Record record = this.fDetailer.getDetails(document, this.mapper, this.cache);

		// add original csw data, if requested
		if (this.supportsDirectData()) {
			ElementSetName elementSetName = this.getDirectDataElementSetName(hit);
			if (elementSetName != null) {
				// add the document id of the hit, in order to find the lucene document
				// for the record
				record.put(IngridDocument.DOCUMENT_ID, hit.getDocumentId());
				this.setDirectData(record, elementSetName);
			}
		}
		return record;
	}

	public void close() throws Exception {
		if (this.fDetailer != null) {
			this.fDetailer.close();
		}
		if (this.fSearcher != null) {
			this.fSearcher.close();
		}
		if (this.fScheduler != null) {
			this.fScheduler.shutdown();
		}
	}

	public PlugDescription getPlugDescription() {
		return this.fPlugDescription;
	}

	/**
	 * Check if this searcher returns original csw data, if requested
	 * @return boolean
	 */
	protected boolean supportsDirectData() {
		if (this.fPlugDescription != null)
			return this.fPlugDescription.getBoolean("directData") == Boolean.TRUE;
		else
			return false;
	}

	/**
	 * Get the ElementSetName of the requested original csw data, if any
	 * @param document
	 * @return The ElementSetName or null
	 */
	protected ElementSetName getDirectDataElementSetName(IngridDocument document) {
		if (document.containsKey("cswDirectResponse")) {
			for (ElementSetName name : ElementSetName.values()) {
				if (name.toString().equals(document.getString("cswDirectResponse")))
					return name;
			}
		}
		return null;
	}

	/**
	 * Set the ElementSetName of the requested original csw data
	 * @param document
	 * @param elementSetName
	 * @return The ElementSetName or null
	 */
	protected void setDirectDataElementSetName(IngridDocument document, ElementSetName elementSetName) {
		document.put("cswDirectResponse", elementSetName.toString());
	}

	/**
	 * Set the original csw data in an IngridDocument
	 * @param document
	 * @param elementSetName
	 * @throws IOException 
	 */
	protected void setDirectData(IngridDocument document, ElementSetName elementSetName) throws IOException {
		Document luceneDoc = this.fSearcher.doc(document.getInt(IngridDocument.DOCUMENT_ID));
		CSWRecord record = this.fDetailer.getRecord(luceneDoc, elementSetName, this.cache);
		document.put("cswData", record.getOriginalResponse());
	}

	@Override
	public IngridHitDetail getDetail(IngridHit hit, IngridQuery ingridQuery,
			String[] fields) throws Exception {
		IngridHitDetail detail = super.getDetail(hit, ingridQuery, fields);
		
		// add original csw data, if requested
		if (this.supportsDirectData()) {
			ElementSetName elementSetName = this.getDirectDataElementSetName(ingridQuery);
			if (elementSetName != null) {
				this.setDirectData(detail, elementSetName);
			}
		}
		
		return detail;
	}

	@Override
	public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query,
			String[] requestedFields) throws Exception {
		IngridHitDetail[] details = super.getDetails(hits, query, requestedFields);

		// add original csw data, if requested
		if (this.supportsDirectData()) {
			ElementSetName elementSetName = this.getDirectDataElementSetName(query);
			if (elementSetName != null) {
				for (IngridHitDetail detail : details) {
					this.setDirectData(detail, elementSetName);
				}
			}
		}
		
		return details;
	}
}
