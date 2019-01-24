package jaicore.ml.tsc.classifier;

import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

public class TimeSeriesForestClassifier extends ASimplifiedTSClassifier<Integer> {

	public TimeSeriesForestClassifier(
			ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm) {
		super(algorithm);
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
