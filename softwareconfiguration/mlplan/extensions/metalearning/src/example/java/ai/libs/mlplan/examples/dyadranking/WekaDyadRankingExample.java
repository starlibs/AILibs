package ai.libs.mlplan.examples.dyadranking;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.MLEvaluationUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.DyadRankedBestFirstFactory;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness;
import ai.libs.mlplan.metamining.dyadranking.WEKADyadRankedNodeQueueConfig;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;
import ai.libs.mlplan.weka.MLPlanWekaClassifier;
import ai.libs.mlplan.weka.weka.WekaMLPlanWekaClassifier;

/**
 * Demonstrated the usage of ML-Plan with a dyad ranked OPEN list
 *
 * @author Helena Graf
 *
 */
public class WekaDyadRankingExample {

	private static final Logger logger = LoggerFactory.getLogger(WekaDyadRankingExample.class);

	public static void main(final String[] args) throws Exception {
		long starttime = System.currentTimeMillis();
		WekaInstances data = new WekaInstances(OpenMLDatasetReader.deserializeDataset(40983));
		List<WekaInstances> split = SplitterUtil.getLabelStratifiedTrainTestSplit(data, 0, 0.7);

		WEKADyadRankedNodeQueueConfig openConfig = new WEKADyadRankedNodeQueueConfig();
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withSearchSpaceConfigFile(new File("resources/automl/searchmodels/weka/weka-reduced5.json")).withAlgorithmConfigFile(new File("conf/mlplan.properties"))
				.withSearchFactory(new DyadRankedBestFirstFactory<>(openConfig), new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness<>()).withPreferredNodeEvaluator(n -> 1.0);

		MLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier(builder);
		openConfig.setComponents(mlplan.getComponents());
		openConfig.setData(split.get(0).getInstances());

		try {
			long start = System.currentTimeMillis();
			mlplan.setTimeout(new Timeout(60 - (start - starttime) / 1000, TimeUnit.SECONDS));
			mlplan.buildClassifier(split.get(0).getInstances());
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			logger.info("Finished build of the classifier. Training time was {}s", trainTime);

			/* evaluate solution produced by mlplan */
			double errorRate = MLEvaluationUtil.getLossForTrainedClassifier(mlplan, split.get(1), EClassificationPerformanceMeasure.ERRORRATE);
			logger.info("Error Rate of the solution produced by ML-Plan: {}", errorRate);
		} catch (NoSuchElementException e) {
			logger.error("Building the classifier failed: {}", e.getMessage());
		}
		logger.info("Total experiment time: {}", (System.currentTimeMillis() - starttime) / 1000);
	}
}
