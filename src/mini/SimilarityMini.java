package mini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;

public class SimilarityMini {

	static String[] termsArray;
	static int totalNumDocs;

	static HashSet<String> termSet 									= new HashSet<String>();
	static HashMap<String, HashMap<String, Integer>> docTermFreqMap = new HashMap<String, HashMap<String, Integer>>();
	static HashMap<String, Integer> invDocFreqMap 					= new HashMap<String, Integer>();
	static HashMap<String, Integer> maxTfMap 						= new HashMap<String, Integer>();
	static HashMap<Integer, Integer> maxQueryTfMap 					= new HashMap<Integer, Integer>();
	static HashMap<Integer, TreeMap<String, Integer>> queryMap 		= new HashMap<Integer, TreeMap<String, Integer>>();
	static HashMap<String, HashSet<String>> idfMap 					= new HashMap<String, HashSet<String>>();
	static HashMap<Integer, HashMap<String, Double>> queryRelMap 	= new HashMap<Integer, HashMap<String, Double>>();

	//BM25 constants
	static final double B = 0.75;
	static final double K1 = 1.2;
	static final double K2 = 100.;

	public static void init() {

		docTermFreqMap = getTermFrequency(Constants.DATA_DIR + Constants.CACM_IDX);
		queryMap = loadTokenizedQueries(Constants.CACM_QUERY);
		idfMap = getInverseDocFreq(docTermFreqMap);

		// initialize termsArray
		termsArray = new String[termSet.size()];
		initializeTermsArray();

		// initialize maxTfMap
		maxTfMap(docTermFreqMap);

		// initialize maxQueryTfMap
		maxQueryTfMap(queryMap);

		totalNumDocs = docTermFreqMap.size();
	}

	public static void initMed() {

		docTermFreqMap = getTermFrequency(Constants.DATA_DIR + Constants.MEDL_IDX);
		queryMap = loadTokenizedQueries(Constants.MED_QUERY);
		idfMap = getInverseDocFreq(docTermFreqMap);

		// initialize termsArray
		termsArray = new String[termSet.size()];
		initializeTermsArray();

		// initialize maxTfMap
		maxTfMap(docTermFreqMap);

		// initialize maxQueryTfMap
		maxQueryTfMap(queryMap);

		totalNumDocs = docTermFreqMap.size();

	}

	/*****************************************
	 * QUESTION 3 PART A
	 * ***************************************
	 */
	// Q3A: CACM
	public static void printMapPartACACM(int numDocs) {
		init();

		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);

