package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.HashMap;
import java.util.Map;

public class NodeLabel<A> {

	private int visits = 0;
	private Map<A, Integer> numberOfChoicesPerAction = new HashMap<>();
	private Map<A, Double> accumulatedRewardsOfAction = new HashMap<>();


	public int getVisits() {
		return this.visits;
	}

	public void setVisits(final int visits) {
		this.visits = visits;
	}

	public Map<A, Integer> getNumberOfChoicesPerAction() {
		return this.numberOfChoicesPerAction;
	}

	public void setNumberOfChoicesPerAction(final Map<A, Integer> numberOfChoicesPerAction) {
		this.numberOfChoicesPerAction = numberOfChoicesPerAction;
	}

	public double getAccumulatedRewardsOfAction(final A action) {
		return this.accumulatedRewardsOfAction.computeIfAbsent(action, a -> 0.0);
	}

	public void setAccumulatedRewardsOfAction(final Map<A, Double> accumulatedRewardsOfAction) {
		this.accumulatedRewardsOfAction = accumulatedRewardsOfAction;
	}

	public int getNumPulls(final A action) {
		return this.numberOfChoicesPerAction.computeIfAbsent(action, a -> 0);
	}

	public boolean isVirgin(final A action) {
		return !this.numberOfChoicesPerAction.containsKey(action);
	}

	public double getAverageRewardOfAction(final A action) {
		return this.getAccumulatedRewardsOfAction(action) / this.getNumPulls(action);
	}

	public void addRewardForAction(final A action, final double reward) {
		this.accumulatedRewardsOfAction.put(action,  this.getAccumulatedRewardsOfAction(action) + reward);
	}

	public void addVisit() {
		this.visits ++;
	}

	public void addPull(final A a) {
		this.numberOfChoicesPerAction.put(a, this.numberOfChoicesPerAction.computeIfAbsent(a, ac -> 0) + 1);
	}
}
