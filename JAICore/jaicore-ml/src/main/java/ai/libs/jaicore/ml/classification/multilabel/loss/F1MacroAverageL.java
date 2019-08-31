package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.loss.IBatchLossFunction;

import meka.core.Metrics;

public class F1MacroAverageL extends InstanceWiseF1 implements IBatchLossFunction<double[]> {

	@Override
	public double loss(final List<double[]> actual, final List<double[]> expected) {
		double[][] ypred = new double[actual.size()][];
		int[][] ypredint = new int[actual.size()][];
		for (int i = 0; i < actual.size(); i++) {
			ypred[i] = actual.get(i);
			ypredint[i] = Arrays.stream(actual.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		int[][] y = new int[expected.size()][];
		for (int i = 0; i < expected.size(); i++) {
			y[i] = Arrays.stream(expected.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		return Metrics.P_FmacroAvgL(y, ypredint);
	}

}
