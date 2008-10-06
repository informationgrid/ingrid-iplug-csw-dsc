package de.ingrid.iplug.csw.dsc.index;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;

import de.ingrid.utils.IPlug;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.FuzzyFieldQuery;
import de.ingrid.utils.query.FuzzyTermQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.RangeQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.query.WildCardFieldQuery;
import de.ingrid.utils.query.WildCardTermQuery;

/**
 * The IPlug searcher interface that all new dsc variant IPlugs have to be
 * implemented.
 */
public abstract class AbstractSearcher implements IPlug, IRecordLoader {

	/**
	 * The field name for the index summary field.
	 */
	public static final String FIELD_NAME_SUMMARY = "summary";

	/**
	 * The field name for the index title field.
	 */
	public static final String FIELD_NAME_TITLE = "title";

	/**
	 * The field name for the detailed url field.
	 */
	public static final String DETAIL_URL = "_detail_url_";

	private static final String CONTENT = "content";

	protected IndexSearcher fSearcher;

	protected String fPlugId;

	protected String fUrl;

	private boolean fIsLocked;

	private Object fMonitor = new Object();

	final protected static Log log = LogFactory.getLog(AbstractSearcher.class);

	private static AbstractSearcher fInstance;

	private static SimpleDateFormat fDateFormat = new SimpleDateFormat(
			"yyyyMMdd");

	private static GermanAnalyzer fAnalyzer = new GermanAnalyzer(new String[0]);

	private final static BooleanQuery fBOOLEANQUERYCOMPARATOROBJECT = new BooleanQuery();

	/**
	 * neither qx1 or qx2 are between x1 and x2
	 */
	private static final int FIRST_X_CASE = 0;

	/**
	 * qx1 and qx2 ar between x1 and x2
	 */
	private static final int SECOND_X_CASE = 1;

	/**
	 * qx1 and qx2 are not between x1 and x2
	 */
	private static final int THIRD_X_CASE = 2;

	private static float fTitleBoost = -1;

	/**
	 * The default constructor for PlugServer.
	 */
	public AbstractSearcher() {
		AbstractSearcher.fInstance = this;
	}

	/**
	 * JUST FOR TESTING DON'T USE THIS FOR RUNTIME!
	 * 
	 * @param indexFolder
	 *            The path to the index.
	 * @param plugId
	 *            The IPlug id.
	 * @throws IOException
	 *             If something goes wrong.
	 */
	public AbstractSearcher(File indexFolder, String plugId) throws IOException {
		this.fSearcher = new IndexSearcher(indexFolder.getAbsolutePath());
		this.fPlugId = plugId;
		AbstractSearcher.fInstance = this;
	}

	public abstract void configure(PlugDescription plugDescription)
			throws Exception;

	public abstract IngridHits search(IngridQuery query, int start, int length)
			throws Exception;

	/**
	 * Search the IPlug with the given query and the given range.
	 * 
	 * @param query
	 *            The query to look for.
	 * @param addDataTypes
	 *            True when the datatypes sahould be on the created lucene
	 *            query.
	 * @param start
	 *            The result to start from.
	 * @param length
	 *            The length of the result.
	 * @return The found hits.
	 * @throws Exception
	 *             If something goes wrong.
	 */
	public IngridHits search(IngridQuery query, boolean addDataTypes,
			int start, int length) throws Exception {
		checkLock();
		if (this.fSearcher == null) {
			throw new IllegalArgumentException(
					"searcher can not be null, plug not correct configured.");
		}
		if (fTitleBoost == -1) {
			setTitleBoost();
			log.info("boost title's with " + fTitleBoost);
		}

		Query luceneQuery = buildLuceneQuery(query, addDataTypes);
		log.debug("LuceneQuery: " + luceneQuery.toString());
		Hits luceneHits = this.fSearcher.search(luceneQuery);
		log.debug("hits: " + luceneHits.length());
		int count = luceneHits.length();
		int max = 0;
		final int countMinusStart = count - start;
		if (countMinusStart >= 0) {
			max = Math.min(length, countMinusStart);
		}

		IngridHit[] hits = new IngridHit[max];
		for (int i = start; i < (max + start); i++) {
			final int id = luceneHits.id(i);
			final float score = luceneHits.score(i);
			IngridHit ingridHit = new IngridHit(this.fPlugId, id, -1, score);
			addGroupingInformation(ingridHit, query, count);
			hits[i - start] = ingridHit;
		}

		return new IngridHits(this.fPlugId, count, hits, true);
	}

	private void setTitleBoost() {
		// TODO boost was readed from plugdescription and database
		this.fTitleBoost = 1.0f;
	}

	private void addGroupingInformation(IngridHit ingridHit, IngridQuery query,
			int count) {
		String groupBy = query.getGrouped();
		if (groupBy == null) {
			return;
		}
		String[] groupInfos = null;
		if (IngridQuery.GROUPED_BY_PARTNER.equalsIgnoreCase(groupBy)) {
			groupInfos = getPlugDescription().getPartners();
		} else if (IngridQuery.GROUPED_BY_ORGANISATION
				.equalsIgnoreCase(groupBy)) {
			groupInfos = getPlugDescription().getProviders();
		} else if (IngridQuery.GROUPED_BY_DATASOURCE.equalsIgnoreCase(groupBy)) {
			ingridHit.setGroupTotalHitLength(count);
		}
		if (groupInfos != null) {
			for (int i = 0; i < groupInfos.length; i++) {
				ingridHit.addGroupedField(groupInfos[i]);
			}
		}
	}

