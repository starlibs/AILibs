package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.ArrayList;
import java.util.List;

public class F1MacroAverageL extends F1MacroAverageD {

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		throw new UnsupportedOperationException("This method is not applicable for F1Macro averaged by L");
	}

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
}
