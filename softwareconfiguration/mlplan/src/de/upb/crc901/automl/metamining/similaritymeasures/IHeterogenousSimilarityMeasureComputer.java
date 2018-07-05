package de.upb.crc901.automl.metamining.similaritymeasures;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public interface IHeterogenousSimilarityMeasureComputer {
	public void build(RealMatrix X, RealMatrix W, RealMatrix R);
	public double computeSimilarity(RealVector x, RealVector w);
}
