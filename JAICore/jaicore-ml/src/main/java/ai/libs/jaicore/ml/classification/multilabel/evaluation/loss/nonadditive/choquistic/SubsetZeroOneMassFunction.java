package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.choquistic;

import java.util.Collection;

public class SubsetZeroOneMassFunction implements IMassFunction {

	@Override
	public double mu(final Collection<Double> cis, final int m) {
		if (cis.size() == m) {
			return 1;
		} else {
			return 0;
		}
	}

}
