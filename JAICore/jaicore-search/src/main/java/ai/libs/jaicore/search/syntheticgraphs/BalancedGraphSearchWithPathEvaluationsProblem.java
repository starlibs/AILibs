package ai.libs.jaicore.search.syntheticgraphs;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public class BalancedGraphSearchWithPathEvaluationsProblem extends GraphSearchWithPathEvaluationsInput<N, Integer, Double> {

	public BalancedGraphSearchWithPathEvaluationsProblem(final int branchingFactor, final int depth, final TreasureIslandPathCostGenerator generator) {
		super(new BalanceGraphSearchProblem(branchingFactor, depth), generator);
	}
}
