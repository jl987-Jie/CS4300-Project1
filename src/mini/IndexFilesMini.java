package mini;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

	public static void buildIndex(String indexPath, String docsPath, 
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
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void indexDocs(File file, CharArraySet stopwords) {
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(new File(file, files[i]), stopwords);
					}
				}
			} else {
				FileInputStream fis = null;
				
				try {
					fis = new FileInputStream(file);

					BufferedReader fileContents = new BufferedReader(new InputStreamReader(fis));
					TokenStream tokenizedFile = createTokenizer(fileContents, stopwords);

					try {
						fileContents.close();
					} catch (IOException e) {
						System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
					}
					// Probably do stuff here
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

	public static HashMap<String, Integer> createDictionary(TokenStream tokenizedFile) {
		HashMap<String, Integer> tokenizedFileDict = new HashMap<String, Integer>();
		CharTermAttribute tokenText = tokenizedFile.addAttribute(CharTermAttribute.class);
		try {
			tokenizedFile.reset();
			while (tokenizedFile.incrementToken()) {
				String currentToken = tokenText.toString();
			}
			tokenizedFile.end();
			tokenizedFile.close();
		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}

		return tokenizedFileDict;
	}

	// read file.
	public static void readFile(String fileName) {
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(fileName));
 
			while ((sCurrentLine = br.readLine()) != null) {
//				Analyzer analyzer = new StandardAnalyzer(...);
//				List<String> tokenized_string = analyzer.analyze(sCurrentLine);
				System.out.println(sCurrentLine);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// analyze file
	
	// 
}

