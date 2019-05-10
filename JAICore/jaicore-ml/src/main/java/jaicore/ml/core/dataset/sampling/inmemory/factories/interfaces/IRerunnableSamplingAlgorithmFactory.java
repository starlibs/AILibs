package jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces;

import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;

/**
 * Extension of the ISamplingAlgorithmFactory for sampling algorithms that can
 * re-use informations from a previous run of the Sampling algorithm.
 * 
 * @author Lukas Brandt
 * @param <I> Type of the dataset instances.
 * @param <A> Type of the sampling algorithm that will be created.
 */
public interface IRerunnableSamplingAlgorithmFactory<I extends IInstance, A extends ASamplingAlgorithm<I>>
		extends ISamplingAlgorithmFactory<I, A> {

	/**
	 * Set the previous run of the sampling algorithm, if one occurred, can be set
	 * here to get data from it.
	 * 
	 * @param previousRun Algorithm object of the previous of the sampling
	 *                    algorithm.
	 */
	public void setPreviousRun(A previousRun);

}
