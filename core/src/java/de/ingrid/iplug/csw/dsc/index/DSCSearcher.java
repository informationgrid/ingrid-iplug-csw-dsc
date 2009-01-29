/*
 * Copyright (c) 1997-2005 by media style GmbH
 */

package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;

import de.ingrid.iplug.csw.dsc.ConfigurationKeys;
import de.ingrid.iplug.csw.dsc.TestUtil;
import de.ingrid.iplug.csw.dsc.cache.Cache;
import de.ingrid.iplug.csw.dsc.cswclient.CSWFactory;
import de.ingrid.iplug.csw.dsc.mapping.DocumentMapper;
import de.ingrid.iplug.csw.dsc.tools.SimpleSpringBeanFactory;
import de.ingrid.iplug.scheduler.SchedulingService;
import de.ingrid.utils.IIngridHitEnrichment;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.IngridHitsEnrichmentFactory;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;

/**
 * Searcher for the local index.
 */
public class DSCSearcher extends AbstractSearcher {

	private RecordLoader fDetailer;

	private SchedulingService fScheduler;

	private PlugDescription fPlugDescription;

	private List<IIngridHitEnrichment> _enrichmentCollection = new ArrayList<IIngridHitEnrichment>();

	private DocumentMapper mapper = null;
	private Cache cache = null;

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

	@SuppressWarnings("unchecked")
	public void configure(PlugDescription plugDescription) throws Exception {
		this.fPlugDescription = plugDescription;
		this.fPlugId = plugDescription.getPlugId();
		this.fUrl = (String) plugDescription.get("detailUrl");
		this.fSearcher = new IndexSearcher(new File(plugDescription
				.getWorkinDirectory(), "index").getAbsolutePath());
		this.fDetailer = new RecordLoader();
		this.fScheduler = new SchedulingService(new File(plugDescription
				.getWorkinDirectory(), "jobstore"));
		IngridHitsEnrichmentFactory factory = new IngridHitsEnrichmentFactory();
		factory.register(new CswDscIdentifierEnrichment());
		_enrichmentCollection = factory.getIngridHitsEnrichmentCollection();
		
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

		IngridHits ingridHits = search(query, false, start, length);

		// enrich the hit object, for example set hitId
		for (IIngridHitEnrichment ingridHitsEnrichment : _enrichmentCollection) {
			IngridHit[] hits = ingridHits.getHits();
			for (int i = 0; i < hits.length; i++) {
				IngridHit ingridHit = hits[i];
				ingridHitsEnrichment.enrichment(ingridHit);
			}
		}
		return ingridHits;
	}

	public Record getRecord(IngridHit hit) throws Exception {
		Document document = this.fSearcher.doc(hit.getDocumentId());
		Record record = this.fDetailer.getDetails(document, this.mapper, this.cache);

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
}
