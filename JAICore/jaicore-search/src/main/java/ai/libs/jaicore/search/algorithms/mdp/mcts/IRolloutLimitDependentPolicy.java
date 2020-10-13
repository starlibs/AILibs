package ai.libs.jaicore.search.algorithms.mdp.mcts;

public interface IRolloutLimitDependentPolicy {
	public void setEstimatedNumberOfRemainingRollouts(int numRollouts);
}
