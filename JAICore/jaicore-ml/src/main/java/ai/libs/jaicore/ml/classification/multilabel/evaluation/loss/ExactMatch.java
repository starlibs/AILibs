package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.collections4.SetUtils;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.basic.ArrayUtil;

public class ExactMatch extends AMultiLabelClassificationMeasure {

	public ExactMatch() {
		super();
	}

	public ExactMatch(final double threshold) {
		super(threshold);
	}

	@Override
	public double loss(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> predicted) {
		this.checkConsistency(expected, predicted);
		return (double) IntStream.range(0, expected.size()).map(x -> SetUtils.isEqualSet(ArrayUtil.argMax(expected.get(x)), this.getThresholdedPredictionAsSet(predicted.get(x))) ? 0 : 1).sum() / expected.size();
	}

}
