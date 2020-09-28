package ai.libs.jaicore.ml.core.evaluation.loss;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.loss.dataset.APredictionPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.TypelessPredictionDiff;

/**
 * At the time of writing, we had no concrete heterogeneous prediction performance measure available.
 * This test only serves to check whether the intended interface architecture can be made work.
 *
 * We cover only the very simplest case of a type-different comparison, checking whether the int-value of a string equals a given int value.
 *
 * In particular, we check that a typeless prediction (of objects) can be used in a more specific loss function after a respective type cast.
 *
 * @author Felix Mohr
 *
 */
public class HeterogeneousPredictionPerformanceMeasureTest {

	private static final double DELTA = 1E-8;

	private static final List<Integer> VEC_EXP = Arrays.asList(1, 0, 0, 1, 1, 1, 0, 1);
	private static final List<String> VEC_ACT = Arrays.asList("1", "0", "1", "0", "1", "0", "1", "1");

	@Test
	public void testArtificialHeterogeneousPredictionPerformanceMeasure() {
		assertEquals("Prediction failed", .5, new ArtificialPredictionPerformanceMeasure().loss(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testArtificialHeterogeneousPredictionPerformanceMeasureTypelessViaPredictionDiff() {
		IPredictionAndGroundTruthTable<?, ?> diff = new TypelessPredictionDiff(VEC_EXP, VEC_ACT); // create a prediction where type information is lost
		assertEquals("Prediction failed", .5, new ArtificialPredictionPerformanceMeasure().loss(diff.getCastedView(Integer.class, String.class)), DELTA);
	}

	private class ArtificialPredictionPerformanceMeasure extends APredictionPerformanceMeasure<Integer, String> {

		@Override
		public double loss(final List<? extends Integer> expected, final List<? extends String> actual) {
			int mistakes = 0;
			int n = expected.size();
			if (n != actual.size()) {
				throw new IllegalArgumentException();
			}
			for (int i = 0; i < n; i++) {
				if (Integer.parseInt(actual.get(i)) != expected.get(i)) {
					mistakes++;
				}
			}
			return mistakes * 1.0 / n;
		}
	}

}
