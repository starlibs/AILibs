package ai.libs.jaicore.search.syntheticgraphs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.search.experiments.ISearchExperimentDecoder;
import ai.libs.jaicore.search.experiments.StandardExperimentSearchAlgorithmFactory;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.AbyssTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.FunnelTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.RelativeFunnelTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.LinkedTreasureIslandPathCostGenerator;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.NoisyMeanTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ShiftedSineTreasureGenerator;

public class SyntheticSearchProblemExperimentDecoder implements ISearchExperimentDecoder<ITransparentTreeNode, Integer, ISyntheticTreasureIslandProblem, IEvaluatedPath<ITransparentTreeNode, Integer, Double>, IOptimalPathInORGraphSearch<? extends ISyntheticTreasureIslandProblem, ? extends IEvaluatedPath<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double>> {

	@Override
	public ISyntheticTreasureIslandProblem getProblem(final Experiment experiment) {

		/* read experiment data */
		Map<String, String> experimentData = experiment.getValuesOfKeyFields();
		this.requireKeyField(experimentData, "seed");
		this.requireKeyField(experimentData, "branching");
		this.requireKeyField(experimentData, "depth");
		this.requireKeyField(experimentData, "maxislandsize");
		this.requireKeyField(experimentData, "treasures");
		int seed = Integer.parseInt(experimentData.get("seed"));
		int branchingFactor = Integer.parseInt(experimentData.get("branching"));
		int depth = Integer.parseInt(experimentData.get("depth"));
		double maxIslandSize = Double.parseDouble(experimentData.get("maxislandsize"));
		int numberOfIslandsWithTreasure = Integer.parseInt(experimentData.get("treasures"));

		/* derive number of leafs in total */
		BigInteger numberOfLeafs = BigInteger.valueOf(branchingFactor).pow(depth);
		BigInteger islandSize = new BigDecimal(numberOfLeafs).multiply(BigDecimal.valueOf(maxIslandSize)).round(new MathContext(1, RoundingMode.FLOOR)).toBigInteger();

		/* create graph search input */
		IIslandModel islandModel = new EqualSizedIslandsModel(islandSize);
		experimentData.put("treasuremodel", "abyss");
		ITreasureModel treasureGenerator = this.getTreasureModel(islandModel, numberOfIslandsWithTreasure, new Random(seed), experimentData.get("treasuremodel"));
		return new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(seed), 0, branchingFactor, depth, treasureGenerator);
	}

	private void requireKeyField(final Map<String, String> experimentData, final String field) {
		if (!experimentData.containsKey(field)) {
			throw new IllegalArgumentException("Invalid experiment definition. Field " + field + " is not defined!");
		}
	}

	public ITreasureModel getTreasureModel(final IIslandModel islandModel, final int numberOfIslandsWithTreasure, final Random random, final String model) {
		switch (model) {
		case "abyss":
			return new AbyssTreasureModel(islandModel, numberOfIslandsWithTreasure, random);
		case "funnel":
			return new FunnelTreasureModel(islandModel, numberOfIslandsWithTreasure, random);
		case "relativefunnel":
			return new RelativeFunnelTreasureModel(islandModel, numberOfIslandsWithTreasure, random);

		default:
			throw new IllegalArgumentException("Model " + model + " is not supported.");
		}
	}

	public NoisyMeanTreasureModel getTreasureGenerator(final String function, final IIslandModel islandModel, final int numberOfTreasures) {
		switch (function.toLowerCase()) {
		case "sine":
			ShiftedSineTreasureGenerator linkFuction = new ShiftedSineTreasureGenerator(islandModel, numberOfTreasures, 0.1, 0.5);
			LinkedTreasureIslandPathCostGenerator treasureGenerator = new LinkedTreasureIslandPathCostGenerator(islandModel, linkFuction);
			return treasureGenerator;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public IOptimalPathInORGraphSearch<? extends ISyntheticTreasureIslandProblem, ? extends IEvaluatedPath<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double> getAlgorithm(
			final Experiment experiment) {
		StandardExperimentSearchAlgorithmFactory<ITransparentTreeNode, Integer, ISyntheticTreasureIslandProblem> algoFactory = new StandardExperimentSearchAlgorithmFactory<>();
		return algoFactory.getAlgorithm(experiment, this.getProblem(experiment));
	}
}
