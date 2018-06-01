package jaicore.ml.multilabel.evaluators;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import jaicore.basic.SetUtil;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.core.Instances;

@SuppressWarnings("serial")
public class RankMultilabelEvaluator extends MultilabelEvaluator {

	public RankMultilabelEvaluator(Random r) {
		super(r);
	}

	@Override
	public double loss(MultiLabelClassifier builtClassifier, Instances test) throws Exception {
		Result result = Evaluation.testClassifier(builtClassifier, test);
		int[][] actuals = result.allTrueValues();
		double[][] predictions = result.allPredictions();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		int labels = predictions[0].length;
		List<Set<Integer>> labelPairs = SetUtil.getAllPossibleSubsetsWithSize(ContiguousSet.create(Range.closed(0, labels - 1), DiscreteDomain.integers()).asList(), 2);

		for (int row = 0; row < actuals.length; row++) {

			int mistakes = 0;
			int differentPairs = 0;
			for (Set<Integer> pair : labelPairs) {
				Iterator<Integer> it = pair.iterator();
				int x = it.next();
				int y = it.next();
				double xProb = predictions[row][x];
				double yProb = predictions[row][y];
				int xTrue = actuals[row][x];
				int yTrue = actuals[row][y];
				if (xTrue == yTrue)
					continue;
				differentPairs++;
				if (xProb == yProb)
					mistakes += 0.5;
				else if (xTrue == 1 && xProb < yProb)
					mistakes++;
				else if (yTrue == 1 && yProb < xProb)
					mistakes++;
			}
			stats.addValue(mistakes * 100f / differentPairs);
		}
		return stats.getMean();
	}

}
