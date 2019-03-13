package de.upb.crc901.mlplan.examples.dyadranking;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.metamining.dyadranking.WEKADyadRankedNodeQueueConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.MLPlanWekaClassifier;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory;
import jaicore.basic.TimeOut;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class WekaDyadRankingExample {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaDyadRankingExample.class);

	public static void main(final String[] args) throws Exception {

		LOGGER.trace("test");

		ReproducibleInstances data = ReproducibleInstances.fromOpenML("40983", "4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances) data, (new Random(0)).nextLong(), 0.7d);

		// SQLAdapter adapter = new SQLAdapter("host", "user", "password", "database");
		// PerformanceDBAdapter pAdapter = new PerformanceDBAdapter(adapter,
		// "performance_cache");

		// MLPlanWekaBuilder builder = new MLPlanWekaBuilder(
		// new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"), new
		// File("conf/mlplan.properties"),
		// MultiClassPerformanceMeasure.ERRORRATE, pAdapter);

		WEKADyadRankedNodeQueueConfig openConfig = new WEKADyadRankedNodeQueueConfig();
		MLPlanBuilder builder = new MLPlanBuilder()
				.withSearchSpaceConfigFile(new File("conf/automl/searchmodels/weka/weka-all-dyadranking-reduced.json"))
				.withAlgorithmConfigFile(new File("conf/mlplan.properties"))
				.withPerformanceMeasure(MultiClassPerformanceMeasure.ERRORRATE)
				.setHascoFactory(new HASCOViaFDAndBestFirstWithDyadRankedNodeQueueFactory(openConfig));
		builder.prepareNodeEvaluatorInFactoryWithData(data);

		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);
		System.out.println(mlplan.getComponents());
		openConfig.setComponents(mlplan.getComponents());
		openConfig.setData(data);
		mlplan.setTimeout(new TimeOut(300, TimeUnit.SECONDS));

		// mlplan.activateVisualization();
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
