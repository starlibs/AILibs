package jaicore.ml.dyadranking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import jaicore.ml.dyadranking.algorithm.BiliniearFeatureTransform;
import jaicore.ml.dyadranking.general.DyadSupplier;

/**
 * Class for testing the functionality of
 * 
 * @author Helena Graf
 *
 */
public class BilinearFeatureTransformTest {

	/**
	 * Tests if the bilinear feature transformation yields a vector of the correct
	 * length.
	 */
	@Test
	public void testLengthOfVectorTransform() {
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

	}
}
