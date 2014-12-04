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
		SimilarityMini.init();
		initCacmMap();
		ArrayList<ClusterObj> clusterList = cluster(1, 20);
		Pair newRankedDoc[] = new Pair[30];
		int idx = 0;
		int clusterId = 1;
		for (ClusterObj clusterObj : clusterList) {
			System.out.println("Cluster " + clusterId + ": ...");
			for (Pair p : clusterObj.getItems()) {
				System.out.println(p);
				newRankedDoc[idx] = p;
				idx++;
			}
			clusterId++;
		}
		//		Arrays.sort(newRankedDoc);

		//		double test[] = SimilarityMini.getRelevance("CACM-0001", -1, null, null);
		//		for (int i = 0; i < test.length; i++) {
		//			System.out.println(test[i]);
		//		}

		//		SimilarityMini.initMed();
		//		initMedMap();
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
	public static ArrayList<ClusterObj> cluster(int queryId, int K) {
		Pair pArray[] = cacmTop100DocMap.get(queryId); // returns a list of documents ranked by score.
		// consider only top 30.
		ArrayList<ClusterObj> clusters = new ArrayList<ClusterObj>();
		for (int i = 0; i < 30; i++) {
			Pair p = new Pair(); // doc, value of similarity to query, weight vector
			p.setId(pArray[i].getId());
			p.setVal(pArray[i].getVal());
			System.out.println(p.getId() + ", " + p.getVal());
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
			System.out.println("Combining clusters " + clusterOneIdx + ", " + clusterTwoIdx + ": " + minDist);
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
			System.out.println(ClusterObj.numItems(clusters));
		}

		return clusters;
	}
}
