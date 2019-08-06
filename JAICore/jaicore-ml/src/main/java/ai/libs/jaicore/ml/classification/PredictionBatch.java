package ai.libs.jaicore.ml.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.learner.predict.IPrediction;
import org.api4.java.ai.ml.learner.predict.IPredictionBatch;

public class PredictionBatch<Y> implements IPredictionBatch<Y> {

	private final List<IPrediction<Y>> predictionsList;

	public PredictionBatch(final IPrediction<Y>[] predictionBatch) {
		this.predictionsList = Arrays.asList(predictionBatch);
	}

	public PredictionBatch(final List<IPrediction<Y>> predictionBatch) {
		this.predictionsList = new ArrayList<>(predictionBatch);
	}

	public void add(final IPrediction<Y> prediction) {
		this.predictionsList.add(prediction);
	}

	@Override
	public IPrediction<Y> get(final int pos) {
		return this.predictionsList.get(pos);
	}

	@Override
	public int getNumPredictions() {
		return this.predictionsList.size();
	}

}
