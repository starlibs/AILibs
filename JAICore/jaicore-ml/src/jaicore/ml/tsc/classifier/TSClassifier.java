package jaicore.ml.tsc.classifier;

import java.util.List;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.IBatchLearner;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;

public abstract class TSClassifier<T> implements IBatchLearner<T>{
	
	protected IAlgorithm<IDataset, ? extends IBatchLearner<T>> algorithm;
	protected IBatchLearner<T> model;
	
	@Override
	public void train(IDataset dataset) throws TrainingException {
		// TODO Auto-generated method stub
		try {
			model = algorithm.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public List<T> predict(IDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		return model.predict(dataset);
	}
	
	@Override
	public T predict(IInstance instance) throws PredictionException {
		// TODO Auto-generated method stub
		return model.predict(instance);
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

	public IAlgorithm<IDataset, ? extends IBatchLearner<T>> getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(IAlgorithm<IDataset, ? extends IBatchLearner<T>> algorithm) {
		this.algorithm = algorithm;
	}
	
	
}
