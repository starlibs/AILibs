package ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated;

import java.util.Random;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticTreasureIslandProblem;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.DegeneratedGraphGenerator;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;

public class DegeneratedGraphSearchWithPathEvaluationsProblem extends GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> implements ISyntheticTreasureIslandProblem {

	public DegeneratedGraphSearchWithPathEvaluationsProblem(final Random random, final int deadEndsPerGeneration, final int branchingFactor, final int depth, final ITreasureModel generator) {
		super(new DegeneratedGraphSearchProblem(random, deadEndsPerGeneration, branchingFactor, depth), generator);
	}

	@Override
	public DegeneratedGraphGenerator getGraphGenerator() {
		return (DegeneratedGraphGenerator)super.getGraphGenerator();
	}
}
