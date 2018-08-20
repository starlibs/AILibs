package de.upb.crc901.mlplan.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import de.upb.crc901.automl.hascoml.HASCOMLContinuousSelection;
import de.upb.crc901.automl.hascoml.HASCOMLContinuousSelectionSolution;
import de.upb.crc901.automl.hascoml.weka.WEKAOnlyPipelineFactory;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.FixedSplitValidationEvaluator;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class MLPlanForWekaBenchmarkExecutor {

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		File componentFile = new File("model/weka/mlplan-components.json");
		File datasetFile = new File("../../../datasets/classification/multi-class/abalone.arff");
		long seed = 1;
		int timeout = 60;

		HASCOMLContinuousSelection<Classifier> hml = new HASCOMLContinuousSelection<>(componentFile,
				"AbstractClassifier", timeout, seed);

		// Prepare dataset for benchmarks
		Instances data = new Instances(new FileReader(datasetFile));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> testSplit = WekaUtil.getStratifiedSplit(data, new Random(seed), .7);
		List<Instances> selectSplit = WekaUtil.getStratifiedSplit(testSplit.get(0), new Random(seed), .7);

		// initialize the benchmarks
		hml.setSearchBenchmark(new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(seed)), 5,
				selectSplit.get(0), .7f));
		hml.setSelectionBenchmark(new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(new Random(seed)), 10,
				testSplit.get(0), .7f));
		FixedSplitValidationEvaluator testBenchmark = new FixedSplitValidationEvaluator(
				new MulticlassEvaluator(new Random(seed)), testSplit.get(0), testSplit.get(1));
		hml.setTestBenchmark(testBenchmark);

		hml.setTimeout(timeout * 1000);
		hml.gatherSolutions(timeout * 1000, new WEKAOnlyPipelineFactory());

		Map<String, Object> result = new HashMap<>();
		if (hml.getCurrentBestSolution() != null) {
			HASCOMLContinuousSelectionSolution<Classifier> solution = hml.getCurrentBestSolution();
			Classifier c = solution.getSolution();

			System.out.println("Solution found: " + c);
			System.out.println("Val Loss: " + solution.getValidationScore());
			if (solution.getSelectionScore() != null) {
				System.out.println("Select Loss: " + solution.getSelectionScore());
			}

			if (solution.getTestScore() == null) {
				try {
					solution.setTestScore(testBenchmark.evaluate(c));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Test Loss: " + solution.getTestScore());

		} else {
			throw new NoSuchElementException("no solution has been found in the given timeout");
		}

	}

}
