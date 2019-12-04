package ai.libs.mlplan.core;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.evaluation.execution.ISupervisedLearnerMetric;

import ai.libs.jaicore.ml.core.evaluation.ClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;

public abstract class AbstractMLPlanSingleLabelBuilder<C extends IClassifier, B extends AbstractMLPlanSingleLabelBuilder<C, B>> extends AbstractMLPlanBuilder<C, B> {

	/* Default configuration values. */
	protected static final int DEFAULT_SEARCH_NUM_MC_ITERATIONS = 5;
	protected static final double DEFAULT_SEARCH_TRAIN_FOLD_SIZE = 0.7;
	protected static final int DEFAULT_SELECTION_NUM_MC_ITERATIONS = 5;
	protected static final double DEFAULT_SELECTION_TRAIN_FOLD_SIZE = 0.7;
	protected static final ISupervisedLearnerMetric DEFAULT_PERFORMANCE_MEASURE = ClassifierMetric.MEAN_ERRORRATE;

	protected AbstractMLPlanSingleLabelBuilder() {
		super();
	}

	/**
	 * Configure ML-Plan to use MCCV for the given number of iterations, train fold size and loss function in the search phase.
	 * @param numIterations The number of iterations of the MCCV.
	 * @param trainFoldSize The portion of the data that is to be used as training data.
	 * @param lossFunction The loss function to evaluate the performance of the classifier.
	 * @return The builder object.
	 */
	public B withMonteCarloCrossValidationInSearchPhase(final int numIterations, final double trainFoldSize) {
		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSearchPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory());
		}

		((MonteCarloCrossValidationEvaluatorFactory) this.getSearchEvaluatorFactory()).withNumMCIterations(numIterations).withTrainFoldSize(trainFoldSize);
		return this.getSelf();
	}

	/**
	 * Configure ML-Plan to use MCCV for the given number of iterations, train fold size and loss function in the selection phase.
	 * @param numIterations The number of iterations of the MCCV.
	 * @param trainFoldSize The portion of the data that is to be used as training data.
	 * @param lossFunction The loss function to evaluate the performance of the classifier.
	 * @return The builder object.
	 */
	public B withMonteCarloCrossValidationInSelectionPhase(final int numIterations, final double trainFoldSize) {
		if (!(this.getSelectionEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSelectionPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory());
		}

		((MonteCarloCrossValidationEvaluatorFactory) this.getSelectionEvaluatorFactory()).withNumMCIterations(numIterations).withTrainFoldSize(trainFoldSize);
		return this.getSelf();
	}
}
