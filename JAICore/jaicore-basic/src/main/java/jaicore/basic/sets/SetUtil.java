package jaicore.basic.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import jaicore.basic.IGetter;
import jaicore.basic.MathExt;

/**
 * Utility class for sets.
 *
 * @author fmohr, mbunse
 */
public class SetUtil {

	public static class Pair<X, Y> {
		private X x;
		private Y y;

		public Pair(final X x, final Y y) {
			super();
			this.x = x;
			this.y = y;
		}

		public X getX() {
			return this.x;
		}

		public Y getY() {
			return this.y;
		}

		@Override
		public String toString() {
			return "<" + this.x + ", " + this.y + ">";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.x == null) ? 0 : this.x.hashCode());
			result = prime * result + ((this.y == null) ? 0 : this.y.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Pair<X, Y> other = (Pair<X, Y>) obj;
			if (this.x == null) {
				if (other.x != null) {
					return false;
				}
			} else if (!this.x.equals(other.x)) {
				return false;
			}
			if (this.y == null) {
				if (other.y != null) {
					return false;
				}
			} else if (!this.y.equals(other.y)) {
				return false;
			}
			return true;
		}
	}

	/* BASIC SET OPERATIONS */
	@SafeVarargs
	public static <T> Collection<T> union(final Collection<T>... set) {
		Collection<T> union = new HashSet<>();
		for (int i = 0; i < set.length; i++) {
			union.addAll(set[i]);
		}
		return union;
	}

	public static <T> Collection<T> symmetricDifference(final Collection<T> a, final Collection<T> b) {
		return SetUtil.union(SetUtil.difference(a, b), SetUtil.difference(b, a));
	}

	public static <T> Collection<T> getMultiplyContainedItems(final List<T> list) {
		Set<T> doubleEntries = new HashSet<>();
		Set<T> observed = new HashSet<>();
		for (T item : list) {
			if (observed.contains(item)) {
				doubleEntries.add(item);
			} else {
				observed.add(item);
			}
		}
		return doubleEntries;
	}

	/**
	 * @param a
	 *            The set A.
	 * @param b
	 *            The set B.
	 * @return The intersection of sets A and B.
	 */
	public static <S, T extends S, U extends S> Collection<S> intersection(final Collection<T> a, final Collection<U> b) {
		List<S> out = new ArrayList<>();
		Collection<? extends S> bigger = a.size() < b.size() ? b : a;
		for (S item : ((a.size() >= b.size()) ? b : a)) {
			if (bigger.contains(item)) {
				out.add(item);
			}
		}
		return out;
	}

	public static <S, T extends S, U extends S> boolean disjoint(final Collection<T> a, final Collection<U> b) {
		Collection<? extends S> bigger = a.size() < b.size() ? b : a;
		for (S item : ((a.size() >= b.size()) ? b : a)) {
			if (bigger.contains(item)) {
				return false;
			}
		}
		return true;
	}

	public static <T> Collection<Collection<T>> getPotenceOfSet(final Collection<T> set, final byte exponent) {
		Collection<Collection<T>> items = new ArrayList<Collection<T>>();
		for (byte i = 0; i < exponent; i++) {
			items.add(set);
		}
		return getCartesianProductOfSetsOfSameClass(items);
	}

	public static <T> Collection<Collection<T>> getCartesianProductOfSetsOfSameClass(final Collection<Collection<T>> items) {

		/* recursion abortion */
		if (items.isEmpty()) {
			return new ArrayList<Collection<T>>();
		}
		if (items.size() == 1) {
			Collection<Collection<T>> tuples = new ArrayList<Collection<T>>();
			for (Collection<T> set : items) { // only one run exists here
				for (T value : set) {
					Collection<T> trivialTuple = new ArrayList<T>();
					trivialTuple.add(value);
					tuples.add(trivialTuple);
				}
			}
			return tuples;
		}

		/* compute cartesian product of n-1 */
		Collection<Collection<T>> subproblem = new ArrayList<Collection<T>>();
		Collection<T> unconsideredDomain = null;
		int i = 0, limit = items.size();
		for (Collection<T> set : items) {
			if (i < limit - 1) {
				subproblem.add(set);
			} else if (i == limit - 1) {
				unconsideredDomain = set;
				break;
			}
			i++;
		}
		Collection<Collection<T>> subsolution = getCartesianProductOfSetsOfSameClass(subproblem);

		/* compute solution */
		Collection<Collection<T>> solution = new ArrayList<Collection<T>>();
		for (Collection<T> tuple : subsolution) {
			for (T value : unconsideredDomain) {
				List<T> newTuple = new ArrayList<T>();
				newTuple.addAll(tuple);
				newTuple.add(value);
				solution.add(newTuple);
			}
		}
		return solution;
	}

	/* SUBSETS */
	public static <T> Collection<Collection<T>> powerset(final Collection<T> items) throws InterruptedException {
		/* |M| = 0 */
		if (items.isEmpty()) {
			Collection<Collection<T>> setWithEmptySet = new ArrayList<Collection<T>>();
			setWithEmptySet.add(new ArrayList<T>());
			return setWithEmptySet;
		}

		/* |M| >= 1 */
		T baseElement = null;
		Collection<T> restList = new ArrayList<T>();
		int i = 0;
		for (T item : items) {
			if (i == 0) {
				baseElement = item;
			} else {
				restList.add(item);
			}
			i++;
		}
		Collection<Collection<T>> subsets = powerset(restList);
		Collection<Collection<T>> toAdd = new ArrayList<Collection<T>>();
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted during calculation of power set");
		}
		for (Collection<T> existingSubset : subsets) {
			Collection<T> additionalList = new ArrayList<T>();
			additionalList.addAll(existingSubset);
			additionalList.add(baseElement);
			toAdd.add(additionalList);
		}
		subsets.addAll(toAdd);
		return subsets;
	}

	public static <T> Collection<Collection<T>> getAllPossibleSubsets(final Collection<T> items) {

		/* |M| = 0 */
		if (items.isEmpty()) {
			Collection<Collection<T>> setWithEmptySet = new ArrayList<Collection<T>>();
			setWithEmptySet.add(new ArrayList<T>());
			return setWithEmptySet;
		}

		/* |M| >= 1 */
		T baseElement = null;
		Collection<T> restList = new ArrayList<T>();
		int i = 0;
		for (T item : items) {
			if (i == 0) {
				baseElement = item;
			} else {
				restList.add(item);
			}
			i++;
		}
		Collection<Collection<T>> subsets = getAllPossibleSubsets(restList);
		Collection<Collection<T>> toAdd = new ArrayList<Collection<T>>();
		for (Collection<T> existingSubset : subsets) {
			Collection<T> additionalList = new ArrayList<T>();
			additionalList.addAll(existingSubset);
			additionalList.add(baseElement);
			toAdd.add(additionalList);
		}
		subsets.addAll(toAdd);
		return subsets;
	}

	private static class SubSetComputer<T> implements Runnable {

		private List<T> superSet;
		private ExecutorService pool;
		private int k;
		private int idx;
		private Set<T> current;
		private List<Set<T>> allSolutions;
		private Semaphore semThreads, semComplete;
		private long goalSize;

		public SubSetComputer(final List<T> superSet, final int k, final int idx, final Set<T> current, final List<Set<T>> allSolutions, final ExecutorService pool, final Semaphore sem, final long goalSize, final Semaphore semComplete) {
			super();
			this.superSet = superSet;
			this.pool = pool;
			this.k = k;
			this.idx = idx;
			this.current = current;
			this.allSolutions = allSolutions;
			this.semThreads = sem;
			this.semComplete = semComplete;
			this.goalSize = goalSize;
		}

		@Override
		public void run() {
			List<Set<T>> localSolutions = new ArrayList<>();
			this.performStep(this.superSet, this.k, this.idx, this.current, localSolutions);
			synchronized (this.allSolutions) {
				this.allSolutions.addAll(localSolutions);
				if (this.allSolutions.size() == this.goalSize) {
					this.semComplete.release();
				}
			}
			this.semThreads.release();
		}

		public void performStep(final List<T> superSet, final int k, final int idx, final Set<T> current, final List<Set<T>> solution) {

			// successful stop clause
			if (current.size() == k) {
				solution.add(new HashSet<>(current));
				return;
			}
			// unseccessful stop clause
			if (idx == superSet.size()) {
				return;
			}
			T x = superSet.get(idx);
			current.add(x);

			// "guess" x is in the subset
			if (this.semThreads.tryAcquire()) {

				/* outsource first task in a new thread */
				this.pool.submit(new SubSetComputer<T>(superSet, k, idx + 1, new HashSet<>(current), this.allSolutions, this.pool, this.semThreads, this.goalSize, this.semComplete));

				/* also try to outsorce the second task into its own thread */
				current.remove(x);
				if (this.semThreads.tryAcquire()) {
					this.pool.submit(new SubSetComputer<T>(superSet, k, idx + 1, new HashSet<>(current), this.allSolutions, this.pool, this.semThreads, this.goalSize, this.semComplete));
				} else {

					/* solve the second task in this same thread */
					this.performStep(superSet, k, idx + 1, current, solution);
				}
			} else {
				this.performStep(superSet, k, idx + 1, current, solution);
				current.remove(x);

				/* now check if a new thread is available for the second task */
				if (this.semThreads.tryAcquire()) {
					this.pool.submit(new SubSetComputer<T>(superSet, k, idx + 1, new HashSet<>(current), this.allSolutions, this.pool, this.semThreads, this.goalSize, this.semComplete));
				} else {
					this.performStep(superSet, k, idx + 1, current, solution);
				}
			}
		}
	}

	public static <T> Collection<Set<T>> subsetsOfSize(final Collection<T> set, final int size) throws InterruptedException {
		List<Set<T>> subsets = new ArrayList<>();
		List<T> setAsList = new ArrayList<T>(); // for easier access
		setAsList.addAll(set);
		getSubsetOfSizeRec(setAsList, size, 0, new HashSet<T>(), subsets);
		return subsets;
	}

	private static <T> void getSubsetOfSizeRec(final List<T> superSet, final int k, final int idx, final Set<T> current, final Collection<Set<T>> solution) throws InterruptedException {
		// successful stop clause
		if (current.size() == k) {
			solution.add(new HashSet<>(current));
			return;
		}
		// unseccessful stop clause
		if (idx == superSet.size()) {
			return;
		}
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted during calculation of subsets with special size");
		}
		T x = superSet.get(idx);
		current.add(x);
		// "guess" x is in the subset
		getSubsetOfSizeRec(superSet, k, idx + 1, current, solution);
		current.remove(x);
		// "guess" x is not in the subset
		getSubsetOfSizeRec(superSet, k, idx + 1, current, solution);
	}

	public static <T> List<Set<T>> getAllPossibleSubsetsWithSizeParallely(final Collection<T> superSet, final int k) {
		List<Set<T>> res = new ArrayList<>();
		int n = 1;
		ExecutorService pool = Executors.newFixedThreadPool(n);
		Semaphore solutionSemaphore = new Semaphore(1);
		try {
			solutionSemaphore.acquire();
			pool.submit(new SubSetComputer<T>(new ArrayList<>(superSet), k, 0, new HashSet<T>(), res, pool, new Semaphore(n - 1), MathExt.binomial(superSet.size(), k), solutionSemaphore));
			solutionSemaphore.acquire();
			pool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return res;
	}

	private static <T> void getAllPossibleSubsetsWithSizeRecursive(final List<T> superSet, final int k, final int idx, final Set<T> current, final List<Set<T>> solution) {
		// successful stop clause
		if (current.size() == k) {
			solution.add(new HashSet<>(current));
			return;
		}
		// unseccessful stop clause
		if (idx == superSet.size()) {
			return;
		}
		T x = superSet.get(idx);
		current.add(x);
		// "guess" x is in the subset
		getAllPossibleSubsetsWithSizeRecursive(superSet, k, idx + 1, current, solution);
		current.remove(x);
		// "guess" x is not in the subset
		getAllPossibleSubsetsWithSizeRecursive(superSet, k, idx + 1, current, solution);
	}

	public static <T> List<Set<T>> getAllPossibleSubsetsWithSize(final Collection<T> superSet, final int k) {
		List<Set<T>> res = new ArrayList<>();
		getAllPossibleSubsetsWithSizeRecursive(new ArrayList<>(superSet), k, 0, new HashSet<T>(), res);
		return res;
	}

	public static <T> Collection<List<T>> getPermutations(final Collection<T> set) {
		Collection<List<T>> permutations = new ArrayList<>();
		List<T> setAsList = new ArrayList<>(set);
		getPermutationsRec(setAsList, 0, permutations);
		return permutations;
	}

	private static <T> void getPermutationsRec(final List<T> list, final int pointer, final Collection<List<T>> solution) {
		if (pointer == list.size()) {
			solution.add(list);
			return;
		}
		for (int i = pointer; i < list.size(); i++) {
			List<T> permutation = new ArrayList<>(list);
			permutation.set(pointer, list.get(i));
			permutation.set(i, list.get(pointer));
			getPermutationsRec(permutation, pointer + 1, solution);
		}
	}

	/**
	 * @param a
	 *            The set A.
	 * @param b
	 *            The set B.
	 * @return The difference A \ B.
	 */
	public static <S, T extends S, U extends S> Collection<S> difference(final Collection<T> a, final Collection<U> b) {

		List<S> out = new ArrayList<>();

		for (S item : a) {
			if (b == null || !b.contains(item)) {
				out.add(item);
			}
		}

		return out;
	}

	/**
	 * @param a
	 *            The set A.
	 * @param b
	 *            The set B.
	 * @return The difference A \ B.
	 */
	public static <S, T extends S, U extends S> List<S> difference(final List<T> a, final Collection<U> b) {

		List<S> out = new ArrayList<>();

		for (S item : a) {
			if (b == null || !b.contains(item)) {
				out.add(item);
			}
		}

		return out;
	}

	public static <S, T extends S, U extends S> boolean differenceEmpty(final Collection<T> a, final Collection<U> b) {
		if (a == null || a.isEmpty()) {
			return true;
		}
		for (S item : a) {
			if (!b.contains(item)) {
				return false;
			}
		}
		return true;
	}

	public static <S, T extends S, U extends S> boolean differenceNotEmpty(final Collection<T> a, final Collection<U> b) {
		if (b == null) {
			return !a.isEmpty();
		}
		for (S item : a) {
			if (!b.contains(item)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param a
	 *            The set A.
	 * @param b
	 *            The set B.
	 * @return The Cartesian product A x B.
	 */
	public static <S, T> Collection<Pair<S, T>> cartesianProduct(final Collection<S> a, final Collection<T> b) {
		Set<Pair<S, T>> product = new HashSet<>();

		for (S item1 : a) {
			for (T item2 : b) {
				product.add(new Pair<S, T>(item1, item2));
			}
		}
		return product;
	}

	/**
	 * @param a
	 *            The set A.
	 * @param b
	 *            The set B.
	 * @return The Cartesian product A x B.
	 */
	public static <T> Collection<List<T>> cartesianProduct(final List<? extends Collection<T>> listOfSets) {

		/* compute expected number of items of the result */
		int expectedSize = 1;
		for (Collection<T> items : listOfSets) {
			assert items.size() == new HashSet<>(items).size() : "One of the collection is effectively a multi-set, which is forbidden for CP computation: " + items;
			expectedSize *= items.size();
		}

		/* there must be at least one set */
		if (listOfSets.isEmpty()) {
			throw new IllegalArgumentException("Empty list of sets");
		}

		/*
		 * if there is only one set, create tuples of size 1 and return the set of tuples
		 */
		if (listOfSets.size() == 1) {
			Set<List<T>> product = new HashSet<>();
			for (T obj : listOfSets.get(0)) {
				List<T> tupleOfSize1 = new ArrayList<>();
				tupleOfSize1.add(obj);
				product.add(tupleOfSize1);
			}
			assert product.size() == expectedSize : "Invalid number of expected entries! Expected " + expectedSize + " but computed " + product.size() + " for a single set: " + listOfSets.get(0);
			return product;
		}

		/*
		 * if there are more sets, remove the last one, compute the cartesian for the rest, and append the removed one afterwards
		 */
		Collection<T> removed = listOfSets.get(listOfSets.size() - 1);
		listOfSets.remove(listOfSets.size() - 1);
		Collection<List<T>> subSolution = cartesianProduct(listOfSets);
		Set<List<T>> product = new HashSet<>();
		for (List<T> tuple : subSolution) {
			for (T item : removed) {
				List<T> newTuple = new ArrayList<>(tuple);
				newTuple.add(item);
				product.add(newTuple);
			}
		}
		assert product.size() == expectedSize : "Invalid number of expected entries! Expected " + expectedSize + " but computed " + product.size();
		return product;
	}

	/**
	 * @param a
	 *            The set A.
	 * @throws InterruptedException
	 */
	public static <S> Collection<List<S>> cartesianProduct(final Collection<S> set, final int number) throws InterruptedException {
		List<List<S>> product = new ArrayList<>();
		List<S> setAsList = new ArrayList<>(set);
		if (number <= 1) {
			for (S elem : set) {
				List<S> tuple = new ArrayList<S>();
				tuple.add(elem);
				product.add(tuple);
			}
			return product;
		}
		for (List<S> restProduct : cartesianProduct(setAsList, number - 1)) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
			for (S elem : set) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException();
				}
				List<S> tuple = new ArrayList<>(restProduct.size() + 1);
				for (S elementOfRestProduct : restProduct) {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException();
					}
					tuple.add(elementOfRestProduct);
				}
				tuple.add(0, elem);
				product.add(tuple);
			}
		}
		return product;
	}

	/* RELATIONS */
	public static <K, V> Collection<Pair<K, V>> relation(final Collection<K> keys, final Collection<V> values, final Predicate<Pair<K, V>> relationPredicate) {
		Collection<Pair<K, V>> relation = new HashSet<>();
		for (K key : keys) {
			for (V val : values) {
				Pair<K, V> p = new Pair<K, V>(key, val);
				if (relationPredicate.test(p)) {
					relation.add(p);
				}
			}
		}
		return relation;
	}

	public static <K, V> Map<K, Collection<V>> relationAsFunction(final Collection<K> keys, final Collection<V> values, final Predicate<Pair<K, V>> relationPredicate) {
		Map<K, Collection<V>> relation = new HashMap<>();
		for (K key : keys) {
			relation.put(key, new HashSet<>());
			for (V val : values) {
				Pair<K, V> p = new Pair<K, V>(key, val);
				if (relationPredicate.test(p)) {
					relation.get(key).add(val);
				}
			}
		}
		return relation;
	}

	/* FUNCTIONS */
	public static <K, V> Collection<Map<K, V>> allMappings(final Collection<K> domain, final Collection<V> range, final boolean totalsOnly, final boolean injectivesOnly, final boolean surjectivesOnly) throws InterruptedException {

		Collection<Map<K, V>> mappings = new ArrayList<>();

		/* compute possible domains of the functions */
		if (totalsOnly) {

			if (domain.isEmpty()) {
				return mappings;
			}
			List<K> domainAsList = new ArrayList<>(domain);
			int n = domainAsList.size();
			for (List<V> reducedRange : cartesianProduct(range, domain.size())) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Interrupted during calculating all mappings");
				}
				/*
				 * create map that corresponds to this entry of the cartesian product
				 */
				boolean considerMap = true;
				Map<K, V> map = new HashMap<>();
				List<V> coveredRange = new ArrayList<>();
				for (int i = 0; i < n; i++) {
					V val = reducedRange.get(i);

					/* check injectivity (if required) */
					if (injectivesOnly && coveredRange.contains(val)) {
						considerMap = false;
						break;
					}
					coveredRange.add(val);
					map.put(domainAsList.get(i), val);
				}

				/* check surjectivity (if required) */
				if (surjectivesOnly && !coveredRange.containsAll(range)) {
					considerMap = false;
				}

				/* if all criteria are satisfied, add map */
				if (considerMap) {
					mappings.add(map);
				}
			}
		} else {
			for (Collection<K> reducedDomain : powerset(domain)) {
				mappings.addAll(allMappings(reducedDomain, range, true, injectivesOnly, surjectivesOnly));
			}
			if (!surjectivesOnly) {
				mappings.add(new HashMap<>()); // add the empty mapping
			}
		}
		return mappings;
	}

	public static <K, V> Collection<Map<K, V>> allTotalMappings(final Collection<K> domain, final Collection<V> range) throws InterruptedException {
		return allMappings(domain, range, true, false, false);
	}

	public static <K, V> Collection<Map<K, V>> allPartialMappings(final Collection<K> domain, final Collection<V> range) throws InterruptedException {
		return allMappings(domain, range, false, false, false);
	}

	/**
	 * Computes all total mappings that satisfy some given predicate. The predicate is already applied to the partial mappings from which the total mappings are computed in order to prune and speed up
	 * the computation.
	 *
	 * @param domain
	 *            The domain set.
	 * @param range
	 *            The range set.
	 * @param pPredicate
	 *            The predicate that is evaluated for every partial
	 * @return All partial mappings from the domain set to the range set.
	 */
	public static <K, V> Set<Map<K, V>> allTotalAndInjectiveMappingsWithConstraint(final Collection<K> domain, final Collection<V> range, final Predicate<Map<K, V>> pPredicate) throws InterruptedException {
		Set<Map<K, V>> mappings = new HashSet<>();
		if (domain.isEmpty()) {
			return mappings;
		}

		/* now run breadth first search */
		List<K> domainAsList = new ArrayList<>(domain);
		int domainSize = domainAsList.size();
		List<Map<K, V>> open = new ArrayList<>();
		open.add(new HashMap<>());
		while (!open.isEmpty()) {
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedException("Interrupted during calculation of allTotalMappingsWithConstraint.");
			}
			Map<K, V> partialMap = open.get(0);
			open.remove(0);

			/* add partial map if each key has a value assigned (map is total) */
			int index = partialMap.keySet().size();
			if (index >= domainSize) {
				mappings.add(partialMap);
				continue;
			}

			/* add new assignment to partial map */
			K key = domainAsList.get(index);
			for (V val : range) {

				/* due to injectivity, skip this option */
				if (partialMap.containsValue(val)) {
					continue;
				}
				Map<K, V> extendedMap = new HashMap<>(partialMap);
				extendedMap.put(key, val);
				if (pPredicate.test(extendedMap)) {
					open.add(extendedMap);
				}
			}
		}
		return mappings;

	}

	public static <K, V> Set<Map<K, V>> allTotalMappingsWithLocalConstraints(final Collection<K> domain, final Collection<V> range, final Predicate<Pair<K, V>> pPredicate) throws InterruptedException {
		Map<K, Collection<V>> pairsThatSatisfyCondition = relationAsFunction(domain, range, pPredicate);
		return allFuntionsFromFunctionallyDenotedRelation(pairsThatSatisfyCondition);

	} // allPartialMappings

	public static <K, V> Set<Map<K, V>> allFuntionsFromFunctionallyDenotedRelation(final Map<K, Collection<V>> pRelation) throws InterruptedException {
		return allFunctionsFromFunctionallyDenotedRelationRewritingReference(new HashMap<>(pRelation));
	}

	private static <K, V> Set<Map<K, V>> allFunctionsFromFunctionallyDenotedRelationRewritingReference(final Map<K, Collection<V>> pRelation) throws InterruptedException {
		Set<Map<K, V>> out = new HashSet<>();
		if (pRelation.isEmpty()) {
			out.add(new HashMap<>(0, 1.0f));
			return out;
		}

		/* compute all pairs that share one particular entry as key */
		K firstKey = pRelation.keySet().iterator().next();
		Collection<V> vals = pRelation.get(firstKey);
		pRelation.remove(firstKey);

		/* if the domain has size 1 or 0, return the set of mappings for the element in the domain */
		if (pRelation.isEmpty()) {
			for (V val : vals) {
				final Map<K, V> mapWithOneEntry = new HashMap<>(1);
				mapWithOneEntry.put(firstKey, val);
				out.add(mapWithOneEntry);
			}
		}

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted during allFunctionsFromFunctionallyDenotedRelationRewritingReference");
		}
		/* otherwise decompose by recursion */
		else {
			Set<Map<K, V>> recursivelyObtainedFunctions = allFunctionsFromFunctionallyDenotedRelationRewritingReference(pRelation);
			for (Map<K, V> func : recursivelyObtainedFunctions) {
				for (V val : vals) {
					Map<K, V> newFunc = new HashMap<>(func);
					newFunc.put(firstKey, val);
					out.add(newFunc);
				}
			}
		}
		return out;
	}

	/* ORDER OPERATIONS (SHUFFLE, SORT, PERMUTATE) */
	public static <T> void shuffle(final List<T> list) {

		/* preliminaries */
		List<Integer> unusedItems = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			unusedItems.add(i);
		}
		List<T> copy = new ArrayList<T>();
		copy.addAll(list);
		list.clear();

		/* select randomly from unusedItems until unusedItems is empty */
		while (!unusedItems.isEmpty()) {
			int index = (int) Math.floor(Math.random() * unusedItems.size());
			list.add(copy.get(unusedItems.get(index)));
		}
	}

	public static <T> T getRandomElement(final Collection<T> set) {
		int choice = (int) Math.floor(Math.random() * set.size());
		if (set instanceof List) {
			return ((List<T>) set).get(choice);
		}
		int i = 0;
		for (T elem : set) {
			if (i++ == choice) {
				return elem;
			}
		}
		return null;
	}

	public static <T extends Comparable<T>> List<T> mergeSort(final Collection<T> set) {
		if (set.isEmpty()) {
			return new ArrayList<T>();
		}
		if (set.size() == 1) {
			List<T> result = new ArrayList<T>();
			result.addAll(set);
			return result;
		}

		/* create sublists */
		List<T> sublist1 = new ArrayList<T>(), sublist2 = new ArrayList<T>();
		int mid = (int) Math.ceil(set.size() / 2.0);
		int i = 0;
		for (T elem : set) {
			if (i++ < mid) {
				sublist1.add(elem);
			} else {
				sublist2.add(elem);
			}
		}

		/* sort sublists */
		return mergeLists(mergeSort(sublist1), mergeSort(sublist2));
	}

	private static <T extends Comparable<T>> List<T> mergeLists(final List<T> list1, final List<T> list2) {
		List<T> result = new ArrayList<T>();
		while (!list1.isEmpty() && !list2.isEmpty()) {
			if (list1.get(0).compareTo(list2.get(0)) < 0) {
				result.add(list1.get(0));
				list1.remove(0);
			} else {
				result.add(list2.get(0));
				list2.remove(0);
			}
		}
		while (!list1.isEmpty()) {
			result.add(list1.get(0));
			list1.remove(0);
		}
		while (!list2.isEmpty()) {
			result.add(list2.get(0));
			list2.remove(0);
		}
		return result;
	}

	public static <K, V extends Comparable<V>> List<K> keySetSortedByValues(final Map<K, V> map, final boolean asc) {
		if (map.isEmpty()) {
			return new ArrayList<K>();
		}
		if (map.size() == 1) {
			List<K> result = new ArrayList<K>();
			result.addAll(map.keySet());
			return result;
		}

		/* create submaps */
		Map<K, V> submap1 = new Hashtable<K, V>(), submap2 = new Hashtable<K, V>();
		int mid = (int) Math.ceil(map.size() / 2.0);
		int i = 0;
		for (Entry<K, V> entry : map.entrySet()) {
			if (i++ < mid) {
				submap1.put(entry.getKey(), entry.getValue());
			} else {
				submap2.put(entry.getKey(), entry.getValue());
			}
		}

		/* sort sublists */
		return mergeMaps(keySetSortedByValues(submap1, asc), keySetSortedByValues(submap2, asc), map, asc);
	}

	private static <K, V extends Comparable<V>> List<K> mergeMaps(final List<K> keys1, final List<K> keys2, final Map<K, V> map, final boolean asc) {
		List<K> result = new ArrayList<K>();
		while (!keys1.isEmpty() && !keys2.isEmpty()) {
			double comp = map.get(keys1.get(0)).compareTo(map.get(keys2.get(0)));
			if (asc && comp < 0 || !asc && comp >= 0) {
				result.add(keys1.get(0));
				keys1.remove(0);
			} else {
				result.add(keys2.get(0));
				keys2.remove(0);
			}
		}
		while (!keys1.isEmpty()) {
			result.add(keys1.get(0));
			keys1.remove(0);
		}
		while (!keys2.isEmpty()) {
			result.add(keys2.get(0));
			keys2.remove(0);
		}
		return result;
	}

	public static <E> int calculateNumberOfTotalOrderings(final PartialOrderedSet<E> set) throws InterruptedException {
		/*
		 * Since set sizes of zero or one might cause problems, we catch them here.
		 */
		if (set.size() == 1) {
			return 1;
		}
		if (set.size() == 0) {
			return 0;
		}
		/*
		 * Calculate all edges that aren't part of the corresponding graph.
		 */
		List<Set<E>> possibleEdges = SetUtil.getAllPossibleSubsetsWithSize(set, 2);
		final Iterator<Set<E>> edgeIt = possibleEdges.iterator();
		while (edgeIt.hasNext()) {
			final Iterator<E> it = edgeIt.next().iterator();
			final E a = it.next();
			final E b = it.next();
			if (set.isADirectlyBeforeB(a, b)) {
				edgeIt.remove();
			} else if (set.isADirectlyBeforeB(b, a)) {
				edgeIt.remove();
			}
		}
		return getNumberOfAllowedPermutations(set, new LinkedList<>(possibleEdges));
	}

	private static <E> int getNumberOfAllowedPermutations(final PartialOrderedSet<E> set, final Queue<Set<E>> possibleEdges) throws InterruptedException {

		/* if interrupted, return one ordering (which is a lower bound here) */
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}

		/*
		 * If there isn't an edge left, the given partial order actually is a total order.
		 */
		if (possibleEdges.size() == 0) {
			return 1;
		}
		int numberOfAllowedPermutations = 0;
		boolean atLeastOneWithoutException = false;
		/*
		 * We stop the loop if we actually went into the recursion at least once, or the queue is empty.
		 */
		while (!atLeastOneWithoutException && !possibleEdges.isEmpty()) {
			atLeastOneWithoutException = false;
			Set<E> edgeSet = possibleEdges.poll();
			assert edgeSet.size() == 2;
			final Iterator<E> edge = edgeSet.iterator();
			E a = edge.next();
			E b = edge.next();
			final PartialOrderedSet<E> copyOne = new PartialOrderedSet<>(set);
			final PartialOrderedSet<E> copyTwo = new PartialOrderedSet<>(set);
			/*
			 * For edges e1 = (a,b), e2 = (b, a), check whether it is possible to add the edge. If so, continue recursively until a total order (no remaining edges) or a loop in the graph is reached.
			 */
			try {
				copyOne.addABeforeB(a, b);
				atLeastOneWithoutException = true;
				numberOfAllowedPermutations += getNumberOfAllowedPermutations(copyOne, new LinkedList<>(possibleEdges));
			} catch (IllegalStateException isex) {
			}
			try {
				copyTwo.addABeforeB(b, a);
				atLeastOneWithoutException = true;
				numberOfAllowedPermutations += getNumberOfAllowedPermutations(copyTwo, new LinkedList<>(possibleEdges));
			} catch (IllegalStateException isex) {
			}
		}
		return numberOfAllowedPermutations;
	}

	public static String serializeAsSet(final Collection<String> set) {
		return set.toString().replaceAll("\\[", "{").replaceAll("\\]", "}");
	}

	public static Set<String> unserializeSet(final String setDescriptor) {
		Set<String> items = new HashSet<>();
		for (String item : setDescriptor.substring(1, setDescriptor.length() - 1).split(",")) {
			if (!item.trim().isEmpty()) {
				items.add(item.trim());
			}
		}
		return items;
	}

	public static List<String> unserializeList(final String listDescriptor) {
		if (listDescriptor == null) {
			throw new IllegalArgumentException("Invalid list descriptor NULL.");
		}
		if (!listDescriptor.startsWith("[") || !listDescriptor.endsWith("]")) {
			throw new IllegalArgumentException("Invalid list descriptor \"" + listDescriptor + "\". Must start with '[' and end with ']'");
		}
		List<String> items = new ArrayList<>();
		for (String item : listDescriptor.substring(1, listDescriptor.length() - 1).split(",")) {
			if (!item.trim().isEmpty()) {
				items.add(item.trim());
			}
		}
		return items;
	}

	public static Interval unserializeInterval(final String intervalDescriptor) {
		List<String> interval = unserializeList(intervalDescriptor);
		double min = Double.valueOf(interval.get(0));
		return new Interval(min, interval.size() == 1 ? min : Double.valueOf(interval.get(1)));
	}

	public static <T> List<T> getInvertedCopyOfList(final List<T> list) {
		List<T> copy = new ArrayList<>();
		int n = list.size();
		for (int i = 0; i < n; i++) {
			copy.add(list.get(n - i - 1));
		}
		return copy;
	}

	public static <T> List<T> addAndGet(final List<T> list, final T item) {
		list.add(item);
		return list;
	}

	public static <T, U> Map<U, Collection<T>> groupCollectionByAttribute(final Collection<T> collection, final IGetter<T, U> getter) {
		Map<U, Collection<T>> groupedCollection = new HashMap<>();
		collection.forEach(i -> {
			U val = getter.getPropertyOf(i);
			if (!groupedCollection.containsKey(val)) {
				groupedCollection.put(val, new ArrayList<>());
			}
			groupedCollection.get(val).add(i);
		});
		return groupedCollection;
	}
}