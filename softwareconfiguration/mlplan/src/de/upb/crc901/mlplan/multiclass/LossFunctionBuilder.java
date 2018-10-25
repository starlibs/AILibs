package de.upb.crc901.mlplan.multiclass;

import java.util.Random;

import jaicore.ml.evaluation.BasicMLEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;

public class LossFunctionBuilder {
	public BasicMLEvaluator getEvaluator(MultiClassPerformanceMeasure pm) {
		switch (pm) {
		case ERRORRATE:
			return new MulticlassEvaluator(new Random(0));

		default:
			throw new IllegalArgumentException("No support for performance measure " + pm);
		}
	}
}
