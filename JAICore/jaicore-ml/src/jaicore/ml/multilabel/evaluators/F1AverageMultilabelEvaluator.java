package jaicore.ml.multilabel.evaluators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.order.SetUtil;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.core.Instances;

@SuppressWarnings("serial")
public class F1AverageMultilabelEvaluator extends MultilabelEvaluator {
	private static final Logger logger = LoggerFactory.getLogger(F1AverageMultilabelEvaluator.class);

	public F1AverageMultilabelEvaluator(Random r) {
		super(r);
	}

	@Override
	public double loss(MultiLabelClassifier builtClassifier, Instances test) throws Exception {
		Result result = Evaluation.testClassifier(builtClassifier, test);
		int[][] actuals = result.allTrueValues();
		int[][] decisions = result.allPredictions(.5);
		DescriptiveStatistics stats = new DescriptiveStatistics();
		if (actuals.length == 0) {
			logger.error("Cannot compute F1 measure, because apparently there are no test instances. In fact, size of test instance set is {}!", test.size());
			throw new IllegalArgumentException("No test data given");
		}
		int numLabels = actuals[0].length;
		for (int row = 0; row < actuals.length; row ++) {
			Collection<Integer> t = new HashSet<>();
			Collection<Integer> p = new HashSet<>();
			for (int col = 0; col < numLabels; col ++) {
				if (actuals[row][col] == 1)
					t.add(col);
				if (decisions[row][col] == 1)
					p.add(col);
			}
			int correctlyClassified = SetUtil.intersection(t, p).size();
			int numberOfPredicted = p.size();
			int numberOfTrue = t.size();
			double precision = numberOfPredicted > 0 ? (correctlyClassified * 1f / numberOfPredicted) : 0;
			double recall = numberOfTrue > 0 ? (correctlyClassified * 1f / numberOfTrue) : 0;
			double f1 = (2f / (1 / precision + 1 / recall));
			double f1Error = 100 * (1 - f1);
			stats.addValue(f1Error);
		}
		return stats.getMean();
	}


}
