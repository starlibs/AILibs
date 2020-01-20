package ai.libs.jaicore.ml.experiments;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.openml.apiconnector.io.OpenmlConnector;

import ai.libs.jaicore.ml.classification.singlelabel.learner.MajorityClassifier;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetDescriptor;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class OpenMLProblemSet extends MLProblemSet {
	private final int id;

	public OpenMLProblemSet(final int id) throws Exception {
		super("OpenML-" + id + " (" + new OpenmlConnector().dataGet(id).getName() + ")");
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public ILabeledDataset<?> getDataset() throws DatasetDeserializationFailedException, InterruptedException {
		return new OpenMLDatasetReader().deserializeDataset(new OpenMLDatasetDescriptor(this.id));
	}

	public int getTrainTimeOfMajorityClassifier() throws TrainingException, InterruptedException, DatasetDeserializationFailedException {
		long start = System.currentTimeMillis();
		new MajorityClassifier().fit(this.getDataset());
		return (int)(System.currentTimeMillis() - start);
	}
}
