package de.upb.crc901.automl.metamining.similaritymeasures;

import org.nd4j.linalg.api.ndarray.INDArray;

public interface IHeterogenousSimilarityMeasureComputer {
	public void build(INDArray X, INDArray W, INDArray R);
	public double computeSimilarity(INDArray x, INDArray w);
}
