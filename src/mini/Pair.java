package mini;

public class Pair implements Comparable<Pair> {

	private String id;
	private double val;
	private double[] valArray;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getVal() {
		return val;
	}
	public void setVal(double val) {
		this.val = val;
	}
	public double[] getValArray() {
		return valArray;
	}
	public void setValArray(double[] valArray) {
		this.valArray = valArray;
	}
	
	// Project 2: distance function: 1 / dot product.
	public static double distFunction(Pair p, Pair q) {
	
		double dotProd = 0;
		double magP = UtilsMini.magnitude(p.getValArray());
		double magQ = UtilsMini.magnitude(q.getValArray());
		
		for (int i = 0; i < p.getValArray().length; i++) {
			dotProd += (p.getValArray()[i] / magP) * (q.getValArray()[i] / magQ);
		}
		if (dotProd == 0) {
			return Integer.MAX_VALUE;
		}
		return 1.0 / dotProd;
	}
	
	@Override
	public int compareTo(Pair o) {
		if (this.val < o.val) {
			return 1;
		} else if (this.val > o.val) {
			return -1;
		}
		return 0;
	}
	@Override
	public String toString() {
		return "Pair [id=" + id + "]";
	}
	
}
