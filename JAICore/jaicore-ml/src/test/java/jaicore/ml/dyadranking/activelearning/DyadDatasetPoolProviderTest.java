package jaicore.ml.dyadranking.activelearning;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * Simple test to check whether the queried rankings the pool provider returns
 * are correct.
 * 
 * @author Jonas Hanselle
 *
 */
public class DyadDatasetPoolProviderTest {

	private static final String GATSP_DATASET_FILE = "testrsc/ml/dyadranking/ga-tsp/GATSP-Data.txt";

	@Test
	public void testPoolProvider() {
		DyadRankingDataset dataset = new DyadRankingDataset();
		try {
			dataset.deserialize(new FileInputStream(new File(GATSP_DATASET_FILE)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(dataset);

		// get some true rankings
		IDyadRankingInstance trueRanking1 = (IDyadRankingInstance) dataset.get(0);
		IDyadRankingInstance trueRanking2 = (IDyadRankingInstance) dataset.get(5);
		IDyadRankingInstance trueRanking3 = (IDyadRankingInstance) dataset.get(13);

		// build sparse dyad ranking instances from them
		List<Vector> alternatives1 = new ArrayList<>(trueRanking1.length());
		List<Vector> alternatives2 = new ArrayList<>(trueRanking2.length());
		List<Vector> alternatives3 = new ArrayList<>(trueRanking3.length());

		for (Dyad dyad : trueRanking1)
			alternatives1.add(dyad.getAlternative());
		for (Dyad dyad : trueRanking2)
			alternatives2.add(dyad.getAlternative());
		for (Dyad dyad : trueRanking3)
			alternatives3.add(dyad.getAlternative());

		SparseDyadRankingInstance si1 = new SparseDyadRankingInstance(trueRanking1.getDyadAtPosition(0).getInstance(),
				alternatives1);
		SparseDyadRankingInstance si2 = new SparseDyadRankingInstance(trueRanking2.getDyadAtPosition(0).getInstance(),
				alternatives1);
		SparseDyadRankingInstance si3 = new SparseDyadRankingInstance(trueRanking3.getDyadAtPosition(0).getInstance(),
				alternatives1);

		// shuffle the sparse instances
		Collections.shuffle(alternatives1);
		Collections.shuffle(alternatives2);
		Collections.shuffle(alternatives3);

		// query the sparse instances
		IDyadRankingInstance queriedRanking1 = (IDyadRankingInstance) poolProvider.query(si1);
		IDyadRankingInstance queriedRanking2 = (IDyadRankingInstance) poolProvider.query(si2);
		IDyadRankingInstance queriedRanking3 = (IDyadRankingInstance) poolProvider.query(si3);

		// assert that they are equals to the true rankings
		assertEquals(trueRanking1, queriedRanking1);
		assertEquals(trueRanking2, queriedRanking2);
		assertEquals(trueRanking3, queriedRanking3);
	}

}
