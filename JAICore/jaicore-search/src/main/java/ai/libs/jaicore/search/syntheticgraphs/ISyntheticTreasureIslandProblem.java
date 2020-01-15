package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public interface ISyntheticTreasureIslandProblem extends IPathSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> {

}
