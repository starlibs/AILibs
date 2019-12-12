package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.collections4.SetUtils;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

public class ExactMatch extends AMultiLabelClassificationMeasure {

	public ExactMatch() {
		super();
	}

	public ExactMatch(final double threshold) {
		super(threshold);
	}

	@Override
	public double loss(final List<IMultiLabelClassification> actual, final List<IMultiLabelClassification> expected) {
		this.checkConsistency(expected, actual);
		return (double) IntStream.range(0, expected.size()).map(x -> SetUtils.isEqualSet(expected.get(x).getPrediction(), actual.get(x).getPrediction()) ? 0 : 1).sum() / expected.size();
	}

	@Override
	public double score(final List<IMultiLabelClassification> expected, final List<IMultiLabelClassification> actual) {
		return 1 - this.loss(actual, expected);
	}

}
