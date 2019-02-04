package jaicore.basic.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.HomogeneousGeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithmFactory;

public class LDSRelationComputerTester extends HomogeneousGeneralAlgorithmTester<RelationComputationProblem<Integer>, List<List<Integer>>> {

	@Test
	public void testOutputSizeForCartesianProducts() throws Exception {
		RelationComputationProblem<Integer> problem = getCartesianProductProblem();
		int expected = problem.getSets().get(0).size() * problem.getSets().get(1).size() * problem.getSets().get(2).size();
		testRelation(problem, expected);
	}
	
	@Test
	public void testOutputSizeForNonEmptyRelation() throws Exception {
		RelationComputationProblem<Integer> problem = getSimpleProblemInputForGeneralTestPurposes();
		List<List<Integer>> cartesianProduct = new LDSRelationComputer<>(getCartesianProductProblem()).call();
		List<List<Integer>> groundTruth = cartesianProduct.stream().filter(problem.getPrefixFilter()).collect(Collectors.toList());
//		cartesianProduct.forEach(t -> System.out.println(t));
		testRelation(problem, groundTruth.size());
	}
	
	@Test
	public void testOutputSizeForEmptyRelation() throws Exception {
		testRelation(getInfeasibleRelationProblem(), 0);
	}
	
	@Test
	public void testOutputSizeForPrunedRelation() throws Exception {
		testRelation(getInfeasibleCompletelyPrunedRelationProblem(), 0);
	}
	
	private void testRelation(RelationComputationProblem<Integer> problem, int expected) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException  {
		LDSRelationComputer<Integer> cpc = new LDSRelationComputer<>(problem);
		List<List<Integer>> relation = cpc.call();
		assertEquals(expected, relation.size()); // the size of the output must be correct
		for (int i = 0; i < expected - 1; i++) {
			List<Integer> tuple1 = relation.get(i);
			assertEquals(problem.getSets().size(), tuple1.size());
			List<Integer> tuple2 = relation.get(i + 1);
			assertEquals(problem.getSets().size(), tuple2.size());
			int d1 = computeDefficiency(problem.getSets(), tuple1);
			int d2 = computeDefficiency(problem.getSets(), tuple2);
			assertTrue(d1 <= d2);
		}
	}

	private int computeDefficiency(List<? extends Collection<Integer>> collections, List<Integer> tuple) {
		int defficiency = 0;
		for (int i = 0; i < tuple.size(); i++) {
			List<Integer> ithSet = (List<Integer>)collections.get(i);
			defficiency += ithSet.indexOf(tuple.get(i));
		}
		return defficiency;
	}
	
	@Override
	public RelationComputationProblem<Integer> getSimpleProblemInputForGeneralTestPurposes() {
		List<Integer> a = Arrays.asList(new Integer[] { 1, 2, 3 });
		List<Integer> b = Arrays.asList(new Integer[] { 4, 5, 6 });
		List<Integer> c = Arrays.asList(new Integer[] { 7, 8, 9 });
		List<Collection<Integer>> collections = new ArrayList<>();
		collections.add(a);
		collections.add(b);
		collections.add(c);
		return new RelationComputationProblem<>(collections, t -> t.size() < 2 || t.get(0) + 3 == t.get(1));
	}

	@Override
	public RelationComputationProblem<Integer> getDifficultProblemInputForGeneralTestPurposes() {
		List<Collection<Integer>> collections = new ArrayList<>();
		List<Integer> collection = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			collection.add(i);
			if (collection.size() > 2)
				collections.add(new ArrayList<>(collection));
		}
		return new RelationComputationProblem<>(collections, t -> t.size() < 2|| t.get(0) + 3 == t.get(1));
	}
	
	public RelationComputationProblem<Integer> getCartesianProductProblem() {
		return new RelationComputationProblem<>(getSimpleProblemInputForGeneralTestPurposes().getSets()); // remove the filter condition
	}
	
	public RelationComputationProblem<Integer> getInfeasibleRelationProblem() {
		return new RelationComputationProblem<>(getSimpleProblemInputForGeneralTestPurposes().getSets(), t -> t.size() < 3); // all full tuples are forbidden
	}
	
	public RelationComputationProblem<Integer> getInfeasibleCompletelyPrunedRelationProblem() {
		return new RelationComputationProblem<>(getSimpleProblemInputForGeneralTestPurposes().getSets(), t -> false); // all tuples are forbidden
	}

	@Override
	public IAlgorithmFactory<RelationComputationProblem<Integer>, List<List<Integer>>> getFactory() {
		return new LDSRelationComputerFactory<Integer>();
	}
}
