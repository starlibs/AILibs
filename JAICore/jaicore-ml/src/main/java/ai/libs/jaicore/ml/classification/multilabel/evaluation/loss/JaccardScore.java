package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.core.evaluation.loss.APredictionPerformanceMeasure;

public class JaccardScore extends APredictionPerformanceMeasure<Collection<Object>> implements IMultiLabelClassificationPredictionPerformanceMeasure<Collection<Object>> {

	private ai.libs.jaicore.ml.core.evaluation.loss.JaccardScore instanceScorer;

	public JaccardScore() {
		super();
	}

	@Override
	public double score(final List<Collection<Object>> expected, final List<Collection<Object>> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.instanceScorer.score(expected.get(x), actual.get(x))).average();
		if (!res.isPresent()) {
			throw new IllegalStateException("Could not average the jaccord score.");
		} else {
			return res.getAsDouble();
		}
	}

	@Override
	public double loss(final List<Collection<Object>> actual, final List<Collection<Object>> expected) {
		return 1 - this.score(expected, actual);
	}

}
