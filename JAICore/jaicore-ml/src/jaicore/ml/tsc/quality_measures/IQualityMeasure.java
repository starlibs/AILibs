package jaicore.ml.tsc.quality_measures;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for a quality measure assessing distances of instances to a
 * shapelet given the corresponding class values. This functional interface is
 * used within the Shapelet Transform approach to assess shapelet candidates.
 * 
 * @author Julian Lienen
 *
 */
public interface IQualityMeasure extends Serializable {

	/**
	 * Computes a quality score based on the distances of each instance to the
	 * shapelet and the corresponding <code>classValues</code>.
	 * 
	 * @param distances
	 *            List of distances storing the distance of each instance to a
	 *            shapelet
	 * @param classValues
	 *            The class values of the instances
	 * @return Returns the calculated quality score
	 */
	public double assessQuality(final List<Double> distances, final int[] classValues);
}
