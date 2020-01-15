package ai.libs.jaicore.ml.ranking.loss;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.junit.Test;

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.ranking.dyad.DyadRankingLossUtil;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;

public class KendallsTauOfTopKTest {

	private static final double P = 0.5d;

	private static final double INST_1[] = { 1, 2, 3 };
	private static final double ALT_1[] = { 1, 3, 9 };
	private static final double INST_2[] = { 4, 7, 7 };
	private static final double ALT_2[] = { 4, 5, 6 };
	private static final double INST_3[] = { 4, 5, 2 };
	private static final double ALT_3[] = { 4, 8, 7 };
	private static final double INST_4[] = { 1, 7, 5 };
	private static final double ALT_4[] = { 1, 7, 2 };

	private static final Dyad DYAD_1 = new Dyad(new DenseDoubleVector(INST_1), new DenseDoubleVector(ALT_1));
	private static final Dyad DYAD_2 = new Dyad(new DenseDoubleVector(INST_2), new DenseDoubleVector(ALT_2));
	private static final Dyad DYAD_3 = new Dyad(new DenseDoubleVector(INST_3), new DenseDoubleVector(ALT_3));
	private static final Dyad DYAD_4 = new Dyad(new DenseDoubleVector(INST_4), new DenseDoubleVector(ALT_4));

	private static final List<Dyad> GT_DYAD_LIST = Arrays.asList(DYAD_1, DYAD_2, DYAD_3, DYAD_4);
	private static final List<IRanking<?>> GT_RANKINGS = Arrays.asList(new Ranking<>(GT_DYAD_LIST));

	@Test
	public void testTop2Isolation() {
		// Case 1: i and j appear in both top k lists
		List<IDyad> dyadList = Arrays.asList(DYAD_1, DYAD_2, DYAD_4, DYAD_3);
		List<IRanking<?>> predBatch = Arrays.asList(new Ranking<>(dyadList));
		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch);

		List<Dyad> dyadList2 = Arrays.asList(DYAD_1, DYAD_2, DYAD_3, DYAD_4);
		List<IRanking<?>> predBatch2 = Arrays.asList(new Ranking<>(dyadList2));
		double distance2 = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch2);

		// distance should be 0 as the last two do not influence the ranking
		assertEquals(distance, distance2, 0.0);
	}

	@Test
	public void testTop2CorrectOrder() {
		// Case 1: i and j appear in both top k lists
		List<Dyad> dyadList2 = Arrays.asList(DYAD_1, DYAD_2, DYAD_4, DYAD_3);
		List<IRanking<?>> predBatch = Arrays.asList(new Ranking<>(dyadList2));
		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch);
		// distance should be 0 as the last two do not influence the ranking
		assertEquals(0.0d, distance, 0.0);
	}

	@Test
	public void testTop2WrongOrder() {
		// Case 1: i and j appear in both top k lists
		List<Dyad> dyadList2 = Arrays.asList(DYAD_2, DYAD_1, DYAD_3, DYAD_4);
		List<IRanking<?>> predBatch = Arrays.asList(new Ranking<>(dyadList2));
		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch);
		// distance should be 1 as the last two do not influence the ranking
		assertEquals(1.0d, distance, 0.0);
	}

	/**
	 * Case 2 i,j in one ranking but just one in the other
	 */
	@Test
	public void testOnlyOneTop2ElementFirst() {
		List<Dyad> dyadList2 = Arrays.asList(DYAD_1, DYAD_3, DYAD_2, DYAD_4);
		List<IRanking<?>> predBatch = Arrays.asList(new Ranking<>(dyadList2));
		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch);
		// for the pair 1,2 we should gain no loss (as 1 < 2 holds for the second pair as well)
		// but 3 > 2 holds which is wrong!
		assertEquals(1.0d, distance, 0.0);
	}

	/**
	 * Case 2 i,j in one ranking but just one in the other
	 */
	@Test
	public void testOnlyOneTop2ElementSecond() {
		List<Dyad> dyadList2 = Arrays.asList(DYAD_2, DYAD_3, DYAD_1, DYAD_4);
		List<IRanking<?>> predBatch = Arrays.asList(new Ranking<>(dyadList2));
		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch);
		// only 2 appears the in the predicted ranking, thus, we know that 2 > 1 which is wrong
		// 1 > 3 and 2 > 1 hold which are bot wrong -> 2 pens
		assertEquals(2.0d, distance, 0.0);
	}

	/**
	 * Case 4
	 * the top k lists are disjoint. For k = 2 we have 6 unordered pairs in total, 4 pairs for which both elements are in
	 * opposing top k lists (case 3) and 2 pairs for which both elements are in one top k list and none in the other (case 4).
	 * With a penalty parameter of p = 0.5 we expect the overall distance to be 5.
	 */
	@Test
	public void testWrongRanking() {
		List<Dyad> dyadList2 = Arrays.asList(DYAD_4, DYAD_3, DYAD_1, DYAD_2);
		List<IRanking<?>> predBatch = Arrays.asList(new Ranking<>(dyadList2));
		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), GT_RANKINGS, predBatch);
		assertEquals(5.0d, distance, 0.0);
	}

}
