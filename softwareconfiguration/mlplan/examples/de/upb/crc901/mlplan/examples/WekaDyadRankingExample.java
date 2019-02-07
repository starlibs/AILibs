package de.upb.crc901.mlplan.examples;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import de.upb.crc901.mlplan.metamining.dyadranking.WEKADyadRankedNodeQueueConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class WekaDyadRankingExample {
	public static void main(final String[] args) throws Exception {
		
		ReproducibleInstances data = ReproducibleInstances.fromOpenML("40983", "4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances)data, (new Random(0)).nextLong(), 0.7d);

		
		/* initialize mlplan, and let it run for 30 seconds */

//		SQLAdapter adapter = new SQLAdapter("host", "user", "password", "database");
//        PerformanceDBAdapter pAdapter = new PerformanceDBAdapter(adapter, "performance_cache");

//		MLPlanWekaBuilder builder = new MLPlanWekaBuilder(
//				new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"), new File("conf/mlplan.properties"),
//				MultiClassPerformanceMeasure.ERRORRATE, pAdapter);
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder(
				new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"), new File("conf/mlplan.properties"),
				MultiClassPerformanceMeasure.ERRORRATE).usingBFwithDyadRankedNodeQueue(new WEKADyadRankedNodeQueueConfig());

		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);

		mlplan.setLoggerName("mlplan");
		mlplan.setTimeout(60);
		//System.out.println(mlplan.getHascoFactory());
		//BestFirst bestFirst = (BestFirst) mlplan.getHascoFactory().getAlgorithm().getHasco().getSearch();
		//System.out.println(bestFirst);
		//bestFirst.setOpen(new WEKADyadRankedNodeQueue(new DenseDoubleVector(new double[0])));
		//mlplan.activateVisualization();
		try {
			long start = System.currentTimeMillis();
			mlplan.buildClassifier(split.get(0));
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(mlplan, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
