package mini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;

public class SimilarityMini {

	static HashSet<String> termSet = new HashSet<String>();
	static String[] termsArray;
	static HashMap<String, HashMap<String, Integer>> docTermFreqMap = new HashMap<String, HashMap<String, Integer>>();
	static HashMap<String, Integer> invDocFreqMap = new HashMap<String, Integer>();
	static HashMap<String, Integer> maxTfMap = new HashMap<String, Integer>();
	static int totalNumDocs = docTermFreqMap.size();
	static HashMap<Integer, HashMap<String, Double>> queryIdRelevanceMap = 
			new HashMap<Integer, HashMap<String, Double>>();

	// Question 3.
	public static void main(String[] args) {

		HashMap<String, HashMap<String, Integer>> map 
			= getTermFrequency(Constants.DATA_DIR + Constants.CACM_IDX); // doc id -> term -> frequency.
		HashMap<Integer, TreeMap<String, Integer>> queryMap 
			= loadTokenizedQueries(Constants.CACM_QUERY); // query id -> term -> frequency.

		// need idf.
		HashMap<String, HashSet<String>> idfMap = getInverseDocFreq(map);

		// initialize termsArray
		termsArray = new String[termSet.size()];
		initializeTermsArray();
		
		// initialize maxTfMap
		maxTfMap(map);
		
		// for each query, we have to calculate the similarity between 
		// the query and the document (which is determined by the tf-idf score)

		for (Integer queryId : queryMap.keySet()) {
			double[] result = getRelevance(queryId, idfMap);
			
			for (int i = 0; i < result.length; i++) {
				if (result[i] != 0) {
					System.out.println(result[i]);
				}
			}
			break;
		}

	}

	// Stores the maxTf into maxTfMap.
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

	public static double[] getRelevance(Integer queryId, HashMap<String, HashSet<String>> idfMap) {

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

}
