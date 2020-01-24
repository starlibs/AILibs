package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalancedGraphGeneratorGenerator;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalancedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

@RunWith(Parameterized.class)
public class BalancedTreasureIslandTester extends SyntheticGraphTester {

	private final int branchingFactor;
	private final int depth;
	private final int distanceToIslands;
	private final int numberOfIslandsWithTreasure;
	private final int numberOfIslands;

	private int exactIslandSize;
	private IIslandModel islandModel;
	private ChaoticMeansTreasureModel  treasureGenerator;
	private BalancedGraphSearchWithPathEvaluationsProblem searchProblem;

	public BalancedTreasureIslandTester(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
		this.distanceToIslands = distanceToIslands;
		this.numberOfIslandsWithTreasure = numberOfIslandsWithTreasure;
		this.numberOfIslands = (int)Math.pow(branchingFactor, distanceToIslands);
	}

	// creates the test data
	@Parameters(name = "branchingFactor = {0}, depth = {1}, distanceToIslands = {2}, numberOfIslandsWithTreasure = {3}")
	public static Collection<Object[]> data() {

		final int MAX_BF = 4;
		final int MAX_DEPTH = 4;
		int combos = MAX_BF * MAX_DEPTH;

		Object[][] data = new Object[combos][4];
		int i = 0;
		for (int bf = 1; bf <= MAX_BF; bf++) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				data[i][0] = bf;
				data[i][1] = depth;
				data[i][2] = Math.min(3, depth);
				data[i][3] = Math.min(2, (int)Math.pow(bf, (int)data[i][2] - 1));
				i++;
			}
		}
		return Arrays.asList(data);
	}

	@Before
	public void setupTest() throws PathEvaluationException, InterruptedException {

		this.islandModel = new EqualSizedIslandsModel(BigInteger.valueOf((long)Math.pow(this.branchingFactor, this.depth - this.distanceToIslands)));
		this.treasureGenerator = new ChaoticMeansTreasureModel(this.numberOfIslandsWithTreasure, this.islandModel, 0);
		this.treasureGenerator.setLoggerName(this.getLoggerName() + ".treasuregen");
		this.searchProblem = new BalancedGraphSearchWithPathEvaluationsProblem(this.branchingFactor, this.depth, this.treasureGenerator);

		/* now tell the treasure generator about the graph */
		this.treasureGenerator.setGraphSearchInput(this.searchProblem);
		ITransparentTreeNode root = this.searchProblem.getGraphGenerator().getRootGenerator().getRoots().iterator().next();
		this.searchProblem.getPathEvaluator().evaluate(new SearchGraphPath<>(root)); // this triggers the generation of treasures

		/* now compute exact size of islands */
		this.exactIslandSize = BalancedGraphGeneratorGenerator.getNumberOfLeafsUnderANonTerminalNodeInDepth(this.distanceToIslands, this.branchingFactor, this.depth);
		this.logger.info("Now considering graph with bf {}, depth {}, (exact) island size {} (configured distance to islands was {}), and {} islands with treasure.", this.branchingFactor, this.depth, this.exactIslandSize, this.distanceToIslands, this.numberOfIslandsWithTreasure);
		assertEquals("Island size is not computed correctly.", (int)Math.pow(this.branchingFactor, this.depth - this.distanceToIslands), this.exactIslandSize);

		/* check that the island model is correct*/
		assertEquals(this.numberOfIslands, root.getNumberOfSubtreesWithMaxNumberOfNodes(BigInteger.valueOf(this.exactIslandSize)).intValueExact());
		assertEquals ("Island model has a wrong imagination of the number of islands.", this.numberOfIslands, this.islandModel.getNumberOfIslands().intValueExact());

		/* check that treasures have been distributed and that their number is correct */
		assertTrue("Treasures have not been distribued.", this.treasureGenerator.isTreasuresDistributed());
		assertEquals("The treasure model is not correct.", this.numberOfIslandsWithTreasure, this.treasureGenerator.getTreasureIslands().size());
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
		return this.numberOfIslands;
	}

	@Override
	public int getMaximumIslandSizes() {
		return this.exactIslandSize;
	}

	@Override
	public int getMinimumIslandSizes() {
		return this.exactIslandSize;
	}

	@Override
	public int getNumberOfTreasureIslands() {
		return this.numberOfIslandsWithTreasure;
	}

	@Override
	public boolean isPathATreasure(final ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		return this.treasureGenerator.isPathToTreasureIsland(path);
	}
}
