package de.upb.crc901.mlplan.search.evaluators.multilabel;

import java.util.Arrays;
import java.util.Random;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.core.Instances;

@SuppressWarnings("serial")
public class ExactMatchMultilabelEvaluator extends MultilabelEvaluator {

	public ExactMatchMultilabelEvaluator(Random r) {
		super(r);
	}

	@Override
	public double loss(MultiLabelClassifier builtClassifier, Instances test) throws Exception {
		Result result = Evaluation.testClassifier(builtClassifier, test);
		int mistakes = 0;
		int[][] actuals = result.allTrueValues();
		int[][] decisions = result.allPredictions(.5);
		for (int row = 0; row < actuals.length; row ++) {
			if (!Arrays.equals(actuals[row], decisions[row]))
				mistakes ++;
		}
		return mistakes * 100f / test.size();
	}

}
