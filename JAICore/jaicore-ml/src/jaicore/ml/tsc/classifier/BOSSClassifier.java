package jaicore.ml.tsc.classifier;

import java.util.List;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.TimeSeriesInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.exception.PredictionException;

public class BOSSClassifier extends TSClassifier<CategoricalAttributeType, TimeSeriesDataset> {

	// TODO: Model parameters

	public BOSSClassifier() {
		super(new BOSSAlgorithm());
	}

	@Override
	public CategoricalAttributeType predict(TimeSeriesInstance instance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CategoricalAttributeType> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}
}
