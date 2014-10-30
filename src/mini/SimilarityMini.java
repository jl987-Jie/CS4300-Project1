package mini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;

public class SimilarityMini {

	static String[] termsArray;
	
	static HashSet<String> termSet 									= new HashSet<String>();
	static HashMap<String, HashMap<String, Integer>> docTermFreqMap = new HashMap<String, HashMap<String, Integer>>();
	static HashMap<String, Integer> invDocFreqMap 					= new HashMap<String, Integer>();
	static HashMap<String, Integer> maxTfMap 						= new HashMap<String, Integer>();
	static HashMap<Integer, TreeMap<String, Integer>> queryMap 		= new HashMap<Integer, TreeMap<String, Integer>>();
	static HashMap<String, HashSet<String>> idfMap 					= new HashMap<String, HashSet<String>>();
	static int totalNumDocs 										= docTermFreqMap.size();
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
	}
	
	// Question 3.
	public static void main(String[] args) {
		// for each query, we have to calculate the similarity between 
		// the query and the document (which is determined by the tf-idf score)
		init();
		for (Integer queryId : queryMap.keySet()) {
			
			double[] result = getRelevance(queryId);
			
			for (int i = 0; i < result.length; i++) {
				if (result[i] != 0) {
					System.out.println(result[i]);
				}
			}
			break;
		}

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

	public static double[] getRelevance(Integer queryId) {

		double[] relevanceArray = new double[termSet.size()];

		for (String docId : docTermFreqMap.keySet()) {

			for (int i = 0; i < relevanceArray.length; i++) {

				HashMap<String, Integer> termFreqMap = docTermFreqMap.get(docId);

				for (String s : termFreqMap.keySet())
					if (termFreqMap.containsKey(termsArray[i])) {

						int tf 	= termFreqMap.get(termsArray[i]);
						int idf = idfMap.get(termsArray[i]).size();
						int maxTf = maxTfMap.get(docId);

						double tfScore 		= 0.5 + ((double) tf / maxTf);
						double idfScore 	= Math.log10((double) totalNumDocs / idf);
						relevanceArray[i] 	= tfScore * idfScore;
					}
			}
		}
		return relevanceArray;
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

				map.put(docId, IndexFilesMini.createDictionary(tokenStr));
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
		TreeSet<Pair> rankedDocs = rankings.get(queryKey);
		for (Pair docRank : rankedDocs) {
			docList.add(docRank.getId());
		}
		return docList;
	}

}
