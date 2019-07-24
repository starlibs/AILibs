package ai.libs.jaicore.search.syntheticgraphs;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public class BalanceGraphSearchProblem extends GraphSearchInput<N, Integer> {

	public BalanceGraphSearchProblem(final int branchingFactor, final int depth) {
		super(new BalancedGraphGeneratorGenerator(branchingFactor, depth).create(), new NodeGoalTester<N, Integer>() {

			@Override
			public boolean isGoal(final N node) {
				return node.depth == depth;
			}
		});
	}
}
