import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

// import lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

public class EvaluateQueries {
	public static void main(String[] args) {
		String cacmDocsDir = "data/cacm"; // directory containing CACM documents
		String medDocsDir = "data/med"; // directory containing MED documents

		String cacmIndexDir = "data/index/cacm"; // the directory where index is written into
		String medIndexDir = "data/index/med"; // the directory where index is written into

		String cacmQueryFile = "data/cacm_processed.query";    // CACM query file
		String cacmAnswerFile = "data/cacm_processed.rel";   // CACM relevance judgements file

		String medQueryFile = "data/med_processed.query";    // MED query file
		String medAnswerFile = "data/med_processed.rel";   // MED relevance judgements file

		int cacmNumResults = 100;
		int medNumResults = 100;

		// CharArraySet stopwords = new CharArraySet(Version.LUCENE_44,0,false);
		CharArraySet stopwords = new CharArraySet(0, false);
		
		System.out.println(evaluate(cacmIndexDir, cacmDocsDir, cacmQueryFile,
				cacmAnswerFile, cacmNumResults, stopwords));

		System.out.println("\n");

		System.out.println(evaluate(medIndexDir, medDocsDir, medQueryFile,
				medAnswerFile, medNumResults, stopwords));
		
		// Evaluation using MAP:
		// Results: 0.403 and 0.599
		System.out.println(evaluateMap(cacmIndexDir, cacmDocsDir, cacmQueryFile,
				cacmAnswerFile, cacmNumResults, stopwords));

		System.out.println("\n");

		System.out.println(evaluateMap(medIndexDir, medDocsDir, medQueryFile,
				medAnswerFile, medNumResults, stopwords));
	}

	/**
	 * Returns a map of integer to queries in *.query files.
	 * @param filename
	 * @return
	 */
	private static Map<Integer, String> loadQueries(String filename) {
		HashMap<Integer, String> queryIdMap = new HashMap<Integer, String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(
					new File(filename)));
		} catch (FileNotFoundException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}

		String line;
		try {
			while ((line = in.readLine()) != null) {
				int pos = line.indexOf(',');
				queryIdMap.put(Integer.parseInt(line.substring(0, pos)), line
						.substring(pos + 1));
			}
		} catch(IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		} finally {
			try {
				in.close();
			} catch(IOException e) {
				System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
			}
		}
		return queryIdMap;
	}

	/**
	 * Loads a map of query question (integers) to a set of relevant documents.
	 * @param filename
	 * @return
	 */
	private static Map<Integer, HashSet<String>> loadAnswers(String filename) {
		HashMap<Integer, HashSet<String>> queryAnswerMap = new HashMap<Integer, HashSet<String>>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(
					new File(filename)));

			String line;
			while ((line = in.readLine()) != null) {
				String[] parts = line.split(" ");
				HashSet<String> answers = new HashSet<String>();
				for (int i = 1; i < parts.length; i++) {
					answers.add(parts[i]);
				}
				queryAnswerMap.put(Integer.parseInt(parts[0]), answers);
			}
		} catch(IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		} finally {
			try {
				in.close();
			} catch(IOException e) {
				System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
			}
		}
		return queryAnswerMap;
	}

	private static double precision(HashSet<String> answers,
			List<String> results) {
		double matches = 0;
		for (String result : results) {
			if (answers.contains(result))
				matches++;
		}

		return matches / results.size();
	}

	private static double evaluate(String indexDir, String docsDir,
			String queryFile, String answerFile, int numResults,
			CharArraySet stopwords) {

		// Build Index
		IndexFiles.buildIndex(indexDir, docsDir, stopwords);

		// load queries and answer
		Map<Integer, String> queries = loadQueries(queryFile);
		Map<Integer, HashSet<String>> queryAnswers = loadAnswers(answerFile);

		// Search and evaluate
		double sum = 0;
		for (Integer i : queries.keySet()) {
			if (i == 1) {
				List<String> results = SearchFiles.searchQuery(indexDir, queries
						.get(i), numResults, stopwords);
				System.out.println("Results: " + results);
				sum += precision(queryAnswers.get(i), results);
				System.out.printf("\nTopic %d  ", i);
				System.out.print (results);
				System.out.println();
			}

		}

		return sum / queries.size();
	}
	
	/**
	 * *************
	 *  QUESTION 1
	 * *************
	 * 
	 * Implement the MAP evaluation measure within the Java code used for 
	 * Homework 2. Run that setup (stopwords turned off and no stemming) 
	 * with the default Lucene similarity, retrieving 100 documents per query,
	 * evaluated with the MAP measure. Do this for both the CACM and
	 * Medlars collections.
	 * 
	 * @param indexDir
	 * @param docsDir
	 * @param queryFile
	 * @param answerFile
	 * @param numResults
	 * @param stopwords not using stopwords
	 * @return MAP for all documents. (Either CACM list of documents or
	 * Medlars collections).
	 */
	public static double evaluateMap(String indexDir, String docsDir, 
			String queryFile, String answerFile, int numResults,
			CharArraySet stopwords) {

		// Build Index
		IndexFiles.buildIndex(indexDir, docsDir, stopwords);

		// load queries and answer
		Map<Integer, String> queries = loadQueries(queryFile);
		Map<Integer, HashSet<String>> queryAnswers = loadAnswers(answerFile);

		// Search and evaluate
		double sum = 0;
		for (Integer i : queries.keySet()) {
			List<String> results = SearchFiles.searchQuery(indexDir, queries
					.get(i), numResults, stopwords);
			sum += mapPrecision(queryAnswers.get(i), results);
		}
		System.out.println(sum + ", " + queries.size());
		return sum / queries.size();
	}
	
	/**
	 * ****************************
	 *  Question 1 Helper Method.
	 * ****************************
	 * Evaluate the list of retrieved results for a single query using MAP.
	 * @param answers Set of relevant documents
	 * @param results List of retrieved documents by a query
	 * @return MAP for a single query.
	 */
	private static double mapPrecision(HashSet<String> answers, 
			List<String> results) {

		double precision			= 0.0;
		int matchedDocumentCount 	= 0;
		int totalDocumentSoFar 		= 0;
		double sumPrecisionVal		= 0.0;
		List<Double> precisionList 	= new ArrayList<Double>();

		// loop through each retrieved results and check if the doc is relevant.
		// produces a precision list.
		for (String result : results) {
			totalDocumentSoFar++;
			if (answers.contains(result)) {
				matchedDocumentCount++;
				precisionList.add((double)matchedDocumentCount / totalDocumentSoFar);
			}
		}

		for (double val : precisionList) {
			sumPrecisionVal += val;
		}

		precision = sumPrecisionVal / matchedDocumentCount;
		return precision;
	}

}
