package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.evaluation.loss.APredictionPerformanceMeasure;

public class Hamming extends APredictionPerformanceMeasure<Collection<Object>> implements IMultiLabelClassificationPredictionPerformanceMeasure<Collection<Object>> {

	public Hamming() {
		super();
	}

	@Override
	public double loss(final List<Collection<Object>> actual, final List<Collection<Object>> expected) {
		this.checkConsistency(expected, actual);
		return (double) IntStream.range(0, expected.size()).map(x -> SetUtil.getDisjointSet(actual.get(x), expected.get(x)).size()).sum() / (expected.size() * expected.get(0).size());
	}

	@Override
	public double score(final List<Collection<Object>> expected, final List<Collection<Object>> actual) {
		return 1 - this.loss(actual, expected);
	}

}