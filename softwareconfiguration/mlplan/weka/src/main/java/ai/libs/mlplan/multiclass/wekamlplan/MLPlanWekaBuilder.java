package ai.libs.mlplan.multiclass.wekamlplan;

import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
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
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;

public class MLPlanWekaBuilder extends AbstractMLPlanBuilder<IWekaClassifier, MLPlanWekaBuilder> {

	private Logger logger = LoggerFactory.getLogger(MLPlanWekaBuilder.class);

	/* DEFAULT VALUES FOR THE WEKA SETTING */
	private static final EMLPlanWekaProblemType DEF_PROBLEM_TYPE = EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS;
	private static final WekaPipelineFactory DEF_CLASSIFIER_FACTORY = new WekaPipelineFactory(DEF_PROBLEM_TYPE);

	public MLPlanWekaBuilder() throws IOException {
		super(DEF_PROBLEM_TYPE);
		this.withClassifierFactory(DEF_CLASSIFIER_FACTORY);
		this.withPipelineValidityCheckingNodeEvaluator(new WekaPipelineValidityCheckingNodeEvaluator());
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
	}

	@Override
	public MLPlanWekaBuilder withDataset(final ILabeledDataset<?> dataset) {
		if (!(dataset.getLabelAttribute() instanceof ICategoricalAttribute)) {
			throw new IllegalArgumentException("MLPlanWeka currently only support categorically labeled data!");
		}
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
		this.configureHASCOBuilder();
		this.prepareNodeEvaluatorInFactoryWithData(); // inform node evaluator about data and create the MLPlan object
		return new MLPlan4Weka(this, this.getDataset());
	}

}
