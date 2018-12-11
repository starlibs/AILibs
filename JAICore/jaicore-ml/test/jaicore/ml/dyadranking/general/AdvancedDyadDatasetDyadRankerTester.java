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
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Class that runs a simple functionality check on all dyad rankers.
 * 
 * @author Helena Graf, Mirko Jürgens, Jonas Hanselle, Michael Braun
 *
 */
@RunWith(Parameterized.class)
public class AdvancedDyadDatasetDyadRankerTester {

	ADyadRanker ranker;

	int seedTest = 60;

	public AdvancedDyadDatasetDyadRankerTester(ADyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void trainRanker() throws TrainingException {
		ranker.train(DyadRankingInstanceSupplier.getDyadRankingDataset(100, 100));
	}

	@Test
	public void testSwapOrdering1() throws PredictionException {
		System.out.println("Now testing ordering");

		IDyadRankingInstance test = DyadRankingInstanceSupplier.getDyadRankingInstance(100, seedTest);
		IDyadRankingInstance predict = ranker.predict(test);

		Dyad currentMin = predict.getDyadAtPosition(0);
		int failures = 0;
		for (Dyad dyad : predict) {
			if (!(DyadRankingInstanceSupplier.complexDyadRanker().compare(currentMin, dyad) <= 0)) {
				failures++;
			}
			currentMin = dyad;
		}
		System.out.println("Found failures: " + failures);

	}

	@Parameters
	public static List<ADyadRanker> supplyDyadRankers() {
		return Arrays.asList(new FeatureTransformPLDyadRanker());
	}
}
