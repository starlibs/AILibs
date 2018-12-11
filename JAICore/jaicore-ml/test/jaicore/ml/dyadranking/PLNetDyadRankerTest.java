package jaicore.ml.dyadranking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.core.predictivemodel.IPredictiveModel;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

/**
 * Class for testing the functionality of the {@link PLNetDyadRanker}.
 * @author Jonas Hanselle
 *
 */
public class PLNetDyadRankerTest {

	/**
	 * 	Tests whether the PLNet ist constructed correctly.
	 */
	@Test
	public void testPLNetCreation() {
		PLNetDyadRanker pldr = new PLNetDyadRanker(4,7,7);
		
		DyadRankingDataset trainingDataset = DyadRankingInstanceSupplier.getDyadRankingDataset(10, 1000);
		DyadRankingDataset testDataset = DyadRankingInstanceSupplier.getDyadRankingDataset(10, 200);
		
		
		
		try {
			pldr.train(trainingDataset);
		} catch (TrainingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
