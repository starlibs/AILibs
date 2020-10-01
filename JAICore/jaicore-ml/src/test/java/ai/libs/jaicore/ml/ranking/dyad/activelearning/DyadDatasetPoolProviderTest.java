package ai.libs.jaicore.ml.ranking.dyad.activelearning;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.activelearning.DyadDatasetPoolProvider;

/**
 * Simple test to check whether the queried rankings the pool provider returns
 * are correct.
 *
 * @author Jonas Hanselle
 *
 */
public class DyadDatasetPoolProviderTest {

	private static final String GATSP_DATASET_FILE = "testrsc/ml/dyadranking/ga-tsp/GATSP-Data.txt";

	@Disabled
	@Test
	public void testPoolProvider() throws FileNotFoundException {
		DyadRankingDataset dataset = new DyadRankingDataset();
		dataset.deserialize(new FileInputStream(new File(GATSP_DATASET_FILE)));

		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(dataset);

		// get some true rankings
		IDyadRankingInstance trueRanking1 = dataset.get(0);
		IDyadRankingInstance trueRanking2 = dataset.get(5);
		IDyadRankingInstance trueRanking3 = dataset.get(13);

		// build sparse dyad ranking instances from them
		List<IVector> alternatives1 = new ArrayList<>(trueRanking1.getNumAttributes());
		List<IVector> alternatives2 = new ArrayList<>(trueRanking2.getNumAttributes());
		List<IVector> alternatives3 = new ArrayList<>(trueRanking3.getNumAttributes());

		for (IDyad dyad : trueRanking1.getLabel()) {
			alternatives1.add(dyad.getAlternative());
		}
		for (IDyad dyad : trueRanking2.getLabel()) {
			alternatives2.add(dyad.getAlternative());
		}
		for (IDyad dyad : trueRanking3.getLabel()) {
			alternatives3.add(dyad.getAlternative());
		}

		SparseDyadRankingInstance si1 = new SparseDyadRankingInstance(trueRanking1.getLabel().get(0).getContext(), alternatives1);
		SparseDyadRankingInstance si2 = new SparseDyadRankingInstance(trueRanking2.getLabel().get(0).getContext(), alternatives1);
		SparseDyadRankingInstance si3 = new SparseDyadRankingInstance(trueRanking3.getLabel().get(0).getContext(), alternatives1);

		// shuffle the sparse instances
		Collections.shuffle(alternatives1);
		Collections.shuffle(alternatives2);
		Collections.shuffle(alternatives3);

		// query the sparse instances
		IDyadRankingInstance queriedRanking1 = poolProvider.query(si1);
		IDyadRankingInstance queriedRanking2 = poolProvider.query(si2);
		IDyadRankingInstance queriedRanking3 = poolProvider.query(si3);

		// assert that they are equals to the true rankings
		assertEquals(trueRanking1, queriedRanking1);
		assertEquals(trueRanking2, queriedRanking2);
		assertEquals(trueRanking3, queriedRanking3);
	}

}
