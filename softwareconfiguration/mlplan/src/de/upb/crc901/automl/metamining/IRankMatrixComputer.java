package de.upb.crc901.automl.metamining;

import org.apache.commons.math3.linear.RealMatrix;

public interface IRankMatrixComputer {
	public RealMatrix computeRankMatrix(RealMatrix X, RealMatrix W);
}
