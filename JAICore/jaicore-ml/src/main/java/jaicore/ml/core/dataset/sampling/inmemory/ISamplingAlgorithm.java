package jaicore.ml.core.dataset.sampling.inmemory;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Interface for sampling algorithms. Sampling algorithms take a dataset as input and return a (reduced) dataset as their output.
 *
 * @author wever
 *
 */
public interface ISamplingAlgorithm <I extends IInstance> extends IAlgorithm<IDataset<I>, IDataset<I>> {

}
