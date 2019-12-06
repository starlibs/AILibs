package ai.libs.mlplan.examples.multiclass.weka;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.algorithm.TimeOut;

import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.ClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
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
public class MLPlanOpenMLExample {

	public static void main(final String[] args) throws Exception {

		ILabeledDataset<?> ds = OpenMLDatasetReader.deserializeDataset(3);
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(ds, new Random(0), .7);

		/* initialize mlplan, and let it run for 30 seconds */
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withNodeEvaluationTimeOut(new TimeOut(10, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new TimeOut(5, TimeUnit.SECONDS));
		builder.withTimeOut(new TimeOut(30, TimeUnit.SECONDS));
		builder.withNumCpus(1);

		MLPlan<IWekaClassifier> mlplan = builder.withDataset(split.get(0)).build();
		mlplan.setRandomSeed(1);
		mlplan.setPortionOfDataForPhase2(.3f);

		try {
			long start = System.currentTimeMillis();
			IWekaClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier. Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			SupervisedLearnerExecutor<ILabeledDataset<?>> executor = new SupervisedLearnerExecutor<>();
			ILearnerRunReport report = executor.execute(optimizedClassifier, split.get(1));
			System.out.println("Error Rate of the solution produced by ML-Plan: " + ClassifierMetric.MEAN_ERRORRATE.evaluateToDouble(Arrays.asList(report)));
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}
}
