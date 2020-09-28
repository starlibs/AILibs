package ai.libs.mlplan.examples.multiclass.weka;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.Timeout;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.MLEvaluationUtil;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.AFileSamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling.ClassStratiFileAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling.StratifiedFileSampling;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

public class MLPlanSubsamplingExample {

	public static void main(final String[] args) throws Exception {

		/* create a subsample of the input with 1000 datapoints */
		File file = new File("testrsc/car.arff");
		File sampleFile = new File("testrsc/car_sample.arff");
		sampleFile.deleteOnExit();
		AFileSamplingAlgorithm samplingAlgorithm = new StratifiedFileSampling(new Random(1l), new ClassStratiFileAssigner(), file);
		samplingAlgorithm.setSampleSize(1000);
		samplingAlgorithm.setOutputFileName(sampleFile.getAbsolutePath());
		samplingAlgorithm.call();

		/* create a train-test-split */
		ILabeledDataset<?> data = ArffDatasetAdapter.readDataset(sampleFile);
		List<ILabeledDataset<?>> split = SplitterUtil.getLabelStratifiedTrainTestSplit(data, 0, .7);

		/* build and run ml-plan */
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withNumCpus(1);
		MLPlan<IWekaClassifier> mlplan = builder.withDataset(split.get(0)).build();
		mlplan.setPortionOfDataForPhase2(0f);
		mlplan.setLoggerName("mlplan");

		try {
			long start = System.currentTimeMillis();
			IClassifier optimizedClassifier = mlplan.call();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			System.out.println("Finished build of the classifier.");
			System.out.println("Chosen model is: " + mlplan.getSelectedClassifier());
			System.out.println("Training time was " + trainTime + "s.");

			/* evaluate solution produced by mlplan */
			double errorRate = MLEvaluationUtil.getLossForTrainedClassifier(optimizedClassifier, split.get(1), EClassificationPerformanceMeasure.ERRORRATE);
			System.out.println("Error Rate of the solution produced by ML-Plan: " + errorRate + ". Internally believed error was " + mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			System.out.println("Building the classifier failed: " + e.getMessage());
		}
	}

}
