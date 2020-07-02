package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.basic.ArrayUtil;
import ai.libs.jaicore.basic.sets.SetUtil;

public class Hamming extends AMultiLabelClassificationMeasure {

	public Hamming() {
		super();
	}

	public Hamming(final double threshold) {
		super(threshold);
	}

	@Override
	public double loss(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);
		return (double) IntStream.range(0, expected.size()).map(x -> SetUtil.getDisjointSet(this.getThresholdedPredictionAsSet(predicted.get(x)), ArrayUtil.argMax(expected.get(x))).size()).sum() / (expected.size() * expected.get(0).length);
	}

}