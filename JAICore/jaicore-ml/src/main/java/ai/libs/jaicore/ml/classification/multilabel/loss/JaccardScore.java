package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

public class JaccardScore extends AMultiLabelClassificationMeasure {

	private ai.libs.jaicore.ml.core.evaluation.loss.JaccardScore instanceScorer;

	public JaccardScore() {
		super();
	}

	public JaccardScore(final double threshold) {
		super(threshold);
	}

	@Override
	public double score(final List<IMultiLabelClassification> expected, final List<IMultiLabelClassification> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.instanceScorer.score(expected.get(x).getPrediction(), actual.get(x).getPrediction())).average();
		if (!res.isPresent()) {
			throw new IllegalStateException("Could not average the jaccord score.");
		} else {
			return res.getAsDouble();
		}
	}

	@Override
	public double loss(final List<IMultiLabelClassification> actual, final List<IMultiLabelClassification> expected) {
		return 1 - this.score(expected, actual);
	}

}
