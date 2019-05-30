package jaicore.ml.core.dataset.sampling;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;

/**
 * An abstract class for sampling algorithms providing basic functionality of an algorithm.
 *
 * @author wever
 */
public abstract class ASamplingAlgorithm extends AAlgorithm<AILabeledAttributeArrayDataset, AILabeledAttributeArrayDataset> {

	protected ASamplingAlgorithm(IAlgorithmConfig config, AILabeledAttributeArrayDataset input) {
		super(config, input);
	}
	
	protected ASamplingAlgorithm(AILabeledAttributeArrayDataset input) {
		super(input);
	}
}
