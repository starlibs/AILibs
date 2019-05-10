package ai.libs.jaicore.basic.sets;

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
		this.set.addABeforeB(this.a, this.b);
		this.set.addABeforeB(this.a, this.c);
		this.set.addABeforeB(this.b, this.c);
		this.set.addABeforeB(this.c, this.d);
		this.transitiveClosureOfA.add(this.a);
		this.transitiveClosureOfA.add(this.b);
		this.transitiveClosureOfA.add(this.c);
		this.transitiveClosureOfA.add(this.d);

		this.transitiveClosureOfB.add(this.b);
		this.transitiveClosureOfB.add(this.c);
		this.transitiveClosureOfB.add(this.d);

		this.transitiveClosureOfC.add(this.c);
		this.transitiveClosureOfC.add(this.d);

		this.transitiveClosureOfD.add(this.d);
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#clear()}
	 * .
	 */
	@Test
	public void testClear() {
		this.set.clear();
		assertTrue("Even after clear the set wasn't empty.", this.set.isEmpty());
		assertTrue("Empty set should allow a before a, but doesn't.", !this.set.allowsABeforeB(this.a, this.a));
		assertTrue("Empty set should allow a before b, but doesn't.", this.set.allowsABeforeB(this.a, this.b));
		assertTrue("Empty set should allow a before c, but doesn't.", this.set.allowsABeforeB(this.a, this.c));
		assertTrue("Empty set should allow a before d, but doesn't.", this.set.allowsABeforeB(this.a, this.d));
		assertTrue("Empty set should allow b before a, but doesn't.", this.set.allowsABeforeB(this.b, this.a));
		assertTrue("Empty set should allow b before b, but doesn't.", !this.set.allowsABeforeB(this.b, this.b));
		assertTrue("Empty set should allow b before c, but doesn't.", this.set.allowsABeforeB(this.b, this.c));
		assertTrue("Empty set should allow b before d, but doesn't.", this.set.allowsABeforeB(this.b, this.d));
		assertTrue("Empty set should allow c before a, but doesn't.", this.set.allowsABeforeB(this.c, this.a));
		assertTrue("Empty set should allow c before b, but doesn't.", this.set.allowsABeforeB(this.c, this.b));
		assertTrue("Empty set should allow c before c, but doesn't.", !this.set.allowsABeforeB(this.c, this.c));
		assertTrue("Empty set should allow c before d, but doesn't.", this.set.allowsABeforeB(this.c, this.d));
		assertTrue("Empty set should allow d before a, but doesn't.", this.set.allowsABeforeB(this.d, this.a));
		assertTrue("Empty set should allow d before b, but doesn't.", this.set.allowsABeforeB(this.d, this.b));
		assertTrue("Empty set should allow d before c, but doesn't.", this.set.allowsABeforeB(this.d, this.c));
		assertTrue("Empty set should allow d before d, but doesn't.", !this.set.allowsABeforeB(this.d, this.d));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#addABeforeB(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test(expected = IllegalStateException.class)
	public void testAddABeforeB() {
		String e = "e";
		this.set.addABeforeB(this.a, e);
		this.set.addABeforeB(e, this.b);
		assertTrue("Since a before e was added, this should also be allowed.", this.set.allowsABeforeB(this.a, e));
		assertTrue("Since e before b was added, this should also be allowed.", this.set.allowsABeforeB(e, this.b));
		assertTrue("Since e before b was added, this should also be allowed.", this.set.allowsABeforeB(e, this.c));
		assertTrue("Since e before b was added, this should also be allowed.", this.set.allowsABeforeB(e, this.d));

		// negative test, because by transitivity this shouldn't be allowed.
		this.set.addABeforeB(this.c, e);
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#allowsABeforeB(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test
	public void testAllowsABeforeB() {
		assertTrue(this.set.allowsABeforeB(this.a, this.b));
		assertTrue(this.set.allowsABeforeB(this.a, this.c));
		assertTrue(this.set.allowsABeforeB(this.a, this.d));
		assertTrue(this.set.allowsABeforeB(this.b, this.c));
		assertTrue(this.set.allowsABeforeB(this.b, this.d));
		assertTrue(this.set.allowsABeforeB(this.c, this.d));
		assertFalse(this.set.allowsABeforeB(this.b, this.a));
		assertFalse(this.set.allowsABeforeB(this.c, this.a));
		assertFalse(this.set.allowsABeforeB(this.d, this.a));
		assertFalse(this.set.allowsABeforeB(this.c, this.b));
		assertFalse(this.set.allowsABeforeB(this.d, this.b));
		assertFalse(this.set.allowsABeforeB(this.d, this.c));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#getTransitiveClosure(java.lang.Object)}
	 * .
	 */
	@Test
	public void testGetTransitiveClosureE() {
		assertTrue(this.set.getTransitiveClosure(this.a).equals(this.transitiveClosureOfA));
		assertTrue(this.set.getTransitiveClosure(this.b).equals(this.transitiveClosureOfB));
		assertTrue(this.set.getTransitiveClosure(this.c).equals(this.transitiveClosureOfC));
		assertTrue(this.set.getTransitiveClosure(this.d).equals(this.transitiveClosureOfD));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#getTransitiveClosure(java.util.Set)}
	 * .
	 */
	@Test
	public void testGetTransitiveClosureSetOfE() throws InterruptedException {
		Set<String> everything = new HashSet<>();
		everything.add(this.a);
		everything.add(this.b);
		everything.add(this.c);
		everything.add(this.d);

		for (Collection<String> subsetAsCollection : SetUtil.powerset(everything)) {
			Set<String> subset = new HashSet<>(subsetAsCollection);
			Set<String> expectedTransitiveClosure = new HashSet<>();
			if (subset.contains(this.a)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfA);
			}
			if (subset.contains(this.b)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfB);
			}
			if (subset.contains(this.c)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfC);
			}
			if (subset.contains(this.d)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfD);
			}
			assertTrue("Expedted transitive closure of " + subset.toString() + " is " + expectedTransitiveClosure.toString() + " but calculated was " + this.set.getTransitiveClosure(subset),
					this.set.getTransitiveClosure(subset).equals(expectedTransitiveClosure));
		}
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#isReflexive()}
	 * .
	 */
	@Test
	public void testIsReflexive() {
		assertFalse("The given set isn't refelxive, but calculation says it is.", this.set.isReflexive());
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#removeAll(java.util.Collection)}
	 * .
	 */
	@Test
	public void testRemoveAllCollectionOfQ() {
		Set<String> remover = new HashSet<>();
		remover.add(this.a);
		remover.add(this.c);
		this.set.removeAll(remover);

		assertTrue("b wasn't removed but either anyway isn't part of the set.", this.set.contains(this.b));
		assertTrue("d wasn't removed but either anyway isn't part of the set.", this.set.contains(this.d));
		assertFalse("a was removed but is still part of the set.", this.set.contains(this.a));
		assertFalse("c was removed but is still part of the set.", this.set.contains(this.c));

		assertTrue("Since c was removed d before b should be allowed now.", this.set.allowsABeforeB(this.d, this.b));

	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#retainAll(java.util.Collection)}
	 * .
	 */
	@Test
	public void testRetainAllCollectionOfQ() {
		Set<String> remainingCollection = new HashSet<>();

		remainingCollection.add(this.b);
		remainingCollection.add(this.d);

		this.set.retainAll(remainingCollection);
		Set<String> remover = new HashSet<>();
		remover.add(this.a);
		remover.add(this.c);

		this.set.removeAll(remover);

		assertTrue("b wasn't removed but either anyway isn't part of the set.", this.set.contains(this.b));
		assertTrue("d wasn't removed but either anyway isn't part of the set.", this.set.contains(this.d));
		assertFalse("a was removed but is still part of the set.", this.set.contains(this.a));
		assertFalse("c was removed but is still part of the set.", this.set.contains(this.c));

		assertTrue("Since c was removed, d before b should be allowed now.", this.set.allowsABeforeB(this.d, this.b));

	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#removeIf(java.util.function.Predicate)}
	 * .
	 */
	@Test
	public void testRemoveIf() {
		this.set.removeIf(s -> "c".equals(s));
		assertFalse("c was removed but contains still says it's in.", this.set.contains("c"));
		assertTrue("Since c was removed from the set, d before a should be allowed but isn't.", this.set.allowsABeforeB(this.d, this.a));
		assertTrue("Since c was removed from the set, d before b should be allowed but isn't.", this.set.allowsABeforeB(this.d, this.b));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#remove(java.lang.Object)}
	 * .
	 */
	@Test
	public void testRemoveObject() {
		this.set.remove(this.c);

		assertFalse("c was removed but contains still says it's in.", this.set.contains("c"));
		assertTrue("Since c was removed from the set, d before a should be allowed but isn't.", this.set.allowsABeforeB(this.d, this.a));
		assertTrue("Since c was removed from the set, d before b should be allowed but isn't.", this.set.allowsABeforeB(this.d, this.b));
	}

}
