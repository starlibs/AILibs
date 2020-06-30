package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;

public class R2Score extends ARegressionMeasure {

	public R2Score() {
		super();
		// nothing to do here
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends Double> actual) {
		this.checkConsistency(expected, actual);
		double meanExpected = expected.stream().mapToDouble(x -> x).average().getAsDouble();

		double sumOfSquares = 0.0;
		double sumOfActualSquares = 0.0;
		double sumOfExpectedSquares = 0.0;
		for (int i = 0; i < actual.size(); i++) {
			sumOfSquares += Math.pow(expected.get(i) - actual.get(i), 2);
			sumOfActualSquares += Math.pow(actual.get(i) - meanExpected, 2);
			sumOfExpectedSquares += Math.pow(expected.get(i) - meanExpected, 2);
		}

		System.out.println(sumOfSquares + " " + sumOfActualSquares + " " + sumOfExpectedSquares);

		double inverse = 1 - sumOfSquares / sumOfExpectedSquares;
		System.out.println(inverse + " " + (sumOfActualSquares / sumOfExpectedSquares));

		return sumOfActualSquares / sumOfExpectedSquares;
	}

}
