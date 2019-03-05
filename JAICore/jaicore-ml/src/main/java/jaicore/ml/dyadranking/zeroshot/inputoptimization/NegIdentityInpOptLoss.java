package main.java.jaicore.ml.dyadranking.zeroshot.inputoptimization;

import org.nd4j.linalg.api.ndarray.INDArray;

public class NegIdentityInpOptLoss implements InputOptimizerLoss {

	@Override
	public double loss(INDArray plNetOutput) {
		return - plNetOutput.getDouble(0);
	}
	
	@Override
	public double lossGradient(INDArray plNetOutput) {
		return -1.0;
	}

}
