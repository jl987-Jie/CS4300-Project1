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
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;

public class SimilarityMini {

	static final String DATA_DIR 	= "data/";
	static final String CACM_IDX 	= "cacm_index.txt";
	static final String MEDL_IDX 	= "med_index.txt";
	static final String CACM_QUERY 	= "data/cacm_processed.query";
	static final String STOP_WORDS = "data/stopwords/stopwords_indri.txt";
	
	static HashSet<String> termSet = new HashSet<String>();
	static String[] termsArray = new String[termSet.size()];
	static HashMap<String, HashMap<String, Integer>> docTermFreqMap = new HashMap<String, HashMap<String, Integer>>();
	static HashMap<String, Integer> invDocFreqMap = new HashMap<String, Integer>();
	static Pair[] relScore = new Pair[100000];
	
	// Test Main Function.
	public static void main(String[] args) {
		
		HashMap<String, HashMap<String, Integer>> map = getTermFrequency(DATA_DIR + CACM_IDX); // doc id -> term -> frequency.
		HashMap<Integer, HashMap<String, Integer>> queryMap = loadTokenizedQueries(CACM_QUERY); // query id -> term -> frequency.
		
		int totalNumDocs = map.size();
		int maxTf = 0;
		
		
	}

	public static int[] retrieveDocs(int queryId, int k) {
		int[] relevantResults = new int[k];
		
		return relevantResults;
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

	// Returns the term frequencies map for a given document id
	public static HashMap<String, Integer> getTermFreq(String docId) {
		return docTermFreqMap.get(docId);
	}

	// Returns an vector of tf-idf scores for a given docId.
	public static double[] getVector(
			String docId, String variant, int tf, int totalNumDocs, int numDocs, int maxTf) {

		double[] vector = new double[termSet.size()];
		HashMap<String, Integer> map = getTermFreq(docId);
		for (int i = 0; i < termsArray.length; i++) {
			if (map.containsKey(termsArray[i])) {
				// calculate tfidf.
				vector[i] = tfidf(variant, tf, totalNumDocs, numDocs, maxTf);
			}
		}
		return vector;
	}

	/**
	 * Calculate the tfidf given the following parameters.
	 * 
	 * @param variant variant of the tfidf
	 * @param tf term frequency
	 * @param totalNumDocs total number of documents in the collection
	 * @param numDocs number of documents that contain this term
	 * @param maxTf max term frequency in the collection
	 * 
	 * @return tfidf
	 */
	public static double tfidf(String variant, int tf, int totalNumDocs, int numDocs, int maxTf) {

		double tfScore 	= 0.0;
		double idfScore = 0.0;

		switch (variant) {
		case "atc.atc":
			tfScore 	= 0.5 + ((double) tf / maxTf);
			idfScore 	= Math.log10((double) totalNumDocs / numDocs);
			break;

		case "atn.atn":
			tfScore 	= 0.5 + ((double) tf / maxTf);
			idfScore 	= Math.log10((double) totalNumDocs / numDocs);
			break;

		case "ann.bpn":
			// not sure what they are asking here...
			break;

		default:
			// Our own method.
			break;
		}

		return tfScore * idfScore;
	}

	// Dot Product (inner product) between two vectors.
	public static double dotProduct(double[] v1, double[] v2) {
		double prod = 0.0;
		for (int i = 0; i < v1.length; i++) {
			prod += v1[i] * v2[i];
		}
		return prod;
	}

	// Magnitude of vector.
	public static double magnitude(double[] v1) {
		double mag = 0.0;
		for (int i = 0; i < v1.length; i++) {
			mag += v1[i] * v1[i];
		}
		return Math.sqrt(mag);
	}

	// Cosine Normalization between two vectors.
	public static double cosineNormalization(double[] v1, double[] v2) {
		double prod = dotProduct(v1, v2);
		double mag 	= magnitude(v1) * magnitude(v2);
		if (mag == 0.0)
			return 0.0;
		return prod / mag;
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
	public static HashMap<Integer, HashMap<String, Integer>> loadTokenizedQueries(String filename) {
		CharArraySet stopwords = EvaluateQueriesMini.createStopwordSet(STOP_WORDS);
		
		HashMap<Integer, HashMap<String, Integer>> map 
			= new HashMap<Integer, HashMap<String, Integer>>();

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
