package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;
import java.util.stream.IntStream;

public class FalsePositives extends AHomogeneousPredictionPerformanceMeasure<Object> {

	private final Object positiveClass;

	public FalsePositives(final Object positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public double score(final List<?> expected, final List<?> actual) {
		return IntStream.range(0, expected.size()).filter(i -> !expected.get(i).equals(this.positiveClass) && !expected.get(i).equals(actual.get(i))).map(x -> 1).sum();
	}
}
