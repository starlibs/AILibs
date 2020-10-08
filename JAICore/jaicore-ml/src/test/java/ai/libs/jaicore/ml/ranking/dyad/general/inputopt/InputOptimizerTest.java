package ai.libs.jaicore.ml.ranking.dyad.general.inputopt;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.general.DyadRankingInstanceSupplier;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLNetDyadRankerConfiguration;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.zeroshot.inputoptimization.NegIdentityInpOptLoss;
import ai.libs.jaicore.ml.ranking.dyad.learner.zeroshot.inputoptimization.PLNetInputOptimizer;
import ai.libs.jaicore.test.LongTest;

/**
 * Tests input optimizer on a PLNet trained on synthetic data set
 * consisting of dyads (x1, x2, y1, y2) that are ranked based on the score s = x1 + x2 - y1 - y2, where lower is better.
 *
 * @author Michael Braun
 *
 */
public class InputOptimizerTest {

	@Test
	@LongTest
	public void testPLNetInputOptimizer() throws TrainingException, InterruptedException {

		PLNetDyadRanker testnet = new PLNetDyadRanker();
		DyadRankingDataset train = DyadRankingInstanceSupplier.getInputOptTestSet(5, 2000);
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,6,4,4");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_LEARNINGRATE, "0.01");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_SEED, "1");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_ACTIVATION_FUNCTION, "SIGMOID");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_MINI_BATCH_SIZE, "1");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_INTERVAL, "1");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_PATIENCE, "20");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "1.0");
		testnet.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_RETRAIN, "false");
		testnet.fit(train);

		Random rng = new Random(1);
		double[] randDoubles = new double[4];
		for (int i = 0; i < 4; i++) {
			randDoubles[i] = rng.nextGaussian();
		}
		INDArray testinp = Nd4j.create(randDoubles);
		INDArray optimized = new PLNetInputOptimizer().optimizeInput(testnet, testinp, new NegIdentityInpOptLoss(), 0.01, 50, new Pair<>(2, 4));
		assertTrue(optimized.getDouble(0) + optimized.getDouble(1) - optimized.getDouble(0) - optimized.getDouble(1) <= 0.1);
	}
}
