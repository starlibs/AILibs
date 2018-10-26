package de.upb.crc901.mlpipeline_evaluation;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.NotImplementedException;

import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.ClassifierEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
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
	public static double evaluateClassifier(String testSplitTechnique, String testEvaluationTechnique, int testSeed,
			String valSplitTechnique, String valEvaluationTechnique, int valSeed, Instances data, Classifier classifier)
			throws Exception {
		switch (testEvaluationTechnique) {
		case "single":
			return evaluateClassifier(valSplitTechnique, valEvaluationTechnique, valSeed,
					getTrainSplit(testSplitTechnique, data, testSeed), classifier);
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
	 * @param split_technique
	 * @param evaluation_technique
	 * @param seed
	 * @param data
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	public static double evaluateClassifier(String split_technique, String evaluation_technique, int seed,
			Instances data, Classifier classifier) throws Exception {
		switch (evaluation_technique) {
		case "single":
			Instances train_split = ConsistentMLPipelineEvaluator.getTrainSplit(split_technique, data, seed);
			Evaluation eval = new Evaluation(train_split);
			classifier.buildClassifier(train_split);
			eval.evaluateModel(classifier, ConsistentMLPipelineEvaluator.getTestSplit(split_technique, data, seed));
			return (1 - eval.pctCorrect() / 100.0d);
		case "multi":
			return ConsistentMLPipelineEvaluator.getEvaluatorForSplitTechnique(split_technique, data, seed)
					.evaluate(classifier);
		default:
			throw new IllegalArgumentException("Invalid split technique: " + evaluation_technique);
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
	public static ClassifierEvaluator getEvaluatorForSplitTechnique(String split_technique, Instances data, int seed) {
		String[] techniqueAndDescription = split_technique.split("_");

		switch (techniqueAndDescription[0]) {
		case "3MCCV":
			return new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(seed)), 3, data,
					Float.parseFloat(techniqueAndDescription[1]));
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
	public static Instances getTrainSplit(String split_technique, Instances data, int seed) {
		String[] techniquAndDescription = split_technique.split("_");

		switch (techniquAndDescription[0]) {
		case "MCCV":
			Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data, new Random(seed),
					Double.parseDouble(techniquAndDescription[1]));
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
	public static Instances getTestSplit(String split_technique, Instances data, int seed) {
		String[] techniquAndDescription = split_technique.split("_");

		switch (techniquAndDescription[0]) {
		case "MCCV":
			Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data, new Random(seed),
					Double.parseDouble(techniquAndDescription[1]));
			List<Instances> folds = WekaUtil.realizeSplit(data, instancesInFolds);
			return folds.get(1);
		}

		return null;
	}
}
