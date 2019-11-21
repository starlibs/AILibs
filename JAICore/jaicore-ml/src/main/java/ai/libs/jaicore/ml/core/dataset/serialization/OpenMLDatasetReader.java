package ai.libs.jaicore.ml.core.dataset.serialization;

import java.io.File;

import org.api4.java.ai.ml.core.IDatasetDescriptor;
import org.api4.java.ai.ml.core.dataset.IDatasetBuilder;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

public class OpenMLDatasetReader implements IDatasetBuilder {

	private static final OpenmlConnector connector = new OpenmlConnector();

	public static ILabeledDataset<ILabeledInstance> readDataset(final int openMLId) throws DatasetCreationException {
		try {
			DataSetDescription dsd = connector.dataGet(openMLId);
			File arffFile = connector.datasetGet(dsd);
			return new ArffDatasetAdapter().deserializeDataset(arffFile);
		}
		catch (Exception e) {
			throw new DatasetCreationException(e);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance>  getDataset(final IDatasetDescriptor descriptor) throws DatasetCreationException {
		if (!(descriptor instanceof OpenMLDatasetDescriptor)) {
			throw new IllegalArgumentException("Only openml descriptors supported.");
		}
		OpenMLDatasetDescriptor cDescriptor = (OpenMLDatasetDescriptor)descriptor;
		return readDataset(cDescriptor.getOpenMLId());
	}
}
