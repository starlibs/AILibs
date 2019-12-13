package ai.libs.jaicore.ml.core.evaluation.loss;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ai.libs.jaicore.ml.classification.singlelabel.loss.ErrorRate;

public class PredictionPerformanceMeasureTest {

	private static final double DELTA = 1E-8;

	private static final int[] VEC_EXP = { 1, 0, 0, 1, 1, 1, 0, 1 };
	private static final int[] VEC_ACT = { 1, 0, 1, 0, 1, 0, 1, 1 };

	@Test
	public void testTrueNegatives() {
		assertEquals("Wrong number of true negatives", 1.0, new TrueNegatives(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testTruePositives() {
		assertEquals("Wrong number of true positives", 3.0, new TruePositives(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testFalseNegatives() {
		assertEquals("Wrong number of false negatives", 2.0, new FalseNegatives(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testFalsePositives() {
		assertEquals("Wrong number of false positives", 2.0, new FalseNegatives(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testErrorRate() {
		ErrorRate er = new ErrorRate();
		List<Integer> exp = new ArrayList<>();
		List<Integer> act = new ArrayList<>();
		for (int i : VEC_EXP) {
			exp.add(i);
		}
		for (int i : VEC_ACT) {
			act.add(i);
		}
		double loss = er.loss(exp, act);
		assertEquals("ErrorRate is not correct", 0.5, loss, DELTA);
	}

	@Test
	public void testPrecision() {
		assertEquals("Precision is not correct", 0.6, new Precision(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testRecall() {
		assertEquals("Recall is not correct", 0.6, new Recall(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

	@Test
	public void testFMeasure() {
		assertEquals("FMeasure is not correct", 0.6, new F1Measure(1).score(VEC_EXP, VEC_ACT), DELTA);
	}

}
