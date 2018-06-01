package jaicore.ml.multilabel.evaluators;

import java.util.Random;

import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.core.Instances;

@SuppressWarnings("serial")
public class HammingMultilabelEvaluator extends MultilabelEvaluator {

	public HammingMultilabelEvaluator(Random r) {
		super(r);
	}

	@Override
	public double loss(MultiLabelClassifier builtClassifier, Instances test) throws Exception {
		Result result = Evaluation.testClassifier(builtClassifier, test);
		int[][] actuals = result.allTrueValues();
		int[][] decisions = result.allPredictions(.5);
		int score = 0;
		for (int row = 0; row < actuals.length; row ++) {
			for (int col = 0; col < actuals[row].length; col ++) {
				if (actuals[row][col] != decisions[row][col])
					score ++;
			}
		}
		double hamming = score *100f / (actuals.length * actuals[0].length);
		return hamming;
	}

}
