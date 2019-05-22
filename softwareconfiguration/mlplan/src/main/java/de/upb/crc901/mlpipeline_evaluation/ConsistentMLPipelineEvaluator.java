package de.upb.crc901.mlpipeline_evaluation;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.NotImplementedException;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * For consistent evaluations of MLPipelines.
 *
 * @author Helena Graf
 * @author Lukas
 * @author Joshua
 *
 */
public class ConsistentMLPipelineEvaluator {

	private ConsistentMLPipelineEvaluator() {
		/* Private c'tor to prevent instantiation. */
	}

	/**
	 * Get the error rate of the classifier according to the given info about the
	 * split and evaluation technique.
	 *
	 * @param testSplitTechnique
	 * @param testEvaluationTechnique
	 * @param testSeed
	 * @param valSplitTechnique
	 * @param valEvaluationTechnique
	 * @param valSeed
	 * @param data
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	public static double evaluateClassifier(final String testSplitTechnique, final String testEvaluationTechnique, final int testSeed, final String valSplitTechnique, final String valEvaluationTechnique, final int valSeed,
			final Instances data, final Classifier classifier) throws Exception {
		switch (testEvaluationTechnique) {
		case "single":
			return evaluateClassifier(valSplitTechnique, valEvaluationTechnique, valSeed, getTrainSplit(testSplitTechnique, data, testSeed), classifier);
		case "multi":
			throw new NotImplementedException("\"multi\" not yet supported!");
		default:
			throw new IllegalArgumentException("Unkown evaluation technique.");
		}
	}

	/**
	 * Get the error rate of the classifier according to the given info about the
	 * split and evaluation technique.
	 *
	 * @param splitTechnique
	 * @param evaluationTechnique
	 * @param seed
	 * @param data
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	public static double evaluateClassifier(final String splitTechnique, final String evaluationTechnique, final int seed, final Instances data, final Classifier classifier) throws Exception {
		switch (evaluationTechnique) {
		case "single":
			Instances trainSplit = ConsistentMLPipelineEvaluator.getTrainSplit(splitTechnique, data, seed);
			Evaluation eval = new Evaluation(trainSplit);
			classifier.buildClassifier(trainSplit);
			eval.evaluateModel(classifier, ConsistentMLPipelineEvaluator.getTestSplit(splitTechnique, data, seed));
			return (1 - eval.pctCorrect() / 100.0d);
		case "multi":
			IClassifierEvaluator evaluator = ConsistentMLPipelineEvaluator.getEvaluatorForSplitTechnique(splitTechnique, data, seed);
			if (evaluator != null) {
				return evaluator.evaluate(classifier);
			} else {
				throw new IllegalArgumentException("Could not find classifier evaluator.");
			}
		default:
			throw new IllegalArgumentException("Invalid split technique: " + evaluationTechnique);
		}
	}

	/**
	 * Get an evaluator object for the given split configuration for the datasets,
	 * which can then be used to evaluate a classifier.
	 *
	 * @param split_technique
	 * @param data
	 * @param seed
	 * @return
	 */
	public static IClassifierEvaluator getEvaluatorForSplitTechnique(final String split_technique, final Instances data, final int seed) {
		String[] techniqueAndDescription = split_technique.split("_");

		if (techniqueAndDescription[0].equals("3MCCV")) {
			return new MonteCarloCrossValidationEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()), 3, data, Float.parseFloat(techniqueAndDescription[1]), seed);
		}

		return null;
	}

	/**
	 * Split the dataset according to the given parameters and return the train
	 * portion of the split.
	 *
	 * @param split_technique
	 * @param data
	 * @param seed
	 * @return
	 */
	public static Instances getTrainSplit(final String split_technique, final Instances data, final int seed) {
		String[] techniquAndDescription = split_technique.split("_");

		if (techniquAndDescription[0].equals("MCCV")) {
			Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data, new Random(seed), Double.parseDouble(techniquAndDescription[1]));
			List<Instances> folds = WekaUtil.realizeSplit(data, instancesInFolds);
			return folds.get(0);
		}

		return null;
	}

	/**
	 * Split the dataset according to the given parameters and return the test
	 * portion of the split.
	 *
	 * @param split_technique
	 * @param data
	 * @param seed
	 * @return
	 */
	public static Instances getTestSplit(final String split_technique, final Instances data, final int seed) {
		String[] techniquAndDescription = split_technique.split("_");

		if (techniquAndDescription[0].equals("MCCV")) {
			Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data, new Random(seed), Double.parseDouble(techniquAndDescription[1]));
			List<Instances> folds = WekaUtil.realizeSplit(data, instancesInFolds);
			return folds.get(1);
		}

		return null;
	}
}
