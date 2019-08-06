package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces;

import java.util.Random;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;

/**
 * Interface for a factory, which creates a sampling algorithm.
 *
 * @author Lukas Brandt
 * @param <I> Type of the dataset instances.
 * @param <A> Type of the sampling algorithm that will be created.
 */
public interface ISamplingAlgorithmFactory<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>, A extends ASamplingAlgorithm<X, Y, I, D>> {

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

}
