package ai.libs.jaicore.ml.core.newdataset.reader;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public interface IDatasetLoader {

	public ILabeledDataset<ILabeledInstance> loadDataset() throws InterruptedException;

}
