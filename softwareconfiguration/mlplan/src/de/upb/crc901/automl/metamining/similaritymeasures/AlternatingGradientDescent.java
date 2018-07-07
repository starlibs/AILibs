package de.upb.crc901.automl.metamining.similaritymeasures;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class AlternatingGradientDescent implements IHeterogenousSimilarityMeasureComputer {
	
	/**
	 * Used to compute the RRt matrix.
	 */
	private IRankMatrixSimilarityComputer rankMatrixSimilarityComputer = new RankMatrixSimilarityComputer();
	private RealMatrix U;
	private RealMatrix V_transposed;

	@Override
	public void build(RealMatrix X, RealMatrix W, RealMatrix R) {
		// TODO Auto-generated method stub
		// set U and V here
	}

	@Override
	public double computeSimilarity(RealVector x, RealVector w) {
		RealMatrix xNew = convertRealVectorToMatrix(x);
		RealMatrix wNew = convertRealVectorToMatrix(w); 
		return xNew.multiply(U).multiply(V_transposed).multiply(wNew.transpose()).getEntry(0, 0);
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
