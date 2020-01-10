package ai.libs.jaicore.ml.weka.dataset;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import weka.core.Instances;

public interface IWekaInstances extends ILabeledDataset<IWekaInstance> {

	public Instances getList();

	public default Instances getInstances() {
		return this.getList();
	}

	@Override
	public IWekaInstances createEmptyCopy() throws DatasetCreationException, InterruptedException;

}
