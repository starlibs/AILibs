package jaicore.ml.tsc.classifier;

import java.util.List;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.exception.PredictionException;

public class BOSSClassifier extends TSClassifier<CategoricalAttributeType> {

	// TODO: Model parameters

	public BOSSClassifier() {
		super(new BOSSAlgorithm());
	}

	@Override
	public CategoricalAttributeType predict(IInstance instance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CategoricalAttributeType> predict(IDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}
}
