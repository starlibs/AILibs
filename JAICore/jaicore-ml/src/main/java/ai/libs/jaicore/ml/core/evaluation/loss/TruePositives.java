package ai.libs.jaicore.ml.core.evaluation.loss;

import java.util.stream.IntStream;

public class TruePositives extends AInstanceMeasure<int[]> {

	private final int positiveClass;

	public TruePositives(final int positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public double score(final int[] expected, final int[] actual) {
		return IntStream.range(0, expected.length).filter(i -> expected[i] == this.positiveClass && expected[i] == actual[i]).map(x -> 1).sum();
	}

}