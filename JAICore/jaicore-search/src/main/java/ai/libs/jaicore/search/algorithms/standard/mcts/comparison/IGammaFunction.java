package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

public interface IGammaFunction {
	public double getNodeGamma(int visits, double nodeProbability, double relativeDepth);
}
