package ai.libs.jaicore.ml.core.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.learner.algorithm.IPrediction;
import org.api4.java.ai.ml.core.learner.algorithm.IPredictionBatch;

public class PredictionBatch implements IPredictionBatch {

	private final List<IPrediction> predictionsList;

	public PredictionBatch(final IPrediction[] predictionBatch) {
		this.predictionsList = Arrays.asList(predictionBatch);
	}

	public PredictionBatch(final List<IPrediction> predictionBatch) {
		this.predictionsList = new ArrayList<>(predictionBatch);
	}

	public void add(final IPrediction prediction) {
		this.predictionsList.add(prediction);
	}

	@Override
	public IPrediction get(final int pos) {
		return this.predictionsList.get(pos);
	}

	@Override
	public int getNumPredictions() {
		return this.predictionsList.size();
	}

}
