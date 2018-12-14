package jaicore.ml.tsc.classifier;

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
 * @param <TARGET>
 *            Type of the target attribute
 * @param <DATASET>
 *            The type of the time series data set used to learn from and
 *            predict batches.
 */
public abstract class TSClassifier<TARGET, DATASET extends TimeSeriesDataset>
		extends ABatchLearner<TARGET, TimeSeriesInstance, DATASET> {

	/**
	 * The algorithm object used for the training of the classifier.
	 */
	protected ATSCAlgorithm<TARGET, DATASET, ? extends TSClassifier<TARGET, DATASET>> algorithm;

	/**
	 * Constructor for a time series classifier.
	 * 
	 * @param algorithm
	 *            The algorithm object used for the training of the classifier
	 */
	public TSClassifier(ATSCAlgorithm<TARGET, DATASET, ? extends TSClassifier<TARGET, DATASET>> algorithm) {
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
	public ATSCAlgorithm<TARGET, DATASET, ? extends IBatchLearner<TARGET, TimeSeriesInstance, DATASET>> getAlgorithm() {
		return algorithm;
	}

	/**
	 * Sets the training algorithm for the classifier.
	 * 
	 * @param algorithm
	 *            The algorithm object used to maintain the model's parameters.
	 */
	public void setAlgorithm(
			ATSCAlgorithm<TARGET, DATASET, ? extends IBatchLearner<TARGET, TimeSeriesInstance, DATASET>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * {@inheritDoc ABatchLearner#predict(jaicore.ml.core.dataset.IDataset)}
	 */
	@Override
	public List<TARGET> predict(DATASET dataset) throws PredictionException {
		final List<TARGET> result = new ArrayList<>();
		for (TimeSeriesInstance inst : dataset) {
			result.add(this.predict(inst));
		}
		return result;
	}

	/**
	 * {@inheritDoc ABatchLearner#getConfiguration()}
	 */
	@Override
	public IPredictiveModelConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc ABatchLearner#setConfiguration(IPredictiveModelConfiguration)}
	 */
	@Override
	public void setConfiguration(IPredictiveModelConfiguration configuration) throws ConfigurationException {
		// TODO Auto-generated method stub

	}
}
