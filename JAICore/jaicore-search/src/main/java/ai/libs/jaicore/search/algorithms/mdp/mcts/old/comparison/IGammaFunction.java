package ai.libs.jaicore.search.algorithms.mdp.mcts.old.comparison;

public interface IGammaFunction {
	public double getNodeGamma(int visits, double nodeProbability, double relativeDepth);
}
