package jaicore.basic.sets;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class CartesianProductTest {

	@Test
	public void orderedIntegerTest() throws Exception {
		List<Integer> a = Arrays.asList(new Integer[] { 1, 3, 2 });
		List<Integer> b = Arrays.asList(new Integer[] { 5, 4, 6 });
		List<Integer> c = Arrays.asList(new Integer[] { 9, 8, 7 });
		List<Collection<Integer>> problem = new ArrayList<>();
		problem.add(a);
		problem.add(b);
		problem.add(c);
		int expectedSize = a.size() * b.size() * c.size();

		CartesianProductComputer<Integer> cpc = new CartesianProductComputer<>(problem);
//		assertEquals(expectedSize, cpc.call().size());
//		cpc.reset();
		List<List<Integer>> product = new ArrayList<>();
		for (AlgorithmEvent e : cpc) {
			if (e instanceof TupleFoundEvent) {
				System.out.println(((TupleFoundEvent) e).getTuple());
				product.add(((TupleFoundEvent) e).getTuple());
			}
		}
		
		assertEquals(expectedSize, product.size());
//		cpc.reset();
//		assertEquals(expectedSize, cpc.call().size());
	}

}
