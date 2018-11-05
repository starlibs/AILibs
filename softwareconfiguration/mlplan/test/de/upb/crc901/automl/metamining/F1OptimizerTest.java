package de.upb.crc901.automl.metamining;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import de.upb.crc901.mlplan.metamining.similaritymeasures.F1Optimizer;

public class F1OptimizerTest {

	@Test
	public void test() {

		/* invent some R and X matrices */
		INDArray R = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 3 });
		INDArray X = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 3 });

		/* learn the U matrix */
		F1Optimizer uLearner = new F1Optimizer();
		uLearner.build(X, null, R);
		System.out.println("learned U = " + uLearner.getU());
		System.out.println("loss of U: " + uLearner.getCost(uLearner.getU()));
	}

}
