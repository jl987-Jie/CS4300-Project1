package mini;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mini.ClusterObj;
import mini.Pair;
import mini.SimilarityMini;

public class ClusterMini {

	public static void main(String[] args) {
		SimilarityMini.initMed();
		ClusterMini.initTopDocs("med");
		completeLinkCluster(9, 20, "med", "highest");
		Pair[] p = medQuery.get(9);
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);            
			nf.setGroupingUsed(false);
			for (int k = 0; k < 30; k++) {
				System.out.println(p[k].getId() + "                      " + nf.format(p[k].getVal()));
		}
	}
	public static HashMap<Integer, Pair[]> medQuery = new HashMap<Integer, Pair[]>();
	public static HashMap<Integer, Pair[]> cacmQuery = new HashMap<Integer, Pair[]>();
	
	// Perform an atc.atc weighted inner-product similarity retrieval
	// retrieving the top 100 documents for each query. Stored them inside medQuery or cacmQuery.(DONE).
	public static void initTopDocs(String collectionName) {
		// med_processed.query only has 30 queries; using query # as id.
		if (collectionName.equals("med")) {
			for (int i = 1; i <= 30; i++) {
				Pair[] relDocs = SimilarityMini.relevantDocs(i, 100, "none", null); // get relevant documents for a query.
				medQuery.put(i, relDocs); // assign relevant documents to query.
			}
		} else if (collectionName.equals("cacm")) {
			for (int i = 1; i <= 52; i++) {
				Pair[] relDocs = SimilarityMini.relevantDocs(i, 100, "none", null); // get relevant documents for a query.
				cacmQuery.put(i, relDocs); // assign relevant documents to query.
			}
		}
	}

	// For each query, do a complete link clustering of the top 30 atc weighted 
	// documents, stopping the clustering when you have reduced
	// the number of clusters from 30 (initial setup) to K clusters.
	public static void completeLinkCluster(int queryId, int clusterK, String collectionName, String rerank) {
		Pair[] top30Docs = new Pair[30];
		Pair[] relDocs;
		if (collectionName.equals("med")) {
			for (int i = 0; i < 30; i++) {
				relDocs = medQuery.get(queryId);
				top30Docs[i] = relDocs[i]; // assign the top 30 documents to top30Docs array.
			}
		} else if (collectionName.equals("cacm")) {
			for (int i = 0; i < 30; i++) {
				relDocs = cacmQuery.get(queryId);
				top30Docs[i] = relDocs[i]; // assign the top 30 documents to top30Docs array.
			}
		}

		Cluster resultCluster = new Cluster(); // final cluster result. this contains a list of clusters.
		// initialize each document's atc weight vector.
		for (int i = 0; i < top30Docs.length; i++) {
			String documentId = top30Docs[i].getId();
			top30Docs[i].setValArray(SimilarityMini.getRelevance(documentId, -1, "none", null));

			// assign each document as its own cluster
			ClusterObj clusterObj = new ClusterObj();
			ArrayList<Pair> documentList = new ArrayList<Pair>();
			documentList.add(top30Docs[i]);
			clusterObj.setItems(documentList);
			resultCluster.add(clusterObj);
		}

		// perform complete link clustering on the top 30 documents.
		while (resultCluster.size() > clusterK) {
			double minDistance = Double.MAX_VALUE;
			int idxOne = -1;
			int idxTwo = -1;
			for (int i = 0; i < resultCluster.size(); i++) {
				for (int j = i+1; j < resultCluster.size(); j++) {
					double sim = ClusterObj.similarity(resultCluster.get(i), 
							resultCluster.get(j));
					if (sim < minDistance) {
						minDistance = sim;
						idxOne = i;
						idxTwo = j;
					}
				}
			}
			ClusterObj c1 = resultCluster.get(idxOne);
			ClusterObj c2 = resultCluster.get(idxTwo);
			ClusterObj newCluster = resultCluster.combine(c1, c2);
			resultCluster.add(newCluster);
			resultCluster.remove(c1);
			resultCluster.remove(c2);
		}
//		if (queryId == 9)
//			resultCluster.printCluster();
		
		int docSeenSoFar = 0;
		// re-ranking the documents.
		if (rerank.equals("highest")) {
			for (ClusterObj clusterObj : resultCluster.getClusterList()) {
				double maxRelScore = -1;
				// retrieving the highest relevant score for a clusterObj.
				for (Pair doc : clusterObj.getItems()) {
					if (doc.getVal() > maxRelScore) {
						maxRelScore = doc.getVal();
					}
				}
				// re-ranking the relevance scores for each document within this clusterObj.
				for (Pair doc : clusterObj.getItems()) {
					doc.setVal(maxRelScore);
					top30Docs[docSeenSoFar] = doc;
					docSeenSoFar += 1; // (forgot to increment this earlier!)
				}
				
				// update the rankings in the original list of relevant documents for this query.
				if (collectionName.equals("med")) {
					for (int i = 0; i < 30; i++) {
						relDocs = medQuery.get(queryId); // get the relevant top 100 docs again.
						for (int j = 0; j < 30; j++) {
							relDocs[j] = top30Docs[j]; // update the top 30 documents
						}
						Arrays.sort(relDocs);
						medQuery.put(queryId, relDocs); // put back into map.
					}
				} else if (collectionName.equals("cacm")) {
					for (int i = 0; i < 30; i++) {
						relDocs = cacmQuery.get(queryId);
						for (int j = 0; j < 30; j++) {
							relDocs[j] = top30Docs[j];
						}
						Arrays.sort(relDocs);
						cacmQuery.put(queryId, relDocs);
					}
				}
			}
		} else if (rerank.equals("average")) {
			for (ClusterObj clusterObj : resultCluster.getClusterList()) {
				double totalRelScore = 0.0;
				int totalDocs = 0;
				// retrieving the highest relevant score for a clusterObj.
				for (Pair doc : clusterObj.getItems()) {
					totalRelScore += doc.getVal();
					totalDocs += 1;
				}
				// re-ranking the relevance scores for each document within this clusterObj.
				for (Pair doc : clusterObj.getItems()) {
					doc.setVal(totalRelScore / totalDocs);
					top30Docs[docSeenSoFar] = doc;
					docSeenSoFar += 1;
				}
				
				// update the rankings in the original list of relevant documents for this query.
				if (collectionName.equals("med")) {
					for (int i = 0; i < 30; i++) {
						relDocs = medQuery.get(queryId); // get the relevant top 100 docs again.
						for (int j = 0; j < 30; j++) {
							relDocs[j] = top30Docs[j]; // update the top 30 documents
						}
						Arrays.sort(relDocs);
						medQuery.put(queryId, relDocs); // put back into map.
					}
				} else if (collectionName.equals("cacm")) {
					for (int i = 0; i < 30; i++) {
						relDocs = cacmQuery.get(queryId);
						for (int j = 0; j < 30; j++) {
							relDocs[j] = top30Docs[j];
						}
						Arrays.sort(relDocs);
						cacmQuery.put(queryId, relDocs);
					}
				}
			}
		}
	}
}
