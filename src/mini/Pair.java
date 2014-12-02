package mini;

public class Pair implements Comparable<Pair> {

	private String id;
	private double val;
	
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
		return this.getId() + "=" + this.getVal();
	}
	
}
