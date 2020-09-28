package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.DegeneratedGraphGenerator;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

@RunWith(Parameterized.class)
public class DegeneratedGraphTreasureIslandTester extends SyntheticGraphTester {



	public static Stream<Arguments> getTreeSetups() {

		final int MAX_BF = 8;
		final int MAX_DEPTH = 4;
		final int MAX_ISLANDSIZE = 10;

		List<Arguments> data = new ArrayList<>();
		for (int bf = 2; bf <= MAX_BF; bf += 2) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				for (int islandSize = 1; islandSize <= MAX_ISLANDSIZE; islandSize++) {
					data.add(Arguments.of(bf, depth, (int) Math.min(Math.pow(bf / 2, depth), islandSize),  1));
				}
			}
		}
		return data.stream();
	}

	private int exactIslandSize;
	private IIslandModel islandModel;
	private ChaoticMeansTreasureModel treasureGenerator;
	private DegeneratedGraphSearchWithPathEvaluationsProblem searchProblem;

	public void setupTest() throws PathEvaluationException, InterruptedException {
		this.islandModel = new EqualSizedIslandsModel(this.maxIslandSize);
		this.treasureGenerator = new ChaoticMeansTreasureModel(this.numberOfIslandsWithTreasure.intValue(), this.islandModel, 0);
		this.treasureGenerator.setLoggerName(this.getLoggerName() + ".treasuregen");
		this.searchProblem = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), this.branchingFactor / 2, this.branchingFactor, this.depth, this.treasureGenerator);
		this.treasureGenerator.setGraphSearchInput(this.searchProblem);
		DegeneratedGraphGenerator gg = this.searchProblem.getGraphGenerator();
		this.searchProblem.getPathEvaluator().evaluate(new SearchGraphPath<>(gg.getRootGenerator().getRoot())); // this triggers the generation of treasures
		this.exactIslandSize = gg.getMaxNumberOfLeafsInEverySubtreeOfMaxLength(this.maxIslandSize).intValueExact();
		this.logger.info("Now considering graph with bf {}, depth {}, (exact) island size {} (max configured size was {}), and {} islands with treasure.", this.branchingFactor, this.depth, this.exactIslandSize,
				this.maxIslandSize.intValueExact(), this.numberOfIslandsWithTreasure.intValue());
		assertTrue(this.treasureGenerator.isTreasuresDistributed(), "Treasures have not been distribued.");
		assertEquals(this.numberOfIslandsWithTreasure.intValueExact(), this.treasureGenerator.getTreasureIslands().size(), "The treasure model is not correct.");
	}

	@Override
	public IPathSearchInput<ITransparentTreeNode, Integer> getSearchProblem() {
		return this.searchProblem;
	}

	@Override
	public IIslandModel getIslandModel() {
		return this.islandModel;
	}

	@Override
	public int getExpectedNumberOfIslands() {
		return this.searchProblem.getGraphGenerator().getRootGenerator().getRoots().iterator().next().getNumberOfSubtreesWithMaxNumberOfNodes(this.maxIslandSize).intValueExact();
	}

	public int getExactMaximumIslandSize() {
		return this.exactIslandSize;
	}

	@Override
	public int getMaximumIslandSizes() {
		return this.getExactMaximumIslandSize();
	}

	@Override
	public int getMinimumIslandSizes() {
		return 1; // there can always be islands of size 1
	}

	@Override
	public boolean isPathATreasure(final ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		return this.treasureGenerator.isPathToTreasureIsland(path);
	}

	@Override
	public int getNumberOfTreasureIslands() {
		return this.numberOfIslandsWithTreasure.intValue();
	}
}
