package ai.libs.jaicore.ml.classification.multilabel.dataset;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import weka.core.Instances;

public interface IMekaInstances extends ILabeledDataset<IMekaInstance> {

	public Instances getList();

	public default Instances getInstances() {
		return this.getList();
	}

	@Override
	public IMekaInstances createEmptyCopy() throws DatasetCreationException, InterruptedException;

}
