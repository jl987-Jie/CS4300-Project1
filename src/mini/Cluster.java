package mini;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import mini.Constants;
import mini.EvaluateQueriesMini;
import mini.Pair;
import mini.SimilarityMini;

/** Part 2
 * 
 * @author jl987
 *
Perform an atc.atc weighted inner-product similarity retrieval, retrieving the top 100
documents for each query. For each query, do a complete link clustering of the top 30 (do
not use all 100) atc weighted documents, stopping the clustering when you have reduced
the number of clusters from 30 (initial setup) to K clusters. The distance function you
should use is 1/dot product (this is equivalent to the inverse cosine similarity, since these
are cosine normalized document vector).

(a). Use K = 20. Re-rank the 100 documents you initially retrieved by, for each cluster
assigning the highest similarity found in any of the documents for that cluster to all
documents in that cluster (i.e., all documents in a cluster will be retrieved at the best
rank of any of them, except for having to break the similarity ties). If a document
is not in a cluster, its similarity does not change.

 */
public class Cluster {

	public static void main(String[] args) {
		//		SimilarityMini.init();
		//		initCacmMap();
		//
		//		printMapScore(100, 20);

		SimilarityMini.initMed();
		initMedMap();
		printMapScoreMed(100, 20);
	}

	// <queryId, Pair[]> where Pair[] is a sorted 100 top documents using atc.atc (CACM)
	public static HashMap<Integer, Pair[]> cacmTop100DocMap = new HashMap<Integer, Pair[]>();

	// <queryId, Pair[]> where Pair[] is a sorted 100 top documents using atc.atc (MED)
	public static HashMap<Integer, Pair[]> medTop100DocMap = new HashMap<Integer, Pair[]>();

	// Initialize cacmTop100DocMap (cacm queries are from 1 to 52)
	public static void initCacmMap() {
		for (int i = 1; i <= 52; i++) {
			Pair pArray[] = SimilarityMini.relevantDocs(i, 100, null, null);
			cacmTop100DocMap.put(i, pArray);
		}
	}

	// Initialize medTop100DocMap (med queries are from 1 to 30)
	public static void initMedMap() {
		for (int i = 1; i <= 30; i++) {
			Pair pArray[] = SimilarityMini.relevantDocs(i, 100, null, null);
			medTop100DocMap.put(i, pArray);
		}
	}

	/**
	 * Cluster
	 * 1. Find similarity between doc i and all other docs.
	 * 2. Get the smallest similarity value (C1, C2).
	 * 3. Cluster (C1, C2) together
	 * 4. Repeat until K clusters left.
	 * @param queryId
	 * @return
	 */
	public static ArrayList<ClusterObj> cluster(int queryId, int K, String type) {
		Pair pArray[];

		if ("med".equals(type)) {
			pArray = medTop100DocMap.get(queryId);
		} else {
			pArray = cacmTop100DocMap.get(queryId); // returns a list of documents ranked by score.
		}
		// consider only top 30.
		ArrayList<ClusterObj> clusters = new ArrayList<ClusterObj>();
		for (int i = 0; i < 30; i++) {
			Pair p = new Pair(); // doc, value of similarity to query, weight vector
			p.setId(pArray[i].getId());
			p.setVal(pArray[i].getVal());
			p.setValArray(SimilarityMini.getRelevance(pArray[i].getId(), -1, null, null));
			ArrayList<Pair> list = new ArrayList<Pair>();
			list.add(p);
			ClusterObj clusterObj = new ClusterObj();
			clusterObj.setItems(list);
			clusters.add(clusterObj);
		}

		while (clusters.size() > K) {
			//			ArrayList<ClusterObj> temp = clusters;

			double minDist = Double.MAX_VALUE;
			int clusterOneIdx = -1;
			int clusterTwoIdx = -1;
			// get the two clusters with the min distance.
			for (int i = 0; i < clusters.size(); i++) {
				for (int j = i+1; j < clusters.size(); j++) {
					double sim = ClusterObj.similarity(clusters.get(i), clusters.get(j));
					if (sim < minDist) {
						clusterOneIdx = i;
						clusterTwoIdx = j;
						minDist = sim;
					}
				}
			}
			// combine the two clusters.
			//			System.out.println("Combining clusters " + clusterOneIdx + ", " + clusterTwoIdx + ": " + minDist);
			ClusterObj newCluster = new ClusterObj();
			ArrayList<Pair> newList = new ArrayList<Pair>();
			for (Pair p : clusters.get(clusterOneIdx).getItems()) {
				newList.add(p);
				newCluster.setItems(newList);
			}
			for (Pair p : clusters.get(clusterTwoIdx).getItems()) {
				newCluster.getItems().add(p);
			}

			// reranking the documents to the highest similarity to the given query.
			double maxSimtoQuery = 0;
			for (Pair p : newCluster.getItems()) {
				if (p.getVal() > maxSimtoQuery) {
					maxSimtoQuery = p.getVal();
				}
			}
			for (Pair p : newCluster.getItems()) {
				p.setVal(maxSimtoQuery);
			}

			ClusterObj obj1 = clusters.get(clusterOneIdx);
			ClusterObj obj2 = clusters.get(clusterTwoIdx);
			clusters.remove(obj1);
			clusters.remove(obj2);
			clusters.add(newCluster);
		}

		return clusters;
	}