	/**
	 * Creates a query for lucene from a ingrid query.
	 * 
	 * @param query
	 *            The ingrid query.
	 * @param addDataTypes
	 *            True if the datatypes should be on the resulting ingrid query.
	 * @return The created lucene query.
	 * @throws IOException
	 *             If something goes wrong.
	 */
	public static Query buildLuceneQuery(IngridQuery query, boolean addDataTypes)
			throws IOException {
		BooleanQuery booleanQuery = new BooleanQuery();
		// datatypes
		if (addDataTypes) {
			FieldQuery[] fields = query.getDataTypes();
			for (int i = 0; i < fields.length; i++) {
				FieldQuery fieldQuery = fields[i];
				if (fieldQuery.isRequred() && fieldQuery.isProhibited()) {
					booleanQuery.add(new org.apache.lucene.search.TermQuery(
							new Term(fieldQuery.getFieldName(), fieldQuery
									.getFieldValue().toLowerCase())), false,
							true);
				} else {
					booleanQuery.add(new org.apache.lucene.search.TermQuery(
							new Term(fieldQuery.getFieldName(), fieldQuery
									.getFieldValue().toLowerCase())),
							fieldQuery.isRequred(), fieldQuery.isProhibited());
				}
			}
		}

		// term, prefix term and phrase queries
		TermQuery[] terms = query.getTerms();
		for (int i = 0; i < terms.length; i++) {
			TermQuery termQuery = terms[i];
			String term = termQuery.getTerm().toLowerCase();
			if (term.indexOf(' ') > -1) {
				addPhraseQuery(booleanQuery, termQuery, term, fTitleBoost);
			} else {
				if (term.endsWith("*")) {
					String filteredTerm = filterTerm(term.substring(0, term
							.length() - 1));
					if (filteredTerm.indexOf(' ') > -1) {
						filteredTerm = "\"" + filteredTerm + "\"";
					}
					if (termQuery.isProhibited() && termQuery.isRequred()) {
						booleanQuery.add(new PrefixQuery(new Term(CONTENT,
								filteredTerm)), false, true);
					} else {
						booleanQuery.add(new PrefixQuery(new Term(CONTENT,
								filteredTerm)), termQuery.isRequred(),
								termQuery.isProhibited());
					}
					PrefixQuery titlePrefixQuery = new PrefixQuery(new Term(
							"title", filteredTerm));
					titlePrefixQuery.setBoost(fTitleBoost);
					booleanQuery.add(titlePrefixQuery, false, false);
				} else {
					String filteredTerm = filterTerm(term);
					if (filteredTerm.indexOf(' ') > -1) {
						addPhraseQuery(booleanQuery, termQuery, term,
								fTitleBoost);
					} else {
						if (termQuery.isProhibited() && termQuery.isRequred()) {
							booleanQuery.add(
									new org.apache.lucene.search.TermQuery(
											new Term(CONTENT, filteredTerm)),
									false, true);
						} else {
							booleanQuery.add(
									new org.apache.lucene.search.TermQuery(
											new Term(CONTENT, filteredTerm)),
									termQuery.isRequred(), termQuery
											.isProhibited());
						}
						org.apache.lucene.search.TermQuery titleTermQuery = new org.apache.lucene.search.TermQuery(
								new Term("title", filteredTerm));
						titleTermQuery.setBoost(fTitleBoost);
						booleanQuery.add(titleTermQuery, false, false);
					}
				}
			}
		}
		// field queries
		FieldQuery[] fields = query.getFields();
		processGeoAndTimeQueries(fields, booleanQuery);

		// subclauses
		ClauseQuery[] clauses = query.getClauses();
		for (int i = 0; i < clauses.length; i++) {
			final ClauseQuery clauseQuery = clauses[i];
			final Query sc = buildLuceneQuery(clauseQuery, addDataTypes);
			if (!sc.equals(fBOOLEANQUERYCOMPARATOROBJECT)) {
				if (clauseQuery.isProhibited() && clauseQuery.isRequred()) {
					booleanQuery.add(sc, false, true);
				} else {
					booleanQuery.add(sc, clauseQuery.isRequred(), clauseQuery
							.isProhibited());
				}
			}
		}

		// range queries
		RangeQuery[] rangeQueries = query.getRangeQueries();
		for (int i = 0; i < rangeQueries.length; i++) {
			RangeQuery rangeQuery = rangeQueries[i];
			boolean prohibited = rangeQuery.isProhibited();
			boolean required = rangeQuery.isRequred();
			Term termFrom = new Term(rangeQuery.getRangeName(), rangeQuery
					.getRangeFrom().toLowerCase());
			Term termTo = new Term(rangeQuery.getRangeName(), rangeQuery
					.getRangeTo().toLowerCase());
			org.apache.lucene.search.RangeQuery luceneQuery = new org.apache.lucene.search.RangeQuery(
					termFrom, termTo, rangeQuery.isInclusive());
			if (required && prohibited) {
				booleanQuery.add(luceneQuery, false, true);
			} else {
				booleanQuery.add(luceneQuery, required, prohibited);
			}
		}

		// wildcard fields
		WildCardFieldQuery[] wildCardFieldQueries = query
				.getWildCardFieldQueries();
		for (int i = 0; i < wildCardFieldQueries.length; i++) {
			WildCardFieldQuery wildCardQuery = wildCardFieldQueries[i];
			boolean isProhibited = wildCardQuery.isProhibited();
			boolean isRequired = wildCardQuery.isRequred();
			WildcardQuery luceneQuery = new WildcardQuery(
					new Term(wildCardQuery.getFieldName(), wildCardQuery
							.getFieldValue()));
			if (isProhibited && isRequired) {
				booleanQuery.add(luceneQuery, false, true);
			} else {
				booleanQuery.add(luceneQuery, isRequired, isProhibited);
			}
		}

		// wildcard terms
		WildCardTermQuery[] wildCardTermQueries = query
				.getWildCardTermQueries();
		for (int i = 0; i < wildCardTermQueries.length; i++) {
			WildcardQuery luceneQuery = new WildcardQuery(new Term(CONTENT,
					wildCardTermQueries[i].getTerm()));
			if (wildCardTermQueries[i].isRequred()
					&& wildCardTermQueries[i].isProhibited()) {
				booleanQuery.add(luceneQuery, false, true);
			} else {
				booleanQuery.add(luceneQuery, wildCardTermQueries[i]
						.isRequred(), wildCardTermQueries[i].isProhibited());
			}
		}

		// fuzzy fields
		FuzzyFieldQuery[] fuzzyFieldQueries = query.getFuzzyFieldQueries();
		for (int i = 0; i < fuzzyFieldQueries.length; i++) {
			FuzzyFieldQuery fuzzyFieldQuery = fuzzyFieldQueries[i];
			FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(fuzzyFieldQuery
					.getFieldName(), fuzzyFieldQuery.getFieldValue()));
			booleanQuery.add(fuzzyQuery, fuzzyFieldQuery.isRequred(),
					fuzzyFieldQuery.isProhibited());
		}

