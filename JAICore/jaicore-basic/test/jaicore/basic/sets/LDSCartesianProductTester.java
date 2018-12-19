package jaicore.basic.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithmFactory;

public class LDSCartesianProductTester extends GeneralAlgorithmTester<List<? extends Collection<Integer>>, List<? extends Collection<Integer>>, List<List<Integer>>> {

	@Test
	public void testOutputSize() throws Exception {
		List<List<Integer>> collections = getSimpleProblemInputForGeneralTestPurposes();
		LDSBasedCartesianProductComputer<Integer> cpc = new LDSBasedCartesianProductComputer<>(collections);
		List<List<Integer>> cartesianProduct = cpc.call();
		int expected = collections.get(0).size() * collections.get(1).size() * collections.get(2).size();
		assertEquals(expected, cartesianProduct.size()); // the size of the output must be correct
		for (int i = 0; i < expected - 1; i++) {
			List<Integer> tuple1 = cartesianProduct.get(i);
			List<Integer> tuple2 = cartesianProduct.get(i + 1);
			int d1 = computeDefficiency(collections, tuple1);
			int d2 = computeDefficiency(collections, tuple2);
			assertTrue(d1 <= d2);
		}
	}

	private int computeDefficiency(List<List<Integer>> collections, List<Integer> tuple) {
		int defficiency = 0;
		for (int i = 0; i < tuple.size(); i++) {
			defficiency += collections.get(i).indexOf(tuple.get(i));
		}
		return defficiency;
	}

	@Override
	public AlgorithmProblemTransformer<List<? extends Collection<Integer>>, List<? extends Collection<Integer>>> getProblemReducer() {
		return r -> r;
	}

	@Override
	public IAlgorithmFactory<List<? extends Collection<Integer>>, List<List<Integer>>> getFactory() {
		return new LDSCartesianProductComputerFactory<>();
	}
	
	@Override
	public List<List<Integer>> getSimpleProblemInputForGeneralTestPurposes() {
		List<Integer> a = Arrays.asList(new Integer[] { 1, 2, 3 });
		List<Integer> b = Arrays.asList(new Integer[] { 4, 5, 6 });
		List<Integer> c = Arrays.asList(new Integer[] { 7, 8, 9 });
		List<List<Integer>> collections = new ArrayList<>();
		collections.add(a);
		collections.add(b);
		collections.add(c);
		return collections;
	}

	@Override
	public List<? extends Collection<Integer>> getDifficultProblemInputForGeneralTestPurposes() {
		List<List<Integer>> collections = new ArrayList<>();
		List<Integer> collection = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			collection.add(i);
			if (collection.size() > 2)
				collections.add(new ArrayList<>(collection));
		}
		return collections;
	}
}
