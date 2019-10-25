package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset;

/**
 * Abstract algorithm class which is able to take {@link TimeSeriesDataset}
 * objects and builds {@link ATimeSeriesClassificationModel} instances specified by the generic
 * parameter <CLASSIFIER>.
 *
 * @author Julian Lienen
 *
 * @param <Y>
 *            The type of the target that the <CLASSIFIER> to be trained
 * @param <V>
 *            The value type of the target that the <CLASSIFIER> to be trained
 *            predicts.
 * @param <D>
 *            The type of the time series data set used to learn from and
 *            predict batches.
 * @param <C>
 *            The time series classifier which is modified and returned as
 *            algorithm result.
 */
public abstract class ATSCAlgorithm<Y, D extends TimeSeriesDataset, C extends ATimeSeriesClassificationModel<Y, D>> implements IAlgorithm<TimeSeriesDataset, C> {

	/**
	 * The model which is maintained during algorithm calls
	 */
	protected C model;

	/**
	 * The {@link TimeSeriesDataset} object used for maintaining the
	 * <code>model</code>.
	 */
	protected D input;

	/**
	 * Setter for the model to be maintained.
	 *
	 * @param model
	 *            The {@link ATimeSeriesClassificationModel} model which is maintained during
	 *            algorithm calls.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ATimeSeriesClassificationModel<Y, D>> void setModel(final T model) {
		this.model = (C) model;
	}

	/**
	 * Setter for the data set input used during algorithm calls.
	 *
	 * @param input
	 *            The {@link TimeSeriesDataset} object (or a subtype) used for the
	 *            model maintenance
	 */
	public void setInput(final D input) {
		this.input = input;
	}

	/**
	 * Getter for the data set input used during algorithm calls.
	 */
	@Override
	public D getInput() {
		return this.input;
	}
}
