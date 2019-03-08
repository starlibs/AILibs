package jaicore.ml.dyadranking.loss;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import junit.framework.Assert;

public class KendallsTauOfTopKTest {
	
	private static final double P = 0.5d;

	@Test
	public void test() {

		double inst1[] = { 1, 2, 3 };
		double alt1[] = { 1, 3, 9 };
		double inst2[] = { 4, 7, 7 };
		double alt2[] = { 4, 5, 6 };
		double inst3[] = { 4, 5, 2 };
		double alt3[] = { 4, 8, 7 };
		double inst4[] = { 1, 7, 5 };
		double alt4[] = { 1, 7, 2 };
		Dyad dyad1 = new Dyad(new DenseDoubleVector(inst1), new DenseDoubleVector(alt1));
		Dyad dyad2 = new Dyad(new DenseDoubleVector(inst2), new DenseDoubleVector(alt2));
		Dyad dyad3 = new Dyad(new DenseDoubleVector(inst3), new DenseDoubleVector(alt3));
		Dyad dyad4 = new Dyad(new DenseDoubleVector(inst4), new DenseDoubleVector(alt4));

		// Case 1 Case 1:i and j appear in both top k lists
		List<Dyad> dyadList = Arrays.asList(dyad1, dyad2, dyad3, dyad4);
		List<IDyadRankingInstance> instance = Arrays.asList(new DyadRankingInstance(dyadList));
		DyadRankingDataset trueOrdering = new DyadRankingDataset(instance);

		List<Dyad> dyadList2 = Arrays.asList(dyad1, dyad2, dyad4, dyad3);
		List<IDyadRankingInstance> instance2 = Arrays.asList(new DyadRankingInstance(dyadList2));
		DyadRankingDataset predictedOrdering = new DyadRankingDataset(instance2);

		double distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), trueOrdering,
				predictedOrdering);

		// distance should be 1 as the last two do not influence the ranking
		Assert.assertEquals(0.0d, distance, 0);

		dyadList2 = Arrays.asList(dyad2, dyad1, dyad3, dyad4);
		instance2 = Arrays.asList(new DyadRankingInstance(dyadList2));
		predictedOrdering = new DyadRankingDataset(instance2);

		distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), trueOrdering, predictedOrdering);

		//the ranking is wrong
		Assert.assertEquals(1.0d, distance);

		// Case 2 i,j in one ranking but just one in the other
		
		dyadList2 = Arrays.asList(dyad1, dyad3, dyad2, dyad4);
		instance2 = Arrays.asList(new DyadRankingInstance(dyadList2));
		predictedOrdering = new DyadRankingDataset(instance2);

		distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), trueOrdering, predictedOrdering);
		// for the pair 1,2 we should gain no loss (as 1 < 2 holds for the second pair aswell)
		// but 3 > 2 holds which is wrong!
		Assert.assertEquals(1.0d, distance);
	
		dyadList2 = Arrays.asList(dyad2, dyad3, dyad1, dyad4);
		instance2 = Arrays.asList(new DyadRankingInstance(dyadList2));
		predictedOrdering = new DyadRankingDataset(instance2);

		distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), trueOrdering, predictedOrdering);

		//only 2 appears the in the predicted ranking, thus, we know that 2 > 1 which is wrong
		// 1 > 3 and 2 > 1 hold which are bot wrong -> 2 pens
		Assert.assertEquals(2.0d, distance);
		
		// Case 3
		dyadList2 = Arrays.asList(dyad1, dyad3, dyad2, dyad4);
		instance2 = Arrays.asList(new DyadRankingInstance(dyadList2));
		predictedOrdering = new DyadRankingDataset(instance2);

		distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), trueOrdering, predictedOrdering);

		// 3 > 2 holds
		Assert.assertEquals(1.0d, distance);

		// Case 4
		// the top k lists are disjoint. For k = 2 we have 6 unordered pairs in total, 4 pairs for which both elements are in 
		// opposing top k lists (case 3) and 2 pairs for which both elements are in one top k list and none in the other (case 4).
		// With a penalty parameter of p = 0.5 we expect the overall distance to be 5.
		dyadList2 = Arrays.asList(dyad4, dyad3, dyad1, dyad2);
		instance2 = Arrays.asList(new DyadRankingInstance(dyadList2));
		predictedOrdering = new DyadRankingDataset(instance2);

		distance = DyadRankingLossUtil.computeAverageLoss(new KendallsTauOfTopK(2, P), trueOrdering, predictedOrdering);

		Assert.assertEquals(5.0d, distance);
		
	}

}
