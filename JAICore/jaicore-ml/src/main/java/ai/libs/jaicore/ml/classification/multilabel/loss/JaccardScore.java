package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.Collection;
import java.util.HashSet;

import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

import ai.libs.jaicore.basic.sets.SetUtil;

public class JaccardScore implements ILossFunction<double[]> {

	@Override
	public double loss(final double[] expected, final double[] actual) {
		Collection<Integer> t = new HashSet<>();
		Collection<Integer> p = new HashSet<>();
		int numLabels = actual.length;
		for (int label = 0; label < numLabels; label++) {
			if (actual[label] == 1) {
				t.add(label);
			}
			if (expected[label] == 1) {
				p.add(label);
			}
		}
		return ((double) SetUtil.intersection(t, p).size()) / SetUtil.union(t, p).size();
	}

}
