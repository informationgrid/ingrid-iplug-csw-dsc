package de.ingrid.iplug.csw.dsc.index;

import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.query.IngridQuery;

public class HitIdPostProcessor implements IPostProcessor {

	public void process(IngridQuery query, IngridDocument[] documents)
			throws Exception {
		// ingrid documents can cast into ingrid hits
		for (IngridDocument ingridDocument : documents) {
			IngridHit hit = (IngridHit) ingridDocument;
			String id = generateId();
			hit.setHitId(id);
		}
	}

	private String generateId() {
		// TODO generate id
		return null;
	}

}
