package ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.api4.java.ai.ml.ranking.IRanking;

public class Ranking<O> extends ArrayList<O> implements IRanking<O> {

	private static final long serialVersionUID = 6925500382758165610L;
	private static final String MSG_NO_PROBABILITIES = "Ranking predictions are not equipped with probabilities by default.";

	public Ranking(final Collection<O> items) {
		super(items);
	}

	public Ranking() {
		super();
	}

	@Override
	public Ranking<O> getPrediction() {
		return this;
	}

	@Override
	public Object getLabelWithHighestProbability() {
		throw new UnsupportedOperationException(MSG_NO_PROBABILITIES);
	}

	@Override
	public Map<?, Double> getClassDistribution() {
		throw new UnsupportedOperationException(MSG_NO_PROBABILITIES);
	}

	@Override
	public Map<?, Double> getClassConfidence() {
		throw new UnsupportedOperationException(MSG_NO_PROBABILITIES);
	}

	@Override
	public double getProbabilityOfLabel(final Object label) {
		throw new UnsupportedOperationException(MSG_NO_PROBABILITIES);
	}
}
