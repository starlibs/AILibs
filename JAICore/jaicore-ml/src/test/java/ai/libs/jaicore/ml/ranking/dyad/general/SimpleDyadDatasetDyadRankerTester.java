package ai.libs.jaicore.ml.ranking.dyad.general;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.featuretransform.FeatureTransformPLDyadRanker;

/**
 * Class that runs a simple functionality check on all dyad rankers.
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
@RunWith(Parameterized.class)
public class SimpleDyadDatasetDyadRankerTester {

	IDyadRanker ranker;

	private static IVector alternative1 = new DenseDoubleVector(new double[] { 1.0 });
	private static IVector alternative2 = new DenseDoubleVector(new double[] { 0.0 });

	public SimpleDyadDatasetDyadRankerTester(final IDyadRanker ranker) {
		this.ranker = ranker;
	}

	public DyadRankingDataset supplySimpleDataset() {
		DyadRankingDataset dataset = new DyadRankingDataset();

		for (int i = 0; i <= 1; i++) {
			for (int j = 0; j <= 1; j++) {
				IVector instance1 = new DenseDoubleVector(new double[] { i, j, 0.0 });
				dataset.add(new SparseDyadRankingInstance(instance1, Arrays.asList(alternative1, alternative2)));
				IVector instance2 = new DenseDoubleVector(new double[] { i, j, 1.0 });
				dataset.add(new SparseDyadRankingInstance(instance2, Arrays.asList(alternative2, alternative1)));
			}
		}

		return dataset;
	}

	@Before
	public void trainRanker() throws TrainingException, InterruptedException {
		this.ranker.fit(this.supplySimpleDataset());
	}

	@Test
	public void testSwapOrdering0() throws PredictionException, InterruptedException {
		System.out.println("Now testing if alternative2 > alternative1");
		IVector instance = new DenseDoubleVector(new double[] { 1.0, 1.0, 0.0 });
		SparseDyadRankingInstance test = new SparseDyadRankingInstance(instance, Arrays.asList(alternative2, alternative1));
		IRanking<IDyad> predict = this.ranker.predict(test);

		assertEquals(new double[] { 1.0 }, predict.get(0).getAlternative().asArray());
		assertEquals(new double[] { 0.0 }, predict.get(1).getAlternative().asArray());
	}

	@Test
	public void testSwapOrdering1() throws PredictionException, InterruptedException {
		System.out.println("Now testing if alternative1 > alternative2");

		IVector instance = new DenseDoubleVector(new double[] { 1.0, 1.0, 1.0 });
		SparseDyadRankingInstance test = new SparseDyadRankingInstance(instance, Arrays.asList(alternative2, alternative1));
		IRanking<IDyad> predicted = this.ranker.predict(test);

		assertEquals(new double[] { 0.0 }, predicted.get(0).getAlternative().asArray());
		assertEquals(new double[] { 1.0 }, predicted.get(1).getAlternative().asArray());
	}

	@Parameters
	public static List<IDyadRanker> supplyDyadRankers() {
		return Arrays.asList(new FeatureTransformPLDyadRanker(), new PLNetDyadRanker());
	}
}
