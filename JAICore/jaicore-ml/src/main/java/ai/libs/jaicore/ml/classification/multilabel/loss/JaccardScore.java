package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;

import ai.libs.jaicore.basic.sets.SetUtil;

public class JaccardScore extends AMultiLabelClassificationMeasure {

	private double jaccardScore(final Collection<String> expected, final Collection<String> actual) {
		return ((double) SetUtil.intersection(expected, actual).size()) / SetUtil.union(expected, actual).size();
	}

	@Override
	public double score(final List<IMultiLabelClassification> expected, final List<IMultiLabelClassification> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.jaccardScore(expected.get(x).getPrediction(), actual.get(x).getPrediction())).average();
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
