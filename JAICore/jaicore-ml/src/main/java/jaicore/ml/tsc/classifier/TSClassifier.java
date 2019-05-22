package jaicore.ml.tsc.classifier;

import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;
import java.util.List;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.ABatchLearner;
import jaicore.ml.core.predictivemodel.IBatchLearner;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;

/**
 * Time series classifier which can be trained and used as a predictor. Uses
 * <code>algorithm</code> to train the model parameters (if necessary).
 * 
 * @author Julian Lienen
 *
 * @param <TARGETTYPE>
 *            The attribute type of the target.
 * @param <TARGETVALUETYPE>
 *            The value type of the target attribute.
 * @param <DATASET>
 *            The type of the time series data set used to learn from and
 *            predict batches.
 */
public abstract class TSClassifier<TARGETTYPE, TARGETVALUETYPE, DATASET extends TimeSeriesDataset<TARGETTYPE>>
		extends ABatchLearner<TARGETTYPE, TARGETVALUETYPE, TimeSeriesInstance<TARGETTYPE>, DATASET> {

	/**
	 * The algorithm object used for the training of the classifier.
	 */
	protected ATSCAlgorithm<TARGETTYPE, TARGETVALUETYPE, DATASET, ? extends TSClassifier<TARGETTYPE, TARGETVALUETYPE, DATASET>> algorithm;

	/**
	 * Constructor for a time series classifier.
	 * 
	 * @param algorithm
	 *            The algorithm object used for the training of the classifier
	 */
	public TSClassifier(
			ATSCAlgorithm<TARGETTYPE, TARGETVALUETYPE, DATASET, ? extends TSClassifier<TARGETTYPE, TARGETVALUETYPE, DATASET>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * {@inheritDoc ABatchLearner#train(jaicore.ml.core.dataset.IDataset)}
	 */
	@Override
	public void train(DATASET dataset) throws TrainingException {
		// Set model which is trained
		this.algorithm.setModel(this);

		// Set input data from which the model should learn
		algorithm.setInput(dataset);
		try {
			// Train
			algorithm.call();
		} catch (Exception e) {
			throw new TrainingException("Could not train model " + this.getClass().getSimpleName(), e);
		}
	}

	/**
	 * Getter for the model's training algorithm object.
	 * 
	 * @return The model's training algorithm
	 */
	public ATSCAlgorithm<TARGETTYPE, TARGETVALUETYPE, DATASET, ? extends IBatchLearner<TARGETVALUETYPE, TimeSeriesInstance<TARGETTYPE>, DATASET>> getAlgorithm() {
		return algorithm;
	}

	/**
	 * Sets the training algorithm for the classifier.
	 * 
	 * @param algorithm
	 *            The algorithm object used to maintain the model's parameters.
	 */
	public void setAlgorithm(
			ATSCAlgorithm<TARGETTYPE, TARGETVALUETYPE, DATASET, ? extends IBatchLearner<TARGETVALUETYPE, TimeSeriesInstance<TARGETTYPE>, DATASET>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * {@inheritDoc ABatchLearner#predict(jaicore.ml.core.dataset.IDataset)}
	 */
	@Override
	public List<TARGETVALUETYPE> predict(DATASET dataset) throws PredictionException {
		final List<TARGETVALUETYPE> result = new ArrayList<>();
		for (TimeSeriesInstance<TARGETTYPE> inst : dataset) {
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
	public void setConfiguration(IPredictiveModelConfiguration configuration) throws ConfigurationException {
		throw new UnsupportedOperationException();
	}
}
