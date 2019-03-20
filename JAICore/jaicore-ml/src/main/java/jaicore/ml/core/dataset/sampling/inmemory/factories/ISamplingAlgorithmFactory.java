package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;

/**
 * Interface for a factory, which creates a sampling algorithm.
 * 
 * @author Lukas Brandt
 * @param <I>
 *            Type of the dataset instances.
 */
public interface ISamplingAlgorithmFactory<I extends IInstance> {

	public void setSampleSize(int sampleSize);

	public void setInputDataset(IDataset<I> inputDataset);

	public void setRandom(long seed);

	public void setRandom(Random random);

	/**
	 * After the neccessary config is done, this method returns a fully configured
	 * instance of a sampling algorithm.
	 * 
	 * @return Configured sampling algorithm object.
	 * @throws IllegalStateException
	 *             Method was called before all mandatory configuration was done.
	 */
	public ASamplingAlgorithm<I> getAlgorithm() throws IllegalStateException;

}
