package jaicore.ml.dyadranking.zeroshot.inputoptimization;

import org.nd4j.linalg.api.ndarray.INDArray;

public interface InputOptimizerLoss {
	
	public double loss(INDArray plNetOutput);
	
	public double lossGradient(INDArray plNetOutput);
	
}
