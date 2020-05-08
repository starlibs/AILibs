package ai.libs.mlplan.examples.rul.sklearn;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanSkLearnProblemType;
import ai.libs.mlplan.multiclass.sklearn.MLPlanSKLearnBuilder;

public class MLPlanSkLearnRulExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	public static void main(final String[] args) throws Exception {
		long start = System.currentTimeMillis();

		/* load data for segment dataset and create a train-test-split */
		File file = new File("testrsc/smallExample.arff");
		ILabeledDataset<ILabeledInstance> dataset = ArffDatasetAdapter.readDataset(file);
		LOGGER.info("Data read. Time to create dataset object was {}ms", System.currentTimeMillis() - start);

		List<ILabeledDataset<ILabeledInstance>> splits = RandomHoldoutSplitter.createSplit(dataset, 42, .7); // TODO another way to split

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlanSKLearnBuilder<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> builder = new MLPlanSKLearnBuilder<>();
		builder.withProblemType(EMLPlanSkLearnProblemType.RUL);
		builder.withNodeEvaluationTimeOut(new Timeout(60, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(45, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(3, TimeUnit.MINUTES));
		builder.withNumCpus(4);

		((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSearchPhase()).withMeasure(ERulPerformanceMeasure.ASYMMETRIC_LOSS); // TODO
		((MonteCarloCrossValidationEvaluatorFactory) builder.getLearnerEvaluationFactoryForSelectionPhase()).withMeasure(ERulPerformanceMeasure.ASYMMETRIC_LOSS); // TODO

		MLPlan<ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch>> mlplan = builder.withDataset(splits.get(0)).build();
		mlplan.setPortionOfDataForPhase2(0f);
		mlplan.setLoggerName("testedalgorithm");

		try {
			start = System.currentTimeMillis();
			ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier.");
			LOGGER.info("Training time was {}s.", trainTime);
			LOGGER.info("Chosen model is: {}", (mlplan.getSelectedClassifier()));

			/* evaluate solution produced by mlplan */
			IPredictionBatch batch = optimizedClassifier.predict(splits.get(1));
			double error = ERulPerformanceMeasure.ASYMMETRIC_LOSS.loss(splits.get(1).stream().map(i -> (double) i.getLabel()).collect(Collectors.toList()),
					batch.getPredictions().stream().map(i -> (double) i.getPrediction()).collect(Collectors.toList())); // TODO
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}. Internally believed error was {}", error, mlplan.getInternalValidationErrorOfSelectedClassifier());

		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed.", e);
		}
	}

}
