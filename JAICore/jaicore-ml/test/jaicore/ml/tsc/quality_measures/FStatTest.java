package jaicore.ml.tsc.quality_measures;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit tests for {@link FStat}.
 * 
 * @author Julian Lienen
 *
 */
public class FStatTest {
	/**
	 * Maximal delta for asserts with precision.
	 */
	private static final double EPS_DELTA = 0.00001;

	/**
	 * See {@link FStat#assessQuality(List, int[])}.
	 */
	@Test
	public void assessQualityTest() {
		FStat fStat = new FStat();

		List<Double> distances = Arrays.asList(2d, 4d, 3d);
		int[] classValues = new int[] { 0, 0, 1 };
		
		// D_0^bar = 3, D_1^bar = 3, D^bar = 3
		// Therefore, the assessed quality has to be 0
		Assert.assertEquals(0, fStat.assessQuality(distances, classValues), EPS_DELTA);

		distances = Arrays.asList(1d, 4d, 3d);
		classValues = new int[] { 0, 0, 1 };

		// D_0^bar = 2.5, D_1^bar = 3, D^bar = 8/3
		// Therefore, the assessed quality has to be (5/36) / (9/2)
		Assert.assertEquals((5d / 36d) / (9d / 2d), fStat.assessQuality(distances, classValues), EPS_DELTA);
	}
}
