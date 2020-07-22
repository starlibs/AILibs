package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.File;
import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.Test;

public class CSVDatasetAdapterTest {

	@Test
	public void testWriteDataset() throws DatasetDeserializationFailedException, IOException {
		ILabeledDataset<ILabeledInstance> dataset = OpenMLDatasetReader.deserializeDataset(31);
		CSVDatasetAdapter.writeDataset(new File("test.csv"), dataset);
	}

}
