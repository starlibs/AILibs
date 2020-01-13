package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;

/**
 * Interface for a factory, which creates a sampling algorithm.
 *
 * @author Lukas Brandt
 * @param <A> Type of the sampling algorithm that will be created.
 */
public interface ISamplingAlgorithmFactory<D extends IDataset<?>, A extends ASamplingAlgorithm<D>> {

	/**
	 * After the necessary config is done, this method returns a fully configured
	 * instance of a sampling algorithm.
	 *
	 * @param sampleSize   Desired size of the sample that will be created.
	 * @param inputDataset Dataset where the sample will be drawn from.
	 * @param random       Random object to make samples reproducible.
	 * @return Configured sampling algorithm object.
	 */
	public A getAlgorithm(int sampleSize, D inputDataset, Random random);


	/**
	 * After the necessary config is done, this method returns a fully configured
	 * instance of a sampling algorithm.
	 *
	 * Here, it is assumed that random aspects or sample sizes have already been defined before
	 **/
	public A getAlgorithm(D inputDataset);

}
