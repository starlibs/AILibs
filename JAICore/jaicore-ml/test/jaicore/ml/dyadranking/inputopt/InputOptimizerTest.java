package jaicore.ml.dyadranking.inputopt;

import java.util.Random;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;
import jaicore.ml.dyadranking.inputoptimization.NegIdentityInpOptLoss;
import jaicore.ml.dyadranking.inputoptimization.PLNetInputOptimizer;

public class InputOptimizerTest {

	@Test
	public void testPLNetInputOptimizer() throws TrainingException, PredictionException {
		PLNetDyadRanker testnet = new PLNetDyadRanker();
		IDataset train = DyadRankingInstanceSupplier.getInputOptTestSet(5, 2000);
		testnet.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "8,6,4,4");
		testnet.train(train);
		
		int maxDyadRankingLength = 5;
		int nTestInstances = 100;
		double avgKendallTau = 0;
		
		for (int testInst = 0; testInst < nTestInstances; testInst++) {
			IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(maxDyadRankingLength, 2, 2, DyadRankingInstanceSupplier.inputOptimizerTestRanker());
			IDyadRankingInstance predict = testnet.predict(test);
			
			int dyadRankingLength = test.length();
			int nConc = 0;
			int nDisc = 0;
			for (int i = 1; i < dyadRankingLength; i++) {
				for (int j = 0; j < i; j++) {
					if (DyadRankingInstanceSupplier.inputOptimizerTestRanker().compare(
							predict.getDyadAtPosition(j), predict.getDyadAtPosition(i)) <= 0) {
						nConc++;
					} else {
						nDisc++;
					}
				}
			}
			double kendallTau = 2.0 * (nConc - nDisc) / (dyadRankingLength * (dyadRankingLength - 1) );
			avgKendallTau += kendallTau;
			
		}
		avgKendallTau /= nTestInstances;
		
		System.out.println("Kendall's tau: " + avgKendallTau); 
		
		PLNetInputOptimizer inpopt = new PLNetInputOptimizer(testnet);	
		Random rng = new Random(1);
		double[] randDoubles = new double[4];
		for (int i = 0; i < 4; i++) {
			randDoubles[i] = rng.nextGaussian();
		}
		INDArray testinp = Nd4j.create(randDoubles); 
		INDArray optimized = inpopt.optimizeInput(testinp, new NegIdentityInpOptLoss(), 0.01, 50, new Pair<Integer, Integer>(2,4));
		System.out.println("Optimized input: " + optimized);
	}
}
