package ai.libs.jaicore.basic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.Well1024a;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test-suite to test the StatisticsUtil.
 *
 * @author mwever
 */
class StatisticsUtilTest {

	private static final int SAMPLE_SIZE = 30;

	private static double[] posSampleA;
	private static double[] posSampleB;

	private static double[] negSampleA;
	private static double[] negSampleB;

	@BeforeAll
	static void setup() {
		double[][] samples = generateDistributionSamples(new NormalDistribution(new Well1024a(0), 0.0, 1.0), new NormalDistribution(new Well1024a(2), 0.0, 1.0));
		posSampleA = samples[0];
		posSampleB = samples[1];

		samples = generateDistributionSamples(new NormalDistribution(new Well1024a(0), 0.0, 1.0), new NormalDistribution(new Well1024a(2), 2.0, 1.2));
		negSampleA = samples[0];
		negSampleB = samples[1];
	}

	@Test
	void testWilcoxonSignedRankSumTest() {
		assertFalse("Wilcoxon Signed Rank Test detects different distributions which is not the case.", StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(posSampleA, posSampleB));
		assertTrue("Wilcoxon Signed Rank Test did not detect different distributions although they are.", StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(negSampleA, negSampleB));
	}

	@Test
	void testMannWhitneyUTest() {
		assertFalse("MannWhitneyUTest detects different distributions which is not the case.", StatisticsUtil.mannWhitneyTwoSidedSignificance(posSampleA, posSampleB));
		assertTrue("Wilcoxon Signed Rank Test did not detect different distributions although they are.", StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(negSampleA, negSampleB));
	}

	@Test
	void testTTest() {
		assertFalse("TTest identifies different distributions which is not the case", StatisticsUtil.twoSampleTTestSignificance(posSampleA, posSampleB));
		assertTrue("TTest did not detect different distributions although they are.", StatisticsUtil.twoSampleTTestSignificance(negSampleA, negSampleB));
	}

	private static final List<Integer> EMPTY_LIST = Arrays.asList();
	private static final List<Integer> LIST_A = Arrays.asList(5, 3, 9, 13);
	private static final List<Integer> LIST_B = Arrays.asList(7, 4, 10, 11, 18);
	private static final List<Integer> LIST_C = Arrays.asList(3);
	private static final List<List<Integer>> ALL_LISTS = Arrays.asList(EMPTY_LIST, LIST_A, LIST_B, LIST_C);

	void testDoubleEquals(final List<Double> expected, final List<Double> actual) {
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).doubleValue(), actual.get(i).doubleValue());
		}
	}

	@Test
	void testMedian() {
		List<Double> expected = Arrays.asList(Double.NaN, 7.0, 10.0, 3.0);
		this.testDoubleEquals(expected, ALL_LISTS.stream().map(StatisticsUtil::median).collect(Collectors.toList()));
	}

	@Test
	void testMean() {
		List<Double> expected = Arrays.asList(Double.NaN, 7.5, 10.0, 3.0);
		this.testDoubleEquals(expected, ALL_LISTS.stream().map(StatisticsUtil::mean).collect(Collectors.toList()));
	}

	@Test
	void testSum() {
		List<Double> expected = Arrays.asList(0.0, 30.0, 50.0, 3.0);
		this.testDoubleEquals(expected, ALL_LISTS.stream().map(StatisticsUtil::sum).collect(Collectors.toList()));
	}

	@Test
	void testMin() {
		List<Double> expected = Arrays.asList(Double.NaN, 3.0, 4.0, 3.0);
		this.testDoubleEquals(expected, ALL_LISTS.stream().map(StatisticsUtil::min).collect(Collectors.toList()));
	}

	@Test
	void testMax() {
		List<Double> expected = Arrays.asList(Double.NaN, 13.0, 18.0, 3.0);
		this.testDoubleEquals(expected, ALL_LISTS.stream().map(StatisticsUtil::max).collect(Collectors.toList()));
	}

	/**
	 * Generates a paired sample for two real distribution.
	 *
	 * @param dist0 The distribution to draw the first sample from.
	 * @param dist1 The distribution to draw the second sample from.
	 * @return The drawn samples according to the given distributions.
	 */
	private static double[][] generateDistributionSamples(final AbstractRealDistribution dist0, final AbstractRealDistribution dist1) {
		double[] sampleA = new double[SAMPLE_SIZE];
		double[] sampleB = new double[SAMPLE_SIZE];

		for (int i = 0; i < SAMPLE_SIZE; i++) {
			sampleA[i] = dist0.sample();
			sampleB[i] = dist1.sample();
		}
		return new double[][] { sampleA, sampleB };
	}

}
