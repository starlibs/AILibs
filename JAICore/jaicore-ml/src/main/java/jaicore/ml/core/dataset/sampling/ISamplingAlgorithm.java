package jaicore.ml.core.dataset.sampling;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;

/**
 * Interface for sampling algorithms. Sampling algorithms take a dataset as input and return a (reduced) dataset as their output.
 *
 * @author wever
 *
 */
public interface ISamplingAlgorithm extends IAlgorithm<IDataset, IDataset> {

}
