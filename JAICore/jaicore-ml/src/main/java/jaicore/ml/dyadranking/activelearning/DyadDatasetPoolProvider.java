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
import jaicore.ml.core.dataset.IInstance;
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
	private List<IInstance> pool;
	private boolean removeDyadsWhenQueried;
	private HashSet<IDyadRankingInstance> queriedRankings;
	private int numberQueries;

	public DyadDatasetPoolProvider(DyadRankingDataset dataset) {
		numberQueries = 0;
		removeDyadsWhenQueried = false;
		dyadsByInstances = new HashMap<Vector, Set<Dyad>>();
		dyadsByAlternatives = new HashMap<Vector, Set<Dyad>>();
		dyadRankingsByInstances = new HashMap<Vector, IDyadRankingInstance>();
		dyadRankingsByAlternatives = new HashMap<Vector, IDyadRankingInstance>();
		pool = new ArrayList<IInstance>(dataset.size());
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			this.addDyadRankingInstance(drInstance);
		}
		this.queriedRankings = new HashSet<IDyadRankingInstance>();
	}

	@Override
	public Collection<IInstance> getPool() {
		return pool;
	}

	@Override
	public IInstance query(IInstance queryInstance) {
		numberQueries++;
		if (!(queryInstance instanceof SparseDyadRankingInstance)) {
			throw new IllegalArgumentException("Currently only supports SparseDyadRankingInstances!");
		}
		SparseDyadRankingInstance drInstance = (SparseDyadRankingInstance) queryInstance;
		List<Pair<Dyad, Integer>> dyadPositionPairs = new ArrayList<Pair<Dyad, Integer>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			int position = this.getPositionInRankingByInstanceFeatures(dyad);
			dyadPositionPairs.add(new Pair<Dyad, Integer>(dyad, position));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadPositionPairs, Comparator.comparing(p -> p.getRight()));
		List<Dyad> dyadList = new ArrayList<Dyad>(dyadPositionPairs.size());
		for (Pair<Dyad, Integer> pair : dyadPositionPairs) {
			dyadList.add(pair.getFirst());
		}
		DyadRankingInstance trueRanking = new DyadRankingInstance(dyadList);
		if (this.removeDyadsWhenQueried) {
			for (Dyad dyad : dyadList)
				this.removeDyadFromPool(dyad);
		}
		queriedRankings.add(trueRanking);
		return trueRanking;
	}

	@Override
	public Set<Dyad> getDyadsByInstance(Vector instanceFeatures) {
		if (!dyadsByInstances.containsKey(instanceFeatures))
			return new HashSet<Dyad>();
		return dyadsByInstances.get(instanceFeatures);
	}

	@Override
	public Set<Dyad> getDyadsByAlternative(Vector alternativeFeatures) {
		if (!dyadsByAlternatives.containsKey(alternativeFeatures))
			return new HashSet<Dyad>();
		return dyadsByAlternatives.get(alternativeFeatures);
	}

	/**
	 * Adds a {@link IDyadRankingInstance} instance to the pool.
	 * 
	 * @param instance
	 */
	private void addDyadRankingInstance(IDyadRankingInstance instance) {
		// Add the dyad ranking instance to the pool
		pool.add(instance);

		// Add the dyad ranking instances to the hash maps
		dyadRankingsByInstances.put(instance.getDyadAtPosition(0).getInstance(), instance);
		dyadRankingsByAlternatives.put(instance.getDyadAtPosition(0).getAlternative(), instance);

		for (Dyad dyad : instance) {
			// Add all dyads to the HashMap with instance features as key
			if (!dyadsByInstances.containsKey(dyad.getInstance())) {
				dyadsByInstances.put(dyad.getInstance(), new HashSet<Dyad>());
			}
			dyadsByInstances.get(dyad.getInstance()).add(dyad);

			// Add all dyads to the HashMap with alternative features as key
			if (!dyadsByAlternatives.containsKey(dyad.getAlternative())) {
				dyadsByAlternatives.put(dyad.getAlternative(), new HashSet<Dyad>());
			}
			dyadsByAlternatives.get(dyad.getAlternative()).add(dyad);
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
	private int getPositionInRankingByInstanceFeatures(Dyad dyad) {
		if (!dyadRankingsByInstances.containsKey(dyad.getInstance())) {
			return -1;
		} else {
			IDyadRankingInstance ranking = dyadRankingsByInstances.get(dyad.getInstance());
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
		return dyadsByInstances.keySet();
	}

	private void removeDyadFromPool(Dyad dyad) {
		if (dyadsByInstances.containsKey(dyad.getInstance())) {
			dyadsByInstances.get(dyad.getInstance()).remove(dyad);
			if (dyadsByInstances.get(dyad.getInstance()).size() < 2)
				dyadsByInstances.remove(dyad.getInstance());
		}
		if (dyadsByAlternatives.containsKey(dyad.getAlternative())) {
			dyadsByAlternatives.get(dyad.getAlternative()).remove(dyad);
			if (dyadsByAlternatives.get(dyad.getAlternative()).size() < 2)
				dyadsByAlternatives.remove(dyad.getAlternative());
		}
	}

	@Override
	public void setRemoveDyadsWhenQueried(boolean flag) {
		this.removeDyadsWhenQueried = flag;
	}

	@Override
	public int getPoolSize() {
		int size = 0;
		for (Set<Dyad> set : dyadsByInstances.values())
			size += set.size();
		return size;
	}

	/**
	 * Returns the number of queries the pool provider has answered so far.
	 * 
	 * @return Number of queries this pool provider has answered.
	 */
	public int getNumberQueries() {
		return numberQueries;
	}

	@Override
	public DyadRankingDataset getQueriedRankings() {
		return new DyadRankingDataset(new ArrayList<IDyadRankingInstance>(queriedRankings));
	}

}
