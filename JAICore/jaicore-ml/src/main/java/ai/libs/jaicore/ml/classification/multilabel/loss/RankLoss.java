package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceWiseLossFunction;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import ai.libs.jaicore.basic.sets.SetUtil;

public class RankLoss implements IInstanceWiseLossFunction<double[]> {

	@Override
	public double loss(final double[] actual, final double[] expected) {
		int numLabels = actual.length;
		List<Set<Integer>> labelPairs = SetUtil.getAllPossibleSubsetsWithSize(ContiguousSet.create(Range.closed(0, numLabels - 1), DiscreteDomain.integers()).asList(), 2);

		int mistakes = 0;
		int differentPairs = 0;
		for (Set<Integer> pair : labelPairs) {
			Iterator<Integer> it = pair.iterator();
			int x = it.next();
			int y = it.next();
			double xProb = expected[x];
			double yProb = expected[y];
			double xTrue = actual[x];
			double yTrue = actual[y];
			if (xTrue == yTrue) {
				continue;
			}
			differentPairs++;
			if (xProb == yProb) {
				mistakes += 0.5;
			} else if (xTrue == 1 && xProb < yProb) {
				mistakes++;
			} else if (yTrue == 1 && yProb < xProb) {
				mistakes++;
			}
		}

		if (differentPairs == 0) {
			return Double.NaN;
		}

		return (double) mistakes / differentPairs;
	}
}
