package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.basic.sets.SetUtil;

public class Hamming extends AMultiLabelClassificationMeasure {

	public Hamming() {
		super();
	}

	public Hamming(final double threshold) {
		super(threshold);
	}

	@Override
	public double loss(final List<IMultiLabelClassification> actual, final List<IMultiLabelClassification> expected) {
		this.checkConsistency(expected, actual);
		return (double) IntStream.range(0, expected.size()).map(x -> SetUtil.getDisjointSet(actual.get(x).getPrediction(), expected.get(x).getPrediction()).size()).sum() / (expected.size() * expected.get(0).getLabelSet().size());
	}

	@Override
	public double score(final List<IMultiLabelClassification> expected, final List<IMultiLabelClassification> actual) {
		return 1 - this.loss(actual, expected);
	}

}