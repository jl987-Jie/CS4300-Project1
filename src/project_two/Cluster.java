package project_two;

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
		
	}
}
