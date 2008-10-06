package de.ingrid.iplug.csw.dsc.index;

import java.util.ArrayList;
import java.util.List;

public class IngridHitsEnrichmentFactory {

	private List<IIngridHitEnrichment> _enrichmentCollection = new ArrayList<IIngridHitEnrichment>();

	public IngridHitsEnrichmentFactory() {
		_enrichmentCollection.add(new CswDscIdentifierEnrichment());
	}

	public List<IIngridHitEnrichment> getIngridHitsEnrichmentCollection() {
		return _enrichmentCollection;
	}

}
