package ai.libs.mlplan.examples.multiclass.sklearn;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnARFFExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		long start = System.currentTimeMillis();
		File file = new File("testrsc/waveform.arff");
		ILabeledDataset<ILabeledInstance> dataset = ArffDatasetAdapter.readDataset(file);

		LOGGER.info("Data read. Time to create dataset object was {}ms", System.currentTimeMillis() - start);
		List<ILabeledDataset<ILabeledInstance>> split = RandomHoldoutSplitter.createSplit(dataset, 42, .7);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlanScikitLearnBuilder builder = MLPlanScikitLearnBuilder.forClassification();
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(3, TimeUnit.MINUTES));
		builder.withNumCpus(4);
		builder.withPortionOfDataReservedForSelection(0); // disable selection

		MLPlan<ScikitLearnWrapper<IPrediction, IPredictionBatch>> mlplan = builder.withDataset(split.get(0)).build();
		mlplan.setLoggerName("testedalgorithm");

		try {
			start = System.currentTimeMillis();
			ScikitLearnWrapper<IPrediction, IPredictionBatch> optimizedClassifier = mlplan.call();
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
