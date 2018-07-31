package de.upb.crc901.automl.metamining.similaritymeasures;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.nd4j.linalg.api.ndarray.INDArray;

public class AlternatingGradientDescent implements IHeterogenousSimilarityMeasureComputer {
	
	/**
	 * Used to compute the RRt matrix.
	 */
	private IRankMatrixSimilarityComputer rankMatrixSimilarityComputer = new RankMatrixSimilarityComputer();
	private INDArray U;
	private INDArray V_transposed;

	@Override
	public void build(INDArray X, INDArray W, INDArray R) {
		// TODO Auto-generated method stub
		// set U and V here
	}

	@Override
	public double computeSimilarity(INDArray x, INDArray w) {
		return x.mmul(U).mmul(V_transposed).mmul(w).getDouble(0, 0);
	}
	
	private RealMatrix convertRealVectorToMatrix(RealVector vector) {
		return new Array2DRowRealMatrix(vector.toArray()).transpose();
	}

	/**
	 * @return the rankMatrixSimilarityComputer
	 */
	public IRankMatrixSimilarityComputer getRankMatrixSimilarityComputer() {
		return rankMatrixSimilarityComputer;
	}

	/**
	 * @param rankMatrixSimilarityComputer the rankMatrixSimilarityComputer to set
	 */
	public void setRankMatrixSimilarityComputer(IRankMatrixSimilarityComputer rankMatrixSimilarityComputer) {
		this.rankMatrixSimilarityComputer = rankMatrixSimilarityComputer;
	}

}
