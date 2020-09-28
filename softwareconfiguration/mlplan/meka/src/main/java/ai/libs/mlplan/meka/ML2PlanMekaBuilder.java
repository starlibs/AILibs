package ai.libs.mlplan.meka;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.core.IProblemType;

public class ML2PlanMekaBuilder extends AMLPlanBuilder<IMekaClassifier, ML2PlanMekaBuilder> {

	public ML2PlanMekaBuilder() throws IOException {
		this (EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL);
	}

	public ML2PlanMekaBuilder(final IProblemType<IMekaClassifier> problemType) throws IOException {
		super(problemType);
	}

	/**
	 * Configures ML-Plan with the configuration as compared to AutoMEKA_GGP and GA-Auto-MLC.
	 * @return The builder object.
	 */
	public ML2PlanMekaBuilder withAutoMEKADefaultConfiguration() {
		this.withPerformanceMeasure(new AutoMEKAGGPFitnessMeasureLoss());
		return this;
	}

	/**
	 * Sets the performance measure to evaluate a candidate solution's generalization performance. Caution: This resets the evaluators to MCCV for both search and selection phase if these are not already MCCVs.
	 * @param lossFunction The loss function to be used.
	 * @return The builder object.
	 */
	public ML2PlanMekaBuilder withPerformanceMeasure(final IMultiLabelClassificationPredictionPerformanceMeasure measure) {
		List<ISupervisedLearnerEvaluatorFactory> phaseList = Arrays.asList(this.getSearchEvaluatorFactory(), this.getSelectionEvaluatorFactory());
		for (ISupervisedLearnerEvaluatorFactory factory : phaseList) {
			if (factory instanceof MonteCarloCrossValidationEvaluatorFactory) {
				((MonteCarloCrossValidationEvaluatorFactory) factory).withMeasure(measure);
			}
		}
		return this;
	}

	@Override
	public ML2PlanMekaBuilder getSelf() {
		return this;
	}

}
