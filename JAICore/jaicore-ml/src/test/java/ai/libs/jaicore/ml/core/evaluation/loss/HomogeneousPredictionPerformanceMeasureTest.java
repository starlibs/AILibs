package ai.libs.jaicore.ml.core.evaluation.loss;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;

public class HomogeneousPredictionPerformanceMeasureTest {

	private static final double DELTA = 1E-8;

	private static final List<Integer> VEC_EXP = getIntArrayAsIntList(new int[]{ 1, 0, 0, 1, 1, 1, 0, 1 });
	private static final List<Integer> VEC_ACT = getIntArrayAsIntList(new int[]{ 1, 0, 1, 0, 1, 0, 1, 1 });

	private static List<Integer> getIntArrayAsIntList(final int[] array) {
		List<Integer> list = new ArrayList<>();
		for (int i : array) {
			list.add(i);
		}
		return list;
	}

	@Test
	public void testTrueNegatives() {
		assertEquals("Wrong number of true negatives", 1.0, EClassificationPerformanceMeasure.TRUE_NEGATIVES_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testTruePositives() {
		assertEquals("Wrong number of true positives", 3.0, EClassificationPerformanceMeasure.TRUE_POSITIVES_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testFalseNegatives() {
		assertEquals("Wrong number of false negatives", 2.0, EClassificationPerformanceMeasure.FALSE_NEGATIVES_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testFalsePositives() {
		assertEquals("Wrong number of false positives", 2.0, EClassificationPerformanceMeasure.FALSE_POSITIVES_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testErrorRate() {
		assertEquals("ErrorRate is not correct", 0.5, EClassificationPerformanceMeasure.ERRORRATE.loss(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testPrecision() {
		assertEquals("Precision is not correct", 0.6, EClassificationPerformanceMeasure.PRECISION_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testRecall() {
		assertEquals("Recall is not correct", 0.6, EClassificationPerformanceMeasure.RECALL_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testF1Measure() {
		assertEquals("FMeasure is not correct", 0.6, EClassificationPerformanceMeasure.F1_WITH_1_POSITIVE.score(VEC_EXP, VEC_ACT), DELTA);
	}

}
