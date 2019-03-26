package jaicore.ml.core.evaluation.measure.singlelabel;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class MultiClassMeasureBuilder {
	public ADecomposableDoubleMeasure<Double> getEvaluator(MultiClassPerformanceMeasure pm) {
		switch (pm) {
		case ERRORRATE:
			return new ZeroOneLoss();

		default:
			throw new IllegalArgumentException("No support for performance measure " + pm);
		}
	}
}
