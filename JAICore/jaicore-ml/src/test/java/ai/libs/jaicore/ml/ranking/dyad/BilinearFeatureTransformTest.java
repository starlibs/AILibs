package ai.libs.jaicore.ml.ranking.dyad;

import static org.junit.Assert.assertEquals;

import org.api4.java.common.math.IVector;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.ranking.dyad.general.DyadSupplier;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.featuretransform.BiliniearFeatureTransform;

/**
 * Class for testing the functionality of
 *
 * @author Helena Graf, Mirko Juergens
 *
 */
public class BilinearFeatureTransformTest {

	/**
	 * Tests if the bilinear feature transformation yields a vector of the correct
	 * length.
	 */
	@Test
	public void testLengthOfVectorTransform() {
		System.out.println("Testing the length of the result of the Bilinear Feature Transform");
		Dyad dyad = DyadSupplier.getRandomDyad(4, 5);

		BiliniearFeatureTransform featureTransform = new BiliniearFeatureTransform();

		assertEquals(20, featureTransform.transform(dyad).length());
	}

	/**
	 * Tests if the transform method generates the correct values for a small
	 * example.
	 */
	@Test
	public void testTransform() {
		System.out.println("Testing the soundness of the Bilinear Feature Transform.");
		/**
		 * This yields the dyad x = (0.730967787376657, 0.24053641567148587,
		 * 0.6374174253501083) y = (0.5504370051176339, 0.5975452777972018)
		 *
		 * By definition, the BilinierFT is the kroenecker product x * y : = (x_1 * y_1,
		 * x_1 * y_2, x_2 * y_1, x_2 * y_2, x_3 * y_1, x_3* y_2)
		 *
		 */
		Dyad dyad = new Dyad(new DenseDoubleVector(new double[] { 0.730967787376657, 0.24053641567148587, 0.6374174253501083 }), new DenseDoubleVector(new double[] { 0.5504370051176339, 0.5975452777972018 }));

		/**
		 * This should yield the vector y = (0.402352, 0.436786, 0.1324, 0.143731,
		 * 0.350858, 0.380886)
		 */

		IVector expectedResult = new DenseDoubleVector(new double[] { 0.402352, 0.436786, 0.1324, 0.143731, 0.350858, 0.380886 });

		BiliniearFeatureTransform bft = new BiliniearFeatureTransform();
		IVector actualResult = bft.transform(dyad);

		Assert.assertArrayEquals(expectedResult.asArray(), actualResult.asArray(), 0.0001);

	}
}
