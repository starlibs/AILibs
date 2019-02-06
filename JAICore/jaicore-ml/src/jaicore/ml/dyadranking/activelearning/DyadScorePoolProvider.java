package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.avro.file.SyncableFileOutputStream;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

/**
 * A pool provider which is created out of a list of {@link Dyad}s and ground
 * truth scores.
 * 
 * @author Jonas Hanselle
 *
 */
public class DyadScorePoolProvider implements IDyadRankingPoolProvider {

	private HashMap<Vector, Set<Dyad>> dyadsByInstances;
	private HashMap<Vector, Set<Dyad>> dyadsByAlternatives;
	private HashMap<Dyad, SummaryStatistics> dyadScores;
	private List<IInstance> pool;

	public DyadScorePoolProvider(List<Pair<Dyad, Double>> dyadScorePairs) {
		dyadsByInstances = new HashMap<Vector, Set<Dyad>>();
		dyadsByAlternatives = new HashMap<Vector, Set<Dyad>>();
		dyadScores = new HashMap<Dyad, SummaryStatistics>();
		pool = new ArrayList<IInstance>(dyadScorePairs.size());
		for (Pair<Dyad, Double> pair : dyadScorePairs) {
			addDyad(pair.getFirst(), pair.getSecond());
		}
	}

	@Override
	public Collection<IInstance> getPool() {
		return pool;
	}

	@Override
	public IInstance query(IInstance queryInstance) {

		IDyadRankingInstance drInstance = (IDyadRankingInstance) queryInstance;
		List<Pair<Dyad, Double>> dyadUtilityPairs = new ArrayList<Pair<Dyad, Double>>(drInstance.length());
		for (Dyad dyad : drInstance) {
			if (!dyadScores.containsKey(dyad)) {
				throw new IllegalStateException("Dyad not contained yet!");
			}
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, dyadScores.get(dyad).getMean()));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		List<Dyad> ranking = new ArrayList<Dyad>();

		for (Pair<Dyad, Double> pair : dyadUtilityPairs) {
			ranking.add(pair.getLeft());
//			removeDyadFromPool(pair.getLeft());
		}
		return new DyadRankingInstance(ranking);

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

	private void addDyad(Dyad dyad, Double score) {
		// Add the dyad ranking instance to the pool

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

		if (!dyadScores.containsKey(dyad))
			dyadScores.put(dyad, new SummaryStatistics());
		dyadScores.get(dyad).addValue(score);;

	}

	@Override
	public Collection<Vector> getInstanceFeatures() {
		return dyadsByInstances.keySet();
	}

	private void removeDyadFromPool(Dyad dyad) {
//			System.out.println("set before: " + dyadsByInstances.get(dyad.getInstance()).toString());
		if (dyadsByInstances.containsKey(dyad.getInstance()))
			dyadsByInstances.get(dyad.getInstance()).remove(dyad);
		if (dyadsByAlternatives.containsKey(dyad.getAlternative()))
			dyadsByAlternatives.get(dyad.getAlternative()).remove(dyad);
//			System.out.println("set after: " + dyadsByInstances.get(dyad.getInstance()).toString());
	}

	public IDyadRankingInstance getDyadRankingInstanceForInstanceFeatures(Vector instanceFeatures) {
		if (!dyadsByInstances.containsKey(instanceFeatures))
			return new SparseDyadRankingInstance(instanceFeatures, new ArrayList<Vector>());
		Set<Dyad> dyads = getDyadsByInstance(instanceFeatures);

		List<Pair<Dyad, Double>> dyadUtilityPairs = new LinkedList<Pair<Dyad, Double>>();
		for (Dyad dyad : dyads) {
			if (!dyadScores.containsKey(dyad)) {
				throw new IllegalStateException("Dyad not contained yet!");
			}
			dyadUtilityPairs.add(new Pair<Dyad, Double>(dyad, dyadScores.get(dyad).getMean()));
		}
		// sort the instance in descending order of utility values
		Collections.sort(dyadUtilityPairs, Comparator.comparing(p -> -p.getRight()));
		List<Dyad> ranking = new ArrayList<Dyad>();

		for (Pair<Dyad, Double> pair : dyadUtilityPairs) {
			ranking.add(pair.getLeft());
		}
		return new DyadRankingInstance(ranking);
	}
	
	public void removeDyadsFromPoolByInstances(Vector instanceFeatures) {
		dyadsByInstances.remove(instanceFeatures);
	}
	
	public void printCounts() {
		for(Vector instanceFeatures : dyadsByInstances.keySet()) {
			System.out.println(instanceFeatures.toString() + "\t" + dyadsByInstances.get(instanceFeatures).size());
		}
	}
}