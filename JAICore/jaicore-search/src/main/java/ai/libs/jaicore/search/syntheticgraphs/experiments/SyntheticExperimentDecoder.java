package ai.libs.jaicore.search.syntheticgraphs.experiments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.experiments.Experiment;
import ai.libs.jaicore.search.experiments.ASearchExperimentDecoder;
import ai.libs.jaicore.search.experiments.StandardExperimentSearchAlgorithmFactory;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticTreasureIslandProblem;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.LinearTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.AbyssTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.DominatedFunnelTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.FunnelTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.funnel.RelativeFunnelTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.LinkedTreasureIslandPathCostGenerator;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.NoisyMeanTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ShiftedSineTreasureGenerator;

public class SyntheticExperimentDecoder extends ASearchExperimentDecoder<ITransparentTreeNode, Integer, ISyntheticTreasureIslandProblem, IEvaluatedPath<ITransparentTreeNode, Integer, Double>, IOptimalPathInORGraphSearch<? extends ISyntheticTreasureIslandProblem, ? extends IEvaluatedPath<ITransparentTreeNode,Integer, Double>, ITransparentTreeNode, Integer, Double>> {

	public SyntheticExperimentDecoder(final ISyntheticSearchExperimentConfig config) {
		super(config);
	}

	@Override
	public ISyntheticTreasureIslandProblem getProblem(final Experiment experiment) {

		/* check validity of experiment */
		this.checkThatAllKeyFieldsInExperimentAreDefined(experiment);

		/* read experiment data */
		Map<String, String> experimentData = experiment.getValuesOfKeyFields();
		int seed = Integer.parseInt(experimentData.get(ISyntheticSearchExperimentConfig.K_SEED));
		int branchingFactor = Integer.parseInt(experimentData.get(ISyntheticSearchExperimentConfig.K_BRANCHING));
		int depth = Integer.parseInt(experimentData.get(ISyntheticSearchExperimentConfig.K_DEPTH));
		double maxIslandSize = Double.parseDouble(experimentData.get(ITreasureIslandExperimentSetConfig.K_ISLANDS_MAXISLANDSIZE));
		int numberOfIslandsWithTreasure = Integer.parseInt(experimentData.get(ITreasureIslandExperimentSetConfig.K_ISLANDS_NUMBER_OF_TREASURES));

		/* derive number of leafs in total */
		BigInteger numberOfLeafs = BigInteger.valueOf(branchingFactor).pow(depth);
		BigInteger islandSize = new BigDecimal(numberOfLeafs).multiply(BigDecimal.valueOf(maxIslandSize)).round(new MathContext(1, RoundingMode.FLOOR)).toBigInteger();

		/* create graph search input */
		IIslandModel islandModel = new EqualSizedIslandsModel(islandSize);
		ITreasureModel treasureGenerator = this.getTreasureModel(islandModel, numberOfIslandsWithTreasure, new Random(seed), experimentData.get("treasuremodel"));
		return new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(seed), 0, branchingFactor, depth, treasureGenerator);
	}


	public ITreasureModel getTreasureModel(final IIslandModel islandModel, final int numberOfIslandsWithTreasure, final Random random, final String model) {
		switch (model) {
		case "abyss":
			return new AbyssTreasureModel(islandModel, numberOfIslandsWithTreasure, random);
		case "funnel":
			return new FunnelTreasureModel(islandModel, numberOfIslandsWithTreasure, random);
		case "relativefunnel":
			return new RelativeFunnelTreasureModel(islandModel, numberOfIslandsWithTreasure, random);
		case "dominatedfunnel":
			return new DominatedFunnelTreasureModel(islandModel, random);
		case "linear-asc":
			return new LinearTreasureModel();
		case "linear-desc":
			return new LinearTreasureModel(false);

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
	public IOptimalPathInORGraphSearch<? extends ISyntheticTreasureIslandProblem, ? extends IEvaluatedPath<ITransparentTreeNode, Integer, Double>, ITransparentTreeNode, Integer, Double> getAlgorithm(final Experiment experiment) {
		StandardExperimentSearchAlgorithmFactory<ITransparentTreeNode, Integer, ISyntheticTreasureIslandProblem> algoFactory = new StandardExperimentSearchAlgorithmFactory<>();
		return algoFactory.getAlgorithm(experiment, this.getProblem(experiment));
	}
}
