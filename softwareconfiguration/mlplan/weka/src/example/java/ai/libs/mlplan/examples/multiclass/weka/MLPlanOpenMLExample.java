package ai.libs.mlplan.examples.multiclass.weka;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

/**
 * This is an example class that illustrates the usage of ML-Plan on the segment dataset of OpenML. It is configured to run for 30 seconds and to use 80% of the data for search and 30% for selection in its second phase.
 *
 * @author fmohr
 *
 */
public class MLPlanOpenMLExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	public static void main(final String[] args) throws Exception {

		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(3);
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(0), .7);

		/* initialize mlplan, and let it run for 30 seconds */
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withNodeEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(5, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withNumCpus(4);
		builder.withSeed(1);
		builder.withMCCVBasedCandidateEvaluationInSearchPhase(3, .8);
		builder.withPortionOfDataReservedForSelection(.0);

		MLPlan<IWekaClassifier> mlplan = builder.withDataset(split.get(0)).build();
		mlplan.setRandomSeed(1);
		mlplan.setLoggerName("example");

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
