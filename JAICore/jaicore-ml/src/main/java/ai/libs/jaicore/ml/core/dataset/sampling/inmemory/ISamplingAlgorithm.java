package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import org.api4.java.ai.ml.IDataset;
import org.api4.java.algorithm.IAlgorithm;

/**
 * Interface for sampling algorithms. Sampling algorithms take a dataset as input and return a (reduced) dataset as their output.
 *
 * @author wever
 *
 */
public interface ISamplingAlgorithm <D extends IDataset<?>> extends IAlgorithm<D, D> {

}
