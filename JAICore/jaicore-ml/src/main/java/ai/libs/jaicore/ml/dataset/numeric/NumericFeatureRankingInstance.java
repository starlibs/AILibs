package ai.libs.jaicore.ml.dataset.numeric;

import org.api4.java.ai.ml.IRanking;
import org.api4.java.ai.ml.dataset.supervised.ranking.INumericFeatureRankingInstance;

public class NumericFeatureRankingInstance<O> extends ANumericFeatureInstance implements INumericFeatureRankingInstance<O> {

	private final IRanking<O> label;

	protected NumericFeatureRankingInstance(final double[] features, final IRanking<O> label) {
		super(features);
		this.label = label;
	}

	@Override
	public IRanking<O> getLabel() {
		return this.label;
	}

}
