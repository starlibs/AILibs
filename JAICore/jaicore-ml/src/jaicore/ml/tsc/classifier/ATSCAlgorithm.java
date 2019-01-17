package jaicore.ml.tsc.classifier;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.TimeSeriesDataset;

/**
 * Abstract algorithm class which is able to take {@link TimeSeriesDataset}
 * objects and builds {@link TSClassifier} instances specified by the generic
 * parameter <CLASSIFIER>.
 * 
 * @author Julian Lienen
 *
 * @param <TARGETTYPE>
 *            The type of the target that the <CLASSIFIER> to be trained
 * @param <TARGETVALUETYPE>
 *            The value type of the target that the <CLASSIFIER> to be trained
 *            predicts.
 * @param <DATASET>
 *            The type of the time series data set used to learn from and
 *            predict batches.
 * @param <CLASSIFIER>
 *            The time series classifier which is modified and returned as
 *            algorithm result.
 */
public abstract class ATSCAlgorithm<TARGETTYPE, TARGETVALUETYPE, DATASET extends TimeSeriesDataset, CLASSIFIER extends TSClassifier<TARGETTYPE, TARGETVALUETYPE, DATASET>>
		implements IAlgorithm<TimeSeriesDataset, CLASSIFIER> {

	/**
	 * The model which is maintained during algorithm calls
	 */
	protected CLASSIFIER model;

	/**
	 * The {@link TimeSeriesDataset} object used for maintaining the
	 * <code>model</code>.
	 */
	protected TimeSeriesDataset input;

	/**
	 * Setter for the model to be maintained.
	 * 
	 * @param model
	 *            The {@link TSClassifier} model which is maintained during
	 *            algorithm calls.
	 */
	@SuppressWarnings("unchecked")
	public <T extends TSClassifier<TARGETTYPE, TARGETVALUETYPE, DATASET>> void setModel(T model) {
		this.model = (CLASSIFIER) model;
	}

	/**
	 * Setter for the data set input used during algorithm calls.
	 * 
	 * @param input
	 *            The {@link TimeSeriesDataset} object (or a subtype) used for the
	 *            model maintenance
	 */
	public void setInput(DATASET input) {
		this.input = input;
	}

	/**
	 * Getter for the data set input used during algorithm calls.
	 */
	@Override
	public DATASET getInput() {
		return (DATASET) this.input;
	}
}
