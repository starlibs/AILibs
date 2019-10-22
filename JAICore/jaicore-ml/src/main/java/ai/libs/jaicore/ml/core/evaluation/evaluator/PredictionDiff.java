package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.classification.execution.IClassificationPredictionAndGroundTruthTable;


public class PredictionDiff<T> implements IClassificationPredictionAndGroundTruthTable {

	private final Class<?> classInGeneric;
	private final List<T> predictions = new ArrayList<>();
	private final List<T> groundTruths= new ArrayList<>();

	public PredictionDiff() {
		super();
		this.classInGeneric = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getClass();
	}

	public PredictionDiff(final List<? extends T> predictions, final List<? extends T> groundTruths) {
		this();
		if (predictions.size() != groundTruths.size()) {
			throw new IllegalArgumentException("Predictions and ground truths must have the same length!");
		}
		this.predictions.addAll(predictions);
		this.groundTruths.addAll(groundTruths);
	}

	public void addPair(final T prediction, final T groundTruth) {
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
	public List<T> getPredictionsAsList() {
		return Collections.unmodifiableList(this.predictions);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] getPredictionsAsArray() {
		return this.predictions.toArray((T[])Array.newInstance(this.classInGeneric, this.predictions.size()));
	}

	@Override
	public List<T> getGroundTruthAsList() {
		return Collections.unmodifiableList(this.groundTruths);
	}

	@Override
	public T[] getGroundTruthAsArray() {
		return null;
	}

}
