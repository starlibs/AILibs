package ai.libs.jaicore.ml.core.evaluation.loss;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceMeasure;

public class AInstanceMeasure<O> implements IInstanceMeasure<O> {

	@Override
	public double loss(final O expected, final O actual) {
		return 1 - this.score(expected, actual);
	}

	@Override
	public double score(final O expected, final O actual) {
		return 1 - this.loss(expected, actual);
	}

}
