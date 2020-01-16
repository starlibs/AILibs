package ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.DegeneratedGraphGenerator;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.TreeNode;

public class DegeneratedGraphSearchProblem extends GraphSearchInput<ITransparentTreeNode, Integer> {

	public DegeneratedGraphSearchProblem(final Random r, final int deadEndsPerGeneration, final int branchingFactor, final int depth) {
		super(new DegeneratedGraphGeneratorGenerator(r, deadEndsPerGeneration, branchingFactor, depth).build(), new INodeGoalTester<ITransparentTreeNode, Integer>() {

			@Override
			public boolean isGoal(final ITransparentTreeNode node) {
				return !((TreeNode)node).hasChildren;
			}
		});
	}

	@Override
	public DegeneratedGraphGenerator getGraphGenerator() {
		return (DegeneratedGraphGenerator)super.getGraphGenerator();
	}
}
