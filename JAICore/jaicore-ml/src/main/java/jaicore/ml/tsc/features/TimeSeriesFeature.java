package jaicore.ml.tsc.features;

import jaicore.ml.tsc.classifier.trees.TimeSeriesTreeClassifier;
import jaicore.ml.tsc.classifier.trees.TimeSeriesTreeLearningAlgorithm;
import jaicore.ml.tsc.util.MathUtil;

/**
 * Class calculating features (e. g. mean, stddev or slope) on given
 * subsequences of time series. Used e. g. for {@link TimeSeriesTreeClassifier}
 * classifier.
 *
 * @author Julian Lienen
 *
 */
public class TimeSeriesFeature {
	/**
	 * Feature types used within the time series tree.
	 */
	public enum FeatureType {
		MEAN, STDDEV, SLOPE
	}

	/**
	 * Number of features used within the time series tree.
	 */
	public static final int NUM_FEATURE_TYPES = FeatureType.values().length;

	/**
	 * Function calculating all features occurring in {@link FeatureType} at once
	 * using an online calculation approach for mean, standard deviation and the
	 * slope.
	 *
	 * @param vector
	 *            The instance's vector which is used to calculate the features
	 * @param t1
	 *            Start of the interval
	 * @param t2
	 *            End of the interval (inclusive)
	 * @param useBiasCorrection
	 *            Indicator whether the bias (Bessel's) correction should be used
	 *            for the standard deviation calculation
	 * @return Returns an double array of the size
	 *         {@link TimeSeriesTreeLearningAlgorithm#NUM_FEATURE_TYPES} storing the
	 *         generated feature values.
	 */
	public static double[] getFeatures(final double[] vector, final int t1, final int t2,
			final boolean useBiasCorrection) {
		double[] result = new double[NUM_FEATURE_TYPES];

		if (t1 >= vector.length || t2 >= vector.length) {
			throw new IllegalArgumentException("Parameters t1 and t2 must be valid indices of the vector.");
		}

		if (t1 == t2) {
			return new double[] { vector[t1], 0d, 0d };
		}

		// Calculate running sums for mean, stddev and slope
		double xx = 0;
		double x = 0;
		double xy = 0;
		double y = 0;
		double yy = 0;
		for (int i = t1; i <= t2; i++) {
			x += i;
			y += vector[i];
			yy += vector[i] * vector[i];
			xx += i * i;
			xy += i * vector[i];
		}
		double length = t2 - t1 + 1d;

		// Calculate the mean
		result[0] = y / length;

		// Calculate the standard deviation
		double stddev = (yy / length - ((y / length) * (y / length)));
		if (useBiasCorrection) {
			stddev *= length / (length - 1);
		}
		stddev = Math.sqrt(stddev);
		result[1] = stddev;

		// Calculate slope
		result[2] = (length * xy - x * y) / (length * xx - x * x);
		return result;
	}

	/**
	 * Function calculating the feature specified by the feature type
	 * <code>fType</code> for a given instance <code>vector</code> of the interval
	 * [<code>t1</code>, <code>t2</code>].
	 *
	 * @param fType
	 *            The feature type to be calculated
	 * @param instance
	 *            The instance's vector which values are used
	 * @param t1
	 *            Start of the interval
	 * @param t2
	 *            End of the interval (inclusive)
	 * @param useBiasCorrection
	 *            Indicator whether the bias (Bessel's) correction should be used
	 *            for the standard deviation calculation
	 * @return Returns the calculated feature for the specific instance and interval
	 */
	public static double calculateFeature(final FeatureType fType, final double[] vector, final int t1, final int t2,
			final boolean useBiasCorrection) {
		switch (fType) {
		case MEAN:
			return MathUtil.mean(vector, t1, t2);
		case STDDEV:
			return MathUtil.stddev(vector, t1, t2, useBiasCorrection);
		case SLOPE:
			return MathUtil.slope(vector, t1, t2);
		default:
			throw new UnsupportedOperationException("Feature calculation function with id '" + fType + "' is unknwon.");
		}
	}

}
