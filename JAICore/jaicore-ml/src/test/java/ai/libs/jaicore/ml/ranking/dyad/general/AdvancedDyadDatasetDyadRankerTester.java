package ai.libs.jaicore.ml.ranking.dyad.general;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLNetDyadRankerConfiguration;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.loss.KendallsTauDyadRankingLoss;

/**
 * Class that runs a simple functionality check on all dyad rankers.
 *
 * @author Helena Graf, Mirko Jürgens, Jonas Hanselle, Michael Braun
 *
 */
public class AdvancedDyadDatasetDyadRankerTester {

	public static Stream<Arguments> supplyDyadRankers() {
		PLNetDyadRanker ranker1 = new PLNetDyadRanker();
		ranker1.getConfig().put(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "0");
		ranker1.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8");
		ranker1.getConfig().put(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		PLNetDyadRanker ranker2 = new PLNetDyadRanker();
		ranker2.getConfig().put(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		ranker2.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "1.0");
		ranker2.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,4");
		return Stream.of(Arguments.of(ranker1), Arguments.of(ranker2), Arguments.of(new PLNetDyadRanker()));
	}

	private static final int SEED = 7;

	@Disabled
	@ParameterizedTest
	@MethodSource("supplyDyadRankers")
	public void testSwapOrdering1(final IPLDyadRanker ranker) throws PredictionException, InterruptedException, TrainingException {

		ranker.fit(DyadRankingInstanceSupplier.getDyadRankingDataset(55, 200));

		int maxDyadRankingLength = 4;
		int nTestInstances = 100;
		double avgKendallTau = 0;

		for (int testInst = 0; testInst < nTestInstances; testInst++) {
			IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(maxDyadRankingLength, SEED);
			IPrediction predict = ranker.predict(test);

			double kendallTau = new KendallsTauDyadRankingLoss().loss(test.getLabel(), (IRanking<IDyad>) predict.getPrediction());

			avgKendallTau += kendallTau;
		}
		avgKendallTau /= nTestInstances;

		assertTrue(avgKendallTau >= 0.5d);
	}
}
