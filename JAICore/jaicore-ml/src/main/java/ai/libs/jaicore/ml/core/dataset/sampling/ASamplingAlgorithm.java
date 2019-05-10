package ai.libs.jaicore.ml.core.dataset.sampling;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.algorithm.IAlgorithmConfig;
import ai.libs.jaicore.ml.core.dataset.IDataset;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 */
public abstract class ASamplingAlgorithm extends AAlgorithm<IDataset, IDataset> {

	protected ASamplingAlgorithm(IAlgorithmConfig config, IDataset input) {
		super(config, input);
	}
	
	protected ASamplingAlgorithm(IDataset input) {
		super(input);
	}
}
