package ai.libs.jaicore.basic.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class SetUtilTest {

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
		for (Map<String, String> map : SetUtil.allMappings(a, b, false, true, false)) {
			List<String> vals = new ArrayList<>();
			for (String k : map.keySet()) {
				String val = map.get(k);
				assertTrue("Mapping " + map + " should be injective but is not! It contains " + val + " at least twice.", !vals.contains(val));
				vals.add(val);
			}
		}

		/* check surjectivity */
		for (Map<String, String> map : SetUtil.allMappings(a, b, false, false, true)) {
			Set<String> vals = new HashSet<>(map.values());
			Collection<String> diff = SetUtil.difference(b, vals);
			assertTrue("Mapping " + map + " should be surjective but is not! The items " + diff + " are not hit!", diff.isEmpty());
		}
	}

	@Test
	public void testLocallyFilteredTotalMappings() throws InterruptedException {

		/* create a = b with values 1,2,3,4,5 */
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

		/* compute all pairs with even product */
		Set<Map<Integer, Integer>> maps = SetUtil.allTotalMappingsWithLocalConstraints(a, b, pair -> pair.getX() * pair.getY() % 2 == 0);

		/* compute the same set manually */
		Collection<Map<Integer, Integer>> maps2 = SetUtil.allMappings(a, b, true, false, false);
		Set<Map<Integer, Integer>> out2 = new HashSet<>();
		for (Map<Integer, Integer> map : maps2) {
			boolean insert = true;
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				if (entry.getKey() * entry.getValue() % 2 != 0) {
					insert = false;
					break;
				}
			}
			if (insert) {
				out2.add(map);
			}
		}

		/* check that the maps are identical */
		assertEquals(out2, maps);
	}

	@Test
	public void testDifferenceEmptyPositiveCase() {
		Collection<String> a = new ArrayList<>();
		a.add("i1");
		a.add("i2");
		a.add("i3");
		Collection<String> b = new HashSet<>();
		b.add("i1");
		b.add("i2");
		b.add("i3");
		assertTrue("Difference IS empty, but SetUtil.differenceEmpty returns false", SetUtil.differenceEmpty(a, b));
	}

	@Test
	public void testDifferenceEmptyNegativeCase() {
		Collection<String> a = new ArrayList<>();
		a.add("i1");
		a.add("i2");
		a.add("i3");
		a.add("i4");
		a.add("i5");
		Collection<String> b = new ArrayList<>();
		b.add("i2");
		b.add("i3");
		assertTrue(!SetUtil.differenceEmpty(a, b));
	}

	@Test
	public void testDifferenceNotEmptyPositiveCase() {
		Collection<String> a = new ArrayList<>();
		a.add("i1");
		a.add("i2");
		a.add("i3");
		a.add("i4");
		a.add("i5");
		Collection<String> b = new ArrayList<>();
		b.add("i2");
		b.add("i3");
		assertTrue(SetUtil.differenceNotEmpty(a, b));
	}

	@Test
	public void testDifferenceNotEmptyNegativeCase() {
		Collection<String> a = new ArrayList<>();
		a.add("i1");
		a.add("i2");
		a.add("i3");
		Collection<String> b = new HashSet<>();
		b.add("i1");
		b.add("i2");
		b.add("i3");
		assertTrue("Difference IS empty, but SetUtil.differenceNotEmpty returns true", !SetUtil.differenceNotEmpty(a, b));
	}

	@Test
	public void testThatConstraintsAreAddedInPartialSet() throws InterruptedException {
		String a = "a";
		String b = "b";
		final PartialOrderedSet<String> set = new PartialOrderedSet<>();
		set.add(a);
		set.add(b);
		set.addABeforeB(a, b);
		assertTrue(set.getOrder().get(a).size() == 1);
		assertTrue(set.getOrder().get(a).contains(b));
	}

	@Test
	public void testThatObjectRemovalImpliesConstraintRemovalInPartialSet() throws InterruptedException {
		String a = "a";
		String b = "b";
		String c = "c";
		final PartialOrderedSet<String> set = new PartialOrderedSet<>();
		set.add(a);
		set.add(b);
		set.add(c);

		/* check that the relation is entirely removed if this was the only dependent element */
		set.addABeforeB(a, b);
		set.remove(b);
		assertFalse(set.getOrder().containsKey(a));

		/* check that the element is not in the constrained anymore  */
		set.add(b);
		set.addABeforeB(a, b);
		set.addABeforeB(a, c);
		set.remove(b);
		assertFalse(set.getOrder().get(a).contains(b));

		/* check that all forward dependencies are removed if the key is removed */
		set.add(b);
		set.addABeforeB(a, b);
		set.remove(a);
		assertFalse(set.getOrder().containsKey(a));
	}

	@Test
	public void testcalculateNumberOfTotalOrderings() throws InterruptedException {
		final String a, b, c, d;
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
