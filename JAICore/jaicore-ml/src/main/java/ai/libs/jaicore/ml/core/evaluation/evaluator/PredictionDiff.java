package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;


public class PredictionDiff implements IPredictionAndGroundTruthTable<Object, Object> {

	private final List<Object> predictions = new ArrayList<>();
	private final List<Object> groundTruths= new ArrayList<>();

	public PredictionDiff() {
		super();
	}

	public PredictionDiff(final List<?> predictions, final List<?> groundTruths) {
		this();
		if (predictions.size() != groundTruths.size()) {
			throw new IllegalArgumentException("Predictions and ground truths must have the same length!");
		}
		this.predictions.addAll(predictions);
		this.groundTruths.addAll(groundTruths);
	}

	public void addPair(final Object prediction, final Object groundTruth) {
		this.predictions.add(prediction);
		this.groundTruths.add(groundTruth);
	}

	@Override
	public int size() {
		return this.predictions.size();
	}

	@Override
	public Object getPrediction(final int instance) {
		return this.predictions.get(instance);
	}

	@Override
	public Object getGroundTruth(final int instance) {
		return this.groundTruths.get(instance);
	}

	@Override
	public List<Object> getPredictionsAsList() {
		return Collections.unmodifiableList(this.predictions);
	}

	public <T> List<T> getPredictionsAsList(final Class<T> clazz) {
		List<T> cList = new ArrayList<>(this.predictions.size());
		this.predictions.forEach(e -> cList.add((T)e));
		return Collections.unmodifiableList(cList);
	}

	@Override
	public Object[] getPredictionsAsArray() {
		return this.predictions.toArray();
	}

	@Override
	public List<Object> getGroundTruthAsList() {
		return Collections.unmodifiableList(this.groundTruths);
	}

	public <T> List<T> getGroundTruthAsList(final Class<T> clazz) {
		List<T> cList = new ArrayList<>(this.groundTruths.size());
		this.groundTruths.forEach(e -> cList.add((T)e));
		return Collections.unmodifiableList(cList);
	}

	@Override
	public Object[] getGroundTruthAsArray() {
		return null;
	}

}
