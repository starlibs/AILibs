package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.common.reconstruction.IReconstructible;
import org.api4.java.common.reconstruction.ReconstructionException;
import org.junit.Test;

public class OpenMLDatasetAdapterTest {

	@Test
	public void testReadingKRVKP() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(3, 3196, 36, 2);
	}

	@Test
	public void testReadingHiggs() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(23512, 98050, 28, 2);
	}

	@Test
	public void testReadingAmazon() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(1457, 1500, 10000, 50);
	}

	@Test
	public void testReadingArticleInfluence() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(42123, 3615, 6, 3169);
	}

	@Test
	public void testReadingMNIST() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(554, 70000, 784, 10);
	}

	@Test
	public void testReadingDexter() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(4136, 600, 20000, 2);
	}

	@Test
	public void testReadingDorothea() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(4137, 1150, 100000, 2);
	}

	@Test
	public void testReadingGisette() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(41026, 7000, 5000, 2);
	}

	@Test
	public void testReadingCifar10() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(40927, 60000, 3072, 10);
	}

	@Test
	public void testReadingAbalone() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException, ReconstructionException {
		this.testReadAndReconstructibility(183, 4177, 8, 28);
	}

	public void testReadAndReconstructibility(final int id, final int expectedInstances, final int expectedAttributes, final int expectedClasses) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		this.testReconstructibility(this.read(id, expectedInstances, expectedAttributes, expectedClasses));
	}

	public ILabeledDataset<ILabeledInstance> read(final int id, final int expectedInstances, final int expectedAttributes, final int expectedClasses) throws DatasetDeserializationFailedException, InterruptedException {
		ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.deserializeDataset(id);
		assertEquals("Incorrect number of instances.", expectedInstances, data.size());
		assertEquals("Incorrect number of attributes.", expectedAttributes, data.getNumAttributes());
		Set<Object> labels = data.stream().map(i -> i.getLabel()).collect(Collectors.toSet());
		return data;
	}

	public void testReconstructibility(final ILabeledDataset<?> dataset) throws DatasetDeserializationFailedException, InterruptedException, ReconstructionException {
		if (!(dataset instanceof IReconstructible)) {
			fail("Dataset not reconstructible");
		}
		IReconstructible rDataset = (IReconstructible) dataset;
		ILabeledDataset<?> reproducedDataset = (ILabeledDataset<?>) rDataset.getConstructionPlan().reconstructObject();
		assertEquals(dataset.get(0), reproducedDataset.get(0)); // first check that the first instance is equal in both cases. This is of course covered by later assertions but simplifies debugging in case of failure
		assertEquals(dataset.getInstanceSchema(), reproducedDataset.getInstanceSchema()); // check that schema are identically. Again, this is checked for convenience
		assertEquals(rDataset, reproducedDataset); // full equality check
	}
}
