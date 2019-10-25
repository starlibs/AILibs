package ai.libs.jaicore.ml.classification.singlelabel.learner.timeseries;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.core.timeseries.dataset.ITimeSeriesInstance;
import ai.libs.jaicore.ml.core.timeseries.dataset.TimeSeriesDataset;

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
public abstract class ATimeSeriesClassificationModel<L, D extends TimeSeriesDataset> extends ASupervisedLearner<ITimeSeriesInstance, D, ISingleLabelClassificationPrediction, ISingleLabelClassificationPredictionBatch> {

	/**
	 * The algorithm object used for the training of the classifier.
	 */
	protected ATSCAlgorithm<L, D, ? extends ATimeSeriesClassificationModel<L, D>> algorithm;

	/**
	 * Constructor for a time series classifier.
	 *
	 * @param algorithm
	 *            The algorithm object used for the training of the classifier
	 */
	public ATimeSeriesClassificationModel(final ATSCAlgorithm<L, D, ? extends ATimeSeriesClassificationModel<L, D>> algorithm) {
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
	public ATSCAlgorithm<L, D, ? extends ATimeSeriesClassificationModel<L, D>> getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Sets the training algorithm for the classifier.
	 *
	 * @param algorithm
	 *            The algorithm object used to maintain the model's parameters.
	 */
	public void setAlgorithm(final ATSCAlgorithm<L, D, ? extends ATimeSeriesClassificationModel<L, D>> algorithm) {
		this.algorithm = algorithm;
	}

}
