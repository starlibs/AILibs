package jaicore.ml.weka.dataset.splitter;

import java.util.List;

import weka.core.Instances;

public interface IDatasetSplitter {

	public List<Instances> split(Instances data, long seed, double... portions);

}
