package ai.libs.jaicore.ml.ranking.dyad.general;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.Test;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;

/**
 * A simple test to check whether serialization and deserialization works for {@link DyadRankingDataset}s.
 * @author Jonas Hanselle, Michael Braun
 *
 */
public class DyadRankingDatasetSerializationTest {

	private static final String DATASET_FILE = "testrsc/ml/dyadranking/serialized-dataset.txt";

	@Test
	public void testSerialization() throws FileNotFoundException {
		DyadRankingDataset dataset = DyadRankingInstanceSupplier.getDyadRankingDataset(5, 15);
		dataset.serialize(new FileOutputStream(new File(DATASET_FILE)));
		DyadRankingDataset dataset2 = new DyadRankingDataset();
		dataset2.deserialize(new FileInputStream(new File(DATASET_FILE)));
		System.out.println(dataset.toString());
		System.out.println(dataset2.toString());
		assertEquals(dataset, dataset2);
		assertEquals(dataset2, dataset);

	}
}
