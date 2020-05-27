package ai.libs.jaicore.ml.weka.preprocessing;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;

/**
 * A WEKA preprocessing algorithm takes a labeled dataset and produces itself as to allow for applying the
 * obtained dimensionality reduction to some new data.
 *
 * @author Felix Mohr
 *
 */
public interface IWekaPreprocessingAlgorithm extends IAlgorithm<ILabeledDataset<?>, IWekaPreprocessingAlgorithm> {

}
