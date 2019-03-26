package de.upb.crc901.mlplan.examples;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import jaicore.basic.SQLAdapter;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * This is an example class that illustrates the usage of ML-Plan on the segment dataset of OpenML. It is configured to run for 30 seconds and to use 70% of the data for search and 30% for selection in its second phase.
 *
 * The API key used for OpenML is ML-Plan's key (read only).
 *
 * @author fmohr
 *
 */
public class MLPlanOpenMLExample {

	public static void main(final String[] args) throws Exception {

		ReproducibleInstances data = ReproducibleInstances.fromOpenML("40983", "4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances) data, (new Random(0)).nextLong(), 0.7d);

		/* initialize mlplan, and let it run for 30 seconds */

	//	SQLAdapter adapter = new SQLAdapter("host", "user", "password", "database");
      //  PerformanceDBAdapter pAdapter = new PerformanceDBAdapter(adapter, "performance_cache");

		MLPlanBuilder builder = new MLPlanBuilder(
				new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"), new File("conf/mlplan.properties"),
				MultiClassPerformanceMeasure.ERRORRATE);

		MLPlan mlplan = new MLPlan(builder, split.get(0));

		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(30, TimeUnit.SECONDS);
		// mlplan.activateVisualization();
		try {
			long start = System.currentTimeMillis();
			Classifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(optimizedClassifier, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
