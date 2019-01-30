package jaicore.ml.tsc.util;

/**
 * Utility class consisting of mathematical utility functions.
 * 
 * @author Julian Lienen
 *
 */
public class MathUtil {
	/**
	 * Function to calculate the sigmoid for the given value <code>z</code>.
	 * 
	 * @param z
	 *            Parameter z
	 * @return Returns the sigmoid for the parameter <code>z</code>.
	 */
	public static double sigmoid(final double z) {
		return 1 / (1 + Math.exp((-1) * z));
	}

}
