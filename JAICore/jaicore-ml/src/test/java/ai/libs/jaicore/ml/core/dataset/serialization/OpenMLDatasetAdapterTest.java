package ai.libs.jaicore.ml.core.dataset.serialization;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.junit.Test;

public class OpenMLDatasetAdapterTest {

	@Test
	public void testReading() throws DatasetDeserializationFailedException, InterruptedException, DatasetCreationException {
		ILabeledDataset<ILabeledInstance> data = OpenMLDatasetReader.readDataset(60);
		System.out.println(data.size());
	}

}
