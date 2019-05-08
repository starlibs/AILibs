package jaicore.ml.wekautil.dataset.splitter;

import java.util.List;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

public interface IDatasetSplitter<O extends IInstance> {

	public <I extends IInstance> List<IDataset<O>> split(IDataset<I> data, long seed, double... portions);

}
