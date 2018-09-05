package jaicore.basic.sets;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetUtilTest {
	
	private static final Logger logger = LoggerFactory.getLogger(jaicore.basic.sets.SetUtilTest.class);
	
	@Test
	public void testMappingCreation() throws InterruptedException {
		Collection<String> a = new ArrayList<>();
		a.add("D1");
		a.add("D2");
		a.add("D3");
		a.add("D4");
		a.add("D5");
		Collection<String> b = new ArrayList<>();
		b.add("R1");
		b.add("R2");
		b.add("R3");
		
		/* numerical test */
		assertTrue(SetUtil.allMappings(a, b, true, false, false).size() == Math.pow(b.size(), a.size()));
		assertTrue(SetUtil.allMappings(a, b, false, false, false).size() == Math.pow(1 + b.size(), a.size()));
		
		/* check injectivity */
		for (Map<String,String> map : SetUtil.allMappings(a, b, false, true, false)) {
			List<String> vals = new ArrayList<>();
			for (String k : map.keySet()) {
				String val = map.get(k);
				assertTrue("Mapping " + map + " should be injective but is not! It contains " + val + " at least twice.", !vals.contains(val));
				vals.add(val);
			}
		}
		
		/* check surjectivity */
		for (Map<String,String> map : SetUtil.allMappings(a, b, false, false, true)) {
			Set<String> vals = new HashSet<>(map.values());
			Collection<String> diff = SetUtil.difference(b, vals);
			assertTrue("Mapping " + map + " should be surjective but is not! The items " + diff + " are not hit!", diff.isEmpty());
		}
	}
	
	@Test
	public void testFilteredMapping() throws InterruptedException {
		
		int m = 5;
		int n = m;
		
		Set<Integer> a = new HashSet<>();
		for (int i = 1; i <= m; i++) {
			a.add(i);
		}
		Set<Integer> b = new HashSet<>();
		for (int i = 1; i <= n; i++) {
			b.add(i);
		}
		
		long start1 = System.currentTimeMillis();
		Set<Map<Integer,Integer>> maps = SetUtil.allTotalMappingsWithLocalConstraints(a, b, pair -> {
			return pair.getX() * pair.getY() % 2 == 0;
		});
//		Set<Map<Integer,Integer>> maps = SetUtil.allTotalMappingsWithConstraint(a, b, map -> {
//			for (Integer key : map.keySet()) {
//				if (key * map.get(key) % 2 != 0)
//					return false;
//			}
//			return true;
//		});
		long end1 = System.currentTimeMillis();

		
		System.out.println((end1 - start1) + "ms. Result: " + maps.size());
		
		Collection<Map<Integer,Integer>> maps2 = SetUtil.allMappings(a, b, true, false, false);
		Set<Map<Integer,Integer>> out2 = new HashSet<>();
		for (Map<Integer,Integer> map : maps2) {
			boolean insert = true;
			for (Integer key : map.keySet()) {
				if (key * map.get(key) % 2 != 0) {
					insert = false;
					break;
				}
			}
			if (insert)
				out2.add(map);
		}
		long end2 = System.currentTimeMillis();
		
		
		System.out.println(maps.size() + "/" + out2.size());
		System.out.println((end2 - end1) + "ms");
	}
	
	//@Test
	public void testcalculateNumberOfTotalOrderings() throws InterruptedException {
		final String a,b,c,d;
		a = "a";
		b = "b";
		c = "c";
		d = "d";
		
		final PartialOrderedSet<String> unorderedSet = new PartialOrderedSet<>();
		unorderedSet.add(a);
		unorderedSet.add(b);
		unorderedSet.add(c);
		unorderedSet.add(d);
		assertTrue(SetUtil.calculateNumberOfTotalOrderings(unorderedSet) == 24);
		
		final PartialOrderedSet<String> sortedSetA = new PartialOrderedSet<>();
		sortedSetA.add(a);
		sortedSetA.add(b);
		sortedSetA.add(c);
		sortedSetA.add(d);
		sortedSetA.addABeforeB(a, b);
		sortedSetA.addABeforeB(c, d);
		assertTrue(SetUtil.calculateNumberOfTotalOrderings(sortedSetA) == 6);
		
		final PartialOrderedSet<String> sortedSetB = new PartialOrderedSet<>();
		sortedSetB.add(a);
		sortedSetB.add(b);
		sortedSetB.add(c);
		sortedSetB.add(d);
		sortedSetB.addABeforeB(a, b);
		sortedSetB.addABeforeB(b, c);
		assertTrue(SetUtil.calculateNumberOfTotalOrderings(sortedSetB) == 4);
	}
}
