package jaicore.ml.ranking.clusterbased.modifiedisac;

/**
 * @author Helen
 *         implements the Manhattan distance metric
 */
public class L1DistanceMetric implements IDistanceMetric<Double, double[], double[]> {

	/* (non-Javadoc)
	 * @see jaicore.modifiedISAC.IDistanceMetric#computeDistance(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Double computeDistance(final double[] start, final double[] end) {
		if (start.length != end.length) {
			throw new IllegalArgumentException("Arrays must have same length");
		}
		double distance = 0;
		// takes each entry of the two vectors and computes the difference between
		// the entry in the start vector and the end vector in absolute value.
		for (int i = 0; i < start.length; i++) {
			// If the entry is Nan nothing will be added.
			if (Double.isNaN(start[i]) || Double.isNaN(end[i])) {
				distance += 0;
			} else {
				distance += Math.abs((start[i] - end[i]));
			}
		}
		return distance;
	}
}
