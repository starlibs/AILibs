package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * A pool provider which is created out of a {@link DyadRankingDataset}. Each
 * {@link SparseDyadRankingInstance} or {@link DyadRankingInstance} of the
 * {@link DyadRankingDataset} must represent a full ranking. Only queries of
 * rankings over the same instance features can be answered.
 *
 * @author Jonas Hanselle
 *
 */
public class DyadDatasetPoolProvider implements IDyadRankingPoolProvider {

	private HashMap<Vector, Set<Dyad>> dyadsByInstances;
	private HashMap<Vector, Set<Dyad>> dyadsByAlternatives;
	private HashMap<Vector, IDyadRankingInstance> dyadRankingsByInstances;
	private HashMap<Vector, IDyadRankingInstance> dyadRankingsByAlternatives;
	private List<IDyadRankingInstance> pool;
	private boolean removeDyadsWhenQueried;
	private HashSet<IDyadRankingInstance> queriedRankings;
	private int numberQueries;

	public DyadDatasetPoolProvider(final DyadRankingDataset dataset) {
		this.numberQueries = 0;
		this.removeDyadsWhenQueried = false;
		this.dyadsByInstances = new HashMap<>();
		this.dyadsByAlternatives = new HashMap<>();
		this.dyadRankingsByInstances = new HashMap<>();
		this.dyadRankingsByAlternatives = new HashMap<>();
		this.pool = new ArrayList<>(dataset.size());
		for (IDyadRankingInstance instance : dataset) {
			this.addDyadRankingInstance(instance);
		}
		this.queriedRankings = new HashSet<>();
	}

	@Override
	public Collection<IDyadRankingInstance> getPool() {
		return this.pool;
	}

	@Override
	public IDyadRankingInstance query(final IDyadRankingInstance queryInstance) {
		this.numberQueries++;
		if (!(queryInstance instanceof SparseDyadRankingInstance)) {
			throw new IllegalArgumentException("Currently only supports SparseDyadRankingInstances!");
		}
		SparseDyadRankingInstance drInstance = (SparseDyadRankingInstance) queryInstance;
		List<Pair<Dyad, Integer>> dyadPositionPairs = new ArrayList<>(drInstance.length());
		for (Dyad dyad : drInstance) {
			int position = this.getPositionInRankingByInstanceFeatures(dyad);
			dyadPositionPairs.add(new Pair<Dyad, Integer>(dyad, position));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadPositionPairs, Comparator.comparing(Pair<Dyad, Integer>::getRight));
		List<Dyad> dyadList = new ArrayList<>(dyadPositionPairs.size());
		for (Pair<Dyad, Integer> pair : dyadPositionPairs) {
			dyadList.add(pair.getFirst());
		}
		DyadRankingInstance trueRanking = new DyadRankingInstance(dyadList);
		if (this.removeDyadsWhenQueried) {
			for (Dyad dyad : dyadList) {
				this.removeDyadFromPool(dyad);
			}
		}
		this.queriedRankings.add(trueRanking);
		return trueRanking;
	}

	@Override
	public Set<Dyad> getDyadsByInstance(final Vector instanceFeatures) {
		if (!this.dyadsByInstances.containsKey(instanceFeatures)) {
			return new HashSet<>();
		}
		return this.dyadsByInstances.get(instanceFeatures);
	}

	@Override
	public Set<Dyad> getDyadsByAlternative(final Vector alternativeFeatures) {
		if (!this.dyadsByAlternatives.containsKey(alternativeFeatures)) {
			return new HashSet<>();
		}
		return this.dyadsByAlternatives.get(alternativeFeatures);
	}

	/**
	 * Adds a {@link IDyadRankingInstance} instance to the pool.
	 *
	 * @param instance
	 */
	private void addDyadRankingInstance(final IDyadRankingInstance instance) {
		// Add the dyad ranking instance to the pool
		this.pool.add(instance);

		// Add the dyad ranking instances to the hash maps
		this.dyadRankingsByInstances.put(instance.getDyadAtPosition(0).getInstance(), instance);
		this.dyadRankingsByAlternatives.put(instance.getDyadAtPosition(0).getAlternative(), instance);

		for (Dyad dyad : instance) {
			// Add all dyads to the HashMap with instance features as key
			if (!this.dyadsByInstances.containsKey(dyad.getInstance())) {
				this.dyadsByInstances.put(dyad.getInstance(), new HashSet<Dyad>());
			}
			this.dyadsByInstances.get(dyad.getInstance()).add(dyad);

			// Add all dyads to the HashMap with alternative features as key
			if (!this.dyadsByAlternatives.containsKey(dyad.getAlternative())) {
				this.dyadsByAlternatives.put(dyad.getAlternative(), new HashSet<Dyad>());
			}
			this.dyadsByAlternatives.get(dyad.getAlternative()).add(dyad);
		}
	}

	/**
	 * Returns the position of a dyad in the ranking over the same instance
	 * features. Returns -1 if the ranking does not contain the dyad.
	 *
	 * @param dyad
	 * @return Position of the dyad in the ranking, -1 if the ranking does not
	 *         contain the dyad.
	 */
	private int getPositionInRankingByInstanceFeatures(final Dyad dyad) {
		if (!this.dyadRankingsByInstances.containsKey(dyad.getInstance())) {
			return -1;
		} else {
			IDyadRankingInstance ranking = this.dyadRankingsByInstances.get(dyad.getInstance());
			boolean found = false;
			int curPos = 0;
			while (curPos < ranking.length() && !found) {
				Dyad dyadInRanking = ranking.getDyadAtPosition(curPos);
				if (dyadInRanking.equals(dyad)) {
					found = true;
				} else {
					curPos++;
				}
			}
			return curPos;
		}
	}

	@Override
	public Collection<Vector> getInstanceFeatures() {
		return this.dyadsByInstances.keySet();
	}

	private void removeDyadFromPool(final Dyad dyad) {
		if (this.dyadsByInstances.containsKey(dyad.getInstance())) {
			this.dyadsByInstances.get(dyad.getInstance()).remove(dyad);
			if (this.dyadsByInstances.get(dyad.getInstance()).size() < 2) {
				this.dyadsByInstances.remove(dyad.getInstance());
			}
		}
		if (this.dyadsByAlternatives.containsKey(dyad.getAlternative())) {
			this.dyadsByAlternatives.get(dyad.getAlternative()).remove(dyad);
			if (this.dyadsByAlternatives.get(dyad.getAlternative()).size() < 2) {
				this.dyadsByAlternatives.remove(dyad.getAlternative());
			}
		}
	}

	@Override
	public void setRemoveDyadsWhenQueried(final boolean flag) {
		this.removeDyadsWhenQueried = flag;
	}

	@Override
	public int getPoolSize() {
		int size = 0;
		for (Set<Dyad> set : this.dyadsByInstances.values()) {
			size += set.size();
		}
		return size;
	}

	/**
	 * Returns the number of queries the pool provider has answered so far.
	 *
	 * @return Number of queries this pool provider has answered.
	 */
	public int getNumberQueries() {
		return this.numberQueries;
	}

	@Override
	public DyadRankingDataset getQueriedRankings() {
		return new DyadRankingDataset(new ArrayList<IDyadRankingInstance>(this.queriedRankings));
	}

}
