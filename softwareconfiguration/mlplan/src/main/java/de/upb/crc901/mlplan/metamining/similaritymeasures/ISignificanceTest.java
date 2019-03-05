package de.upb.crc901.mlplan.metamining.similaritymeasures;

/**
 * Represents a significance test that can be used to decide whether one of two
 * algorithms is better on a specific dataset.
 * 
 * @author Helena Graf
 *
 */
public interface ISignificanceTest {

	/**
	 * Computes the significance of whether the first array of given performance
	 * values is considered to be better than the second
	 * 
	 * @param performanceValues1
	 *            The first array of performance values to compare
	 * @param performanceValues2
	 *            The second array of performance values to compare
	 * @return The resulting significance level of the test
	 */
	public double computeSignificance(double[] performanceValues1, double[] performanceValues2);
}
