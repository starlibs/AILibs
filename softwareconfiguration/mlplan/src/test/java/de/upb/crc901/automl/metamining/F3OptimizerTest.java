package de.upb.crc901.automl.metamining;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import de.upb.crc901.mlplan.metamining.similaritymeasures.F3Optimizer;
import de.upb.crc901.mlplan.metamining.similaritymeasures.IHeterogenousSimilarityMeasureComputer;

public class F3OptimizerTest {

//	@Test
//	public void optimizeF3() {
//		INDArray R = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 4 });
//		INDArray X = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 4 });
//		INDArray W = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 4, 2 });
//
//		/* learn the U matrix */
//		IHeterogenousSimilarityMeasureComputer learner = new F3Optimizer(1);
//		learner.build(X, W, R);
//	}
	
	@Test
	public void optimizeF3Simple() {
		INDArray R = Nd4j.create(new float[] { 1, 2}, new int[] { 1, 2 });
		INDArray W = Nd4j.create(new float[] { 1, 3}, new int[] { 2, 1 });

		//System.err.println(R.mul(W));;
		System.err.println(R.mul(W.transpose()));
		System.err.println(W.mmul(R));;
		System.err.println(R.mmul(W));

	}

}
