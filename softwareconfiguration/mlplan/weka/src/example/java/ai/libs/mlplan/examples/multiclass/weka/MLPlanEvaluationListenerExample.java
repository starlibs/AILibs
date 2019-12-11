package ai.libs.mlplan.examples.multiclass.weka;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.TimeOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.ClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.evaluation.evaluator.events.TrainTestSplitEvaluationCompletedEvent;
import ai.libs.jaicore.ml.core.evaluation.evaluator.events.TrainTestSplitEvaluationFailedEvent;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

/**
 * This is an example class that illustrates the usage of ML-Plan on the segment dataset of OpenML. It is configured to run for 30 seconds and to use 70% of the data for search and 30% for selection in its second phase.
 *
 * The API key used for OpenML is ML-Plan's key (read only).
 *
 * @author fmohr
 *
 */
public class MLPlanEvaluationListenerExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	public static void main(final String[] args) throws Exception {

		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(60);
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(0), .7);

		/* initialize mlplan */
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withNodeEvaluationTimeOut(new TimeOut(10, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.SECONDS));
		builder.withTimeOut(new TimeOut(2, TimeUnit.MINUTES));
		MLPlan<IWekaClassifier> mlplan = builder.withDataset(split.get(0)).build();

		/* register a listener  */
		mlplan.registerListener(new Object() {

			@Subscribe
			public void receiveEvent(final TrainTestSplitEvaluationFailedEvent e) { // this event is fired whenever any pipeline is evaluated successfully
				MLPipeline pipeline = ((MLPipeline)((WekaClassifier)e.getLearner()).getClassifier());
				LOGGER.info("Received exception for learner {}: {}", pipeline, e.getReport().getException().getClass().getName());
			}

			@Subscribe
			public void receiveEvent(final TrainTestSplitEvaluationCompletedEvent e) { // this event is fired whenever any pipeline is evaluated successfully
				double errorRate = ClassifierMetric.MEAN_ERRORRATE.evaluate(Arrays.asList(e.getReport()));
				MLPipeline pipeline = ((MLPipeline)((WekaClassifier)e.getLearner()).getClassifier());
				LOGGER.info("Received single evaluation error rate for learner {} is {}", pipeline, errorRate);
			}
		});

		try {
			long start = System.currentTimeMillis();
			IWekaClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier. Training time was {}s.", trainTime);

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}", ClassifierMetric.MEAN_ERRORRATE.evaluateToDouble(Arrays.asList(report)));
		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed: {}", LoggerUtil.getExceptionInfo(e));
		}
	}
}
