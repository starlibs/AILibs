package de.upb.crc901.automl.mlplan.examples;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import jaicore.basic.TimeOut;
import jaicore.ml.WekaUtil;
import jaicore.ml.cache.ReproducibleInstances;
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

		ReproducibleInstances data = ReproducibleInstances.fromOpenML("3", "4350e421cdc16404033ef1812ea38c01");
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit((Instances) data, (new Random(0)).nextLong(), 0.7d);

		/* initialize mlplan, and let it run for 30 seconds */
		AbstractMLPlanBuilder builder = AbstractMLPlanBuilder.forWeka();
		builder.withNodeEvaluationTimeOut(new TimeOut(10, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.SECONDS));
		builder.withTimeOut(new TimeOut(300, TimeUnit.SECONDS));
		builder.withNumCpus(1);

		MLPlan mlplan = new MLPlan(builder, split.get(0));
		mlplan.setRandomSeed(1);
		mlplan.setPortionOfDataForPhase2(0f);
		mlplan.setLoggerName("mlplan");

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