	public static void printMapScore(int numDocs, int clusterK) {

		Map<Integer, HashSet<String>> answerMap = EvaluateQueriesMini.loadAnswers(Constants.CACM_ANSWER);

		// CACM
		int docSize = Math.min(numDocs, SimilarityMini.totalNumDocs);
		double[] cacmMap = new double[docSize];

		for (int i = 1; i <= SimilarityMini.queryMap.size(); i++) {
			System.out.println("Performing query " + i);
			Pair[] result = SimilarityMini.relevantDocs(i, numDocs, null, null);
			// re-rank the result array.
			ArrayList<ClusterObj> clusterList = cluster(i, clusterK, "cacm");
			Pair newRankedDoc[] = new Pair[30];
			int idx = 0;
			for (ClusterObj clusterObj : clusterList) {
				for (Pair p : clusterObj.getItems()) {
					newRankedDoc[idx] = p;
					idx++;
				}
			}
			// replace the original with the new ranked scores.
			for (int k = 0; k < 30; k++) {
				result[k] = newRankedDoc[k];
			}
			cacmMap[i] = SimilarityMini.mapPrecision(answerMap.get(i), result, null, null);
		}
		double sum = 0.0;
		for (int i = 0; i < cacmMap.length; i++) {
			sum += cacmMap[i];
		}
		System.out.println("Question 3a atcatc CACM MAP: " + sum/SimilarityMini.queryMap.size());
	}

	// Q3A: MED
	public static void printMapScoreMed(int numDocs, int clusterK) {

		Map<Integer, HashSet<String>> medAnswerMap = EvaluateQueriesMini.loadAnswers(Constants.MED_ANSWER);

		int docSize = Math.min(numDocs, SimilarityMini.totalNumDocs);
		double[] medMap = new double[docSize];

		for (int i = 1; i <= SimilarityMini.queryMap.size(); i++) {
			System.out.println("Performing query " + i);
			Pair[] result = SimilarityMini.relevantDocs(i, numDocs, null, null);
			// re-rank the result array.
			ArrayList<ClusterObj> clusterList = cluster(i, clusterK, "med");
			Pair newRankedDoc[] = new Pair[30];
			int idx = 0;
			for (ClusterObj clusterObj : clusterList) {
				for (Pair p : clusterObj.getItems()) {
					newRankedDoc[idx] = p;
					idx++;
				}
			}
			// replace the original with the new ranked scores.
			for (int k = 0; k < 30; k++) {
				result[k] = newRankedDoc[k];
			}
			medMap[i] = SimilarityMini.mapPrecision(medAnswerMap.get(i), result, null, null);
		}
		double medsum = 0.0;
		for (int i = 0; i < medMap.length; i++) {
			medsum += medMap[i];
		}
		System.out.println("Question 3a atcatc MED MAP: " + medsum/SimilarityMini.queryMap.size());
	}

}
