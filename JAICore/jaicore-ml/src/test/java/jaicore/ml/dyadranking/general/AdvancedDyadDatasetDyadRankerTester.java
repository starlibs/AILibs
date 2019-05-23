package jaicore.ml.dyadranking.general;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.IDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import junit.framework.Assert;

/**
 * Class that runs a simple functionality check on all dyad rankers.
 * 
 * @author Helena Graf, Mirko Jürgens, Jonas Hanselle, Michael Braun
 *
 */
@RunWith(Parameterized.class)
public class AdvancedDyadDatasetDyadRankerTester {
	
	private static final int SEED = 7;

	IDyadRanker ranker;

	int seedTest = 60;

	public AdvancedDyadDatasetDyadRankerTester(IDyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void trainRanker() throws TrainingException {
		DyadRankingDataset drTrain = DyadRankingInstanceSupplier.getDyadRankingDataset(55, 200);
		ranker.train(drTrain);
	}

	@Test
	public void testSwapOrdering1() throws PredictionException {
		
		int maxDyadRankingLength = 4;
		int nTestInstances = 100;
		double avgKendallTau = 0;
		
		for (int testInst = 0; testInst < nTestInstances; testInst++) {
			IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(maxDyadRankingLength, SEED);
			IDyadRankingInstance predict = ranker.predict(test);
			
			double kendallTau = new KendallsTauDyadRankingLoss().loss(test, predict);
			
			avgKendallTau += kendallTau;
		}
		avgKendallTau /= nTestInstances;
		
		Assert.assertTrue(avgKendallTau >= 0.5d);		
	}

	@Parameters
	public static List<IPLDyadRanker[]> supplyDyadRankers() {
		PLNetDyadRanker ranker1 = new PLNetDyadRanker();
		ranker1.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "0");
		ranker1.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8");
		ranker1.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		PLNetDyadRanker ranker2 = new PLNetDyadRanker();
		ranker2.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		ranker2.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "1.0");
		ranker2.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,4");
		
		return Arrays.asList(new PLNetDyadRanker[] {ranker1}, new PLNetDyadRanker[] {ranker2}, new PLNetDyadRanker[] { new PLNetDyadRanker()});
	}
}
