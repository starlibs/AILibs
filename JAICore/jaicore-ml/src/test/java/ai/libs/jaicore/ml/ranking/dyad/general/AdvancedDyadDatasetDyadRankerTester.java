package ai.libs.jaicore.ml.ranking.dyad.general;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.loss.KendallsTauDyadRankingLoss;
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

	public AdvancedDyadDatasetDyadRankerTester(final IDyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void trainRanker() throws TrainingException, InterruptedException {
		DyadRankingDataset drTrain = DyadRankingInstanceSupplier.getDyadRankingDataset(55, 200);
		this.ranker.fit(drTrain);
	}

	@Test
	@Ignore
	public void testSwapOrdering1() throws PredictionException, InterruptedException {

		int maxDyadRankingLength = 4;
		int nTestInstances = 100;
		double avgKendallTau = 0;

		for (int testInst = 0; testInst < nTestInstances; testInst++) {
			IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(maxDyadRankingLength, SEED);
			IPrediction predict = this.ranker.predict(test);

			double kendallTau = new KendallsTauDyadRankingLoss().loss(test.getLabel(), (IRanking<IDyad>) predict.getPrediction());

			avgKendallTau += kendallTau;
		}
		avgKendallTau /= nTestInstances;

		Assert.assertTrue(avgKendallTau >= 0.5d);
	}

	@Parameters
	public static List<IPLDyadRanker[]> supplyDyadRankers() {
		PLNetDyadRanker ranker1 = new PLNetDyadRanker();
		//		ranker1.getConfig().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "0");
		//		ranker1.getConfig().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8");
		//		ranker1.getConfig().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		PLNetDyadRanker ranker2 = new PLNetDyadRanker();
		//		ranker2.getConfig().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		//		ranker2.getConfig().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "1.0");
		//		ranker2.getConfig().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,4");

		return Arrays.asList(new PLNetDyadRanker[] { ranker1 }, new PLNetDyadRanker[] { ranker2 }, new PLNetDyadRanker[] { new PLNetDyadRanker() });
	}
}
