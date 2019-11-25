package ai.libs.jaicore.ml;

import org.junit.Test;

import ai.libs.jaicore.basic.ArrayUtil;
import ai.libs.jaicore.ml.core.evaluation.loss.F1Measure;
import meka.core.Metrics;

public class MultilabelMeasureTest {

	@Test
	public void test() {
		int[][] gt = { { 1, 0, 1, 1, 0, 1, 1 }, { 1, 0, 1, 0, 0, 1, 0 } };
		int[][] test = { { 0, 1, 1, 1, 1, 0, 1 }, { 1, 0, 1, 1, 0, 0, 1 } };

		double sum = 0.0;
		for (int i = 0; i < gt.length; i++) {
			System.out.println(Metrics.F1(gt[i], test[i]) + " / " + new F1Measure(1).score(gt[i], test[i]));
			sum += Metrics.F1(gt[i], test[i]);
		}
		System.out.println((sum / 2));
		System.out.println(Metrics.P_FmacroAvgD(gt, test));
		System.out.println(Metrics.P_FmacroAvgL(gt, test));
		System.out.println(Metrics.P_FmicroAvg(gt, test));

		gt = ArrayUtil.transposeIntegerMatrix(gt);
		test = ArrayUtil.transposeIntegerMatrix(test);

		System.out.println("====");
		sum = 0.0;
		for (int i = 0; i < gt.length; i++) {
			System.out.println(Metrics.F1(gt[i], test[i]) + " / " + new F1Measure(1).score(gt[i], test[i]));
			sum += Metrics.F1(gt[i], test[i]);
		}
		System.out.println((sum / 7));
	}

}
