package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class RankMultilabelEvaluator extends ADecomposableDoubleMeasure<double[]> {

	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		int numLabels = actual.length;
		List<Set<Integer>> labelPairs = SetUtil.getAllPossibleSubsetsWithSize(
				ContiguousSet.create(Range.closed(0, numLabels - 1), DiscreteDomain.integers()).asList(), 2);

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
		return mistakes * 1.0 / differentPairs;
	}
}
