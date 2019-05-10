package ai.libs.mlplan.metamining.similaritymeasures;

import org.nd4j.linalg.api.ndarray.INDArray;

public interface IRankMatrixSimilarityComputer {
	public INDArray computeSimilarityOfRankMatrix(INDArray R);
}
