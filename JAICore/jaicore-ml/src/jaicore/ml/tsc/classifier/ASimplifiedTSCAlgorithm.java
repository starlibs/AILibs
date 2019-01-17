package jaicore.ml.tsc.classifier;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

public abstract class ASimplifiedTSCAlgorithm<TARGETDOMAIN, CLASSIFIER extends ASimplifiedTSClassifier>
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
	 *            The {@link ASimplifiedTSClassifier} model which is maintained
	 *            during algorithm calls.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ASimplifiedTSClassifier> void setModel(T model) {
		this.model = (CLASSIFIER) model;
	}

	/**
	 * Setter for the data set input used during algorithm calls.
	 * 
	 * @param input
	 *            The {@link TimeSeriesDataset} object (or a subtype) used for the
	 *            model maintenance
	 */
	public void setInput(TimeSeriesDataset input) {
		this.input = input;
	}

	/**
	 * Getter for the data set input used during algorithm calls.
	 */
	@Override
	public TimeSeriesDataset getInput() {
		return this.input;
	}
}
