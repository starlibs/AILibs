package ai.libs.jaicore.ml.weka.dataset.splitter;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public interface IDatasetSplitter<I extends ILabeledInstance, D extends ILabeledDataset<I>> {

	public List<D> split(D data, long seed, double portions) throws SplitFailedException, InterruptedException;

}
