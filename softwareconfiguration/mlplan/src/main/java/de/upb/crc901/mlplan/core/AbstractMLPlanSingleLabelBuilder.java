package de.upb.crc901.mlplan.core;

import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.factory.MonteCarloCrossValidationEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import jaicore.ml.weka.dataset.splitter.MulticlassClassStratifiedSplitter;

public abstract class AbstractMLPlanSingleLabelBuilder extends AbstractMLPlanBuilder {

	/* Default configuration values. */
	protected static final int SEARCH_NUM_MC_ITERATIONS = 5;
	protected static final double SEARCH_TRAIN_FOLD_SIZE = 0.7;
	protected static final int SELECTION_NUM_MC_ITERATIONS = 5;
	protected static final double SELECTION_TRAIN_FOLD_SIZE = 0.7;
	protected static final IMeasure<Double, Double> LOSS_FUNCTION = new ZeroOneLoss();

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
	public AbstractMLPlanSingleLabelBuilder withMonteCarloCrossValidationInSearchPhase(final int numIterations, final double trainFoldSize, final IMeasure<Double, Double> lossFunction) {
		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSearchPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(new MulticlassClassStratifiedSplitter()));
		}

		((MonteCarloCrossValidationEvaluatorFactory) this.getSearchEvaluatorFactory()).withNumMCIterations(numIterations).withTrainFoldSize(trainFoldSize).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(lossFunction));
		return this;
	}

	/**
	 * Configure ML-Plan to use MCCV for the given number of iterations, train fold size and loss function in the selection phase.
	 * @param numIterations The number of iterations of the MCCV.
	 * @param trainFoldSize The portion of the data that is to be used as training data.
	 * @param lossFunction The loss function to evaluate the performance of the classifier.
	 * @return The builder object.
	 */
	public AbstractMLPlanSingleLabelBuilder withMonteCarloCrossValidationInSelectionPhase(final int numIterations, final double trainFoldSize, final IMeasure<Double, Double> lossFunction) {
		if (!(this.getSelectionEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSelectionPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(new MulticlassClassStratifiedSplitter()));
		}

		((MonteCarloCrossValidationEvaluatorFactory) this.getSelectionEvaluatorFactory()).withNumMCIterations(numIterations).withTrainFoldSize(trainFoldSize).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(lossFunction));
		return this;
	}

	/**
	 * Sets the performance measure to evaluate a candidate solution's generalization performance. Caution: This resets the evaluators to MCCV for both search and selection phase if these are not already MCCVs.
	 * @param lossFunction The loss function to be used.
	 * @return The builder object.
	 */
	public AbstractMLPlanSingleLabelBuilder withPerformanceMeasure(final IMeasure<Double, Double> lossFunction) {
		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSearchPhaseEvaluatorFactory(
					new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(new MulticlassClassStratifiedSplitter()).withNumMCIterations(SEARCH_NUM_MC_ITERATIONS).withTrainFoldSize(SEARCH_TRAIN_FOLD_SIZE));
		}
		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSearchPhaseEvaluatorFactory(
					new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(new MulticlassClassStratifiedSplitter()).withNumMCIterations(SELECTION_NUM_MC_ITERATIONS).withTrainFoldSize(SELECTION_TRAIN_FOLD_SIZE));
		}

		((MonteCarloCrossValidationEvaluatorFactory) this.getSelectionEvaluatorFactory()).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(lossFunction));
		return this;
	}

	protected IDatasetSplitter getDefaultDatasetSplitter() {
		return new MulticlassClassStratifiedSplitter();
	}
}
