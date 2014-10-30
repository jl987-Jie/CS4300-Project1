package mini;

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
import java.util.TreeMap;
import java.util.TreeSet;

// import lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

public class EvaluateQueriesMini {
	public static void main(String[] args) {
		
		String cacmDocsDir = "data/cacm"; // directory containing CACM documents
		String medDocsDir = "data/med"; // directory containing MED documents

		String cacmIndexDir = "data/index/cacm"; // the directory where index is written into
		String medIndexDir = "data/index/med"; // the directory where index is written into

		String cacmQueryFile = "data/cacm_processed.query";    // CACM query file
		String cacmAnswerFile = "data/cacm_processed.rel";   // CACM relevance judgements file

		String medQueryFile = "data/med_processed.query";    // MED query file
		String medAnswerFile = "data/med_processed.rel";   // MED relevance judgements file
		
		String stopwordFile = "data/stopwords/stopwords_indri.txt"; // Indri stopword file 
		
		String indexPath = "data/";
		String medIndexName = "med_index.txt";
		String cacmIndexName = "cacm_index.txt";
		
		HashMap<String, HashMap<String, Integer>> cacmDocTermIndex = null;
		HashMap<Integer, TreeMap<String, Integer>> cacmQueryTermIndex = null;
		HashMap<String, HashMap<String, Integer>> medDocTermIndex = null;
		HashMap<Integer, TreeMap<String, Integer>> medQueryTermIndex = null;
		

		CharArraySet stopwords = createStopwordSet(stopwordFile);

		int argsPosition = 1;
		if (args != null && args[0].equals("run")) {
			String collection = null;
			
			while (argsPosition <= (args.length -1)) {
				try {
					switch (args[argsPosition]) {
					case "-d":
						collection = args[argsPosition + 1];
						switch (collection) {
						case "cacm":
							break;
						case "med":
							break;
						case "all":
							break;
						default:
							System.out.println("Invalid document argument supplied");
							break;
						}
						argsPosition += 2;
						break;
					case "-i":
						switch (collection) {
						case "cacm":
							IndexFilesMini.buildIndex(indexPath, cacmIndexName, 
									cacmDocsDir, stopwords);
							break;
						case "med":
							IndexFilesMini.buildIndex(indexPath, medIndexName, 
									medDocsDir, stopwords);
							break;
						case "all":
							IndexFilesMini.buildIndex(indexPath, cacmIndexName, 
									cacmDocsDir, stopwords);
							IndexFilesMini.buildIndex(indexPath, medIndexName, 
									medDocsDir, stopwords);
							break;
						default:
							System.out.println("Invalid argument supplied to indexer");
							break;
						}
						argsPosition += 2;
						break;
					case "-b":
						String bmNumberStr = args[argsPosition + 1];
						int bmNumberVal = 0;
						double bmCacmResult = 0.0;
						double bmMedResult = 0.0;
						switch (bmNumberStr) {
						case "total":
							bmNumberVal = Integer.MAX_VALUE;
							break;
						default:
							try {
								bmNumberVal = Integer.parseInt(bmNumberStr);
							} catch (NumberFormatException e){
								System.out.println("Invalid argument supplied for number of documents");
								break;
							}
							break;
						}
						if (collection.equals("cacm") || collection.equals("all")) {
							if (cacmDocTermIndex == null) {
								cacmDocTermIndex = SimilarityMini.getTermFrequency(
										indexPath + cacmIndexName);
							}
							if (cacmQueryTermIndex == null) {
								cacmQueryTermIndex = SimilarityMini.
										loadTokenizedQueries(cacmQueryFile);
							}
							
							bmCacmResult = evaluateMap(indexPath, cacmDocsDir, cacmQueryFile, 
									cacmAnswerFile, bmNumberVal, cacmIndexName, stopwords,
									"bm25", cacmDocTermIndex, cacmQueryTermIndex);
							System.out.println("CACM BM25 MAP is " + bmCacmResult
									+ "for " + bmNumberVal + " documents");
						} else if (collection.equals("med") || collection.equals("all")) {
							if (medDocTermIndex == null) {
								medDocTermIndex = SimilarityMini.getTermFrequency(
										indexPath + medIndexName);
							}
							if (medQueryTermIndex == null) {
								medQueryTermIndex = SimilarityMini.
										loadTokenizedQueries(medQueryFile);
							}
							bmMedResult = evaluateMap(indexPath, medDocsDir, medQueryFile, 
									medAnswerFile, bmNumberVal, medIndexName, stopwords,
									"bm25", medDocTermIndex, medQueryTermIndex);
							System.out.println("MED BM25 MAP is " + bmMedResult
									+ "for " + bmNumberVal + " documents");
						} else {
							System.out.println("Invalid arguments supplied for bm25");
							System.out.println(collection);
							System.out.println(bmNumberVal);
						}
						argsPosition += 2;
						break;
					case "-t":
						String numberStr = args[argsPosition + 2];
						int numberVal;
						switch (numberStr) {
						case "total":
							break;
						default:
							try {
								numberVal = Integer.parseInt(numberStr);
							} catch (NumberFormatException e){
								System.out.println("Invalid argument supplied for tfidf");
								break;
							}
							break;
						}
						String tfidfType = args[argsPosition + 1];
						switch (tfidfType) {
						case "atcatc":
							break;
						case "atnatn":
							break;
						case "annbpn":
							break;
						case "ourown": //TODO- CHANGE NAME OF THIS IN RUNCONFIG
							break;
						case "all":
							break;
						}
						argsPosition += 3;
						break;
						
					default:
						System.out.println("Invalid arguments supplied to main");
						break;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Invalid number of arguments supplied");
				}
			}
			System.out.println("Done running.");
		} else {
			System.out.println("Invalid argument configuration supplied");
		}
	}

	/**
	 * Returns a map of integer to queries in *.query files.
	 * @param filename
	 * @return
	 */
	public static Map<Integer, String> loadQueries(String filename) {
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
	
	/**
	 * Implement the MAP evaluation measure for a given set of results
	 * from a similarity measure
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
			String indexName, CharArraySet stopwords,
			String similarityMeasure, 
			HashMap<String, HashMap<String, Integer>> docTermIndex,
			HashMap<Integer, TreeMap<String, Integer>> queryTermIndex) {


		// load queries and answer
		Map<Integer, String> queries = loadQueries(queryFile);
		Map<Integer, HashSet<String>> queryAnswers = loadAnswers(answerFile);
		
		ArrayList<String> results = null;
		HashMap<Integer, TreeSet<Pair>> bm25results = null;
		// load results
		if (similarityMeasure == "bm25") {
			bm25results = SimilarityMini.calculateBM25(docTermIndex, queryTermIndex);	
		}
		
		

		// Search and evaluate
		double sum = 0;
		for (Integer i : queries.keySet()) {
			double numRelDocs = (double) queryAnswers.get(i).size();
			if (similarityMeasure == "bm25") {
				results = SimilarityMini.extractDocList(bm25results, i);
				results = new ArrayList<String>(results.subList(0, 
						Math.min(numResults, docTermIndex.size() -1)));
			}
			sum += mapPrecision(queryAnswers.get(i), results, numRelDocs);
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
	public static double mapPrecision(HashSet<String> answers, 
			ArrayList<String> results, double numRelDocs) {

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

		precision = sumPrecisionVal / numRelDocs;
		return precision;
	}
	
	/**
	 * ****************************
	 *  Question 2 Helper Method.
	 * ****************************
	 * Create a CharArraySet of stopwords from a text file
	 * This CharArraySet can then be used for the analyzer and indexer
	 * @param filename 
	 * @param results List of retrieved documents by a query
	 * @return CharArraySet containing stopwords
	 */
	public static CharArraySet createStopwordSet(String stopFilename) {
		CharArraySet stopwordSet = new CharArraySet(0, true);
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					new File(stopFilename)));
			
			String line;
			try {
				while ((line = in.readLine()) != null) {
					stopwordSet.add(line);
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
		} catch (FileNotFoundException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
		
		return stopwordSet;
	}

}

