package de.upb.crc901.mlplan.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import hasco.knowledgebase.FANOVAParameterImportanceEstimator;
import jaicore.basic.SQLAdapter;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.evaluation.measures.multiclass.MultiClassPerformanceMeasure;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * This is an example class that illustrates the usage of ML-Plan on the segment
 * dataset of OpenML. It is configured to run for 30 seconds and to use 70% of
 * the data for search and 30% for selection in its second phase.
 *
 * The API key used for OpenML is ML-Plan's key (read only).
 *
 * @author fmohr
 *
 */
public class MLPlanOpenMLExample {

	public static void main(final String[] args) throws Exception {

		ReproducibleInstances data = ReproducibleInstances.fromOpenML("181", "4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances)data, (new Random(0)).nextLong(), 0.7d);
		/* initialize mlplan, and let it run for 30 seconds */

//		SQLAdapter adapter = new SQLAdapter("host", "user", "password", "database");
//        PerformanceDBAdapter pAdapter = new PerformanceDBAdapter(adapter, "performance_cache");

		MLPlanWekaBuilder builder = new MLPlanWekaBuilder(
				new File("conf/automl/searchmodels/weka/svm.json"), new File("conf/mlplan.properties"),
				MultiClassPerformanceMeasure.ERRORRATE);
		
		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);
		
		mlplan.setLoggerName("mlplan");
		mlplan.setUseParameterPruning(true);
		mlplan.setParameterImportanceEstimator(new FANOVAParameterImportanceEstimator("test", 6, 0.20d));
		mlplan.setTimeout(180);
		mlplan.setTimeoutForNodeEvaluation(300);
		mlplan.setTimeoutForSingleSolutionEvaluation(300);
		mlplan.activateVisualization();
		try {
			long start = System.currentTimeMillis();
			mlplan.buildClassifier(split.get(0));
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(mlplan, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
			System.out.println("pruned params: " + mlplan.getParameterImportanceEstimator().getPrunedParameters());
			System.out.println("number pruned: " + mlplan.getParameterImportanceEstimator().getNumberPrunedParameters());
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