		// fuzzy terms
		FuzzyTermQuery[] fuzzyTermQueries = query.getFuzzyTermQueries();
		for (int i = 0; i < fuzzyTermQueries.length; i++) {
			FuzzyTermQuery fuzzyTermQuery = fuzzyTermQueries[i];
			FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(CONTENT,
					fuzzyTermQuery.getTerm()));
			if (fuzzyTermQuery.isRequred() && fuzzyTermQuery.isProhibited()) {
				booleanQuery.add(fuzzyQuery, false, true);
			} else {
				booleanQuery.add(fuzzyQuery, fuzzyTermQuery.isRequred(),
						fuzzyTermQuery.isProhibited());
			}
		}

		return booleanQuery;
	}

	private static void addPhraseQuery(BooleanQuery booleanQuery,
			TermQuery termQuery, String term, float titleBoost)
			throws IOException {
		PhraseQuery phraseQuery = new PhraseQuery();
		PhraseQuery titleQuery = new PhraseQuery();
		titleQuery.setBoost(titleBoost);
		StringTokenizer tokenizer = new StringTokenizer(term);
		while (tokenizer.hasMoreTokens()) {
			final String filteredTerm = filterTerm(tokenizer.nextToken());
			phraseQuery.add(new Term(CONTENT, filteredTerm));
			titleQuery.add(new Term("title", filteredTerm));
		}
		if (termQuery.isProhibited() && termQuery.isRequred()) {
			booleanQuery.add(phraseQuery, false, true);
		} else {
			booleanQuery.add(phraseQuery, termQuery.isRequred(), termQuery
					.isProhibited());
		}
		booleanQuery.add(titleQuery, false, false);
	}

	/**
	 * @param fields
	 * @param booleanQuery
	 */
	private static void processGeoAndTimeQueries(FieldQuery[] fields,
			BooleanQuery booleanQuery) {
		Map geoMap = new HashMap(fields.length);
		Map timeMap = new HashMap(fields.length);
		for (int i = 0; i < fields.length; i++) {
			FieldQuery query = fields[i];
			String indexField = query.getFieldName();
			String value = query.getFieldValue().toLowerCase();
			if (indexField.equals("x1")) {
				geoMap.put(indexField, DoublePadding.padding(Double
						.parseDouble(value)));
			} else if (indexField.equals("x2")) {
				geoMap.put(indexField, DoublePadding.padding(Double
						.parseDouble(value)));
			} else if (indexField.equals("y1")) {
				geoMap.put(indexField, DoublePadding.padding(Double
						.parseDouble(value)));
			} else if (indexField.equals("y2")) {
				geoMap.put(indexField, DoublePadding.padding(Double
						.parseDouble(value)));
			} else if (indexField.equals("coord")) {
				List list = (List) geoMap.get(indexField);
				if (list == null) {
					list = new LinkedList();
				}
				list.add(value);
				geoMap.put(indexField, list);
			} else if ("t0".equals(indexField)) {
				timeMap.put(indexField, value);
			} else if ("t1".equals(indexField)) {
				timeMap.put(indexField, value);
			} else if ("t2".equals(indexField)) {
				timeMap.put(indexField, value);
			} else if ("time".equals(indexField)) {
				List list = (List) timeMap.get(indexField);
				if (list == null) {
					list = new LinkedList();
				}
				list.add(value);
				timeMap.put(indexField, list);
			} else if ("incl_meta".equals(indexField) && "on".equals(value)) {
				booleanQuery.add(new org.apache.lucene.search.TermQuery(
						new Term(query.getFieldName(), query.getFieldValue()
								.toLowerCase())), false, false);
			} else {
				final String term = query.getFieldValue().toLowerCase();
				final String field = query.getFieldName();

				if (term.indexOf(' ') > -1) {
					PhraseQuery phraseQuery = new PhraseQuery();
					StringTokenizer tokenizer = new StringTokenizer(term);
					while (tokenizer.hasMoreTokens()) {
						phraseQuery.add(new Term(field, tokenizer.nextToken()));
					}
					booleanQuery.add(phraseQuery, query.isRequred(), query
							.isProhibited());
				} else {
					booleanQuery.add(new org.apache.lucene.search.TermQuery(
							new Term(field, term)), query.isRequred(), query
							.isProhibited());
				}
			}
		}

		if (null == geoMap.get("coord")) {
			final List list = new LinkedList();
			list.add("exact");
			geoMap.put("coord", list);
		}
		prepareGeo(booleanQuery, geoMap);
		prepareTime(booleanQuery, timeMap);

	}

	/**
	 * @param term
	 * @return filtered term
	 * @throws IOException
	 */
	public static String filterTerm(String term) throws IOException {
		String result = "";

		TokenStream ts = fAnalyzer.tokenStream(null, new StringReader(term));
		Token token = ts.next();
		while (null != token) {
			result = result + " " + token.termText();
			token = ts.next();
		}

		return result.trim();
	}

	public IngridHitDetail getDetail(IngridHit hit, IngridQuery ingridQuery,
			String[] fields) throws Exception {
		checkLock();
		Document luceneDocument = this.fSearcher.doc(hit.getDocumentId());
		// TODO we may found something more sensefully than just return a
		// hardcoded message here..
		String title = luceneDocument.get(FIELD_NAME_TITLE);
		title = title != null ? title : "no title indexed";
		String summary = luceneDocument.get(FIELD_NAME_SUMMARY);
		summary = summary != null ? summary : "no summary indexed";

		IngridHitDetail detail = new IngridHitDetail(hit, title, summary);
		for (int i = 0; i < fields.length; i++) {
			String[] values = luceneDocument.getValues(fields[i].toLowerCase());
			if (values != null) {
				detail.put(fields[i], values);
			}
		}
		// adding a detail url there is one.
		try {
			if (this.fUrl != null) {
				detail
						.put("url", constructDetailUrl(this.fUrl,
								luceneDocument));
			} else {
				// if there is no general url we try to find one in the
				// document
				String url = luceneDocument.get(DETAIL_URL);
				if (url != null) {
					detail.put("url", constructDetailUrl(url, luceneDocument));
				}
			}
		} catch (Exception e) {
			log.error("exception on adding detail url");
		}
		addPlugDescriptionInformations(detail, fields);
		return detail;
	}

	private void addPlugDescriptionInformations(IngridHitDetail detail,
			String[] fields) {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equals(PlugDescription.PARTNER)) {
				detail.setArray(PlugDescription.PARTNER, getPlugDescription()
						.getPartners());
			} else if (fields[i].equals(PlugDescription.PROVIDER)) {
				detail.setArray(PlugDescription.PROVIDER, getPlugDescription()
						.getProviders());
			}
		}

	}

	public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query,
			String[] requestedFields) throws Exception {
		IngridHitDetail[] details = new IngridHitDetail[hits.length];
		for (int i = 0; i < hits.length; i++) {
			details[i] = getDetail(hits[i], query, requestedFields);
		}
		return details;
	}

	public abstract Record getRecord(IngridHit hit) throws IOException,
			Exception;

	protected void finalize() throws Throwable {
		this.fSearcher.close();
	}

	/**
	 * Creates a detailed url for the construct.
	 * 
	 * @param url
	 *            The url to find.
	 * @param document
	 *            A document to search in.
	 * @return The created url.
	 * @throws IllegalArgumentException
	 */
	public static String constructDetailUrl(String url, Document document)
			throws IllegalArgumentException {
		StringBuffer buffer = new StringBuffer();
		int lastPos = -1;
		int pos = -1;
		while ((pos = url.indexOf("{", lastPos)) > -1) {
			buffer.append(url.substring(lastPos + 1, pos));
			// find key:
			lastPos = url.indexOf("}", pos);
			if (lastPos == -1) {
				throw new IllegalArgumentException("url pattern not valid");
			}
			String key = url.substring(pos + 1, lastPos);
			buffer.append(document.get(key));

		}
		return buffer.toString();
	}

	/**
	 * The instance of the IPlug searcher.
	 * 
	 * @return The instance.
	 */
	public static AbstractSearcher getInstance() {
		synchronized (AbstractSearcher.class) {
			return fInstance;
		}
	}

	/**
	 * This method stops the searcher.
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception {
		synchronized (this.fMonitor) {
			this.fIsLocked = true;
			if (this.fSearcher != null) {
				this.fSearcher.close();
			}
		}
	}

	/**
	 * This method starts the searcher.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception {
		configure(getPlugDescription());
		this.fIsLocked = false;
		synchronized (this.fMonitor) {
			this.fMonitor.notify();
		}
	}

	private void checkLock() {
		while (this.fIsLocked) {
			synchronized (this.fMonitor) {
				try {
					this.fMonitor.wait(1000);
				} catch (InterruptedException e) {
					// nothing to do.
				}
			}
		}
	}

	/**
	 * Returns the IPlug description.
	 * 
	 * @return The IPlug description.
	 */
	public abstract PlugDescription getPlugDescription();

	private static void prepareGeo(BooleanQuery booleanQuery, Map geoMap) {
		List list = (List) geoMap.get("coord");
		if (list != null) {
			BooleanQuery.setMaxClauseCount(10240);
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				String value = (String) iterator.next();
				if ("inside".equals(value)) {
					// innerhalb
					prepareInsideGeoQuery(booleanQuery, geoMap);
				} else if ("intersect".equals(value)) {
					// schneiden
					prepareIntersectGeoQuery(booleanQuery, geoMap);
				} else if ("include".equals(value)) {
					// enthalten
					prepareIncludeGeoQuery(booleanQuery, geoMap);
				} else {
					prepareExactGeoQuery(booleanQuery, geoMap);
				}
			}
		}
	}

	private static void prepareIncludeGeoQuery(BooleanQuery booleanQuery,
			Map geoMap) {
		String x1 = (String) geoMap.get("x1");
		String x2 = (String) geoMap.get("x2");
		String y1 = (String) geoMap.get("y1");
		String y2 = (String) geoMap.get("y2");

		if (x1 != null && x2 != null && y1 != null && y2 != null) {
			Term x1Term1 = new Term("x1", x1);
			Term x2Term1 = new Term("x2", x2);
			Term y1Term1 = new Term("y1", y1);
			Term y2Term1 = new Term("y2", y2);

			Term x1TermMin = new Term("x1", DoublePadding.padding(5.3));
			Term x2TermMax = new Term("x2", DoublePadding.padding(14.77));
			Term y1TermMin = new Term("y1", DoublePadding.padding(46.76));
			Term y2TermMax = new Term("y2", DoublePadding.padding(54.73));

			Query xRangeQuery1 = new org.apache.lucene.search.RangeQuery(
					x1TermMin, x1Term1, true);
			Query xRangeQuery2 = new org.apache.lucene.search.RangeQuery(
					x2Term1, x2TermMax, true);
			Query yRangeQuery1 = new org.apache.lucene.search.RangeQuery(
					y1TermMin, y1Term1, true);
			Query yRangeQuery2 = new org.apache.lucene.search.RangeQuery(
					y2Term1, y2TermMax, true);

			booleanQuery.add(xRangeQuery1, true, false);
			booleanQuery.add(xRangeQuery2, true, false);
			booleanQuery.add(yRangeQuery1, true, false);
			booleanQuery.add(yRangeQuery2, true, false);
		}

	}

	private static void prepareExactGeoQuery(BooleanQuery booleanQuery,
			Map geoMap) {
		String x1 = (String) geoMap.get("x1");
		String x2 = (String) geoMap.get("x2");
		String y1 = (String) geoMap.get("y1");
		String y2 = (String) geoMap.get("y2");

		if (x1 != null && x2 != null && y1 != null && y2 != null) {
			Term x1Term1 = new Term("x1", x1);
			Term x2Term1 = new Term("x2", x2);
			Term y1Term1 = new Term("y1", y1);
			Term y2Term1 = new Term("y2", y2);

			Query xTermQuery1 = new org.apache.lucene.search.TermQuery(x1Term1);
			Query xTermQuery2 = new org.apache.lucene.search.TermQuery(x2Term1);
			Query yTermQuery1 = new org.apache.lucene.search.TermQuery(y1Term1);
			Query yTermQuery2 = new org.apache.lucene.search.TermQuery(y2Term1);

			booleanQuery.add(xTermQuery1, true, false);
			booleanQuery.add(xTermQuery2, true, false);
			booleanQuery.add(yTermQuery1, true, false);
			booleanQuery.add(yTermQuery2, true, false);
		}

	}

	/**
	 * @param booleanQuery
	 * @param geoMap
	 */
	private static void prepareIntersectGeoQuery(BooleanQuery booleanQuery,
			Map geoMap) {
		String x1 = (String) geoMap.get("x1");
		String x2 = (String) geoMap.get("x2");
		String y1 = (String) geoMap.get("y1");
		String y2 = (String) geoMap.get("y2");

		BooleanQuery geoQuery = new BooleanQuery();
		if (x1 != null && x2 != null && y1 != null && y2 != null) {
			BooleanQuery query1 = prepareIntersectGeoQuery(x1, x2, y1, y2,
					FIRST_X_CASE);
			BooleanQuery query2 = prepareIntersectGeoQuery(x1, x2, y1, y2,
					SECOND_X_CASE);
			BooleanQuery query3 = prepareIntersectGeoQuery(x1, x2, y1, y2,
					THIRD_X_CASE);

			geoQuery.add(query1, false, false);
			geoQuery.add(query2, false, false);
			geoQuery.add(query3, false, false);
			if (geoQuery.getClauses().length > 0) {
				booleanQuery.add(geoQuery, true, false);
			}
		}
	}

	/**
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param x_case
	 * @return booleanquery
	 */
	private static BooleanQuery prepareIntersectGeoQuery(String x1, String x2,
			String y1, String y2, int x_case) {

		BooleanQuery booleanQuery = new BooleanQuery();
		Term x1Term1 = new Term("x1", x1);
		Term x1Term2 = new Term("x1", x2);
		Term x2Term1 = new Term("x2", x1);
		Term x2Term2 = new Term("x2", x2);
		Term y1Term1 = new Term("y1", y1);
		Term y1Term2 = new Term("y1", y2);
		Term y2Term1 = new Term("y2", y1);
		Term y2Term2 = new Term("y2", y2);
		Term y1TermMin = new Term("y1", DoublePadding.padding(46.76));
		Term y2TermMax = new Term("y2", DoublePadding.padding(54.73));

		switch (x_case) {
		case FIRST_X_CASE:
			BooleanQuery xQuery1FirstCase = new BooleanQuery();
			BooleanQuery xQuery2FirstCase = new BooleanQuery();
			BooleanQuery yQueryFistCase = new BooleanQuery();
			BooleanQuery yOutside = new BooleanQuery();

			org.apache.lucene.search.RangeQuery xRangeQuery1 = new org.apache.lucene.search.RangeQuery(
					x1Term1, x1Term2, true);
			org.apache.lucene.search.RangeQuery xRangeQuery2 = new org.apache.lucene.search.RangeQuery(
					x2Term1, x2Term2, true);
			org.apache.lucene.search.RangeQuery xRangeQuery3 = new org.apache.lucene.search.RangeQuery(
					x1Term1, x1Term2, true);
			org.apache.lucene.search.RangeQuery xRangeQuery4 = new org.apache.lucene.search.RangeQuery(
					x2Term1, x2Term2, true);

			org.apache.lucene.search.RangeQuery yRangeQuery1 = new org.apache.lucene.search.RangeQuery(
					y1Term1, y1Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery2 = new org.apache.lucene.search.RangeQuery(
					y2Term1, y2Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery3 = new org.apache.lucene.search.RangeQuery(
					y1TermMin, y1Term1, true);
			org.apache.lucene.search.RangeQuery yRangeQuery4 = new org.apache.lucene.search.RangeQuery(
					y2Term1, y2TermMax, true);

			// must: true, false must_not: false, true should: false, false
			xQuery1FirstCase.add(xRangeQuery1, true, false);
			xQuery1FirstCase.add(xRangeQuery2, false, true);
			xQuery2FirstCase.add(xRangeQuery3, false, true);
			xQuery2FirstCase.add(xRangeQuery4, true, false);

			yOutside.add(yRangeQuery3, true, false);
			yOutside.add(yRangeQuery4, true, false);

			yQueryFistCase.add(yRangeQuery1, false, false);
			yQueryFistCase.add(yRangeQuery2, false, false);
			yQueryFistCase.add(yOutside, false, false);

			booleanQuery.add(xQuery1FirstCase, false, false);
			booleanQuery.add(xQuery2FirstCase, false, false);

			break;

		case SECOND_X_CASE:
			org.apache.lucene.search.RangeQuery xRangeQuery1SecondCase = new org.apache.lucene.search.RangeQuery(
					x1Term1, x1Term2, true);
			org.apache.lucene.search.RangeQuery xRangeQuery2SecondCase = new org.apache.lucene.search.RangeQuery(
					x2Term1, x2Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery1SecondCase = new org.apache.lucene.search.RangeQuery(
					y1Term1, y1Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery2SecondCase = new org.apache.lucene.search.RangeQuery(
					y2Term1, y2Term2, true);
			booleanQuery.add(xRangeQuery1SecondCase, true, false);
			booleanQuery.add(xRangeQuery2SecondCase, true, false);
			booleanQuery.add(yRangeQuery1SecondCase, false, true);
			booleanQuery.add(yRangeQuery2SecondCase, false, true);
			break;

		case THIRD_X_CASE:
			BooleanQuery thirdCase = new BooleanQuery();
			org.apache.lucene.search.RangeQuery xRangeQuery1ThirdCase = new org.apache.lucene.search.RangeQuery(
					x1Term1, x1Term2, true);
			org.apache.lucene.search.RangeQuery xRangeQuery2ThirdCase = new org.apache.lucene.search.RangeQuery(
					x2Term1, x2Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery1ThirdCase = new org.apache.lucene.search.RangeQuery(
					y1Term1, y1Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery2ThirdCase = new org.apache.lucene.search.RangeQuery(
					y2Term1, y2Term2, true);
			thirdCase.add(yRangeQuery1ThirdCase, false, false);
			thirdCase.add(yRangeQuery2ThirdCase, false, false);
			booleanQuery.add(xRangeQuery1ThirdCase, false, true);
			booleanQuery.add(xRangeQuery2ThirdCase, false, true);
			booleanQuery.add(thirdCase, true, false);
			break;

		default:
			break;
		}

		return booleanQuery;
	}

	private static void prepareInsideGeoQuery(BooleanQuery booleanQuery,
			Map geoMap) {
		String x1 = (String) geoMap.get("x1");
		String x2 = (String) geoMap.get("x2");
		String y1 = (String) geoMap.get("y1");
		String y2 = (String) geoMap.get("y2");

		if (x1 != null && x2 != null && y1 != null && y2 != null) {
			Term x1Term1 = new Term("x1", x1);
			Term x1Term2 = new Term("x1", x2);

			Term y1Term1 = new Term("y1", y1);
			Term y1Term2 = new Term("y1", y2);

			Term x2Term1 = new Term("x2", x1);
			Term x2Term2 = new Term("x2", x2);

			Term y2Term1 = new Term("y2", y1);
			Term y2Term2 = new Term("y2", y2);

			org.apache.lucene.search.RangeQuery xRangeQuery1 = new org.apache.lucene.search.RangeQuery(
					x1Term1, x1Term2, true);
			org.apache.lucene.search.RangeQuery xRangeQuery2 = new org.apache.lucene.search.RangeQuery(
					x2Term1, x2Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery1 = new org.apache.lucene.search.RangeQuery(
					y1Term1, y1Term2, true);
			org.apache.lucene.search.RangeQuery yRangeQuery2 = new org.apache.lucene.search.RangeQuery(
					y2Term1, y2Term2, true);

			booleanQuery.add(xRangeQuery1, true, false);
			booleanQuery.add(xRangeQuery2, true, false);
			booleanQuery.add(yRangeQuery1, true, false);
			booleanQuery.add(yRangeQuery2, true, false);

		}
	}

	private static void prepareTime(BooleanQuery query, Map timeMap) {
		if (log.isDebugEnabled()) {
			log
					.debug("start prepareTime with t0=" + timeMap.get("t0")
							+ ", t1:" + timeMap.get("t1") + ", t2:"
							+ timeMap.get("t2"));
		}

		List list = (List) timeMap.get("time");
		if (list == null) {
			// nothing selected -> default inside
			prepareInsideTime(query, timeMap);
		} else {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				String value = (String) iterator.next();
				if ("intersect".equals(value)) {
					// innerhalb oder schneidet
					prepareInsideOrIntersectTime(query, timeMap);
				} else if ("include".equals(value)) {
					// innerhalb oder umschliesst
					prepareInsideOrIncludeQuery(query, timeMap);
				} else {
					prepareInsideTime(query, timeMap);
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("resulting query after prepareTime:" + query.toString());
		}

	}

	private static void prepareInsideOrIncludeQuery(BooleanQuery query,
			Map timeMap) {
		BooleanQuery booleanQueryTime = new BooleanQuery();
		BooleanQuery inside = new BooleanQuery();
		BooleanQuery include = new BooleanQuery();
		prepareInsideTime(inside, timeMap);
		prepareIncludeTimeQuery(include, timeMap);
		if (include.getClauses().length > 0) {
			booleanQueryTime.add(include, false, false);
		}
		if (inside.getClauses().length > 0) {
			booleanQueryTime.add(inside, false, false);
		}

		if (booleanQueryTime.getClauses().length > 0) {
			query.add(booleanQueryTime, true, false);
		}
	}

	private static void prepareInsideOrIntersectTime(BooleanQuery query,
			Map timeMap) {
		BooleanQuery booleanQueryTime = new BooleanQuery();
		BooleanQuery inside = new BooleanQuery();
		BooleanQuery traverse = new BooleanQuery();
		prepareInsideTime(inside, timeMap);
		if (inside.getClauses().length > 0) {
			booleanQueryTime.add(inside, false, false);
		}
		prepareTraverseTime(traverse, timeMap);
		if (traverse.getClauses().length > 0) {
			booleanQueryTime.add(traverse, false, false);
		}
		if (booleanQueryTime.getClauses().length > 0) {
			query.add(booleanQueryTime, true, false);
		}
	}

	private static void prepareInsideTime(BooleanQuery query, Map timeMap) {
		String t0 = (String) timeMap.get("t0");
		String t1 = (String) timeMap.get("t1");
		String t2 = (String) timeMap.get("t2");
		if (t1 != null && t2 != null) {
			// e.g. 2006-04-05 -> 20040405
			t1 = t1.replaceAll("-", "");
			t2 = t2.replaceAll("-", "");
			Term termT1Min = new Term("t1", t1);
			Term termT1Max = new Term("t1", t2);

			Term termT2Min = new Term("t2", t1);
			Term termT2Max = new Term("t2", t2);

			// we must match also documents where t0 are in this range
			Term termT0Min = new Term("t0", t1);
			Term termT0Max = new Term("t0", t2);

			org.apache.lucene.search.RangeQuery rangeQueryt0 = new org.apache.lucene.search.RangeQuery(
					termT0Min, termT0Max, true);
			org.apache.lucene.search.RangeQuery rangeQuery11 = new org.apache.lucene.search.RangeQuery(
					termT1Min, termT1Max, true);
			org.apache.lucene.search.RangeQuery rangeQuery12 = new org.apache.lucene.search.RangeQuery(
					termT2Min, termT2Max, true);

			// connect with AND
			BooleanQuery booleanQueryT1T2 = new BooleanQuery();
			booleanQueryT1T2.add(rangeQuery11, true, false);
			booleanQueryT1T2.add(rangeQuery12, true, false);
			// connect with OR
			BooleanQuery booleanQueryTime = new BooleanQuery();
			booleanQueryTime.add(booleanQueryT1T2, false, false);
			booleanQueryTime.add(rangeQueryt0, false, false);
			// connect to whole query with AND
			query.add(booleanQueryTime, true, false);
		} else if (null != t0) {
			t0 = t0.replaceAll("-", "");
			Term termT0 = new Term("t0", t0);
			query.add(new org.apache.lucene.search.TermQuery(termT0), true,
					false);
		}
	}

	private static void prepareIncludeTimeQuery(BooleanQuery query, Map timeMap) {
		String t0 = (String) timeMap.get("t0");
		String t1 = (String) timeMap.get("t1");
		String t2 = (String) timeMap.get("t2");
		if (t1 != null && t2 != null) {
			// e.g. 2006-04-05 -> 20040405
			t1 = t1.replaceAll("-", "");
			t2 = t2.replaceAll("-", "");
			Term termT1Min = new Term("t1", "00000000");
			Term termT2Max = new Term("t2", "99999999");
			Term termT1Max = new Term("t1", t1);
			Term termT2Min = new Term("t2", t2);

			org.apache.lucene.search.RangeQuery rangeQuery11 = new org.apache.lucene.search.RangeQuery(
					termT1Min, termT1Max, true);
			org.apache.lucene.search.RangeQuery rangeQuery12 = new org.apache.lucene.search.RangeQuery(
					termT2Min, termT2Max, true);

			query.add(rangeQuery11, true, false);
			query.add(rangeQuery12, true, false);
		} else if (null != t0) {
			t0 = t0.replaceAll("-", "");
			Term termT1 = new Term("t1", t0);
			Term termT2 = new Term("t2", t0);
			Term min = new Term("t1", "00000000");
			Term max = new Term("t2", "99999999");
			org.apache.lucene.search.RangeQuery rangeQueryT1 = new org.apache.lucene.search.RangeQuery(
					min, termT1, false);
			org.apache.lucene.search.RangeQuery rangeQueryT2 = new org.apache.lucene.search.RangeQuery(
					termT2, max, false);
			query.add(rangeQueryT1, true, false);
			query.add(rangeQueryT2, true, false);
		}

	}

	private static void prepareTraverseTime(BooleanQuery query, Map timeMap) {
		String t0 = (String) timeMap.get("t0");
		String t1 = (String) timeMap.get("t1");
		String t2 = (String) timeMap.get("t2");
		if (t1 != null && t2 != null) {
			// e.g. 2006-04-05 -> 20040405
			t1 = t1.replaceAll("-", "");
			t2 = t2.replaceAll("-", "");

			// (ti2:[tq1 TO tq2] && ti1:[00000000 TO tq1]) || (ti1:[tq1 TO tq2]
			// && ti2:[tq2 TO 99999999])
			Term termT1Min = new Term("t1", "00000000");
			Term termT1Date1 = new Term("t1", t1);
			Term termT1Date2 = new Term("t1", t2);

			Term termT2Max = new Term("t2", "99999999");
			Term termT2Date1 = new Term("t2", t1);
			Term termT2Date2 = new Term("t2", t2);

			org.apache.lucene.search.RangeQuery rangeQuery11 = new org.apache.lucene.search.RangeQuery(
					termT1Min, termT1Date1, true);
			org.apache.lucene.search.RangeQuery rangeQuery12 = new org.apache.lucene.search.RangeQuery(
					termT2Date1, termT2Date2, true);

			org.apache.lucene.search.RangeQuery rangeQuery21 = new org.apache.lucene.search.RangeQuery(
					termT1Date1, termT1Date2, true);
			org.apache.lucene.search.RangeQuery rangeQuery22 = new org.apache.lucene.search.RangeQuery(
					termT2Date2, termT2Max, true);

			BooleanQuery booleanQueryTime = new BooleanQuery();
			BooleanQuery first = new BooleanQuery();
			first.add(rangeQuery11, true, false);
			first.add(rangeQuery12, true, false);

			BooleanQuery second = new BooleanQuery();
			second.add(rangeQuery21, true, false);
			second.add(rangeQuery22, true, false);

			booleanQueryTime.add(first, false, false);
			booleanQueryTime.add(second, false, false);
			query.add(booleanQueryTime, true, false);
		} else if (null != t0) {
			t0 = t0.replaceAll("-", "");
			Term termT0 = new Term("t0", t0);
			Term termT1 = new Term("t1", t0);
			Term termT2 = new Term("t2", t0);

			BooleanQuery booleanQueryTime = new BooleanQuery();
			booleanQueryTime.add(
					new org.apache.lucene.search.TermQuery(termT0), false,
					false);
			booleanQueryTime.add(
					new org.apache.lucene.search.TermQuery(termT1), false,
					false);
			booleanQueryTime.add(
					new org.apache.lucene.search.TermQuery(termT2), false,
					false);
			query.add(booleanQueryTime, true, false);
		}
	}
}
