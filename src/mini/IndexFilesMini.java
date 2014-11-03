package mini;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
// import org.apache.lucene.util.Version;

/** Index all text files under a directory, the directory is at data/txt/
 */

public class IndexFilesMini {

	private IndexFilesMini() {}

	public static void buildIndex(String indexPath, String indexName, String docsPath, 
			CharArraySet stopwords) {
	
		// check whether docsPath is valid
		if (docsPath == null || docsPath.isEmpty()) {
			System.err.println("Document directory cannot be null.");
			System.exit(1);
		}
		
		// Check whether the directory is readable
		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out.println("Document directory '" + 
					docDir.getAbsolutePath() + 
					"' does not exist or is not readable, please check the path");
			System.exit(1);
		}
		
		Date start = new Date();
		String indexFile = indexPath + indexName;
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
			System.out.println("Indexing on " + indexName.split("_index.txt")[0].toUpperCase() 
					+ " collection...");
			PrintWriter reset = new PrintWriter(indexFile);
			reset.close();
			indexDocs(docDir, stopwords, indexFile);
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void indexDocs(File file, CharArraySet stopwords, String indexFile) {
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(new File(file, files[i]), stopwords, indexFile);
					}
				}
			} else {
				FileInputStream fis = null;

				String filename = file.toString();
				String fileID = filename.substring(filename.lastIndexOf('/') + 1, filename.lastIndexOf('.'));
				
				try {
					fis = new FileInputStream(file);

					BufferedReader fileContents = new BufferedReader(new InputStreamReader(fis));
					TokenStream tokenizedFile = createTokenizer(fileContents, stopwords);
					TreeMap<String, Integer> fileDictionary = createDictionary(tokenizedFile);
					
					outputIndex(fileID, fileDictionary, indexFile);

					try {
						fileContents.close();
					} catch (IOException e) {
						System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
					}
				} catch (FileNotFoundException e) {
					System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
				}
				
			}
		}
	}
	
	public static TokenStream createTokenizer(BufferedReader reader, CharArraySet stopwords) {
		StandardTokenizer tokenizer = new StandardTokenizer(reader);
		tokenizer.setMaxTokenLength(10000);
		TokenStream tok = new StandardFilter(tokenizer);
		tok = new LowerCaseFilter(tok);
		tok = new StopFilter(tok, stopwords);
		tok = new PorterStemFilter(tok);
		return tok;
	}

	public static TreeMap<String, Integer> createDictionary(TokenStream tokenizedFile) {
		HashMap<String, Integer> tokenizedFileDict = new HashMap<String, Integer>();
		CharTermAttribute tokenText = tokenizedFile.addAttribute(CharTermAttribute.class);
		try {
			tokenizedFile.reset();
			while (tokenizedFile.incrementToken()) {
				String currentToken = tokenText.toString();
				Integer currTokenCount = tokenizedFileDict.get(currentToken);
				if (currTokenCount == null) {
					tokenizedFileDict.put(currentToken, 1);
				} else {
					tokenizedFileDict.put(currentToken, currTokenCount.intValue() + 1);
				}
			}
			tokenizedFile.end();
			tokenizedFile.close();
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}

		TreeMap<String, Integer> sortedTokenizedFileDict = new TreeMap<String, Integer>(tokenizedFileDict);
		return sortedTokenizedFileDict;
	}

	public static void outputIndex(String doc, TreeMap<String, Integer> dict, String outputFile) {
		try {
			PrintWriter outWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
			String currentLine = createLine(doc, dict);
			outWriter.println(currentLine);
			outWriter.close();
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}
	
	public static String createLine(String doc, TreeMap<String, Integer> dict) {
		StringBuilder currentLine = new StringBuilder();
		currentLine.append(doc + ": ");
		
		currentLine.append(dict.toString());
		return currentLine.toString();
	}
}

