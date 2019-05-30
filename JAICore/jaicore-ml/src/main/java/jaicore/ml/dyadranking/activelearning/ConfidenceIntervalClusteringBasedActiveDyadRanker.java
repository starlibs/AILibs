package jaicore.ml.dyadranking.activelearning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * A prototypical active dyad ranker based on clustering of pseudo confidence
 * intervals. During the learning procedure, it keeps track over the standard
 * deviation of the skill values predicted for a dyad. First a constant number
 * of random queries is sampled at the beginning. Then the sampling strategy
 * clusteres the skill values of all alternatives for each instance according to
 * the lower and upper bounds of the confidence intervals of the skill for all
 * corresponding dyads. Confidence intervals are given by [skill - std, skill +
 * std] where skill denotes the skill and std denotes the empirical standard
 * deviaition of the skill for a dyad. Afterwards, it picks one of the largest
 * clusters and then selects the two dyads for which the confidence intervals
 * overlap the most within the cluster for pairwise comparison, until a
 * minibatch of constant size is filled.
 *
 * @author Jonas Hanselle
 *
 */
public class ConfidenceIntervalClusteringBasedActiveDyadRanker extends ARandomlyInitializingDyadRanker {

	private static final Logger log = LoggerFactory.getLogger(ConfidenceIntervalClusteringBasedActiveDyadRanker.class);

	private Clusterer clusterer;

	public ConfidenceIntervalClusteringBasedActiveDyadRanker(final PLNetDyadRanker ranker, final IDyadRankingPoolProvider poolProvider, final int seed, final int numberRandomQueriesAtStart, final int minibatchSize, final Clusterer clusterer) {
		super(ranker, poolProvider, seed, numberRandomQueriesAtStart, minibatchSize);
		this.clusterer = clusterer;
	}

	@Override
	public void activelyTrainWithOneInstance() {

		PriorityQueue<List<Dyad>> clusterQueue = new PriorityQueue<>(new ListComparator());

		Set<IDyadRankingInstance> minibatch = new HashSet<>();
		Map<Dyad, SummaryStatistics> dyadStats = this.getDyadStats();

		for (Vector inst : this.getInstanceFeatures()) {
			// Create instances for clustering
			Attribute upperAttr = new Attribute("upper_bound");
			Attribute lowerAttr = new Attribute("lower_bound");
			ArrayList<Attribute> attributes = new ArrayList<>();
			attributes.add(upperAttr);
			attributes.add(lowerAttr);
			Instances intervalInstances = new Instances("confidence_intervalls", attributes, this.poolProvider.getDyadsByInstance(inst).size());
			for (Dyad dyad : this.poolProvider.getDyadsByInstance(inst)) {
				double skill = this.ranker.getSkillForDyad(dyad);
				dyadStats.get(dyad).addValue(skill);
				double[] attValues = new double[2];
				attValues[0] = skill + dyadStats.get(dyad).getStandardDeviation();
				attValues[1] = skill - dyadStats.get(dyad).getStandardDeviation();
				Instance intervalInstance = new DenseInstance(1.0d, attValues);
				intervalInstances.add(intervalInstance);
			}

			try {
				this.clusterer.buildClusterer(intervalInstances);

				List<List<Dyad>> instanceClusters = new ArrayList<>();
				int numClusters = this.clusterer.numberOfClusters();
				for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++) {
					instanceClusters.add(new ArrayList<Dyad>());
				}

				for (Dyad dyad : this.poolProvider.getDyadsByInstance(inst)) {
					double skill = this.ranker.getSkillForDyad(dyad);
					double[] attValues = new double[2];
					attValues[0] = skill + dyadStats.get(dyad).getStandardDeviation();
					attValues[1] = skill - dyadStats.get(dyad).getStandardDeviation();
					Instance intervalInstance = new DenseInstance(1.0d, attValues);
					int cluster = this.clusterer.clusterInstance(intervalInstance);
					instanceClusters.get(cluster).add(dyad);
				}

				for (int j = 0; j < instanceClusters.size(); j++) {
					clusterQueue.add(instanceClusters.get(j));
				}
			} catch (Exception e1) {
				log.error(e1.getMessage());
			}
		}

		Random random = this.getRandom();
		for (int minibatchIndex = 0; minibatchIndex < this.getMinibatchSize(); minibatchIndex++) {
			// get the largest cluster
			List<Dyad> curDyads = clusterQueue.poll();
			if (curDyads.size() < 2) {
				continue;
			}
			// check overlap for all pairs of dyads in the current cluster
			double curMax = -1;
			int[] curPair = { 0, 1 };
			boolean changed = false;
			for (int j = 1; j < curDyads.size(); j++) {
				for (int k = 0; k < j; k++) {
					Dyad dyad1 = curDyads.get(j);
					Dyad dyad2 = curDyads.get(k);
					double overlap = this.getConfidenceIntervalOverlapForDyads(dyad1, dyad2);
					if (overlap > curMax) {
						curPair[0] = j;
						curPair[1] = k;
						curMax = overlap;
						changed = true;
					}

				}
			}
			// if the pair hasn't changed, i.e. there are no overlapping intervals, sample a random pair
			if (!changed) {
				curPair[0] = random.nextInt(curDyads.size());
				curPair[1] = random.nextInt(curDyads.size());
				while (curPair[0] == curPair[1]) {
					curPair[1] = random.nextInt(curDyads.size());
				}
			}

			// query them
			LinkedList<Vector> alternatives = new LinkedList<>();
			alternatives.add(curDyads.get(curPair[0]).getAlternative());
			alternatives.add(curDyads.get(curPair[1]).getAlternative());
			SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(curDyads.get(curPair[0]).getInstance(), alternatives);
			IDyadRankingInstance trueRanking = this.poolProvider.query(queryInstance);
			minibatch.add(trueRanking);
		}

		// update the ranker
		try {
			this.updateRanker(minibatch);
		} catch (TrainingException e) {
			log.error(e.getMessage());
		}
	}

	private double getConfidenceIntervalOverlapForDyads(final Dyad dyad1, final Dyad dyad2) {
		double skill1 = this.ranker.getSkillForDyad(dyad1);
		double skill2 = this.ranker.getSkillForDyad(dyad2);
		Map<Dyad, SummaryStatistics> dyadStats = this.getDyadStats();
		double lower1 = skill1 - dyadStats.get(dyad1).getStandardDeviation();
		double upper1 = skill1 + dyadStats.get(dyad1).getStandardDeviation();
		double lower2 = skill2 - dyadStats.get(dyad2).getStandardDeviation();
		double upper2 = skill2 + dyadStats.get(dyad2).getStandardDeviation();
		// if the intervals dont intersect, return 0
		if (lower1 > upper2 || upper1 < lower2) {
			return 0.0d;
		}
		// else compute intersection
		else {
			double upperlower = Math.max(lower1, lower2);
			double lowerupper = Math.min(upper1, upper2);
			return Math.abs((lowerupper - upperlower));
		}
	}

	private class ListComparator implements Comparator<List<Dyad>> {

		@Override
		public int compare(final List<Dyad> o1, final List<Dyad> o2) {
			if (o1.size() > o2.size()) {
				return -1;
			}
			if (o1.size() < o2.size()) {
				return 1;
			}
			return 0;
		}

	}

}
