package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.api4.java.common.parallelization.IParallelizable;

/**
 * Functional interface to write custom logic for selecting the amount of strati
 * for a dataset.
 *
 * @author Lukas Brandt
 */
public interface IStratiAmountSelector<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> extends IParallelizable {

	/**
	 * Select a suitable amount of strati for a Dataset.
	 *
	 * @param dataset
	 *            The dataset that will be stratified.
	 * @return The determined amount of strati.
	 */
	public int selectStratiAmount(D dataset);

}
