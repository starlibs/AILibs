package ai.libs.automl.mlplan.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.Timeout;

import ai.libs.automl.AutoMLAlgorithmResultProductionTester;
import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.SimpleRandomSamplingFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.ipl.InversePowerLawExtrapolationMethod;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlanWithLearningCurvePredictionResultDeliveryTester extends AutoMLAlgorithmResultProductionTester {

	@Override
	public IAlgorithm<ILabeledDataset<?>, IWekaClassifier> getAutoMLAlgorithm(final ILabeledDataset<?> data) throws AlgorithmCreationException {
		try {
			this.logger.info("Creating ML-Plan instance.");
			MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
			builder.withLearningCurveExtrapolationEvaluation(new int[] { 8, 16, 64, 128 }, new SimpleRandomSamplingFactory<>(), .7, new InversePowerLawExtrapolationMethod());
			builder.withNodeEvaluationTimeOut(new Timeout(15, TimeUnit.MINUTES));
			builder.withCandidateEvaluationTimeOut(new Timeout(5, TimeUnit.MINUTES));
			MLPlan<IWekaClassifier> mlplan = builder.withDataset(data).build();
			mlplan.setRandomSeed(1);
			mlplan.setPortionOfDataForPhase2(.3f);
			mlplan.setNumCPUs(2);
			this.logger.info("Done");
			return mlplan;
		} catch (IOException e) {
			throw new AlgorithmCreationException(e);
		}
	}
}
