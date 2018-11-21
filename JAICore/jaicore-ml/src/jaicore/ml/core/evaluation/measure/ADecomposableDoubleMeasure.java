package jaicore.ml.core.evaluation.measure;

import java.util.List;

public abstract class ADecomposableDoubleMeasure<INPUT> extends ADecomposableMeasure<INPUT, Double> {

	@Override
	public Double calculateAvgMeasure(List<INPUT> actual, List<INPUT> expected) {
		return calculateMeasure(actual,  expected, l -> {
			double sum = 0;
			for (double i : l)
				sum += i;
			return sum / l.size();
		});
	}
}
