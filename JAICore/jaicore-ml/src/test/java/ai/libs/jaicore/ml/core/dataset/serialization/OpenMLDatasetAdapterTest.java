package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.junit.Test;

public class OpenMLDatasetAdapterTest {

	@Test
	public void testReadingKRVKP() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException {
		this.read(3, 3196,36, 2);
	}

	@Test
	public void testReadingHiggs() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException {
		this.read(23512, 98050, 28, 2);
	}

	@Test
	public void testReadingAmazon() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException {
		this.read(1457, 1500, 10000, 50);
	}

	@Test
	public void testReadingArticleInfluence() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException {
		this.read(42123, 3615, 6, 3169);
	}

	public void read(final int id, final int expectedInstances, final int expectedAttributes, final int expectedClasses) throws DatasetDeserializationFailedException, InterruptedException {
		ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.deserializeDataset(id);
		assertEquals("Incorrect number of instances.", expectedInstances, data.size());
		assertEquals("Incorrect number of attributes.", expectedAttributes, data.getNumAttributes());
		Set<Object> labels = data.stream().map(i -> i.getLabel()).collect(Collectors.toSet());
		assertEquals("Incorrect number of class labels.", expectedClasses, labels.size());
	}
}
