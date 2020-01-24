package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.choquistic;

import java.util.Collection;

public interface IMassFunction {

	public double mu(final Collection<Double> cis, final int m);

}
