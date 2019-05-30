package de.upb.crc901.mlplan.metamining.similaritymeasures;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Encapsulates a model that is trained to compute the similarity between two
 * multidimensional measures, e.g. data set meta features, algorithm meta
 * features and algorithm performance on a data set.
 * 
 * @author Helena Graf
 *
 */
public interface IHeterogenousSimilarityMeasureComputer {

	/**
	 * Build a model based on training data that can then be used to estimate the
	 * similarity of two measures for a new problem.
	 * 
	 * @param x
	 *            Feature values for instances of the first measure (One row =
	 *            features of one instance, e.g. meta features of a data set)
	 * @param w
	 *            Feature values for instances of the second measure (One row =
	 *            features of one instance, e.g. a characterization of a machine
	 *            learning pipeline)
	 * @param r
	 *            A matrix giving an indication of how good of a match a specific
	 *            instance of the first measure is to a specific instance of the
	 *            second measure, i.e. how well a pipeline performs on a data set
	 */
	public void build(INDArray x, INDArray w, INDArray r);

	/**
	 * Compute the 'quality of the match' of given feature values for a new problem
	 * instance based on the training.
	 * 
	 * @param x
	 *            Feature values for the instance for the first measure (e.g. meta
	 *            data of a new data set)
	 * @param w
	 *            Feature values for the instance for the second measure (e.g. a
	 *            characterization of machine learning pipeline)
	 * @return The quality of the match, or similarity for the given vectors
	 */
	public double computeSimilarity(INDArray x, INDArray w);
}
