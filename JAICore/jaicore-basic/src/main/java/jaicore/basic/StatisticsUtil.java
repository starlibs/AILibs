package jaicore.basic;

import java.util.Collection;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

/**
 * Utils for computing some statistics from collections of doubles or arrays.
 *
 * @author mwever
 */
public class StatisticsUtil {

	/**
	 * Forbid to create an object of ListHelper as there are only static methods allowed here.
	 */
	private StatisticsUtil() {
		// intentionally do nothing
	}

	/**
	 * Filters the maximum value of the given collection.
	 *
	 * @param values Values to take the maximum from.
	 * @return The maximum value of the provided collection.
	 */
	public static double max(final Collection<? extends Number> values) {
		return values.stream().mapToDouble(x -> x.doubleValue()).max().getAsDouble();
	}

	/**
	 * Filters the minimum value of the given collection.
	 *
	 * @param values Values to take the minimum from.
	 * @return The minimum value of the provided collection.
	 */
	public static double min(final Collection<? extends Number> values) {
		return values.stream().mapToDouble(x -> x.doubleValue()).min().getAsDouble();
	}

	/**
	 * Computes the mean of the given collection.
	 *
	 * @param values Values to take the mean of.
	 * @return The mean of the provided values.
	 */
	public static double mean(final Collection<? extends Number> values) {
		return values.stream().mapToDouble(x -> x.doubleValue()).average().getAsDouble();
	}

	/**
	 * Computes the sum of the given collection.
	 *
	 * @param values Values to take the sum of.
	 * @return The sum of the provided values.
	 */
	public static double sum(final Collection<? extends Number> values) {
		return values.stream().mapToDouble(x -> x.doubleValue()).sum();
	}

	/**
	 * Computes the variance of the given collection.
	 *
	 * @param values Values to compute the variance for.
	 * @return The variance of the provided values.
	 */
	public static double variance(final Collection<? extends Number> values) {
		final double mean = mean(values);
		return values.stream().mapToDouble(x -> x.doubleValue()).map(x -> Math.pow(x - mean, 2) / values.size()).sum();
	}

	/**
	 * Computes the standard deviation of the given collection.
	 *
	 * @param values Values to compute the standard deviation for.
	 * @return The standard deviation of the provided values.
	 */
	public static double standardDeviation(final Collection<? extends Number> values) {
		return Math.sqrt(variance(values));
	}

	/**
	 * Computes the p-value according to the Wilcoxon signed rank test for related samples A and B.
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return The p-value of the test for the given two samples.
	 */
	public static double wilcoxonSignedRankSumTestP(final double[] sampleA, final double[] sampleB) {
		return new WilcoxonSignedRankTest().wilcoxonSignedRankTest(sampleA, sampleB, false);
	}

	/**
	 * Uses the Wilcoxon Signed Rank test to determine whether the difference of the distributions of the the two given samples is significantly different (two-sided test).
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */
	public static boolean wilcoxonSignedRankSumTestTwoSided(final double[] sampleA, final double[] sampleB) {
		return wilcoxonSignedRankSumTestP(sampleA, sampleB) < 0.05;
	}

	/**
	 * Uses the Wilcoxon Signed Rank test to carry out a single-sided significance test.
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.01)
	 */
	public static boolean wilcoxonSignedRankSumTestSingleSided(final double[] sampleA, final double[] sampleB) {
		return wilcoxonSignedRankSumTestP(sampleA, sampleB) < 0.01;
	}

	/**
	 * Computes the p-value according to the MannWhitneyU test for iid. samples A and B.
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return The p-value of the test for the given two samples.
	 */
	public static double mannWhitneyTwoSidedSignificanceP(final double[] sampleA, final double[] sampleB) {
		return new MannWhitneyUTest().mannWhitneyUTest(sampleA, sampleB);
	}

	/**
	 * Uses the MannWhitneyU test to determine whether the distributions of the the two given samples is significantly different (two-sided test).
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */
	public static boolean mannWhitneyTwoSidedSignificance(final double[] sampleA, final double[] sampleB) {
		return mannWhitneyTwoSidedSignificanceP(sampleA, sampleB) < 0.05;
	}

	/**
	 * Uses the MannWhitneyU test to determine whether the distributions of the the two given samples is significantly different (two-sided test).
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */
	public static boolean mannWhitneyTwoSidedSignificance(final Collection<Double> sampleA, final Collection<Double> sampleB) {
		return mannWhitneyTwoSidedSignificance(collectionToArray(sampleA), collectionToArray(sampleB));
	}

	/**
	 * Uses the MannWhitneyU test to carry out a single-sided significance test.
	 *
	 * @param sampleA The first sample.
	 * @param sampleB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.01)
	 */
	public static boolean mannWhitneyOneSidedSignificance(final double[] sampleA, final double[] sampleB) {
		return mannWhitneyTwoSidedSignificanceP(sampleA, sampleB) < 0.01;
	}

	/**
	 * Carries out a two sample ttest to determine whether the distributions of the two given samples are significantly different. Requires the distributions to be a normal distribution respectively.
	 *
	 * @param valuesA The first sample..
	 * @param valuesB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */
	public static boolean twoSampleTTestSignificance(final Collection<Double> valuesA, final Collection<Double> valuesB) {
		return twoSampleTTestSignificance(collectionToArray(valuesA), collectionToArray(valuesB));
	}

	/**
	 * Carries out a two sample ttest to determine whether the distributions of the two given samples are significantly different. Requires the distributions to be a normal distribution respectively.
	 *
	 * @param valuesA The first sample..
	 * @param valuesB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */
	public static boolean twoSampleTTestSignificance(final double[] valuesA, final double[] valuesB) {
		return new TTest().tTest(valuesA, valuesB, 0.05);
	}

	/**
	 *
	 * @param valuesA The first sample..
	 * @param valuesB The second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */

	/**
	 * Carries out a two sample ttest when the sampled values have already been aggregated to mean, variance, and n, to determine whether the distributions of the two given samples are significantly different. Requires the distributions to be a normal distribution respectively.
	 *
	 * @param mean1 The mean of the first sample.
	 * @param variance1 The variance of the first sample.
	 * @param n1 The sample size of the first sample.
	 * @param mean2 The mean of the second sample.
	 * @param variance2 The variance of the second sample.
	 * @param n2 The sample size of the second sample.
	 * @return True iff the difference is significant (p-value &lt; 0.05)
	 */
	public static boolean twoSampleTTestSignificance(final double mean1, final double variance1, final double n1, final double mean2, final double variance2, final double n2) {
		double meanDifference = mean1 - mean2;
		double sP = Math.sqrt(((n1 - 1) * variance1 + (n2 - 1) * variance2) / (n1 + n2 - 2));
		double t = meanDifference / (sP * Math.sqrt(1 / n1 + 1 / n2));
		return t < 0.05;
	}

	/* Helper method to transform double collections into arrays. */
	private static double[] collectionToArray(final Collection<Double> collection) {
		return collection.stream().mapToDouble(x -> x).toArray();
	}
}
