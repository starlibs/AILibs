package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public interface ISyntheticTreasureIslandProblem extends IGraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> {

}
