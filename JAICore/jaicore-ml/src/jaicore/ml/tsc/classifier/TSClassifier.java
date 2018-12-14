package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.List;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.ABatchLearner;
import jaicore.ml.core.predictivemodel.IBatchLearner;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;

/**
 * Time series classifier which can be trained and used as a predictor. Uses
 * <code>algorithm</code> to train the model parameters (if necessary).
 * 
 * @author Julian Lienen
 *
 * @param <TARGET>
 *            Type of the target attribute
 */
public abstract class TSClassifier<TARGET> extends ABatchLearner<TARGET> {

	protected ATSCAlgorithm<TARGET, ? extends TSClassifier<TARGET>> algorithm;

	public TSClassifier(ATSCAlgorithm<TARGET, ? extends TSClassifier<TARGET>> algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public void train(IDataset dataset) throws TrainingException {
		this.algorithm.setModel(this);

		// TODO Auto-generated method stub
		try {
			algorithm.setInput(dataset);
			algorithm.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IPredictiveModelConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfiguration(IPredictiveModelConfiguration configuration) throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	public ATSCAlgorithm<TARGET, ? extends IBatchLearner<TARGET>> getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(ATSCAlgorithm<TARGET, ? extends IBatchLearner<TARGET>> algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public List<TARGET> predict(IDataset dataset) throws PredictionException {
		final List<TARGET> result = new ArrayList<>();
		for (IInstance inst : dataset) {
			result.add(this.predict(inst));
		}
		return result;
	}
}
