package jaicore.ml.tsc.quality_measures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
		for (Integer clazz : classDistances.keySet()) {
			classMeans.put(clazz, classDistances.get(clazz).stream().mapToDouble(a -> a).average().getAsDouble());
		}
		double completeMean = distances.stream().mapToDouble(a -> a).average().getAsDouble();
		double denominator = 0;

		// Calculate actual F score
		double result = 0;
		for (Integer clazz : classMeans.keySet()) {
			result += Math.pow(classMeans.get(clazz) - completeMean, 2);

			for (Double dist : classDistances.get(clazz)) {
				denominator += Math.pow(dist - classMeans.get(clazz), 2);
			}
		}
		result /= numClasses - 1;
		denominator /= distances.size() - numClasses;
		result /= denominator;

		return result;
	}
}
