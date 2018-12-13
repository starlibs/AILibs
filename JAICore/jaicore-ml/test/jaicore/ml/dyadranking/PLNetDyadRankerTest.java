package jaicore.ml.dyadranking;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.general.DyadRankingInstanceSupplier;

/**
 * Class for testing the functionality of the {@link PLNetDyadRanker}.
 * @author Jonas Hanselle
 *
 */
public class PLNetDyadRankerTest {

	@Test
	public void testPLNetCreation() {
		PLNetDyadRanker pldr = new PLNetDyadRanker(4,7,7);
		DyadRankingDataset trainingDataset = DyadRankingInstanceSupplier.getDyadRankingDataset(10, 1000);
		DyadRankingDataset testDataset = DyadRankingInstanceSupplier.getDyadRankingDataset(10, 200);
		
		try {
			pldr.train(trainingDataset);
			List<IDyadRankingInstance> predictions = pldr.predict(testDataset);
			
		} catch (TrainingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PredictionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
