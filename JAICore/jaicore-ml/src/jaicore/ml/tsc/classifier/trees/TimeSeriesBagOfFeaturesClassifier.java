package jaicore.ml.tsc.classifier.trees;

import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

// Implementation of the TSBF classifier
public class TimeSeriesBagOfFeaturesClassifier extends ASimplifiedTSClassifier<Integer> {

	public TimeSeriesBagOfFeaturesClassifier(final int seed) {
		super(new TimeSeriesBagOfFeaturesAlgorithm(seed));
		// TODO Auto-generated constructor stub
	}


	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}
}
