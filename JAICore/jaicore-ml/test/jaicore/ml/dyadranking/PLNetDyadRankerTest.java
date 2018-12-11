package jaicore.ml.dyadranking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import jaicore.ml.core.predictivemodel.IPredictiveModel;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;

/**
 * Class for testing the functionality of the {@link PLNetDyadRanker}.
 * @author Jonas Hanselle
 *
 */
class PLNetDyadRankerTest {

	/**
	 * 	Tests whether the PLNet ist constructed correctly.
	 */
	@Test
	void testPLNetCreation() {
		DyadRankingDataset drDataset = new DyadRankingDataset();
	}

}
