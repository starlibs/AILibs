package jaicore.ml.evaluation.measures.multiclass;

import jaicore.ml.evaluation.measures.ADecomposableDoubleMeasure;

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
