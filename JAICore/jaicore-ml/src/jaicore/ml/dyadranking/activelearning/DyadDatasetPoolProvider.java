package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * A pool provider which is created out of a {@link DyadRankingDataset}.
 * @author Jonas Hanselle
 *
 */
public class DyadDatasetPoolProvider implements IDyadRankingPoolProvider {

	private HashMap<Vector, Set<Dyad>> dyadsByInstances;
	private HashMap<Vector, Set<Dyad>> dyadsByAlternatives;
	private HashMap<Vector, IDyadRankingInstance> dyadRankingsByInstances;
	private HashMap<Vector, IDyadRankingInstance> dyadRankingsByAlternatives;
	private List<IInstance> pool;

	public DyadDatasetPoolProvider(DyadRankingDataset dataset) {
		dyadsByInstances = new HashMap<Vector, Set<Dyad>>();
		dyadsByAlternatives = new HashMap<Vector, Set<Dyad>>();
		
		pool = new ArrayList<IInstance>(dataset.size());
	}

	@Override
	public Collection<IInstance> getPool() {
		return pool;
	}

	@Override
	public IInstance query(IInstance queryInstance) {
		if(!(queryInstance instanceof SparseDyadRankingInstance)) {
			throw new IllegalArgumentException("Currently only supports SparseDyadRankingInstances!");
		}
		
		return null;
	}

	@Override
	public Set<Dyad> getDyadsByInstance(Vector instanceFeatures) {
		if(!dyadsByInstances.containsKey(instanceFeatures))
			return new HashSet<Dyad>();
		return dyadsByInstances.get(instanceFeatures);
	}

	@Override
	public Set<Dyad> getDyadsByAlternative(Vector alternativeFeatures) {
		if(!dyadsByAlternatives.containsKey(alternativeFeatures))
			return new HashSet<Dyad>();
		return dyadsByAlternatives.get(alternativeFeatures);
	}

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
}
