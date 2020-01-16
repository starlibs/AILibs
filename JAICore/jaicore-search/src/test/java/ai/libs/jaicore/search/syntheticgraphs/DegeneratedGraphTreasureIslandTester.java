package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.DegeneratedGraphGenerator;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

@RunWith(Parameterized.class)
public class DegeneratedGraphTreasureIslandTester extends SyntheticGraphTester {

	private final int branchingFactor;
	private final int depth;
	private final BigInteger maxIslandSize;
	private int exactIslandSize;
	private final BigInteger numberOfIslandsWithTreasure;
	private IIslandModel islandModel;
	private ChaoticMeansTreasureModel  treasureGenerator;
	private DegeneratedGraphSearchWithPathEvaluationsProblem searchProblem;

	public DegeneratedGraphTreasureIslandTester(final int branchingFactor, final int depth, final int maxIslandSize, final int numberOfIslandsWithTreasure) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
		this.maxIslandSize = BigInteger.valueOf(maxIslandSize);
		this.numberOfIslandsWithTreasure = BigInteger.valueOf(numberOfIslandsWithTreasure);
	}

	// creates the test data
	@Parameters(name = "branchingFactor = {0}, depth = {1}, maxIslandSize = {2}, numberOfIslandsWithTreasure = {3}")
	public static Collection<Object[]> data() {

		final int MAX_BF = 8;
		final int MAX_DEPTH = 4;
		final int MAX_ISLANDSIZE = 10;
		int combos = (MAX_BF / 2) * MAX_DEPTH * MAX_ISLANDSIZE;

		Object[][] data = new Object[combos][4];
		int i = 0;
		for (int bf = 2; bf <= MAX_BF; bf+=2) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				for (int islandSize = 1; islandSize <= MAX_ISLANDSIZE; islandSize++) {
					data[i][0] = bf;
					data[i][1] = depth;
					data[i][2] = (int)Math.min(Math.pow(bf / 2, depth), islandSize);
					data[i][3] = 1;
					i++;
				}
			}
		}
		List<Object[]> dataAsList = Arrays.asList(data);
		assertEquals(new HashSet<>(dataAsList).size(), dataAsList.size());
		return dataAsList;
	}

	@Before
	public void setupTest() throws PathEvaluationException, InterruptedException {
		this.islandModel = new EqualSizedIslandsModel(this.maxIslandSize);
		this.treasureGenerator = new ChaoticMeansTreasureModel(this.numberOfIslandsWithTreasure.intValue(), this.islandModel, 0);
		this.treasureGenerator.setLoggerName(this.getLoggerName() + ".treasuregen");
		this.searchProblem = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), this.branchingFactor / 2,this.branchingFactor, this.depth, this.treasureGenerator);
		this.treasureGenerator.setGraphSearchInput(this.searchProblem);
		DegeneratedGraphGenerator gg = this.searchProblem.getGraphGenerator();
		this.searchProblem.getPathEvaluator().evaluate(new SearchGraphPath<>(gg.getRootGenerator().getRoot())); // this triggers the generation of treasures
		this.exactIslandSize = gg.getMaxNumberOfLeafsInEverySubtreeOfMaxLength(this.maxIslandSize).intValueExact();
		this.logger.info("Now considering graph with bf {}, depth {}, (exact) island size {} (max configured size was {}), and {} islands with treasure.", this.branchingFactor, this.depth, this.exactIslandSize, this.maxIslandSize.intValueExact(), this.numberOfIslandsWithTreasure.intValue());
		assertTrue("Treasures have not been distribued.", this.treasureGenerator.isTreasuresDistributed());
		assertEquals("The treasure model is not correct.", this.numberOfIslandsWithTreasure.intValueExact(), this.treasureGenerator.getTreasureIslands().size());
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
