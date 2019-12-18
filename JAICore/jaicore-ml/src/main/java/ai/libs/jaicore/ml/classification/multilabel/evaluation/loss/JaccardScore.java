package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.basic.ArrayUtil;

public class JaccardScore extends AMultiLabelClassificationMeasure {

	private ai.libs.jaicore.ml.classification.loss.dataset.JaccardScore instanceScorer;

	public JaccardScore() {
		super();
	}

	public JaccardScore(final double threshold) {
		super(threshold);
	}

	@Override
	public double score(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.instanceScorer.score(ArrayUtil.argMax(expected.get(x)), this.getThresholdedPredictionAsSet(actual.get(x)))).average();
		if (!res.isPresent()) {
			throw new IllegalStateException("Could not average the jaccord score.");
		} else {
			return res.getAsDouble();
		}
	}

	@Override
	public double loss(final List<? extends int[]> expected, final List<? extends IMultiLabelClassification> actual) {
		return 1 - this.score(expected, actual);
	}

}
