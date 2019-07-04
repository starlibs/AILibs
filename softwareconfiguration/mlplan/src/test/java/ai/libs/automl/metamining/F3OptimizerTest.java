package ai.libs.automl.metamining;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.mlplan.metamining.similaritymeasures.F3Optimizer;
import ai.libs.mlplan.metamining.similaritymeasures.IHeterogenousSimilarityMeasureComputer;

public class F3OptimizerTest {

	@Test
	public void optimizeF3() {
		INDArray r = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 4 });
		INDArray x = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 2, 4 });
		INDArray w = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8 }, new int[] { 4, 2 });

		/* learn the U matrix */
		IHeterogenousSimilarityMeasureComputer learner = new F3Optimizer(1);
		learner.build(x, w, r);
		assertNotNull(learner.computeSimilarity(x, w));
	}

}