		// CACM
		int docSize = Math.min(numDocs, totalNumDocs);
		double[] cacmMap = new double[docSize];
		System.out.println("3a. Retrieving docs from CACM...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocs(i, numDocs);
			cacmMap[i] = mapPrecision(answerMap.get(i), result);
		}
		System.out.println("Calculating MAP for CACM tfidf atc.atc...");
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			sum += cacmMap[i];
		}
		System.out.println("Question 3a CACM MAP: " + sum/queryMap.size());
	}

	// Q3A: MED
	public static void printMapPartAMed(int numDocs) {
		// MED

		initMed();
		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);

		int docSize = Math.min(numDocs, totalNumDocs);
		double[] medMap = new double[docSize];
		System.out.println("3a. Retrieving docs from MED...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocs(i, numDocs);
			medMap[i] = mapPrecision(medAnswerMap.get(i), result);
		}
		System.out.println("Calculating MAP for MED tfidf atc.atc...");
		double medsum = 0.0;
		for (int i = 0; i < medMap.length; i++) {
			medsum += medMap[i];
		}
		System.out.println("Question 3a MED MAP: " + medsum/queryMap.size());
	}

	/*****************************************
	 * QUESTION 3 PART B
	 * ***************************************/
	// Q3B: CACM
	public static void printMapPartBCACM(int numDocs) {
		init();

		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);

		// CACM
		int docSize = Math.min(numDocs, totalNumDocs);
		double[] cacmMap = new double[docSize];
		System.out.println("3b. Retrieving docs from CACM...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocsNoNorm(i, numDocs);
			cacmMap[i] = mapPrecision(answerMap.get(i), result);
		}
		System.out.println("Calculating MAP for CACM tfidf atn.atn...");
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			sum += cacmMap[i];
		}
		System.out.println("Question 3b CACM MAP: " + sum/queryMap.size());
	}

	// Q3B: MED
	public static void printMapPartBMed(int numDocs) {
		// MED

		initMed();
		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);

		int docSize = Math.min(numDocs, totalNumDocs);
		double[] medMap = new double[docSize];
		System.out.println("3b. Retrieving docs from MED...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocsNoNorm(i, numDocs);
			medMap[i] = mapPrecision(medAnswerMap.get(i), result);
		}
		System.out.println("Calculating MAP for MED tfidf atn.atn...");
		double medsum = 0.0;
		for (int i = 0; i < medMap.length; i++) {
			medsum += medMap[i];
		}
		System.out.println("Question 3b MED MAP: " + medsum/queryMap.size());
	}

	/*****************************************
	 * QUESTION 3 PART C
	 * ***************************************/
	// Q3C: CACM
	public static void printMapPartCCACM(int numDocs) {
		init();

		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);

		// CACM
		int docSize = Math.min(numDocs, totalNumDocs);
		double[] cacmMap = new double[docSize];
		System.out.println("3c. Retrieving docs from CACM...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocsNoNormC(i, numDocs);
			cacmMap[i] = mapPrecision(answerMap.get(i), result);
		}
		System.out.println("Calculating MAP for CACM tfidf ann.bpn...");
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			sum += cacmMap[i];
		}
		System.out.println("Question 3c CACM MAP: " + sum/queryMap.size());
	}

	// Q3C: MED
	public static void printMapPartCMed(int numDocs) {
		// MED

		initMed();
		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);

		int docSize = Math.min(numDocs, totalNumDocs);
		double[] medMap = new double[docSize];
		System.out.println("3c. Retrieving docs from MED...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocsNoNormC(i, numDocs);
			medMap[i] = mapPrecision(medAnswerMap.get(i), result);
		}
		System.out.println("Calculating MAP for MED tfidf ann.bpn...");
		double medsum = 0.0;
		for (int i = 0; i < medMap.length; i++) {
			medsum += medMap[i];
		}
		System.out.println("Question 3c MED MAP: " + medsum/queryMap.size());
	}
	
	/*****************************************
	 * QUESTION 3 PART D
	 * ***************************************/
	// Q3D: CACM
	public static void printMapPartDCACM(int numDocs) {
		init();

		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);

		// CACM
		int docSize = Math.min(numDocs, totalNumDocs);
		double[] cacmMap = new double[docSize];
		System.out.println("3d. Retrieving docs from CACM...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocsNoNormD(i, numDocs);
			cacmMap[i] = mapPrecision(answerMap.get(i), result);
		}
		System.out.println("Calculating MAP for CACM tfidf ltc.ltc...");
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			sum += cacmMap[i];
		}
		System.out.println("Question 3d CACM MAP: " + sum/queryMap.size());
	}

	// Q3D: MED
	public static void printMapPartDMed(int numDocs) {
		// MED

		initMed();
		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);

		int docSize = Math.min(numDocs, totalNumDocs);
		double[] medMap = new double[docSize];
		System.out.println("3d. Retrieving docs from MED...");
		for (int i = 1; i <= queryMap.size(); i++) {
			Pair[] result = relevantDocsNoNormD(i, numDocs);
			medMap[i] = mapPrecision(medAnswerMap.get(i), result);
		}
		System.out.println("Calculating MAP for MED tfidf ltc.ltc...");
		double medsum = 0.0;
		for (int i = 0; i < medMap.length; i++) {
			medsum += medMap[i];
		}
		System.out.println("Question 3d MED MAP: " + medsum/queryMap.size());
	}

	// Calculates the MAP for a single query.
	public static double mapPrecision(HashSet<String> answers, 
			Pair[] results) {

		double precision			= 0.0;
		int matchedDocumentCount 	= 0;
		int totalDocumentSoFar 		= 0;
		double sumPrecisionVal		= 0.0;
		List<Double> precisionList 	= new ArrayList<Double>();

		// loop through each retrieved results and check if the doc is relevant.
		// produces a precision list.
		for (Pair result : results) {
			totalDocumentSoFar++;
			if (answers.contains(result.getId())) {
				matchedDocumentCount++;
				precisionList.add((double)matchedDocumentCount / totalDocumentSoFar);
			}
		}

		for (double val : precisionList) {
			sumPrecisionVal += val;
		}

		precision = sumPrecisionVal / ((double) answers.size());
		return precision;
	}

	/**
	 * Returns the top 100 most relevant documents to this query.
	 * @param queryId query id.
	 * @return Pair[] with array that contains 100 such (doc, relevance score) pairs
	 */
	public static Pair[] relevantDocs(int queryId, int numResults) {
		Pair[] pairArray = new Pair[docTermFreqMap.size()];

		int i = 0;
		for (String docId : docTermFreqMap.keySet()) {
			Pair newPair = new Pair();
			newPair.setId(docId);
			newPair.setVal(relevance(docId, queryId));
			pairArray[i] = newPair;
			i++;
		}
		Arrays.sort(pairArray);

		Pair[] result = new Pair[numResults];
		for (int j = 0; j < result.length; j++) {
			result[j] = pairArray[j];
		}
		return result;
	}

	// Returns the relevance of document and query.
	public static double relevance(String docId, int queryId) {
		double[] dVector 	= getRelevance(docId);
		double[] qVector 	= getRelevance(queryId);
		double innerProd 	= UtilsMini.dotProduct(dVector, qVector);

		double mag 			= 0.0;
		for (int i = 0; i < dVector.length; i++) {
			mag += dVector[i] * dVector[i];
		}
		for (int i = 0; i < qVector.length; i++) {
			mag += qVector[i] * qVector[i];
		}

		mag = Math.sqrt(mag);
		if (mag == 0) return 0;
		return innerProd / mag;
	}

	/**
	 * Returns the top 100 most relevant documents to this query.
	 * @param queryId query id.
	 * @return Pair[] with array that contains 100 such (doc, relevance score) pairs
	 */
	public static Pair[] relevantDocsNoNorm(int queryId, int numResults) {
		Pair[] pairArray = new Pair[docTermFreqMap.size()];

		int i = 0;
		for (String docId : docTermFreqMap.keySet()) {
			Pair newPair = new Pair();
			newPair.setId(docId);
			newPair.setVal(relevanceNoNorm(docId, queryId));
			pairArray[i] = newPair;
			i++;
		}
		Arrays.sort(pairArray);

		Pair[] result = new Pair[numResults];
		for (int j = 0; j < result.length; j++) {
			result[j] = pairArray[j];
		}
		return result;
	}

	// Returns the relevance of document and query.
	public static double relevanceNoNorm(String docId, int queryId) {
		double[] dVector 	= getRelevance(docId);
		double[] qVector 	= getRelevance(queryId);
		double innerProd 	= UtilsMini.dotProduct(dVector, qVector);

		return innerProd;
	}

	public static Pair[] relevantDocsNoNormC(int queryId, int numResults) {
		Pair[] pairArray = new Pair[docTermFreqMap.size()];

		int i = 0;
		for (String docId : docTermFreqMap.keySet()) {
			Pair newPair = new Pair();
			newPair.setId(docId);
			newPair.setVal(relevanceNoNormC(docId, queryId));
			pairArray[i] = newPair;
			i++;
		}
		Arrays.sort(pairArray);

		Pair[] result = new Pair[numResults];
		for (int j = 0; j < result.length; j++) {
			result[j] = pairArray[j];
		}
		return result;
	}
	
	// Returns the relevance of document and query.
	public static double relevanceNoNormC(String docId, int queryId) {
		double[] dVector 	= getRelevanceC(docId);
		double[] qVector 	= getRelevanceC(queryId);
		double innerProd 	= UtilsMini.dotProduct(dVector, qVector);

		return innerProd;
	}
	
	public static Pair[] relevantDocsNoNormD(int queryId, int numResults) {
		Pair[] pairArray = new Pair[docTermFreqMap.size()];

		int i = 0;
		for (String docId : docTermFreqMap.keySet()) {
			Pair newPair = new Pair();
			newPair.setId(docId);
			newPair.setVal(relevanceNoNormD(docId, queryId));
			pairArray[i] = newPair;
			i++;
		}
		Arrays.sort(pairArray);

		Pair[] result = new Pair[numResults];
		for (int j = 0; j < numResults; j++) {
			result[j] = pairArray[j];
		}
		return result;
	}
	
	public static double relevanceNoNormD(String docId, int queryId) {
		double[] dVector 	= getRelevanceD(docId);
		double[] qVector 	= getRelevanceD(queryId);
		double innerProd 	= UtilsMini.dotProduct(dVector, qVector);

		return innerProd;
	}

	/**
	 * Returns a tf*idf vector for this document using atc.atc.
	 * 
	 * @param docId document id.
	 * @return double[] of tf*idf scores for each term.
	 */
	public static double[] getRelevance(String docId) {

		double[] relevanceArray = new double[termSet.size()];
		HashMap<String, Integer> thisMap = docTermFreqMap.get(docId);
		for (int i = 0; i < termsArray.length; i++) {
			String term = termsArray[i];
			if (thisMap.containsKey(term)) {

				int tf 		= thisMap.get(term);
				if (!idfMap.containsKey(term)) {
					relevanceArray[i] = 0.0;
				} else {
					int idf 	= idfMap.get(term).size();
					int maxTf 	= maxTfMap.get(docId);

					double tfScore 		= 0.5 + ((double) tf / maxTf);
					double idfScore 	= Math.log10((double) totalNumDocs / idf);

					relevanceArray[i] 	= tfScore * idfScore;
				}
			}
		}
		return relevanceArray;
	}

	// Q3, part c's variation of tf*idf. tf stays the same, but no idf.
	public static double[] getRelevanceC(String docId) {

		double[] relevanceArray = new double[termSet.size()];
		HashMap<String, Integer> thisMap = docTermFreqMap.get(docId);
		for (int i = 0; i < termsArray.length; i++) {
			String term = termsArray[i];
			if (thisMap.containsKey(term)) {

				int tf 				= thisMap.get(term);
				int maxTf 			= maxTfMap.get(docId);
				double tfScore 		= 0.5 + ((double) tf / maxTf);
				relevanceArray[i] 	= tfScore;

			}
		}
		return relevanceArray;
	}

	public static double[] getRelevanceD(String docId) {

		double[] relevanceArray = new double[termSet.size()];
		HashMap<String, Integer> thisMap = docTermFreqMap.get(docId);
		for (int i = 0; i < termsArray.length; i++) {
			String term = termsArray[i];
			if (thisMap.containsKey(term)) {

				int tf 		= thisMap.get(term);
				if (!idfMap.containsKey(term)) {
					relevanceArray[i] = 0.0;
				} else {
					int idf 	= idfMap.get(term).size();
					int maxTf 	= maxTfMap.get(docId);

					double tfScore 		= 0.5 + ((double) tf / maxTf);
					double idfScore 	= Math.max(0.0, Math.log10((double) (totalNumDocs - idf) / idf));

					relevanceArray[i] 	= tfScore * idfScore;
				}
			}
		}
		return relevanceArray;
	}

	/**
	 * Returns a tf*idf vector for this query using atc.atc.
	 * 
	 * @param queryId id of this query
	 * @return double[] of tf*idf scores for each term
	 */
	public static double[] getRelevance(int queryId) {

		double[] relevanceArray = new double[termSet.size()];
		TreeMap<String, Integer> thisMap = queryMap.get(queryId);
		for (int i = 0; i < termsArray.length; i++) {
			String term = termsArray[i];
			if (thisMap.containsKey(term)) {

				int tf 		= thisMap.get(term);
				if (!idfMap.containsKey(term)) {
					relevanceArray[i] = 0;
				} else {
					int idf 	= idfMap.get(term).size();
					int maxTf 	= maxQueryTfMap.get(queryId);

					double tfScore 		= 0.5 + ((double) tf / maxTf);
					double idfScore 	= Math.log10((double) totalNumDocs / idf);

					relevanceArray[i] 	= tfScore * idfScore;
				}
			}
		}
		return relevanceArray;
	}

	// Q3, part c's variation of tf*idf. tf is 1 or 0. idf is max(0, log(N - n)/n)
	public static double[] getRelevanceC(int queryId) {

		double[] relevanceArray = new double[termSet.size()];
		TreeMap<String, Integer> thisMap = queryMap.get(queryId);
		for (int i = 0; i < termsArray.length; i++) {
			String term = termsArray[i];
			if (thisMap.containsKey(term)) {

				if (!idfMap.containsKey(term)) {
					relevanceArray[i] = 0;
				} else {
					int idf 	= idfMap.get(term).size();

					double idfScore 	= Math.max(0.0, Math.log10((double) (totalNumDocs - idf) / idf));
					relevanceArray[i] 	= idfScore;
				}
			}
		}
		return relevanceArray;
	}
	
	public static double[] getRelevanceD(int queryId) {

		double[] relevanceArray = new double[termSet.size()];
		TreeMap<String, Integer> thisMap = queryMap.get(queryId);
		for (int i = 0; i < termsArray.length; i++) {
			String term = termsArray[i];
			if (thisMap.containsKey(term)) {

				int tf 		= thisMap.get(term);
				if (!idfMap.containsKey(term)) {
					relevanceArray[i] = 0;
				} else {
					int idf 	= idfMap.get(term).size();
					int maxTf 	= maxQueryTfMap.get(queryId);

					double tfScore 		= 0.5 + ((double) tf / maxTf);
					double idfScore 	= Math.max(0.0, Math.log10((double) (totalNumDocs - idf) / idf));

					relevanceArray[i] 	= tfScore * idfScore;
				}
			}
		}
		return relevanceArray;
	}


	// Stores the maxTf into maxTfMap for each document.
	public static void maxTfMap(HashMap<String, HashMap<String, Integer>> map) {
		for (String docId : map.keySet()) {
			int maxTf = 0;
			HashMap<String, Integer> termFreqMap = map.get(docId); 
			for (String term : termFreqMap.keySet()) {
				if (termFreqMap.get(term) > maxTf) {
					maxTf = termFreqMap.get(term);
				}
			}
			maxTfMap.put(docId, maxTf);
		}
	}

	// Stores the maxTf into maxTfMap for each query.
	public static void maxQueryTfMap(HashMap<Integer, TreeMap<String, Integer>> map) {
		for (Integer docId : map.keySet()) {
			int maxTf = 0;
			TreeMap<String, Integer> termFreqMap = map.get(docId); 
			for (String term : termFreqMap.keySet()) {
				if (termFreqMap.get(term) > maxTf) {
					maxTf = termFreqMap.get(term);
				}
			}
			maxQueryTfMap.put(docId, maxTf);
		}
	}

	// Sort the termsArray into lexicographically ordered.
	public static void initializeTermsArray() {
		int i = 0;
		for (String s : termSet) {
			termsArray[i] = s;
			i++;
		}
		Arrays.sort(termsArray);
	}

	/**
	 * Takes in a file path to the indexed files (ie: cacm_index.txt or med_index.txt)
	 * and returns a map of doc id to its token frequencies.
	 * 
	 * @param filePath file path to the indexed files.
	 * @return Map of doc id to its token frequencies.
	 */
	public static HashMap<String, HashMap<String, Integer>> getTermFrequency(String filePath) {

		HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String currentLine;

			while ((currentLine = br.readLine()) != null) {

				int firstColonPosition 		= currentLine.indexOf(":");
				int firstBracketPosition 	= currentLine.indexOf("{");
				int lastBracketPosition 	= currentLine.lastIndexOf("}");

				String documentId 	= currentLine.substring(0, firstColonPosition);
				String wordsFreq 	= currentLine.substring(firstBracketPosition + 1, lastBracketPosition);

				map.put(documentId, termFreqMap(wordsFreq));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return map;
	}

	/**
	 * Given a String "word1=freq1, word2=freq2, ..., wordn=freqn", return
	 * a hashmap that contains word i as key and freq i as value.
	 * @param input String to be parsed.
	 * @return Hashmap of words as keys and frequency as values.
	 */
	private static HashMap<String, Integer> termFreqMap(String input) {

		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String[] wordFreqArray = input.split(", ");

		for (int i = 0; i < wordFreqArray.length; i++) {
			String wordFreq = wordFreqArray[i];
			String[] single = wordFreq.split("=");
			String term 	= single[0];
			int freq		= Integer.parseInt(single[1]);
			termSet.add(term); // adding to hashset for later tf*idf calculation.
			map.put(term, freq);
		}

		return map;
	}

	// Returns an inverse document frequency collection.
	public static HashMap<String, HashSet<String>> getInverseDocFreq(
			HashMap<String, HashMap<String, Integer>> inputMap) {

		HashMap<String, HashSet<String>> map = 
				new HashMap<String, HashSet<String>>();

		for (String docId : inputMap.keySet()) {
			for (String termId : inputMap.get(docId).keySet()) {
				if (map.containsKey(termId)) {
					map.get(termId).add(docId);
				} else {
					HashSet<String> docSet = new HashSet<String>();
					docSet.add(docId);
					map.put(termId, docSet);
				}
			}
		}
		return map;
	}

	/**
	 * Returns a map of integer to queries in *.query files.
	 * @param filename
	 * @return
	 */
	public static HashMap<Integer, TreeMap<String, Integer>> loadTokenizedQueries(String filename) {
		CharArraySet stopwords = EvaluateQueriesMini.createStopwordSet(Constants.STOP_WORDS);

		HashMap<Integer, TreeMap<String, Integer>> map 
		= new HashMap<Integer, TreeMap<String, Integer>>();

		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(new File(filename)));

			String line;
			while ((line = in.readLine()) != null) {
				int pos 			= line.indexOf(',');
				int docId 			= Integer.parseInt(line.substring(0, pos));
				String terms 		= line.substring(pos + 1);
				StringReader sr		= new StringReader(terms); // wrap your String
				BufferedReader br 	= new BufferedReader(sr); // wrap your StringReader

				TokenStream tokenStr = IndexFilesMini.createTokenizer(br, stopwords);
				TreeMap<String, Integer> treemap = IndexFilesMini.createDictionary(tokenStr);
				map.put(docId, treemap);

				// adding query tokens to the entire set of tokens.
				for (String s : treemap.keySet()) {
					termSet.add(s);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		} catch(IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public static HashMap<String, Integer> calculateDocLengths (HashMap<String, HashMap<String, Integer>> index) {
		HashMap<String, Integer> docLength = new HashMap<String, Integer>();
		for (String key : index.keySet()) {
			HashMap<String, Integer> currDocIndex = index.get(key);
			int currDocLength = 0;
			for (String currKey : currDocIndex.keySet()) {
				int currFreq = currDocIndex.get(currKey);
				currDocLength += currFreq;
			}
			docLength.put(key, currDocLength);
		}
		return docLength;
	}

	public static double calculateAvgDocLength(HashMap<String, Integer> docLengths,
			double docCount) {
		int totalDocLength = 0;
		for (String key : docLengths.keySet()) {
			int currLength = docLengths.get(key);
			totalDocLength += currLength;
		}
		return (((double) totalDocLength) / docCount);
	}

	public static HashMap<String, Integer> calculateNumberOfHitsPerTerm(HashMap<String, HashMap<String,Integer>> index, 
			TreeMap<String, Integer> query) {
		HashMap<String, Integer> numHits = new HashMap<String, Integer>();
		for (String key : query.keySet()) {
			int hits = 0;
			for (String doc : index.keySet()) {
				hits += index.get(doc).containsKey(key) ? 1 : 0;
			}
			numHits.put(key, hits);
		}
		return numHits;
	}


	public static double calculateBM25PerDoc(HashMap<String, Integer> document, 
			TreeMap<String, Integer> query, HashMap<String, Integer> numHits,
			double docLength, double docCount, double avgDocLength) {
		double score = 0;
		for (String key : query.keySet()) {
			double numHit;
			double docFreq;
			double queryFreq;
			try {
				numHit = (double) numHits.get(key);
			} catch (NullPointerException e) {
				numHit = 0.;
			}
			try {
				docFreq = (double) document.get(key);
			} catch (NullPointerException e) {
				docFreq = 0.;
			}
			try {
				queryFreq = (double) query.get(key);
			} catch (NullPointerException e) {
				queryFreq = 0.;
			}
			double subscore = Math.log((numHit + 0.5) 
					/ (docCount - numHit + 0.5));
			double K = K1 * ((1-B) + B * (docLength / docCount));
			subscore = subscore * (((K1 + 1) * docFreq) / (K + docFreq));
			subscore = subscore * (((K2 + 1) * queryFreq) / (K + queryFreq));
			score += subscore;
		}

		return score;
	}

	public static HashSet<Pair> calculateBM25perQuery 
		(HashMap<String, HashMap<String, Integer>> index, TreeMap<String, Integer> query) {
		HashSet<Pair> queryScores = new HashSet<Pair>();
		HashMap<String, Integer> numHits = calculateNumberOfHitsPerTerm(index, query);
		HashMap<String, Integer> docLengths = calculateDocLengths(index);
		double docCount = (double) docLengths.size();
		double avgDocLength = (double) calculateAvgDocLength(docLengths, docCount);
		for (String docKey : index.keySet()) {	
			double docLength = (double) docLengths.get(docKey);
			HashMap<String, Integer> document = index.get(docKey);
			double score = calculateBM25PerDoc(document, query, numHits, 
					docLength, docCount, avgDocLength);
			Pair queryScoreOnDoc = new Pair();
			queryScoreOnDoc.setId(docKey);
			queryScoreOnDoc.setVal(score);
			queryScores.add(queryScoreOnDoc);
		}
		
		return queryScores;
	}
	
	public static HashMap<Integer, TreeSet<Pair>> calculateBM25 
		(HashMap<String, HashMap<String, Integer>> index, 
				HashMap<Integer, TreeMap<String, Integer>> queries) {
		HashMap<Integer, TreeSet<Pair>> queryScores = 
				new HashMap<Integer, TreeSet<Pair>>();
		for (Integer queryKey : queries.keySet()) {
			TreeMap<String, Integer> query = queries.get(queryKey);
			HashSet<Pair> queryScoreSetUnsorted = 
					calculateBM25perQuery(index, query);
			TreeSet<Pair> queryScoreSet = new TreeSet<Pair>(queryScoreSetUnsorted);
			queryScores.put(queryKey, queryScoreSet);
		}
		
		return queryScores;
	}

	public static ArrayList<String> extractDocList 
		(HashMap<Integer, TreeSet<Pair>> rankings, int queryKey) {
		ArrayList<String> docList = new ArrayList<String>();
		TreeSet<Pair> rankedDocs = (TreeSet<Pair>) rankings.get(queryKey).descendingSet();
		for (Pair docRank : rankedDocs) {
			docList.add(docRank.getId());
		}
		return docList;
	}

}
