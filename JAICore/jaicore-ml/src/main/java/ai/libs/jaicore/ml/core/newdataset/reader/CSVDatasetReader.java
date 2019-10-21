package ai.libs.jaicore.ml.core.newdataset.reader;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class CSVDatasetReader implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>> {

	public CSVDatasetReader() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ILabeledDataset<ILabeledInstance> deserializeDataset(final File datasetFile) throws InterruptedException {
		return null;
	}

}
