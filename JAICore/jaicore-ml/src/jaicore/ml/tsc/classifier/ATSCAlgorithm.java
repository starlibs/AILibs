package jaicore.ml.tsc.classifier;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;

// TODO: Change to TimeSeriesDataset when available
public abstract class ATSCAlgorithm<TARGET, T extends TSClassifier<TARGET>> implements IAlgorithm<IDataset, T> {

	protected T model;
	protected IDataset input;

	@SuppressWarnings("unchecked")
	public <M extends TSClassifier<TARGET>> void setModel(M model) {
		this.model = (T) model;
	}

	public void setInput(IDataset input) {
		this.input = input;
	}

	@Override
	public IDataset getInput() {
		return this.getInput();
	}
}
