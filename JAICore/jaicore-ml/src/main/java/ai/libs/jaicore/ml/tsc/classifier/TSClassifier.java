package ai.libs.jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.algorithm.PredictionException;
import org.api4.java.ai.ml.algorithm.TrainingException;
import org.api4.java.ai.ml.algorithm.predictivemodel.IBatchLearner;
import org.api4.java.ai.ml.algorithm.predictivemodel.IPredictiveModelConfiguration;

import ai.libs.jaicore.ml.core.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.core.dataset.TimeSeriesInstance;
import ai.libs.jaicore.ml.core.predictivemodel.ABatchLearner;

/**
 * Time series classifier which can be trained and used as a predictor. Uses
 * <code>algorithm</code> to train the model parameters (if necessary).
 *
 * @author Julian Lienen
 *
 * @param <L>
 *            The attribute type of the target.
 * @param <V>
 *            The value type of the target attribute.
 * @param <D>
 *            The type of the time series data set used to learn from and
 *            predict batches.
 */
public abstract class TSClassifier<L, V, D extends TimeSeriesDataset<L>> extends ABatchLearner<L, V, TimeSeriesInstance<L>, D> {

	/**
	 * The algorithm object used for the training of the classifier.
	 */
	protected ATSCAlgorithm<L, V, D, ? extends TSClassifier<L, V, D>> algorithm;

	/**
	 * Constructor for a time series classifier.
	 *
	 * @param algorithm
	 *            The algorithm object used for the training of the classifier
	 */
	public TSClassifier(final ATSCAlgorithm<L, V, D, ? extends TSClassifier<L, V, D>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * {@inheritDoc ABatchLearner#train(jaicore.ml.core.dataset.IDataset)}
	 */
	@Override
	public void train(final D dataset) throws TrainingException {
		// Set model which is trained
		this.algorithm.setModel(this);

		// Set input data from which the model should learn
		this.algorithm.setInput(dataset);
		try {
			// Train
			this.algorithm.call();
		} catch (Exception e) {
			throw new TrainingException("Could not train model " + this.getClass().getSimpleName(), e);
		}
	}

	/**
	 * Getter for the model's training algorithm object.
	 *
	 * @return The model's training algorithm
	 */
	public ATSCAlgorithm<L, V, D, ? extends IBatchLearner<V, TimeSeriesInstance<L>, D>> getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Sets the training algorithm for the classifier.
	 *
	 * @param algorithm
	 *            The algorithm object used to maintain the model's parameters.
	 */
	public void setAlgorithm(final ATSCAlgorithm<L, V, D, ? extends IBatchLearner<V, TimeSeriesInstance<L>, D>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * {@inheritDoc ABatchLearner#predict(jaicore.ml.core.dataset.IDataset)}
	 */
	@Override
	public List<V> predict(final D dataset) throws PredictionException {
		final List<V> result = new ArrayList<>();
		for (TimeSeriesInstance<L> inst : dataset) {
			result.add(this.predict(inst));
		}
		return result;
	}

	/**
	 * {@inheritDoc ABatchLearner#getConfiguration()}
	 */
	@Override
	public IPredictiveModelConfiguration getConfiguration() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc ABatchLearner#setConfiguration(IPredictiveModelConfiguration)}
	 */
	@Override
	public void setConfiguration(final IPredictiveModelConfiguration configuration) {
		throw new UnsupportedOperationException();
	}
}
