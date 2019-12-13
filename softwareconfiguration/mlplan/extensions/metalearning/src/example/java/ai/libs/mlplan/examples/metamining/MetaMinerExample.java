package ai.libs.mlplan.examples.metamining;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.MLEvaluationUtil;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.metamining.MetaMLPlan;

/**
 * Illustrates the usage of the WEKAMetaMiner.
 *
 * @author Helena Graf
 *
 */
public class MetaMinerExample {

	private static Logger logger = LoggerFactory.getLogger(MetaMinerExample.class);

	public static void main(final String[] args) throws Exception {
		// Load data for a data set and create a train-test-split
		logger.info("Load data.");
		WekaInstances data = new WekaInstances(OpenMLDatasetReader.deserializeDataset(40984));
		List<IWekaInstances> split = WekaUtil.getStratifiedSplit(data, 0, .7f);

		// Initialize meta mlplan and let it run for 2 minutes
		logger.info("Configure ML-Plan");
		MetaMLPlan metaMLPlan = new MetaMLPlan(data);
		metaMLPlan.setCPUs(4);
		metaMLPlan.setTimeOutInSeconds(60);
		metaMLPlan.setMetaFeatureSetName("all");
		metaMLPlan.setDatasetSetName("metaminer_standard");

		// Limit results to 20 pipelines so that the conversion / downloading doesn't take too long
		logger.info("Build meta components");
		StopWatch watch = new StopWatch();
		watch.start();
		metaMLPlan.buildMetaComponents(args[0], args[1], args[2], 5);
		watch.stop();
		logger.info("Find solution");
		metaMLPlan.buildClassifier(split.get(0).getInstances());

		// Evaluate solution produced by meta mlplan
		logger.info("Evaluate.");
		double score = MLEvaluationUtil.getLossForTrainedClassifier(new WekaClassifier(metaMLPlan), split.get(1), EClassificationPerformanceMeasure.ERRORRATE);
		logger.info("Error Rate of the solution produced by Meta ML-Plan: {}", score);
		logger.info("Time in Seconds: {}",watch.getTime()/1000);
	}

}