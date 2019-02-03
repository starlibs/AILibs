package jaicore.ml.evaluation.evaluators.weka.batcheval;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class BatchEvaluator {

	private BatchEvaluator() {
		// intentionally do nothing here.
	}

	public static BatchEvaluationResult evaluateModel(final Classifier c, final Instances testData) throws Exception {
		BatchEvaluationResult result = new BatchEvaluationResult();
		for (Instance i : testData) {
			result.addDatapoint(i.classValue(), c.classifyInstance(i));
		}
		return result;
	}

}
