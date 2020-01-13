package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;

import ai.libs.jaicore.basic.sets.ListView;

public class PredictionDiff<E, A> implements IPredictionAndGroundTruthTable<E, A> {

	private final Class<?> expectedClass;
	private final Class<?> predictionClass;
	private final List<E> groundTruths = new ArrayList<>();
	private final List<A> predictions = new ArrayList<>();

	public PredictionDiff() {
		super();
		Type genericSuperClass = this.getClass().getGenericSuperclass();
		this.expectedClass = (genericSuperClass instanceof ParameterizedType) ? (Class<E>)((ParameterizedType)genericSuperClass).getActualTypeArguments()[0].getClass() : Object.class;
		this.predictionClass = (genericSuperClass instanceof ParameterizedType) ? (Class<A>)((ParameterizedType)genericSuperClass).getActualTypeArguments()[1].getClass() : Object.class;
	}

	public PredictionDiff(final List<? extends E> groundTruths, final List<? extends A> predictions) {
		this();
		if (predictions.size() != groundTruths.size()) {
			throw new IllegalArgumentException("Predictions and ground truths must have the same length!");
		}
		this.predictions.addAll(predictions);
		this.groundTruths.addAll(groundTruths);
	}

	public void addPair(final E groundTruth, final A prediction) {
		this.groundTruths.add(groundTruth);
		this.predictions.add(prediction);
	}

	@Override
	public <E1, A1> PredictionDiff<E1, A1> getCastedView(final Class<E1> expectedClass, final Class<A1> actualClass) {
		return new PredictionDiff<>(new ListView<E1>(this.groundTruths), new ListView<A1>(this.predictions));
	}

	@Override
	public int size() {
		return this.predictions.size();
	}

	@Override
	public A getPrediction(final int instance) {
		return this.predictions.get(instance);
	}

	@Override
	public E getGroundTruth(final int instance) {
		return this.groundTruths.get(instance);
	}

	@Override
	public List<A> getPredictionsAsList() {
		return Collections.unmodifiableList(this.predictions);
	}

	public <T> List<T> getPredictionsAsList(final Class<T> clazz) {
		return Collections.unmodifiableList(new ListView<T>(this.predictions, clazz));
	}

	@Override
	public A[] getPredictionsAsArray() {
		return this.predictions.toArray((A[])Array.newInstance(this.predictionClass, this.predictions.size()));
	}

	@Override
	public List<E> getGroundTruthAsList() {
		return Collections.unmodifiableList(this.groundTruths);
	}

	public <T> List<T> getGroundTruthAsList(final Class<T> clazz) {
		return Collections.unmodifiableList(new ListView<T>(this.groundTruths, clazz));
	}

	@Override
	public E[] getGroundTruthAsArray() {
		return this.groundTruths.toArray((E[])Array.newInstance(this.expectedClass, this.groundTruths.size()));
	}

}
