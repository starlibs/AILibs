package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import meka.core.Metrics;

public class F1MacroAverageL extends InstanceWiseF1 {

	@Override
	public List<Double> calculateMeasure(final List<double[]> actual, final List<double[]> expected) {
		List<Double> results = new ArrayList<>();
		for (int i = 0; i < actual.get(0).length; i++) {
			double[] actualColumn = this.getIthColumn(actual, i);
			double[] expectedColumn = this.getIthColumn(expected, i);
			results.add(super.calculateMeasure(actualColumn, expectedColumn));
		}
		return results;
	}

	private double[] getIthColumn(final List<double[]> matrix, final int i) {
		double[] column = new double[matrix.size()];
		for (int j = 0; j < column.length; j++) {
			column[j] = matrix.get(j)[i];
		}
		return column;
	}

	@Override
	public Double calculateAvgMeasure(final List<double[]> actual, final List<double[]> expected) {
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
