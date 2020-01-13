package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.choquistic;

import java.util.Collection;

public class HammingMassFunction implements IMassFunction {

	@Override
	public double mu(final Collection<Double> cis, final int m) {
		return (double) cis.size() / m;
	}

}
