package ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.TreeNode;

public class DegeneratedGraphSearchProblem extends GraphSearchInput<ITransparentTreeNode, Integer> {

	public DegeneratedGraphSearchProblem(final Random r, final int deadEndsPerGeneration, final int branchingFactor, final int depth) {
		super(new DegeneratedGraphGeneratorGenerator(r, deadEndsPerGeneration, branchingFactor, depth).create(), new NodeGoalTester<ITransparentTreeNode, Integer>() {

			@Override
			public boolean isGoal(final ITransparentTreeNode node) {
				return !((TreeNode)node).hasChildren;
			}
		});
	}
}
