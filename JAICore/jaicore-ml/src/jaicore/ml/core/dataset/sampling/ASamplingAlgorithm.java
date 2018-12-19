package jaicore.ml.core.dataset.sampling;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.sampling.stratified.sampling.AttributeBasedStratiAmountSelector;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;

/**
 * An abstract class for sampling algorithms providing basic functionality of an
 * algorithm.
 *
 * @author wever
 * @author Lukas Brandt
 * @author Felix Weiland
 */
public abstract class ASamplingAlgorithm extends AAlgorithm<IDataset, IDataset> {

	private static Logger LOG = LoggerFactory.getLogger(ASamplingAlgorithm.class);

	protected Integer sampleSize = null;
	protected IDataset sample = null;

	public void setSampleSize(int size) {
		this.sampleSize = size;
	}

	@Override
	public IDataset call() throws Exception {
		Instant timeoutTime;
		if (this.getTimeout().milliseconds() <= 0) {
			LOG.warn("Invalid or no timeout set. There will be no timeout in this algorithm run");
			timeoutTime = Instant.MAX;
		} else {
			timeoutTime = Instant.now().plus(getTimeout().milliseconds(), ChronoUnit.MILLIS);
		}
		// Check missing or invalid configuration.
		if (sampleSize == null) {
			throw new Exception("No valid sample size specified");
		}
		if (sampleSize == 0) {
			return this.createEmptyDatasetFromInputSchema();
		}
		IDataset dataset = this.getInput();
		if (dataset == null || dataset.size() == 0) {
			throw new Exception("No dataset or an empty dataset was given as an input.");
		}
		if (dataset.size() < this.sampleSize) {
			throw new Exception("Specified sample size is bigger than the dataset.");
		} else if (dataset.size() == this.sampleSize) {
			// The dataset size is exactly the specified sample size, so just return the
			// whole dataset.
			return dataset;
		} else {
			// Working configuration, so create the actual sample.
			while (this.hasNext()) {
				if (Instant.now().isAfter(timeoutTime)) {
					this.cancel();
				}
				if (this.isCanceled()) {
					throw new InterruptedException("Subsampling not finished");
				}
				this.next();
			}
			return sample;
		}
	}

	/**
	 * @return An empty dataset that has the same schema as the input dataset.
	 */
	protected IDataset createEmptyDatasetFromInputSchema() {
		return new SimpleDataset(
				new InstanceSchema(this.getInput().getAttributeTypes(), this.getInput().getTargetType()));
	}

}
