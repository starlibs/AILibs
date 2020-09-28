package ai.libs.mlplan.examples.rul.sklearn;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.sklearn.EMLPlanScikitLearnProblemType;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnRulExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws Exception {
		long start = System.currentTimeMillis();

		/* load data for segment dataset and create a train-test-split */
		File file = new File("testrsc/rul_smallExample.arff");
		ILabeledDataset<ILabeledInstance> dataset = ArffDatasetAdapter.readDataset(file);
		LOGGER.info("Data read. Time to create dataset object was {}ms", System.currentTimeMillis() - start);

		List<ILabeledDataset<ILabeledInstance>> splits = RandomHoldoutSplitter.createSplit(dataset, 42, .7);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlanScikitLearnBuilder builder = MLPlanScikitLearnBuilder.forRUL();
		builder.withProblemType(EMLPlanScikitLearnProblemType.RUL);
		builder.withNodeEvaluationTimeOut(new Timeout(60, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(45, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(3, TimeUnit.MINUTES));
		builder.withNumCpus(4);
		builder.withSeed(42);

		MLPlan<ScikitLearnWrapper<IPrediction, IPredictionBatch>> mlplan = builder.withDataset(splits.get(0)).build();
		mlplan.setLoggerName("testedalgorithm");

		try {
			start = System.currentTimeMillis();
			ScikitLearnWrapper<IPrediction, IPredictionBatch> optimizedRegressor = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier. Training time was {}s.", trainTime);
			LOGGER.info("Chosen model is: {}", (mlplan.getSelectedClassifier()));

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor executor = new SupervisedLearnerExecutor();
			ILearnerRunReport report = executor.execute(optimizedRegressor, splits.get(1));
			List<Double> expected = (List<Double>) report.getPredictionDiffList().getGroundTruthAsList();
			List<IRegressionPrediction> predicted = (List<IRegressionPrediction>) report.getPredictionDiffList().getPredictionsAsList();
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}. Internally believed error was {}", ERulPerformanceMeasure.ASYMMETRIC_LOSS.loss(expected, predicted), mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed: {}", LoggerUtil.getExceptionInfo(e));
		}

	}

}
