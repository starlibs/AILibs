package ai.libs.mlplan.examples.multiclass.weka;

import java.io.File;
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
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

public class MLPlanARFFExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	private static final String DATASET_PATH = "testrsc/waveform.arff";

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		long start = System.currentTimeMillis();
		ILabeledDataset<?> ds = ArffDatasetAdapter.readDataset(new File(DATASET_PATH));
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(42), .7);
		LOGGER.info("Data read. Time to create dataset object was {}ms", System.currentTimeMillis() - start);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlan<IWekaClassifier> mlplan = new MLPlanWekaBuilder().withNumCpus(4).withTimeOut(new Timeout(60, TimeUnit.SECONDS)).withDataset(split.get(0)).build();
		mlplan.setLoggerName("testedalgorithm");

		try {
			start = System.currentTimeMillis();
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
