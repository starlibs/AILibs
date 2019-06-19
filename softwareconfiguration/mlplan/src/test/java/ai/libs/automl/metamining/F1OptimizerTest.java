package ai.libs.automl.metamining;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.mlplan.metamining.similaritymeasures.F1Optimizer;

public class F1OptimizerTest {

	@Test
	public void test() {

		/* invent some R and X matrices */
		INDArray vR = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 3 });
		INDArray vX = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 3 });

		/* learn the U matrix */
		F1Optimizer uLearner = new F1Optimizer();
		uLearner.build(vX, null, vR);

		INDArray vW = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 4, 2 });
		assertNotNull(uLearner.computeSimilarity(vX, vW));
	}

}
