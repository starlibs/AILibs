package ai.libs.jaicore.ml.ranking.dyad.learner.activelearning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingDataset;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.nd4j.linalg.primitives.Pair;

import ai.libs.jaicore.ml.ranking.dyad.dataset.DenseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;

/**
 * A pool provider which is created out of a {@link DyadRankingDataset}. Each
 * {@link SparseDyadRankingInstance} or {@link DenseDyadRankingInstance} of the
 * {@link DyadRankingDataset} must represent a full ranking. Only queries of
 * rankings over the same instance features can be answered.
 *
 * @author Jonas Hanselle
 *
 */
public class DyadDatasetPoolProvider implements IDyadRankingPoolProvider {

	private HashMap<IVector, Set<IDyad>> dyadsByInstances;
	private HashMap<IVector, Set<IDyad>> dyadsByAlternatives;
	private HashMap<IVector, IDyadRankingInstance> dyadRankingsByInstances;
	private HashMap<IVector, IDyadRankingInstance> dyadRankingsByAlternatives;
	private List<IDyadRankingInstance> pool;
	private boolean removeDyadsWhenQueried;
	private HashSet<IDyadRankingInstance> queriedRankings;
	private int numberQueries;

	public DyadDatasetPoolProvider(final IDyadRankingDataset dataset) {
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
		List<Pair<IDyad, Integer>> dyadPositionPairs = new ArrayList<>(drInstance.getNumberOfRankedElements());
		for (IDyad dyad : drInstance) {
			int position = this.getPositionInRankingByInstanceFeatures(dyad);
			dyadPositionPairs.add(new Pair<>(dyad, position));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadPositionPairs, Comparator.comparing(Pair<IDyad, Integer>::getRight));
		List<IDyad> dyadList = new ArrayList<>(dyadPositionPairs.size());
		for (Pair<IDyad, Integer> pair : dyadPositionPairs) {
			dyadList.add(pair.getFirst());
		}
		IDyadRankingInstance trueRanking = new DenseDyadRankingInstance(dyadList);
		if (this.removeDyadsWhenQueried) {
			for (IDyad dyad : dyadList) {
				this.removeDyadFromPool(dyad);
			}
		}
		this.queriedRankings.add(trueRanking);
		return trueRanking;
	}

	@Override
	public Set<IDyad> getDyadsByInstance(final IVector instanceFeatures) {
		if (!this.dyadsByInstances.containsKey(instanceFeatures)) {
			return new HashSet<>();
		}
		return this.dyadsByInstances.get(instanceFeatures);
	}

	@Override
	public Set<IDyad> getDyadsByAlternative(final IVector alternativeFeatures) {
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
		this.dyadRankingsByInstances.put(instance.getLabel().get(0).getContext(), instance);
		this.dyadRankingsByAlternatives.put(instance.getLabel().get(0).getAlternative(), instance);

		for (IDyad dyad : instance) {
			// Add all dyads to the HashMap with instance features as key
			if (!this.dyadsByInstances.containsKey(dyad.getContext())) {
				this.dyadsByInstances.put(dyad.getContext(), new HashSet<IDyad>());
			}
			this.dyadsByInstances.get(dyad.getContext()).add(dyad);

			// Add all dyads to the HashMap with alternative features as key
			if (!this.dyadsByAlternatives.containsKey(dyad.getAlternative())) {
				this.dyadsByAlternatives.put(dyad.getAlternative(), new HashSet<IDyad>());
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
	private int getPositionInRankingByInstanceFeatures(final IDyad dyad) {
		if (!this.dyadRankingsByInstances.containsKey(dyad.getContext())) {
			return -1;
		}
		IDyadRankingInstance ranking = this.dyadRankingsByInstances.get(dyad.getContext());
		boolean found = false;
		int curPos = 0;
		while (curPos < ranking.getNumberOfRankedElements() && !found) {
			IDyad dyadInRanking = ranking.getLabel().get(curPos);
			if (dyadInRanking.equals(dyad)) {
				found = true;
			} else {
				curPos++;
			}
		}
		return curPos;
	}

	@Override
	public Collection<IVector> getInstanceFeatures() {
		return this.dyadsByInstances.keySet();
	}

	private void removeDyadFromPool(final IDyad dyad) {
		if (this.dyadsByInstances.containsKey(dyad.getContext())) {
			this.dyadsByInstances.get(dyad.getContext()).remove(dyad);
			if (this.dyadsByInstances.get(dyad.getContext()).size() < 2) {
				this.dyadsByInstances.remove(dyad.getContext());
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
		for (Set<IDyad> set : this.dyadsByInstances.values()) {
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
		return new DyadRankingDataset(new ArrayList<>(this.queriedRankings));
	}

}
