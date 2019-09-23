package ai.libs.jaicore.ml.core.filter.sampling;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.filter.unsupervised.sampling.ISamplingAlgorithm;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 */
public abstract class ASamplingAlgorithm<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends AAlgorithm<D, D> implements ISamplingAlgorithm<I, D> {

	protected ASamplingAlgorithm(final IOwnerBasedAlgorithmConfig config, final D input) {
		super(config, input);
	}

	protected ASamplingAlgorithm(final D input) {
		super(input);
	}
}
