package ai.libs.jaicore.ml.core.evaluation;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;

public class PredictionBatch implements IPredictionBatch {

	private final List<? extends IPrediction> predictions;

	public PredictionBatch(final List<? extends IPrediction> predictions) {
		super();
		this.predictions = predictions;
	}

	public <I extends IPrediction> PredictionBatch(final I[] predictions) {
		super();
		this.predictions = Arrays.asList(predictions);
	}

	@Override
	public List<? extends IPrediction> getPredictions() {
		return this.predictions;
	}

	@Override
	public int getNumPredictions() {
		return this.predictions.size();
	}

	@Override
	public IPrediction get(final int index) {
		return this.predictions.get(index);
	}

}
