package mini;

public class UtilsMini {

	// Dot Product (inner product) between two vectors.
	public static double dotProduct(double[] v1, double[] v2) {
		double prod = 0.0;
		for (int i = 0; i < v1.length; i++) {
			prod += v1[i] * v2[i];
		}
		return prod;
	}

	// Magnitude of vector.
	public static double magnitude(double[] v1) {
		double mag = 0.0;
		for (int i = 0; i < v1.length; i++) {
			mag += v1[i] * v1[i];
		}
		return Math.sqrt(mag);
	}

	// Cosine Normalization between two vectors.
	public static double cosineNormalization(double[] v1, double[] v2) {
		double prod = dotProduct(v1, v2);
		double mag 	= magnitude(v1) * magnitude(v2);
		if (mag == 0.0)
			return 0.0;
		return prod / mag;
	}
}
