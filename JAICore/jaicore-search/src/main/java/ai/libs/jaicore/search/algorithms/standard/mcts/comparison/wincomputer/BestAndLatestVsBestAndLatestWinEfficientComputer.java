package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class BestAndLatestVsBestAndLatestWinEfficientComputer implements IWinComputer {

	private static class PreferenceSet {
		private Map<Integer, Double> scoresForObservations = new HashMap<>();
		private Map<Integer, Integer> childIndicesOfObservations = new HashMap<>();
		private Map<Integer, List<Integer>> listOfObservationsPerChild = new HashMap<>();
		private Map<Integer, Map<Integer, Integer>> winsPerChild = new HashMap<>();

		public void addObservation(final int observationId, final double score, final int childIndex) {
			this.scoresForObservations.put(observationId, score);
			this.childIndicesOfObservations.put(observationId, childIndex);
			this.listOfObservationsPerChild.computeIfAbsent(childIndex, c -> new ArrayList<>()).add(observationId);

			/* update wins */
			for (Entry<Integer, Integer> observationWithItsChild : this.childIndicesOfObservations.entrySet()) {
				int childIndexOfOther = observationWithItsChild.getValue();
				if (childIndexOfOther != childIndex) {
					int indexOfOtherObservation = observationWithItsChild.getKey();
					double scoreOfOther = this.scoresForObservations.get(indexOfOtherObservation);
					if (scoreOfOther <= score) {
						int winsUpToNow = this.winsPerChild.computeIfAbsent(childIndexOfOther, n -> new HashMap<>()).computeIfAbsent(childIndex, m -> 0);
						this.winsPerChild.get(childIndexOfOther).put(childIndex, winsUpToNow + 1);
					}
					if (scoreOfOther >= score) {
						int winsUpToNow = this.winsPerChild.computeIfAbsent(childIndex, n -> new HashMap<>()).computeIfAbsent(childIndexOfOther, m -> 0);
						this.winsPerChild.get(childIndex).put(childIndexOfOther, winsUpToNow + 1);
					}
				}
			}
		}

		public void removeObservation(final int observationId) {

			/* update wins */
			double score = this.scoresForObservations.get(observationId);
			int childIndex = this.childIndicesOfObservations.get(observationId);
			for (Entry<Integer, Integer> observationWithItsChild : this.childIndicesOfObservations.entrySet()) {
				int childIndexOfOther = observationWithItsChild.getValue();
				if (childIndexOfOther != childIndex) {
					int indexOfOtherObservation = observationWithItsChild.getKey();
					double scoreOfOther = this.scoresForObservations.get(indexOfOtherObservation);
					if (scoreOfOther <= score) {
						int winsUpToNow = this.winsPerChild.computeIfAbsent(childIndexOfOther, n -> new HashMap<>()).computeIfAbsent(childIndex, m -> 0);
						this.winsPerChild.get(childIndexOfOther).put(childIndex, winsUpToNow - 1);
					}
					if (scoreOfOther >= score) {
						int winsUpToNow = this.winsPerChild.computeIfAbsent(childIndex, n -> new HashMap<>()).computeIfAbsent(childIndexOfOther, m -> 0);
						this.winsPerChild.get(childIndex).put(childIndexOfOther, winsUpToNow - 1);
					}
				}
			}

			this.scoresForObservations.remove(observationId);
			this.childIndicesOfObservations.remove(observationId);
			this.listOfObservationsPerChild.get(childIndex).remove((Object)observationId);
		}

		public int getNumberOfObservations() {
			return this.scoresForObservations.size();
		}

		public List<Integer> getBestObservationsUnderChild(final int childIndex, final int k) {
			return this.listOfObservationsPerChild.get(childIndex).stream().sorted((i1, i2)-> Double.compare(this.scoresForObservations.get(i1), this.scoresForObservations.get(i2))).limit(k).collect(Collectors.toList());
		}

		public int getWinsOfChildOverAnother(final int childIndexA, final int childIndexB) {
			return this.winsPerChild.containsKey(childIndexA) ?  this.winsPerChild.get(childIndexA).get(childIndexB) : 0;
		}
	}

	private Logger logger = LoggerFactory.getLogger(BestAndLatestVsBestAndLatestWinEfficientComputer.class);
	private final int kBest = 5;
	private final int kLatest = this.kBest * 10;
	private Map<BTModel, PriorityQueue<Pair<Integer, Double>>> bestObservations = new HashMap<>();
	private Map<BTModel, Queue<Pair<Integer, Double>>> latestObservations = new HashMap<>();
	private Map<BTModel, PreferenceSet> preferenceSets = new HashMap<>(); // contains for each model and each run id the win pairs in which the id forms the minimum
	private Map<BTModel, Integer> numObservations = new HashMap<>();

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean scoreIsOfRightChild) {
		long start = System.currentTimeMillis();
		this.logger.debug("Recomputing wins for children of {}.",  nodeModel);
		int numberOfObservationForThisNode = this.numObservations.computeIfAbsent(nodeModel, n -> 0) + 1;
		this.numObservations.put(nodeModel, numberOfObservationForThisNode);
		int childIndex = scoreIsOfRightChild ? 1 : 0;
		PreferenceSet ps = this.preferenceSets.computeIfAbsent(nodeModel, nm -> new PreferenceSet());

		/* add the new value */
		ps.addObservation(numberOfObservationForThisNode, newScore, childIndex);

		/* determine oldest observation that is not among the optimal ones */
		List<Integer> bestObservationsUnderUpdated = ps.getBestObservationsUnderChild(childIndex, this.kBest);
		List<Integer> observationsUnderUpdatedChild = ps.listOfObservationsPerChild.get(childIndex);
		Optional<Integer> idOfOldestObservationThatIsNotOptimal = observationsUnderUpdatedChild.stream().filter(o -> !bestObservationsUnderUpdated.contains(o)).findFirst();
		if (idOfOldestObservationThatIsNotOptimal.isPresent() && ps.getNumberOfObservations() > this.kBest + this.kLatest) {
			ps.removeObservation(idOfOldestObservationThatIsNotOptimal.get());
		}

		/* update wins in node model */
		int winsLeft = ps.getWinsOfChildOverAnother(0, 1);
		int winsRight = ps.getWinsOfChildOverAnother(1, 0);
		//		if (nodeModel.depth < 5) {
		//			System.out.println(winsLeft + "/" + winsRight);
		//		}
		nodeModel.setWinsLeft(winsLeft);
		nodeModel.setWinsRight(winsRight);
		long walltime = System.currentTimeMillis() - start;
		if (walltime > 2) {
			System.out.println("Update took " + walltime);
		}
	}



}
