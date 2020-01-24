package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.File;

import org.api4.java.ai.ml.core.dataset.descriptor.IDatasetDescriptor;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.serialization.IDatasetDeserializer;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import ai.libs.jaicore.basic.reconstruction.ReconstructionInstruction;
import ai.libs.jaicore.ml.core.dataset.Dataset;

public class OpenMLDatasetReader implements IDatasetDeserializer<ILabeledDataset<ILabeledInstance>> {

	private static final OpenmlConnector connector = new OpenmlConnector();

	public static ILabeledDataset<ILabeledInstance> deserializeDataset(final int openMLId) throws DatasetDeserializationFailedException {
		try {
			DataSetDescription dsd = connector.dataGet(openMLId);
			if (dsd.getDefault_target_attribute().contains(",")) {
				throw new IllegalArgumentException("The dataset with ID " + openMLId + " cannot be read as it is a multi-target dataset which is currently not supported.");
			}

			File arffFile = connector.datasetGet(dsd);
			Dataset ds = (Dataset) (new ArffDatasetAdapter().deserializeDataset(new FileDatasetDescriptor(arffFile), dsd.getDefault_target_attribute()));
			ds.addInstruction(new ReconstructionInstruction(OpenMLDatasetReader.class.getMethod("deserializeDataset", int.class), openMLId));
			return ds;
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException("Could not deserialize OpenML dataset with id " + openMLId, e);
		}
	}

	public static File getArffFileOfOpenMLID(final int id) throws Exception {
		DataSetDescription dsd = connector.dataGet(id);
		return connector.datasetGet(dsd);
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
