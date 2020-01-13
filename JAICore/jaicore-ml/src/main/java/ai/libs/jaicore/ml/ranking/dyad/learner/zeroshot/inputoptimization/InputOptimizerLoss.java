package ai.libs.jaicore.ml.ranking.dyad.learner.zeroshot.inputoptimization;

import org.nd4j.linalg.api.ndarray.INDArray;

public interface InputOptimizerLoss {
	
	public double loss(INDArray plNetOutput);
	
	public double lossGradient(INDArray plNetOutput);
	
}
