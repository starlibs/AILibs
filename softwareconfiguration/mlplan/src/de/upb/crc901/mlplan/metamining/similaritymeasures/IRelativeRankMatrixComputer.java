package de.upb.crc901.mlplan.metamining.similaritymeasures;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Represents an algorithm that, presented with a list of performance values of
 * a pipeline on a data set computes a relative rank of this performance on the
 * data set compared to other pipelines' performance on the same data set.
 * 
 * @author Helena Graf
 *
 */
public interface IRelativeRankMatrixComputer {

	/**
	 * Computes the relative rank matrix for the given performance values of
	 * pipelines on datasets.
	 * 
	 * @param performanceValues
	 *            The results of pipelines on datasets: rows: data sets, columns:
	 *            pipelines, entries: array of results of pipeline on data set
	 * @return The converted matrix as an INDArray for more efficient computing
	 */
	INDArray computeRelativeRankMatrix(double[][][] performanceValues);
}
