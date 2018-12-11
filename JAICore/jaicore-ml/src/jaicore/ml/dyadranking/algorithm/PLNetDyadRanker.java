package jaicore.ml.dyadranking.algorithm;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.ConfigurationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.IPredictiveModelConfiguration;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * A dyad ranker based on a Placket-Luce network.
 * 
 * @author Helena Graf
 *
 */
public class PLNetDyadRanker extends APLDyadRanker {

	@Override
	public void train(IDataset dataset) throws TrainingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDyadRankingInstance predict(IInstance instance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
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

}
