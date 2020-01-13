package ai.libs.jaicore.ml.classification.singlelabel.timeseries.quality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * F-Stat quality measure performing a analysis of variance according to chapter
 * 3.2 of the original paper. It analyzes the ratio of the variability between
 * the group of instances within a class to the variability within the class
 * groups.
 *
 * @author Julian Lienen
 *
 */
public class FStat implements IQualityMeasure {
	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 6991529180002046551L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double assessQuality(final List<Double> distances, final int[] classValues) {
		// Order class distances
		HashMap<Integer, List<Double>> classDistances = new HashMap<>();
		for (int i = 0; i < distances.size(); i++) {
			if (!classDistances.containsKey(classValues[i])) {
				classDistances.put(classValues[i], new ArrayList<>());
			}

			classDistances.get(classValues[i]).add(distances.get(i));
		}
		int numClasses = classDistances.size();

		// Calculate class and overall means
		HashMap<Integer, Double> classMeans = new HashMap<>();
		for (Entry<Integer, List<Double>> entry : classDistances.entrySet()) {
			Integer clazz = entry.getKey();
			classMeans.put(clazz, entry.getValue().stream().mapToDouble(a -> a).average().getAsDouble());
		}
		double completeMean = distances.stream().mapToDouble(a -> a).average().getAsDouble();
		double denominator = 0;

		// Calculate actual F score
		double result = 0;
		for (Entry<Integer, Double> entry : classMeans.entrySet()) {
			Integer clazz = entry.getKey();
			double mean = entry.getValue();
			result += Math.pow(mean - completeMean, 2);

			for (Double dist : classDistances.get(clazz)) {
				denominator += Math.pow(dist - mean, 2);
			}
		}
		result /= numClasses - 1;
		denominator /= distances.size() - numClasses;
		if (denominator == 0) {
			throw new IllegalArgumentException("Given arguments yield a 0 " + denominator);
		}
		result /= denominator;

		return result;
	}
}
