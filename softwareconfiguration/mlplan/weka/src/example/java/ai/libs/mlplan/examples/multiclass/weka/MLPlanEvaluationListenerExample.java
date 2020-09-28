package ai.libs.mlplan.examples.multiclass.weka;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.evaluation.evaluator.events.TrainTestSplitEvaluationFailedEvent;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.TimeTrackingLearnerWrapper;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

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
		builder.withNodeEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(5, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withDataset(split.get(0));

		MLPlan<IWekaClassifier> mlplan = builder.build();
		mlplan.setLoggerName("testedalgorithm");

		/* register a listener  */
		mlplan.registerListener(new Object() {
			@Subscribe
			public void receiveEvent(final ClassifierFoundEvent event) {
				WekaClassifier learner = (WekaClassifier) event.getSolutionCandidate();
				LOGGER.info("Received successful evaluation with error={} within {}ms for pipeline: {}", event.getInSampleError(), event.getTimeToEvaluate(), learner.toString());
			}

			@Subscribe
			public void receiveEvent(final TrainTestSplitEvaluationFailedEvent<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> event) {
				WekaClassifier learner = null;
				if (event.getLearner() instanceof TimeTrackingLearnerWrapper) {
					TimeTrackingLearnerWrapper wrapper = ((TimeTrackingLearnerWrapper) event.getLearner());
					learner = (WekaClassifier) wrapper.getLearner();
				}
				if (learner != null) {
					ILearnerRunReport report = event.getReport();
					String exceptionStackTrace = ExceptionUtils.getStackTrace(report.getException());
					long candidateRuntime = (report.getTrainEndTime() - report.getTrainStartTime()) + (report.getTestEndTime() - report.getTestStartTime());
					LOGGER.info("Received failed evaluation within {}ms for pipeline: {}", candidateRuntime, learner.getClassifier(), exceptionStackTrace);
				}
			}
		});

		try {
			long start = System.currentTimeMillis();
			IWekaClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier. Training time was {}s.", trainTime);
			LOGGER.info("Chosen model is: {}", (mlplan.getSelectedClassifier()));

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}. Internally believed error was {}",
					EClassificationPerformanceMeasure.ERRORRATE.loss(report.getPredictionDiffList().getCastedView(Integer.class, ISingleLabelClassification.class)), mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed: {}", LoggerUtil.getExceptionInfo(e));
		}
	}
}
