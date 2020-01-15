package ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public class BalanceGraphSearchProblem extends GraphSearchInput<ITransparentTreeNode, Integer> {

	public BalanceGraphSearchProblem(final int branchingFactor, final int depth) {
		super(new BalancedGraphGeneratorGenerator(branchingFactor, depth).create(), new INodeGoalTester<ITransparentTreeNode, Integer>() {

			@Override
			public boolean isGoal(final ITransparentTreeNode node) {
				return node.getDepth() == depth;
			}
		});
	}
}
