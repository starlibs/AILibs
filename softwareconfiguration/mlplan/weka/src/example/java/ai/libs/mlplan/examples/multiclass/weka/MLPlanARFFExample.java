package ai.libs.mlplan.examples.multiclass.weka;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class MLPlanARFFExample {

	private static final Logger LOGGER = LoggerFactory.getLogger("example");

	public static void main(final String[] args) throws Exception {

		/* load data for segment dataset and create a train-test-split */
		long start = System.currentTimeMillis();
		File file = new File("testrsc/waveform.arff");
		Instances data = new Instances(new FileReader(file));
		LOGGER.info("Data read. Time to create dataset object was {}ms", System.currentTimeMillis() - start);
		data.setClassIndex(data.numAttributes() - 1);
		List<IWekaInstances> split = WekaUtil.getStratifiedSplit(new WekaInstances(data), 0, .7f);

		/* initialize mlplan with a tiny search space, and let it run for 30 seconds */
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withNodeEvaluationTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withCandidateEvaluationTimeOut(new Timeout(10, TimeUnit.SECONDS));
		builder.withTimeOut(new Timeout(30, TimeUnit.SECONDS));
		builder.withNumCpus(4);

		MLPlan<IWekaClassifier> mlplan = builder.withDataset(split.get(0)).build();
		mlplan.setPortionOfDataForPhase2(0f);
		mlplan.setLoggerName("testedalgorithm");

		try {
			start = System.currentTimeMillis();
			Classifier optimizedClassifier = mlplan.call().getClassifier();
			long trainTime = (int) (System.currentTimeMillis() - start) / 1000;
			LOGGER.info("Finished build of the classifier.");
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Chosen model is: {}", (mlplan.getSelectedClassifier()));
			}
			LOGGER.info("Training time was {}s.", trainTime);

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(split.get(0).getInstances());
			eval.evaluateModel(optimizedClassifier, split.get(1).getInstances());
			LOGGER.info("Error Rate of the solution produced by ML-Plan: {}. Internally believed error was {}", ((100 - eval.pctCorrect()) / 100f), mlplan.getInternalValidationErrorOfSelectedClassifier());
		} catch (NoSuchElementException e) {
			LOGGER.error("Building the classifier failed.", e);
		}
	}

}
