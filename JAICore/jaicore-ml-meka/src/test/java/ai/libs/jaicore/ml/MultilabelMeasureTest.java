package ai.libs.jaicore.ml;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ai.libs.jaicore.basic.ArrayUtil;
import meka.core.Metrics;

public class MultilabelMeasureTest {

	private static final double DELTA = 1E-8;

	@Test
	public void test() {
		int[][] gt = { { 1, 0, 1, 1, 0, 1, 1 }, { 1, 0, 1, 0, 0, 1, 0 } };
		int[][] test = { { 0, 1, 1, 1, 1, 0, 1 }, { 1, 0, 1, 1, 0, 0, 1 } };

		double sum = 0.0;
		for (int i = 0; i < gt.length; i++) {
			sum += Metrics.F1(gt[i], test[i]);
		}

		double fmeasureAggregated = sum / 2;
		assertEquals("Instance-wise f-measure is not correct", Metrics.P_FmacroAvgD(gt, test), fmeasureAggregated, DELTA);

		double mekaFMeasureMacroL = Metrics.P_FmacroAvgL(gt, test);

		gt = ArrayUtil.transposeIntegerMatrix(gt);
		test = ArrayUtil.transposeIntegerMatrix(test);

		sum = 0.0;
		for (int i = 0; i < gt.length; i++) {
			sum += Metrics.F1(gt[i], test[i]);
		}
		fmeasureAggregated = sum / 7;
		assertEquals("Instance-wise f-measure is not correct", mekaFMeasureMacroL, fmeasureAggregated, DELTA);
	}

}
