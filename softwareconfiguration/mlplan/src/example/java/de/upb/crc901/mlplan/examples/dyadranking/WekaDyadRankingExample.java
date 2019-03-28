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
import jaicore.basic.TimeOut;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * Demonstrated the usage of ML-Plan with a dyad ranked OPEN list
 * 
 * @author Helena Graf
 *
 */
public class WekaDyadRankingExample {

	private static final Logger LOGGER = LoggerFactory.getLogger(WekaDyadRankingExample.class);

	public static void main(final String[] args) throws Exception {
		long starttime = System.currentTimeMillis();

		LOGGER.trace("test");

		ReproducibleInstances data = ReproducibleInstances.fromOpenML("40983", "4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances) data, (new Random(0)).nextLong(), 0.7d);

		WEKADyadRankedNodeQueueConfig openConfig = new WEKADyadRankedNodeQueueConfig();
		MLPlanBuilder builder = new MLPlanBuilder()
				.withSearchSpaceConfigFile(new File("resources/automl/searchmodels/weka/weka-approach-5-autoweka.json"))
				.withAlgorithmConfigFile(new File("conf/mlplan.properties"))
				.withPerformanceMeasure(MultiClassPerformanceMeasure.ERRORRATE).withOPENListConfiguration(openConfig);

		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);
		openConfig.setComponents(mlplan.getComponents());
		openConfig.setData(data);

		try {
			long start = System.currentTimeMillis();
			mlplan.setTimeout(new TimeOut(60 - (start - starttime) / 1000, TimeUnit.SECONDS));
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
		System.out.println("Total experiment time: " + (System.currentTimeMillis() - starttime) / 1000);
	}
}
