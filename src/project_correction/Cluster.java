package project_correction;

import java.util.ArrayList;

import mini.ClusterObj;
import mini.Pair;

public class Cluster {

	public Cluster() {
		clusterList = new ArrayList<ClusterObj>();
	}
	private ArrayList<ClusterObj> clusterList;

	public ArrayList<ClusterObj> getClusterList() {
		return clusterList;
	}

	public void setClusterList(ArrayList<ClusterObj> clusterList) {
		this.clusterList = clusterList;
	}
	
	public void add(ClusterObj clusterObj) {
		clusterList.add(clusterObj);
	}
	
	public int size() {
		return clusterList.size();
	}
	
	public void remove(ClusterObj clusterObj) {
		clusterList.remove(clusterObj);
	}
	
	public ClusterObj get(int index) {
		return clusterList.get(index);
	}
	
	public ClusterObj combine(ClusterObj c1, ClusterObj c2) {
		ClusterObj combine = new ClusterObj();
		for (Pair p : c1.getItems())
			combine.getItems().add(p);
		for (Pair p : c2.getItems())
			combine.getItems().add(p);
		return combine;
	}
	
	public void printCluster() {
		for (ClusterObj cObj : clusterList) {
			System.out.println(cObj);
		}
	}
}
