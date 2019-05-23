package jaicore.ml.dyadranking.general;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.IDyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.algorithm.featuretransform.FeatureTransformPLDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * Class that runs a simple functionality check on all dyad rankers.
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
@RunWith(Parameterized.class)
public class SimpleDyadDatasetDyadRankerTester {

	IDyadRanker ranker;

	private static Vector alternative1 = new DenseDoubleVector(new double[] { 1.0 });
	private static Vector alternative2 = new DenseDoubleVector(new double[] { 0.0 });

	public SimpleDyadDatasetDyadRankerTester(IDyadRanker ranker) {
		this.ranker = ranker;
	}

	public DyadRankingDataset supplySimpleDataset() {
		DyadRankingDataset dataset = new DyadRankingDataset();

		for (int i = 0; i <= 1; i++) {
			for (int j = 0; j <= 1; j++) {
				Vector instance1 = new DenseDoubleVector(new double[] { i, j, 0.0 });
				dataset.add(new SparseDyadRankingInstance(instance1, Arrays.asList(alternative1, alternative2)));
				Vector instance2 = new DenseDoubleVector(new double[] { i, j, 1.0 });
				dataset.add(new SparseDyadRankingInstance(instance2, Arrays.asList(alternative2, alternative1)));
			}
		}

		return dataset;
	}

	@Before
	public void trainRanker() throws TrainingException {
		ranker.train(supplySimpleDataset());
	}

	@Test
	public void testSwapOrdering0() throws PredictionException {
		System.out.println("Now testing if alternative2 > alternative1");
		Vector instance = new DenseDoubleVector(new double[] { 1.0, 1.0, 0.0 });
		SparseDyadRankingInstance test = new SparseDyadRankingInstance(instance,
				Arrays.asList(alternative2, alternative1));
		IDyadRankingInstance predict = ranker.predict(test);

		assertEquals(new double[] { 1.0 }, predict.getDyadAtPosition(0).getAlternative().asArray());
		assertEquals(new double[] { 0.0 }, predict.getDyadAtPosition(1).getAlternative().asArray());
	}

	@Test
	public void testSwapOrdering1() throws PredictionException {
		System.out.println("Now testing if alternative1 > alternative2");

		Vector instance = new DenseDoubleVector(new double[] { 1.0, 1.0, 1.0 });
		SparseDyadRankingInstance test = new SparseDyadRankingInstance(instance,
				Arrays.asList(alternative2, alternative1));
		IDyadRankingInstance predict = ranker.predict(test);

		assertEquals(new double[] { 0.0 }, predict.getDyadAtPosition(0).getAlternative().asArray());
		assertEquals(new double[] { 1.0 }, predict.getDyadAtPosition(1).getAlternative().asArray());
	}

	@Parameters
	public static List<IDyadRanker> supplyDyadRankers() {
		return Arrays.asList(new FeatureTransformPLDyadRanker(), new PLNetDyadRanker());
	}
}
