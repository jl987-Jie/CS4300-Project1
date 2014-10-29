package mini;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class SimilarityMini {

	static final String DATA_DIR = "data/";
	static final String CACM_IDX = "cacm_index.txt";
	static final String MEDL_IDX = "med_index.txt";
	
	// Test Main Function.
	public static void main(String[] args) {
		HashMap<String, HashMap<String, Integer>> map = getTermFrequency(DATA_DIR + CACM_IDX);
		System.out.println(map.size());
		
	}
	
	// Returns the term frequency collection.
	public static HashMap<String, HashMap<String, Integer>> getTermFrequency(String filePath) {
		
		HashMap<String, HashMap<String, Integer>> map = 
				new HashMap<String, HashMap<String, Integer>>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
		{
 
			String currentLine;
 
			while ((currentLine = br.readLine()) != null) {
				int firstColonPosition = currentLine.indexOf(":");
				int firstBracketPosition = currentLine.indexOf("{");
				int lastBracketPosition = currentLine.lastIndexOf("}");
				
				String documentId = currentLine.substring(0, firstColonPosition);
				String wordsFreq = currentLine.substring(firstBracketPosition + 1, lastBracketPosition);
				map.put(documentId, getWordFreqMap(wordsFreq));
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
	private static HashMap<String, Integer> getWordFreqMap(String input) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String[] wordFreqArray = input.split(", ");
		for (int i = 0; i < wordFreqArray.length; i++) {
			String wordFreq = wordFreqArray[i];
			String[] single = wordFreq.split("=");
			map.put(single[0], Integer.parseInt(single[1]));
		}
		return map;
	}
	
	// Returns an inverse document frequency collection.
	public static HashMap<String, HashMap<String, Integer>> getInverseDocFreq(
			HashMap<String, HashMap<String, Integer>> inputMap) {

		HashMap<String, HashMap<String, Integer>> map = 
				new HashMap<String, HashMap<String, Integer>>();
		
		return map;
	}
}
