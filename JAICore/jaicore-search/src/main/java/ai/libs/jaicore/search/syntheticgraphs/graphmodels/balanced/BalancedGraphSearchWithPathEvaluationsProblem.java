package ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticTreasureIslandProblem;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;

public class BalancedGraphSearchWithPathEvaluationsProblem extends GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> implements ISyntheticTreasureIslandProblem {

	public BalancedGraphSearchWithPathEvaluationsProblem(final int branchingFactor, final int depth, final ITreasureModel generator) {
		super(new BalanceGraphSearchProblem(branchingFactor, depth), generator);
	}
}
