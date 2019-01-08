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
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.FeatureTransformPLDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Class that runs a simple functionality check on all dyad rankers.
 * 
 * @author Helena Graf, Mirko Jürgens, Jonas Hanselle, Michael Braun
 *
 */
@RunWith(Parameterized.class)
public class AdvancedDyadDatasetDyadRankerTester {
	
	public static int SEED = 7;

	ADyadRanker ranker;

	int seedTest = 60;

	public AdvancedDyadDatasetDyadRankerTester(ADyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void trainRanker() throws TrainingException {
		ranker.train(DyadRankingInstanceSupplier.getDyadRankingDataset(10, 1000, SEED));
	}

	@Test
	public void testSwapOrdering1() throws PredictionException {
		System.out.println("Now testing ordering");
		
		int maxDyadRankingLength = 10;
		int nTestInstances = 100;
		double avgKendallTau = 0;
		double avgFailures = 0;
		
		for (int testInst = 0; testInst < nTestInstances; testInst++) {
			IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(maxDyadRankingLength, SEED);
			IDyadRankingInstance predict = ranker.predict(test);
			
			int dyadRankingLength = test.length();
			int nConc = 0;
			int nDisc = 0;
			for (int i = 1; i < dyadRankingLength; i++) {
				for (int j = 0; j < i; j++) {
					if (DyadRankingInstanceSupplier.complexDyadRanker().compare(
							predict.getDyadAtPosition(j), predict.getDyadAtPosition(i)) < 0) {
						nConc++;
					} else {
						nDisc++;
					}
				}
			}
			double kendallTau = 2.0 * (nConc - nDisc) / (dyadRankingLength * (dyadRankingLength - 1) );
			avgKendallTau += kendallTau;
			
			Dyad currentMin = predict.getDyadAtPosition(0);
			int failures = 0;
			for (Dyad dyad : predict) {
				if (!(DyadRankingInstanceSupplier.complexDyadRanker().compare(currentMin, dyad) <= 0)) {		
					failures++;
				} 
				currentMin = dyad;
			}
			avgFailures += failures;
		}
		avgKendallTau /= nTestInstances;
		avgFailures /= nTestInstances;
		
		System.out.println("Kendall's tau: " + avgKendallTau); 
		System.out.println("Found failures: "+ avgFailures);
		
	}

	@Parameters
	public static List<ADyadRanker[]> supplyDyadRankers() {
		PLNetDyadRanker ranker1 = new PLNetDyadRanker();
		ranker1.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "0");
		ranker1.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "6,4,3");
		PLNetDyadRanker ranker2 = new PLNetDyadRanker();
		ranker2.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		ranker2.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "1.0");
		ranker2.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,4");
		
		return Arrays.asList(new PLNetDyadRanker[] {ranker1}, new PLNetDyadRanker[] {ranker2}, new PLNetDyadRanker[] { new PLNetDyadRanker()}, new FeatureTransformPLDyadRanker[] {new FeatureTransformPLDyadRanker()});
	}
}
