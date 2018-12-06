package jaicore.ml.core.dataset.sampling;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.ml.core.dataset.IDataset;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 * @author Lukas Brandt
 */
public abstract class ASamplingAlgorithm extends AAlgorithm<IDataset, IDataset> {

	protected Integer sampleSize = null;
	
	public void setSampleSize(int size) {
		this.sampleSize = size;
	}
	
	@Override
	public IDataset call() throws Exception {
		// Check missing or invalid configuration.
		if (sampleSize == null) {
			throw new Exception("No sample size specified");
		}
		IDataset dataset = this.getInput();
		if (dataset == null || dataset.size() == 0) {
			throw new Exception("No dataset or an empty dataset was given as an input.");
		}
		if (dataset.size() < this.sampleSize) {
			throw new Exception("Specified sample size is bigger than the dataset.");
		} else if (dataset.size() == this.sampleSize) {
			// The dataset size is exactly the specified sample size, so just return the whole dataset.
			return dataset;
		} else {
			// Working configuration, so create the actual sample.
			return this.createSampleFromDataset(dataset);
		}
	}
	
	public abstract IDataset createSampleFromDataset (IDataset dataset) throws Exception;
	
}
