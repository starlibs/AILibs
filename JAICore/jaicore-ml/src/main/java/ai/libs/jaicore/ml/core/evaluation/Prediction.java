package ai.libs.jaicore.ml.core.evaluation;

import java.util.Map;

import org.api4.java.ai.ml.core.evaluation.IPrediction;

public class Prediction implements IPrediction {

	private final Object predicted;
	private static final String MSG_NOSUPPORT = "Dumb jaicore-ml prediction objects don't support probabilistic predictions yet.";

	public Prediction(final Object predicted) {
		this.predicted = predicted;
	}

	@Override
	public Object getPrediction() {
		return this.predicted;
	}

	@Override
	public Object getLabelWithHighestProbability() {
		throw new UnsupportedOperationException(MSG_NOSUPPORT);
	}

	@Override
	public Map<?, Double> getClassDistribution() {
		throw new UnsupportedOperationException(MSG_NOSUPPORT);
	}

	@Override
	public Map<?, Double> getClassConfidence() {
		throw new UnsupportedOperationException(MSG_NOSUPPORT);
	}

	@Override
	public double getProbabilityOfLabel(final Object label) {
		throw new UnsupportedOperationException(MSG_NOSUPPORT);
	}

}
