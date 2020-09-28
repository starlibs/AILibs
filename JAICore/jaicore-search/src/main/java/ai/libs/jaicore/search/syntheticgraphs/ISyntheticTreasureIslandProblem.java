package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public interface ISyntheticTreasureIslandProblem extends IPathSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> {

	public IIslandModel getIslandModel();

	public int getExpectedNumberOfIslands(); // we don't allow BigInteger here, because this could not be tested anyway!

	public int getMaximumIslandSizes(); // we don't allow BigInteger here, because this could not be tested anyway!

	public int getMinimumIslandSizes(); // we don't allow BigInteger here, because this could not be tested anyway!

	public int getNumberOfTreasureIslands(); // we don't allow BigInteger here, because this could not be tested anyway!

	public boolean isPathATreasure(ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException;
}
