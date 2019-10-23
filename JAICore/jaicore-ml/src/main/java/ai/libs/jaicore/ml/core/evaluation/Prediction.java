package ai.libs.jaicore.ml.core.evaluation;

import org.api4.java.ai.ml.core.evaluation.IPrediction;

public class Prediction implements IPrediction {

	private final Object predicted;

	public Prediction(final Object predicted) {
		this.predicted = predicted;
	}

	@Override
	public Object getPrediction() {
		return this.predicted;
	}

}
