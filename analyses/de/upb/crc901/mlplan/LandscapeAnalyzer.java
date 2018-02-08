package de.upb.crc901.mlplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.upb.crc901.mlplan.core.MLPipeline;
import de.upb.crc901.mlplan.core.MLUtil;
import de.upb.crc901.mlplan.core.SolutionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.DoubleRandomCompletionEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MonteCarloCrossValidationEvaluator;
import de.upb.crc901.mlplan.search.evaluators.MulticlassEvaluator;
import de.upb.crc901.mlplan.search.evaluators.RandomCompletionEvaluator;
import jaicore.basic.FileUtil;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNUtil;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import weka.classifiers.Classifier;
import weka.classifiers.meta.ClassificationViaRegression;
import weka.core.Instances;

public class LandscapeAnalyzer {

	public static void main(String[] args) throws Throwable {
		genData("autowekasets/glass");
	}

	public static void recoverResults(String dataset) throws Throwable {
		String filename = getSerializationFileName(dataset);
		System.out.println("Reading data from " + filename);
		Map<String, Map<String, Map<List<TFDNode>, Integer>>> scores = (Map<String, Map<String, Map<List<TFDNode>, Integer>>>) FileUtil.unserializeObject(filename);
		for (String preprocessor : scores.keySet()) {
			System.out.println(preprocessor);
			for (String classifier : scores.get(preprocessor).keySet()) {
				System.out.println("\t" + classifier);
				for (List<TFDNode> solution : scores.get(preprocessor).get(classifier).keySet()) {
					System.out.println("\t\t" + scores.get(preprocessor).get(classifier).get(solution) + " for "
							+ MLUtil.extractGeneratedClassifierFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(solution)));
				}
			}
		}

	}

	public static void genData(String dataset) throws Throwable {
		System.out.print("Reading in data ...");
		// Instances overallData = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + ".arff")));
		// overallData.setClassIndex(overallData.numAttributes() - 1);
		// List<Instances> overallSplit = WekaUtil.getStratifiedSplit(overallData, new Random(2), .7f);
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + ".arff")));
		// data.addAll(new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/test.arff"))));
		// Instances internalData = overallSplit.get(0);
		// Instances testData = overallSplit.get(1);
		
		System.out.println("Done");
		data.setClassIndex(data.numAttributes() - 1);
		Classifier c = new ClassificationViaRegression();
		ClassLoader loader = ClassificationViaRegression.class.getClassLoader();
        System.out.println(loader.getResource("weka/classifiers/meta/ClassificationViaRegression.class"));
//		System.exit(0);
		Random r = new Random(0);
		GraphGenerator<TFDNode, String> graphGenerator = MLUtil.getGraphGenerator(new File("testrsc/automl3.testset"), null, null, null);
		SolutionEvaluator solutionEvaluator = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(r), 3, .7f);
		RandomCompletionEvaluator<Double> rce = new DoubleRandomCompletionEvaluator(r, 3, solutionEvaluator);
		rce.setGenerator(graphGenerator);
		rce.setData(data);
		rce.setMaxSolutionsPerTechnique(5);

		ORGraphSearch<TFDNode, String, Double> search = new ORGraphSearch<>(graphGenerator, rce);
		search.parallelizeNodeExpansion(6);
		search.setTimeoutForComputationOfF(1000 * 10, n -> null);
		SimpleGraphVisualizationWindow<Node<TFDNode, Double>> visualizer = new SimpleGraphVisualizationWindow<>(search.getEventBus());
//		visualizer.getPanel().setTooltipGenerator(new TFDTooltipGenerator());
		List<TFDNode> solution;
		List<List<TFDNode>> solutions = new ArrayList<>();
		// Map<String,Map<String,Map<List<TFDNode>,Integer>>> scores = new HashMap<>();
		LandscapeDBAdapater mysql = new LandscapeDBAdapater();
		while ((solution = search.nextSolution()) != null) {
			solutions.add(solution);
			MLPipeline pipeline = (MLPipeline)MLUtil.extractGeneratedClassifierFromPlan(CEOCSTNUtil.extractPlanFromSolutionPath(solution));
			mysql.addEntry(dataset, pipeline, (int)Math.round(search.getFOfReturnedSolution(solution)));
		}
		search.cancel();

		System.out.println("Finished search. Found " + solutions.size() + " solutions.");
	}

	private static String getSerializationFileName(String dataset) {
		return "tmp/landscape_results_" + dataset.substring(dataset.lastIndexOf("/") + 1);
	}
}
