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

	private static final String A = "a";
	private static final String B = "b";
	private static final String C = "c";
	private static final String D = "d";
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
		this.set.addABeforeB(A, B);
		this.set.addABeforeB(A, C);
		this.set.addABeforeB(B, C);
		this.set.addABeforeB(C, D);
		this.transitiveClosureOfA.add(A);
		this.transitiveClosureOfA.add(B);
		this.transitiveClosureOfA.add(C);
		this.transitiveClosureOfA.add(D);

		this.transitiveClosureOfB.add(B);
		this.transitiveClosureOfB.add(C);
		this.transitiveClosureOfB.add(D);

		this.transitiveClosureOfC.add(C);
		this.transitiveClosureOfC.add(D);

		this.transitiveClosureOfD.add(D);
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
		assertTrue("Empty set should allow a before a, but doesn't.", !this.set.allowsABeforeB(A, A));
		assertTrue("Empty set should allow a before b, but doesn't.", this.set.allowsABeforeB(A, B));
		assertTrue("Empty set should allow a before c, but doesn't.", this.set.allowsABeforeB(A, C));
		assertTrue("Empty set should allow a before d, but doesn't.", this.set.allowsABeforeB(A, D));
		assertTrue("Empty set should allow b before a, but doesn't.", this.set.allowsABeforeB(B, A));
		assertTrue("Empty set should allow b before b, but doesn't.", !this.set.allowsABeforeB(B, B));
		assertTrue("Empty set should allow b before c, but doesn't.", this.set.allowsABeforeB(B, C));
		assertTrue("Empty set should allow b before d, but doesn't.", this.set.allowsABeforeB(B, D));
		assertTrue("Empty set should allow c before a, but doesn't.", this.set.allowsABeforeB(C, A));
		assertTrue("Empty set should allow c before b, but doesn't.", this.set.allowsABeforeB(C, B));
		assertTrue("Empty set should allow c before c, but doesn't.", !this.set.allowsABeforeB(C, C));
		assertTrue("Empty set should allow c before d, but doesn't.", this.set.allowsABeforeB(C, D));
		assertTrue("Empty set should allow d before a, but doesn't.", this.set.allowsABeforeB(D, A));
		assertTrue("Empty set should allow d before b, but doesn't.", this.set.allowsABeforeB(D, B));
		assertTrue("Empty set should allow d before c, but doesn't.", this.set.allowsABeforeB(D, C));
		assertTrue("Empty set should allow d before d, but doesn't.", !this.set.allowsABeforeB(D, D));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#addABeforeB(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test(expected = IllegalStateException.class)
	public void testAddABeforeB() {
		String e = "e";
		this.set.addABeforeB(A, e);
		this.set.addABeforeB(e, B);
		assertTrue("Since a before e was added, this should also be allowed.", this.set.allowsABeforeB(A, e));
		assertTrue("Since e before b was added, this should also be allowed.", this.set.allowsABeforeB(e, B));
		assertTrue("Since e before b was added, this should also be allowed.", this.set.allowsABeforeB(e, C));
		assertTrue("Since e before b was added, this should also be allowed.", this.set.allowsABeforeB(e, D));

		// negative test, because by transitivity this shouldn't be allowed.
		this.set.addABeforeB(C, e);
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#allowsABeforeB(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test
	public void testAllowsABeforeB() {
		assertTrue(this.set.allowsABeforeB(A, B));
		assertTrue(this.set.allowsABeforeB(A, C));
		assertTrue(this.set.allowsABeforeB(A, D));
		assertTrue(this.set.allowsABeforeB(B, C));
		assertTrue(this.set.allowsABeforeB(B, D));
		assertTrue(this.set.allowsABeforeB(C, D));
		assertFalse(this.set.allowsABeforeB(B, A));
		assertFalse(this.set.allowsABeforeB(C, A));
		assertFalse(this.set.allowsABeforeB(D, A));
		assertFalse(this.set.allowsABeforeB(C, B));
		assertFalse(this.set.allowsABeforeB(D, B));
		assertFalse(this.set.allowsABeforeB(D, C));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#getTransitiveClosure(java.lang.Object)}
	 * .
	 */
	@Test
	public void testGetTransitiveClosureE() {
		assertTrue(this.set.getTransitiveClosure(A).equals(this.transitiveClosureOfA));
		assertTrue(this.set.getTransitiveClosure(B).equals(this.transitiveClosureOfB));
		assertTrue(this.set.getTransitiveClosure(C).equals(this.transitiveClosureOfC));
		assertTrue(this.set.getTransitiveClosure(D).equals(this.transitiveClosureOfD));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#getTransitiveClosure(java.util.Set)}
	 * .
	 */
	@Test
	public void testGetTransitiveClosureSetOfE() throws InterruptedException {
		Set<String> everything = new HashSet<>();
		everything.add(A);
		everything.add(B);
		everything.add(C);
		everything.add(D);

		for (Collection<String> subsetAsCollection : SetUtil.powerset(everything)) {
			Set<String> subset = new HashSet<>(subsetAsCollection);
			Set<String> expectedTransitiveClosure = new HashSet<>();
			if (subset.contains(A)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfA);
			}
			if (subset.contains(B)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfB);
			}
			if (subset.contains(C)) {
				expectedTransitiveClosure.addAll(this.transitiveClosureOfC);
			}
			if (subset.contains(D)) {
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
		remover.add(A);
		remover.add(C);
		this.set.removeAll(remover);

		assertTrue("b wasn't removed but either anyway isn't part of the set.", this.set.contains(B));
		assertTrue("d wasn't removed but either anyway isn't part of the set.", this.set.contains(D));
		assertFalse("a was removed but is still part of the set.", this.set.contains(A));
		assertFalse("c was removed but is still part of the set.", this.set.contains(C));

		assertTrue("Since c was removed d before b should be allowed now.", this.set.allowsABeforeB(D, B));

	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#retainAll(java.util.Collection)}
	 * .
	 */
	@Test
	public void testRetainAllCollectionOfQ() {
		Set<String> remainingCollection = new HashSet<>();

		remainingCollection.add(B);
		remainingCollection.add(D);

		this.set.retainAll(remainingCollection);
		Set<String> remover = new HashSet<>();
		remover.add(A);
		remover.add(C);

		this.set.removeAll(remover);

		assertTrue("b wasn't removed but either anyway isn't part of the set.", this.set.contains(B));
		assertTrue("d wasn't removed but either anyway isn't part of the set.", this.set.contains(D));
		assertFalse("a was removed but is still part of the set.", this.set.contains(A));
		assertFalse("c was removed but is still part of the set.", this.set.contains(C));

		assertTrue("Since c was removed, d before b should be allowed now.", this.set.allowsABeforeB(D, B));

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
		assertTrue("Since c was removed from the set, d before a should be allowed but isn't.", this.set.allowsABeforeB(D, A));
		assertTrue("Since c was removed from the set, d before b should be allowed but isn't.", this.set.allowsABeforeB(D, B));
	}

	/**
	 * Test method for
	 * {@link de.upb.crc901.configurationsetting.util.PartialOrderedSet#remove(java.lang.Object)}
	 * .
	 */
	@Test
	public void testRemoveObject() {
		this.set.remove(C);

		assertFalse("c was removed but contains still says it's in.", this.set.contains("c"));
		assertTrue("Since c was removed from the set, d before a should be allowed but isn't.", this.set.allowsABeforeB(D, A));
		assertTrue("Since c was removed from the set, d before b should be allowed but isn't.", this.set.allowsABeforeB(D, B));
	}

}
