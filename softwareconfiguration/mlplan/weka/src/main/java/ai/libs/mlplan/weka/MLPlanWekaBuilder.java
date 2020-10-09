package ai.libs.mlplan.weka;

import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearningCurveExtrapolationEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;

public class MLPlanWekaBuilder extends AMLPlanBuilder<IWekaClassifier, MLPlanWekaBuilder> {

	private Logger logger = LoggerFactory.getLogger(MLPlanWekaBuilder.class);

	public static MLPlanWekaBuilder forClassification() throws IOException {
		return new MLPlanWekaBuilder(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS);
	}

	public static MLPlanWekaBuilder forRegression() throws IOException {
		return new MLPlanWekaBuilder(EMLPlanWekaProblemType.REGRESSION);
	}

	public static MLPlanWekaBuilder forClassificationWithTinySearchSpace() throws IOException {
		return new MLPlanWekaBuilder(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS_TINY);
	}

	public MLPlanWekaBuilder() throws IOException {
		this(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS);
	}

	public MLPlanWekaBuilder(final EMLPlanWekaProblemType problemType) throws IOException {
		super(problemType);
	}

	/**
	 * Allows to use learning curve extrapolation for predicting the quality of candidate solutions.
	 * @param anchorpoints The anchor points for which samples are actually evaluated on the respective data.
	 * @param subsamplingAlgorithmFactory The factory for the sampling algorithm that is to be used to randomly draw training instances.
	 * @param trainSplitForAnchorpointsMeasurement The training fold size for measuring the acnhorpoints.
	 * @param extrapolationMethod The method to be used in order to extrapolate the learning curve from the anchorpoints.
	 */
	public void withLearningCurveExtrapolationEvaluation(final int[] anchorpoints, final ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		this.withSearchPhaseEvaluatorFactory(new LearningCurveExtrapolationEvaluatorFactory(anchorpoints, subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod));
		this.withSelectionPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(3).withTrainFoldSize(.7));
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, "" + 10);
		throw new UnsupportedOperationException("Learning Curve Prediction based ML-Plan runs are not supported in this release. They will be activated again in the upcoming release.");
	}

	@Override
	public MLPlanWekaBuilder withDataset(final ILabeledDataset<?> dataset) {
//		if (!(dataset.getLabelAttribute() instanceof ICategoricalAttribute)) {
//			throw new IllegalArgumentException("MLPlanWeka currently only support categorically labeled data!");
//		}
		WekaInstances instances = dataset instanceof WekaInstances ? (WekaInstances) dataset : new WekaInstances(dataset);
		super.withDataset(instances);
		this.logger.info("Setting dataset as WekaInstances object.");
		return this.getSelf();
	}

	@Override
	public MLPlanWekaBuilder getSelf() {
		return this;
	}

	@Override
	public MLPlan4Weka build() {
		this.checkPreconditionsForInitialization();
		return new MLPlan4Weka(this, this.getDataset());
	}

}
