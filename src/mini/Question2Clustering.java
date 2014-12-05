package mini;

import java.util.HashSet;
import java.util.Map;

import mini.Constants;
import mini.EvaluateQueriesMini;
import mini.Pair;
import mini.SimilarityMini;

// Print out of question 2 MAP values
public class Question2Clustering {
	
	public static int numQueriesWorse = 0;
	
	public static void main(String[] args) {
		printMedMapK20();
		System.out.println("Number of Queries got worse: " + numQueriesWorse);
	}

	// CACM MAP k = 10. (average)
	public static void printCacmMapK10() {
		SimilarityMini.init();
		ClusterMini.initTopDocs("cacm");
		for (int i = 1; i <= 52; i++) {
			System.out.println("query :" + i);
			ClusterMini.completeLinkCluster(i, 10, "cacm", "average");	
		}
		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);
		int docSize = Math.min(100, SimilarityMini.totalNumDocs);
		double[] cacmMap = new double[docSize];
		double[] projOneCacmMap = new double[docSize]; // used to calculate part (d): # queries got worse.
		for (int i = 1; i <= SimilarityMini.queryMap.size(); i++) {
			Pair[] result = ClusterMini.cacmQuery.get(i);
			Pair[] projOneResult = SimilarityMini.relevantDocs(i, 100, "none", null);
			cacmMap[i] = SimilarityMini.mapPrecision(answerMap.get(i), result, "none", null);
			projOneCacmMap[i] = SimilarityMini.mapPrecision(answerMap.get(i), projOneResult, "none", null);
		}
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			if (cacmMap[i] < projOneCacmMap[i])
				numQueriesWorse += 1;
			sum += cacmMap[i];
		}
		System.out.println("CACM MAP K = 10: " + sum/SimilarityMini.queryMap.size());
	}

	// CACM MAP k = 20. (highest)
	public static void printCacmMapK20() {
		SimilarityMini.init();
		ClusterMini.initTopDocs("cacm");
		for (int i = 1; i <= 52; i++) {
			System.out.println("query :" + i);
			ClusterMini.completeLinkCluster(i, 20, "cacm", "highest");	
		}
		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);
		int docSize = Math.min(100, SimilarityMini.totalNumDocs);
		double[] cacmMap = new double[docSize];
		double[] projOneCacmMap = new double[docSize]; // used to calculate part (d): # queries got worse.
		for (int i = 1; i <= SimilarityMini.queryMap.size(); i++) {
			Pair[] result = ClusterMini.cacmQuery.get(i);
			Pair[] projOneResult = SimilarityMini.relevantDocs(i, 100, "none", null);
			cacmMap[i] = SimilarityMini.mapPrecision(answerMap.get(i), result, "none", null);
			projOneCacmMap[i] = SimilarityMini.mapPrecision(answerMap.get(i), projOneResult, "none", null);
		}
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			if (cacmMap[i] < projOneCacmMap[i])
				numQueriesWorse += 1;
			sum += cacmMap[i];
		}
		System.out.println("CACM MAP K = 20: " + sum/SimilarityMini.queryMap.size());
	}

	// MED MAP k = 10. (average)
	public static void printMedMapK10() {
		SimilarityMini.initMed();
		ClusterMini.initTopDocs("med");
		for (int i = 1; i <= 30; i++) {
			ClusterMini.completeLinkCluster(i, 10, "med", "average");	
		}
		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);
		int docSize = Math.min(100, SimilarityMini.totalNumDocs);
		double[] medMap = new double[docSize];
		double[] projOneMedMap = new double[docSize]; // used to calculate part (d): # queries got worse.
		for (int i = 1; i <= SimilarityMini.queryMap.size(); i++) {
			Pair[] result = ClusterMini.medQuery.get(i);
			Pair[] projOneResult = SimilarityMini.relevantDocs(i, 100, "none", null);
			medMap[i] = SimilarityMini.mapPrecision(medAnswerMap.get(i), result, "none", null);
			projOneMedMap[i] = SimilarityMini.mapPrecision(medAnswerMap.get(i), projOneResult, "none", null);
		}
		double medsum = 0.0;
		for (int i = 0; i < medMap.length; i++) {
			if (medMap[i] < projOneMedMap[i])
				numQueriesWorse += 1;
			medsum += medMap[i];
		}
		System.out.println("MED MAP K = 10: " + medsum/SimilarityMini.queryMap.size());
	}

	// MED MAP k = 20. (highest)
	public static void printMedMapK20() {
		SimilarityMini.initMed();
		ClusterMini.initTopDocs("med");
		for (int i = 1; i <= 30; i++) {
			ClusterMini.completeLinkCluster(i, 20, "med", "highest");	
		}
		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);
		int docSize = Math.min(100, SimilarityMini.totalNumDocs);
		double[] medMap = new double[docSize];
		double[] projOneMedMap = new double[docSize]; // used to calculate part (d): # queries got worse.
		for (int i = 1; i <= SimilarityMini.queryMap.size(); i++) {
			Pair[] result = ClusterMini.medQuery.get(i);
			Pair[] projOneResult = SimilarityMini.relevantDocs(i, 100, "none", null);
			medMap[i] = SimilarityMini.mapPrecision(medAnswerMap.get(i), result, "none", null);
			projOneMedMap[i] = SimilarityMini.mapPrecision(medAnswerMap.get(i), projOneResult, "none", null);
		}
		double medsum = 0.0;
		double maxDiff = -1; // for part (f) analysis. getting the max difference between map of atc and clustering.
		int index = -1;
		for (int i = 0; i < medMap.length; i++) {
			
			if (medMap[i] < projOneMedMap[i])
				numQueriesWorse += 1;
			double diff = Math.abs(medMap[i] - projOneMedMap[i]);
			if (diff > maxDiff) {
				maxDiff = diff;
				index = i;
			}
			medsum += medMap[i];
		}
		System.out.println("Part(f) analysis only: index=" + index + 
				" difference in map (" + medMap[index] + ", " + projOneMedMap[index] + ")=" + maxDiff);
		System.out.println("MED MAP K = 20: " + medsum/SimilarityMini.queryMap.size());
	}
}
