package de.upb.crc901.mlplan.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import de.upb.crc901.mlplan.multiclass.classifiers.TwoPhaseHTNBasedPipelineSearcher;
import de.upb.crc901.mlplan.multiclass.core.MLUtil;
import de.upb.crc901.mlplan.multiclass.evaluators.DoubleRandomCompletionEvaluator;
import jaicore.ml.WekaUtil;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class PipelineOptimizerTest {

	@Test
	public void test() throws Exception {
		Random r = new Random(0);

		int runs = 5;
		int timeoutPerRun = 3 * 60 * 1000;
		int timeoutForFComputation = 60 * 1000;

		final String dataset = "autowekasets/yeast";
		System.out.print("Reading in data ...");
		// Instances overallData = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + ".arff")));
		// overallData.setClassIndex(overallData.numAttributes() - 1);
		// List<Instances> overallSplit = WekaUtil.getStratifiedSplit(overallData, new Random(2), .7f);
		Instances data = new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/train.arff")));
		data.addAll(new Instances(new BufferedReader(new FileReader("testrsc/" + dataset + "/test.arff"))));
		// Instances internalData = overallSplit.get(0);
		// Instances testData = overallSplit.get(1);
		System.out.println("Done");
		data.setClassIndex(data.numAttributes() - 1);

		TwoPhaseHTNBasedPipelineSearcher<Double> optimizer = new TwoPhaseHTNBasedPipelineSearcher(MLUtil.getGraphGenerator(new File("testrsc/automl3.testset"), null, null, null), r, timeoutPerRun,
				timeoutForFComputation, 100, 20, false);
		optimizer.setRce(new DoubleRandomCompletionEvaluator(r, 3, new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(r), 3, .7f)));
//		optimizer.setTooltipGenerator(new TFDTooltipGenerator());

		/* now evaluate the approach */
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int runId = 1; runId <= runs; runId++) {
			
			/* create a split */
			List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(runId), .7f);
			Instances internalData = split.get(0);
			Instances testData = split.get(1);

			/* now search for the best pipeline */
			long start = System.currentTimeMillis();
			optimizer.buildClassifier(internalData);
			long end = System.currentTimeMillis();
			System.out.println("Search has finished. Runtime: " + (end - start) / 1000f + " s");

			/* check performance of the pipeline */
			optimizer.buildClassifier(internalData);
			Evaluation eval = new Evaluation(internalData);
			eval.evaluateModel(optimizer, testData);
			System.out.println("Error of returned solution: " + (eval.pctIncorrect() + eval.pctUnclassified()) + ".\n-----------------------------");
//			System.out.println(MLUtil.getJavaCodeFromPlan(optimizer.getSelectedModel().getCreationPlan()));
			stats.addValue(eval.pctIncorrect());
		}

		/* print stats */
		System.out.println("Overall error stats: " + stats);
		while (true);
	}
}
