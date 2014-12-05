package mini;

import java.text.NumberFormat;

public class Question2PartF {
	
	/**
	 * Analysis part F of question 2.
	 * @param args
	 */
	public static void main(String[] args) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);            
		nf.setGroupingUsed(false);
		
		SimilarityMini.initMed();
		Pair[] result = SimilarityMini.relevantDocs(9, 30, "none", null);
		for (int i = 0; i < result.length; i++) {
			System.out.println(result[i].getId() + "                      " + nf.format(result[i].getVal()));
		}
	}
}
