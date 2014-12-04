package project_two;

import java.util.ArrayList;

import mini.Pair;

public class ClusterObj {

	private ArrayList<Pair> items;
	
	public ArrayList<Pair> getItems() {
		return items;
	}

	public void setItems(ArrayList<Pair> items) {
		this.items = items;
	}
	
	public static double similarity(ClusterObj c1, ClusterObj c2) {
		
		double maxSim = 0.0;
		for (Pair item : c1.getItems()) {
			for (Pair otherItem : c2.getItems()) {
				double sim = Pair.distFunction(item, otherItem);
				if (sim > maxSim) {
					maxSim = sim;
				}
			}
		}
		return maxSim;
	}
	
	public static int numItems(ArrayList<ClusterObj> clusterList) {
		int numItems = 0;
		for (ClusterObj clusterObj : clusterList) {
			for (Pair p : clusterObj.getItems()) {
				numItems++;
			}
		}
		return numItems;
	}
	
	@Override
	public String toString() {
		return "ClusterObj [items=" + items + "]";
	}
	
}
