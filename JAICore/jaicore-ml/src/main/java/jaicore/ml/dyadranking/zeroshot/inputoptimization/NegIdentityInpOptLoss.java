package jaicore.ml.dyadranking.zeroshot.inputoptimization;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Loss function for PLNet input optimization that maximizes the output of a PLNet. (i.e. minimizes the negative output)
 * @author Michael Braun
 *
 */
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
