package jaicore.basic.sets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author David Niehues - davnie@mail.upb.de
 */
public class PartialOrderedSetTest {

	private final String a = "a";
	private final String b = "b";
	private final String c = "c";
	private final String d = "d";
	private PartialOrderedSet<String> set;
	private Set<String> transitiveClosureOfA = new HashSet<>();
	private Set<String> transitiveClosureOfB = new HashSet<>();
	private Set<String> transitiveClosureOfC = new HashSet<>();
	private Set<String> transitiveClosureOfD = new HashSet<>();

	/**
	 * Sets up set with a newly created {@link PartialOrderedSet} with the order
	 * a < b, a < c; b < c; c < d.
	 */
	@Before
	public void prepareSet() {
		this.set = new PartialOrderedSet<>();
		this.set.addABeforeB(a, b);
		this.set.addABeforeB(a, c);
		this.set.addABeforeB(b, c);
		this.set.addABeforeB(c, d);
		transitiveClosureOfA.add(a);
		transitiveClosureOfA.add(b);
		transitiveClosureOfA.add(c);
		transitiveClosureOfA.add(d);

		transitiveClosureOfB.add(b);
		transitiveClosureOfB.add(c);
		transitiveClosureOfB.add(d);

		transitiveClosureOfC.add(c);
		transitiveClosureOfC.add(d);

		transitiveClosureOfD.add(d);
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#clear()}
	 * .
	 */
	@Test
	public void testClear() {
		set.clear();
		assertTrue("Even after clear the set wasn't empty.", set.isEmpty());
		assertTrue("Empty set should allow a before a, but doesn't.", !set.allowsABeforeB(a, a));
		assertTrue("Empty set should allow a before b, but doesn't.", set.allowsABeforeB(a, b));
		assertTrue("Empty set should allow a before c, but doesn't.", set.allowsABeforeB(a, c));
		assertTrue("Empty set should allow a before d, but doesn't.", set.allowsABeforeB(a, d));
		assertTrue("Empty set should allow b before a, but doesn't.", set.allowsABeforeB(b, a));
		assertTrue("Empty set should allow b before b, but doesn't.", !set.allowsABeforeB(b, b));
		assertTrue("Empty set should allow b before c, but doesn't.", set.allowsABeforeB(b, c));
		assertTrue("Empty set should allow b before d, but doesn't.", set.allowsABeforeB(b, d));
		assertTrue("Empty set should allow c before a, but doesn't.", set.allowsABeforeB(c, a));
		assertTrue("Empty set should allow c before b, but doesn't.", set.allowsABeforeB(c, b));
		assertTrue("Empty set should allow c before c, but doesn't.", !set.allowsABeforeB(c, c));
		assertTrue("Empty set should allow c before d, but doesn't.", set.allowsABeforeB(c, d));
		assertTrue("Empty set should allow d before a, but doesn't.", set.allowsABeforeB(d, a));
		assertTrue("Empty set should allow d before b, but doesn't.", set.allowsABeforeB(d, b));
		assertTrue("Empty set should allow d before c, but doesn't.", set.allowsABeforeB(d, c));
		assertTrue("Empty set should allow d before d, but doesn't.", !set.allowsABeforeB(d, d));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#addABeforeB(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test(expected = IllegalStateException.class)
	public void testAddABeforeB() {
		String e = "e";
		set.addABeforeB(a, e);
		set.addABeforeB(e, b);
		assertTrue("Since a before e was added, this should also be allowed.", set.allowsABeforeB(a, e));
		assertTrue("Since e before b was added, this should also be allowed.", set.allowsABeforeB(e, b));
		assertTrue("Since e before b was added, this should also be allowed.", set.allowsABeforeB(e, c));
		assertTrue("Since e before b was added, this should also be allowed.", set.allowsABeforeB(e, d));

		// negative test, because by transitivity this shouldn't be allowed.
		set.addABeforeB(c, e);
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#allowsABeforeB(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test
	public void testAllowsABeforeB() {
		assertTrue(this.set.allowsABeforeB(a, b));
		assertTrue(this.set.allowsABeforeB(a, c));
		assertTrue(this.set.allowsABeforeB(a, d));
		assertTrue(this.set.allowsABeforeB(b, c));
		assertTrue(this.set.allowsABeforeB(b, d));
		assertTrue(this.set.allowsABeforeB(c, d));
		assertFalse(this.set.allowsABeforeB(b, a));
		assertFalse(this.set.allowsABeforeB(c, a));
		assertFalse(this.set.allowsABeforeB(d, a));
		assertFalse(this.set.allowsABeforeB(c, b));
		assertFalse(this.set.allowsABeforeB(d, b));
		assertFalse(this.set.allowsABeforeB(d, c));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#getTransitiveClosure(java.lang.Object)}
	 * .
	 */
	@Test
	public void testGetTransitiveClosureE() {
		assertTrue(this.set.getTransitiveClosure(a).equals(this.transitiveClosureOfA));
		assertTrue(this.set.getTransitiveClosure(b).equals(this.transitiveClosureOfB));
		assertTrue(this.set.getTransitiveClosure(c).equals(this.transitiveClosureOfC));
		assertTrue(this.set.getTransitiveClosure(d).equals(this.transitiveClosureOfD));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#getTransitiveClosure(java.util.Set)}
	 * .
	 */
	@Test
	public void testGetTransitiveClosureSetOfE() throws InterruptedException {
		Set<String> everything = new HashSet<>();
		everything.add(a);
		everything.add(b);
		everything.add(c);
		everything.add(d);

		for (Collection<String> subsetAsCollection : SetUtil.powerset(everything)) {
			Set<String> subset = new HashSet<>(subsetAsCollection);
			Set<String> expectedTransitiveClosure = new HashSet<>();
			if (subset.contains(a)) {
				expectedTransitiveClosure.addAll(transitiveClosureOfA);
			}
			if (subset.contains(b)) {
				expectedTransitiveClosure.addAll(transitiveClosureOfB);
			}
			if (subset.contains(c)) {
				expectedTransitiveClosure.addAll(transitiveClosureOfC);
			}
			if (subset.contains(d)) {
				expectedTransitiveClosure.addAll(transitiveClosureOfD);
			}
			assertTrue("Expedted transitive closure of " + subset.toString() + " is "
					+ expectedTransitiveClosure.toString() + " but calculated was " +
					set.getTransitiveClosure(subset),
					set.getTransitiveClosure(subset).equals(expectedTransitiveClosure));
		}
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#isReflexive()}
	 * .
	 */
	@Test
	public void testIsReflexive() {
		assertFalse("The given set isn't refelxive, but calculation says it is.", set.isReflexive());
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#removeAll(java.util.Collection)}
	 * .
	 */
	@Test
	public void testRemoveAllCollectionOfQ() {
		Set<String> remover = new HashSet<>();
		remover.add(a);
		remover.add(c);
		this.set.removeAll(remover);
		
		assertTrue("b wasn't removed but either anyway isn't part of the set.", this.set.contains(b));
		assertTrue("d wasn't removed but either anyway isn't part of the set.", this.set.contains(d));
		assertFalse("a was removed but is still part of the set.", this.set.contains(a));
		assertFalse("c was removed but is still part of the set.", this.set.contains(c));
		
		assertTrue("Since c was removed d before b should be allowed now.", this.set.allowsABeforeB(d, b));
		
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#retainAll(java.util.Collection)}
	 * .
	 */
	@Test
	public void testRetainAllCollectionOfQ() {
		Set<String> remainingCollection = new HashSet<>();

		remainingCollection.add(b);
		remainingCollection.add(d);
		
		set.retainAll(remainingCollection);
		Set<String> remover = new HashSet<>();
		remover.add(a);
		remover.add(c);
		
		this.set.removeAll(remover);
		
		assertTrue("b wasn't removed but either anyway isn't part of the set.", this.set.contains(b));
		assertTrue("d wasn't removed but either anyway isn't part of the set.", this.set.contains(d));
		assertFalse("a was removed but is still part of the set.", this.set.contains(a));
		assertFalse("c was removed but is still part of the set.", this.set.contains(c));
		
		assertTrue("Since c was removed, d before b should be allowed now.", this.set.allowsABeforeB(d, b));
		
		
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#removeIf(java.util.function.Predicate)}
	 * .
	 */
	@Test
	public void testRemoveIf() {
		set.removeIf(s -> "c".equals(s));
		assertFalse("c was removed but contains still says it's in.", set.contains("c"));
		assertTrue("Since c was removed from the set, d before a should be allowed but isn't.", set.allowsABeforeB(d, a));
		assertTrue("Since c was removed from the set, d before b should be allowed but isn't.", set.allowsABeforeB(d, b));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#remove(java.lang.Object)}
	 * .
	 */
	@Test
	public void testRemoveObject() {
		set.remove(c);
		
		assertFalse("c was removed but contains still says it's in.", set.contains("c"));
		assertTrue("Since c was removed from the set, d before a should be allowed but isn't.", set.allowsABeforeB(d, a));
		assertTrue("Since c was removed from the set, d before b should be allowed but isn't.", set.allowsABeforeB(d, b));
	}

}
