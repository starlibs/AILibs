package de.upb.crc901.automl.metamining.similaritymeasures;

import org.apache.commons.math3.linear.RealMatrix;

public interface IRankMatrixSimilarityComputer {
	public RealMatrix computeSimilarityOfRankMatrix(RealMatrix R);
}
