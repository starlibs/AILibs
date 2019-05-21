package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import meka.core.Metrics;

public class RankLoss extends ADecomposableDoubleMeasure<double[]> {

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
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

	@Override
	public Double calculateAvgMeasure(final List<double[]> actual, final List<double[]> expected) {
		double[][] ypred = new double[actual.size()][];
		int[][] ypredint = new int[actual.size()][];
		for (int i = 0; i < actual.size(); i++) {
			ypred[i] = actual.get(i);
			ypredint[i] = Arrays.stream(actual.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		int[][] y = new int[expected.size()][];
		for (int i = 0; i < expected.size(); i++) {
			y[i] = Arrays.stream(expected.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		return Metrics.L_RankLoss(y, ypred);
	}
}
