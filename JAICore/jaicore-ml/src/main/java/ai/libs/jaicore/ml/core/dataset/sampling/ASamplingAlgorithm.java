package ai.libs.jaicore.ml.core.dataset.sampling;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 */
public abstract class ASamplingAlgorithm extends AAlgorithm<AILabeledAttributeArrayDataset, AILabeledAttributeArrayDataset> {

	protected ASamplingAlgorithm(final IOwnerBasedAlgorithmConfig config, final AILabeledAttributeArrayDataset input) {
		super(config, input);
	}

	protected ASamplingAlgorithm(final AILabeledAttributeArrayDataset input) {
		super(input);
	}
}
