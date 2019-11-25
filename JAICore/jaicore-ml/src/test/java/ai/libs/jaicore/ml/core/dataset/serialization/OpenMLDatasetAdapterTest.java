package ai.libs.jaicore.ml.core.dataset.serialization;

import static org.junit.Assert.assertEquals;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.junit.Test;

public class OpenMLDatasetAdapterTest {

	@Test
	public void testReading() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException {

		/* read the krvskp dataset */
		ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.readDataset(3);
		assertEquals("Incorrect number of instances.", 3196, data.size());
		assertEquals("Incorrect number of attributes.", 36, data.getNumAttributes());
	}

}
