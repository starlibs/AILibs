package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

public class OpenMLDatasetReader implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>> {

	private static final OpenmlConnector connector = new OpenmlConnector();

	public static ILabeledDataset<ILabeledInstance> deserializeDataset(final int openMLId) throws DatasetDeserializationFailedException, InterruptedException {
		try {
			DataSetDescription dsd = connector.dataGet(openMLId);
			File arffFile = connector.datasetGet(dsd);
			return new ArffDatasetAdapter().deserializeDataset(new FileDatasetDescriptor(arffFile));
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException("Could not deserialize OpenML dataset with id " + openMLId, e);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance> deserializeDataset(final IDatasetDescriptor descriptor) throws DatasetDeserializationFailedException, InterruptedException {
		if (!(descriptor instanceof OpenMLDatasetDescriptor)) {
			throw new IllegalArgumentException("Only openml descriptors supported.");
		}
		OpenMLDatasetDescriptor cDescriptor = (OpenMLDatasetDescriptor) descriptor;
		return deserializeDataset(cDescriptor.getOpenMLId());
	}

}
