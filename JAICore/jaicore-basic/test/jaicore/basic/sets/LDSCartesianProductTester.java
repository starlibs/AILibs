package jaicore.basic.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class LDSCartesianProductTester {

	@Test
	public void test() {
		List<Integer> a = Arrays.asList(new Integer[] {1,2,3});
		List<Integer> b = Arrays.asList(new Integer[] {4,5,6});
		List<Integer> c = Arrays.asList(new Integer[] {7,8,9});
		List<List<Integer>> collections = new ArrayList<>();
		collections.add(a);
		collections.add(b);
		collections.add(c);
		
		LDSBasedCartesianProductComputer<Integer> cpc = new LDSBasedCartesianProductComputer<>(collections);
		List<List<Integer>> cartesianProduct = cpc.call();
		int expected = a.size() * b.size() * c.size();
		assertEquals(expected, cartesianProduct.size());
		for (int i = 0; i < expected - 1; i++) {
			List<Integer> tuple1 = cartesianProduct.get(i);
			List<Integer> tuple2 = cartesianProduct.get(i + 1);
			assertTrue(computeDefficiency(collections, tuple1) <= computeDefficiency(collections, tuple2));
		}
	}
	
	private int computeDefficiency(List<List<Integer>> collections, List<Integer> tuple) {
		int defficiency = 0;
		for (int i = 0; i < tuple.size(); i++) {
			defficiency += collections.get(i).indexOf(tuple.get(i));
		}
		return defficiency;
	}
}
