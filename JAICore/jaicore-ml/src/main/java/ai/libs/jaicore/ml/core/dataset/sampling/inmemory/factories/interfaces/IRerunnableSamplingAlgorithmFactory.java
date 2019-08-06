package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;

/**
 * Extension of the ISamplingAlgorithmFactory for sampling algorithms that can
 * re-use informations from a previous run of the Sampling algorithm.
 *
 * @author Lukas Brandt
 * @param <I> Type of the dataset instances.
 * @param <A> Type of the sampling algorithm that will be created.
 */
public interface IRerunnableSamplingAlgorithmFactory<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>, A extends ASamplingAlgorithm<X, Y, I, D>>
		extends ISamplingAlgorithmFactory<X, Y, I, D, A> {

	/**
	 * Set the previous run of the sampling algorithm, if one occurred, can be set
	 * here to get data from it.
	 *
	 * @param previousRun Algorithm object of the previous of the sampling
	 *            algorithm.
	 */
	public void setPreviousRun(A previousRun);

}
