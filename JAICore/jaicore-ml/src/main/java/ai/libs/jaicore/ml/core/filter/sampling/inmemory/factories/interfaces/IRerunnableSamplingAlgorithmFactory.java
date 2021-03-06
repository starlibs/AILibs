package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces;

import org.api4.java.ai.ml.core.dataset.IDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;

/**
 * Extension of the ISamplingAlgorithmFactory for sampling algorithms that can
 * re-use informations from a previous run of the Sampling algorithm.
 *
 * @author Lukas Brandt
 * @param <I> Type of the dataset instances.
 * @param <A> Type of the sampling algorithm that will be created.
 */
public interface IRerunnableSamplingAlgorithmFactory<D extends IDataset<?>, A extends ASamplingAlgorithm<D>> extends ISamplingAlgorithmFactory<D, A> {

	/**
	 * Set the previous run of the sampling algorithm, if one occurred, can be set
	 * here to get data from it.
	 *
	 * @param previousRun Algorithm object of the previous of the sampling
	 *            algorithm.
	 */
	public void setPreviousRun(A previousRun);

}
