package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;

public abstract class AUnboundedRegressionMeasure extends ARegressionMeasure {

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		return -this.score(expected, actual);
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends Double> actual) {
		return -this.loss(expected, actual);
	}
}
