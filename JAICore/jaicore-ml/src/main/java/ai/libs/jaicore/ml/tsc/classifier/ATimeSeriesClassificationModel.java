package ai.libs.jaicore.ml.tsc.classifier;

import org.api4.java.ai.ml.learner.ILearnerConfig;
import org.api4.java.ai.ml.learner.fit.TrainingException;

import ai.libs.jaicore.ml.classification.ASupervisedLearner;
import ai.libs.jaicore.ml.core.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.core.dataset.TimeSeriesInstance;
import ai.libs.jaicore.ml.core.dataset.attribute.timeseries.INDArrayTimeseries;

/**
 * Time series classifier which can be trained and used as a predictor. Uses
 * <code>algorithm</code> to train the model parameters (if necessary).
 *
 * @author Julian Lienen
 *
 * @param <Y>
 *            The attribute type of the target.
 * @param <V>
 *            The value type of the target attribute.
 * @param <D>
 *            The type of the time series data set used to learn from and
 *            predict batches.
 */
public abstract class ATimeSeriesClassificationModel<Y, D extends TimeSeriesDataset<Y>> extends ASupervisedLearner<ILearnerConfig, INDArrayTimeseries, Y, TimeSeriesInstance<Y>, D> {

	/**
	 * The algorithm object used for the training of the classifier.
	 */
	protected ATSCAlgorithm<Y, D, ? extends ATimeSeriesClassificationModel<Y, D>> algorithm;

	/**
	 * Constructor for a time series classifier.
	 *
	 * @param algorithm
	 *            The algorithm object used for the training of the classifier
	 */
	public ATimeSeriesClassificationModel(final ATSCAlgorithm<Y, D, ? extends ATimeSeriesClassificationModel<Y, D>> algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * {@inheritDoc ABatchLearner#train(jaicore.ml.core.dataset.IDataset)}
	 */
	@Override
	public void fit(final D dataset) throws TrainingException {
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
	public ATSCAlgorithm<Y, D, ? extends ATimeSeriesClassificationModel<Y, D>> getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Sets the training algorithm for the classifier.
	 *
	 * @param algorithm
	 *            The algorithm object used to maintain the model's parameters.
	 */
	public void setAlgorithm(final ATSCAlgorithm<Y, D, ? extends ATimeSeriesClassificationModel<Y, D>> algorithm) {
		this.algorithm = algorithm;
	}

}
