package ai.libs.jaicore.ml.dataset;

import org.api4.java.ai.ml.learner.predict.IPrediction;

public class Prediction<Y> implements IPrediction<Y> {

	private final Y predicted;

	public Prediction(final Y predicted) {
		this.predicted = predicted;
	}

	@Override
	public Y getPrediction() {
		return this.predicted;
	}

}
