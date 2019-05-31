package ai.libs.mlplan.metamining.similaritymeasures;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

/**
 * A wrapper for the Mann-Whitney U Significance test.
 *
 * @author Helena Graf
 *
 */
public class MannWhitneyUSignificance implements ISignificanceTest {

	/**
	 * The test object used to compute the significance for new given values
	 */
	private MannWhitneyUTest significanceTest = new MannWhitneyUTest();

	@Override
	public double computeSignificance(final double[] performanceValues1, final double[] performanceValues2) {
		return this.significanceTest.mannWhitneyUTest(performanceValues1, performanceValues2);
	}

}
