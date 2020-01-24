package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;
import java.util.stream.IntStream;

public class FalseNegatives extends AHomogeneousPredictionPerformanceMeasure<Object> {

	private final Object positiveClass;

	public FalseNegatives(final Object positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public double score(final List<? extends Object> expected, final List<? extends Object> actual) {
		return IntStream.range(0, expected.size()).filter(i -> expected.get(i).equals(this.positiveClass) && !expected.get(i).equals(actual.get(i))).map(x -> 1).sum();
	}

}
