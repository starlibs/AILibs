package ai.libs.jaicore.ml.core.dataset.sampling;

import org.api4.java.ai.ml.core.dataset.AILabeledAttributeArrayDataset;
import org.api4.java.algorithm.IAlgorithm;

/**
 * Interface for sampling algorithms. Sampling algorithms take a dataset as input and return a (reduced) dataset as their output.
 *
 * @author wever
 *
 */
public interface ISamplingAlgorithm extends IAlgorithm<AILabeledAttributeArrayDataset, AILabeledAttributeArrayDataset> {

}
