package jaicore.basic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.Well1024a;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test-suite to test the StatisticsUtil.
 *
 * @author mwever
 */
public class StatisticsUtilTest {

	private static final int SAMPLE_SIZE = 30;

	private static double[] posSampleA;
	private static double[] posSampleB;

	private static double[] negSampleA;
	private static double[] negSampleB;

	@BeforeClass
	public static void setup() {
		double[][] samples = generateDistributionSamples(new NormalDistribution(new Well1024a(0), 0.0, 1.0), new NormalDistribution(new Well1024a(2), 0.0, 1.0));
		posSampleA = samples[0];
		posSampleB = samples[1];

		samples = generateDistributionSamples(new NormalDistribution(new Well1024a(0), 0.0, 1.0), new NormalDistribution(new Well1024a(2), 2.0, 1.2));
		negSampleA = samples[0];
		negSampleB = samples[1];
	}

	@Test
	public void testWilcoxonSignedRankSumTest() {
		assertFalse("Wilcoxon Signed Rank Test detects different distributions which is not the case.", StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(posSampleA, posSampleB));
		assertTrue("Wilcoxon Signed Rank Test did not detect different distributions although they are.", StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(negSampleA, negSampleB));
	}

	@Test
	public void testMannWhitneyUTest() {
		assertFalse("MannWhitneyUTest detects different distributions which is not the case.", StatisticsUtil.mannWhitneyTwoSidedSignificance(posSampleA, posSampleB));
		assertTrue("Wilcoxon Signed Rank Test did not detect different distributions although they are.", StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(negSampleA, negSampleB));
	}

	@Test
	public void testTTest() {
		assertFalse("TTest identifies different distributions which is not the case", StatisticsUtil.twoSampleTTestSignificance(posSampleA, posSampleB));
		assertTrue("TTest did not detect different distributions although they are.", StatisticsUtil.twoSampleTTestSignificance(negSampleA, negSampleB));
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
