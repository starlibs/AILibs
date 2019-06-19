package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import ai.libs.jaicore.basic.algorithm.IAlgorithm;
import ai.libs.jaicore.ml.core.dataset.IDataset;

/**
 * Interface for sampling algorithms. Sampling algorithms take a dataset as input and return a (reduced) dataset as their output.
 *
 * @author wever
 *
 */
public interface ISamplingAlgorithm <D extends IDataset<?>> extends IAlgorithm<D, D> {

}
