package ai.libs.jaicore.ml.weka.dataset.splitter;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import weka.core.Instances;

public interface IDatasetSplitter<X, Y, I extends IInstance<X> & ILabeledInstance<Y>> {

	public List<Instances> split(ILabeledDataset<X, Y, I> data, long seed, double portions) throws SplitFailedException, InterruptedException;

}
