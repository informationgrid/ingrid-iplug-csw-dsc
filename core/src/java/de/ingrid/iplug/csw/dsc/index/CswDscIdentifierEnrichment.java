package de.ingrid.iplug.csw.dsc.index;

import de.ingrid.utils.IngridHit;

public class CswDscIdentifierEnrichment implements IIngridHitEnrichment {

	public void enrichment(IngridHit ingridHit) {
		// TODO
		ingridHit.setHitId("cswDscId");
	}

}
