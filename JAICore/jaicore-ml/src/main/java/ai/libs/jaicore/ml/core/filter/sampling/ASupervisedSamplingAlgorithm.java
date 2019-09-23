package ai.libs.jaicore.ml.core.filter.sampling;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.filter.supervised.sampling.ISupervisedSamplingAlgorithm;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 */
public abstract class ASupervisedSamplingAlgorithm<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends AAlgorithm<D, D> implements ISupervisedSamplingAlgorithm<I, D> {

	protected ASupervisedSamplingAlgorithm(final IOwnerBasedAlgorithmConfig config, final D input) {
		super(config, input);
	}

	protected ASupervisedSamplingAlgorithm(final D input) {
		super(input);
	}
}
