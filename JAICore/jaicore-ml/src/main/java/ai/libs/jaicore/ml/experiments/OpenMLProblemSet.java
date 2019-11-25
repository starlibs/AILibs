package ai.libs.jaicore.ml.experiments;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.openml.apiconnector.io.OpenmlConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class OpenMLProblemSet extends MLProblemSet {

	private static final Logger logger = LoggerFactory.getLogger(OpenMLProblemSet.class);

	private final int id;

	public OpenMLProblemSet(final int id) throws Exception {
		super("OpenML-" + id + " (" + new OpenmlConnector().dataGet(id).getName() + ")");
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public ILabeledDataset<?> getDataset() throws DatasetCreationException {
		return OpenMLDatasetReader.readDataset(this.id);
	}
}
