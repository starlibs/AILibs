package jaicore.ml.multilabel.evaluators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jaicore.order.SetUtil;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.core.Instances;

@SuppressWarnings("serial")
public class JaccardMultilabelEvaluator extends MultilabelEvaluator {

	public JaccardMultilabelEvaluator(Random r) {
		super(r);
	}

	@Override
	public double loss(MultiLabelClassifier builtClassifier, Instances test) throws Exception {
		Result result = Evaluation.testClassifier(builtClassifier, test);
		int[][] actuals = result.allTrueValues();
		int[][] decisions = result.allPredictions(.5);
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int row = 0; row < actuals.length; row ++) {
			Collection<Integer> t = new HashSet<>();
			Collection<Integer> p = new HashSet<>();
			for (int col = 0; col < actuals[row].length; col ++) {
				if (actuals[row][col] == 1)
					t.add(col);
				if (decisions[row][col] == 1)
					p.add(col);
			}
			double jaccardScore = SetUtil.intersection(t, p).size() * 1f / SetUtil.union(t, p).size();
			double jaccardError  = 100f * (1 - jaccardScore);
			stats.addValue(jaccardError);
		}
		return stats.getMean();
	}

}
